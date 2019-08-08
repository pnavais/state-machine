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
import com.github.pnavais.machine.StateMachine;
import com.github.pnavais.machine.api.Message;
import com.github.pnavais.machine.api.exception.IllegalTransitionException;
import com.github.pnavais.machine.api.exception.NullTransitionException;
import com.github.pnavais.machine.api.exception.TransitionInitializationException;
import com.github.pnavais.machine.impl.StateTransitionMap;
import com.github.pnavais.machine.model.State;
import com.github.pnavais.machine.model.StateTransition;
import com.github.pnavais.machine.model.StringMessage;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

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
    public void testTransitionInitializationFail() {
        expectWrongTransitionAndCheck(null, new StringMessage("1"), new State("B"));
        expectWrongTransitionAndCheck(new State("A"), null, new State("B"));
        expectWrongTransitionAndCheck(new State("A"), new StringMessage("1"), null);
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
    public void testStateTransitionMapInit() {
        StateTransitionMap transitionMap = new StateTransitionMap();
        transitionMap.add(new StateTransition("A", StringMessage.from("1"), "B"));
        transitionMap.add(new StateTransition("B", StringMessage.from("2"), "C"));
        assertNotNull(transitionMap.getTransitionMap(), "Error initializing state transition map");
        assertThat(transitionMap.getTransitionMap().size(), is(3));
        Arrays.asList("A", "B", "C").forEach(s -> {
            Optional<State> state = transitionMap.find(s);
            assertTrue(state.isPresent(), "Error retrieving state");
            assertThat("State retrieved mismatch", state.get().getName(), is(s));
        });
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
        transitionMap.add(aToD);
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

        //TODO Extract validation logic outside Transition class
    }

    /**
     * Expect wrong transition and check that the initialization
     * was not correct
     *
     * @param source  the source state
     * @param message the message
     * @param target  the target state
     */
    private void expectWrongTransitionAndCheck(State source, Message message, State target) {
        try {
            new StateTransition(source, message, target);
            fail("State Transition initialization mismatch");
        } catch (Exception e) {
            assertTrue(e instanceof TransitionInitializationException, "Exception mismatch");
        }
    }


}
