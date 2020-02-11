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
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.JavaCommand;
import com.iksgmbh.sysnat.domain.JavaCommand.CommandType;

public class XXGroupBuilderClassLevelTest 
{
	
	@Before
	public void setup() 
	{
		GenerationRuntimeInfo.reset();
		ExecutionRuntimeInfo.setSysNatSystemProperty("sysnat.testdata.import.directory", "../sysnat.testcase.generation/src/test/resources/testData");
		ExecutionRuntimeInfo.setSysNatSystemProperty("ApplicationUnderTest", "TestParameterTestApplication");
		ExecutionRuntimeInfo.setSysNatSystemProperty("sysnat.properties.path", "../sysnat.testcase.generation/src/test/resources/testData/TestParameterTestApplication");
		ExecutionRuntimeInfo.setSysNatSystemProperty(SysNatConstants.TESTING_CONFIG_PROPERTY, "../sysnat.testcase.generation/src/test/resources/testData/TestParameterTestApplication/TestParameterTestApplication.config");
		GenerationRuntimeInfo.getInstance();
	}

	@Test
	public void findsXXInGroups() 
	{
		// arrange
		final HashMap<Filename, List<JavaCommand>> javaCommandCollectionRaw = new HashMap<>();
		javaCommandCollectionRaw.put(new Filename("firstFile"), createBehaviorCommandListWithPreconditionAndCleanup());
		javaCommandCollectionRaw.put(new Filename("secondFile"), createFeatureCommandList());
		
		// act
		final HashMap<Filename, List<JavaCommand>> javaCommandCollection = 
				XXGroupBuilder.doYourJob(javaCommandCollectionRaw);
		
		// arrange
		assertEquals("Number of testcases", 4, javaCommandCollection.size());

		String toFind = "firstFile/BehaviourId/XXId2_Test.java";
		int i = 0;
		assertEquals("Java Command", "private static final String BEHAVIOUR_ID = \"BehaviourId\";", getCommandListFor(javaCommandCollection, toFind).get(i++).value);
		assertEquals("Java Command", "templateContainer.declareXXGroupForBehaviour(\"BehaviourId\");", getCommandListFor(javaCommandCollection, toFind).get(i++).value);
		assertEquals("Java Command", "templateContainer.prepareOnce1();", getCommandListFor(javaCommandCollection, toFind).get(i++).value);
		assertEquals("Java Command", "templateContainer.prepareOnce2();", getCommandListFor(javaCommandCollection, toFind).get(i++).value);
		assertEquals("Java Command", "languageTemplatesCommon.createComment(\"End of OneTimePrecondition\");", getCommandListFor(javaCommandCollection, toFind).get(i++).value);
		assertEquals("Java Command", "templateContainer.startNewXX(\"XXId2\");", getCommandListFor(javaCommandCollection, toFind).get(i++).value);
		assertEquals("Java Command", "templateContainer.prepare1();", getCommandListFor(javaCommandCollection, toFind).get(i++).value);
		assertEquals("Java Command", "templateContainer.prepare2();", getCommandListFor(javaCommandCollection, toFind).get(i++).value);
		assertEquals("Java Command", "templateContainer.doSomethingElse();", getCommandListFor(javaCommandCollection, toFind).get(i++).value);
		assertEquals("Java Command", "templateContainer.cleanup1();", getCommandListFor(javaCommandCollection, toFind).get(i++).value);
		assertEquals("Java Command", "templateContainer.cleanup2();", getCommandListFor(javaCommandCollection, toFind).get(i++).value);
		assertEquals("Java Command", "languageTemplatesCommon.createComment(\"Start of OneTimeCleanup\");", getCommandListFor(javaCommandCollection, toFind).get(i++).value);
		assertEquals("Java Command", "templateContainer.cleanupOnce1();", getCommandListFor(javaCommandCollection, toFind).get(i++).value);
		assertEquals("Java Command", "templateContainer.cleanupOnce2();", getCommandListFor(javaCommandCollection, toFind).get(i++).value);
		
		toFind = "secondFile/FeatureId/XXId1_Test.java";
		assertEquals("Java Command", "private static final String BEHAVIOUR_ID = \"FeatureId\";", getCommandListFor(javaCommandCollection, toFind).get(0).value);
		assertEquals("Java Command", "templateContainer.setBddKeyword(\"Feature\");", getCommandListFor(javaCommandCollection, toFind).get(1).value);
		assertEquals("Java Command", "templateContainer.declareXXGroupForBehaviour(\"FeatureId\");", getCommandListFor(javaCommandCollection, toFind).get(2).value);
		assertEquals("Java Command", "templateContainer.setBddKeyword(\"Scenario\");", getCommandListFor(javaCommandCollection, toFind).get(3).value);
		assertEquals("Java Command", "templateContainer.startNewXX(\"XXId1\");", getCommandListFor(javaCommandCollection, toFind).get(4).value);
	}

