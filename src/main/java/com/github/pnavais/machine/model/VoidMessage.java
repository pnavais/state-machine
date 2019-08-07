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

package com.github.pnavais.machine.model;

import com.github.pnavais.machine.api.Payload;
import com.github.pnavais.machine.api.Message;

import java.util.UUID;

public class VoidMessage implements Message {

    /**
     * Default constructor avoids instantiation
     */
    private VoidMessage() { }

    /**
     * Retrieves the default lazy-loaded singleton instance
     */
    public static VoidMessage getDefault() {
        return VoidMessageHolder.instance;
    }

    /**
     * Singleton holder
     */
    private static class VoidMessageHolder {
        private static VoidMessage instance = new VoidMessage();
    }

    @Override
    public UUID getMessageId() {
        return null;
    }

    @Override
    public Payload getPayload() {
        return null;
    }



}
