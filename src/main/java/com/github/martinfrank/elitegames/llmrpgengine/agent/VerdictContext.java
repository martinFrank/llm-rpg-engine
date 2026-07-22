package com.github.martinfrank.elitegames.llmrpgengine.agent;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.GameTime;
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
                              String availableLocations) {


    public static VerdictContext generate(Session session) {
//        String adventurePlot = extractAdventurePlot(session);
        String chapterSummary = extractChapter(session);
        String location = extractLocation(session);
        String persons = extractPersons(session);
        String items = extractItems(session);
        String chatHistory = extractChatHistory(session);
        String availablePersons = extractAvailablePersons(session);
        String availableLocations = extractAvailableLocations(session);
        return new VerdictContext(chapterSummary, location, persons, items, chatHistory, availablePersons, availableLocations);
    }

    private static String extractChapter(Session session) {
        return StringNormalizer.normalize(session.getCurrentChapter().summary());
    }

    private static String extractPersons(Session session) {
        Location location = session.getCurrentLocation();
        List<Person> persons = session.getCurrentPersons(location);
        return persons.stream()
                .map(p -> "Name: "+p.name()+", Rolle: "+ StringNormalizer.normalize(p.role()))
                .collect(Collectors.joining("\n"));
    }

    private static String extractLocation(Session session) {
        Location location = session.getCurrentLocation();
        return location.name()+": "+StringNormalizer.normalize(location.description());
    }

    private static String extractChatHistory(Session session) {
        return session.chatHistory.getLatestEntries(5).stream()
                .map(ChatEntry::toString)
                .collect(Collectors.joining("\n"));
    }

    private static String extractAvailablePersons(Session session) {
        List<Person> availablePersons = session.getCurrentChapter().personConditions().stream()
                .map(PersonCondition::who)
                .distinct().toList();
        return availablePersons.stream()
                .map(p -> p.name() + " (id: " + p.id() + ", Beschreibung: "+StringNormalizer.normalize(p.description())+")")
                .collect(Collectors.joining("\n"));
    }

    private static String extractAvailableLocations(Session session) {
        List<Location> availableLocations = session.getCurrentChapter().locationConditions().stream()
                .map(LocationCondition::location)
                .distinct().toList();
        return availableLocations.stream()
                .map(l -> l.name() + " (id: " + l.id() + ", Beschreibung: "+StringNormalizer.normalize(l.description())+")")
                .collect(Collectors.joining("\n"));
    }

    private static String extractItems(Session session) {
        return "";
    }

}
