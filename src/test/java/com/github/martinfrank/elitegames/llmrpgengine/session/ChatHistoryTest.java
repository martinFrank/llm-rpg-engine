package com.github.martinfrank.elitegames.llmrpgengine.session;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Person;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChatHistoryTest {

    @Test
    void returnsTheLastNEntriesWhenMoreExist() {
        ChatHistory history = new ChatHistory();
        history.player("one");
        history.narrator("two");
        history.player("three");
        history.narrator("four");

        List<ChatEntry> latest = history.getLatestEntries(2);

        assertThat(latest).extracting(ChatEntry::statement)
                .containsExactly("three", "four");
    }

    @Test
    void returnsAllEntriesWhenExactlyNExist() {
        ChatHistory history = new ChatHistory();
        history.player("one");
        history.narrator("two");

        List<ChatEntry> latest = history.getLatestEntries(2);

        assertThat(latest).extracting(ChatEntry::statement)
                .containsExactly("one", "two");
    }

    @Test
    void returnsAllEntriesWhenFewerThanNExist() {
        ChatHistory history = new ChatHistory();
        history.player("one");
        history.narrator("two");

        List<ChatEntry> latest = history.getLatestEntries(5);

        assertThat(latest).extracting(ChatEntry::statement)
                .containsExactly("one", "two");
    }

    @Test
    void returnsEmptyListWhenHistoryIsEmpty() {
        ChatHistory history = new ChatHistory();

        assertThat(history.getLatestEntries(3)).isEmpty();
    }

    @Test
    void keepsChronologicalOrderOfTheLatestEntries() {
        ChatHistory history = new ChatHistory();
        history.player("erste");
        history.narrator("zweite");
        history.player("dritte");

        List<ChatEntry> latest = history.getLatestEntries(2);

        assertThat(latest).containsExactly(
                new ChatEntry("Narrator", "zweite"),
                new ChatEntry("Player", "dritte"));
    }

    @Test
    void attributesAnNpcLineToThePersonWhoSaidIt() {
        ChatHistory history = new ChatHistory();
        Person wirtin = new Person.Builder().name("Kalgeria Mondläufer").build();

        history.player("ich frage die Wirtin nach dem Schmied");
        history.npc(wirtin, "Gewiss, es gibt einen Schmied im Dorf.");

        assertThat(history.getLatestEntries(1)).containsExactly(
                new ChatEntry("Kalgeria Mondläufer", "Gewiss, es gibt einen Schmied im Dorf."));
    }

    @Test
    void narratorLinesStayAttributedToTheNarrator() {
        ChatHistory history = new ChatHistory();

        history.narrator("Der Dorfplatz liegt still vor euch.");

        assertThat(history.getLatestEntries(1)).extracting(ChatEntry::actor)
                .containsExactly("Narrator");
    }
}
