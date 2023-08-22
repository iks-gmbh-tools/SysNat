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
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.iksgmbh.sysnat.GenerationRuntimeInfo;
import com.iksgmbh.sysnat.SysNatJUnitTestClassGenerator;
import com.iksgmbh.sysnat.common.helper.FileFinder;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;

/**
 * Tests for the interaction of sysnat.testcase.generation and sysnat.test.execution.
 * The interaction takes place via files generated by sysnat.testcase.generation 
 * and written into sysnat.test.execution.
 * 
 * @author Reik Oberrath
 */
public class TestcaseGeneration_ModuleLevelTest 
{
	private String testInputDir;

	@Before
	public void setup() {
		GenerationRuntimeInfo.reset();
		System.setProperty("sysnat.dummy.test.run", "true");
	}

	@Test
	public void createsJavaFilesForHelloWorldSpringBoot() throws Exception 
	{
		// arrange
		GenerationRuntimeInfo.setSysNatSystemProperty(SysNatConstants.TESTING_CONFIG_PROPERTY, "../sysnat.quality.assurance/src/test/resources/testSettingConfigs/HelloWorldSpringBoot_All.config");
		GenerationRuntimeInfo.setSysNatSystemProperty("Environment", "LOCAL");
		GenerationRuntimeInfo.getInstance();
		
		// act
		SysNatJUnitTestClassGenerator.doYourJob();

		// assert
		String filePath = (String)System.getProperty("sysnat.generation.target.dir");
		filePath = SysNatFileUtil.findAbsoluteFilePath(filePath);
		List<File> result = FileFinder.searchFilesRecursively(new File(filePath), new FilenameFilter() {
			@Override public boolean accept(File dir, String name) {
				return name.endsWith(".java");
			}
		});
		assertEquals("Number of Java files", 17, result.size());
	}	 
	
	@Test
	public void createsJUnitTestCaseForHomePageIKS() throws Exception 
	{
		// arrange
		GenerationRuntimeInfo.reset();
		GenerationRuntimeInfo.setSysNatSystemProperty(SysNatConstants.TESTING_CONFIG_PROPERTY, "../sysnat.quality.assurance/src/test/resources/testSettingConfigs/HomePageIKS.config");
		GenerationRuntimeInfo.setSysNatSystemProperty("Environment", "PRODUCTION");
		GenerationRuntimeInfo.getInstance();
		
		// act
		SysNatJUnitTestClassGenerator.doYourJob();

		// assert
		String filePath = (String)System.getProperty("sysnat.generation.target.dir");
		filePath = SysNatFileUtil.findAbsoluteFilePath(filePath);
		List<File> result = FileFinder.searchFilesRecursively(new File(filePath), new FilenameFilter() {
			@Override public boolean accept(File dir, String name) {
				return name.endsWith(".java");
			}
		});
		assertEquals("Number of generated Java files", 11, result.size());
		
		// check content of test case file
		String expectedFileContent = SysNatStringUtil.removeWhitespaceLinewise(
				SysNatFileUtil.readTextFileToString(
						"../sysnat.quality.assurance/src/test/resources/HomePageIKS_ExpectedTestCaseContent.txt"));
		String actualFileContent = SysNatStringUtil.removeWhitespaceLinewise(
				SysNatFileUtil.readTextFileToString( result.get(2) ) );
		assertEquals("Generated Java File Content", 
				     expectedFileContent, 
				     actualFileContent);
	}	 
	
	@Test
	@Ignore // script file is currently not needed
	public void createsScriptFileForHomePageIKS() throws Exception 
	{
		// arrange
		GenerationRuntimeInfo.setSysNatSystemProperty(SysNatConstants.TESTING_CONFIG_PROPERTY, "../sysnat.quality.assurance/src/test/resources/testSettingConfigs/HomePageIKS.config");
		GenerationRuntimeInfo.setSysNatSystemProperty("Environment", "PRODUCTION");
		GenerationRuntimeInfo.getInstance();
		
		// act
		SysNatJUnitTestClassGenerator.doYourJob();

		// assert
		String filePath = (String)System.getProperty("sysnat.generation.target.dir");
		List<File> result = FileFinder.searchFilesRecursively(new File(filePath), new FilenameFilter() {
			@Override public boolean accept(File dir, String name) {
				return name.endsWith("Script.java");
			}
		});
		assertEquals("Number of generated Script Java files", 1, result.size());
				
		// check content of script file
		String expectedFileContent = 
				SysNatFileUtil.readTextFileToString(
						"src/test/resources/HomePageIKS_ExpectedScriptContent.txt");
		String actualFileContent = 
				SysNatFileUtil.readTextFileToString( result.get(0) );
		assertEquals("Generated Java File Content", 
				     expectedFileContent, 
				     actualFileContent);		
	}	 

	
	@Test
	public void createsEmptyTestCaseForFakeTestApp() throws Exception 
	{
		// arrange
		System.setProperty("sysnat.testdata.import.directory", "../sysnat.quality.assurance/src/test/resources/testdata");
		prepareFakeTestApp();

		// act
		SysNatJUnitTestClassGenerator.doYourJob();
		
		// assert
		List<File> result = findGeneratedJavaFiles();
		assertEquals("Number", 2, result.size());
		
		String actualFileContent = SysNatStringUtil.removeWhitespaceLinewise(
				                       SysNatFileUtil.readTextFileToString(result.get(0)));
		String expectedFileContent = SysNatStringUtil.removeWhitespaceLinewise(
				                       SysNatFileUtil.readTextFileToString(testInputDir + "/Result.txt"));
		assertEquals("File Content", expectedFileContent, actualFileContent);
	}


