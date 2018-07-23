package com.iksgmbh.sysnat.helper;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.GenerationRuntimeInfo;
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
		assertEquals("Java Command", "languageTemplatesCommon.startNewTestCase(\"ParamTestId_TestParam_1\");", getCommandListFor(javaCommandCollection, "ParamTestId_TestParam_1.java").get(0).value);
		assertEquals("Java Command", "languageTemplatesCommon.importTestData(\"TestParam_1\");", getCommandListFor(javaCommandCollection, "ParamTestId_TestParam_1.java").get(1).value);
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

		commands.add(new JavaCommand("templateContainer.startNewTestCase(\"ParamTestId\");"));
		commands.add(new JavaCommand("templateContainer.applyTestParameter(\"TestParam\");") );
		commands.add(new JavaCommand("templateContainer.doSomething();"));
		commands.add(new JavaCommand("templateContainer.doSomethingElse();"));
		
		return commands;
	}

	private List<JavaCommand> createSimpleCommandList() 
	{
		final List<JavaCommand> commands = new ArrayList<>();

		commands.add(new JavaCommand("templateContainer.startNewTestCase(\"aTestId\");"));
		commands.add(new JavaCommand("templateContainer.doSomething();"));
		commands.add(new JavaCommand("templateContainer.doSomethingElse();"));
		
		return commands;
	}

}
