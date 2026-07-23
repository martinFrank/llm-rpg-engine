package com.github.martinfrank.elitegames.llmrpgengine.agent;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.GameTime;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Person;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.PersonCondition;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.session.StringNormalizer;

import java.util.List;
import java.util.stream.Collectors;

public record NarratorContext (String purpose, String location, String persons, String time, String interestingDetails) {



    public static NarratorContext generateInspectLocation(Session session, Location location) {
        String persons = extractAvailablePersons(session, location);
        String locationString = extractLocation(location);
        String time = extractTime(session.getCurrentTime());
        String interestingDetails = extractDetails(session, location);
        return new NarratorContext(
                "der Spieler untersucht einen Ort und möchte Details über diesen Ort wissen",
                locationString,
                persons,
                time,
                interestingDetails);
    }

    private static String extractDetails(Session session, Location location) {
        return "";
    }

    private static String extractLocation(Location location) {
        return location.name()+": "+ StringNormalizer.normalize(location.description());
    }

    private static String extractAvailablePersons(Session session, Location location) {
        List<Person> persons = session.getCurrentPersons(location);
        return persons.stream()
                .map(p -> p.name() + " (id: " + p.id() + ", Beschreibung: "+StringNormalizer.normalize(p.description())+")")
                .collect(Collectors.joining("\n"));
    }

    private static String extractTime(GameTime time) {
        return switch (time){
            case AFTERNOON -> "nachmittag";
            case IN_THE_EVENING -> "abends";
            case AT_NIGHT -> "nachts";
            case HIGH_NOON -> "mittags";
            case MIDNIGHT -> "mitternachts";
            case MORNING -> "morgens";
            case DAWN -> "Sonnenaufgang";
            case DUSK -> "Sonnenuntergang";
        };
    }
}
