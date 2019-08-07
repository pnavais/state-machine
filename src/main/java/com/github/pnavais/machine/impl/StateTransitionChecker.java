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

import com.github.pnavais.machine.api.Message;
import com.github.pnavais.machine.api.Status;
import com.github.pnavais.machine.api.transition.TransitionChecker;
import com.github.pnavais.machine.api.transition.TransitionIndex;
import com.github.pnavais.machine.model.FilteredState;
import com.github.pnavais.machine.model.State;
import com.github.pnavais.machine.model.StateTransition;

import java.util.Optional;

/**
 * Implements the check between state transitions
 */
public class StateTransitionChecker implements TransitionChecker<State, Message, StateTransition> {

    /**
     * Validates the departure from the current state upon reception
     * of a given message using the specified transition index to ensure
     * transitions are accepted.
     *
     * @param transitionsIndex the transition index
     * @param message          the received message
     * @param currentState     the current state
     * @return the status of the operation
     */
    @Override
    public Status validateDeparture(TransitionIndex<State, Message, StateTransition> transitionsIndex, Message message, State currentState) {
        Status status = Status.ABORT;

        // Check initially that the state is not final
        if ((currentState!=null) && (!currentState.isFinal())) {
            Optional<State> targetState = transitionsIndex.getNext(currentState, message);
            status = (targetState.isPresent()) && (currentState instanceof FilteredState) ?
                    ((FilteredState) currentState).onDispatch(message, targetState.get())
                    : Status.PROCEED;
        }

        return status;
    }

    /**
     * Validates the arrival to a given target state upon reception
     * of a given message using the specified transition index to ensure
     * transitions are accepted.
     *
     * @param transitionsIndex the transition index
     * @param message          the received message
     * @param targetState      the target state
     * @return the status of the operation
     */
    @Override
    public Status validateArrival(TransitionIndex<State, Message, StateTransition> transitionsIndex, Message message, State targetState) {
        Optional<State> sourceState = transitionsIndex.getPrevious(targetState, message);
        return (sourceState.isPresent()) && (targetState instanceof FilteredState) ?
                ((FilteredState) targetState).onReceive(message, sourceState.get())
                : Status.PROCEED;
    }
}
