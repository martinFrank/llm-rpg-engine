package com.github.martinfrank.elitegames.llmrpgengine.session;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * The game log as the player experiences it: their own inputs, the narrator's prose, and the
 * spoken lines of the non-player characters. Every entry names its speaker, so a line said by
 * an NPC is attributed to that person rather than to the narrator. The history is also fed to
 * the agents as context, where that attribution tells them who was last speaking.
 */
public class ChatHistory {

    private static final String NARRATOR = "Narrator";
    private static final String PLAYER = "Player";
    private static final String GAME_MASTER = "Spielleiter";
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatHistory.class);

    private final List<ChatEntry> chatEntries = new ArrayList<>();

    /** Records prose told by the narrator: everything that is not a person speaking. */
    public void narrator(String statement) {
        add(NARRATOR, statement, ChatEntry.Kind.STORY);
    }

    /**
     * Records the cover of the adventure: its title and who wrote it. The player reads it, but it is
     * no part of the story and therefore {@link ChatEntry.Kind#META}.
     * <p>
     * As story lines these two travelled into the agents' context window attributed to the Narrator,
     * i.e. as something the Narrator had just said about the world – and the Narrator duly picked the
     * author up as a figure of the village and built him a house down the road.
     */
    public void credits(String statement) {
        add(NARRATOR, statement, ChatEntry.Kind.META);
    }

    public void player(String statement) {
        add(PLAYER, statement, ChatEntry.Kind.STORY);
    }

    /**
     * Records a question the player put to the game master rather than an action in the game.
     * It is logged as {@link ChatEntry.Kind#META}, so the player still sees it but the agents do
     * not mistake it for a turn of the story.
     */
    public void playerQuestion(String statement) {
        add(PLAYER, statement, ChatEntry.Kind.META);
    }

    /**
     * Records the game master's answer to such a question: a plain statement of fact about the
     * game state, not narration, and therefore {@link ChatEntry.Kind#META}.
     * <p>
     * This is the one line that is stored verbatim. {@link StringNormalizer} is there to unwrap
     * prose an author or a model wrapped across lines, but a game-master answer is assembled line
     * by line by the engine – normalizing it would pull its enumerations of places and figures into
     * a single run-on line. Whatever authored text it quotes has already been normalized on the way
     * in.
     */
    public void gameMaster(String statement) {
        addVerbatim(GAME_MASTER, statement, ChatEntry.Kind.META);
    }

    /**
     * Records what a non-player character said, attributed to that person by name.
     * Use this instead of {@link #narrator(String)} whenever the line is spoken by a figure.
     */
    public void npc(Person person, String statement) {
        add(person.name(), statement);
    }

    private void add(String actor, String statement) {
        add(actor, statement, ChatEntry.Kind.STORY);
    }

    private void add(String actor, String statement, ChatEntry.Kind kind) {
        addVerbatim(actor, StringNormalizer.normalize(statement), kind);
    }

    private void addVerbatim(String actor, String statement, ChatEntry.Kind kind) {
        ChatEntry entry = new ChatEntry(actor, statement, kind);
        LOGGER.info(entry.toString());
        chatEntries.add(entry);
    }


    /** Every kind of entry, newest last – what the player gets to see. */
    public List<ChatEntry> getLatestEntries(int length) {
        return latest(chatEntries, length);
    }

    /**
     * The story only, newest last – what the agents get as context.
     * <p>
     * Meta entries are skipped rather than counted and dropped: with a plain tail of the log, three
     * questions in a row about the time of day would push the whole plot out of a five-entry
     * context window, and the narrator would pick up a conversation about bookkeeping.
     */
    public List<ChatEntry> getLatestStoryEntries(int length) {
        List<ChatEntry> story = chatEntries.stream()
                .filter(entry -> entry.kind() == ChatEntry.Kind.STORY)
                .toList();
        return latest(story, length);
    }

    private static List<ChatEntry> latest(List<ChatEntry> entries, int length) {
        int from = Math.max(0, entries.size() - length);
        return new ArrayList<>(entries.subList(from, entries.size()));
    }

    @Override
    public String toString() {
        return "ChatHistory{" +
                "chatEntries=" + chatEntries +
                '}';
    }

    public void prettyPrint(PrintStream out) {
        out.println("-----------chat history----------");
        for(ChatEntry entry : chatEntries) {
            out.println(entry.toString());
        }
    }
}