	@Test
	public void createsTestCaseWithTestDataInstructionForSingleValues() throws Exception 
	{
		// arrange
		prepareFakeTestApp();

		// act
		SysNatJUnitTestClassGenerator.doYourJob();

		// assert
		List<File> result = findGeneratedJavaFiles();
		assertEquals("Number", 2, result.size());
		
		String actualFileContent = SysNatStringUtil.removeWhitespaceLinewise(
				                       SysNatFileUtil.readTextFileToString(result.get(1)));
		
		String expectedLine = "setTestData(\"Name=Lisa, Age=12\")";
		if (! actualFileContent.contains(expectedLine)) {
			System.err.println(actualFileContent);
		}
		assertTrue("Unexpected File Content", actualFileContent.contains(expectedLine));
	}

	private List<File> findGeneratedJavaFiles() {
		String filePath = (String)System.getProperty("sysnat.generation.target.dir") + "/faketestapp";
		List<File> result = FileFinder.searchFilesRecursively(new File(filePath), new FilenameFilter() {
			@Override public boolean accept(File dir, String name) {
				return name.endsWith(".java");
			}
		});
		return result;
	}	 
	
	
	@Test
	public void createsJavaFilesForParameterizedTest() throws Exception 
	{
		// arrange
		String path = SysNatFileUtil.findAbsoluteFilePath("../sysnat.quality.assurance/src/test/java/com/iksgmbh/sysnat/test/helper");
		setTestProperties("ParameterizedTestApp", path);
		
		// act
		SysNatJUnitTestClassGenerator.doYourJob();

		// assert
		String filePath = (String)System.getProperty("sysnat.generation.target.dir");
		filePath = SysNatFileUtil.findAbsoluteFilePath(filePath);
		List<File> result = FileFinder.searchFilesRecursively(new File(filePath), new FilenameFilter() {
			@Override public boolean accept(File dir, String name) {
				return name.endsWith(".java");
			}
		});
		assertEquals("Number", 4, result.size());
		List<String> filenames = result.stream().map(file -> file.getAbsolutePath()).collect(Collectors.toList());
		Collections.sort(filenames);
		
		String actualFileContent = SysNatStringUtil.removeWhitespaceLinewise(
				                   SysNatFileUtil.readTextFileToString(filenames.get(0)));
		String expectedFileContent = SysNatStringUtil.removeWhitespaceLinewise(
				                     SysNatFileUtil.readTextFileToString(testInputDir + "/FirstTestCaseFile.txt"));
		assertEquals("File Content", expectedFileContent, actualFileContent);
	}	 
	
	
	private void setTestProperties(final String appName, 
			                       final String languageTemplateContainerSourceDir) 
	{
		testInputDir = "../sysnat.quality.assurance/src/test/resources/testdata/" + appName;
		testInputDir = SysNatFileUtil.findAbsoluteFilePath(testInputDir);
		GenerationRuntimeInfo.reset();
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.executable.examples.source.dir", testInputDir);
		GenerationRuntimeInfo.setSysNatSystemProperty(SysNatConstants.TESTING_CONFIG_PROPERTY, testInputDir + "/settings.config");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.properties.path", testInputDir);
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.generation.target.dir", "target/" + appName);
		GenerationRuntimeInfo.setSysNatSystemProperty("execution.properties", testInputDir + "/execution.properties");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.help.command.list.file", "../sysnat.natural.language.executable.examples/help/ExistingNLInstructions_<testapp>.html");

		
		if (languageTemplateContainerSourceDir == null) {
			GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.languageTemplateContainer.source.dir", testInputDir);
		} else {
			GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.languageTemplateContainer.source.dir", languageTemplateContainerSourceDir);
		}
		GenerationRuntimeInfo.getInstance();
	}
	
	private void prepareFakeTestApp() 
	{
		String path = SysNatFileUtil.findAbsoluteFilePath("../sysnat.quality.assurance/src/test/java/com/iksgmbh/sysnat/test/helper");
		setTestProperties("FakeTestApp", path);
		File resultDir = new File(SysNatFileUtil.findAbsoluteFilePath("../sysnat.quality.assurance/target/faketestapp"));
		SysNatFileUtil.deleteFolder(resultDir);
		assertFalse(resultDir.exists());
		
	}	 
	
}