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

package com.github.pnavais.machine.impl;

import com.github.pnavais.machine.api.message.Envelope;
import com.github.pnavais.machine.api.message.Event;
import com.github.pnavais.machine.api.message.Message;
import com.github.pnavais.machine.api.Status;
import com.github.pnavais.machine.api.transition.TransitionChecker;
import com.github.pnavais.machine.model.AbstractFilteredState;
import com.github.pnavais.machine.model.State;
import com.github.pnavais.machine.model.StateContext;

/**
 * Implements the check between state transitions
 */
public class StateTransitionChecker implements TransitionChecker<State, Message> {

    /**
     * Validates the departure from the current state upon reception
     * of a given message using the specified transition index to ensure
     * transitions are accepted.
     *
     * @param envelope the received message
     * @return the status of the operation
     */
    @Override
    public Status validateDeparture(Envelope<State,Message> envelope) {
        Status status = Status.ABORT;

        State currentState = envelope.getOrigin();

        // Check initially that the state is not final
        if ((currentState!=null) && (!currentState.isFinal())) {
                status = (currentState instanceof AbstractFilteredState) ?
                        ((AbstractFilteredState) currentState).onDispatch(createContext(Event.DEPARTURE, envelope))
                        : Status.PROCEED;
        }

        return status;
    }

    /**
     * Validates the arrival to a given target state upon reception
     * of a given message using the specified transition index to ensure
     * transitions are accepted.
     *
     * @param envelope         the received message
     * @return the status of the operation
     */
    @Override
    public Status validateArrival(Envelope<State, Message> envelope) {
        return (envelope.getTarget() instanceof AbstractFilteredState) ?
                    ((AbstractFilteredState) envelope.getTarget()).onReceive(createContext(Event.ARRIVAL, envelope))
                    : Status.PROCEED;
    }

    /**
     * Creates the State context from the current event and envelope
     *
     * @param event the event
     * @param envelope the envelope
     * @return the build state context
     */
    private StateContext createContext(Event event, Envelope<State, Message> envelope) {
        return StateContext.builder()
                .event(event)
                .source(envelope.getOrigin())
                .message(envelope.getMessage())
                .target(envelope.getTarget())
                .build();
    }

}
