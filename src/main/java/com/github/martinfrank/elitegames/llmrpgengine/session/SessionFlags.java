package com.github.martinfrank.elitegames.llmrpgengine.session;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.flags.BaseFlag;

import java.util.*;

public class SessionFlags {

    private final Map<UUID, Object> currentFlags = new HashMap<>();

    public void init(List<Flag<?>> flags) {
        for (Flag<?> flag : flags) {
            currentFlags.put(flag.getId(), flag.getValue());
        }
    }

    public void setFlagValue(UUID id, Object value) {
        currentFlags.put(id, value);
    }

//    public Object getFlagValue(UUID id) {
//        return currentFlags.get(id);
//    }

    public List<Flag<?>> getFlags(List<Flag<?>> flags) {
        List<Flag<?>> result = new ArrayList<>();
        for (Flag<?> flag : flags) {
            result.add(new BaseFlag(flag.getId(), "sessionFlag", currentFlags.get(flag.getId())));
        }
        return result;
    }
}
