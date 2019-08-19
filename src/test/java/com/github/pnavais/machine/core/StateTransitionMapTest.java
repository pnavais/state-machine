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
import com.github.pnavais.machine.api.exception.IllegalTransitionException;
import com.github.pnavais.machine.api.exception.NullTransitionException;
import com.github.pnavais.machine.api.exception.ValidationException;
import com.github.pnavais.machine.api.message.Message;
import com.github.pnavais.machine.api.transition.TransitionIndex;
import com.github.pnavais.machine.api.validator.TransitionValidator;
import com.github.pnavais.machine.api.validator.ValidationResult;
import com.github.pnavais.machine.impl.StateTransitionMap;
import com.github.pnavais.machine.impl.StateTransitionValidator;
import com.github.pnavais.machine.model.State;
import com.github.pnavais.machine.model.StateTransition;
import com.github.pnavais.machine.model.StringMessage;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests related to {@link com.github.pnavais.machine.impl.StateTransitionMap}
 */
public class StateTransitionMapTest extends AbstractStateMachineTest {

    @Test
    public void testStateTransitionMapInit() {
        StateTransitionMap transitionMap = createStateTransitionMap();
        assertNotNull(transitionMap.getTransitionMap(), "Error initializing state transition map");
        assertThat(transitionMap.getTransitionMap().size(), is(3));
        Arrays.asList("A", "B", "C").forEach(s -> {
            assertTrue(transitionMap.contains(State.from(s).build()));
            Optional<State> state = transitionMap.find(s);
            assertTrue(state.isPresent(), "Error retrieving state");
            assertThat("State retrieved mismatch", state.get().getName(), is(s));
        });
    }


    @Test
    public void testCreateStateTransitionMapFromExistingMap() {
        StateTransitionMap transitionMap = createStateTransitionMap();

        StateTransitionMap transitionMapTarget = new StateTransitionMap(transitionMap.getTransitionMap());

        assertNotNull(transitionMapTarget.getTransitionMap(), "Error initializing state transition map");
        assertThat(transitionMapTarget.getTransitionMap().size(), is(3));
        Arrays.asList("A", "B", "C").forEach(s -> {
            Optional<State> state = transitionMapTarget.find(s);
            assertTrue(state.isPresent(), "Error retrieving state");
            assertThat("State retrieved mismatch", state.get().getName(), is(s));
        });
    }

    @Test
    public void testStateTransitionMapWithDefaultValidator() {
        StateTransitionMap transitionMap =  createStateTransitionMap();

        StateTransitionMap transitionMapTarget = new StateTransitionMap(transitionMap.getTransitionValidator());
        transitionMapTarget.addAll(transitionMap.getAllTransitions());


        assertNotNull(transitionMapTarget.getTransitionMap(), "Error initializing state transition map");
        assertThat(transitionMapTarget.getTransitionMap().size(), is(3));
        Arrays.asList("A", "B", "C").forEach(s -> {
            Optional<State> state = transitionMapTarget.find(s);
            assertTrue(state.isPresent(), "Error retrieving state");
            assertThat("State retrieved mismatch", state.get().getName(), is(s));
        });
    }

    @Test
    public void testStateTransitionMapWithValidatorDisabled() {
        StateTransitionValidator validator = new StateTransitionValidator();
        StateTransitionMap stateTransitionMap = new StateTransitionMap(validator);
        StateTransition aToB = new StateTransition(new State("A"), StringMessage.from("1"), State.from("B").isFinal(true).build());
        StateTransition bToC = new StateTransition("B", StringMessage.from("1"), "C");
        stateTransitionMap.add(aToB);
        getStatePrinterBuilder().compactMode(true).build().printTransitions(stateTransitionMap);
        validator.setFailurePolicy(TransitionValidator.FailurePolicy.IGNORE);
        stateTransitionMap.add(bToC);
        getStatePrinterBuilder().title("After bToC").compactMode(true).build().printTransitions(stateTransitionMap);

        assertEquals(2, stateTransitionMap.size(), "Error adding transitions to the map");
        assertEquals(1, stateTransitionMap.getAllTransitions().size(), "Error obtaining transitions");
        assertEquals(aToB, stateTransitionMap.getAllTransitions().iterator().next(), "Transition mismatch");
    }

    @Test
    public void testStateTransitionMapWithCustomValidator() {
        // Create a custom validator avoiding transitions from state C or to state D
        StateTransitionMap transitionMap = new StateTransitionMap((transition, transitionIndex, operation) -> {
            ValidationResult result = ValidationResult.success();
            if (transition.getOrigin().getName().equals("C")) {
                result = ValidationResult.from(new ValidationException("Cannot create transition from state C"));
            } else if (transition.getTarget().getName().equals("D")) {
                result = ValidationResult.fail("Cannot create transition to state D");
            }
            return result;
        });

        transitionMap.add(new StateTransition("A", StringMessage.from("1"), "B"));
        transitionMap.add(new StateTransition("B", StringMessage.from("2"), "C"));
        assertEquals(3, transitionMap.size(), "Error adding transitions to the map");
        try {
            transitionMap.add(new StateTransition("C", StringMessage.from("3"), "D"));
            fail("Validator mismatch");
        } catch (Exception e) {
            assertTrue(e instanceof ValidationException, "Validation exception mismatch");
        } finally {
            assertEquals(3, transitionMap.size(), "Error adding transitions to the map");
            Arrays.asList("A", "B", "C").forEach(s -> {
                Optional<State> state = transitionMap.find(s);
                assertTrue(state.isPresent(), "Error retrieving state");
                assertThat("State retrieved mismatch", state.get().getName(), is(s));
            });
        }
    }

