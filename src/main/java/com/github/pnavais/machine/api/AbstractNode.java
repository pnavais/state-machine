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
import lombok.ToString;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents an arbitrary node of a given directed graph
 */
@ToString
public abstract class AbstractNode implements Node {

    /** The shorthand name associated to the node */
    protected String name;

    /** The identifier of the node */
    protected final UUID id;

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
     * Retrieves the name of the node
     *
     * @return the name of the node
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractNode that = (AbstractNode) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
