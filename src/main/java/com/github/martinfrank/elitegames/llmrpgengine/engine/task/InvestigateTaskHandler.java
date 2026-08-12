package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Event;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Identifiable;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Investigation;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Item;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Person;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Trigger;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.flags.ItemFlag;
import com.github.martinfrank.elitegames.llmrpgengine.agent.*;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.session.StringNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Id;
import java.util.function.DoubleSupplier;

/**
 * Resolves what the player wants to investigate from {@link Verdict#resolvedTargetId()} against
 * the session: first a location, then a person. The resolved subject is what the Narrator
 * later describes to the player.
 * <p>
 * A target that resolves to nothing falls back to the place the player is standing in: an
 * unresolvable target still means the player is looking around here, and describing the
 * current location is always a truthful answer – better than the silent dead turn that
 * returning early would produce.
 * <p>
 * Looking closely is also how the player finds what the chapter hides at a place or on a figure:
 * every {@link Investigation} the current chapter scripts for the resolved subject is rolled for,
 * and a successful {@link com.github.martinfrank.elitegames.llmrpgengine.adventure.SkillCheck}
 * fires its trigger's event – see {@link #investigate}. What was found is handed to the Narrator
 * so the player actually learns about it.
 * <p>
 * Items are covered by the task conceptually, but are not yet modelled, so they cannot be
 * resolved as a target here yet.
 */
@Component
public class InvestigateTaskHandler implements TaskHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvestigateTaskHandler.class);

    private final NarratorAgent narratorAgent;

    /** The engine's dice, so the adventure's skill checks stay pure data (and tests stay stable). */
    private final DoubleSupplier dice;

    /**
     * The constructor Spring builds the bean with. It has to be marked as such: with a second
     * constructor around and none of them annotated, the container stops guessing and looks for a
     * default constructor that does not exist.
     */
    @Autowired
    public InvestigateTaskHandler(NarratorAgent narratorAgent) {
        this(narratorAgent, Math::random);
    }

    /** For a caller that decides the outcome of the skill checks itself, e.g. a test. */
    public InvestigateTaskHandler(NarratorAgent narratorAgent, DoubleSupplier dice) {
        this.narratorAgent = narratorAgent;
        this.dice = dice;
    }

    @Override
    public TaskType type() {
        return TaskType.INVESTIGATE;
    }

    @Override
    public void execute(Verdict verdict, Session session) {
        Optional<Id> targetId = verdict.resolvedTargetId();

        if (targetId.isPresent()) {
            Location location = session.getLocation(targetId.get());
            if (location != null) {
                inspectLocation(session, location);
                return;
            }

            Person person = session.getPerson(targetId.get());
            if (person != null) {
                inspectPerson(session, person);
                return;
            }
        }

        LOGGER.debug("No known investigation target for INVESTIGATE: '{}' (id: {}) - falling back to the current location",
                verdict.target(), verdict.targetId());
        inspectLocation(session, session.getCurrentLocation());
    }

    private void inspectLocation(Session session, Location location) {
        LOGGER.debug("Player investigates the location: {}", location.name());
        String discoveries = investigate(session, location);
        narrate(session, NarratorContext.generateInspectLocationContext(session, location, discoveries));
    }

    private void inspectPerson(Session session, Person person) {
        LOGGER.debug("Player investigates the person: {}", person.name());
        String discoveries = investigate(session, person);
        narrate(session, NarratorContext.generateInspectPersonContext(session, person, discoveries));
    }

    /**
     * Rolls every investigation the current chapter offers for this subject and applies the ones
     * that succeed. Returns what the player found, for the Narrator to tell them about, or an
     * empty string when they found nothing.
     * <p>
     * Guardrail: only the chapter's investigations for <em>this</em> subject whose condition holds
     * are rolled at all (see {@link Session#getAvailableInvestigations}), so a discovery cannot be
     * made at the wrong place, nor a second time once its condition has flipped. On top of that
     * the trigger is run through {@link Session#sessionTriggers}, so even a discovery whose
     * condition an adventure forgot to close cannot raise its flags twice.
     * <p>
     * Guardrail: a failed check changes nothing at all and is not remembered – the player may look
     * again – and nothing about it reaches the Narrator, because a description hinting at the key
     * the player just failed to find would hand them the discovery anyway.
     * <p>
     * Guardrail: an investigation the adventure left without a trigger or event is skipped with a
     * warning. Incomplete authoring is a reason to find nothing, never to end the turn with an
     * exception.
     */
    private String investigate(Session session, Identifiable subject) {
        List<Investigation> available = session.getAvailableInvestigations(subject);
        if (available.isEmpty()) {
            // Worth logging: "the player found nothing" and "the chapter hides nothing here" look
            // identical from the outside, and the second one is usually an authoring question.
            LOGGER.debug("Chapter '{}' scripts no investigation for this subject (id: {})",
                    session.getCurrentChapter().name(), subject.id());
            return "";
        }
        List<String> discoveries = new ArrayList<>();
        for (Investigation investigation : available) {
            Trigger trigger = investigation.trigger();
            if (trigger == null || trigger.event() == null) {
                LOGGER.warn("Guardrail: investigation '{}' has no event to fire -> nothing to find",
                        investigation.name());
                continue;
            }
            if (!check(investigation)) {
                LOGGER.debug("Skill check failed for investigation '{}'", investigation.name());
                continue;
            }
            if (session.sessionTriggers.untriggered(List.of(trigger)).isEmpty()) {
                LOGGER.debug("Guardrail: trigger '{}' of investigation '{}' already fired -> skipped",
                        trigger.trigger(), investigation.name());
                continue;
            }
            LOGGER.debug("Skill check succeeded for investigation '{}' -> firing trigger '{}'",
                    investigation.name(), trigger.trigger());
            session.handleEvent(trigger.event());
            String discovery = describe(trigger.event());
            if (!discovery.isBlank()) {
                discoveries.add(discovery);
            }
        }
        return String.join("\n", discoveries);
    }

    /** The skill check of the investigation, rolled with the engine's dice. */
    private boolean check(Investigation investigation) {
        if (investigation.check() == null) {
            return true; // nothing to fail at: a discovery without a check is simply made
        }
        return investigation.check().check(dice.getAsDouble());
    }

    /**
     * What the Narrator is told about a discovery. An adventure can author it as the event's description;
     * without one the items the event's flags stand for are described, so a find is never silent
     * just because nobody wrote a sentence for it.
     */
    private static String describe(Event event) {
        if (event.description() != null && !event.description().isBlank()) {
            return StringNormalizer.normalize(event.description());
        }
        if (event.raisedFlags() == null) {
            return "";
        }
        List<String> items = new ArrayList<>();
        for (Flag<?> flag : event.raisedFlags()) {
            if (flag instanceof ItemFlag itemFlag && itemFlag.content() != null) {
                Item item = itemFlag.content();
                items.add(" - " + item.name() + ": " + StringNormalizer.normalize(item.description()));
            }
        }
        return String.join("\n", items);
    }

    private void narrate(Session session, NarratorContext context) {
        long now = System.currentTimeMillis();
        String narration = narratorAgent.narrate(context);
        long duration = System.currentTimeMillis() - now;
        LOGGER.info("Duration narration evaluation: {} ms", duration);
        session.chatHistory.narrator(narration);
    }
}
