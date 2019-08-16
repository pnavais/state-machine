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
import com.github.pnavais.machine.api.message.Messages;
import com.github.pnavais.machine.api.message.Payload;
import com.github.pnavais.machine.api.Status;
import com.github.pnavais.machine.api.filter.MessageFilter;
import com.github.pnavais.machine.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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
            MessageFilter<State, StateContext> messageFilter = ((AbstractFilteredState) stateTransition.getOrigin()).getMessageFilter();
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
    public void testDepartureOnAnyMessageNotMapped() {
        StateMachine machine = createStateMachine();
        machine.setCurrent("B");

        State current = machine.send(Messages.ANY).getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertThat("Error retrieving current state", current.getName(), is("B"));
        assertEquals(0, messageBuffer.size(), "Error obtaining messages from buffer");
    }

    @Test
    public void testDepartureOnEmptyMessage() {
        StateMachine machine = createStateMachine();
        machine.init();

        State current = machine.send(Messages.EMPTY).getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertEquals(1, messageBuffer.size(), "Error obtaining messages from buffer");
        assertEquals("Departing from A to E on [_]", messageBuffer.get(0), "Error obtaining departure message");
    }

    @Test
    public void testDepartureOnEmptyMessageNotMapped() {
        StateMachine machine = createStateMachine();
        machine.setCurrent("B");

        State current = machine.send(Messages.EMPTY).getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertThat("Error retrieving current state", current.getName(), is("B"));
        assertEquals(0, messageBuffer.size(), "Error obtaining messages from buffer");
    }

    @Test
    public void testDepartureOnNullMessage() {
        StateMachine machine = createStateMachine();
        machine.add(new StateTransition("A", Messages.NULL, "F"));
        machine.init();

        State current = machine.send(Messages.NULL).getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertEquals(1, messageBuffer.size(), "Error obtaining messages from buffer");
        assertEquals("Departing from A to F on []", messageBuffer.get(0), "Error obtaining departure message");
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

    @Test
    public void testMessageRedirectOnReception() {
        StateMachine machine = createStateMachine();

        // Add a redirection state depending on the message
        FilteredState stateRedirect = new FilteredState(new State("F"));
        assertEquals(stateRedirect.getState(), new State("F"), "Error wrapping state");
        stateRedirect.setReceptionHandler(c -> c.getMessage().getPayload().get().equals("8") ? Status.forward(StringMessage.from("8")) : Status.PROCEED);

        machine.add(new StateTransition(new State("A"), Messages.ANY, stateRedirect));
        machine.add(new StateTransition("F", StringMessage.from("8"), "G"));

        machine.init();
        State current = machine.send("7").getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertEquals("F", current.getName(), "Target state name mismatch");

        machine.init();
        current = machine.send("8").getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertEquals("G", current.getName(), "Target state name mismatch");
    }

    @Test
    public void testMessageRedirectOnDeparture() {
        StateMachine machine = createStateMachine();

        // Add a redirection state depending on the message
        FilteredState stateRedirect = new FilteredState(new State("F"));
        stateRedirect.setDispatchHandler(c -> Status.forward(StringMessage.from("9")));

        machine.add(new StateTransition(new State("A"), Messages.ANY, stateRedirect));
        machine.add(new StateTransition("F", StringMessage.from("8"), "G"));
        machine.add(new StateTransition("F", StringMessage.from("9"), "H"));

        machine.init();
        State current = machine.send("7").getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertEquals("F", current.getName(), "Target state name mismatch");

        current = machine.send("8").getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertEquals("H", current.getName(), "Target state name mismatch");
    }

    @Test
    public void testChainedMessageRedirection() {
        StateMachine machine = createStateMachine();

        // Add a redirection state depending on the message
        FilteredState firstHop = new FilteredState(new State("F"));
        firstHop.setDispatchHandler(c -> Status.forward(StringMessage.from("9")));

        FilteredState secondHop = new FilteredState(new State("G"));
        secondHop.setReceptionHandler(c -> Status.forward(StringMessage.from("10")));

        machine.add(new StateTransition(firstHop, StringMessage.from("1"), new State("Z")));

        machine.setCurrent("F");
        State current = machine.send("1").getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertEquals("F", current.getName(), "Target state name mismatch");

        // Add second hop
        machine.add(new StateTransition(firstHop, StringMessage.from("9"), secondHop));
        current = machine.send("1").getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertEquals("G", current.getName(), "Target state name mismatch");

        // Full circle
        System.out.println("------ FULL CIRCLE ------");
        machine.add(new StateTransition(secondHop, StringMessage.from("10"), firstHop));
        machine.setCurrent("F");
        current = machine.send("1").getCurrent();
        assertNotNull(current, "Error retrieving current state");
    }

    @Test
    public void testOverrideState() {
        AtomicInteger counter = new AtomicInteger();
        FilteredState initial = new FilteredState(new State("A"));
        initial.setDispatchHandler(context -> Status.ABORT);
        FilteredState modified = new FilteredState(new State("A"));
        modified.setDispatchHandler(context -> {
            counter.getAndIncrement();
            return Status.PROCEED;
        });

        StateMachine stateMachine = new StateMachine();
        stateMachine.add(new StateTransition(initial, "1", "B"));
        stateMachine.add(new StateTransition(modified, "2", "C"));
        stateMachine.add(new StateTransition("A", "3", "D"));
        stateMachine.init();
        State current = stateMachine.send("2").getCurrent();
        assertNotNull(current, "Error obtaining current state");
        assertThat("Target State mismatch", current.getName(), is("C"));
        assertThat("Error executing dispatch handler", counter.get(), is(1));
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
     * |   A    |    _    |   E    |
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
     * +--------+-----------------------------------------+
     * | Source |            Target                       |
     * +--------+-----------------------------------------+
     * |   A    | [ * -> C* , 1 -> B , 4 -> D , _ -> E ]  |
     * +--------+-----------------------------------------+
     * |   C*   |              []                         |
     * +--------+-----------------------------------------+
     * |   B    |          [ 2 -> C* ]                    |
     * +--------+-----------------------------------------+
     * |   D    |     [ 2 -> C* , 3 -> B ]                |
     * +--------+-----------------------------------------+
     * |   E    |              []                         |
     * +--------+-----------------------------------------+
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
        FilteredState stateA = FilteredState.from(new State("A"));
        FilteredState stateB = FilteredState.from(new State("B"));
        FilteredState stateC = FilteredState.from(new State("C"));
        FilteredState stateD = FilteredState.from(new State("D"));
        stateC.setFinal(true);

        configureMessageFilters(stateA, stateB, stateD);

        machine.add(new StateTransition(stateA, Messages.ANY, stateC));
        machine.add(new StateTransition(stateA, new StringMessage("1"), stateB));
        machine.add(new StateTransition(new State("B"), new StringMessage("2"), new State("C")));
        machine.add(new StateTransition(stateA, StringMessage.from("4"), stateD));
        machine.add(new StateTransition(stateD, StringMessage.from("2"), stateC));
        machine.add(new StateTransition(stateD, StringMessage.from("3"), stateB));
        machine.add(new StateTransition(stateA, Messages.EMPTY, new State("E")));
        return machine;
    }

    /**
     * Configure the message filters
     * @param stateA the A state
     * @param stateB the B state
     * @param stateD the D state
     */
    private void configureMessageFilters(FilteredState stateA, FilteredState stateB, FilteredState stateD) {

        // Dispatch handler for State A
        stateA.setDispatchHandler(Messages.ANY, context -> {
            messageBuffer.add(String.format("Departing from A to %s on [%s]", context.getTarget().getName(), context.getMessage().getPayload()!=null? context.getMessage().getPayload().get() : ""));
            return Status.PROCEED;
        });

        // Dispatch handler for State B
        stateB.setDispatchHandler(StringMessage.from("2"), context -> {
            Payload payload = context.getMessage().getPayload();
            boolean proceed = false;
            if (payload != null) {
                if (Boolean.class.isAssignableFrom(payload.get().getClass())) {
                    proceed = (boolean) payload.get();
                }
            }
            // Append only on successful transition
            if (proceed) {
                messageBuffer.add(String.format("Departing from B to %s on [%s]", context.getTarget().getName(), context.getMessage()));
            }

            return proceed ? Status.PROCEED : Status.ABORT;
        });

        // Reception handler for State B
        stateB.setReceptionHandler(Messages.ANY, context -> {
            messageBuffer.add(String.format("Arriving to B from %s on [%s]", context.getSource().getName(), context.getMessage().getPayload().get()));
            return Status.PROCEED;
        });

        // Dispatch handler for State D
        stateD.setDispatchHandler(context -> {
            messageBuffer.add(String.format("Departing from D to %s on [%s]", context.getTarget().getName(), context.getMessage().getPayload().get()));
            return Status.PROCEED;
        });

        // Reception handler for State D
        stateD.setReceptionHandler(context -> {
            messageBuffer.add(String.format("Arriving to D from %s on [%s]", context.getSource().getName(), context.getMessage().getPayload().get()));
            return Status.PROCEED;
        });
    }
}
