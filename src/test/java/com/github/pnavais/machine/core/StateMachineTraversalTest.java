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
import com.github.pnavais.machine.api.Status;
import com.github.pnavais.machine.api.exception.NullStateException;
import com.github.pnavais.machine.api.message.Messages;
import com.github.pnavais.machine.model.FilteredState;
import com.github.pnavais.machine.model.State;
import com.github.pnavais.machine.model.StateTransition;
import com.github.pnavais.machine.model.StringMessage;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for State Machine node traversal functionality
 */
public class StateMachineTraversalTest extends AbstractStateMachineTest {

    @Test
    public void testRetrieveCurrentState() {
        StateMachine machine = createStateMachine();
        assertNull(machine.getCurrent(), "Current state should");
        machine.init();
        assertNotNull(machine.getCurrent(), "Error retrieving current state");
        assertEquals("A", machine.getCurrent().getName(), "Current state name mismatch");
        getStatePrinterBuilder().title(null).compactMode(true).build().printTransitions(machine.getTransitionsIndex());
    }

    @Test
    public void testRetrievePreviousState() {
        StateMachine machine = createStateMachine();
        assertNull(machine.getCurrent(), "Current state should");
        machine.init();
        State initial = machine.getCurrent();
        State current = machine.send("1b").getCurrent();
        assertNotNull(current, "Error obtaining current state");
        assertEquals("B", current.getName(), "Error transition to next state");
        Optional<State> previous = machine.getTransitionsIndex().getPrevious(current, StringMessage.from("1b"));
        assertTrue(previous.isPresent(), "Error obtaining previous state");
        assertThat("Previous state mismatch", previous.get(), is(initial));
    }

    @Test
    public void testSetUnavailableCurrentState() {
        StateMachine machine = createStateMachine();
        assertNull(machine.getCurrent(), "Current state should");
        assertThrows(NullStateException.class, () -> machine.setCurrent("Z"), "Error setting current state");
        assertThrows(NullPointerException.class, () -> machine.setCurrent((State) null), "Error setting current state");
    }

    @Test
    public void testRetrieveNextStateFromCurrent() {
        StateMachine machine = createStateMachine();
        assertNull(machine.getCurrent(), "Current state should");
        machine.init();
        assertNotNull(machine.getCurrent(), "Error retrieving current state");
        assertEquals("A", machine.getCurrent().getName(), "Current state name mismatch");

        Optional<State> next = machine.getNext(StringMessage.from("1d"));
        assertFalse(next.isPresent(), "Error retrieving next state");
        next = machine.getNext(StringMessage.from("1b"));
        assertTrue(next.isPresent(), "Error retrieving next state");
        assertThat("Next state mismatch", next.get().getName(), is("B"));
        assertThat(machine.getCurrent().getName(), is("B"));
    }

    @Test
    public void testRetrieveNextStateFromFinal() {
        StateMachine machine = createStateMachine();
        assertNull(machine.getCurrent(), "Current state should");
        machine.setCurrent(State.from("E").build());
        assertNotNull(machine.getCurrent(), "Error retrieving current state");
        assertEquals("E", machine.getCurrent().getName(), "Current state name mismatch");

        Optional<State> next = machine.getNext(StringMessage.from("3"));
        assertFalse(next.isPresent(), "Error retrieving next state");
        machine.setCurrent("E");
        next = machine.getNext(StringMessage.from("3"));
        assertFalse(next.isPresent(), "Error retrieving next state");

        machine.setCurrent("D");
        next = machine.getNext(StringMessage.from("3"));
        assertTrue(next.isPresent(), "Error retrieving next state");
        assertThat("Error retrieving next state", next.get().getName(), is("E"));
    }

    @Test
    public void testCompleteStateTraversal() {
        StateMachine machine = createStateMachine();
        machine.init();
        State current = machine.send("1b").send("2").getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertThat("Error retrieving", current.getName(), is("C"));
        assertTrue(current.isFinal(), "Error retrieving last state");
    }

    @Test
    public void testLoopStateTraversal() {
        AtomicBoolean loopComplete = new AtomicBoolean(false);
        StateMachine machine = createStateMachine();
        FilteredState stateF = new FilteredState(new State("F"));
        stateF.setReceptionHandler(context -> {
            if (context.getSource().getName().equals("F")) {
                loopComplete.set(true);
            }
            return Status.PROCEED;
        });


        machine.add(new StateTransition(stateF, StringMessage.from("self"), stateF));
        machine.add(new StateTransition("F", StringMessage.from("toA"), "A"));
        machine.add(new StateTransition("A", StringMessage.from("toF"), "F"));

        getStatePrinterBuilder().compactMode(true).build().printTransitions(machine.getTransitionsIndex());

        machine.setCurrent("F");
        State current = machine.send("toA").getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertThat("Error retrieving", current.getName(), is("A"));
        assertFalse(loopComplete.get(), "Error checking self loop");

        current = machine.send("toF").getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertThat("Error retrieving", current.getName(), is("F"));
        assertFalse(loopComplete.get(), "Error checking self loop");

        current = machine.send("self").getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertThat("Error retrieving", current.getName(), is("F"));
        assertTrue(loopComplete.get(), "Error checking self loop");
    }

    @Test
    public void testStateMachineEmptyStateTraversal() {
        StateMachine machine = StateMachine.newBuilder()
                .from("A").to("B").build();

        assertEquals("B", machine.next().getCurrent().getName(), "Error traversing machine with empty message");
        machine.init();
        assertEquals("B", machine.send(Messages.EMPTY).getCurrent().getName(), "Error traversing machine with empty message");
        machine.init();
        assertEquals("A", machine.send(Messages.ANY).getCurrent().getName(), "Error traversing machine with empty message");
    }

    /**
     * Creates a state Machine for test purposes
     * with the following transitions :
     *<pre>
     * +--------+------------------------+
     * | Source |        Target          |
     * +--------+------------------------+
     * |   A    | [ 1b -> B , 1c -> C* ] |
     * +--------+------------------------+
     * |   B    |      [ 2 -> C* ]       |
     * +--------+------------------------+
     * |   C*   |          []            |
     * +--------+------------------------+
     * |   D    |      [ 3 -> E* ]       |
     * +--------+------------------------+
     * |   E*   |          []            |
     * +--------+------------------------+
     *</pre>
     * @return the test state machine
     */
    @Override
    protected StateMachine createStateMachine() {
        StateMachine machine = new StateMachine();

        machine.add(new StateTransition(new State("A"), new StringMessage("1b"), new State("B")));
        machine.add(new StateTransition(new State("A"), new StringMessage("1c"), State.from("C").isFinal(true).build()));
        machine.add(new StateTransition(new State("B"), new StringMessage("2"), new State("C")));
        machine.add(new StateTransition(new State("D"), new StringMessage("3"), State.from("E").isFinal(true).build()));

        return machine;
    }

}
