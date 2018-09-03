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
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.GenerationRuntimeInfo;
import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.JavaCommand;

public class TestSeriesBuilderClassLevelTest 
{
	
	@Before
	public void setup() {
		GenerationRuntimeInfo.reset();
		ExecutionRuntimeInfo.setSysNatSystemProperty("sysnat.testdata.import.directory", "../sysnat.testcase.generation/src/test/resources/testData");
		ExecutionRuntimeInfo.setSysNatSystemProperty("ApplicationUnderTest", "TestParameterTestApplication");
		ExecutionRuntimeInfo.setSysNatSystemProperty("sysnat.properties.path", "../sysnat.testcase.generation/src/test/resources/testData/TestParameterTestApplication");
		ExecutionRuntimeInfo.setSysNatSystemProperty("settings.config", "../sysnat.testcase.generation/src/test/resources/testData/TestParameterTestApplication/TestParameterTestApplication.config");
		GenerationRuntimeInfo.getInstance();
	}

	@Test
	public void findsParameterizedTestCases() 
	{
		// arrange
		final HashMap<Filename, List<JavaCommand>> javaCommandCollectionRaw = new HashMap<>();
		javaCommandCollectionRaw.put(new Filename("firstFile"), createSimpleCommandList());
		javaCommandCollectionRaw.put(new Filename("secondFile"), createParametrizedCommandList());
		
		// act
		final HashMap<Filename, List<JavaCommand>> javaCommandCollection = 
				TestSeriesBuilder.doYourJob(javaCommandCollectionRaw);
		
		// arrange
		assertEquals("Number of testcases", 3, javaCommandCollection.size());
		assertEquals("Java Command", "languageTemplatesCommon.startNewXX(\"ParamXXId_1\");", getCommandListFor(javaCommandCollection, "ParamXXId_1_Test.java").get(0).value);
		assertEquals("Java Command", "languageTemplatesCommon.importTestData(\"TestParam_1\");", getCommandListFor(javaCommandCollection, "ParamXXId_1_Test.java").get(1).value);
	}

