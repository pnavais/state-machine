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
import com.github.pnavais.machine.api.Message;
import com.github.pnavais.machine.api.Messages;
import com.github.pnavais.machine.api.Status;
import com.github.pnavais.machine.api.VoidMessage;
import com.github.pnavais.machine.api.exception.ValidationException;
import com.github.pnavais.machine.api.filter.FunctionMessageFilter;
import com.github.pnavais.machine.api.filter.MappedFunctionMessageFilter;
import com.github.pnavais.machine.api.filter.MessageFilter;
import com.github.pnavais.machine.api.validator.TransitionValidator;
import com.github.pnavais.machine.api.validator.ValidationResult;
import com.github.pnavais.machine.impl.StateTransitionMap;
import com.github.pnavais.machine.model.State;
import com.github.pnavais.machine.model.StateTransition;
import com.github.pnavais.machine.model.StringMessage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

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
        FunctionMessageFilter<State> messageFilter = new FunctionMessageFilter<>();
        Status status = messageFilter.onDispatch(Messages.ANY, null);
        assertNotNull(status, "Error retrieving default status");
        assertTrue(status.isValid(), "Error obtaining default status validity");

        BiFunction<Message, State, Status> dispatchHandler = messageFilter.getDispatchHandler();
        assertNotNull(dispatchHandler, "Error retrieving dispatch handler");

        BiFunction<Message, State, Status> receptionHandler = messageFilter.getReceptionHandler();
        assertNotNull(receptionHandler, "Error retrieving reception handler");
    }

    @Test
    public void testNullFunctionMessageFilter() {
        FunctionMessageFilter<State> messageFilter = new FunctionMessageFilter<>();
        try {
            messageFilter.setDispatchHandler(null);
            fail("Error setting null dispatch handler");
        } catch (Exception e) {
            assertTrue(e instanceof NullPointerException, "Exception class mismatch");
        }

        try {
            messageFilter.setReceptionHandler(null);
            fail("Error setting null reception handler");
        } catch (Exception e) {
            assertTrue(e instanceof NullPointerException, "Exception class mismatch");
        }
    }

    @Test
    public void testMappedFunctionMessageFilterInit() {
        List<Message> receivedMessages = new ArrayList<>();
        MappedFunctionMessageFilter<State> messageFilter = new MappedFunctionMessageFilter<>();

        BiFunction<Message, State, Status> handler = (message, state) -> {
            receivedMessages.add(message);
            return state.getName().equals("B") ? Status.ABORT : Status.PROCEED;
        };

        messageFilter.setDispatchHandler(StringMessage.from("m1"), handler);
        messageFilter.setReceptionHandler(StringMessage.from("m1"), handler);

        testMessageFilter(messageFilter,
                messageFilter::onDispatch,
                receivedMessages);

        testMessageFilter(messageFilter,
                messageFilter::onReceive,
                receivedMessages);
    }

    @Test
    public void testFunctionMessageFilterCustomMessage() {
        List<Message> receivedMessages = new ArrayList<>();
        FunctionMessageFilter<State> messageFilter = new FunctionMessageFilter<>();
        VoidMessage custom_message = VoidMessage.createWith("CUSTOM_MESSAGE");

        BiFunction<Message, State, Status> handler = (message, state) -> {
            if (Messages.ANY.equals(message)) {
                receivedMessages.add(message);
                return Status.PROCEED;
            } else if (custom_message.getName().equals(message.getPayload().get())) {
                return custom_message.getMessageId() != message.getMessageId() ? Status.PROCEED : Status.ABORT;
            } else {
                return Status.ABORT;
            }
        };

        messageFilter.setDispatchHandler(handler);
        Status status = messageFilter.onDispatch(StringMessage.from("A"), null);
        assertNotNull(status, "Error obtaining status");
        assertEquals(Status.ABORT, status, "Status mismatch");
        assertEquals(0, receivedMessages.size(), "Error obtaining received message size");

        status = messageFilter.onDispatch(Messages.ANY, null);
        assertNotNull(status, "Error obtaining status");
        assertEquals(Status.PROCEED, status, "Status mismatch");
        assertEquals(1, receivedMessages.size(), "Error obtaining received message size");

        status = messageFilter.onDispatch(StringMessage.from("CUSTOM_MESSAGE"), null);
        assertNotNull(status, "Error obtaining status");
        assertEquals(Status.PROCEED, status, "Status mismatch");
        assertEquals(1, receivedMessages.size(), "Error obtaining received message size");
    }

    @Test
    public void testMappedFunctionMessageFilterRemoval() {
        MappedFunctionMessageFilter<State> messageFilter = new MappedFunctionMessageFilter<>();

        BiFunction<Message, State, Status> customHandler = (message, state) -> Status.builder().
                message(StringMessage.from("testMessage"))
                .statusName("TEST_STATUS")
                .validity(false)
                .build();

        BiFunction<Message, State, Status> abortHandler = (message, state) -> Status.ABORT;

        messageFilter.setDispatchHandler(Messages.ANY, abortHandler);
        messageFilter.setDispatchHandler(StringMessage.from("A"), customHandler);
        messageFilter.setReceptionHandler(Messages.ANY, abortHandler);
        messageFilter.setReceptionHandler(StringMessage.from("A"), customHandler);

        testRemovalOfMessageFilter(messageFilter, MappedFunctionMessageFilter::removeDispatchHandler, messageFilter::onDispatch);
        testRemovalOfMessageFilter(messageFilter, MappedFunctionMessageFilter::removeReceptionHandler, messageFilter::onReceive);
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
    private <S extends State, F extends MessageFilter<S>> void testRemovalOfMessageFilter(F messageFilter, BiConsumer<F, Message> removalHandler, BiFunction<Message, State, Status> handler) {
        Status status = handler.apply(StringMessage.from("A"), null);
        assertNotNull(status, "Error obtaining status");
        assertEquals("TEST_STATUS", status.getStatusName(), "Status mismatch");
        assertEquals("testMessage", status.getMessage().getPayload().get(), "Status message mismatch");

        status = handler.apply(Messages.ANY, null);
        assertNotNull(status, "Error obtaining status");
        assertEquals(Status.ABORT, status, "Status mismatch");

        removalHandler.accept(messageFilter, StringMessage.from("A"));
        status = handler.apply(StringMessage.from("A"), null);
        assertNotNull(status, "Error obtaining status");
        assertEquals(Status.ABORT, status, "Status mismatch");

        removalHandler.accept(messageFilter, Messages.ANY);
        status = handler.apply(StringMessage.from("A"), null);
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
    private void testMessageFilter(MessageFilter<State> messageFilter, BiFunction <Message, State, Status> handler, List<Message> receivedMessages) {
        receivedMessages.clear();

        Status status = messageFilter.onDispatch(StringMessage.from("m2"), State.from("C").build());
        assertNotNull(status, "Error obtaining status");
        assertTrue(status.isValid(), "Error obtaining validity");
        assertEquals(0, receivedMessages.size(), "Error receiving messages");

        status = messageFilter.onDispatch(StringMessage.from("m1"), State.from("C").build());
        assertTrue(status.isValid(), "Error obtaining validity");
        assertEquals(1, receivedMessages.size(), "Error receiving messages");
        assertEquals(StringMessage.from("m1"), receivedMessages.get(0), "Received message mismatch");

        receivedMessages.clear();
        status = messageFilter.onDispatch(StringMessage.from("m1"), State.from("B").build());
        assertFalse(status.isValid(), "Error obtaining validity");
        assertEquals(1, receivedMessages.size(), "Error receiving messages");
        assertEquals(StringMessage.from("m1"), receivedMessages.get(0), "Received message mismatch");
    }

}
