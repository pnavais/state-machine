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
 * Exception raised during I/O Processing of a given
 * State Machine representation.Actually the exception is intended
 * to wrap an actual underlying exception (e.g {@link java.io.IOException}
 */
public class FileImportException extends ImportException {

    /**
     * Constructor with a given message and throwable cause.
     *
     * @param message the message
     * @param cause the actual exception
     */
    public FileImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
