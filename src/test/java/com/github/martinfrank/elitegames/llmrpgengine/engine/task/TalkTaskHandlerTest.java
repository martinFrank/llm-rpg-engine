package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Buchenhain;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TalkAgent;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TalkContext;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TalkResponse;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TaskType;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import com.github.martinfrank.elitegames.llmrpgengine.session.ChatEntry;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.user.Player;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies how a TALK turn is recorded: an NPC reply is attributed to the person who said it,
 * and an agent that fails to deliver one degrades to a narrated mishap instead of ending the game.
 */
class TalkTaskHandlerTest {

    /** The inn, where the innkeeper is present at any time of day. */
    private static final UUID WIRTSHAUS = UUID.fromString("603696b5-e1be-4f85-a0e1-1209147b8a3f");
    private static final String KALGERIA = "4bdd45a1-33d0-4ea4-91af-86a53e53dc61";
    private static final String KALGERIA_NAME = "Kalgeria Mondläufer";

    private final TalkAgent talkAgent = mock(TalkAgent.class);
    private final TalkTaskHandler handler = new TalkTaskHandler(talkAgent);

    /** A session standing in the inn, with the player's question as the latest chat entry. */
    private Session sessionAtTheInn() {
        Session session = new Session(new Buchenhain(), new Player("Thorsten"));
        session.start();
        session.setCurrentLocation(session.getLocation(WIRTSHAUS));
        session.chatHistory.player("ich begrüße die Wirtin");
        return session;
    }

    /** Talking to the innkeeper about nothing scripted: gossip, so no dialog needs to resolve. */
    private static Verdict gossipWithKalgeria() {
        return new Verdict("Der Spieler begrüßt die Wirtin.", TaskType.TALK, KALGERIA_NAME, KALGERIA);
    }

    @Test
    void attributesTheReplyToThePersonWhoSaidIt() {
        Session session = sessionAtTheInn();
        when(talkAgent.talk(any(TalkContext.class)))
                .thenReturn(new TalkResponse("Seid willkommen in meinem Haus!", List.of()));

        handler.execute(gossipWithKalgeria(), session);

        assertThat(session.chatHistory.getLatestEntries(1)).containsExactly(
                new ChatEntry(KALGERIA_NAME, "Seid willkommen in meinem Haus!"));
    }

    @Test
    void recordsBothSidesInTheTalkHistory() {
        Session session = sessionAtTheInn();
        when(talkAgent.talk(any(TalkContext.class)))
                .thenReturn(new TalkResponse("Seid willkommen in meinem Haus!", List.of()));

        handler.execute(gossipWithKalgeria(), session);

        assertThat(session.talkHistory.getTalk(UUID.fromString(KALGERIA)))
                .extracting(entry -> entry.actor() + ": " + entry.statement())
                .containsExactly(
                        "Player: ich begrüße die Wirtin",
                        "Npc: Seid willkommen in meinem Haus!");
    }

    @Test
    void narratesAMishapWhenTheAgentDeliversNoReply() {
        Session session = sessionAtTheInn();
        // What an unparseable (truncated) model response looks like from here.
        when(talkAgent.talk(any(TalkContext.class)))
                .thenThrow(new RuntimeException("Unexpected end-of-input"));

        assertThatCode(() -> handler.execute(gossipWithKalgeria(), session)).doesNotThrowAnyException();

        assertThat(session.chatHistory.getLatestEntries(1)).singleElement()
                .satisfies(entry -> {
                    assertThat(entry.actor()).isEqualTo("Narrator");
                    assertThat(entry.statement()).contains(KALGERIA_NAME);
                });
    }

    /** The common knowledge the handler handed to the agent for the turn. */
    private String capturedCommonKnowledge(Session session) {
        when(talkAgent.talk(any(TalkContext.class))).thenReturn(new TalkResponse("Gewiss.", List.of()));
        handler.execute(gossipWithKalgeria(), session);

        ArgumentCaptor<TalkContext> context = ArgumentCaptor.forClass(TalkContext.class);
        verify(talkAgent).talk(context.capture());
        return context.getValue().commonKnowledge();
    }

    @Test
    void namesEveryKnownPersonOnceWithTheirCurrentWhereabouts() {
        // The chapter holds several (person, location, time) triples per figure; each must be
        // collapsed to the single location that holds right now (afternoon), not listed repeatedly.
        String knowledge = capturedCommonKnowledge(sessionAtTheInn());

        // The list prefix, not the bare name: a person's own name may well occur inside their
        // personality or role description (Rangolf's does).
        assertThat(knowledge)
                .containsOnlyOnce(" - Ulf Stetten:")
                .containsOnlyOnce(" - Rangolf Klingbeil:")
                .containsOnlyOnce(" - " + KALGERIA_NAME + ":")
                .contains("AUFENTHALTSORT=Haus des Dorfvorstehers")
                .contains("AUFENTHALTSORT=Die Dorf Schmiede")
                .contains("AUFENTHALTSORT=Wirtshaus zum kleinen Adler")
                .doesNotContain("AUFENTHALTSORT=unbekannt");
    }

    @Test
    void namesTheReachableLocationsOnly() {
        String knowledge = capturedCommonKnowledge(sessionAtTheInn());

        assertThat(knowledge)
                .contains("Buchenhain Dorfplatz")
                .contains("Der Dorfladen")
                .contains("Wirtshaus zum kleinen Adler")
                // Blumental only opens up once the village elder has handed out the quest.
                .doesNotContain("Blumental");
    }

    @Test
    void keepsAuthoredTextBlocksOnOneLinePerParagraph() {
        String knowledge = capturedCommonKnowledge(sessionAtTheInn());

        // The adventure authors descriptions as wrapped description blocks; unnormalized they would
        // arrive in the prompt broken mid-sentence.
        assertThat(knowledge).contains("Er wurde gewählt weil er ein breites Vertrauen in der Bevölkerung geniesst.");
    }

    @Test
    void leavesNoTraceInTheTalkHistoryWhenTheAgentDeliversNoReply() {
        Session session = sessionAtTheInn();
        when(talkAgent.talk(any(TalkContext.class)))
                .thenThrow(new RuntimeException("Unexpected end-of-input"));

        handler.execute(gossipWithKalgeria(), session);

        // The person must not "remember" a turn that never produced an answer.
        assertThat(session.talkHistory.getTalk(UUID.fromString(KALGERIA))).isEmpty();
    }
}
