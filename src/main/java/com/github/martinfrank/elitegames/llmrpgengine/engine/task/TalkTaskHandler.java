package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.*;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.LocationCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.PersonCondition;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TalkAgent;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TalkContext;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TalkResponse;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TaskType;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import com.github.martinfrank.elitegames.llmrpgengine.session.ChatEntry;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.session.StringNormalizer;
import com.github.martinfrank.elitegames.llmrpgengine.session.TalkEntry;
import com.github.martinfrank.elitegames.llmrpgengine.util.Levenshtein;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles the player addressing and communicating with the person resolved from
 * {@link Verdict#targetUuid()} (an id from the available-persons list). If the verdict
 * carries no resolvable person id, nothing happens.
 * <p>
 * The conversation topic is taken from {@link Verdict#dialogUuid()}: if it resolves to a
 * known {@link Dialog}, the player talks about that scripted dialog; otherwise the player
 * only makes small talk (gossip). Either way the {@link TalkAgent} produces the person's
 * in-character reply, which is recorded in the talk- and chat-history.
 * <p>
 * If the agent cannot deliver a usable reply at all, the turn is degraded to a narrated mishap
 * rather than propagated as an error – see {@link #converse}.
 */
@Component
public class TalkTaskHandler implements TaskHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TalkTaskHandler.class);

    private static final int TALK_HISTORY_LENGTH = 5;
    private static final int CHAT_HISTORY_LENGTH = 5;

    /**
     * How far a reported trigger id may be (in edit distance) from a real dialog trigger id and
     * still be accepted as that trigger. Ids (UUIDs) are far apart, so a small threshold recovers
     * LLM typos without risking a wrong match.
     */
    private static final int MAX_TRIGGER_ID_DISTANCE = 2;

    /** Whereabouts of a person no chapter condition currently places anywhere. */
    private static final String UNKNOWN_WHEREABOUTS = "unbekannt";

    /**
     * In-character excuses for a conversation turn the {@link TalkAgent} could not deliver (see
     * {@link #converse}). They keep the mishap inside the fiction instead of showing the player a
     * technical error, and they nudge towards asking again. {@code %s} is the person's name.
     */
    private static final List<String> FAILED_REPLY_NARRATIONS = List.of(
            "%s setzt zu einer Antwort an, verliert mitten im Satz den Faden und blickt euch ratlos an. Fragt am besten noch einmal.",
            "%s beginnt zu sprechen, doch ein plötzlicher Hustenanfall verschluckt jedes Wort. Was gesagt werden sollte, bleibt ungesagt.",
            "%s murmelt etwas so undeutlich in sich hinein, dass ihr nicht das Geringste versteht. Ein zweiter Versuch könnte helfen.",
            "%s redet und redet – bis euch dämmert, dass in dem ganzen Wortschwall keine einzige Antwort steckte.",
            "%s holt tief Luft, um euch alles zu erklären, und hat dann offenbar vergessen, was die Frage war.");

    private final Random random = new Random();

    private final TalkAgent talkAgent;

    public TalkTaskHandler(TalkAgent talkAgent) {
        this.talkAgent = talkAgent;
    }

    @Override
    public TaskType type() {
        return TaskType.TALK;
    }

    @Override
    public void execute(Verdict verdict, Session session) {
        Optional<UUID> personId = verdict.targetUuid();
        if (personId.isEmpty()) {
            LOGGER.info("No known conversation partner for TALK: '{}' (id: {})", verdict.target(), verdict.targetId());
            return;
        }
        Person person = session.getPerson(personId.get());
        if (person == null) {
            LOGGER.info("No known conversation partner for TALK: '{}' (id: {})", verdict.target(), verdict.targetId());
            return;
        }

        Dialog dialog = resolveDialog(verdict, session, person);
        if (dialog != null) {
            LOGGER.debug("Player talks to {} about dialog '{}'", person.name(), dialog.topic());
        } else {
            LOGGER.debug("Player makes small talk (gossip) with {}", person.name());
        }
        converse(session, person, dialog);
    }

    /**
     * Resolves the scripted dialog the player's input matched, or {@code null} when no dialog
     * matched (small talk / gossip).
     * <p>
     * Guardrail: the dialog id is only accepted when it belongs to <em>this</em> person's
     * available dialogs. A verdict that points at a dialog the person cannot talk about (e.g.
     * another person's dialog) falls back to gossip instead of using a foreign dialog.
     */
    private Dialog resolveDialog(Verdict verdict, Session session, Person person) {
        Optional<UUID> dialogId = verdict.dialogUuid();
        if (dialogId.isEmpty()) {
            return null;
        }
        return session.getAvailableDialogs(person).stream()
                .filter(d -> d.id().equals(dialogId.get()))
                .findFirst()
                .orElseGet(() -> {
                    LOGGER.info("Guardrail: dialog id {} ('{}') does not belong to {} -> gossip",
                            verdict.dialogId(), verdict.dialogTopic(), person.name());
                    return null;
                });
    }

    /**
     * Runs one exchange with the person and records it.
     * <p>
     * Guardrail: a local model can fail to deliver a usable answer – most often a reply that runs
     * out of context mid-JSON and thus cannot be parsed. That is a normal operating condition, not
     * a programming error, so it must never end the game. The turn is degraded instead: the player
     * gets a narrator line explaining in-fiction why no answer arrived (see
     * {@link #FAILED_REPLY_NARRATIONS}) and can simply ask again. Nothing is written to the talk
     * history, so the failed turn leaves no trace in the person's memory of the conversation.
     */
    private void converse(Session session, Person person, Dialog dialog) {
        String statement = session.chatHistory.getLatestEntries(1).getFirst().statement();
        TalkContext context = buildContext(session, person, dialog, statement);
//        LOGGER.debug(context.toString());
        long now = System.currentTimeMillis();
        TalkResponse response;
        try {
            response = talkAgent.talk(context);
        } catch (RuntimeException e) {
            LOGGER.warn("Talk agent delivered no usable reply for {} -> narrating the mishap: {}",
                    person.name(), e.toString());
            session.chatHistory.narrator(failedReplyNarration(person)+" (Entwicklerhinweis: die Verarbeitung vom LLM ist fehlgeschlagen, bitte erneut versuchen)");
            return;
        }
        long duration = System.currentTimeMillis() - now;
        LOGGER.info("Duration talk evaluation: {} ms", duration);

        // Guardrail: resolve the (possibly LLM-mangled) reported trigger ids to real dialog
        // triggers. Applying them (flags/knowledge) happens elsewhere.
        List<Trigger> triggers = resolveTriggers(dialog, response);
        handleTalkTriggers(session, triggers);

        // Record both sides in the per-person talk history and surface the reply in the game log,
        // attributed to the person who said it (not to the narrator).
        String reply = response.reply();
        session.talkHistory.player(person.id(), statement);
        session.talkHistory.npc(person.id(), reply);
        session.chatHistory.npc(person, reply);
    }

    private void handleTalkTriggers(Session session, List<Trigger> triggers) {
        List<Trigger> unTriggers = session.sessionTriggers.untriggered( triggers );
        for(Trigger trigger : unTriggers) {
            LOGGER.debug("execute trigger: {}", trigger.trigger());
            session.handleEvent(trigger.event());
        }
    }

    /** A random excuse from {@link #FAILED_REPLY_NARRATIONS}, so repeated mishaps do not read alike. */
    private String failedReplyNarration(Person person) {
        String template = FAILED_REPLY_NARRATIONS.get(random.nextInt(FAILED_REPLY_NARRATIONS.size()));
        return template.formatted(person.name());
    }

    /**
     * Guardrail 3: maps the triggers the agent reported onto the real {@link Trigger}s of
     * the dialog. Instead of rigorously discarding an id that does not match exactly, the closest
     * dialog trigger by {@link Levenshtein} distance wins, as long as it is within
     * {@link #MAX_TRIGGER_ID_DISTANCE}. This recovers ids the model got slightly wrong (a mangled
     * UUID) while still rejecting invented ones (which are far from every candidate).
     */
    private List<Trigger> resolveTriggers(Dialog dialog, TalkResponse response) {
        if (dialog == null || response.triggeredTriggers().isEmpty()) {
            return List.of();
        }
        List<Trigger> candidates = dialog.knowledgeTriggers();
        List<Trigger> resolved = new ArrayList<>();
        for (TalkResponse.TriggeredTrigger reported : response.triggeredTriggers()) {
            Trigger match = closestTrigger(reported.triggerId(), candidates);
            if (match == null) {
                LOGGER.info("Guardrail: reported trigger id '{}' ('{}') matches no dialog trigger -> ignored",
                        reported.triggerId(), reported.trigger());
            } else if (!resolved.contains(match)) {
                resolved.add(match);
            }
        }
        return resolved;
    }

    private static Trigger closestTrigger(String reportedId, List<Trigger> candidates) {
        if (reportedId == null || reportedId.isBlank()) {
            return null;
        }
        String needle = reportedId.strip();
        Trigger best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (Trigger candidate : candidates) {
            int distance = Levenshtein.distance(needle, candidate.id().toString());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return bestDistance <= MAX_TRIGGER_ID_DISTANCE ? best : null;
    }

    private TalkContext buildContext(Session session, Person person, Dialog dialog, String statement) {
        String talkTo = person.name() + " (Beschreibung: " + StringNormalizer.normalize(person.description()) + ")";
        String location = session.getCurrentLocation().name();
        String chatHistory = session.chatHistory.getLatestEntries(CHAT_HISTORY_LENGTH).stream()
                .map(ChatEntry::toString)
                .collect(Collectors.joining("\n"));
        String talkHistory = session.talkHistory.getTalk(person.id(), TALK_HISTORY_LENGTH).stream()
                .map(TalkEntry::toString)
                .collect(Collectors.joining("\n"));
        String commonKnowledge = createCommonKnowledge(session);

        String primaryDialog = "";
        String triggers = "";
        if (dialog != null) {
            primaryDialog = "Thema: " + dialog.topic()
                    + "\nZusammenfassung: " + StringNormalizer.normalize(dialog.summary())
                    + "\nKontext: " + StringNormalizer.normalize(dialog.context());
            triggers = dialog.knowledgeTriggers().stream()
                    .map(t -> "TriggerThema: " + t.trigger() + " (id: " + t.id() + ")")
                    .collect(Collectors.joining("\n"));
        }

        return new TalkContext(talkTo, location, statement, primaryDialog, triggers, talkHistory, chatHistory, commonKnowledge);
    }

    /**
     * What the person knows about the village: its people (with their current whereabouts) and its
     * places. This is the figure's authoritative source for names and locations – without it the
     * agent can only invent a smith when the player asks for one.
     * <p>
     * All texts go through {@link StringNormalizer} because the adventure authors them as wrapped
     * text blocks; unnormalized they would arrive in the prompt broken mid-sentence.
     */
    private String createCommonKnowledge(Session session) {
        StringBuilder commonKnowledge = new StringBuilder("BEKANNTE PERSONEN:\n");
        currentWhereabouts(session).forEach((person, whereabouts) ->
                commonKnowledge.append(" - ").append(person.name())
                        .append(": PERSOENLICHKEIT=").append(StringNormalizer.normalize(person.personality()))
                        .append(" BESCHREIBUNG=").append(StringNormalizer.normalize(person.description()))
                        .append(" ROLLE=").append(StringNormalizer.normalize(person.role()))
                        .append(" AUFENTHALTSORT=").append(whereabouts)
                        .append("\n"));
        commonKnowledge.append("\n");

        commonKnowledge.append("BEKANNTE ORTE:\n");
        for (LocationCondition condition : session.getCurrentChapter().locationConditions()) {
            if (holds(session, condition.condition())) {
                commonKnowledge.append(" - ").append(condition.location().name());
                commonKnowledge.append(": BESCHREIBUNG=").append(StringNormalizer.normalize(condition.location().description()));
                commonKnowledge.append("\n");
            }
        }
        return commonKnowledge.toString();
    }

    /**
     * Every person of the chapter mapped to where they are right now, in chapter order.
     * <p>
     * A chapter holds one {@link PersonCondition} per (person, location, condition) triple, because
     * the same figure is somewhere else at another time of day. Listing those triples directly puts
     * a person into the prompt several times with contradicting whereabouts – once at their current
     * location, once as "unknown" for every condition that does not hold. So they are collapsed to
     * the one location whose condition currently holds; a person no condition covers stays
     * {@value #UNKNOWN_WHEREABOUTS}, which the person still knows about but cannot currently locate.
     */
    private Map<Person, String> currentWhereabouts(Session session) {
        Map<Person, String> whereabouts = new LinkedHashMap<>();
        for (PersonCondition condition : session.getCurrentChapter().personConditions()) {
            String known = whereabouts.get(condition.person());
            if (known != null && !UNKNOWN_WHEREABOUTS.equals(known)) {
                continue; // already located by an earlier condition
            }
            whereabouts.put(condition.person(),
                    holds(session, condition.condition()) ? condition.location().name() : UNKNOWN_WHEREABOUTS);
        }
        return whereabouts;
    }

    /** Evaluates a chapter condition against the session's current flag values. */
    private static boolean holds(Session session, Condition condition) {
        return session.evaluate(condition);
    }
}
