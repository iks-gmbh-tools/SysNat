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
package com.iksgmbh.sysnat.test.module_level;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.GenerationRuntimeInfo;
import com.iksgmbh.sysnat.SysNatTestingExecutor;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.helper.ReportCreator;
import com.iksgmbh.sysnat.test.utils.SysNatTestUtils;

/**
 * Tests for the interaction of sysnat.testcase.generation and sysnat.test.execution.
 * The interaction takes place via files generated by sysnat.testcase.generation 
 * and written into sysnat.test.execution.
 * 
 * @author Reik Oberrath
 */
public class CalculatorApp_ModuleLevelTest 
{
	private static final String EXECUTION_MAIN_DIR = "../sysnat.test.execution/src/main/java/";
	private static final String EXECUTION_TEST_DIR = "../sysnat.test.execution/src/test/gen/";
	private static final String TESTDATA_DIR = "../sysnat.quality.assurance/src/test/resources/testdata/";
	private static final String testAppName = "CalculatorTestApp";

	@Before
	public void setup() 
	{
		SysNatFileUtil.deleteFolder(EXECUTION_TEST_DIR);
		GenerationRuntimeInfo.reset();
		System.setProperty("sysnat.dummy.test.run", "true");
		System.setProperty("sysnat.autolaunch.report", "false");

		String path = SysNatFileUtil.findAbsoluteFilePath("../sysnat.quality.assurance/target");
		Arrays.asList( new File( path ).listFiles() ).stream()
		      .filter(f->f.getName().startsWith("BDDTestReport") && f.isDirectory())
		      .forEach(SysNatFileUtil::deleteFolder);

		System.setProperty("sysnat.properties.path", TESTDATA_DIR + testAppName);
		System.setProperty("settings.config", TESTDATA_DIR + testAppName + "/settings.config");

		SysNatFileUtil.copyTextFileToTargetDir(TESTDATA_DIR + testAppName,
				"Calculator.java", 
				EXECUTION_MAIN_DIR + "com/iksgmbh/sysnat/test");
		SysNatFileUtil.copyTextFileToTargetDir(TESTDATA_DIR + testAppName, 
				"CalculatorLanguageTemplatesContainer.java", 
				EXECUTION_MAIN_DIR + "com/iksgmbh/sysnat/test/language_container");
	}
	
	@After
	public void cleanup() {
		SysNatFileUtil.deleteFolder(EXECUTION_MAIN_DIR);
		SysNatFileUtil.deleteFolder(EXECUTION_TEST_DIR);
	}

	@Test
	public void compilesAndExecutesXX_with_BDDKeyword() throws Exception
	{
		// arrange
		SysNatFileUtil.copyTextFileToTargetDir(TESTDATA_DIR + testAppName,
				                               "BDDKeywordTestCaseTest.java",
				                               EXECUTION_TEST_DIR + "com/iksgmbh/sysnat/test/calculatortestapp");

		GenerationRuntimeInfo.getInstance();

		// act
		final String result = SysNatTestingExecutor.startMavenCleanCompileTest();

		// assert
		assertEquals("Maven result", SysNatTestingExecutor.MAVEN_OK, result);
		String targetDir = SysNatFileUtil.findAbsoluteFilePath("../sysnat.quality.assurance/target");
		File reportFolder = Arrays.asList( new File( targetDir ).listFiles() ).stream().filter(f->f.getName().startsWith("BDDTestReport") && f.isDirectory()).findFirst().get();
		final File fullReportFile = new File(reportFolder, ReportCreator.FULL_REPORT_RESULT_FILENAME);
		SysNatTestUtils.assertFileExists( fullReportFile );
		final String report = SysNatFileUtil.readTextFileToString(fullReportFile); 
		SysNatTestUtils.assertReportContains(report, "1. Feature: BDDKeywordTestCase");
		SysNatTestUtils.assertReportContains(report, "1.1 Scenario: XX1");
		SysNatTestUtils.assertReportContains(report, "1.1.1 <b>Given</b> Number <b>1</b> has been entered.");
		SysNatTestUtils.assertReportContains(report, "1.1.2 <b>Given</b> Number <b>2</b> has been entered.");
		SysNatTestUtils.assertReportContains(report, "1.1.3 <b>When</b> Entered numbers has been summed up.");
		SysNatTestUtils.assertReportContains(report, "1.1.4 <b>Then</b> The result equals <b>3</b>.");
	}

	@Test
	public void compilesAndExecutesXX_withBehaviourLevelInstructions_andCreatesTestReport() throws Exception
	{
		// arrange
		SysNatFileUtil.copyTextFileToTargetDir(TESTDATA_DIR + testAppName,
				                  "BehaviourLevelInstructionsTestCase1Test.java",
				                       EXECUTION_TEST_DIR + "com/iksgmbh/sysnat/test/calculatortestapp");

		SysNatFileUtil.copyTextFileToTargetDir(TESTDATA_DIR + testAppName,
								  "BehaviourLevelInstructionsTestCase2Test.java",
				                       EXECUTION_TEST_DIR + "com/iksgmbh/sysnat/test/calculatortestapp");

		GenerationRuntimeInfo.getInstance();
		
		// act
		final String result = SysNatTestingExecutor.startMavenCleanCompileTest();

		// assert
		assertEquals("Maven result", SysNatTestingExecutor.MAVEN_OK, result);
		String targetDir = SysNatFileUtil.findAbsoluteFilePath("../sysnat.quality.assurance/target");
		File reportFolder = Arrays.asList( new File( targetDir ).listFiles() ).stream().filter(f->f.getName().startsWith("BDDTestReport") && f.isDirectory()).findFirst().get();
		final File fullReportFile = new File(reportFolder, ReportCreator.FULL_REPORT_RESULT_FILENAME);
		SysNatTestUtils.assertFileExists( fullReportFile );
		final String report = SysNatFileUtil.readTextFileToString(fullReportFile); 
		//System.out.println(report);

		SysNatTestUtils.assertReportContains(report, "1. Behaviour: BehaviourId");
		SysNatTestUtils.assertReportContains(report, "This is a OneTimePrecondition instruction.");
		SysNatTestUtils.assertReportContains(report, "1.1 XXID: XX1");
		SysNatTestUtils.assertReportContains(report, "1.1.1 Number <b>11</b> has been entered.");
		SysNatTestUtils.assertReportContains(report, "1.1.2 Number <b>22</b> has been entered.");
		SysNatTestUtils.assertReportContains(report, "1.1.3 Number <b>33</b> has been entered.");
		SysNatTestUtils.assertReportContains(report, "1.1.4 Entered numbers has been summed up.");
		SysNatTestUtils.assertReportContains(report, "1.1.5 The result does not equals <b>99</b>.");
		SysNatTestUtils.assertReportContains(report, "This is a OneTimeCleanup instruction.");
		 
	}
	
}