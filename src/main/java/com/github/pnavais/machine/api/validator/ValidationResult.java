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

package com.github.pnavais.machine.api.validator;

import lombok.*;

/**
 * This class holds the results of a validation
 * with an optional detailed description message and
 * companion exception class.
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
public class ValidationResult {

    /**
     * The Exception.
     */
    private RuntimeException exception;

    /**
     * The Description.
     */
    private String description;

    /**
     * The Result.
     */
    private boolean valid;

    /**
     * Creates an invalid result from a given exception
     */
    public static ValidationResult from(@NonNull RuntimeException e) {
        return ValidationResult.builder().valid(false).description(e.getMessage()).exception(e).build();
    }

    /**
     * Creates a successful validation result
     *
     * @return successful validation result
     */
    public static ValidationResult success() {
        return ValidationResult.builder().valid(true).build();
    }

    /**
     * Creates an unsuccessful validation result
     *
     * @return unsuccessful validation result
     */
    public static ValidationResult fail(String message) {
        return ValidationResult.builder().valid(false).description(message).build();
    }

}
