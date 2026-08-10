package com.github.martinfrank.elitegames.llmrpgengine.session;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.GameTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Id;

import java.util.*;

public class SessionFlags {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionFlags.class);

    private final Map<Id, Boolean> currentFlags = new HashMap<>();

    public void init(List<Flag<?>> flags) {
        for (Flag<?> flag : flags) {
            currentFlags.put(flag.id(), false);
        }
    }

    public void raiseFlagValue(Id id) {
        currentFlags.put(id, true);
    }


    public List<Flag<?>> getFlags(List<Flag<?>> flags) {
        List<Flag<?>> result = new ArrayList<>();
        for (Flag<?> flag : flags) {
            result.add(copyFlag(flag, currentFlags.get(flag.id()))); //new BaseFlag(flag.id(), "sessionFlag", currentFlags.get(flag.id())));
        }
        return result;
    }

    private static <R> Flag<R> copyFlag(Flag<R> flag, Boolean value) {
        return new Flag<>() {
            @Override
            public Id id() {
                return flag.id();
            }

            @Override
            public String name() {
                return flag.name();
            }

            @Override
            public R content() {
                return flag.content();
            }

            @Override
            public boolean isRaised() {
                return value;
            }
        };
    }

    public boolean evaluate(Condition condition, GameTime currentTime) {
        List<Flag<?>> requiredFlags = condition.consideredFlags();
        List<Flag<?>> currentFlags = getFlags(requiredFlags);

        //spezialFall:
        if (condition.id().equals(Condition.DAY_TIME.id())
                || condition.id().equals(Condition.NIGHT_TIME.id())) {
            currentFlags = List.of(currentTimeFlag(currentTime));
        }
        return condition.evaluate(currentFlags);
    }


    private static Flag<GameTime> currentTimeFlag(GameTime currentTime) {
        return new Flag<>() {

            @Override
            public Id id() {
                return null;
            }

            @Override
            public String name() {
                return "GameTime Flag";
            }

            @Override
            public GameTime content() {
                return currentTime;
            }

            @Override
            public boolean isRaised() {
                return false;
            }
        };
    }
}
