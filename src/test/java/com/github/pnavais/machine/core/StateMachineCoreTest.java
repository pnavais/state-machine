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
import com.github.pnavais.machine.api.AbstractNode;
import com.github.pnavais.machine.api.Message;
import com.github.pnavais.machine.api.exception.TransitionInitializationException;
import com.github.pnavais.machine.model.State;
import com.github.pnavais.machine.model.StateTransition;
import com.github.pnavais.machine.model.StringMessage;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for State Machine core functionality
 */
public class StateMachineCoreTest extends AbstractStateMachineTest {

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
    public void testStateMachineWrongAdd() {
        StateMachine machine = new StateMachine();
        expectWrongTransitionAndCheck(null, StringMessage.from("1"), new State("B"), machine);
        expectWrongTransitionAndCheck(new State("A"), null, new State("B"), machine);
        expectWrongTransitionAndCheck(new State("A"), StringMessage.from("1"), null, machine);
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
    public void testStateMachineRemoveOrphans() {
        StateMachine machine = new StateMachine();

        machine.add(new StateTransition(new State("A"), new StringMessage("1b"), new State("B")));
        machine.add(new StateTransition(new State("A"), new StringMessage("1c"), new State("C")));
        machine.add(new StateTransition(new State("B"), new StringMessage("2"), new State("C")));
        machine.add(new StateTransition(new State("D"), new StringMessage("3"), new State("E")));
        machine.add(new StateTransition(new State("D"), new StringMessage("4"), new State("F")));

        getStatePrinterBuilder()
                .compactMode(true)
                .build().printTransitions(machine.getTransitionsIndex());

        assertEquals(6, machine.size(), "Error building state machine");
        machine.remove("D");

        getStatePrinterBuilder()
                .compactMode(true)
                .title("After removing D")
                .build().printTransitions(machine.getTransitionsIndex());

        assertEquals(5, machine.size(), "Error removing state");
        List<State> orphanStates = machine.prune();

        getStatePrinterBuilder()
                .compactMode(true)
                .title("After pruning")
                .build().printTransitions(machine.getTransitionsIndex());

        assertNotNull(orphanStates, "Error obtaining orphan states");
        assertThat("Orphan states size mismatch", orphanStates.size(), is(2));
        List<String> stateNames = orphanStates.stream().map(AbstractNode::getName).collect(Collectors.toList());
        assertThat(stateNames, containsInAnyOrder("E", "F" ));

        assertEquals(3, machine.size(), "Error removing orphan state");

        machine.remove("B");

        assertEquals(2, machine.size(), "Error removing state");
        orphanStates = machine.prune();
        assertNotNull(orphanStates, "Error obtaining orphan states");
        assertThat("Orphan states size mismatch", orphanStates.size(), is(0));
        assertEquals(2, machine.size(), "Error removing state");

        getStatePrinterBuilder()
                .compactMode(true)
                .title("After removing B and pruning")
                .build().printTransitions(machine.getTransitionsIndex());

        machine.remove("A");
        assertEquals(1, machine.size(), "Error removing state");
        orphanStates = machine.prune();
        assertNotNull(orphanStates, "Error obtaining orphan states");
        assertThat("Orphan states size mismatch", orphanStates.size(), is(1));
        assertThat("Orphan states size mismatch", orphanStates.get(0).getName(), is("C"));
        assertEquals(0, machine.size(), "Error removing state");

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


    /**
     * Expect wrong transition and check that the initialization
     * was not correct
     *
     * @param source  the source state
     * @param message the message
     * @param target  the target state
     */
    private void expectWrongTransitionAndCheck(State source, Message message, State target, StateMachine stateMachine) {
        try {
            stateMachine.add(new StateTransition(source, message, target));
            fail("State Machine initialization mismatch");
        } catch (Exception e) {
            assertTrue(e instanceof TransitionInitializationException, "Exception mismatch");
        }
    }

}
