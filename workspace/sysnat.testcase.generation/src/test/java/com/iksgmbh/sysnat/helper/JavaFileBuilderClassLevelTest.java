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
package com.iksgmbh.sysnat.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.GenerationRuntimeInfo;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.JavaCommand;
import com.iksgmbh.sysnat.domain.JavaFieldData;

public class JavaFileBuilderClassLevelTest 
{
	private String targetDir = "../sysnat.testcase.generation/target/testTargetDir";
	private HashMap<Filename, List<JavaCommand>> javaCommandCollection = new HashMap<>();
	private List<JavaCommand> commands = new ArrayList<>();
	private List<JavaFieldData> javaFieldData;
	
	@Before
	public void setup() {
		GenerationRuntimeInfo.reset();
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.languageTemplateContainer.source.dir", 
		           "../sysnat.testcase.generation/src/test/java/com/iksgmbh/sysnat/test/testTemplateContainers/testcasegeneratortestapplication");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.generation.target.dir", targetDir);
		GenerationRuntimeInfo.getInstance();
		javaFieldData = LanguageTemplateContainerFinder.findLanguageTemplateContainers("");
	}
	
	@Test
	public void buildsTargetFileName() 
	{
		// arrange
		javaCommandCollection.put(new Filename("com/iksgmbh/sysnat/test/ExecutableExample.java"), commands);
		
		// act
		final HashMap<File, String> result = JavaFileBuilder.doYourJob(javaCommandCollection, 
				                                                       "HomePageIKS",
				                                                       javaFieldData);
		
		// assert
		assertEquals("Number of test classes", 1, result.size());
		String pathAndFileName = result.keySet().iterator().next().getAbsolutePath();
		int pos = pathAndFileName.lastIndexOf("sysnat.testcase.generation");
		pathAndFileName = "..\\" + pathAndFileName.substring(pos);
		String expected = (targetDir + "/com/iksgmbh/sysnat/test/ExecutableExample.java").replaceAll("/", "\\\\");
		assertEquals("Filename", expected, 
				                 pathAndFileName);
	}
	
	@Test
	public void buildsContentOfJUnitTestCaseFile() 
	{
		// arrange
		final String command1 = "a.do();";
		final String command2 = "b.do();";
		commands.add(new JavaCommand(command1));
		commands.add(new JavaCommand(command2));
		javaCommandCollection.put(new Filename("com/iksgmbh/sysnat/test/TestCaseFilename.java"), commands);
		javaFieldData = LanguageTemplateContainerFinder.findLanguageTemplateContainers("");
		
		// act
		final HashMap<File, String> result = JavaFileBuilder.doYourJob(javaCommandCollection, 
				                                                       "HomePageIKS",
				                                                       javaFieldData);
		
		// assert
		assertEquals("Number of test classes", 1, result.size());		
		final File key = result.keySet().iterator().next();
		final String actualFileContent = SysNatStringUtil.removeLicenceComment( result.get(key).trim() );
		System.out.println(actualFileContent);
		
		String expectedFileContent = SysNatFileUtil.readTextFileToString(
						"../sysnat.testcase.generation/src/test/resources/expectedResults/TestCaseContent.txt");
		assertEquals("Generated Java File Content", 
				     expectedFileContent, 
				     actualFileContent);
	}

	@Test
	public void buildsContentOfScriptFile() 
	{
		// arrange
		final String command1 = "a.do();";
		final String command2 = "b.do();";
		commands.add(new JavaCommand(command1));
		commands.add(new JavaCommand(command2));
		javaCommandCollection.put(new Filename("com/iksgmbh/sysnat/test/FilenameScript.java"), commands);
		
		// act
		final HashMap<File, String> result = JavaFileBuilder.doYourJob(javaCommandCollection, 
				                                                       "HomePageIKS",
				                                                       javaFieldData);
		
		// assert
		assertEquals("Number of test classes", 1, result.size());
		
		final File key = result.keySet().iterator().next();
		final String actualFileContent = SysNatStringUtil.removeLicenceComment( result.get(key).trim() );
		//System.out.println(actualFileContent);
		
		String expectedFileContent = 
				SysNatFileUtil.readTextFileToString(
						"../sysnat.testcase.generation/src/test/resources/expectedResults/ScriptContent.txt");
		assertEquals("Generated Java File Content", 
				     expectedFileContent, 
				     actualFileContent);
	}

	@Test
	public void doesNotAddIntegerAsImportType() throws Exception 
	{
		// arrange
		final String command1 = "a.do();";
		commands.add(new JavaCommand(command1, Integer.class));
		javaCommandCollection.put(new Filename("com/iksgmbh/sysnat/test/FilenameScript.java"), commands);
		
		// act
		final HashMap<File, String> result = JavaFileBuilder.doYourJob(javaCommandCollection, 
				                                                       "HomePageIKS",
				                                                       javaFieldData);
		
		// assert
		assertEquals("Number of test classes", 1, result.size());
		File firstElement = result.keySet().iterator().next();
		System.out.println(result.get(firstElement));
		assertFalse("Integer has been wronly added as import type", 
				    result.get(firstElement).contains("java.lang.Integer"));
	}

	@Test
	public void addsFileAsImportType() throws Exception 
	{
		// arrange
		final String command1 = "a.do();";
		commands.add(new JavaCommand(command1, File.class));
		javaCommandCollection.put(new Filename("com/iksgmbh/sysnat/test/FilenameScript.java"), commands);
		
		// act
		final HashMap<File, String> result = JavaFileBuilder.doYourJob(javaCommandCollection, 
				                                                       "HomePageIKS",
				                                                       javaFieldData);
		
		// assert
		assertEquals("Number of test classes", 1, result.size());
		File firstElement = result.keySet().iterator().next();
		System.out.println(result.get(firstElement));
		assertTrue("File has not been added as import type", 
				    result.get(firstElement).contains("import java.io.File;"));
	}

	@Test
	public void addsReturnTypeDuplicatesOnlyOnceToImportTypes() throws Exception 
	{
		// arrange
		final String command = "a.do();";
		commands.add(new JavaCommand(command, File.class));
		commands.add(new JavaCommand(command, File.class));
		javaCommandCollection.put(new Filename("com/iksgmbh/sysnat/test/FilenameScript.java"), commands);
		
		// act
		final HashMap<File, String> result = JavaFileBuilder.doYourJob(javaCommandCollection, 
				                                                       "HomePageIKS",
				                                                       javaFieldData);
		
		// assert
		assertEquals("Number of test classes", 1, result.size());
		final File firstElement = result.keySet().iterator().next();
		final String javaFileContent = result.get(firstElement);
		final int actualOccurrence = SysNatStringUtil.countNumberOfOccurrences(javaFileContent, 
				                                                               "import java.io.File;");
		System.out.println(javaFileContent);
		assertEquals("Number of File imports", 1, actualOccurrence);
	}
	
}