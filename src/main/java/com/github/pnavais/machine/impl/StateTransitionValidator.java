/*
 * Copyright 2019 Pablo Navais
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.pnavais.machine.impl;

import com.github.pnavais.machine.api.Message;
import com.github.pnavais.machine.api.Transition;
import com.github.pnavais.machine.api.exception.IllegalTransitionException;
import com.github.pnavais.machine.api.exception.NullTransitionException;
import com.github.pnavais.machine.api.exception.TransitionInitializationException;
import com.github.pnavais.machine.api.transition.TransitionIndex;
import com.github.pnavais.machine.api.validator.TransitionValidator;
import com.github.pnavais.machine.api.validator.ValidationResult;
import com.github.pnavais.machine.model.State;
import com.github.pnavais.machine.model.StateTransition;

import java.util.Map;

/**
 * Implementation of the transition validator for state transitions
 */
public class StateTransitionValidator implements TransitionValidator<State, Message, StateTransition> {

    /**
     * Checks the transition parameter correctness
     * with respect to current transitions on the given
     * index.
     *
     * @param transition    the transition to check
     * @param transitionIndex the transition index
     * @param operation the operation to accomplish
     */
    @Override
    public ValidationResult validate(Transition<State, Message> transition, TransitionIndex<State, Message, StateTransition> transitionIndex, Operation operation) {

        ValidationResult result = validateTransition(transition);

        // When removing a transition, check that it is available
        if (result.isValid() && Operation.REMOVE.equals(operation)) {
            boolean found = false;
            Map<State, Map<Message, State>> transitionsMap = transitionIndex.getTransitionsAsMap();
            Map<Message, State> transitionsFound = transitionsMap.get(transition.getOrigin());
            if (transitionsFound != null) {
                State target = transitionsFound.get(transition.getMessage());
                found = transition.getTarget().equals(target);
            }

            if (!found) {
                result.setException(new IllegalTransitionException("Cannot find transition [" + transition.getOrigin() + " -> " + transition.getTarget() + "]"));
                result.setValid(false);
            }
        }

        return result;
    }

    /**
     * Validates transition components.
     *
     * @param transition the transition to validate
     * @return the validation result
     */
    private ValidationResult validateTransition(Transition<State, Message> transition) {
        ValidationResult.ValidationResultBuilder builder = ValidationResult.builder();

        if (transition == null) {
            builder.exception(new NullTransitionException("The transition cannot be null"));
        } else {
            if (transition.getOrigin() == null || transition.getMessage() == null || transition.getTarget() == null) {
                builder.exception(new TransitionInitializationException("Cannot create transitions with null components"));
            } else {
                if (transition.getOrigin().isFinal()) {
                    builder.exception(new TransitionInitializationException("Cannot create transition from final state ["+transition.getOrigin().getName()+"]"));
                } else {
                    builder.valid(true);
                }
            }
        }
        return builder.build();
    }
}
