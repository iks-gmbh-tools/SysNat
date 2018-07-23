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
		assertEquals("Number of java files", 1, result.size());	
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
		
		// arrange
		assertTrue("Missing targer directory " + targetDir.getAbsolutePath(), targetDir.exists());
		final List<File> result = SysNatFileUtil.findFilesEndingWith(".java", targetDir);
		assertEquals("Number of java files", 1, result.size());
		
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
		
		// arrange
		String content = SysNatFileUtil.readTextFileToString("../sysnat.testcase.generation/target/CommandTestLibrary.txt");
		String expected = SysNatFileUtil.readTextFileToString("../sysnat.testcase.generation/src/test/resources/expectedResults/CommandLibrary.txt");
		assertEquals("File content", expected, content);	
	}
	
}
