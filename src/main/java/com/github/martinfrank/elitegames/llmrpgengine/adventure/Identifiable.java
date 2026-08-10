package com.github.martinfrank.elitegames.llmrpgengine.adventure;

/**
 * Anything an adventure can reference by {@link Id}.
 * <p>
 * Resolving an id is not done here: {@link BaseAdventure} indexes everything once and looks it up
 * in that index. A {@code find(id, list)} helper on this interface is what made every reference a
 * walk over a freshly rebuilt list.
 */
public interface Identifiable {

    Id id();

}
