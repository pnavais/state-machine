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

package com.github.pnavais.machine.api.message;

import com.github.pnavais.machine.api.Node;

/**
 * This interface allows wrapping a message with the source/target information
 * in order to be transmitted to interested parties (e.g. transition checker)
 */
public interface Envelope<N extends Node, M extends Message> {

    /**
     * Retrieves the message
     *
     * @return the message
     */
    M getMessage();

    /**
     * Retrieves the source originator
     * of the message.
     *
     * @return the source originator
     */
    N getOrigin();

    /**
     * Retrieves the target of the message.
     *
     * @return the target
     */
    N getTarget();

}
