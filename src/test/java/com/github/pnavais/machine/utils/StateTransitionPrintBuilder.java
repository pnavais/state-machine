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
package com.github.pnavais.machine.utils;

/**
 * A simple builder to create State Transition
 * print options.
 */
public class StateTransitionPrintBuilder {

    /**
     * Private constructor avoiding instantiation
     */
    private StateTransitionPrintBuilder() { }

    /**
     * Creates a new builder with default
     * print options.
     *
     * @return the new builder
     */
    public static StateTransitionPrint.StateTransitionPrintBuilder newBuilder() {
        return DefaultPrintOptionsHolder.instance.toBuilder();
    }

    /**
     * Retrieves the default Print option instance.
     * Contains simple state/message formatter and outputs to stdout.
     *
     * @return the default Print option instance
     */
    public static StateTransitionPrint getDefault() {
        return DefaultPrintOptionsHolder.instance;
    }

    /**
     * Initialization-on-demand holder to return the singleton
     * default Print option instance in a lazy fashion
     */
    private static class DefaultPrintOptionsHolder {
        private static StateTransitionPrint instance = new StateTransitionPrint();
    }

}