	@Test
	public void throwsExceptionForTwoXXGroupsInOneNLXXFile() 
	{
		// arrange
		final HashMap<Filename, List<JavaCommand>> javaCommandCollectionRaw = new HashMap<>();
		javaCommandCollectionRaw.put(new Filename("aFile"), createCommandListWithTwoXXGroups());
		
		try {
			// act
			TestSeriesBuilder.doYourJob(javaCommandCollectionRaw);
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", "Only one Rule can be declared in 'aFile'.", 
					                      e.getMessage());
		}		
	}

	@Test
	public void throwsExceptionForTwoXXWithoutGroupDeclaration() 
	{
		// arrange
		final HashMap<Filename, List<JavaCommand>> javaCommandCollectionRaw = new HashMap<>();
		javaCommandCollectionRaw.put(new Filename("aFile"), createCommandListWithTwoXXWithoutGroup());
		
		try {
			// act
			TestSeriesBuilder.doYourJob(javaCommandCollectionRaw);
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", "Missing Rule declaration in 'aFile'.", 
					                      e.getMessage());
		}		
	}

	@Test
	public void throwsExceptionForMissingXXIDDeclaration() 
	{
		// arrange
		final HashMap<Filename, List<JavaCommand>> javaCommandCollectionRaw = new HashMap<>();
		javaCommandCollectionRaw.put(new Filename("aFile"), createCommandListWithMissingXXID());
		
		try {
			// act
			TestSeriesBuilder.doYourJob(javaCommandCollectionRaw);
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", "Missing XXID declaration in 'aFile'.", 
					                      e.getMessage());
		}		
	}
	
	
	@Test
	public void throwsExceptionForXXGroupDeclarationAfterXXIDDeclaration() 
	{
		// arrange
		final HashMap<Filename, List<JavaCommand>> javaCommandCollectionRaw = new HashMap<>();
		javaCommandCollectionRaw.put(new Filename("aFile"), createCommandListWithXXGroupAfterXXID());
		
		try {
			// act
			TestSeriesBuilder.doYourJob(javaCommandCollectionRaw);
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", "Rule declaration must occur before XXID declaration in 'aFile'.", 
					                      e.getMessage());
		}		
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<JavaCommand> getCommandListFor(final HashMap<Filename, List<JavaCommand>> javaCommandCollection, 
			                                                       final String toFind) 
	{
		final List<Filename> keys = new ArrayList(javaCommandCollection.keySet());
		Filename filename = (Filename) keys.stream().filter(key -> key.value.equals(toFind)).findFirst().get();
		return javaCommandCollection.get(filename);
	}

	private List<JavaCommand> createParametrizedCommandList() 
	{
		final List<JavaCommand> commands = new ArrayList<>();

		commands.add(new JavaCommand("templateContainer.startNewXX(\"ParamXXId\");"));
		commands.add(new JavaCommand("templateContainer.applyTestParameter(\"TestParam\");") );
		commands.add(new JavaCommand("templateContainer.doSomething();"));
		commands.add(new JavaCommand("templateContainer.doSomethingElse();"));
		
		return commands;
	}

	private List<JavaCommand> createSimpleCommandList() 
	{
		final List<JavaCommand> commands = new ArrayList<>();

		commands.add(new JavaCommand("templateContainer.startNewXX(\"XXId\");"));
		commands.add(new JavaCommand("templateContainer.doSomething();"));
		commands.add(new JavaCommand("templateContainer.doSomethingElse();"));
		
		return commands;
	}

	private List<JavaCommand> createCommandListWithTwoXXGroups() 
	{
		final List<JavaCommand> commands = new ArrayList<>();

		commands.add(new JavaCommand("templateContainer.declareXXGroupForRule(\"Group1\");"));
		commands.add(new JavaCommand("templateContainer.startNewXX(\"XXId1\");"));
		commands.add(new JavaCommand("templateContainer.doSomething();"));
		commands.add(new JavaCommand("templateContainer.startNewXX(\"XXId2\");"));
		commands.add(new JavaCommand("templateContainer.doSomethingElse();"));
		commands.add(new JavaCommand("templateContainer.declareXXGroupForRule(\"Group2\");"));
		commands.add(new JavaCommand("templateContainer.startNewXX(\"XXId3\");"));
		commands.add(new JavaCommand("templateContainer.doSomething();"));
		commands.add(new JavaCommand("templateContainer.doSomethingElse();"));
		
		return commands;
	}
	
	private List<JavaCommand> createCommandListWithTwoXXWithoutGroup() 
	{
		final List<JavaCommand> commands = new ArrayList<>();

		commands.add(new JavaCommand("templateContainer.startNewXX(\"XXId1\");"));
		commands.add(new JavaCommand("templateContainer.doSomething();"));
		commands.add(new JavaCommand("templateContainer.startNewXX(\"XXId2\");"));
		commands.add(new JavaCommand("templateContainer.doSomethingElse();"));
		
		return commands;
	}
	

	
	private List<JavaCommand> createCommandListWithXXGroupAfterXXID() 
	{
		final List<JavaCommand> commands = new ArrayList<>();

		commands.add(new JavaCommand("templateContainer.startNewXX(\"XXId1\");"));
		commands.add(new JavaCommand("templateContainer.doSomething();"));
		commands.add(new JavaCommand("templateContainer.declareXXGroupForRule(\"Group1\");"));
		commands.add(new JavaCommand("templateContainer.startNewXX(\"XXId2\");"));
		commands.add(new JavaCommand("templateContainer.doSomethingElse();"));
		commands.add(new JavaCommand("templateContainer.startNewXX(\"XXId3\");"));
		commands.add(new JavaCommand("templateContainer.doSomething();"));
		commands.add(new JavaCommand("templateContainer.doSomethingElse();"));
		
		return commands;
	}

	private List<JavaCommand> createCommandListWithMissingXXID() 
	{
		final List<JavaCommand> commands = new ArrayList<>();

		commands.add(new JavaCommand("templateContainer.doSomething();"));
		commands.add(new JavaCommand("templateContainer.doSomethingElse();"));
		
		return commands;
	}

	
}