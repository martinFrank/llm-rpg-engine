package com.github.martinfrank.elitegames.llmrpgengine.game;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Knowledge;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;
import com.github.martinfrank.elitegames.llmrpgengine.session.ChatEntry;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.session.StringNormalizer;

import java.util.List;

/**
 * The game as the page shows it: the log the player reads, and the handful of facts the sidebar
 * keeps in view.
 * <p>
 * Everything here is read off the {@link Session} at the moment the turn ended, and the page keeps
 * nothing of its own - so a reload can never show a state the session has moved past. What the
 * sidebar lists is deliberately the same the game master would answer with (who is here, where a
 * way leads, what the player knows): it saves the player from spending a turn on a question, and
 * it can hold nothing the session does not really hold.
 *
 * @param error what went wrong in the turn just played, or {@code null} - a turn that fails is a
 *              line the player reads, not a broken page
 */
public record GameView(
        String title,
        String author,
        String chapter,
        String location,
        String locationDescription,
        String time,
        List<Named> persons,
        List<Named> destinations,
        List<Note> knowledge,
        List<Line> log,
        String error) {

    /** How much of the log is handed out - far more than a session produces in an evening. */
    private static final int LOG_LENGTH = 500;

    /** Someone or something the player can name in their next input. */
    public record Named(String name, String sub) {
    }

    /** One thing the player has found out. */
    public record Note(String name, String text) {
    }

    /** One line of the log, with its speaker and whether it belongs to the story. */
    public record Line(String actor, String statement, String kind) {
    }

    public static GameView of(Session session, String error) {
        Location here = session.getCurrentLocation();
        return new GameView(
                session.getMetadata().title(),
                session.getMetadata().author(),
                session.getCurrentChapter().name(),
                here.name(),
                StringNormalizer.normalize(here.description()),
                session.getCurrentTime().label(),
                persons(session, here),
                destinations(session, here),
                knowledge(session),
                log(session),
                error);
    }

    private static List<Named> persons(Session session, Location here) {
        return session.getCurrentPersons(here).stream()
                .map(person -> new Named(person.name(), person.role()))
                .toList();
    }

    private static List<Named> destinations(Session session, Location here) {
        return session.getReachableLocations(here).stream()
                .map(location -> new Named(location.name(), null))
                .toList();
    }

    private static List<Note> knowledge(Session session) {
        return session.getKnownKnowledge().stream()
                .map(GameView::note)
                .toList();
    }

    private static Note note(Knowledge knowledge) {
        return new Note(knowledge.name(), StringNormalizer.normalize(knowledge.knowledge()));
    }

    private static List<Line> log(Session session) {
        return session.chatHistory.getLatestEntries(LOG_LENGTH).stream()
                .map(GameView::line)
                .toList();
    }

    private static Line line(ChatEntry entry) {
        return new Line(entry.actor(), entry.statement(), entry.kind().name());
    }
}
