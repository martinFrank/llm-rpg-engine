package com.github.martinfrank.elitegames.llmrpgengine.session;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;

import java.util.*;

public class SessionFlags {

    private final Map<UUID, Object> currentFlags = new HashMap<>();

    public void init(List<Flag<?,?>> flags) {
        for (Flag<?,?> flag : flags) {
            currentFlags.put(flag.id(), flag.value());
        }
    }

    public void setFlagValue(UUID id, Object value) {
        currentFlags.put(id, value);
    }


    public List<Flag> getFlags(List flags) {
        List result = new ArrayList<>();
        for (Object flagObject : flags) {
            Flag flag = (Flag) flagObject;
            result.add( copyFlag(flag, currentFlags.get(flag.id()))); //new BaseFlag(flag.id(), "sessionFlag", currentFlags.get(flag.id())));
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    private static Flag copyFlag(Flag flag, Object value ){
        return new Flag() {

            @Override
            public String name() {
                return flag.name();
            }

            @Override
            public Object value() {
                return value;
            }

            @Override
            public Object data() {
                return flag.data();
            }

            @Override
            public UUID id() {
                return flag.id();
            }
        };
    }
}
