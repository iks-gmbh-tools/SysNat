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
package com.iksgmbh.sysnat.test.system_level;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import com.iksgmbh.sysnat.SysNatTestCaseGenerator;
import com.iksgmbh.sysnat.SysNatTestingExecutor;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.test.system_level.common.SysNatSystemTest;

public class HomePageIKSSystemLevelTest extends SysNatSystemTest 
{	
	@Test
	public void triesToReachHomePageIKS() throws Exception
	{
		// arrange
		settingsConfigToUseInSystemTest = "../sysnat.quality.assurance/src/test/resources/testSettingConfigs/HomePageIKS.config";
		super.setup();

		// act
		SysNatTestCaseGenerator.doYourJob();
		final String result = SysNatTestingExecutor.startMavenCleanCompileTest();
		Thread.sleep(2000); // give maven time to execute tests
		
		// assert
		assertEquals("Maven execution status", SysNatTestingExecutor.MAVEN_OK, result);

		final File reportFile = getFullOverviewOfCurrentReport();
		final String report = SysNatFileUtil.readTextFileToString(reportFile); 
		final String errorMessageIfNotAvailable = "Die Anwendung <b>HomePageIKS</b> steht derzeit nicht zur Verfügung!";
		final int numberErrorMessagesOccurrences = SysNatStringUtil.countNumberOfOccurrences(report, errorMessageIfNotAvailable); 
		
		
		int expectedNumberOfInactiveTests = 1;
		assertTrue("Unexpected number of inactive test cases found in report.", 
				   report.contains( getHtmlReportSnippet(expectedNumberOfInactiveTests,
						                                 SysNatConstants.YELLOW_HTML_COLOR)));
	

		if (numberErrorMessagesOccurrences == 0) {
			// Internet available and IKS Hompage online
			runsSystemTest_ForHompageIKS_Successfully(report);
		} else {
			// either no Internet available or IKS Hompage offline
			writesCorrectErrorMessageInReportForUnreachableURL(numberErrorMessagesOccurrences,
					                                           report);
		}
	}

	private void writesCorrectErrorMessageInReportForUnreachableURL(int numberErrorMessagesOccurrences, 
			                                                        String report) 
	{
		final int numberOfTestCases = 2; 
		if (numberOfTestCases != numberErrorMessagesOccurrences) {
			System.err.println(report);
		}
		assertEquals("Number of error message occurrences.", 
				      numberOfTestCases, numberErrorMessagesOccurrences);
	}

	private void runsSystemTest_ForHompageIKS_Successfully(String report) throws Exception
	{
		final String expectedMessage = "Die Anwendung <b>HomePageIKS</b> steht derzeit nicht zur Verfügung!";
		final int actualOccurrences = SysNatStringUtil.countNumberOfOccurrences(report, expectedMessage); 
		final int expectedOccurrences = 0; 
		if (expectedOccurrences != actualOccurrences) {
			System.err.println(report);
		}
		assertEquals("Number of error message occurrences.", 
				      expectedOccurrences, actualOccurrences);
	}

}