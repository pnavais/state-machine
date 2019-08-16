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

import com.github.pnavais.machine.api.AbstractNode;
import com.github.pnavais.machine.api.Mergeable;

/**
 * An state represents an arbitrary node in a state machine
 * containing its transitions.
 */
public abstract class AbstractState extends AbstractNode implements Mergeable<AbstractState> {

    /** Flag to control whether the state is final or not */
    private boolean finalState;

    /**
     * Constructor with node name
     *
     * @param name the name of the node
     */
    public AbstractState(String name) {
        super(name);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Sets whether the state is
     * final or not.
     * @param finalState the final state flag
     */
    public void setFinal(boolean finalState) {
        this.finalState = finalState;
    }

    /**
     * Retrieves the final state condition
     * flag. If final, no further transitions
     * can be allowed from this state.
     * @return the final state flag
     */
    public boolean isFinal() {
        return this.finalState;
    }

    /**
     * Merges the information of the given state
     * into the current instance.
     *
     * @param state the state to merge
     * @return the merged state instance
     */
    @Override
    public AbstractState merge(AbstractState state) {
        if (state != null) {
            finalState = (state.isFinal() || finalState);
        }
        return this;
    }
}
