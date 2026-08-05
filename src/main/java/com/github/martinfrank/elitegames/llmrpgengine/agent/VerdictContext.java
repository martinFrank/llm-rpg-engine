package com.github.martinfrank.elitegames.llmrpgengine.agent;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Dialog;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Person;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.LocationCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.PersonCondition;
import com.github.martinfrank.elitegames.llmrpgengine.session.ChatEntry;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.session.StringNormalizer;

import java.util.List;
import java.util.stream.Collectors;

public record VerdictContext (String chapterSummary,
                              String location,
                              String persons,
                              String items,
                              String chatHistory,
                              String availablePersons,
                              String availableLocations,
                              String dialogTopics) {


    public static VerdictContext generate(Session session) {
//        String adventurePlot = extractAdventurePlot(session);
        String chapterSummary = extractChapter(session);
        String location = extractLocation(session);
        String persons = extractPersons(session);
        String items = extractItems(session);
        String chatHistory = extractChatHistory(session);
        String availablePersons = extractAvailablePersons(session);
        String availableLocations = extractAvailableLocations(session);
        String topics = extractTopics(session);
        return new VerdictContext(chapterSummary, location, persons, items, chatHistory, availablePersons, availableLocations, topics);
    }

    private static String extractChapter(Session session) {
        return StringNormalizer.normalize(session.getCurrentChapter().summary());
    }

    /**
     * The persons actually present at the current location (their chapter conditions hold right
     * now). This is the list a person target must be picked from, so it carries the ids: a TALK
     * (or a person INVESTIGATE) may only ever address someone who is here.
     */
    private static String extractPersons(Session session) {
        Location location = session.getCurrentLocation();
        List<Person> persons = session.getCurrentPersons(location);
        return persons.stream()
                .map(p -> p.name() + " (id: " + p.id() + ", Rolle: " + StringNormalizer.normalize(p.role()) + ")")
                .collect(Collectors.joining("\n"));
    }

    /**
     * The place the player is standing right now – carrying its id, because it is a legal target:
     * a question about the surroundings ("gibt es hier einen Schmied?") is an INVESTIGATE of this
     * location. Without the id the agent would have to re-find the same place by name in the
     * available-locations list.
     */
    private static String extractLocation(Session session) {
        Location location = session.getCurrentLocation();
        return location.name()+" (id: "+location.id()+"): "+StringNormalizer.normalize(location.description());
    }

    private static String extractChatHistory(Session session) {
        return session.chatHistory.getLatestEntries(5).stream()
                .map(ChatEntry::toString)
                .collect(Collectors.joining("\n"));
    }

    /**
     * Everyone who appears in the current chapter, whether or not they are here right now. This
     * list deliberately carries <em>no</em> ids: no task may target an absent person (you cannot
     * talk to or inspect someone who is elsewhere), so offering their ids only invited the verdict
     * agent to address the person a question was <em>about</em> instead of the person present.
     * It stays in the prompt purely so the agent can map a description the player used ("der
     * Dorfvorsteher") onto a name.
     */
    private static String extractAvailablePersons(Session session) {
        List<Person> availablePersons = session.getCurrentChapter().personConditions().stream()
                .map(PersonCondition::person)
                .distinct().toList();
        return availablePersons.stream()
                .map(p -> p.name() + " (" + firstSentence(p.description()) + ")")
                .collect(Collectors.joining("\n"));
    }

    private static String extractAvailableLocations(Session session) {
        List<Location> availableLocations = session.getCurrentChapter().locationConditions().stream()
                .map(LocationCondition::location)
                .distinct().toList();
        return availableLocations.stream()
                .map(l -> l.name() + " (id: " + l.id() + ", Beschreibung: " + firstSentence(l.description()) + ")")
                .collect(Collectors.joining("\n"));
    }

    /**
     * A short hint for id resolution: the first sentence of the description only. The verdict
     * agent matches on the name/id; the full multi-sentence description would just bloat the
     * prompt (and thus the prefill time) without helping the classification.
     */
    private static String firstSentence(String text) {
        String normalized = StringNormalizer.normalize(text);
        int end = normalized.indexOf('.');
        return end < 0 ? normalized : normalized.substring(0, end + 1);
    }

    private static String extractTopics(Session session) {
        Location location = session.getCurrentLocation();
        List<Person> persons = session.getCurrentPersons(location);
        StringBuilder topics = new StringBuilder();
        for (Person person : persons) {
            topics.append(createTopics(person, session.getAvailableDialogs(person)));
        }
        return topics.toString();
    }

    private static String createTopics(Person persons, List<Dialog> dialogs) {
        String dialoList = dialogs.stream()
                .map(d -> "Thema:" + d.topic()+" (ID: "+d.id()+", Zusammenfassung: "+d.summary()+")")
                .collect(Collectors.joining("\n"));
        return "Person: "+persons.name()+" (ID: "+persons.id()+") hat folgende Dialog(e): "+dialoList;
    }

    private static String extractItems(Session session) {
        return "";
    }

}
