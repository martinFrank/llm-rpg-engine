package com.github.martinfrank.elitegames.llmrpgengine.agent;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.GameTime;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Person;
import com.github.martinfrank.elitegames.llmrpgengine.session.ChatEntry;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.session.StringNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class VerdictContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerdictContext.class);

    public VerdictContext(String chatHistory, String location, String persons, String items) {
    }

    public static VerdictContext generate(Session session) {
        String chatHistory = extractChatHistory(session);
        String location = extractLocation(session);
        String persons = extractPersons(session);
        String items = extractItems(session);
        return new VerdictContext(chatHistory, location, persons, items);
    }


    private static String extractPersons(Session session) {
        Location location = session.getCurrentLocation();
        GameTime time = session.getCurrentTime();
        List<Person> persons = session.getPersons(location, time);
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
