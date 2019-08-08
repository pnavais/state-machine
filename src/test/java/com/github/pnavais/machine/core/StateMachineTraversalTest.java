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
import com.github.pnavais.machine.api.exception.NullStateException;
import com.github.pnavais.machine.model.State;
import com.github.pnavais.machine.model.StateTransition;
import com.github.pnavais.machine.model.StringMessage;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for State Machine node traversal functionality
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
    public void testSetUnavailableCurrentState() {
        StateMachine machine = createStateMachine();
        assertNull(machine.getCurrent(), "Current state should");
        try {
            machine.setCurrent("Z");
            fail("Cannot set current state to unavailable one");
            machine.setCurrent((State)null);
            fail("Cannot set current state to null");
        } catch (Exception e) {
            assertTrue(e instanceof NullStateException, "Error setting current state");
        }
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

    /**
     * Creates a state Machine for test purposes
     * with the following transitions :
     *
     * +--------+-----------------------+
     * | Source |        Target         |
     * +--------+-----------------------+
     * |   A    | [ 1b -> B , 1c -> C ] |
     * +--------+-----------------------+
     * |   B    |      [ 2 -> C ]       |
     * +--------+-----------------------+
     * |   C    |          []           |
     * +--------+-----------------------+
     * |   D    |      [ 3 -> E ]       |
     * +--------+-----------------------+
     * |   E    |          []           |
     * +--------+-----------------------+
     *
     * @return the test state machine
     */
    private StateMachine createStateMachine() {
        StateMachine machine = new StateMachine();

        machine.add(new StateTransition(new State("A"), new StringMessage("1b"), new State("B")));
        machine.add(new StateTransition(new State("A"), new StringMessage("1c"), new State("C")));
        machine.add(new StateTransition(new State("B"), new StringMessage("2"), new State("C")));
        machine.add(new StateTransition(new State("D"), new StringMessage("3"), State.from("E").isFinal(true).build()));

        return machine;
    }

}
