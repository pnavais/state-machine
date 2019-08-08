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
package com.github.pnavais.machine.api;

import com.github.pnavais.machine.api.exception.IllegalTransitionException;
import com.github.pnavais.machine.api.exception.NullTransitionException;

import java.util.Map;

/**
 * A generic contract allowing to identify
 * the target destination after processing
 * the message.
 *
 * @param <N> the type of nodes of the transition
 */
public interface Transition<N extends Node, M extends Message> {

    /**
     * Retrieves the message of the transition
     *
     * @return the message of the transition
     */
    M getMessage();

    /**
     * The source object receiving the
     * message.
     *
     * @return the source
     */
    N getOrigin();

    /**
     * The destination target after processing
     * the message.
     *
     * @return the target destination
     */
    N getTarget();

    /**
     * Checks the transition parameter correctness
     * or throw an {@link IllegalTransitionException} otherwise
     *
     * @param transition the transition to check
     */
    static <N extends Node, M extends Message> void validate(Transition<N, M> transition) {
        if (transition == null) {
            throw new NullTransitionException("The transition cannot be null");
        } else if (transition.getOrigin() == null) {
            throw new IllegalTransitionException("The transition source cannot be null");
        } else if (transition.getTarget() == null) {
            throw new IllegalTransitionException("The transition target cannot be null");
        } else if (transition.getMessage() == null) {
            throw new IllegalTransitionException("The transition message cannot be null");
        }
    }

    /**
     * Checks the transition parameter correctness
     * or throw an {@link IllegalTransitionException} otherwise
     *
     * @param transition the transition to check
     * @param transitionMap the transition map
     */
    static <N extends Node, M extends Message> void validate(Transition<N, M> transition, Map<N, Map<M, N>> transitionMap) {

        validate(transition);

        // Check that transition exists
        boolean found = false;
        Map<M, N> transitionsFound = transitionMap.get(transition.getOrigin());
        if (transitionsFound != null) {
            N target = transitionsFound.get(transition.getMessage());
            found = transition.getTarget().equals(target);
        }

        if (!found) {
            throw new IllegalTransitionException("Cannot find transition [" + transition.getOrigin() + " -> " + transition.getTarget() + "]");
        }
    }
}
