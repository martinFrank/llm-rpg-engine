package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Dialog;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.KnowledgeTrigger;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Person;
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
import java.util.List;
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

        long now = System.currentTimeMillis();
        TalkResponse response;
        try {
            response = talkAgent.talk(context);
        } catch (RuntimeException e) {
            LOGGER.warn("Talk agent delivered no usable reply for {} -> narrating the mishap: {}",
                    person.name(), e.toString());
            session.chatHistory.narrator(failedReplyNarration(person));
            return;
        }
        long duration = System.currentTimeMillis() - now;
        LOGGER.info("Duration talk evaluation: {} ms", duration);

        // Guardrail: resolve the (possibly LLM-mangled) reported trigger ids to real dialog
        // triggers. Applying them (flags/knowledge) happens elsewhere.
        List<KnowledgeTrigger> triggers = resolveTriggers(dialog, response);
        LOGGER.debug("Resolved triggers: {}", triggers.stream().map(KnowledgeTrigger::id).toList());

        // Record both sides in the per-person talk history and surface the reply in the game log,
        // attributed to the person who said it (not to the narrator).
        String reply = response.reply();
        session.talkHistory.player(person.id(), statement);
        session.talkHistory.npc(person.id(), reply);
        session.chatHistory.npc(person, reply);
    }

    /** A random excuse from {@link #FAILED_REPLY_NARRATIONS}, so repeated mishaps do not read alike. */
    private String failedReplyNarration(Person person) {
        String template = FAILED_REPLY_NARRATIONS.get(random.nextInt(FAILED_REPLY_NARRATIONS.size()));
        return template.formatted(person.name());
    }

    /**
     * Guardrail 3: maps the triggers the agent reported onto the real {@link KnowledgeTrigger}s of
     * the dialog. Instead of rigorously discarding an id that does not match exactly, the closest
     * dialog trigger by {@link Levenshtein} distance wins, as long as it is within
     * {@link #MAX_TRIGGER_ID_DISTANCE}. This recovers ids the model got slightly wrong (a mangled
     * UUID) while still rejecting invented ones (which are far from every candidate).
     */
    private List<KnowledgeTrigger> resolveTriggers(Dialog dialog, TalkResponse response) {
        if (dialog == null || response.triggeredTriggers().isEmpty()) {
            return List.of();
        }
        List<KnowledgeTrigger> candidates = dialog.knowledgeTriggers();
        List<KnowledgeTrigger> resolved = new ArrayList<>();
        for (TalkResponse.TriggeredTrigger reported : response.triggeredTriggers()) {
            KnowledgeTrigger match = closestTrigger(reported.triggerId(), candidates);
            if (match == null) {
                LOGGER.info("Guardrail: reported trigger id '{}' ('{}') matches no dialog trigger -> ignored",
                        reported.triggerId(), reported.trigger());
            } else if (!resolved.contains(match)) {
                resolved.add(match);
            }
        }
        return resolved;
    }

    private static KnowledgeTrigger closestTrigger(String reportedId, List<KnowledgeTrigger> candidates) {
        if (reportedId == null || reportedId.isBlank()) {
            return null;
        }
        String needle = reportedId.strip();
        KnowledgeTrigger best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (KnowledgeTrigger candidate : candidates) {
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private String createCommonKnowledge(Session session) {
        StringBuilder commonKnowledge = new StringBuilder("BEKANNTE PERSONEN:\n");
        for(PersonCondition condition: session.getCurrentChapter().personConditions()){
            commonKnowledge.append(" - ").append(condition.person().name())
                    .append(": PERSOENLICHKEIT=").append(condition.person().personality())
                    .append(" BESCHREIBUNG=").append(condition.person().description())
                    .append(" ROLLE=").append(condition.person().role());
            List consideredFlags = condition.condition().consideredFlags();
            List currentFlags = session.sessionFlags.getFlags(consideredFlags);
            String ort = (condition.condition().evaluate(currentFlags)) ? condition.location().name() : "unbekannt";
            commonKnowledge.append(" AUFENTHALSORT=").append(ort);
            commonKnowledge.append("\n");
        }
        commonKnowledge.append("\n");

        commonKnowledge.append("BEKANNTE ORTE:\n");
        for (LocationCondition condition: session.getCurrentChapter().locationConditions()){
            List consideredFlags = condition.condition().consideredFlags();
            List currentFlags = session.sessionFlags.getFlags(consideredFlags);
            if(condition.condition().evaluate(currentFlags)){
                commonKnowledge.append(" - ").append(condition.location().name());
                commonKnowledge.append(" Beschreibung=").append(condition.location().description());
                commonKnowledge.append("\n");
            }
        }
        return commonKnowledge.toString();
    }
}
