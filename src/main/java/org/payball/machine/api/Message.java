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
package org.payball.machine.api;

/**
 * Represents a message passed to a given state
 * in the state machine.
 *
 * @param <T> the type of the Message
 */
public interface Message<T> {

    /**
     * Retrieves the payload contents
     * of the message.
     *
     * @return the payload contents
     */
    T getPayload();
}
