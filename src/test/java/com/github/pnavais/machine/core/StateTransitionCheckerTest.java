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
import com.github.pnavais.machine.api.Envelope;
import com.github.pnavais.machine.api.Message;
import com.github.pnavais.machine.api.Status;
import com.github.pnavais.machine.api.transition.TransitionChecker;
import com.github.pnavais.machine.model.SimpleEnvelope;
import com.github.pnavais.machine.model.State;
import com.github.pnavais.machine.model.StateTransition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for State Transition checker functionality
 */
public class StateTransitionCheckerTest extends AbstractStateMachineTest {

    /** The list of envelopes received */
    private List<Envelope<State, Message>> envelopesArrival = new ArrayList<>();
    private List<Envelope<State, Message>> envelopesDeparture = new ArrayList<>();

    @BeforeEach
    public void initialize() {
        envelopesArrival.clear();
        envelopesDeparture.clear();
    }

    @Test
    public void testInitStateMachineWithCustomChecker() {
        TransitionChecker<State, Message> transitionChecker = createTransitionChecker();
        StateMachine machine = new StateMachine(transitionChecker);
        Collection<StateTransition> stateTransitions = createStateTransitions();
        machine.addAll(stateTransitions);
        machine.init();
        State current = machine.send("1").send("2").getCurrent();
        assertNotNull(current, "Error in state traversal");
        assertEquals("C", current.getName(), "Error retrieving last state");
        assertEquals(2, envelopesDeparture.size(), "Error retrieving departure messages");
        assertEquals(2, envelopesArrival.size(), "Error retrieving arrival messages");

        String[] departureStates = { "A", "B" };
        String[] arrivalStates = { "B", "C" };
        String[] messages = { "1", "2" };
        IntStream.range(0, 2).forEach(i -> {
            Envelope<State, Message> envelope = envelopesDeparture.get(i);
            assertEquals(departureStates[i], envelope.getOrigin().getName(), "Departure origin mismatch");
            assertEquals(arrivalStates[i], envelope.getTarget().getName(), "Departure origin mismatch");
            assertEquals(messages[i], envelope.getMessage().getPayload().get().toString(), "Message mismatch");
            assertTrue(envelope instanceof SimpleEnvelope, "Error obtaining envelope instance");
            assertEquals(stateTransitions, ((SimpleEnvelope) envelope).getTransitionIndex().getAllTransitions(), "Transition index mismatch");
            assertEquals(envelopesDeparture.get(i), envelopesArrival.get(i), "Envelops mismatch");
            assertEquals(envelopesDeparture.get(i).hashCode(), envelopesArrival.get(i).hashCode(), "Envelops mismatch");
        });

    }

    /**
     * Creates a custom transition checker with pass-all default
     * functionality just to ensure that the correct parameters are supplied
     *
     * @return the test transition checker
     */
    private TransitionChecker<State, Message> createTransitionChecker() {
        return new TransitionChecker<State, Message>() {

            /**
             * Validates the departure from the current state upon reception
             * of a given message using the specified transition index to ensure
             * transitions are accepted.
             *
             * @param envelope the envelope with the received message
             * @return the status of the operation
             */
            @Override
            public Status validateDeparture(Envelope<State, Message> envelope) {
                envelopesDeparture.add(envelope);
                return Status.PROCEED;
            }

            /**
             * Validates the arrival to a given target state upon reception
             * of a given message using the specified transition index to ensure
             * transitions are accepted.
             *
             * @param envelope the envelope with the received message
             * @return the status of the operation
             */
            @Override
            public Status validateArrival(Envelope<State, Message> envelope) {
                envelopesArrival.add(envelope);
                return Status.PROCEED;
            }
        };
    }
}
