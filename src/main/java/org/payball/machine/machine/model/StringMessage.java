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
package org.payball.machine.machine.model;

import org.payball.machine.machine.api.Message;
import org.payball.machine.machine.api.Payload;

import java.util.Objects;
import java.util.UUID;

/**
 * An implementation of the Message interface
 * with a String payload.
 */
public class StringMessage implements Message {

    /** The message key */
    private String messageKey;

    /** The message identifier */
    private final Payload payload;

    /** The message identifier */
    private final UUID messageId;

    /**
     * Default constructor with a message identifier
     *
     * @param messageKey the message identifier
     */
    public StringMessage(String messageKey) {
        Objects.requireNonNull(messageKey);
        this.messageKey = messageKey;
        this.messageId = UUID.randomUUID();
        this.payload = () -> this.messageKey;
    }

    /**
     * Retrieves the message identifier
     *
     * @return the message identifier
     */
    public UUID getMessageId() {
        return messageId;
    }

    @Override
    public Payload getPayload() {
        return payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringMessage that = (StringMessage) o;
        return Objects.equals(messageKey, that.messageKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageKey);
    }
}
