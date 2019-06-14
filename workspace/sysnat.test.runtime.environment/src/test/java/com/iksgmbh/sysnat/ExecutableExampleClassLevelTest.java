/*
 * Copyright 2018 IKS Gesellschaft fuer Informations- und Kommunikationssysteme mbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iksgmbh.sysnat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.iksgmbh.sysnat._testcases.TestCaseOK;

public class ExecutableExampleClassLevelTest 
{
	private List<ExecutableExample> executableExamples = new ArrayList<ExecutableExample>();
	
	@Test
	public void throwsErrorForNonUniqueXXIds() 
	{
		// arrange
		ExecutionRuntimeInfo executionRuntimeInfo = ExecutionRuntimeInfo.getInstance();
		List<String> filter = new ArrayList<>();
		filter.add("-");
		executionRuntimeInfo.setExecutionFilterList(filter);
		TestCaseOK testCase = new TestCaseOK();
		testCase.setThrowExceptionOnFailing(true);
		executableExamples.add(testCase);
		executableExamples.add(testCase);

		try {
			for (ExecutableExample executableExample : executableExamples) {
				// act
				executableExample.executeTestCase();
			}
			fail("Expected exception not thrown!");
		} catch (Exception e) {
			// assert
			assertEquals("Error Message", "Fehler: Uneindeutige XXId: Green Test", e.getMessage() );	
		}		
	}

}
