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
import com.github.pnavais.machine.api.Messages;
import com.github.pnavais.machine.api.Payload;
import com.github.pnavais.machine.api.Status;
import com.github.pnavais.machine.api.filter.MessageFilter;
import com.github.pnavais.machine.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

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
    public void testStateMachineWithFilteringInit() {
        StateMachine machine = createStateMachine();
        Collection<StateTransition> transitions = machine.getAllTransitions();
        assertNotNull(transitions, "Error retrieving initial state");
        transitions.forEach(stateTransition -> {
            assertTrue(stateTransition.getOrigin() instanceof AbstractFilteredState, "Error obtaining transition origin filtered class");
            MessageFilter<State> messageFilter = ((AbstractFilteredState) stateTransition.getOrigin()).getMessageFilter();
            assertNotNull(messageFilter, "Error retrieving message filter");
        });
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

        State current = machine.send(Messages.ANY).getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertEquals(1, messageBuffer.size(), "Error obtaining messages from buffer");
        assertEquals("Departing from A to C on [*]", messageBuffer.get(0), "Error obtaining departure message");
    }

    @Test
    public void testDepartureOnFinalFilteredState() {
        StateMachine machine = createStateMachine();

        machine.init();
        machine.setCurrent("C");
        State current = machine.send(Messages.ANY).getCurrent();
        assertEquals("C", current.getName(), "Error obtaining target state");
        assertNotNull(current, "Error retrieving current state");
        assertEquals(0, messageBuffer.size(), "Error obtaining messages from buffer");

        Optional<State> next = machine.getNext(Messages.ANY);
        assertEquals(Optional.empty(), next, "Error obtaining next from final state");
    }

    @Test
    public void testDepartureOnGlobalFilteredState() {
        StateMachine machine = createStateMachine();
        machine.init();
        State current = machine.send("4").getCurrent();
        assertEquals("D", current.getName(), "Error obtaining target state");
        assertNotNull(current, "Error retrieving current state");
        assertEquals(2, messageBuffer.size(), "Error obtaining messages from buffer");
        assertEquals("Departing from A to D on [4]", messageBuffer.get(0), "Error obtaining departure message");
        assertEquals("Arriving to D from A on [4]", messageBuffer.get(1), "Error obtaining departure message");

        current = machine.send("2").getCurrent();
        assertEquals("C", current.getName(), "Error obtaining target state");
        assertNotNull(current, "Error retrieving current state");
        assertEquals(3, messageBuffer.size(), "Error obtaining messages from buffer");
        assertEquals("Departing from D to C on [2]", messageBuffer.get(2), "Error obtaining departure message");

        machine.setCurrent("D");
        current = machine.send("3").getCurrent();
        assertEquals("B", current.getName(), "Error obtaining target state");
        assertNotNull(current, "Error retrieving current state");
        assertEquals(5, messageBuffer.size(), "Error obtaining messages from buffer");
        assertEquals("Departing from D to B on [3]", messageBuffer.get(3), "Error obtaining departure message");
        assertEquals("Arriving to B from D on [3]", messageBuffer.get(4), "Error obtaining departure message");
    }

    /**
     * Creates a state machine with the following transitions :
     * <pre>
     * +--------+---------+--------+
     * | Source | Message | Target |
     * +--------+---------+--------+
     * |   A    |    *    |   C*   |
     * +--------+---------+--------+
     * |   A    |    1    |   B    |
     * +--------+---------+--------+
     * |   A    |    4    |   D    |
     * +--------+---------+--------+
     * |   B    |    2    |   C*   |
     * +--------+---------+--------+
     * |   D    |    2    |   C*   |
     * +--------+---------+--------+
     * |   D    |    3    |   B    |
     * +--------+---------+--------+
     * </pre>
     * Which is equivalent to :
     * <pre>
     * +--------+-------------------------------+
     * | Source |            Target             |
     * +--------+-------------------------------+
     * |   A    | [ * -> C* , 1 -> B , 4 -> D ] |
     * +--------+-------------------------------+
     * |   C*   |              []               |
     * +--------+-------------------------------+
     * |   B    |          [ 2 -> C* ]          |
     * +--------+-------------------------------+
     * |   D    |     [ 2 -> C* , 3 -> B ]      |
     * +--------+-------------------------------+
     * </pre>
     *
     * Additionally adds a mapped message filter for the departure from A on ANY (*) message
     * , another one for the departure/reception from/in B on ANY and 2 messages
     * respectively and a last simple function filter  for the departure/reception from/in D.
     * In case of reception in B, the operation is only accepted if the payload message
     * is composed of a boolean value evaluating to true.
     *
     * @return the test machine
     */
    @Override
    protected StateMachine createStateMachine() {
        StateMachine machine = new StateMachine();
        MappedFilteredState stateA = MappedFilteredState.from(new State("A"));
        MappedFilteredState stateB = MappedFilteredState.from(new State("B"));
        MappedFilteredState stateC = MappedFilteredState.from(new State("C"));
        FilteredState stateD = FilteredState.from(new State("D"));
        stateC.setFinal(true);

        configureMessageFilters(stateA, stateB, stateD);

        machine.add(new StateTransition(stateA, Messages.ANY, stateC));
        machine.add(new StateTransition(stateA, new StringMessage("1"), stateB));
        machine.add(new StateTransition(new State("B"), new StringMessage("2"), new State("C")));
        machine.add(new StateTransition(stateA, StringMessage.from("4"), stateD));
        machine.add(new StateTransition(stateD, StringMessage.from("2"), stateC));
        machine.add(new StateTransition(stateD, StringMessage.from("3"), stateB));
        return machine;
    }

    /**
     * Configure the message filters
     * @param stateA the A state
     * @param stateB the B state
     * @param stateD the D state
     */
    private void configureMessageFilters(MappedFilteredState stateA, MappedFilteredState stateB, FilteredState stateD) {

        // Dispatch handler for State A
        stateA.setDispatchHandler(Messages.ANY, (message, state) -> {
            messageBuffer.add(String.format("Departing from A to %s on [%s]", state.getName(), message.getPayload().get()));
            return Status.PROCEED;
        });

        // Dispatch handler for State B
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

        // Reception handler for State B
        stateB.setReceptionHandler(Messages.ANY, (message, state) -> {
            messageBuffer.add(String.format("Arriving to B from %s on [%s]", state.getName(), message.getPayload().get()));
            return Status.PROCEED;
        });

        // Dispatch handler for State D
        stateD.setDispatchHandler((message, state) -> {
            messageBuffer.add(String.format("Departing from D to %s on [%s]", state.getName(), message.getPayload().get()));
            return Status.PROCEED;
        });

        // Reception handler for State D
        stateD.setReceptionHandler((message, state) -> {
            messageBuffer.add(String.format("Arriving to D from %s on [%s]", state.getName(), message.getPayload().get()));
            return Status.PROCEED;
        });
    }
}
