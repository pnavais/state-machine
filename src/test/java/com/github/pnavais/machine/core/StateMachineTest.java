/*
 * Copyright 2019 Pablo Navais
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
package com.github.pnavais.machine.core;


import com.github.pnavais.machine.AbstractStateMachineTest;
import com.github.pnavais.machine.StateMachine;
import com.github.pnavais.machine.model.State;
import com.github.pnavais.machine.model.StateTransition;
import com.github.pnavais.machine.model.StringMessage;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for State Machine core functionality
 */
public class StateMachineTest extends AbstractStateMachineTest {

    @Test
    public void testStateMachineAdd() {
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
    public void testStateMachineDuplicateAdd() {
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
    public void testStateMachineOverrideTransitionOnAdd() {
        StateMachine machine = new StateMachine();

        machine.add(new StateTransition(new State("A"), new StringMessage("1"), new State("B")));
        machine.add(new StateTransition(new State("A"), new StringMessage("1"), new State("C")));

        assertEquals(3, machine.size(), "Error building state machine");
        assertNotNull(machine.getTransitions("A"), "Error retrieving transitions");
        assertEquals(1, machine.getTransitions("A").size(), "Transitions size mismatch");
        assertEquals("A", machine.getTransitions("A").iterator().next().getOrigin().getName(), "Transition origin retrieval mismatch");
        assertEquals("C", machine.getTransitions("A").iterator().next().getTarget().getName(), "Transition target retrieval mismatch");
        assertEquals("1", machine.getTransitions("A").iterator().next().getMessage().getPayload().get(), "Transition message retrieval mismatch");

        //TODO: Add functionality to remove orphan states
    }

    @Test
    public void testStateMachineFindTransition() {
        StateMachine machine = new StateMachine();

        machine.add(new StateTransition(new State("A"), new StringMessage("1"), new State("B")));
        machine.add(new StateTransition(new State("B"), new StringMessage("1"), new State("C")));

        assertEquals(3, machine.size(), "Error building state machine");
        assertNotNull(machine.getTransitions("A"), "Error retrieving transitions");

        Arrays.asList("A", "B", "C" ).forEach(s -> {
            Optional<State> sFound = machine.find(s);
            assertTrue(sFound.isPresent(), "Error retrieving state");
            assertEquals(s, sFound.get().getName(), "Error retrieving state");
        });
    }

    @Test
    public void testStateMachineRemoveState() {
        StateMachine machine = new StateMachine();

        machine.add(new StateTransition(new State("A"), new StringMessage("1"), new State("B")));
        machine.add(new StateTransition(new State("B"), new StringMessage("2"), new State("C")));

        assertEquals(3, machine.size(), "Error building state machine");

        AtomicInteger counter = new AtomicInteger(3);
        Arrays.asList("A", "B", "C" ).forEach(s -> {
            Optional<State> sFound = machine.find(s);
            assertTrue(sFound.isPresent(), "Error retrieving state");
            assertEquals(s, sFound.get().getName(), "Error retrieving state");

            machine.remove(s);
            assertEquals(counter.decrementAndGet(), machine.size(), "Error removing state");
        });
    }

    @Test
    public void testStateMachineRemoveStateCascade() {
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
