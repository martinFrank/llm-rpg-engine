package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Dialog;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Person;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TalkContext;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TaskType;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import com.github.martinfrank.elitegames.llmrpgengine.session.ChatEntry;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.session.StringNormalizer;
import com.github.martinfrank.elitegames.llmrpgengine.session.TalkEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
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
 * only makes small talk (gossip).
 * <p>
 * A conversation state (and a dedicated dialogue agent) is not modelled yet, so for now
 * this handler only resolves the person and the dialog the player wants to talk about.
 */
@Component
public class TalkTaskHandler implements TaskHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TalkTaskHandler.class);

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
            handleTalking(session, person, dialog);
        } else {
            LOGGER.debug("Player makes small talk (gossip) with {}", person.name());
            handleGossip(session, person);
        }
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

    private void handleTalking(Session session, Person person, Dialog dialog) {
        String talkTo = person.name();
        String location = session.getCurrentLocation().name();
        String primaryDialog = "Thema: " + dialog.topic()+ "\n Zusammenfassung: "+ StringNormalizer.normalize(dialog.summary()) + "\nKontext: "+StringNormalizer.normalize(dialog.context());
         String chatHistory = session.chatHistory.getLatestEntries(5).stream()
                .map(ChatEntry::toString)
                .collect(Collectors.joining("\n"));
        String talkHistory = session.talkHistory.getTalk(person.id(), 5).stream()
                .map(TalkEntry::toString).collect(Collectors.joining("\n"));
        String triggers = dialog.knowledgeTriggers().stream()
                .map(t -> "Trigger: " + t.trigger()+" (id: "+t.id()+") ")
                .collect(Collectors.joining("\n"));

        TalkContext context = new TalkContext(talkTo, location, primaryDialog, triggers, talkHistory, chatHistory);

        LOGGER.debug("TALK CONTEXT: {}",context);
    }

    private void handleGossip(Session session, Person person) {
        Location location = session.getCurrentLocation();
//        TalkContext context = new TalkContext(location.description());
    }
}
