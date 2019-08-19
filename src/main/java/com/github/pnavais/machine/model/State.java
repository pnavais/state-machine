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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The default implementation of the state.
 * Contains an optional properties map to store generic state properties as
 * attachments.
 */
public class State extends AbstractState {

    /** The optional properties attached to the state */
    @Getter
    private Map<String, String> properties;

    /**
     * Constructor with node name
     *
     * @param name the name of the node
     */
    public State(String name) {
        super(name);
    }

    /**
     * Static factory method to create the State
     *
     * @param name the name of the state
     * @return the state builder
     */
    public static StateBuilder from(String name) {
        return new StateBuilder().named(name);
    }

    /**
     * Adds the property with the given key and value.
     *
     * @param key the name of the property to add.
     * @param value the property value
     * @return self for chaining purposes
     */
    public State addProperty(@NonNull String key, @NonNull String value) {
        properties = Optional.ofNullable(properties).orElse(new LinkedHashMap<>());
        properties.put(key, value);
        return this;
    }

    /**
     * Removes the property identified by the given key
     * or discard the operation silently if not found.
     *
     * @param key the name of the property to remove
     * @return self for chaining purposes
     */
    public State removeProperty(@NonNull String key) {
        Optional.ofNullable(properties).ifPresent(props -> props.remove(key));
        return this;
    }

    /**
     * Retrieves the property identified by the given key
     * or null if not found
     *
     * @param key the name of the property to remove
     * @return the optional property
     */
    public Optional<String> getProperty(@NonNull String key) {
        return Optional.ofNullable(properties).map(m -> m.get(key));
    }

    /**
     * Checks whether the state contains the given property.
     *
     * @param key the key of the property
     * @return true if the state has the property, false otherwise
     */
    public boolean hasProperty(@NonNull String key) {
        return Optional.ofNullable(properties).map(props -> props.containsKey(key)).orElse(false);
    }

    /**
     * Checks whether the state has properties or not
     *
     * @return true if state has properties, false otherwise
     */
    public boolean hasProperties() {
        return (properties != null) && (!properties.isEmpty());
    }

    /**
     * Merges the information of the given state
     * into the current instance.
     * All incoming properties are added to the current
     * ones potentially overriding existing values.
     *
     * @param state the state to merge
     * @return the merged state instance
     */
    @Override
    public AbstractState merge(AbstractState state) {
        super.merge(state);
        if ((state != null) && ((state instanceof State) && ((State)state).hasProperties())) {
            ((State)state).getProperties().forEach(this::addProperty);
        }
        return this;
    }

    /**
     * A basic State builder
     */
    public static class StateBuilder {

        /**
         * The State instance to build
         */
        private State instance;

        /**
         * Named state builder.
         *
         * @param name the name
         * @return the state builder
         */
        public StateBuilder named(String name) {
            this.instance = new State(name);
            return this;
        }

        /**
         * Sets the final state
         *
         * @param finalState the final state
         * @return the state builder
         */
        public StateBuilder isFinal(boolean finalState) {
            this.instance.setFinal(finalState);
            return this;
        }

        /**
         * Adds a new property to the state.
         *
         * @param key the name of the property
         * @param value the value of the property
         * @return the state builder
         */
        public StateBuilder property(@NonNull String key, @NonNull String value) {
            this.instance.addProperty(key, value);
            return this;
        }

        /**
         * Retrieves the built instance
         *
         * @return the instance
         */
        public State build() {
            return this.instance;
        }
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
