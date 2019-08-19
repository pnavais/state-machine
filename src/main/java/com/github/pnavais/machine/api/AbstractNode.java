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
package com.github.pnavais.machine.api;

import lombok.NonNull;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents an arbitrary node of a given directed graph
 */
public abstract class AbstractNode implements Node, Mergeable<AbstractNode>  {

    /** The shorthand name associated to the node */
    protected String name;

    /** The identifier of the node */
    protected final UUID id;

    /** Flag to control whether the node is final or not */
    private boolean finalState;

    /**
     * Constructor with node name
     *
     * @param name the name of the node
     */
    public AbstractNode(@NonNull String name) {
        this.name = name;
        this.id = UUID.randomUUID();
    }

    /**
     * Retrieves the identifier of the node
     *
     * @return the identifier of the node
     */
    public UUID getId() {
        return id;
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
     * Retrieves the name of the node
     *
     * @return the name of the node
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Merges the information of the given node
     * into the current instance.
     *
     * @param node the node to merge
     * @return the merged node instance
     */
    @Override
    public AbstractNode merge(AbstractNode node) {
        if (node != null) {
            finalState = (node.isFinal() || finalState);
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractNode)) return false;
        AbstractNode that = (AbstractNode) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
