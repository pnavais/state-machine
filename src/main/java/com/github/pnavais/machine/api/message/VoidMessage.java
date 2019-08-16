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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Represents an empty/null message
 */
@Getter
@Setter(AccessLevel.PRIVATE)
public class VoidMessage implements Message {

    /**
     * The message identifier
     */
    private final UUID messageId;

    /**
     * The payload
     */
    private final Payload payload;

    /**
     * The name
     */
    private final String name;

    /**
     * Constructor with name and payload
     *
     * @param name    the name
     * @param payload the payload
     */
    private VoidMessage(String name, Payload payload) {
        this.name = name;
        this.messageId = UUID.randomUUID();
        this.payload = payload;
    }

    /**
     * Creates a new void message.
     *
     * @return the void message
     */
    public static VoidMessage createNew() {
        return createWith(null);
    }

    /**
     * Creates a new void message with name.
     *
     * @param name the name
     * @return the void message
     */
    public static VoidMessage createWith(String name) {
        return createWith(name, null);
    }

    /**
     * Creates a new void message with name and payload
     *
     * @param name    the name
     * @param payload the payload
     * @return the void message
     */
    public static VoidMessage createWith(String name, Payload payload) {
        return new VoidMessage(name, payload);
    }

}