	@Test
	public void findsParameterizedXX() 
	{
		// arrange
		final HashMap<Filename, List<JavaCommand>> javaCommandCollectionRaw = new HashMap<>();
		javaCommandCollectionRaw.put(new Filename("firstFile"), createSimpleCommandList());
		javaCommandCollectionRaw.put(new Filename("secondFile"), createParametrizedCommandList());
		
		// act
		final HashMap<Filename, List<JavaCommand>> javaCommandCollection = 
				XXGroupBuilder.doYourJob(javaCommandCollectionRaw);
		
		// arrange
		assertEquals("Number of testcases", 3, javaCommandCollection.size());
		assertEquals("Java Command", "languageTemplatesCommon.startNewXX(\"ParamXXId_1\");", 
				                     getCommandListFor(javaCommandCollection, "ParamXXId_1_Test.java").get(1).value);
		assertEquals("Java Command", "languageTemplatesCommon.setTestData(\"TestParam_1\");", 
				                     getCommandListFor(javaCommandCollection, "ParamXXId_1_Test.java").get(2).value);
	}

	@Test
	public void buildsTableData() 
	{
		// arrange
		final HashMap<Filename, List<JavaCommand>> javaCommandCollectionRaw = new HashMap<>();
		final List<JavaCommand> commands = new ArrayList<>();
		commands.add(new JavaCommand("templateContainer.startNewXX(\"XXId\");"));
		commands.add(new JavaCommand("|Age|Name|" + SysNatConstants.LINE_SEPARATOR + "|12 |Lisa|"));
		javaCommandCollectionRaw.put(new Filename("aFile"), commands);
		
		// act
		final HashMap<Filename, List<JavaCommand>> javaCommandCollection = 
				XXGroupBuilder.doYourJob(javaCommandCollectionRaw);
		
		// arrange
		assertEquals("Java Command", "|Age|Name|<Line Separator>|12 |Lisa|", 
				                     getCommandListFor(javaCommandCollection, "aFile").get(1).value);
	}

	
	@Test
	public void throwsExceptionForTwoXXGroupsInOneNLXXFile() 
	{
		// arrange
		final HashMap<Filename, List<JavaCommand>> javaCommandCollectionRaw = new HashMap<>();
		javaCommandCollectionRaw.put(new Filename("aFile"), createCommandListWithTwoXXGroups());
		
		try {
			// act
			XXGroupBuilder.doYourJob(javaCommandCollectionRaw);
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", "Only one Behaviour can be declared in 'aFile'.",
					                      e.getMessage());
		}		
	}

	@Test
	public void throwsExceptionForTwoXXWithoutGroupDeclaration() 
	{
		// arrange
		ExecutionRuntimeInfo.setSysNatSystemProperty("sysnat.report.dir", "target");
		ExecutionRuntimeInfo.setSysNatSystemProperty(SysNatConstants.TEST_REPORT_NAME_SETTING_KEY, "TestReport");
		ExecutionRuntimeInfo.setSysNatSystemProperty("sysnat.dummy.test.run", "true");
		final HashMap<Filename, List<JavaCommand>> javaCommandCollectionRaw = new HashMap<>();
		javaCommandCollectionRaw.put(new Filename("aFile"), createCommandListWithTwoXXWithoutGroup());
		
		try {
			// act
			XXGroupBuilder.doYourJob(javaCommandCollectionRaw);
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", "Missing Behaviour declaration in 'aFile'.",
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
			XXGroupBuilder.doYourJob(javaCommandCollectionRaw);
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
			XXGroupBuilder.doYourJob(javaCommandCollectionRaw);
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", "Behaviour declaration must occur before XXID declaration in 'aFile'.",
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

	private List<JavaCommand> createBehaviorCommandListWithPreconditionAndCleanup()  
	{
		final List<JavaCommand> commands = new ArrayList<>();

		commands.add(new JavaCommand("templateContainer.declareXXGroupForBehaviour(\"BehaviourId\");"));
		commands.add(new JavaCommand("templateContainer.cleanupOnce1();", CommandType.OneTimeCleanup));
		commands.add(new JavaCommand("templateContainer.cleanupOnce2();", CommandType.OneTimeCleanup));
		commands.add(new JavaCommand("templateContainer.cleanup1();", CommandType.Cleanup));
		commands.add(new JavaCommand("templateContainer.cleanup2();", CommandType.Cleanup));
		commands.add(new JavaCommand("templateContainer.prepare1();", CommandType.Precondition));
		commands.add(new JavaCommand("templateContainer.prepare2();", CommandType.Precondition));
		commands.add(new JavaCommand("templateContainer.prepareOnce1();", CommandType.OneTimePrecondition));
		commands.add(new JavaCommand("templateContainer.prepareOnce2();", CommandType.OneTimePrecondition));
		commands.add(new JavaCommand("templateContainer.startNewXX(\"XXId1\");"));
		commands.add(new JavaCommand("templateContainer.doSomething();"));
		commands.add(new JavaCommand("templateContainer.startNewXX(\"XXId2\");"));
		commands.add(new JavaCommand("templateContainer.doSomethingElse();"));
		
		return commands;
	}
	
	private List<JavaCommand> createFeatureCommandList()  
	{
		final List<JavaCommand> commands = new ArrayList<>();

		commands.add(new JavaCommand("templateContainer.setBddKeyword(\"Feature\");"));
		commands.add(new JavaCommand("templateContainer.declareXXGroupForBehaviour(\"FeatureId\");"));
		commands.add(new JavaCommand("templateContainer.setBddKeyword(\"Scenario\");"));
		commands.add(new JavaCommand("templateContainer.startNewXX(\"XXId1\");"));
		commands.add(new JavaCommand("templateContainer.setBddKeyword(\"Given\");"));
		commands.add(new JavaCommand("templateContainer.doSomething();"));
		commands.add(new JavaCommand("templateContainer.setBddKeyword(\"Scenario\");"));
		commands.add(new JavaCommand("templateContainer.startNewXX(\"XXId2\");"));
		commands.add(new JavaCommand("templateContainer.setBddKeyword(\"Given\");"));
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

		commands.add(new JavaCommand("templateContainer.declareXXGroupForBehaviour(\"Group1\");"));
		commands.add(new JavaCommand("templateContainer.startNewXX(\"XXId1\");"));
		commands.add(new JavaCommand("templateContainer.doSomething();"));
		commands.add(new JavaCommand("templateContainer.startNewXX(\"XXId2\");"));
		commands.add(new JavaCommand("templateContainer.doSomethingElse();"));
		commands.add(new JavaCommand("templateContainer.declareXXGroupForBehaviour(\"Group2\");"));
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
		commands.add(new JavaCommand("templateContainer.declareXXGroupForBehaviour(\"Group1\");"));
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