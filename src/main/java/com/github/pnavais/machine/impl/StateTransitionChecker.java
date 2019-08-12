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

import com.github.pnavais.machine.api.Envelope;
import com.github.pnavais.machine.api.Message;
import com.github.pnavais.machine.api.Status;
import com.github.pnavais.machine.api.transition.TransitionChecker;
import com.github.pnavais.machine.model.FilteredState;
import com.github.pnavais.machine.model.State;

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

        State currentState = envelope.getSource();

        // Check initially that the state is not final
        if ((currentState!=null) && (!currentState.isFinal())) {
                status = (currentState instanceof FilteredState) ?
                        ((FilteredState) currentState).onDispatch(envelope.getMessage(), envelope.getTarget())
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
        return (envelope.getTarget() instanceof FilteredState) ?
                    ((FilteredState) envelope.getTarget()).onReceive(envelope.getMessage(), envelope.getSource())
                    : Status.PROCEED;
    }

}
