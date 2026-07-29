package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.*;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.flags.FlagChange;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.trigger.AndConditionTrigger;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles the player addressing and communicating with the person resolved from
 * {@link Verdict#targetId()} (an id from the available-persons list, resolved through the
 * guardrail {@link Session#resolvePerson(String)}). If the verdict carries no resolvable
 * person id, nothing happens.
 * <p>
 * The conversation topic is taken from {@link Verdict#dialogId()}: if it resolves to a
 * known {@link Dialog}, the player talks about that scripted dialog; otherwise the player
 * only makes small talk (gossip). Either way the {@link TalkAgent} produces the person's
 * in-character reply, which is recorded in the talk- and chat-history.
 */
@Component
public class TalkTaskHandler implements TaskHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TalkTaskHandler.class);

    private static final int TALK_HISTORY_LENGTH = 5;
    private static final int CHAT_HISTORY_LENGTH = 5;

    /**
     * Shortest word length still compared when checking a reported dialog topic against the real
     * one. Keeps filler words ("und", "das", "für") from counting as agreement.
     */
    private static final int MEANINGFUL_WORD_LENGTH = 4;

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
        Person person = session.resolvePerson(verdict.targetId());
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
     * available dialogs – that candidate list is what keeps a verdict pointing at a dialog the
     * person cannot talk about (e.g. another person's dialog) from being used.
     * <p>
     * An exact id is taken as it is. A merely <em>close</em> id has to survive a second check
     * against {@link Verdict#dialogTopic()}: the distance guardrail assumes a near miss is a typo
     * of the intended id, but a model that invents a topic tends to invent an id right next to the
     * one id it saw in the context – a single changed digit is indistinguishable from a typo. In
     * that case the reported topic gives it away, because it does not describe the dialog the id
     * points at, and the turn falls back to gossip.
     */
    private Dialog resolveDialog(Verdict verdict, Session session, Person person) {
        List<Dialog> available = session.getAvailableDialogs(person);

        Dialog exact = exactDialog(verdict.dialogId(), available);
        if (exact != null) {
            return exact;
        }

        Dialog closest = Levenshtein.findClosest(verdict.dialogId(), available);
        if (closest == null) {
            if (verdict.hasDialogId()) {
                LOGGER.info("Guardrail: dialog id {} ('{}') does not belong to {} -> gossip",
                        verdict.dialogId(), verdict.dialogTopic(), person.name());
            }
            return null;
        }
        if (!topicMatches(verdict.dialogTopic(), closest.topic())) {
            LOGGER.info("Guardrail: dialog id {} is close to '{}' ({}), but the reported topic '{}' "
                            + "describes something else -> gossip",
                    verdict.dialogId(), closest.topic(), closest.id(), verdict.dialogTopic());
            return null;
        }
        return closest;
    }

    /** The dialog whose id is exactly the reported one, or {@code null} – ids differ in case only. */
    private static Dialog exactDialog(String reportedId, List<Dialog> candidates) {
        if (reportedId == null || reportedId.isBlank()) {
            return null;
        }
        String needle = reportedId.strip();
        return candidates.stream()
                .filter(d -> d.id().toString().equalsIgnoreCase(needle))
                .findFirst()
                .orElse(null);
    }

    /**
     * Whether the topic the agent reported plausibly describes {@code actualTopic}: they have to
     * share at least one meaningful word. This tolerates rewording ("Gefahr im Dorf" for "Gefahr
     * für das Dorf") but rejects an unrelated, invented topic ("Waffenpflege und Ausrüstung").
     * A missing topic cannot confirm anything and therefore does not match.
     */
    private static boolean topicMatches(String reportedTopic, String actualTopic) {
        if (reportedTopic == null || reportedTopic.isBlank()) {
            return false;
        }
        Set<String> reported = meaningfulWords(reportedTopic);
        reported.retainAll(meaningfulWords(actualTopic));
        return !reported.isEmpty();
    }

    private static Set<String> meaningfulWords(String topic) {
        return Arrays.stream(topic.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+"))
                .filter(word -> word.length() >= MEANINGFUL_WORD_LENGTH)
                .collect(Collectors.toCollection(HashSet::new));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void converse(Session session, Person person, Dialog dialog) {
        String statement = session.chatHistory.getLatestEntries(1).getFirst().statement();
        TalkContext context = buildContext(session, person, dialog, statement);

        long now = System.currentTimeMillis();
        TalkResponse response = talkAgent.talk(context);
        long duration = System.currentTimeMillis() - now;
        LOGGER.info("Duration talk evaluation: {} ms", duration);

        // Guardrail: resolve the (possibly LLM-mangled) reported triggerCondition ids to real dialog
        // triggers. Applying them (flags/knowledge) happens elsewhere.
        List<Trigger<?>> triggers = resolveTriggers(dialog, response);
        LOGGER.debug("Resolved triggers: {}", triggers.stream().map(Identifiable::id).toList());
        if(!triggers.isEmpty()){
            for(Trigger<?> trigger : triggers){
                List conditions = trigger.conditions();
                LOGGER.debug("current trigger: {}, is triggered: {}", trigger, trigger.isTriggered(conditions, session) );
                if (trigger.isTriggered(conditions, session)) {

                    TriggeredEvent<?> event = trigger.triggeredEvent();
                    LOGGER.debug("hei its a triggeredEvent! : {}", event.name());
                    for (FlagChange<?> flagChange : event.flagChanges()) {
                        session.applyFlagChange(flagChange);
                        LOGGER.debug("we make a flag change! : {} -> {}", flagChange.flag().name(), flagChange.newValue());
                    }
                }
            }
        }

        // Record both sides in the per-person talk history and surface the reply in the game log.
        String reply = response.reply();
        session.talkHistory.player(person.id(), statement);
        session.talkHistory.npc(person.id(), reply);
        session.chatHistory.narrator(reply);
    }

    /**
     * Guardrail 3: maps the triggers the agent reported onto the real {@link AndConditionTrigger}s of
     * the dialog via {@link Levenshtein#findClosest(String, List)} – the candidates are the
     * triggers of <em>this</em> dialog, so invented ids are ignored while mangled ones still
     * resolve.
     */
    private List<Trigger<?>> resolveTriggers(Dialog dialog, TalkResponse response) {
        if (dialog == null || response.triggeredTriggers().isEmpty()) {
            return List.of();
        }
        List<Trigger<?>> candidates = dialog.knowledgeTriggers();
        List<Trigger<?>> resolved = new ArrayList<>();
        for (TalkResponse.TriggeredTrigger reported : response.triggeredTriggers()) {
            Trigger<?> match = Levenshtein.findClosest(reported.triggerId(), candidates);
            if (match == null) {
                LOGGER.info("Guardrail: reported triggerCondition id '{}' ('{}') matches no dialog triggerCondition -> ignored",
                        reported.triggerId(), reported.trigger());
            } else if (!resolved.contains(match)) {
                resolved.add(match);
            }
        }
        return resolved;
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

        String primaryDialog = "";
        String triggers = "";
        if (dialog != null) {
            primaryDialog = "Thema: " + dialog.topic()
                    + "\nZusammenfassung: " + StringNormalizer.normalize(dialog.summary())
                    + "\nKontext: " + StringNormalizer.normalize(dialog.context());
            triggers = dialog.knowledgeTriggers().stream()
                    .map(t -> "TriggerThema: " + t.triggerCondition() + " (id: " + t.id() + ")")
                    .collect(Collectors.joining("\n"));
        }

        return new TalkContext(talkTo, location, statement, primaryDialog, triggers, talkHistory, chatHistory);
    }
}
