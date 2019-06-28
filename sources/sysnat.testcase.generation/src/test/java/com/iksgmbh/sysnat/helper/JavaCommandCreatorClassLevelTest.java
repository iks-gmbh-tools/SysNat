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
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.executableExample.source.dir", "./src/test/resources/testTestCases");
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