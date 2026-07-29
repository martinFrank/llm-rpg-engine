package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Buchenhain;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TalkAgent;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TalkContext;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TalkResponse;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TaskType;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.user.Player;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies which dialog a TALK verdict ends up talking about. The dialog itself is not exposed, so
 * the test reads it off the {@link TalkContext} the handler hands to the agent: a scripted dialog
 * fills {@link TalkContext#primaryDialog()}, gossip leaves it empty.
 */
class TalkTaskHandlerTest {

    /** "Gefahr für das Dorf" – common knowledge, therefore available to every person. */
    private static final String GEFAHR_FUER_DAS_DORF = "7975bb9c-72f0-4038-a5f7-591241275826";
    /** Rangolf Klingbeil, the smith – has no dialog of his own in chapter 1. */
    private static final String RANGOLF = "dcd181fb-3bc9-4941-92d4-4edc3aa68636";
    private static final String SCHMIEDE = "2badab9d-825c-4561-815c-80afcb774ad3";

    /** Stub agent: records the context it was called with and answers with a fixed reply. */
    private static class RecordingTalkAgent extends TalkAgent {
        private TalkContext context;

        RecordingTalkAgent() {
            super(null, null);
        }

        @Override
        public TalkResponse talk(TalkContext context) {
            this.context = context;
            return new TalkResponse("(Antwort)", List.of());
        }
    }

    private final RecordingTalkAgent talkAgent = new RecordingTalkAgent();
    private final TalkTaskHandler handler = new TalkTaskHandler(talkAgent);

    /** A session standing in the smithy, so Rangolf is the person present. */
    private Session sessionAtTheSmithy() {
        Session session = new Session(new Buchenhain(), new Player("Thorsten"));
        session.start();
        session.setCurrentLocation(session.resolveLocation(SCHMIEDE));
        session.chatHistory.player("egal, wird als letzte Eingabe gelesen");
        return session;
    }

    private Verdict talkTo(String personId, String dialogTopic, String dialogId) {
        return new Verdict("egal", TaskType.TALK, "Rangolf Klingbeil", personId, dialogTopic, dialogId);
    }

    @Test
    void exactDialogIdIsUsed() {
        Session session = sessionAtTheSmithy();

        handler.execute(talkTo(RANGOLF, "Gefahr für das Dorf", GEFAHR_FUER_DAS_DORF), session);

        assertThat(talkAgent.context.primaryDialog()).contains("Gefahr für das Dorf");
    }

    @Test
    void mangledDialogIdWithMatchingTopicIsRecovered() {
        Session session = sessionAtTheSmithy();
        String mangled = GEFAHR_FUER_DAS_DORF.substring(0, GEFAHR_FUER_DAS_DORF.length() - 1);

        handler.execute(talkTo(RANGOLF, "Gefahr im Dorf", mangled), session);

        assertThat(talkAgent.context.primaryDialog()).contains("Gefahr für das Dorf");
    }

    @Test
    void mangledDialogIdWithForeignTopicFallsBackToGossip() {
        Session session = sessionAtTheSmithy();
        // Exactly the case seen in the log: 7977... instead of 7975..., i.e. one single edit away
        // from the only candidate, reported under a topic the adventure does not have at all.
        String invented = GEFAHR_FUER_DAS_DORF.replace("7975", "7977");

        handler.execute(talkTo(RANGOLF, "Waffenpflege und Ausrüstung", invented), session);

        assertThat(talkAgent.context.primaryDialog()).isEmpty();
    }

    @Test
    void unknownDialogIdIsGossip() {
        Session session = sessionAtTheSmithy();

        handler.execute(talkTo(RANGOLF, "", Verdict.UNKNOWN), session);

        assertThat(talkAgent.context.primaryDialog()).isEmpty();
    }

    @Test
    void mangledDialogIdWithoutTopicFallsBackToGossip() {
        Session session = sessionAtTheSmithy();
        String invented = GEFAHR_FUER_DAS_DORF.replace("7975", "7977");

        handler.execute(talkTo(RANGOLF, "", invented), session);

        assertThat(talkAgent.context.primaryDialog()).isEmpty();
    }

    @Test
    void unknownPersonIsNotTalkedTo() {
        Session session = sessionAtTheSmithy();

        handler.execute(talkTo("00000000-0000-0000-0000-000000000000", "", Verdict.UNKNOWN), session);

        assertThat(talkAgent.context).isNull();
    }
}
