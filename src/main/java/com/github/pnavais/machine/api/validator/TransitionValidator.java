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

package com.github.pnavais.machine.api.validator;

import com.github.pnavais.machine.api.Message;
import com.github.pnavais.machine.api.Node;
import com.github.pnavais.machine.api.Transition;
import com.github.pnavais.machine.api.transition.TransitionIndex;

/**
 *  The transition validator verifies operations
 *  involved in transition management with respect to a given
 *  index.
 *
 * @param <N> the type of node
 * @param <M> the type of message
 * @param <T> the type of transition
 */
public interface TransitionValidator<N extends Node, M extends Message, T extends Transition<N, M>> {

    /** The possible transition operations */
    enum Operation { ADD, REMOVE }

    /** The failure policy */
    enum FailurePolicy { THROW_ON_FAILURE, PROCEED, IGNORE }

    /**
     * Checks the transition parameter correctness
     * with respect to current transitions on the given
     * index.
     *
     * @param transition    the transition to check
     * @param transitionIndex the transition index
     * @param operation the operation to accomplish
     */
    ValidationResult validate(T transition, TransitionIndex<N,M,T> transitionIndex, Operation operation);

    /**
     * Retrieves the failure policy.
     * By default, throw an exception when
     * validation failed.
     * @return the failure policy
     */
    default FailurePolicy getFailurePolicy() {
        return FailurePolicy.THROW_ON_FAILURE;
    }
}
