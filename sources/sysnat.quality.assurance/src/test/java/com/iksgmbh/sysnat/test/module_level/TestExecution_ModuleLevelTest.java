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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.GenerationRuntimeInfo;
import com.iksgmbh.sysnat.SysNatTestingExecutor;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.helper.ReportCreator;
import com.iksgmbh.sysnat.test.utils.SysNatTestUtils;

/**
 * Tests for the interaction of sysnat.natural.language.executable.examples, 
 * sysnat.test.runtime.environment and sysnat.test.execution.
 * The interaction takes place via maven calls.
 *  
 * @author Reik Oberrath
 */
public class TestExecution_ModuleLevelTest 
{
	private static final String TESTDATA_DIR = "../sysnat.quality.assurance/src/test/resources/testdata/";
	private static final String EXECUTION_DIR = "../sysnat.test.execution/src/test/gen/";

	@Before
	public void setup() {
		SysNatFileUtil.deleteFolder(EXECUTION_DIR);
		GenerationRuntimeInfo.reset();
		System.setProperty("sysnat.dummy.test.run", "true");
		System.setProperty("sysnat.autolaunch.report", "false");
	}
		
	@Test
	public void compilesAndExecutes_FakeTestCase_InTestExecutionDir() throws Exception
	{
		// arrange
		final String testAppName = "FakeTestApp";
		System.setProperty("sysnat.properties.path", TESTDATA_DIR + testAppName);
		System.setProperty("settings.config", TESTDATA_DIR + testAppName + "/settings.config");

		final String testCaseName = "FakeTestCase";
		final List<String> fileList = new ArrayList<>();
		fileList.add(testCaseName + ".java");
		createJavaSourceFileInExecutionDir( testAppName, fileList, "com/iksgmbh/sysnat/test/integration/testcase/");
		
		GenerationRuntimeInfo.getInstance();
		final File resultFile = makeSureThatResultFileDoesNotExist(testCaseName + ".txt");
		final File expectedClassFile = makeSureThatExpectedClassFileDoesNotExist(testCaseName);

		// act
		final String result = SysNatTestingExecutor.startMavenCleanCompileTest();

		// assert
		assertEquals("Maven result", SysNatTestingExecutor.MAVEN_OK, result);
		assertTrue("Expected class file not found.", expectedClassFile.exists());
		assertTrue("Result file of test execution not found.", resultFile.exists());
		assertEquals("Result file content.", "This is the result file of testcase '" + testCaseName + "'.", 
				                             SysNatFileUtil.readTextFileToString(resultFile));
	}
	
	@Test
	public void compilesAndExecutes_MiniTestCaseWithScript_InTestExecutionDir() throws Exception
	{
		// arrange
		final String testAppName = "MiniTestApp";
		System.setProperty("sysnat.properties.path", TESTDATA_DIR + testAppName);
		System.setProperty("settings.config", TESTDATA_DIR + testAppName + "/settings.config");
		System.setProperty("sysnat.dummy.test.run", "true");
		
		final List<String> fileList = new ArrayList<>();
		fileList.add("MiniTestCaseTest.java");
		fileList.add("MiniTestScript.java");
		createJavaSourceFileInExecutionDir( testAppName, fileList, "com/iksgmbh/sysnat/test/integration/testcase/");

		GenerationRuntimeInfo.getInstance();  // init System properties
		SysNatTestUtils.assertReportFolderNotExistsBeginningWith("MiniTestCaseTestReport");
		
		final File resultFile = makeSureThatResultFileDoesNotExist("MiniTestScriptResult.txt");
		final File expectedClassFile = makeSureThatExpectedClassFileDoesNotExist("MiniTestCaseTest");
		SysNatFileUtil.writeFile("../sysnat.test.execution/AvailableNaturalLanguageScripts.properties", 
				                 "MiniTestScript=com.iksgmbh.sysnat.test.integration.testcase.MiniTestScript");
		
		// act
		final String result = SysNatTestingExecutor.startMavenCleanCompileTest();

		// assert
		assertEquals("Maven result", SysNatTestingExecutor.MAVEN_OK, result);
		assertTrue("Expected class file not found.", expectedClassFile.exists());
		assertTrue("Result file of test execution not found.", resultFile.exists());
		assertEquals("Result file content.", "This result file is created by 'MiniTestScript'.", 
				                             SysNatFileUtil.readTextFileToString(resultFile));

		String path = SysNatFileUtil.findAbsoluteFilePath(System.getProperty("sysnat.report.dir"));
		File reportFolder = Arrays.asList( new File( path ).listFiles() ).stream().filter(f->f.getName().startsWith("MiniTestCaseTestReport") && f.isDirectory()).findFirst().get();
		SysNatTestUtils.assertFileExists(reportFolder);
		SysNatTestUtils.assertFileExists( new File(reportFolder, ReportCreator.FULL_REPORT_RESULT_FILENAME) );
		SysNatTestUtils.assertFileExists( new File(reportFolder, ReportCreator.SHORT_REPORT_RESULT_FILENAME) );
		final File detailReportFile = new File(reportFolder, "MiniTestCase/" + ReportCreator.DETAIL_RESULT_FILENAME);
		SysNatTestUtils.assertFileExists( detailReportFile );
		final String report = SysNatFileUtil.readTextFileToString(detailReportFile); 
		String expectedText = "Script MiniTestScript has been executed!";
		SysNatTestUtils.assertReportContains(report, expectedText);
		expectedText = "PictureProof:";
		SysNatTestUtils.assertReportContains(report, expectedText);
		expectedText = "\\sources\\sysnat.test.execution\\..\\sysnat.quality.assurance\\target\\reportFolder/MiniTestCaseBILDNACHWEIS1";
		expectedText = expectedText.replace("reportFolder", reportFolder.getName());
		SysNatTestUtils.assertReportContains(report, expectedText);
	}	
	
	// **************************************************************************
	//                     P R I V A T E   M E T H O D S
	// **************************************************************************
	
	private File makeSureThatExpectedClassFileDoesNotExist(String testCaseName) 
	{
		String path = "../sysnat.test.execution/target/test-classes/com/iksgmbh/sysnat/test/integration/testcase";
		path = SysNatFileUtil.findAbsoluteFilePath(path);
		final File expectedClassFile = new File(path, testCaseName + ".class");
		expectedClassFile.delete();
		assertFalse(expectedClassFile.exists());
		return expectedClassFile;
	}

	private File makeSureThatResultFileDoesNotExist(String filename) 
	{
		String path = "../sysnat.quality.assurance/target/" + filename;
		File resultFile = new File(SysNatFileUtil.findAbsoluteFilePath(path));
		SysNatFileUtil.deleteFile(resultFile);
		assertFalse(resultFile.exists());
		return resultFile;
	}

	private void createJavaSourceFileInExecutionDir(final String testdataSubFolder, 
			                                        final List<String> fileList,
			                                        final String packagePath) 
	{
		for (String filename : fileList) 
		{
			final String sourcePathAndFilename = SysNatFileUtil.findAbsoluteFilePath(
					TESTDATA_DIR + testdataSubFolder + "/" + filename);
			String path = EXECUTION_DIR + packagePath + filename;
			path = SysNatFileUtil.findAbsoluteFilePath(path);
			final File targetFile = new File(path);
			targetFile.getParentFile().mkdirs();
			SysNatFileUtil.copyBinaryFile( sourcePathAndFilename, targetFile);
		}
	
	}
	
}