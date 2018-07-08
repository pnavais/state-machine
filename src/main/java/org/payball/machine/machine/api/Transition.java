/*
 * Copyright 2018 Pablo Navais
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
package org.payball.machine.machine.api;

import org.payball.machine.machine.api.exception.IllegalTransitionException;

/**
 * A generic contract allowing to identify
 * the target destination after processing
 * the message.
 *
 * @param <T> the type of the transition
 */
public interface Transition<T> {

    /**
     * Retrieves the message of the transition
     *
     * @return the message of the transition
     */
    Message getMessage();

    /**
     * The source object receiving the
     * message.
     *
     * @return the source
     */
    T getOrigin();

    /**
     * The destination target after processing
     * the message.
     *
     * @return the target destination
     */
    T getTarget();

    /**
     * Checks the transition parameter correctness
     * or throw an {@link IllegalTransitionException} otherwise
     *
     * @param transition the transition to check
     * @return the input transition
     */
    static Transition validate(Transition<?> transition) {
        if (transition == null) {
            throw new IllegalTransitionException("The transition cannot be null");
        } else  if (transition.getOrigin() == null) {
            throw new IllegalTransitionException("The transition source cannot be null");
        } else  if (transition.getTarget() == null) {
            throw new IllegalTransitionException("The transition target cannot be null");
        } else if (transition.getMessage() == null) {
            throw new IllegalTransitionException("The transition message cannot be null");
        }
        return transition;
    }
}
