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
        Person wirtin = new Person.Builder()
                .id("person.kalgeria-mondlaeufer")
                .name("Kalgeria Mondläufer")
                .build();

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

    @Test
    void theGameMastersAsideIsAttributedToTheGameMaster() {
        ChatHistory history = new ChatHistory();

        history.gameMaster("Es ist Nachmittag.");

        assertThat(history.getLatestEntries(1)).containsExactly(
                new ChatEntry("Spielleiter", "Es ist Nachmittag.", ChatEntry.Kind.META));
    }

    /**
     * Asking about the game state must not push the plot out of the agents' context window: the
     * meta entries are skipped, not counted towards the requested length.
     */
    @Test
    void metaEntriesAreLeftOutOfTheStoryEntries() {
        ChatHistory history = new ChatHistory();
        history.player("ich gehe zum wirtshaus");
        history.narrator("Ihr betretet das Wirtshaus.");
        history.playerQuestion("wie spät ist es?");
        history.gameMaster("Es ist Nachmittag.");
        history.playerQuestion("wo bin ich?");
        history.gameMaster("Ihr seid hier: Wirtshaus zum kleinen Adler");

        assertThat(history.getLatestStoryEntries(2)).extracting(ChatEntry::statement)
                .containsExactly("ich gehe zum wirtshaus", "Ihr betretet das Wirtshaus.");
        // The player still gets to see all of it.
        assertThat(history.getLatestEntries(6)).hasSize(6);
    }

    /**
     * The game master's answers are assembled line by line by the engine, so they are the one
     * kind of entry that must not be run through the normalizer.
     */
    @Test
    void theGameMastersEnumerationKeepsItsLineBreaks() {
        ChatHistory history = new ChatHistory();

        history.gameMaster("Von hier aus könnt ihr gehen:\n - Der Dorfladen\n - Die Dorf Schmiede");

        assertThat(history.getLatestEntries(1).getFirst().statement().lines()).hasSize(3);
    }
}
