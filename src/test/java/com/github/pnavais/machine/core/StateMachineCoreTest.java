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
import com.github.pnavais.machine.api.exception.TransitionInitializationException;
import com.github.pnavais.machine.api.message.Message;
import com.github.pnavais.machine.model.FilteredState;
import com.github.pnavais.machine.model.State;
import com.github.pnavais.machine.model.StateTransition;
import com.github.pnavais.machine.model.StringMessage;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for State Machine core functionality
 */
public class StateMachineCoreTest extends AbstractStateMachineTest {

   @Test
    public void testAddingStates() {
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
    public void testStateAddToFinalState() {
        StateMachine machine = new StateMachine();

        State finalState = State.from("B").isFinal(true).build();
        machine.add(new StateTransition(new State("A"), new StringMessage("1"), finalState));
        assertEquals(2, machine.size(), "Error building state machine");
        assertNotNull(machine.getTransitions("A"), "Error retrieving transitions");
        assertEquals(1, machine.getTransitions("A").size(), "Transitions size mismatch");
        assertEquals("A", machine.getTransitions("A").iterator().next().getOrigin().getName(), "Transition origin retrieval mismatch");
        assertTrue(machine.getTransitions("A").iterator().next().getTarget().isFinal(), "Transition target retrieval mismatch");
        assertEquals("B", machine.getTransitions("A").iterator().next().getTarget().getName(), "Transition target retrieval mismatch");
        assertEquals("1", machine.getTransitions("A").iterator().next().getMessage().getPayload().get(), "Transition message retrieval mismatch");

        getStatePrinterBuilder().compactMode(true).build().printTransitions(machine.getTransitionsIndex());

        try {
            machine.add(new StateTransition(finalState, new StringMessage("1"), new State("C")));
            fail("State Transition initialization mismatch");
        } catch (Exception e) {
            assertTrue(e instanceof TransitionInitializationException, "Exception mismatch");
        }
    }

    @Test
    public void testWrongStateAdd() {
        StateMachine machine = new StateMachine();
        expectWrongTransitionAndCheck(null, StringMessage.from("1"), new State("B"), machine);
        expectWrongTransitionAndCheck(new State("A"), null, new State("B"), machine);
        expectWrongTransitionAndCheck(new State("A"), StringMessage.from("1"), null, machine);
    }

    @Test
    public void tesDuplicateAdd() {
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
    public void tesDuplicateAddIdentity() {
        StateMachine machine = new StateMachine();

        State origin = new State("A");

        StateTransition transition = new StateTransition(origin, new StringMessage("1"), new State("B"));
        machine.add(transition);
        StateTransition transitionAlt = new StateTransition(FilteredState.from(origin), new StringMessage("1"), new State("B"));
        machine.add(transitionAlt);
        transitionAlt = new StateTransition(FilteredState.from(origin), new StringMessage("1"), new State("B"));
        machine.add(transitionAlt);

        assertEquals(2, machine.size(), "Error building state machine");
        assertNotNull(machine.getTransitions("A"), "Error retrieving transitions");
        assertEquals(1, machine.getTransitions("A").size(), "Transitions size mismatch");
        assertEquals("A", machine.getTransitions("A").iterator().next().getOrigin().getName(), "Transition origin retrieval mismatch");
        assertEquals("B", machine.getTransitions("A").iterator().next().getTarget().getName(), "Transition target retrieval mismatch");
        assertEquals("1", machine.getTransitions("A").iterator().next().getMessage().getPayload().get(), "Transition message retrieval mismatch");

        Collection<StateTransition> allTransitions = machine.getAllTransitions();
        assertNotNull(allTransitions, "Error retrieving transitions");
        assertEquals(1, allTransitions.size(), "Transitions size mismatch");
        StateTransition trFound = allTransitions.iterator().next();
        assertThat("Error comparing transitions", transition, is(trFound));
        State originFound = trFound.getOrigin();
        assertEquals(State.class, originFound.getClass(), "Class mismatch");
    }

    @Test
    public void testOverrideTransitionOnAdd() {
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
    public void testFindTransition() {
        StateMachine machine = createStateMachine();

        assertEquals(3, machine.size(), "Error building state machine");
        assertNotNull(machine.getTransitions("A"), "Error retrieving transitions");

        Arrays.asList("A", "B", "C" ).forEach(s -> {
            Optional<State> sFound = machine.find(s);
            assertTrue(sFound.isPresent(), "Error retrieving state");
            assertEquals(s, sFound.get().getName(), "Error retrieving state");
        });
    }

    @Test
    public void testRemoveStateByName() {
        StateMachine machine = createStateMachine();

        assertEquals(3, machine.size(), "Error building state machine");

        AtomicInteger counter = new AtomicInteger(3);
        Arrays.asList("A", "B", "C" ).forEach(s -> {
            Optional<State> sFound = machine.find(s);
            assertTrue(sFound.isPresent(), "Error retrieving state");
            assertEquals(s, sFound.get().getName(), "Error retrieving state");

            machine.remove(s);
            assertEquals(counter.decrementAndGet(), machine.size(), "Error removing state");
        });

        assertEquals(0, machine.size(), "Error building state machine");
    }

    @Test
    public void testRemoveStates() {
        StateMachine machine = createStateMachine();
        assertEquals(3, machine.size(), "Error building state machine");

        AtomicInteger counter = new AtomicInteger(3);
        Arrays.asList(State.from("A").build(), State.from("B").build(), State.from("C" ).build()).forEach(s -> {
            machine.remove(s);
            assertEquals(counter.decrementAndGet(), machine.size(), "Error removing state");
        });

        assertEquals(0, machine.size(), "Error building state machine");
    }

    @Test
    public void testRemoveStateTransitions() {
        StateMachine machine = createStateMachine();
        assertEquals(3, machine.size(), "Error building state machine");

        getStatePrinterBuilder().compactMode(true).build().printTransitions(machine.getTransitionsIndex());
        Collection<StateTransition> allTransitions = machine.getAllTransitions();
        allTransitions.forEach(machine::remove);
        getStatePrinterBuilder().title("After removing transitions").compactMode(true).build().printTransitions(machine.getTransitionsIndex());
        assertEquals(3, machine.size(), "Error building state machine");
        assertThat("Error removing transitions", machine.getAllTransitions().size(), is(0));
    }


    @Test
    public void testRemoveOrphans() {
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
    public void testRemoveAllTransitions() {
        StateMachine machine = createStateMachine();
        assertEquals(3, machine.size(), "Error building state machine");
        getStatePrinter().printTransitions(machine.getTransitionsIndex());
        machine.removeAllTransitions();
        getStatePrinterBuilder().compactMode(true).title("After removing transitions").build().printTransitions(machine.getTransitionsIndex());
        assertEquals(3, machine.size(), "Error building state machine");
        assertThat("Error removing transitions", machine.getAllTransitions().size(), is(0));
        machine.prune();
        assertEquals(0, machine.size(), "Error building state machine");
        assertThat("Error removing transitions", machine.getAllTransitions().size(), is(0));
    }

    @Test
    public void testStateMachineClear() {
        StateMachine machine = createStateMachine();
        assertEquals(3, machine.size(), "Error building state machine");
        getStatePrinter().printTransitions(machine.getTransitionsIndex());
        machine.clear();
        getStatePrinterBuilder().title("After clear").build().printTransitions(machine.getTransitionsIndex());
        assertEquals(0, machine.size(), "Error building state machine");
    }

    @Test
    public void testRemoveStateCascade() {
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

    @Test
    public void testRetrieveStateSiblings() {
        StateMachine machine = new StateMachine();

        machine.add(new StateTransition(new State("A"), new StringMessage("1"), new State("B")));
        machine.add(new StateTransition(new State("A"), new StringMessage("2"), new State("C")));
        machine.add(new StateTransition(new State("B"), new StringMessage("2"), new State("C")));

        Collection<State> siblings = machine.getSiblings("A");
        assertNotNull(siblings, "Error obtaining state siblings");
        assertEquals(2, siblings.size());
        assertThat("Error obtaining state siblings", siblings, containsInAnyOrder(State.named("B"), State.named("C")));
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
