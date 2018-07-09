/*
 * Copyright 2017 Pablo Navais
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.payball.machine.core;


import org.junit.FixMethodOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runners.MethodSorters;
import org.payball.machine.machine.StateMachine;
import org.payball.machine.machine.api.exception.IllegalTransitionException;
import org.payball.machine.machine.model.State;
import org.payball.machine.machine.model.StateTransition;
import org.payball.machine.machine.model.StringMessage;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the creation of State Machines.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@DisplayName("State Machine Creation Tests")
public class StateMachineCreationTest extends AbstractStateMachineTest {

    @Test
    @DisplayName("Single Transition creation test")
    void testStateMachineAdd() {
        StateMachine machine = new StateMachine();

        machine.add(new StateTransition(new State("A"), new StringMessage("1"), new State("B")));

        assertEquals(2, machine.size(), "Error building state machine");
        assertNotNull(machine.getTransitions("A"), "Error retrieving transitions");
        assertEquals(1, machine.getTransitions("A").size(), "Transitions size mismatch");
        assertEquals("A", machine.getTransitions("A").iterator().next().getOrigin().getName(), "Transition origin retrieval mismatch");
        assertEquals("B", machine.getTransitions("A").iterator().next().getTarget().getName(), "Transition target retrieval mismatch");
        assertEquals("1", machine.getTransitions("A").iterator().next().getMessage().getPayload().get(), "Transition message retrieval mismatch");
    }

    @Test
    @DisplayName("Single Transition creation with null states test")
    void testStateMachineNullAdd() {
        StateMachine machine = new StateMachine();

        assertThrows(IllegalTransitionException.class, () -> machine.add(new StateTransition(null,
                StringMessage.from("1"), State.named("B"))), "Error adding null origin on State Transition");
        assertThrows(NullPointerException.class, () -> machine.add(new StateTransition(new State(null),
                StringMessage.from("1"), State.named("B"))), "Error adding null named origin on State Transition");
        assertThrows(IllegalTransitionException.class, () -> machine.add(new StateTransition(State.named("A"),
                StringMessage.from("1"), null)), "Error adding null target on State Transition");
        assertThrows(NullPointerException.class, () -> machine.add(new StateTransition(new State(null),
                StringMessage.from("1"), State.named(null))), "Error adding null named target on State Transition");
        assertThrows(IllegalTransitionException.class, () -> machine.add(new StateTransition(State.named("A"),
                null, State.named("B"))), "Error adding null target on State Transition");
        assertThrows(NullPointerException.class, () -> machine.add(new StateTransition(State.named("A"),
                StringMessage.from(null), State.named("B"))), "Error adding null named target on State Transition");
        assertThrows(IllegalTransitionException.class, () -> machine.add(null), "Error adding null transition");

    }

    @Test
    @DisplayName("Duplicate transitions test")
    void testStateMachineDuplicateAdd() {
        StateMachine machine = new StateMachine();

        machine.add(new StateTransition(new State("A"), new StringMessage("1"), new State("B")));
        machine.add(new StateTransition(new State("A"), new StringMessage("1"), new State("B")));

        assertEquals(2, machine.size(), "Error building state machine");
        assertNotNull(machine.getTransitions("A"), "Error retrieving transitions");
        assertEquals(1, machine.getTransitions("A").size(), "Transitions size mismatch");
        assertEquals("A", machine.getTransitions("A").iterator().next().getOrigin().getName(), "Transition origin retrieval mismatch");
        assertEquals("B", machine.getTransitions("A").iterator().next().getTarget().getName(), "Transition target retrieval mismatch");
        assertEquals("1", machine.getTransitions("A").iterator().next().getMessage().getPayload().get(), "Transition message retrieval mismatch");
    }

    @Test
    @DisplayName("Override Transition by adding test")
    void testStateMachineOverrideTransitionOnAdd() {
        StateMachine machine = new StateMachine();

        machine.add(new StateTransition(new State("A"), new StringMessage("1"), new State("B")));
        machine.add(new StateTransition(new State("A"), new StringMessage("1"), new State("C")));

        assertEquals(3, machine.size(), "Error building state machine");
        assertNotNull(machine.getTransitions("A"), "Error retrieving transitions");
        assertEquals(1, machine.getTransitions("A").size(), "Transitions size mismatch");
        assertEquals("A", machine.getTransitions("A").iterator().next().getOrigin().getName(), "Transition origin retrieval mismatch");
        assertEquals("C", machine.getTransitions("A").iterator().next().getTarget().getName(), "Transition target retrieval mismatch");
        assertEquals("1", machine.getTransitions("A").iterator().next().getMessage().getPayload().get(), "Transition message retrieval mismatch");

        //TODO: Add functionality to remove states not reachable
    }

    @Test
    @DisplayName("Find transition test")
    void testStateMachineFindTransition() {
        StateMachine machine = new StateMachine();

        machine.add(new StateTransition(new State("A"), new StringMessage("1"), new State("B")));
        machine.add(new StateTransition(new State("B"), new StringMessage("1"), new State("C")));

        assertEquals(3, machine.size(), "Error building state machine");
        assertNotNull(machine.getTransitions("A"), "Error retrieving transitions");

        Arrays.asList("A", "B", "C").forEach(s -> {
            Optional<State> sFound = machine.find(s);
            assertTrue(sFound.isPresent(), "Error retrieving state");
            assertEquals(s, sFound.get().getName(), "Error retrieving state");
        });
    }

    @Test
    @DisplayName("Remove State test")
    void testStateMachineRemoveState() {
        StateMachine machine = new StateMachine();

        machine.add(new StateTransition(new State("A"), new StringMessage("1"), new State("B")));
        machine.add(new StateTransition(new State("B"), new StringMessage("2"), new State("C")));

        assertEquals(3, machine.size(), "Error building state machine");

        AtomicInteger counter = new AtomicInteger(3);
        Arrays.asList("A", "B", "C").forEach(s -> {
            Optional<State> sFound = machine.find(s);
            assertTrue(sFound.isPresent(), "Error retrieving state");
            assertEquals(s, sFound.get().getName(), "Error retrieving state");

            machine.remove(s);
            assertEquals(counter.decrementAndGet(), machine.size(), "Error removing state");
        });
    }

    @Test
    @DisplayName("Remove States with transitions test")
    void testStateMachineRemoveStateCascade() {
        StateMachine machine = new StateMachine();

        machine.add(new StateTransition(new State("A"), new StringMessage("1"), new State("B")));
        machine.add(new StateTransition(new State("B"), new StringMessage("2"), new State("B")));

        assertEquals(2, machine.size(), "Error building state machine");

        assertNotNull(machine.getTransitions("B"), "Error retrieving transitions");
        assertEquals(1, machine.getTransitions("B").size(), "Error retrieving transitions");

        assertNotNull(machine.getTransitions("A"), "Error retrieving transitions");
        assertEquals(1, machine.getTransitions("A").size(), "Error retrieving transitions");

        machine.remove("B");
        Optional<State> b = machine.find("B");
        assertFalse(b.isPresent(), "Error removing state");
        assertEquals(1, machine.size(), "Error removing state in cascade");
        assertNotNull(machine.getTransitions("A"), "Error retrieving transitions");
        assertEquals(0, machine.getTransitions("A").size(), "Error retrieving transitions");
    }


}
