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
import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.Test;

import com.iksgmbh.sysnat.SysNatJUnitTestClassGenerator;
import com.iksgmbh.sysnat.SysNatExecutor;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.domain.TestApplication;
import com.iksgmbh.sysnat.test.system_level.common.SysNatSystemTest;
import com.iksgmbh.sysnat.test.utils.SysNatTestUtils;

public class HomePageIKSSystemLevelTest extends SysNatSystemTest 
{	
	private static final ResourceBundle ERR_MSG_BUNDLE = ResourceBundle.getBundle("bundles/ErrorMessages", Locale.getDefault());

	@Test
	public void triesToExecuteXX_forHomePageIKS() throws Exception
	{
		// arrange
		settingsConfigToUseInSystemTest = "../sysnat.quality.assurance/src/test/resources/testSettingConfigs/HomePageIKS.config";
		super.setup();
		System.setProperty("sysnat.dummy.test.run", "true");

		// act
		SysNatJUnitTestClassGenerator.doYourJob();
		final String result = SysNatExecutor.startMavenCleanCompileTest();
		Thread.sleep(2000); // give maven time to execute tests
		
		// assert
		if (result.equals(SysNatExecutor.MAVEN_OK)) 
		{
			final File reportFile = getFullOverviewOfCurrentReport();
			final String report = SysNatFileUtil.readTextFileToString(reportFile); 			
			int expectedNumberOfInactiveTests = 1;
			boolean ok = report.contains( getHtmlReportSnippet(expectedNumberOfInactiveTests,
	                                                          SysNatConstants.YELLOW_HTML_COLOR));
			if ( ! ok ) System.err.println(report);
			assertTrue("Unexpected number of inactive test cases found in report.", ok );
			checkReportSuccess(report);
		}
		else
		{
			String filename = "../sysnat.natural.language.executable.examples/reports/"
					          + "HomePageIKS/GenerationError.html";
			String report = SysNatFileUtil.readTextFileToString(filename);
			TestApplication testApplication = new TestApplication("HomePageIKS");
			String expected = ERR_MSG_BUNDLE.getString("AppNotAvailable").replace("XY", testApplication.getStartParameterValue());
			assertTrue("Unexpected error report message.", report.contains(expected));
		}
	}

	private void checkReportSuccess(String report) throws Exception
	{
		final String expectedMessage = "Die Anwendung <b>HomePageIKS</b> steht derzeit nicht zur Verfügung!";
		final int actualOccurrences = SysNatStringUtil.countNumberOfOccurrences(report, expectedMessage); 
		final int expectedOccurrences = 0; 
		if (expectedOccurrences != actualOccurrences) {
			System.err.println(report);
		}
		assertEquals("Number of error message occurrences.", 
				      expectedOccurrences, actualOccurrences);
		SysNatTestUtils.assertReportContains(report, "background:#CDE301;" + System.getProperty("line.separator") +
				"padding:0cm;mso-padding-alt:0cm 0cm 1.0pt 0cm'><span style='font-size:14.0pt;" + System.getProperty("line.separator") +
				"line-height:106%'>InactiveTestExample<");
		
		SysNatTestUtils.assertReportContains(report, "MainMenuItems (9/9)");
	}

}