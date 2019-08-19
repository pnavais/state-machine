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

import com.github.pnavais.machine.api.AbstractNode;
import com.github.pnavais.machine.api.Transition;
import com.github.pnavais.machine.api.exception.IllegalTransitionException;
import com.github.pnavais.machine.api.exception.NullTransitionException;
import com.github.pnavais.machine.api.exception.TransitionInitializationException;
import com.github.pnavais.machine.api.exception.ValidationException;
import com.github.pnavais.machine.api.message.Message;
import com.github.pnavais.machine.api.transition.TransitionIndex;
import com.github.pnavais.machine.api.validator.TransitionValidator;
import com.github.pnavais.machine.api.validator.ValidationResult;
import com.github.pnavais.machine.model.State;
import com.github.pnavais.machine.model.StateTransition;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Optional;

/**
 * Implementation of the transition validator for state transitions.
 * Validation is executed in a two step process. Initially all components
 * of the transition are validated and later the actual operation is
 * validated.
 * This validator poses no restrictions when adding transitions although
 * the latter are searched in the transition when trying to remove them.
 * In case validation fails, by default {@link ValidationException} are raised automatically.
 */
@Getter
@Setter
public class StateTransitionValidator implements TransitionValidator<State, Message, StateTransition> {

    /** The failure policy */
    private FailurePolicy failurePolicy = FailurePolicy.THROW_ON_FAILURE;

    /**
     * Checks the transition parameter correctness
     * with respect to current transitions on the given
     * index before applying the operation.
     *
     * @param transition    the transition to check
     * @param transitionIndex the transition index
     * @param operation the operation to accomplish
     */
    @Override
    public ValidationResult validate(StateTransition transition, TransitionIndex<State, Message, StateTransition> transitionIndex, Operation operation) {

        // Update transition components if found in the index
        transition = mergeTransition(transition, transitionIndex);

        // Validate transition components
        ValidationResult result = validateTransition(transition);

        // Verify the operation
        if (result.isValid()) {
            result = verifyOperation(transition, transitionIndex, operation);
        }

        // Throws the exception on failure depending on the policy
        if ((!result.isValid()) && (getFailurePolicy() == FailurePolicy.THROW_ON_FAILURE)) {
            throw (result.getException() != null) ? result.getException() : new ValidationException(result.getDescription());
        }

        return result;
    }

    /**
     * Updates the given transition with the components found in the index.
     *
     * @param transition the transition
     * @param transitionIndex the index
     * @return the merged state transition
     */
    private StateTransition mergeTransition(StateTransition transition, TransitionIndex<State, Message, StateTransition> transitionIndex) {
        StateTransition t = null;
        if (transition != null) {
            State origin = transitionIndex.find(Optional.ofNullable(transition.getOrigin()).map(AbstractNode::getName).orElse(null)).orElse(transition.getOrigin());
            State target = transitionIndex.find(Optional.ofNullable(transition.getTarget()).map(AbstractNode::getName).orElse(null)).orElse(transition.getTarget());
            t = new StateTransition(origin, transition.getMessage(), target);
        }

        return Optional.ofNullable(t).orElse(transition);
    }

    /**
     * Check that the operation can be executed for the given transition in the given index.
     *
     * @param transition the transition
     * @param transitionIndex the transition index
     * @param operation the operation.
     *
     * @return the result of the operation check
     */
    private ValidationResult verifyOperation(StateTransition transition, TransitionIndex<State, Message, StateTransition> transitionIndex, Operation operation) {

        ValidationResult result = ValidationResult.success();

        // When removing a transition, check that it is available
        if (Operation.REMOVE.equals(operation)) {
            boolean found = false;
            Map<State, Map<Message, State>> transitionsMap = transitionIndex.getTransitionsAsMap();
            Map<Message, State> transitionsFound = transitionsMap.get(transition.getOrigin());
            if (transitionsFound != null) {
                State target = transitionsFound.get(transition.getMessage());
                found = transition.getTarget().equals(target);
            }

            if (!found) {
                result.setException(new IllegalTransitionException("Cannot find transition [" + transition + "]"));
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
                if (Boolean.TRUE.equals(transition.getOrigin().isFinal())) {
                    builder.exception(new TransitionInitializationException("Cannot create transition from final state ["+transition.getOrigin().getName()+"]"));
                } else {
                    builder.valid(true);
                }
            }
        }
        return builder.build();
    }

}