    @Test
    public void testStateTransitionMapWithCustomValidatorPolicies() {
        // Initially ignore on failures
        AtomicReference<TransitionValidator.FailurePolicy> policyRef = new AtomicReference<>(TransitionValidator.FailurePolicy.IGNORE);

        // Create a fail all validator just to test the policy
        TransitionValidator<State, Message, StateTransition> validator = new TransitionValidator<State, Message, StateTransition>() {

            @Override
            public ValidationResult validate(StateTransition transition, TransitionIndex<State, Message, StateTransition> transitionIndex, TransitionValidator.Operation operation) {
                return ValidationResult.fail("Validator disabled");
            }

            @Override
            public FailurePolicy getFailurePolicy() {
                return policyRef.get();
            }
        };

        // Ignore the operation
        StateTransitionMap transitionMap = new StateTransitionMap(validator);
        StateTransition transition = new StateTransition("A", StringMessage.from("1"), "B");
        transitionMap.add(transition);
        assertEquals(0, transitionMap.size(), "Map should be empty");

        // Proceed with the operation
        policyRef.set(TransitionValidator.FailurePolicy.PROCEED);
        transitionMap.add(transition);
        assertEquals(2, transitionMap.size(), "Error adding transitions to the map");
        assertEquals(1, transitionMap.getAllTransitions().size(), "Error obtaining transitions");
        assertEquals(transition, transitionMap.getAllTransitions().iterator().next(), "Transition mismatch");
    }

    @Test
    public void testStateTransitionMapRemovalByState() {
        StateTransitionMap transitionMap = new StateTransitionMap();
        transitionMap.add(new StateTransition("A", StringMessage.from("1"), "B"));
        transitionMap.add(new StateTransition("A", StringMessage.from("2"), "D"));
        transitionMap.add(new StateTransition("B", StringMessage.from("3"), "D"));
        transitionMap.add(new StateTransition("C", StringMessage.from("4"), "D"));
        assertNotNull(transitionMap.getTransitionMap(), "Error initializing state transition map");

        assertThat("Error retrieving transitions", transitionMap.getTransitionMap().size(), is(4));
        getStatePrinterBuilder().compactMode(true).build().printTransitions(transitionMap);
        assertThat("Error retrieving transitions for state A", transitionMap.getTransitions("A").size(), is(2));
        transitionMap.remove("D");
        getStatePrinterBuilder().title("Removed D").compactMode(true).build().printTransitions(transitionMap);

        assertThat("Error retrieving transitions", transitionMap.getTransitionMap().size(), is(3));
        assertThat("Error retrieving transitions for state A", transitionMap.getTransitions("A").size(), is(1));
        assertThat("Error retrieving transitions for state B", transitionMap.getTransitions("B").size(), is(0));
        assertThat("Error retrieving transitions for state C", transitionMap.getTransitions("C").size(), is(0));

        transitionMap.remove(State.from("B").build());
        assertThat("Error retrieving transitions", transitionMap.getTransitionMap().size(), is(2));
        assertThat("Error retrieving transitions for state A", transitionMap.getTransitions("A").size(), is(0));
        assertThat("Error retrieving transitions for state A", transitionMap.getTransitions("C").size(), is(0));
    }

    @Test
    public void testStateTransitionMapRemovalByTransition() {
        StateTransitionMap transitionMap = new StateTransitionMap();
        transitionMap.add(new StateTransition("A", StringMessage.from("1"), "B"));
        StateTransition aToD = new StateTransition("A", StringMessage.from("2"), "D");
        assertFalse(transitionMap.contains(aToD), "Error adding transition");
        transitionMap.add(aToD);
        assertTrue(transitionMap.contains(aToD), "Error adding transition");
        transitionMap.add(new StateTransition("B", StringMessage.from("3"), "D"));
        transitionMap.add(new StateTransition("C", StringMessage.from("4"), "D"));
        assertNotNull(transitionMap.getTransitionMap(), "Error initializing state transition map");

        assertThat("Error retrieving transitions", transitionMap.getTransitionMap().size(), is(4));
        assertThat("Error retrieving transitions for state A", transitionMap.getTransitions("A").size(), is(2));
        transitionMap.remove(aToD);
        assertThat("Error retrieving transitions", transitionMap.getTransitionMap().size(), is(4));
        assertThat("Error retrieving transitions for state A", transitionMap.getTransitions("A").size(), is(1));

    }

    @Test
    public void testStateTransitionMapRemovalByUnavailableTransition() {
        StateTransitionMap transitionMap = new StateTransitionMap();
        StateTransition aToB = new StateTransition("A", StringMessage.from("1"), "B");
        StateTransition bToC = new StateTransition("B", StringMessage.from("1"), "C");
        transitionMap.add(aToB);
        try {
            transitionMap.remove(bToC);
            fail("Cannot remove unavailable transition");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalTransitionException, "Exception mismatch");
        }
        try {
            transitionMap.remove((StateTransition)null);
            fail("Cannot remove null transition");
        } catch (Exception e) {
            assertTrue(e instanceof NullTransitionException, "Exception mismatch");
        }
    }


    /**
     * Creates a test state transition map with the following states :
     *
     *  +--------+---------+--------+
     *  | Source | Message | Target |
     *  +--------+---------+--------+
     *  |   A    |    1    |   B    |
     *  +--------+---------+--------+
     *  |   B    |    2    |   C    |
     *  +--------+---------+--------+
     *
     * @return the state transition map
     */
    private StateTransitionMap createStateTransitionMap() {
        StateTransitionMap stateTransitionMap = new StateTransitionMap();
        stateTransitionMap.add(new StateTransition("A", StringMessage.from("1"), "B"));
        stateTransitionMap.add(new StateTransition("B", StringMessage.from("2"), "C"));

        return stateTransitionMap;
    }
}
