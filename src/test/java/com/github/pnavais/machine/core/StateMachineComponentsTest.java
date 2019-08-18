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
import com.github.pnavais.machine.api.Status;
import com.github.pnavais.machine.api.exception.ValidationException;
import com.github.pnavais.machine.api.filter.Context;
import com.github.pnavais.machine.api.filter.FunctionMessageFilter;
import com.github.pnavais.machine.api.filter.MessageFilter;
import com.github.pnavais.machine.api.message.Event;
import com.github.pnavais.machine.api.message.Message;
import com.github.pnavais.machine.api.message.Messages;
import com.github.pnavais.machine.api.message.VoidMessage;
import com.github.pnavais.machine.api.validator.TransitionValidator;
import com.github.pnavais.machine.api.validator.ValidationResult;
import com.github.pnavais.machine.impl.StateTransitionMap;
import com.github.pnavais.machine.model.State;
import com.github.pnavais.machine.model.StateContext;
import com.github.pnavais.machine.model.StateTransition;
import com.github.pnavais.machine.model.StringMessage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for State machine components
 */
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
    public void testTransitionIdentity() {
        StateTransition transition = new StateTransition(new State("A"), new StringMessage("1"), new State("B"));
        StateTransition transitionAlt = new StateTransition(new State("A"), new StringMessage("1"), new State("B"));
        StateTransition transitionNew = new StateTransition(new State("A"), new StringMessage("1"), new State("C"));

        assertEquals(transition, transitionAlt, "Error comparing transitions");
        assertEquals(transition.hashCode(), transitionAlt.hashCode(), "Error comparing hash codes");
        assertNotEquals(transition, transitionNew, "Error comparing transitions");
    }

    @Test
    public void testCustomValidator() {
        TransitionValidator<State, Message, StateTransition> validator =
                (transition, transitionIndex, operation) -> ValidationResult.fail("Test validator");

        ValidationResult result = validator.validate(null, null, TransitionValidator.Operation.ADD);
        assertNotNull(result, "Error executing validator");
        assertNotNull(result.getDescription(), "Error obtaining validation result description");
        assertNull(result.getException(), "Error obtaining validation result exception");

        StateTransitionMap transitionMap = new StateTransitionMap(validator);

        try {
            transitionMap.add(new StateTransition("A", new StringMessage("1"), "B"));
            fail("Validation failed");
        } catch (Exception e) {
            assertTrue(e instanceof ValidationException, "Validation exception mismatch");
            assertEquals(result.getDescription(), e.getMessage(), "Error obtaining exception description");
        }
    }

    @Test
    public void testFunctionMessageFilterInit() {
        FunctionMessageFilter<State, StateContext> messageFilter = new FunctionMessageFilter<>();
        Status status = messageFilter.onDispatch(StateContext.builder().message(Messages.ANY).build());
        assertNotNull(status, "Error retrieving default status");
        assertTrue(status.isValid(), "Error obtaining default status validity");

        Function<StateContext, Status> dispatchHandler = messageFilter.getDispatchHandler(Messages.ANY);
        assertNotNull(dispatchHandler, "Error retrieving dispatch handler");

        Function<StateContext, Status> receptionHandler = messageFilter.getReceptionHandler(Messages.ANY);
        assertNotNull(receptionHandler, "Error retrieving reception handler");
    }

    @Test
    public void testNullFunctionMessageFilter() {
        FunctionMessageFilter<State, StateContext> messageFilter = new FunctionMessageFilter<>();
        try {
            messageFilter.setDispatchHandler(Messages.ANY, null);
            fail("Error setting null dispatch handler");
        } catch (Exception e) {
            assertTrue(e instanceof NullPointerException, "Exception class mismatch");
        }

        try {
            messageFilter.setReceptionHandler(Messages.ANY, null);
            fail("Error setting null reception handler");
        } catch (Exception e) {
            assertTrue(e instanceof NullPointerException, "Exception class mismatch");
        }
    }

    @Test
    public void testMappedFunctionMessageFilterInit() {
        List<Message> receivedMessages = new ArrayList<>();
        FunctionMessageFilter<State, StateContext> messageFilter = new FunctionMessageFilter<>();

        Function<StateContext, Status> handler = context -> {
            receivedMessages.add(context.getMessage());
            State state = (context.getEvent()== Event.ARRIVAL) ? context.getSource() : context.getTarget();
            return state.getName().equals("B") ? Status.ABORT : Status.PROCEED;
        };

        messageFilter.setDispatchHandler(StringMessage.from("m1"), handler);
        messageFilter.setReceptionHandler(StringMessage.from("m1"), handler);

        testMessageFilter(messageFilter,
                messageFilter::onDispatch,
                receivedMessages, Event.DEPARTURE);

        testMessageFilter(messageFilter,
                messageFilter::onReceive,
                receivedMessages, Event.ARRIVAL);
    }

    @Test
    public void testFunctionMessageFilterCustomMessage() {
        List<Message> receivedMessages = new ArrayList<>();
        FunctionMessageFilter<State, StateContext> messageFilter = new FunctionMessageFilter<>();
        VoidMessage custom_message = VoidMessage.createWith("CUSTOM_MESSAGE");

        Function<StateContext, Status> handler = context -> {
            if (Messages.ANY.equals(context.getMessage())) {
                receivedMessages.add(context.getMessage());
                return Status.PROCEED;
            } else if (custom_message.getName().equals(context.getMessage().getPayload().get())) {
                return custom_message.getMessageId() != context.getMessage().getMessageId() ? Status.PROCEED : Status.ABORT;
            } else {
                return Status.ABORT;
            }
        };

        messageFilter.setDispatchHandler(Messages.ANY, handler);
        Status status = messageFilter.onDispatch(StateContext.builder().message(StringMessage.from("A")).build());
        assertNotNull(status, "Error obtaining status");
        assertEquals(Status.ABORT, status, "Status mismatch");
        assertEquals(0, receivedMessages.size(), "Error obtaining received message size");

        status = messageFilter.onDispatch(StateContext.builder().message(Messages.ANY).build());
        assertNotNull(status, "Error obtaining status");
        assertEquals(Status.PROCEED, status, "Status mismatch");
        assertEquals(1, receivedMessages.size(), "Error obtaining received message size");

        status = messageFilter.onDispatch(StateContext.builder().message(StringMessage.from("CUSTOM_MESSAGE")).build());
        assertNotNull(status, "Error obtaining status");
        assertEquals(Status.PROCEED, status, "Status mismatch");
        assertEquals(1, receivedMessages.size(), "Error obtaining received message size");
    }

    @Test
    public void testMappedFunctionMessageFilterRemoval() {
        FunctionMessageFilter<State, StateContext> messageFilter = new FunctionMessageFilter<>();

        Function<StateContext, Status> customHandler = context -> Status.builder().
                message(StringMessage.from("testMessage"))
                .statusName("TEST_STATUS")
                .validity(false)
                .build();

        Function<StateContext,Status> abortHandler = context -> Status.ABORT;

        messageFilter.setDispatchHandler(Messages.ANY, abortHandler);
        messageFilter.setDispatchHandler(StringMessage.from("A"), customHandler);
        messageFilter.setReceptionHandler(Messages.ANY, abortHandler);
        messageFilter.setReceptionHandler(StringMessage.from("A"), customHandler);

        testRemovalOfMessageFilter(messageFilter, FunctionMessageFilter::removeDispatchHandler, messageFilter::onDispatch);
        testRemovalOfMessageFilter(messageFilter, FunctionMessageFilter::removeReceptionHandler, messageFilter::onReceive);
    }


    @Test
    public void testAddPropertiesToState() {
        State state = new State("A");
        assertFalse(state.hasProperties(), "Error obtaining properties count");

        IntStream.range(1, 5).forEach(i -> state.addProperty("prop"+i, "value"+i));
        assertTrue(state.hasProperties(), "Error obtaining properties count");
        assertThat("Error retrieving properties", state.getProperties().size(), is(4));

        IntStream.range(1, 5).forEach(i -> {
            assertTrue(state.hasProperty("prop"+i), "Error obtaining property");
            Optional<String> property = state.getProperty("prop" + i);
            assertTrue((property.isPresent()), "Property cannot be empty");
            assertThat("Property value mismatch", property.get(), is("value"+i));
        });

        assertThat("Error obtaining non existing property", state.getProperty("prop" + 5), is(Optional.empty()));
    }

    @Test
    public void testRemovePropertiesFromState() {
        State state = new State("A");
        assertFalse(state.hasProperties(), "Error obtaining properties count");

        IntStream.range(1, 5).forEach(i -> state.addProperty("prop"+i, "value"+i));
        assertTrue(state.hasProperties(), "Error obtaining properties count");
        assertThat("Error retrieving properties", state.getProperties().size(), is(4));

        IntStream.range(1, 5).forEach(i -> {
            state.removeProperty("prop"+i);
            assertFalse(state.hasProperty("prop"+i), "Error removing property");
            Optional<String> property = state.getProperty("prop" + i);
            assertFalse((property.isPresent()), "Property should be empty");
            assertThat("Error obtaining non existing property", property, is(Optional.empty()));
            assertThat("Error removing property", state.getProperties().size(), is(4-i));
        });

        assertThat("Error obtaining non existing property", state.getProperty("prop" + 5), is(Optional.empty()));
    }

    /**
     * Test the effects of removing a dispatch/reception handler from a given
     * message filter
     * @param messageFilter the message filter
     * @param removalHandler the removal handler
     * @param handler the dispatch/reception handler
     * @param <S> the node type
     * @param <F> the filter type
     */
    private <S extends State, C extends Context<S>, F extends MessageFilter<S, C>> void testRemovalOfMessageFilter(F messageFilter, BiConsumer<F, Message> removalHandler, Function<StateContext, Status> handler) {
        Status status = handler.apply(StateContext.builder().message(StringMessage.from("A")).build());
        assertNotNull(status, "Error obtaining status");
        assertEquals("TEST_STATUS", status.getStatusName(), "Status mismatch");
        assertEquals("testMessage", status.getMessage().getPayload().get(), "Status message mismatch");

        status = handler.apply(StateContext.builder().message(Messages.ANY).build());
        assertNotNull(status, "Error obtaining status");
        assertEquals(Status.ABORT, status, "Status mismatch");

        removalHandler.accept(messageFilter, StringMessage.from("A"));
        status = handler.apply(StateContext.builder().message(StringMessage.from("A")).build());
        assertNotNull(status, "Error obtaining status");
        assertEquals(Status.ABORT, status, "Status mismatch");

        removalHandler.accept(messageFilter, Messages.ANY);
        status = handler.apply(StateContext.builder().message(StringMessage.from("A")).build());
        assertNotNull(status, "Error obtaining status");
        assertTrue(status.isValid(),"Status mismatch");
    }

    /**
     * Test messages filter message reception.
     *
     * @param messageFilter    the message filter
     * @param handler          the handler
     * @param receivedMessages the received messages
     */
    private void testMessageFilter(MessageFilter<State, StateContext> messageFilter, Function <StateContext, Status> handler, List<Message> receivedMessages, Event event) {
        receivedMessages.clear();

        // Generic function to handle arrival/departure
        BiFunction<StateContext.StateContextBuilder, State, StateContext.StateContextBuilder> function = (stateContextBuilder, state) -> {
            if (event == Event.ARRIVAL) {
                stateContextBuilder.source(state);
            } else {
                stateContextBuilder.target(state);
            }

            return stateContextBuilder;
        };

        Status status = messageFilter.onDispatch(function.apply(StateContext.builder().message(StringMessage.from("m2")), State.from("C").build()).event(event).build());
        assertNotNull(status, "Error obtaining status");
        assertTrue(status.isValid(), "Error obtaining validity");
        assertEquals(0, receivedMessages.size(), "Error receiving messages");

        status = messageFilter.onDispatch(function.apply(StateContext.builder().message(StringMessage.from("m1")), State.from("C").build()).event(event).build());
        assertTrue(status.isValid(), "Error obtaining validity");
        assertEquals(1, receivedMessages.size(), "Error receiving messages");
        assertEquals(StringMessage.from("m1"), receivedMessages.get(0), "Received message mismatch");

        receivedMessages.clear();
        status = messageFilter.onDispatch(function.apply(StateContext.builder().message(StringMessage.from("m1")), State.from("B").build()).event(event).build());
        assertFalse(status.isValid(), "Error obtaining validity");
        assertEquals(1, receivedMessages.size(), "Error receiving messages");
        assertEquals(StringMessage.from("m1"), receivedMessages.get(0), "Received message mismatch");
    }


}
