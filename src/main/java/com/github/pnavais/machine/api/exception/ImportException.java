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

package com.github.pnavais.machine.api.exception;

/**
 * The base class for all import exception types
 */
public abstract class ImportException extends RuntimeException {

    /**
     * Instantiates a new Import exception with a
     * description message.
     *
     * @param message the message
     */
    public ImportException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Import exception with a
     * description message and throwable cause.
     *
     * @param message the message
     * @param cause   the cause
     */
    public ImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
