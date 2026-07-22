package com.github.martinfrank.elitegames.llmrpgengine.agent;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.GameTime;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Person;
import com.github.martinfrank.elitegames.llmrpgengine.session.ChatEntry;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.session.StringNormalizer;

import java.util.List;
import java.util.stream.Collectors;

public record VerdictContext (String chapterSummary, String location, String persons, String items, String chatHistory) {


    public static VerdictContext generate(Session session) {
//        String adventurePlot = extractAdventurePlot(session);
        String chapterSummary = extractChapter(session);
        String location = extractLocation(session);
        String persons = extractPersons(session);
        String items = extractItems(session);
        String chatHistory = extractChatHistory(session);
        return new VerdictContext(chapterSummary, location, persons, items, chatHistory);
    }

    private static String extractChapter(Session session) {
        return session.getCurrentChapter().summary();
    }


    private static String extractPersons(Session session) {
        Location location = session.getCurrentLocation();
        GameTime time = session.getCurrentTime();
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


    private static String extractItems(Session session) {
        return "";
    }
}
