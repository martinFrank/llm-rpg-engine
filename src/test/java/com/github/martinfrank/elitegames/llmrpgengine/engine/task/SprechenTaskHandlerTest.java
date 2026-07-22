package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Person;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TaskType;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies that SPRECHEN resolves its conversation partner via the verdict's targetId
 * (a UUID from the available-persons list) and does not look anyone up when the target
 * could not be resolved.
 */
class SprechenTaskHandlerTest {

    private static final UUID ULF = UUID.fromString("3037dd8d-62d6-42b3-88b0-800fb0e3ccd4");

    private final SprechenTaskHandler handler = new SprechenTaskHandler();

    @Test
    void resolvesPersonByTargetId() {
        Session session = mock(Session.class);
        Person ulf = new Person.Builder().id(ULF).name("Ulf Stetten").build();
        when(session.getPerson(ULF)).thenReturn(Optional.of(ulf));

        handler.execute(new Verdict("Mit Ulf reden.", TaskType.SPRECHEN, "Ulf Stetten", ULF.toString()), session);

        verify(session).getPerson(ULF);
    }

    @Test
    void unknownTargetIdResolvesNobody() {
        Session session = mock(Session.class);

        handler.execute(new Verdict("Mit niemandem reden.", TaskType.SPRECHEN, "", Verdict.UNKNOWN), session);

        verify(session, never()).getPerson(any());
    }

    @Test
    void malformedTargetIdResolvesNobody() {
        Session session = mock(Session.class);

        handler.execute(new Verdict("Unklar.", TaskType.SPRECHEN, "irgendwer", "kein-uuid"), session);

        verify(session, never()).getPerson(any());
    }
}
