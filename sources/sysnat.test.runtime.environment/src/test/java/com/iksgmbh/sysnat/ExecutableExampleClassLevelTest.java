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
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import com.iksgmbh.sysnat._testcases.TestCaseOK;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;

public class ExecutableExampleClassLevelTest 
{
	private ExecutableExample cut = new TestCaseOK();
	
	@Test
	public void importsEmbeddedDatasets() 
	{
		// arrange
		String testData =   "|Name|Dean|Lisa|Susy|Tom|" + SysNatConstants.LINE_SEPARATOR
		                  + "|Age | 8  | 7  |9   |8  |" + SysNatConstants.LINE_SEPARATOR 
		                  + "|Size|120|130  |130 |120|";
		
		// act
		Hashtable<String, Properties> result = cut.importTestData(testData);
		
		// assert
		assertEquals("Number of datasets", 2, result.size());
	}
	
	@Test
	public void throwsErrorForNonUniqueXXIds() 
	{
		// arrange
		List<ExecutableExample> executableExamples = new ArrayList<ExecutableExample>();
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
