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

/**
 * The default implementation of the state
 */
public class State extends AbstractState {

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
         * Retrieves the built instance
         *
         * @return the instance
         */
        public State build() {
            return this.instance;
        }
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
