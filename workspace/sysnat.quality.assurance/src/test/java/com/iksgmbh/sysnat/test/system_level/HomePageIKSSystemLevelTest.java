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
		assertTrue("Unexpected number of successful test cases found in report.", 
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
