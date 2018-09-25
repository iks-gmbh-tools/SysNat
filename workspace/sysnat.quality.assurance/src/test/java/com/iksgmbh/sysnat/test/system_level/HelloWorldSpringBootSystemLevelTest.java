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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.iksgmbh.sysnat.SysNatTestCaseGenerator;
import com.iksgmbh.sysnat.SysNatTestingExecutor;
import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.test.system_level.common.SysNatSystemTest;

public class HelloWorldSpringBootSystemLevelTest extends SysNatSystemTest
{
	private static Integer pid = null;

	@BeforeClass
	public static void startTestApplication() 
	{
		String pidAsString = startHelloWorldSpringBoot();
		try {
			pid = Integer.valueOf( pidAsString );
		} catch (Exception e) {
			System.err.println("HelloWorldSpringBoot cannot start because it is already running.");
		}
		sleep(5000);
		System.out.println("#######################################################");
		System.out.println("HelloWorldSpringBoot has been startet with PID " + pid);
		System.out.println("#######################################################");
	}

	@AfterClass
	public static void tearDown() {
		stopTestApplication();
	}

	private static void stopTestApplication() 
	{
		try {
			final Process process = Runtime.getRuntime().exec("cmd /C taskkill /PID " + pid + " /F");
			final InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
			final BufferedReader reader = new BufferedReader(inputStreamReader);
			String line = null;
			while ( (line = reader.readLine()) != null ) {
				System.out.println(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String startHelloWorldSpringBoot() 
	{
		try {
			String toReturn = null;
			final Process process = Runtime.getRuntime().exec("cmd /C cd HelloWorldSpringBoot && startHelloWorldSpringBoot.bat");
			final InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
			final BufferedReader reader = new BufferedReader(inputStreamReader);
			String line = null;
			while ( (line = reader.readLine()) != null ) {
				System.out.println(line);
				if (line.contains("INFO")) {
					toReturn = extractPid(line);
				}
				if (line.contains("Started SpringBootThymeleafApplication in")) {
					return toReturn;
				}
			}
			return "";
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}


	private static String extractPid(String line) 
	{
		line = line.replaceAll("/t", "");
		while (line.contains("  ")) {
			line = line.replace("  ", " ");
		}
		String[] splitResult = line.split(" ");
		return splitResult[3];
	}

	@Test
	public void runsAll_HelloWorldSpringBoot_Tests() throws Exception
	{
		// arrange
		settingsConfigToUseInSystemTest = "../sysnat.quality.assurance/src/test/resources/testSettingConfigs/HelloWorldSpringBoot_ALL.config";
		
		// act
		final String report = runSNTsucessfully();
		
		// assert
		int expectedNumberSuccessfullyExecutedTests = 17;
		assertTrue("Unexpected number of successful test cases found in report.", 
				   report.contains( getHtmlReportSnippet(expectedNumberSuccessfullyExecutedTests,
						                                 SysNatConstants.GREEN_HTML_COLOR)));
	}

	@Test
	public void runsOneExecFilter_HelloWorldSpringBoot_Tests() throws Exception
	{
		// arrange
		settingsConfigToUseInSystemTest = "../sysnat.quality.assurance/src/test/resources/testSettingConfigs/HelloWorldSpringBoot_Smoketests.config";
		
		// act
		final String report = runSNTsucessfully();
		
		// assert
		int expectedNumberSuccessfullyExecutedTests = 3;
		assertTrue("Unexpected number of successful test cases found in report.", 
				     report.contains( getHtmlReportSnippet(expectedNumberSuccessfullyExecutedTests,
				    		                               SysNatConstants.GREEN_HTML_COLOR)));
	}

	@Test
	public void runsOnlySingleTestcase_HelloWorldSpringBoot_Tests() throws Exception
	{
		// arrange
		settingsConfigToUseInSystemTest = "../sysnat.quality.assurance/src/test/resources/testSettingConfigs/HelloWorldSpringBoot_SingleTest.config";
		
		// act
		final String report = runSNTsucessfully();
		
		// assert
		int expectedNumberSuccessfullyExecutedTests = 1;
		assertTrue("Unexpected number of successful test cases found in report.", 
				   report.contains( getHtmlReportSnippet(expectedNumberSuccessfullyExecutedTests,
						                                 SysNatConstants.GREEN_HTML_COLOR)));
	}

	private String runSNTsucessfully() throws Exception
	{
		// act
		super.setup();
		SysNatTestCaseGenerator.doYourJob();
		final String result = SysNatTestingExecutor.startMavenCleanCompileTest();

		// assert
		assertEquals("Maven result", SysNatTestingExecutor.MAVEN_OK, result);
		final File reportFile = getFullOverviewOfCurrentReport();
		final String report = SysNatFileUtil.readTextFileToString(reportFile); 
		final boolean testResultOk = ! report.contains(ExecutableExample.SMILEY_FAILED) && 
				                     ! report.contains(ExecutableExample.SMILEY_WRONG); 
		assertTrue("Report contains problem(s).", testResultOk);
		return report;
	}

	
}