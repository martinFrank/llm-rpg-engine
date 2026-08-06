package com.github.martinfrank.elitegames.llmrpgengine.agent;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.GameTime;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Person;
import com.github.martinfrank.elitegames.llmrpgengine.session.ChatEntry;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.session.StringNormalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record NarratorContext (String purpose, String location, String persons, String time, String interestingDetails, String conversationHistory) {



    public static NarratorContext generateInspectLocationContext(Session session, Location location) {
        String persons = extractAvailablePersons(session, location);
        String locationString = extractLocation(location);
        String time = extractTime(session.getCurrentTime());
        String interestingDetails = extractDetails(session, location);
        String chatHistory = extractChatHistory(session);
        return new NarratorContext(
                "der Spieler untersucht einen Ort und möchte Details über diesen Ort wissen",
                locationString,
                persons,
                time,
                interestingDetails,
                chatHistory);
    }

    /**
     * For a player taking a closer look at one person. The scene around them stays the same, but the
     * only figure handed to the Narrator is the inspected one, so the description stays on them.
     * <p>
     * Only what is outwardly perceivable is passed on: appearance, plus the description that is
     * common knowledge in the village. A person's {@code role} is an authoring note about their
     * function in the plot ("Nebencharakter", "Auftraggeber dieses Abenteuers"), and
     * {@code background}/{@code personality} are their history and inner life – none of that is
     * revealed by looking at someone, so all three stay out of the prompt.
     */
    public static NarratorContext generateInspectPersonContext(Session session, Person person) {
        return new NarratorContext(
                "der Spieler betrachtet " + person.name() + " genauer und möchte wissen, was er an "
                        + "dieser Person wahrnimmt. Beschreibe nur diese Person.",
                extractLocation(session.getCurrentLocation()),
                extractPerson(person),
                extractTime(session.getCurrentTime()),
                "",
                extractChatHistory(session));
    }

    private static String extractPerson(Person person) {
        List<String> traits = new ArrayList<>();
        addTrait(traits, "Beschreibung", person.description());
        addTrait(traits, "Aussehen", person.appearance());
        if (traits.isEmpty()) {
            return person.name();
        }
        return person.name() + " (" + String.join(", ", traits) + ")";
    }

    private static void addTrait(List<String> traits, String label, String value) {
        if (value != null && !value.isBlank()) {
            traits.add(label + ": " + StringNormalizer.normalize(value));
        }
    }

    private static String extractDetails(Session session, Location location) {
        StringBuilder details = new StringBuilder();
        for (UUID destinationId: location.destinationIds()) {
            Location destination = session.getLocation(destinationId);
            if (destination != null) {
                details.append(" - ")
                        .append(destination.name())
                        .append(": ")
                        .append(StringNormalizer.normalize(destination.description())).append("\n");
            }
        }
        if (details.isEmpty()) {
            return "";
        }
        return "ZIEL-ORTE die von hier aus erreicht werden können:\n" + details;
    }

    private static String extractLocation(Location location) {
        return location.name()+": "+ StringNormalizer.normalize(location.description());
    }

    private static String extractAvailablePersons(Session session, Location location) {
        List<Person> persons = session.getCurrentPersons(location);
        return persons.stream()
                .map(p -> p.name() + " (Beschreibung: "+StringNormalizer.normalize(p.description())+")")
                .collect(Collectors.joining("\n"));
    }

    private static String extractChatHistory(Session session) {
        return session.chatHistory.getLatestEntries(5).stream()
                .map(ChatEntry::toString)
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

    /**
     * For an input the engine cannot map onto any scripted task: the Narrator says – in character,
     * without leaving the fiction – that this is not something the player can do here. The scene
     * around the player is passed along so the refusal still sounds like part of the story.
     */
    public static NarratorContext generateUnknownTaskContext(Session session) {
        Location location = session.getCurrentLocation();
        return new NarratorContext(
                "der Spieler hat etwas eingegeben, das sich im Spiel nicht umsetzen lässt. "
                        + "Sage ihm in einem einzigen kurzen Satz aus der Erzählstimme, dass das "
                        + "hier so nicht geht, ohne die Spielwelt zu verlassen und ohne ihm "
                        + "Vorschläge zu machen. Beschreibe danach nichts weiter.",
                extractLocation(location),
                extractAvailablePersons(session, location),
                extractTime(session.getCurrentTime()),
                "",
                extractChatHistory(session));
    }

    public static NarratorContext generateWalkToContext(Session session, Location location) {
        String persons = extractAvailablePersons(session, location);
        String locationString = extractLocation(location);
        String time = extractTime(session.getCurrentTime());
//        String interestingDetails = extractDetails(session, location);
        String chatHistory = extractChatHistory(session);
        return new NarratorContext(
                "der Spieler geht zu einem anderen Ort, erzähle dem Spieler was er sieht, wenn er dort ankommt.",
                locationString,
                persons,
                time,
                "", //es gibt für den ortswechsel aktuell keine besonderheiten
                chatHistory);
    }
}
