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

import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

public class SysNatTestCaseGeneratorClassLevelTest 
{
	@Before
	public void setup() {
		GenerationRuntimeInfo.reset();
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.properties.path", "../sysnat.testcase.generation/src/test/resources/execution_properties");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.executable.examples.source.dir", "../sysnat.testcase.generation/src/test/resources/testTestCases");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.generation.target.dir", "../sysnat.testcase.generation/target/SysNatTestCaseGeneratorTest");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.languageTemplateContainer.source.dir", "../sysnat.testcase.generation/src/test/java/com/iksgmbh/sysnat/test/testTemplateContainers");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.nls.lookup.file", "../sysnat.test.execution/AvailableNaturalLanguageScripts.properties");
		GenerationRuntimeInfo.setSysNatSystemProperty("settings.config", "../sysnat.testcase.generation/src/test/resources/testSettingConfigs/TestCaseGeneratorTestApplication.config");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.help.command.list.file", "../sysnat.testcase.generation/target/CommandTestLibrary.txt");
		GenerationRuntimeInfo.getInstance();
	}

	@Test
	public void deletesTargetDirBeforeWritingNewFiles() throws IOException
	{
		// arrange
		final File targetDir = new File(System.getProperty("sysnat.generation.target.dir"));
		targetDir.mkdir();
		final File dummyFile = new File(targetDir, "Dummy.java");
		dummyFile.createNewFile();
		assertTrue(dummyFile.exists());
		
		// act
		SysNatTestCaseGenerator.doYourJob();
		
		// arrange
		assertTrue("Missing target directory " + targetDir.getAbsolutePath(), targetDir.exists());
		final List<File> result = SysNatFileUtil.findFilesEndingWith(".java", targetDir);
		assertEquals("Number of java files", 4, result.size());	
	}
	
	@Test
	public void generatesTestCaseFiles() 
	{
		// arrange
		final File targetDir = new File(System.getProperty("sysnat.generation.target.dir"));
		SysNatFileUtil.deleteFolder(targetDir);
		assertFalse(targetDir.exists());
		
		// act
		SysNatTestCaseGenerator.doYourJob();
		
		// assert
		assertTrue("Missing target directory " + targetDir.getAbsolutePath(), targetDir.exists());
		final List<File> result = SysNatFileUtil.findFilesEndingWith(".java", targetDir);
		assertEquals("Number of java files", 4, result.size());
		
		final String filename = result.get(0).getName();
		assertEquals("Name of java file", "ComplexInstructionSequenzTest.java", filename);
		
		final List<String> fileContent = SysNatFileUtil.readTextFile(result.get(0));
		final int numberOfExpectedLines = 52;
		if (fileContent.size() != numberOfExpectedLines) {
			System.err.println("Number of lines mismatch:");
			System.err.println("Expected: " + numberOfExpectedLines);
			System.err.println("Actual: " + fileContent.size());
		}

		String actualFileContent = SysNatFileUtil.readTextFileToString(result.get(0));
		String expectedFileContent = SysNatFileUtil.readTextFileToString("../sysnat.testcase.generation/src/test/resources/expectedResults/TestCaseGeneratorTest.txt");
		assertEquals("File Content", expectedFileContent, actualFileContent);

		final String testdataFilename = result.get(1).getName();
		assertEquals("Name of java file", "TestDataTableSequenceTest.java", testdataFilename);
		actualFileContent = SysNatFileUtil.readTextFileToString(result.get(1));
		assertTrue("Unexpected file content.", actualFileContent.contains("complexInstructionTestApplication_LanguageTemplateContainer.importTestData("
				                                + "\"|A|B|C|<Line Separator>|1|2|3|<Line Separator>|4|5|6|\");"));
		assertFalse("Unexpected file content.", actualFileContent.contains("complexInstructionTestApplication_LanguageTemplateContainer.importTestData(\"-\");"));
	}

	@Test
	public void createsCommandLibrary() throws IOException
	{
		// arrange
		final File targetDir = new File(System.getProperty("sysnat.generation.target.dir"));
		targetDir.mkdir();
		final File dummyFile = new File(targetDir, "Dummy.java");
		dummyFile.createNewFile();
		assertTrue(dummyFile.exists());
		
		// act
		SysNatTestCaseGenerator.doYourJob();
		
		// assert
		String content = SysNatFileUtil.readTextFileToString("../sysnat.testcase.generation/target/CommandTestLibrary.txt");
		String expected = SysNatFileUtil.readTextFileToString("../sysnat.testcase.generation/src/test/resources/expectedResults/CommandLibrary.txt");
		assertEquals("File content", expected, content);	
	}
	
	@Test
	public void createsTwoJUnitTestClassesFromNLXXfileForRuleWithTwoXXs() throws IOException
	{
		// arrange
		final File targetDir = new File(System.getProperty("sysnat.generation.target.dir"));
		SysNatFileUtil.deleteFolder(targetDir);
		assertFalse(targetDir.exists());
		
		// act
		SysNatTestCaseGenerator.doYourJob();
		
		// assert
		assertTrue("Missing target directory " + targetDir.getAbsolutePath(), targetDir.exists());
		final List<File> result = SysNatFileUtil.findFilesEndingWith(".java", targetDir);
		assertEquals("Number of java files", 4, result.size());

		final String testdataFilename = result.get(3).getName();
		assertEquals("Name of java file", "XX2.java", testdataFilename);
		final String actualFileContent = SysNatFileUtil.readTextFileToString(result.get(3));
		final String expected = "complexInstructionTestApplication_LanguageTemplateContainer.declareXXGroupForRule(\"TestExampleGroup\");"
                + System.getProperty("line.separator") +
                "			complexInstructionTestApplication_LanguageTemplateContainer.startNewXX(\"XX2\");";
		assertTrue("Unexpected file content.", actualFileContent.contains(expected));
	}
	
}