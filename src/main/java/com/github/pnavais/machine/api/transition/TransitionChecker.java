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

package com.github.pnavais.machine.api.transition;

import com.github.pnavais.machine.api.Node;
import com.github.pnavais.machine.api.Status;
import com.github.pnavais.machine.api.message.Envelope;
import com.github.pnavais.machine.api.message.Message;

/**
 * The transition checker acts a validator that is used
 * by a given State Machine to allow/deny a given transition.
 */
public interface TransitionChecker<N extends Node, M extends Message> {

    /**
     * Validates the departure from the current state upon reception
     * of a given message using the specified transition index to ensure
     * transitions are accepted.
     *
     * @param envelope the envelope with the received message
     * @return the status of the operation
     */
    Status validateDeparture(Envelope<N,M> envelope);

    /**
     * Validates the arrival to a given target state upon reception
     * of a given message using the specified transition index to ensure
     * transitions are accepted.
     *
     * @param envelope the envelope with the received message
     * @return the status of the operation
     */
    Status validateArrival(Envelope<N,M> envelope);

}
