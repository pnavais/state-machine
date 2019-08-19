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
 * An exception raised on file export operations
 */
public class FileExportException extends RuntimeException {

    /**
     * Creates the exception using the given message
     * description and wrapping the actual exception
     *
     * @param message the message description
     * @param cause the actual exception
     */
    public FileExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
