package com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Identifiable;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Investigation;

/**
 * What a closer look at {@code subject} can turn up in the current chapter, as long as
 * {@code condition} holds. The subject is whatever the player can investigate – a
 * {@link com.github.martinfrank.elitegames.llmrpgengine.adventure.Location Location} or a
 * {@link com.github.martinfrank.elitegames.llmrpgengine.adventure.Person Person} – which is why it
 * is only bound to {@link Identifiable}: the subject is matched by id.
 * <p>
 * The condition is what keeps a discovery from being made twice: an investigation that hands the
 * player a key is typically guarded by "the key has not been found yet".
 */
public record InvestigateCondition<R extends Identifiable>(R subject, Investigation investigation, Condition condition) {
}
