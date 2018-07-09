/**
 * Copyright 2018 Payball Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.payball.machine;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.junit.runner.RunWith;
import org.payball.machine.core.StateMachineBuilderTest;
import org.payball.machine.core.StateMachineCreationTest;
import org.payball.machine.core.StateMachineOperationsTest;

/**
 * Suite of JUnit tests for the State Machine.
 *
 * @author pnavais
 */

@RunWith(JUnitPlatform.class)
@SuiteDisplayName("State Machine tests")
@SelectClasses({ StateMachineCreationTest.class, StateMachineBuilderTest.class, StateMachineOperationsTest.class})
public class StateMachineTestSuite {

}
