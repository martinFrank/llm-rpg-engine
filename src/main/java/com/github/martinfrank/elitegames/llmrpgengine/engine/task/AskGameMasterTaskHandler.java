package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Knowledge;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Person;
import com.github.martinfrank.elitegames.llmrpgengine.agent.GameMasterFacet;
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
 * The answer is assembled from the session, not narrated. That is the whole point of this handler:
 * these questions have exactly one correct answer, and it is already in the game state. Handing
 * them to an agent would buy nothing but a chance to invent a path that does not exist or an hour
 * of the day the world has no concept of – and it would cost a model call per question.
 * <p>
 * Nothing here changes the session. A question is not a move: no flags are raised, no trigger
 * fires, no time passes, and the engine does not advance the chapter over it.
 */
@Component
public class AskGameMasterTaskHandler implements TaskHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AskGameMasterTaskHandler.class);

    @Override
    public TaskType type() {
        return TaskType.ASK_GAME_MASTER;
    }

    @Override
    public void execute(Verdict verdict, Session session) {
        GameMasterFacet facet = verdict.facetOrUnspecified();
        LOGGER.debug("Question to the game master ({}): '{}'", facet, verdict.interpretation());
        session.chatHistory.gameMaster(answer(facet, session));
    }

    private String answer(GameMasterFacet facet, Session session) {
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
