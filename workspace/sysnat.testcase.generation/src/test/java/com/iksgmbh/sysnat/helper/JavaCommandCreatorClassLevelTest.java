package com.iksgmbh.sysnat.helper;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.GenerationRuntimeInfo;
import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.LanguageInstructionPattern;
import com.iksgmbh.sysnat.domain.LanguageTemplatePattern;

public class JavaCommandCreatorClassLevelTest 
{
	
	@Before
	public void setup() {
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.testcase.source.dir", "./src/test/resources/testTestCases");
	}
	
	@Test
	public void createsJavaCommandForSimplePattern() throws Exception 
	{
		// arrange
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("methodWithoutParameter");
		final LanguageTemplatePattern templatePattern = LanguageTemplatePattern.createFrom(method, new Filename("test"), "containerFieldName").get(1);
		final LanguageInstructionPattern instructionPattern = new LanguageInstructionPattern("Do it.", "");
		
		// act
		String result = JavaCommandCreator.doYourJob(instructionPattern, templatePattern).value;
		
		// assert
		assertEquals("Java Command", "containerFieldName.methodWithoutParameter();", result);
	}

	@Test
	public void createsJavaCommandForPatternWithOneStringParameter() throws Exception 
	{
		// arrange
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("methodWithOneParameter", String.class);
		final LanguageTemplatePattern templatePattern = new LanguageTemplatePattern(method, new Filename("containerClass"), "containerFieldName");
		final LanguageInstructionPattern instructionPattern = new LanguageInstructionPattern(" Natural language instruction with \"one\" parameter. ", "");
		
		// act
		String result = JavaCommandCreator.doYourJob(instructionPattern, templatePattern).value;
		
		// assert
		assertEquals("Java Command", "containerFieldName.methodWithOneParameter(\"one\");", result);
	}

	@Test
	public void createsJavaCommandForPatternWithFourStringParameter() throws Exception 
	{
		// arrange
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("methodWithFourParameters", String.class, String.class, String.class, String.class);
		final LanguageTemplatePattern templatePattern = new LanguageTemplatePattern(method, new Filename("containerClass"), "containerFieldName");
		final LanguageInstructionPattern instructionPattern = new LanguageInstructionPattern("Natural ^p1^ language ^p2^ instruction ^p3^ with ^p4^ parameters.", "");
		
		// act
		String result = JavaCommandCreator.doYourJob(instructionPattern, templatePattern).value;
		
		// assert
		assertEquals("Java Command", "containerFieldName.methodWithFourParameters(\"p1\", \"p2\", \"p3\", \"p4\");", result);
	}

	@Test
	public void createsJavaCommandForPatternOnlyWithReturnValue() throws Exception 
	{
		// arrange
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("methodOnlyWithReturnValue");
		final LanguageTemplatePattern templatePattern = new LanguageTemplatePattern(method, new Filename("containerClass"), "containerFieldName");
		final LanguageInstructionPattern instructionPattern = new LanguageInstructionPattern("Create <r>.", "");
		
		// act
		String result = JavaCommandCreator.doYourJob(instructionPattern, templatePattern).value;
		
		// assert
		assertEquals("Java Command", "Integer r = containerFieldName.methodOnlyWithReturnValue();", result);
	}
	
	@Test
	public void createsJavaCommandForPatternWithOneIntParameter() throws Exception 
	{
		// arrange
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("anotherMethodWithOneParameter", int.class);
		final LanguageTemplatePattern templatePattern = new LanguageTemplatePattern(method, new Filename("containerClass"), "containerFieldName");
		final LanguageInstructionPattern instructionPattern = new LanguageInstructionPattern(" Natural language instruction with \"1\" parameter. ", "");
		
		// act
		String result = JavaCommandCreator.doYourJob(instructionPattern, templatePattern).value;
		
		// assert
		assertEquals("Java Command", "containerFieldName.anotherMethodWithOneParameter(1);", result);
	}
	
}
