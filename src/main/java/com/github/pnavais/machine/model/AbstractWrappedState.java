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

import lombok.Getter;
import lombok.NonNull;

/**
 * This class is intended to contain the actual State
 * instance in order to be stored in a state machine.
 * The wrapped instance can be modified at any time
 * allowing references in the state machine to still
 * point to the wrapper.
 */
public abstract class AbstractWrappedState extends State {

    /** The target State. */
    @Getter
    protected State state;

    /**
     * Constructor with the state to wrap
     *
     * @param state the state to wrap
     */
    public AbstractWrappedState(@NonNull State state) {
        super(state.getName());
        this.state = state;
    }

    /**
     * Retrieves the name of the state
     * @return the state's name
     */
    @Override
    public String getName() {
        return this.state.getName();
    }

    /**
     * Sets whether the state is
     * final or not.
     *
     * @param finalState the final state flag
     */
    @Override
    public void setFinal(boolean finalState) {
        this.state.setFinal(finalState);
    }

    /**
     * Retrieves the final state condition
     * flag. If final, no further transitions
     * can be allowed from this state.
     *
     * @return the final state flag
     */
    @Override
    public boolean isFinal() {
        return this.state.isFinal();
    }

    @Override
    public boolean equals(Object o) {
        return state.equals(o);
    }

    @Override
    public int hashCode() {
        return state.hashCode();
    }

}
