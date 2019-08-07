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

package com.github.pnavais.machine.api;

import com.github.pnavais.machine.model.VoidMessage;
import lombok.Getter;

/**
 * Provides default generic Message types
 */
@Getter
public enum MessageConstants {

    /**
     * A void message used as marker representing an empty message
     */
    EMPTY(VoidMessage.getDefault());

    /** The message */
    private Message message;

    /**
     * Default constructor with message
     * @param message the message
     */
    MessageConstants(Message message) {
        this.message = message;
    }
}
