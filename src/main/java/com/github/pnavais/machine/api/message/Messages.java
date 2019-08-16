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

/**
 * Provides default generic Message types
 */
public final class Messages {

    /** The identifier of the ANY void message */
    public static final String ANY_MESSAGE_ID = "ANY";

    /** The payload string of the ANY void message */
    public static final String ANY_MESSAGE_PAYLOAD = "*";

    /** The identifier of the EMPTY void message */
    public static final String EMPTY_MESSAGE_ID = "EMPTY";

    /** The payload string of the EMPTY void message */
    public static final String EMPTY_MESSAGE_PAYLOAD = "_";

    /**
     * A void message used as marker representing an empty message
     */
    public static final Message NULL = VoidMessage.createNew();

    /**
     * A void message used as marker representing an empty message
     */
    public static final Message EMPTY = VoidMessage.createWith(EMPTY_MESSAGE_ID , () -> EMPTY_MESSAGE_PAYLOAD);

    /**
     * A void message used as marker representing any kind of message
     */
    public static final Message ANY = VoidMessage.createWith(ANY_MESSAGE_ID, () -> ANY_MESSAGE_PAYLOAD);

    /**
     * Private constructor to avoid external instantiation
     */
    private Messages() {}

}
