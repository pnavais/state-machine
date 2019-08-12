/*
 * Copyright 2019 Pablo Navais
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.pnavais.machine.core;

import com.github.pnavais.machine.AbstractStateMachineTest;
import com.github.pnavais.machine.api.Message;
import com.github.pnavais.machine.api.exception.ValidationException;
import com.github.pnavais.machine.api.validator.TransitionValidator;
import com.github.pnavais.machine.api.validator.ValidationResult;
import com.github.pnavais.machine.impl.StateTransitionMap;
import com.github.pnavais.machine.model.State;
import com.github.pnavais.machine.model.StateTransition;
import com.github.pnavais.machine.model.StringMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for State machine components
 */
public class StateMachineComponentsTest extends AbstractStateMachineTest {

    @Test
    public void testTransitionInitialization() {
        StateTransition transition = new StateTransition(new State("A"), new StringMessage("1"), new State("B"));
        assertNotNull(transition.getOrigin(), "Error retrieving origin");
        assertNotNull(transition.getOrigin().getId(), "Error retrieving origin identifier");
        assertNotNull(transition.getTarget(), "Error retrieving target");
        assertNotNull(transition.getTarget().getId(), "Error retrieving origin identifier");
        assertNotNull(transition.getMessage(), "Error retrieving message");
        assertNotNull(transition.getMessage().getMessageId(), "Error retrieving message identifier");
        assertNotNull(transition.getMessage().getPayload(), "Error obtaining message payload");
        assertEquals("1", transition.getMessage().getPayload().get().toString(), "Error obtaining message identifier");
    }

    @Test
    public void testTransitionIdentity() {
        StateTransition transition = new StateTransition(new State("A"), new StringMessage("1"), new State("B"));
        StateTransition transitionAlt = new StateTransition(new State("A"), new StringMessage("1"), new State("B"));
        StateTransition transitionNew = new StateTransition(new State("A"), new StringMessage("1"), new State("C"));

        assertEquals(transition, transitionAlt, "Error comparing transitions");
        assertEquals(transition.hashCode(), transitionAlt.hashCode(), "Error comparing hash codes");
        assertNotEquals(transition, transitionNew, "Error comparing transitions");
    }

    @Test
    public void testCustomValidator() {
        TransitionValidator<State, Message, StateTransition> validator =
                (transition, transitionIndex, operation) -> ValidationResult.fail("Test validator");

        ValidationResult result = validator.validate(null, null, TransitionValidator.Operation.ADD);
        assertNotNull(result, "Error executing validator");
        assertNotNull(result.getDescription(), "Error obtaining validation result description");
        assertNull(result.getException(), "Error obtaining validation result exception");

        StateTransitionMap transitionMap = new StateTransitionMap(validator);

        try {
            transitionMap.add(new StateTransition("A", new StringMessage("1"), "B"));
            fail("Validation failed");
        } catch (Exception e) {
            assertTrue(e instanceof ValidationException, "Validation exception mismatch");
            assertEquals(result.getDescription(), e.getMessage(), "Error obtaining exception description");
        }
    }

}
