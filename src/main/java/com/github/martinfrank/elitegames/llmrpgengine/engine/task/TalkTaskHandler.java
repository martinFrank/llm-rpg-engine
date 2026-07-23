package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Dialog;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Person;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TalkAgent;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TalkContext;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TalkResponse;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TaskType;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import com.github.martinfrank.elitegames.llmrpgengine.session.ChatEntry;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.session.StringNormalizer;
import com.github.martinfrank.elitegames.llmrpgengine.session.TalkEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
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
 */
@Component
public class TalkTaskHandler implements TaskHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TalkTaskHandler.class);

    private static final int TALK_HISTORY_LENGTH = 5;
    private static final int CHAT_HISTORY_LENGTH = 5;

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

        Dialog dialog = resolveDialog(verdict, session);
        if (dialog != null) {
            LOGGER.debug("Player talks to {} about dialog '{}'", person.name(), dialog.topic());
        } else {
            LOGGER.debug("Player makes small talk (gossip) with {}", person.name());
        }
        converse(session, person, dialog);
    }

    /**
     * Resolves the scripted dialog the player's input matched, or {@code null} when no
     * dialog matched (small talk / gossip) or the resolved id is not a known dialog.
     */
    private Dialog resolveDialog(Verdict verdict, Session session) {
        Optional<UUID> dialogId = verdict.dialogUuid();
        if (dialogId.isEmpty()) {
            return null;
        }
        Dialog dialog = session.getDialog(dialogId.get());
        if (dialog == null) {
            LOGGER.info("Unknown dialog for TALK: '{}' (id: {})", verdict.dialogTopic(), verdict.dialogId());
        }
        return dialog;
    }

    private void converse(Session session, Person person, Dialog dialog) {
        String statement = session.chatHistory.getLatestEntries(1).getFirst().statement();
        TalkContext context = buildContext(session, person, dialog, statement);

        long now = System.currentTimeMillis();
        TalkResponse response = talkAgent.talk(context);
        long duration = System.currentTimeMillis() - now;
        LOGGER.info("Duration talk evaluation: {} ms", duration);
        LOGGER.debug("Triggered triggers: {}", response.triggeredTriggers());

        // Record both sides in the per-person talk history and surface the reply in the game log.
        // Applying the triggered triggers (flags/knowledge) happens elsewhere.
        String reply = response.reply();
        session.talkHistory.player(person.id(), statement);
        session.talkHistory.npc(person.id(), reply);
        session.chatHistory.narrator(reply);
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
                    .map(t -> "TriggerThema: " + t.trigger() + " (id: " + t.id() + ")")
                    .collect(Collectors.joining("\n"));
        }

        return new TalkContext(talkTo, location, statement, primaryDialog, triggers, talkHistory, chatHistory);
    }
}
