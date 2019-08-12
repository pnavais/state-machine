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
import com.github.pnavais.machine.api.MessageConstants;
import com.github.pnavais.machine.api.Payload;
import com.github.pnavais.machine.api.Status;
import com.github.pnavais.machine.model.FilteredState;
import com.github.pnavais.machine.model.State;
import com.github.pnavais.machine.model.StateTransition;
import com.github.pnavais.machine.model.StringMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for State Machine departure/arrival functionality
 */
public class StateMachineMessagingTest extends AbstractStateMachineTest {

    /** The buffer containing cumulative departure/arrival messages */
    private List<String> messageBuffer = new ArrayList<>();

    @BeforeEach
    public void initialize() {
        messageBuffer.clear();
    }

    @Test
    public void testReceptionAndDeparture() {
        StateMachine machine = createStateMachine();

        machine.init();
        State current = machine.send("1").getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertEquals("B", current.getName(), "Error obtaining target state");
        assertEquals(2, messageBuffer.size(), "Error obtaining messages from buffer");
        assertEquals("Departing from A to B on [1]", messageBuffer.get(0), "Error obtaining departure message");
        assertEquals("Arriving to B from A on [1]", messageBuffer.get(1), "Error obtaining arrival message");
    }

    @Test
    public void testDepartureOnFallbackMessage() {
        StateMachine machine = createStateMachine();
        machine.init();

        State current = machine.send("2").getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertEquals("C", current.getName(), "Error obtaining target state");
        assertEquals(1, messageBuffer.size(), "Error obtaining messages from buffer");
        assertEquals("Departing from A to C on [2]", messageBuffer.get(0), "Error obtaining departure message");
    }

    @Test
    public void testDepartureWithNoFallbackMessage() {
        StateMachine machine = createStateMachine();
        machine.setCurrent("B");

        State current = machine.send("3").getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertEquals("B", current.getName(), "Error obtaining target state");
        assertEquals(0, messageBuffer.size(), "Error obtaining messages from buffer");
    }

    @Test
    public void testDepartureWithPayloadMessage() {
        StateMachine machine = createStateMachine();
        machine.setCurrent("B");

        State current = machine.send("2").getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertEquals("B", current.getName(), "Error obtaining target state");
        assertEquals(0, messageBuffer.size(), "Error obtaining messages from buffer");

        current = machine.send(StringMessage.from("2", () -> false)).getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertEquals("B", current.getName(), "Error obtaining target state");
        assertEquals(0, messageBuffer.size(), "Error obtaining messages from buffer");

        current = machine.send(StringMessage.from("2", () -> true)).getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertEquals("C", current.getName(), "Error obtaining target state");
        assertEquals(1, messageBuffer.size(), "Error obtaining messages from buffer");
        assertEquals("Departing from B to C on [2]", messageBuffer.get(0), "Error obtaining departure message");
    }

    @Test
    public void testDepartureOnAnyMessage() {
        StateMachine machine = createStateMachine();
        machine.init();

        State current = machine.send(MessageConstants.ANY).getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertEquals(1, messageBuffer.size(), "Error obtaining messages from buffer");
        assertEquals("Departing from A to C on [*]", messageBuffer.get(0), "Error obtaining departure message");
    }

    /**
     * Creates a state machine with the following transitions :
     * <pre>
     * +--------+---------+--------+
     * | Source | Message | Target |
     * +--------+---------+--------+
     * |   A    |    *    |   C    |
     * +--------+---------+--------+
     * |   A    |    1    |   B    |
     * +--------+---------+--------+
     * |   B    |    2    |   C    |
     * +--------+---------+--------+
     *
     *  Which is equivalent to :
     *  +--------+---------------------+
     *  | Source |       Target        |
     *  +--------+---------------------+
     *  |   A    | [ * -> C , 1 -> B ] |
     *  +--------+---------------------+
     *  |   C    |         []          |
     *  +--------+---------------------+
     *  |   B    |     [ 2 -> C ]      |
     *  +--------+---------------------+
     * </pre>
     *
     * Additionally add a message filter for the departure from A on ANY (*) message
     * and another one for the departure/reception from/in B on ANY and 2 messages
     * respectively.
     * In case of reception in B, the operation is only accepted if the payload message
     * is composed of a boolean value evaluating to true.
     *
     * @return the test machine
     */
    @Override
    protected StateMachine createStateMachine() {
        StateMachine machine = new StateMachine();
        FilteredState stateA = FilteredState.from(new State("A"));
        FilteredState stateB = FilteredState.from(new State("B"));

        stateA.setDispatchHandler(MessageConstants.ANY, (message, state) -> {
            messageBuffer.add(String.format("Departing from A to %s on [%s]", state.getName(), message.getPayload().get()));
            return Status.PROCEED;
        });

        stateB.setReceptionHandler(MessageConstants.ANY, (message, state) -> {
            messageBuffer.add(String.format("Arriving to B from %s on [%s]", state.getName(), message.getPayload().get()));
            return Status.PROCEED;
        });

        stateB.setDispatchHandler(StringMessage.from("2"), (message, state) -> {
            Payload payload = message.getPayload();
            boolean proceed = false;
            if (payload != null) {
                if (Boolean.class.isAssignableFrom(payload.get().getClass())) {
                    proceed = (boolean) payload.get();
                }
            }
            // Append only on successful transition
            if (proceed) {
                messageBuffer.add(String.format("Departing from B to %s on [%s]", state.getName(), message));
            }

            return proceed ? Status.PROCEED : Status.ABORT;
        });

        machine.add(new StateTransition(stateA, MessageConstants.ANY, new State("C")));
        machine.add(new StateTransition(stateA, new StringMessage("1"), stateB));
        machine.add(new StateTransition(new State("B"), new StringMessage("2"), new State("C")));
        return machine;
    }
}
