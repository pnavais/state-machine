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

import com.github.pnavais.machine.api.exception.ValidationException;
import lombok.*;

/**
 * This class holds the results of a validation
 * with an optional detailed description message and
 * companion exception class.
 */
@Getter
@Setter
@RequiredArgsConstructor(staticName = "of")
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
     * Throws the exception (if any) on
     * invalid validation result.
     */
    public void throwOnFailure() {
        if (!valid) {
            throw (exception != null) ? exception : new ValidationException(description);
        }
    }
}
