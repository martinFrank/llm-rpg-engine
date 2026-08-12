package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Knowledge;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Person;
import com.github.martinfrank.elitegames.llmrpgengine.agent.GameMasterFacet;
import com.github.martinfrank.elitegames.llmrpgengine.agent.NarratorAgent;
import com.github.martinfrank.elitegames.llmrpgengine.agent.NarratorContext;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TaskType;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.session.StringNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Answers a question the player put to the game master instead of acting in the fiction: where
 * they are, where they can go, who is here, what time it is, what they know.
 * <p>
 * The answer is worked out twice over, and the split is what matters here. <em>What</em> is true is
 * assembled from the session ({@link #facts}), because each of these questions has exactly one
 * correct answer and it is already in the game state. <em>How</em> it is told is the
 * {@link NarratorAgent}'s job, so an answer sounds like the story the player is in and not like a
 * readout. Letting the agent work out the content as well would let it name a way that does not
 * exist or invent an hour of the day for a world that only knows times of day.
 * <p>
 * Guardrail: if the agent delivers nothing usable – a local model running out of context mid-reply
 * is a normal operating condition, see {@link TalkTaskHandler} – the assembled facts are shown as
 * they are. A plainly worded answer is still a correct answer; a question about the time of day
 * must never end in a technical error.
 * <p>
 * Nothing here changes the session. A question is not a move: no flags are raised, no trigger
 * fires, no time passes, and the engine does not advance the chapter over it.
 */
@Component
public class AskGameMasterTaskHandler implements TaskHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AskGameMasterTaskHandler.class);

    private final NarratorAgent narratorAgent;

    public AskGameMasterTaskHandler(NarratorAgent narratorAgent) {
        this.narratorAgent = narratorAgent;
    }

    @Override
    public TaskType type() {
        return TaskType.ASK_GAME_MASTER;
    }

    @Override
    public void execute(Verdict verdict, Session session) {
        GameMasterFacet facet = verdict.facetOrUnspecified();
        LOGGER.debug("Question to the game master ({}): '{}'", facet, verdict.interpretation());
        String facts = facts(facet, session);
        session.chatHistory.gameMaster(narrate(facet, facts, session));
    }

    /** The facts put into words by the Narrator, or the facts themselves if that fails. */
    private String narrate(GameMasterFacet facet, String facts, Session session) {
        NarratorContext context = NarratorContext.generateGameMasterAnswerContext(
                session, question(facet), facts);
        long now = System.currentTimeMillis();
        String narration;
        try {
            narration = narratorAgent.narrate(context);
        } catch (RuntimeException e) {
            LOGGER.warn("Narrator delivered no usable answer for {} -> answering plainly: {}",
                    facet, e.toString());
            return facts;
        }
        LOGGER.info("Duration narration evaluation: {} ms", System.currentTimeMillis() - now);
        if (narration == null || narration.isBlank()) {
            LOGGER.warn("Narrator delivered an empty answer for {} -> answering plainly", facet);
            return facts;
        }
        return narration;
    }

    /** What the player wants to know, for the Narrator's AUFGABE field. */
    private String question(GameMasterFacet facet) {
        String asked = switch (facet) {
            case WHERE_AM_I -> "wo er sich gerade befindet";
            case WHERE_CAN_I_GO -> "welche Wege von hier fortführen";
            case WHO_IS_HERE -> "wer sich gerade bei ihm aufhält";
            case WHAT_TIME_IS_IT -> "welche Tageszeit gerade herrscht";
            case WHAT_DO_I_KNOW -> "was sein Auftrag ist und was er bisher herausgefunden hat";
            case UNSPECIFIED -> "wie seine Lage gerade steht";
        };
        return "der Spieler hat dich gefragt, " + asked
                + ". Erinnere ihn daran, ohne die Spielwelt zu verlassen.";
    }

    /** The one correct answer, straight from the session. */
    private String facts(GameMasterFacet facet, Session session) {
        return switch (facet) {
            case WHERE_AM_I -> whereAmI(session);
            case WHERE_CAN_I_GO -> whereCanIGo(session);
            case WHO_IS_HERE -> whoIsHere(session);
            case WHAT_TIME_IS_IT -> whatTimeIsIt(session);
            case WHAT_DO_I_KNOW -> whatDoIKnow(session);
            case UNSPECIFIED -> overview(session);
        };
    }

    private String whereAmI(Session session) {
        Location location = session.getCurrentLocation();
        return "Ihr seid hier: " + location.name() + "\n"
                + StringNormalizer.normalize(location.description());
    }

    private String whereCanIGo(Session session) {
        List<Location> reachable = session.getReachableLocations(session.getCurrentLocation());
        if (reachable.isEmpty()) {
            return "Von hier aus führt euch gerade kein Weg weiter.";
        }
        return "Von hier aus könnt ihr gehen:\n" + bulletList(reachable.stream().map(Location::name).toList());
    }

    private String whoIsHere(Session session) {
        List<Person> persons = session.getCurrentPersons(session.getCurrentLocation());
        if (persons.isEmpty()) {
            return "Außer euch ist niemand hier.";
        }
        // Only the names: who is here is a question of fact, and what a figure looks like or is
        // like is something the player finds out by taking a closer look (INVESTIGATE).
        return "Hier ist außer euch:\n" + bulletList(persons.stream().map(Person::name).toList());
    }

    private String whatTimeIsIt(Session session) {
        return "Es ist " + session.getCurrentTime().label()
                + ". Genauer als nach der Tageszeit rechnet hier niemand.";
    }

    /**
     * The player's own record of the adventure: the briefing they were given plus everything they
     * have found out since.
     * <p>
     * Both parts are things the player has already read – the chapter's intro was shown to them
     * when the chapter began, and a piece of knowledge is only listed once its flag has actually
     * been raised. So this cannot spoil anything, which is exactly why the chapter's
     * {@code summary} (written for the verdict agent, and describing how the chapter is
     * <em>meant</em> to go) must never be used here.
     */
    private String whatDoIKnow(Session session) {
        StringBuilder answer = new StringBuilder("Damit hat es angefangen:\n")
                .append(StringNormalizer.normalize(session.getCurrentChapter().intro().intro()));
        List<Knowledge> known = session.getKnownKnowledge();
        if (known.isEmpty()) {
            return answer.append("\n\nHerausgefunden habt ihr seither noch nichts.").toString();
        }
        answer.append("\n\nDas habt ihr seither herausgefunden:\n");
        for (Knowledge knowledge : known) {
            answer.append(" - ").append(knowledge.name()).append(": ")
                    .append(StringNormalizer.normalize(knowledge.knowledge())).append("\n");
        }
        return answer.toString().stripTrailing();
    }

    /** For a question whose subject stayed unclear: the short version of where things stand. */
    private String overview(Session session) {
        return whereAmI(session) + "\n\n"
                + whatTimeIsIt(session) + "\n\n"
                + whoIsHere(session) + "\n\n"
                + whereCanIGo(session);
    }

    private static String bulletList(List<String> names) {
        return names.stream().map(name -> " - " + name).reduce((a, b) -> a + "\n" + b).orElse("");
    }
}
