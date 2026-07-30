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
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatHistory.class);

    private final List<ChatEntry> chatEntries = new ArrayList<>();

    /** Records prose told by the narrator: everything that is not a person speaking. */
    public void narrator(String statement) {
        add(NARRATOR, statement);
    }

    public void player(String statement) {
        add(PLAYER, statement);
    }

    /**
     * Records what a non-player character said, attributed to that person by name.
     * Use this instead of {@link #narrator(String)} whenever the line is spoken by a figure.
     */
    public void npc(Person person, String statement) {
        add(person.name(), statement);
    }

    private void add(String actor, String statement) {
        String normalized = StringNormalizer.normalize(statement);
        ChatEntry entry = new ChatEntry(actor, normalized);
        LOGGER.info(entry.toString());
        chatEntries.add(entry);
    }


    public List<ChatEntry> getLatestEntries(int length) {
        int from = Math.max(0, chatEntries.size() - length);
        return new ArrayList<>(chatEntries.subList(from, chatEntries.size()));
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
