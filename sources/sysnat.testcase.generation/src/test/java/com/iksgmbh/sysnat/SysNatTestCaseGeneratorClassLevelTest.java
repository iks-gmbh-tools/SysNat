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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;

public class SysNatTestCaseGeneratorClassLevelTest 
{
	@Before
	public void setup() {
		GenerationRuntimeInfo.reset();
		GenerationRuntimeInfo.setSysNatSystemProperty(SysNatConstants.TESTING_CONFIG_PROPERTY, "../sysnat.testcase.generation/src/test/resources/testSettingConfigs/TestCaseGeneratorTestApplication.config");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.properties.path", "../sysnat.testcase.generation/src/test/resources/execution_properties");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.executable.examples.source.dir", "../sysnat.testcase.generation/src/test/resources/testTestCases");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.generation.target.dir", "../sysnat.testcase.generation/target/SysNatTestCaseGeneratorTest");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.languageTemplateContainer.source.dir", "../sysnat.testcase.generation/src/test/java/com/iksgmbh/sysnat/test/testTemplateContainers");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.nls.lookup.file", "../sysnat.test.execution/AvailableNaturalLanguageScripts.properties");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.help.command.list.file", "../sysnat.testcase.generation/target/CommandTestLibrary.html");
		GenerationRuntimeInfo.getInstance();
	}

	@Test
	public void deletesTargetDirBeforeWritingNewFiles() throws IOException
	{
		// arrange
		final String path = SysNatFileUtil.findAbsoluteFilePath(System.getProperty("sysnat.generation.target.dir"));
		final File targetDir = new File(path);
		targetDir.mkdir();
		final File dummyFile = new File(targetDir, "Dummy.java");
		dummyFile.createNewFile();
		assertTrue(dummyFile.exists());
		
		// act
		SysNatJUnitTestClassGenerator.doYourJob();
		
		// arrange
		assertTrue("Missing target directory " + targetDir.getAbsolutePath(), targetDir.exists());
		final List<File> result = SysNatFileUtil.findFilesEndingWith(".java", targetDir);
		assertEquals("Number of java files", 4, result.size());	
	}
	
	@Test
	public void generatesTestCaseFiles() 
	{
		// arrange
		final String dir = SysNatFileUtil.findAbsoluteFilePath(System.getProperty("sysnat.generation.target.dir"));
		final File targetDir = new File(dir);
		SysNatFileUtil.deleteFolder(targetDir);
		assertFalse(targetDir.exists());
		
		// act
		SysNatJUnitTestClassGenerator.doYourJob();
		
		// assert
		assertTrue("Missing target directory " + targetDir.getAbsolutePath(), targetDir.exists());
		final List<File> result = SysNatFileUtil.findFilesEndingWith(".java", targetDir);
		assertEquals("Number of java files", 4, result.size());
		
		final String filename = result.get(0).getName();
		assertEquals("Name of java file", "ComplexInstructionSequenzTest.java", filename);
		
		String actualFileContent = SysNatStringUtil.removeLicenceComment( SysNatFileUtil.readTextFileToString(result.get(0)) );
		actualFileContent = SysNatStringUtil.removeWhitespaceLinewise(actualFileContent);
		String expectedFileContent = SysNatFileUtil.readTextFileToString("../sysnat.testcase.generation/src/test/resources/expectedResults/" +
				                     "TestCaseGeneratorTest.txt");
		expectedFileContent = SysNatStringUtil.removeWhitespaceLinewise(expectedFileContent);
		assertEquals("File Content", expectedFileContent, actualFileContent);

		final String testdataFilename = result.get(1).getName();
		assertEquals("Name of java file", "TestDataTableSequenceTest.java", testdataFilename);
		actualFileContent = SysNatFileUtil.readTextFileToString(result.get(1));
		assertTrue("Unexpected file content.", actualFileContent.contains("complexInstructionTestApplication_LanguageTemplateContainer.setTestData("
				                                + "\"|A|B|C|<Line Separator>|1|2|3|<Line Separator>|4|5|6|\");"));
		assertFalse("Unexpected file content.", actualFileContent.contains("complexInstructionTestApplication_LanguageTemplateContainer.setTestData(\"-\");"));
	}

	@Test
	public void createsCommandLibrary() throws IOException
	{
		// arrange
		final String path = SysNatFileUtil.findAbsoluteFilePath(System.getProperty("sysnat.generation.target.dir"));
		final File targetDir = new File(path);
		targetDir.mkdir();
		final File dummyFile = new File(targetDir, "Dummy.java");
		dummyFile.createNewFile();
		assertTrue(dummyFile.exists());
		
		// act
		SysNatJUnitTestClassGenerator.doYourJob();
		
		// assert
		String content = SysNatFileUtil.readTextFileToString("../sysnat.testcase.generation/target/CommandTestLibrary.html");
		String expected = SysNatFileUtil.readTextFileToString("../sysnat.testcase.generation/src/test/resources/expectedResults/CommandLibrary.txt");
		assertEquals("File content", expected, content);	
	}
	
	@Test
	public void createsTwoJUnitTestClassesFromNLXXfileForBehaviourWithTwoXXs() throws IOException
	{
		// arrange
		String path = SysNatFileUtil.findAbsoluteFilePath(System.getProperty("sysnat.generation.target.dir"));
		File targetDir = new File(path);
		SysNatFileUtil.deleteFolder(targetDir);
		assertFalse(targetDir.exists());
		
		// act
		SysNatJUnitTestClassGenerator.doYourJob();
		
		// assert
		assertTrue("Missing target directory " + targetDir.getAbsolutePath(), targetDir.exists());
		final List<File> result = SysNatFileUtil.findFilesEndingWith(".java", targetDir);
		assertEquals("Number of java files", 4, result.size());

		final String testdataFilename = result.get(3).getName();
		assertEquals("Name of java file", "XX2_Test.java", testdataFilename);
		final String actualFileContent = SysNatStringUtil.removeWhitespaceLinewise(
				                         SysNatFileUtil.readTextFileToString(result.get(3)));
		final String expected = "complexInstructionTestApplication_LanguageTemplateContainer.declareXXGroupForBehaviour(\"TestExampleGroup\");"
                + System.getProperty("line.separator") +
                "complexInstructionTestApplication_LanguageTemplateContainer.startNewXX(\"XX2\");";
		assertTrue("Unexpected file content.", actualFileContent.contains(expected));
	}

	@Test
	public void generatesTestCaseFilesWithBddKeywords()
	{
		GenerationRuntimeInfo.reset();
		GenerationRuntimeInfo.setSysNatSystemProperty(SysNatConstants.TESTING_CONFIG_PROPERTY, "../sysnat.testcase.generation/src/test/resources/testSettingConfigs/ScenarioBasedApplication.config");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.properties.path", "../sysnat.testcase.generation/src/test/resources/execution_properties");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.executable.examples.source.dir", "../sysnat.testcase.generation/src/test/resources/testTestCases");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.generation.target.dir", "../sysnat.testcase.generation/target/ScenarioBasedApplicationTest");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.languageTemplateContainer.source.dir", "../sysnat.testcase.generation/src/test/java/com/iksgmbh/sysnat/test/testTemplateContainers");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.nls.lookup.file", "../sysnat.test.execution/AvailableNaturalLanguageScripts.properties");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.help.command.list.file", "../sysnat.testcase.generation/target/CommandTestLibrary.txt");
		GenerationRuntimeInfo.getInstance();

		// arrange
		final String path = SysNatFileUtil.findAbsoluteFilePath(System.getProperty("sysnat.generation.target.dir"));
		final File targetDir = new File(path);
		SysNatFileUtil.deleteFolder(targetDir);
		assertFalse(targetDir.exists());

		// act
		SysNatJUnitTestClassGenerator.doYourJob();

		// assert
		assertTrue("Missing target directory " + targetDir.getAbsolutePath(), targetDir.exists());
		final List<File> result = SysNatFileUtil.findFilesEndingWith(".java", targetDir);
		assertEquals("Number of java files", 2, result.size());

		String actualFileContent = SysNatStringUtil.removeLicenceComment( SysNatFileUtil.readTextFileToString(result.get(0)) );
		actualFileContent = SysNatStringUtil.removeWhitespaceLinewise(actualFileContent);
		String expectedFileContent = SysNatFileUtil.readTextFileToString("../sysnat.testcase.generation/src/test/resources/expectedResults/"
				                                                          + "ScenarioBasedTestCase.txt");
		expectedFileContent = SysNatStringUtil.removeWhitespaceLinewise(expectedFileContent);
		assertEquals("File Content", expectedFileContent, actualFileContent);
	}

	@Test
	public void generatesTestCaseFilesWithBehaviourLevelInstructions()
	{
		GenerationRuntimeInfo.reset();
		GenerationRuntimeInfo.setSysNatSystemProperty(SysNatConstants.TESTING_CONFIG_PROPERTY, "../sysnat.testcase.generation/src/test/resources/testSettingConfigs/BehaviourLevelInstructionTestApp.config");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.properties.path", "../sysnat.testcase.generation/src/test/resources/execution_properties");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.executable.examples.source.dir", "../sysnat.testcase.generation/src/test/resources/testTestCases");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.generation.target.dir", "../sysnat.testcase.generation/target/ScenarioBasedApplicationTest");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.languageTemplateContainer.source.dir", "../sysnat.testcase.generation/src/test/java/com/iksgmbh/sysnat/test/testTemplateContainers");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.nls.lookup.file", "../sysnat.test.execution/AvailableNaturalLanguageScripts.properties");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.help.command.list.file", "../sysnat.testcase.generation/target/CommandTestLibrary.txt");
		GenerationRuntimeInfo.getInstance();

		// arrange
		final String path = SysNatFileUtil.findAbsoluteFilePath(System.getProperty("sysnat.generation.target.dir"));
		final File targetDir = new File(path);
		SysNatFileUtil.deleteFolder(targetDir);
		assertFalse(targetDir.exists());

		// act
		SysNatJUnitTestClassGenerator.doYourJob();

		// assert
		assertTrue("Missing target directory " + targetDir.getAbsolutePath(), targetDir.exists());
		final List<File> result = SysNatFileUtil.findFilesEndingWith(".java", targetDir);
		assertEquals("Number of java files", 6, result.size());

		String actualFileContent = SysNatStringUtil.removeLicenceComment( SysNatFileUtil.readTextFileToString(result.get(0)) );
		actualFileContent = SysNatStringUtil.removeWhitespaceLinewise(actualFileContent);
		String expectedFileContent = SysNatFileUtil.readTextFileToString("../sysnat.testcase.generation/src/test/resources/expectedResults/"
				                                                          + "BehaviourLevelInstructionTestCase1.txt");
		expectedFileContent = SysNatStringUtil.removeWhitespaceLinewise(expectedFileContent);
		assertEquals("File Content", expectedFileContent, actualFileContent);

		actualFileContent = SysNatStringUtil.removeLicenceComment( SysNatFileUtil.readTextFileToString(result.get(5)) );
		actualFileContent = SysNatStringUtil.removeWhitespaceLinewise(actualFileContent);
		expectedFileContent = SysNatFileUtil.readTextFileToString("../sysnat.testcase.generation/src/test/resources/expectedResults/"
				                                                          + "BehaviourLevelInstructionTestCase2.txt");
		expectedFileContent = SysNatStringUtil.removeWhitespaceLinewise(expectedFileContent);
		assertEquals("File Content", expectedFileContent, actualFileContent);

	}
	
}