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
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.GenerationRuntimeInfo;
import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.exception.SysNatException.ErrorCode;
import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.JavaCommand;
import com.iksgmbh.sysnat.domain.JavaFieldData;
import com.iksgmbh.sysnat.domain.LanguageInstructionPattern;
import com.iksgmbh.sysnat.domain.LanguageTemplatePattern;

public class PatternMergeJavaCommandGeneratorClassLevelTest 
{
	private PatternMergeJavaCommandGenerator cut = new PatternMergeJavaCommandGenerator(null, null, "SubFolderTestApplication");
	
	@Before
	public void setup() {
		GenerationRuntimeInfo.reset();
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.executable.examples.source.dir", "../sysnat.testcase.generation/src/test/resources/testTestCases");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.languageTemplateContainer.source.dir", 
		                                              "../sysnat.testcase.generation/src/test/java/com/iksgmbh/sysnat/test/testTemplateContainers");
		ExecutionRuntimeInfo.setSysNatSystemProperty("settings.config", "../sysnat.test.runtime.environment/src/test/resources/testSettingConfigs/settingsHomePageIKS.config");
		GenerationRuntimeInfo.getInstance();
	}
	
	@Test
	public void buildsJavaFileName() throws Exception 
	{
		// act
		String result = cut.buildJavaFileName(new Filename("./src/test/resources/testTestCases/SubFolderTestApplication/Login/Test1.nlxx")).value;
		
		// assert
		assertEquals("Name of java file", "subfoldertestapplication/login/Test1Test.java", result);
	}
	
	@Test
	public void findsNoMatchForSimplePattern() throws Exception 
	{
		// arrange
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("methodWithoutParameter");
		final LanguageTemplatePattern templatePattern = LanguageTemplatePattern.createFrom(method, new Filename("test"), "").get(0);
		final LanguageInstructionPattern instructionPattern = new LanguageInstructionPattern("Do it.", "");
		
		// act
		boolean isMatching = cut.isMatching(templatePattern, instructionPattern);
		
		// assert
		assertFalse(isMatching);
	}

	@Test
	public void findsPatternMatchWithoutParameter() throws Exception 
	{
		// arrange
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("methodWithoutParameter");
		final LanguageTemplatePattern templatePattern = LanguageTemplatePattern.createFrom(method, new Filename("test"), "").get(0);
		final LanguageInstructionPattern instructionPattern = new LanguageInstructionPattern("Natural language instruction without parameter.", "");
		
		// act
		boolean isMatching = cut.isMatching(templatePattern, instructionPattern);
		
		// assert
		assertTrue(isMatching);
	}

	@Test
	public void findsPatternMatchWithOneStringParameter() throws Exception 
	{
		// arrange
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("methodWithOneParameter", String.class);
		final LanguageTemplatePattern templatePattern = new LanguageTemplatePattern(method, new Filename("test"), "");
		final LanguageInstructionPattern instructionPattern = new LanguageInstructionPattern(" Natural language instruction with \"one\" parameter. ", "");
		
		// act
		boolean isMatching = cut.isMatching(templatePattern, instructionPattern);
		
		// assert
		assertTrue(isMatching);
	}

	@Test
	public void findsPatternMatchWithOneIntParameter() throws Exception 
	{
		// arrange
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("anotherMethodWithOneParameter", int.class);
		final LanguageTemplatePattern templatePattern = new LanguageTemplatePattern(method, new Filename("test"), "");
		final LanguageInstructionPattern instructionPattern = new LanguageInstructionPattern("Natural language instruction with \"1\" parameter.", "");
		
		// act
		boolean isMatching = cut.isMatching(templatePattern, instructionPattern);
		
		// assert
		assertTrue(isMatching);
	}

	@Test
	public void throwsExceptionForParameterTypeMismatch() throws Exception 
	{
		// arrange
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("anotherMethodWithOneParameter", int.class);
		final LanguageTemplatePattern templatePattern = new LanguageTemplatePattern(method, new Filename("LanguageTemplateContainerTestImpl"), "");
		final LanguageInstructionPattern instructionPattern = new LanguageInstructionPattern("Natural language instruction with \"one\" parameter.", "PatternMergeJavaCommandGeneratorTest.throwsExceptionForParameterTypeMisMatch");
		
		try {
			// act
			cut.isMatching(templatePattern, instructionPattern);
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", ErrorCode.NATURAL_LANGUAGE_INSTRUCTING_PARSING__PARAMETER_TYPE_MISMATCH, e.getErrorCode());
		}
	}

	@Test
	public void findsPatternMatchWithReturnValueAndParameter() throws Exception 
	{
		// arrange
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("methodWithReturnValue", String.class);
		final LanguageTemplatePattern templatePattern = new LanguageTemplatePattern(method, new Filename("test"), "");
		final LanguageInstructionPattern instructionPattern = new LanguageInstructionPattern("Create <r> with \"p\".", "");
		
		// act
		boolean isMatching = cut.isMatching(templatePattern, instructionPattern);
		
		// assert
		assertTrue(isMatching);
	}

	@Test
	public void findsPatternMatchOnlyWithReturnValue() throws Exception 
	{
		// arrange
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("methodOnlyWithReturnValue");
		final LanguageTemplatePattern templatePattern = new LanguageTemplatePattern(method, new Filename("test"), "");
		final LanguageInstructionPattern instructionPattern = new LanguageInstructionPattern("Create <r>.", "");
		
		// act
		boolean isMatching = cut.isMatching(templatePattern, instructionPattern);
		
		// assert
		assertTrue(isMatching);
	}

	@Test
	public void findsPatternMatchWithFourParameter() throws Exception 
	{
		// arrange
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("methodWithFourParameters", String.class, String.class, String.class, String.class);
		final LanguageTemplatePattern templatePattern = new LanguageTemplatePattern(method, new Filename("test"), "");
		final LanguageInstructionPattern instructionPattern = new LanguageInstructionPattern("Natural ^p1^ language ^p2^ instruction ^p3^ with ^p4^ parameters.", "");
		
		// act
		boolean isMatching = cut.isMatching(templatePattern, instructionPattern);
		
		// assert
		assertTrue(isMatching);
	}
	
	@Test
	public void doesNotFindPatternMatchDueToLastSpaceMismatch() throws Exception 
	{
		// arrange
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("methodWithFourParameters", String.class, String.class, String.class, String.class);
		final LanguageTemplatePattern templatePattern = new LanguageTemplatePattern(method, new Filename("test"), "");
		final LanguageInstructionPattern instructionPattern = new LanguageInstructionPattern("Natural ^p1^ language ^p2^ instruction ^p3^ with ^p4^ parameters. ", "");
		
		// act
		boolean isMatching = cut.isMatching(templatePattern, instructionPattern);
		
		// assert
		assertFalse(isMatching);
	}

	@Test
	public void createsCodeForComplexInstructionTestApplication() throws Exception 
	{
		// arrange
		final HashMap<Filename, List<LanguageTemplatePattern>> languageTemplateCollection = 
				LanguageTemplateCollector.doYourJob(createJavaFieldData("ComplexInstructionTestApplication"));
		final HashMap<Filename, List<LanguageInstructionPattern>> languageInstructionCollection = 
				LanguageInstructionCollector.doYourJob("ComplexInstructionTestApplication");
		
		// act
		final HashMap<Filename, List<JavaCommand>> result = 
				PatternMergeJavaCommandGenerator.doYourJob(languageTemplateCollection, 
				                                           languageInstructionCollection,
				                                           "ComplexInstructionTestApplication");
		
		// assert
		assertEquals("Instruction file number", 1, result.size());
		
		final Filename key = result.keySet().iterator().next();
		assertEquals("Filename", "complexinstructiontestapplication//ComplexInstructionSequenzTest.java", key.value);
		
		final List<JavaCommand> javaCommands = result.get(key);
		assertEquals("number of commands", 5, javaCommands.size());
		
		assertEquals("java command", 
				     "Order auftrag_1 = complexInstructionTestApplication_LanguageTemplateContainer.createOrder();", 
				     javaCommands.get(0).value);
		assertEquals("java command", 
			         "Order auftrag_2 = complexInstructionTestApplication_LanguageTemplateContainer.createOrder();", 
			          javaCommands.get(1).value);
		assertEquals("java command", 
				     "complexInstructionTestApplication_LanguageTemplateContainer.addToOrder(auftrag_1, \"Wert A\");", 
				      javaCommands.get(2).value);
		assertEquals("java command", 
			         "complexInstructionTestApplication_LanguageTemplateContainer.addToOrder(auftrag_2, \"Wert B\");", 
			         javaCommands.get(3).value);
		assertEquals("java command", 
				     "Order auftrag_3 = complexInstructionTestApplication_LanguageTemplateContainer.toOrder(auftrag_1, auftrag_2, \"C\");", 
				     javaCommands.get(4).value);
	}

	@Test
	public void throwsExceptionForUnknownInstruction() throws ClassNotFoundException 
	{
		// arrange
		final HashMap<Filename, List<LanguageTemplatePattern>> languageTemplateCollection = 
				LanguageTemplateCollector.doYourJob(createJavaFieldData("DummyApplication"));
		final HashMap<Filename, List<LanguageInstructionPattern>> languageInstructionCollection = 
				LanguageInstructionCollector.doYourJob("ComplexInstructionTestApplication");
		System.setProperty("sysnat.dummy.test.run", "true");
		
		// act
		try {
			PatternMergeJavaCommandGenerator.doYourJob(languageTemplateCollection, 
				                                       languageInstructionCollection,
			                                           "TestApp");
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("Error Code", 
					     ErrorCode.MATCHING_INSTRUCTION_AND_LANGUAGE_TEMPLATES__UNKNOWN_INSTRUCTION, 
					     e.getErrorCode());
		}

	}
	
	@Test
	public void throwsExceptionForUnknownParamVariable() throws ClassNotFoundException 
	{
		// arrange
		final HashMap<Filename, List<LanguageTemplatePattern>> languageTemplateCollection = 
				LanguageTemplateCollector.doYourJob(createJavaFieldData("ComplexInstructionTestApplication"));
		final HashMap<Filename, List<LanguageInstructionPattern>> languageInstructionCollection = 
				LanguageInstructionCollector.doYourJob("UnknownParamVariableTestApplication");
		
		// act
		try {
			PatternMergeJavaCommandGenerator.doYourJob(languageTemplateCollection, 
				                                       languageInstructionCollection,
			                                           "TestApp");
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("Error Code", 
					     ErrorCode.JAVA_CODE_VERIFICATION__UNKNOWN_VARIABLE_NAME, 
					     e.getErrorCode());
		}
	}

	@Test
	public void throwsExceptionForWrongParamVariableType() throws ClassNotFoundException 
	{
		// arrange
		final HashMap<Filename, List<LanguageTemplatePattern>> languageTemplateCollection = 
				LanguageTemplateCollector.doYourJob(createJavaFieldData("ComplexInstructionTestApplication"));
		final HashMap<Filename, List<LanguageInstructionPattern>> languageInstructionCollection = 
				LanguageInstructionCollector.doYourJob("WrongParamVariableTypeTestApplication");
		
		// act
		try {
			PatternMergeJavaCommandGenerator.doYourJob(languageTemplateCollection, 
				                                       languageInstructionCollection,
			                                           "TestApp");
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("Error Code", 
					     ErrorCode.JAVA_CODE_VERIFICATION__WRONG_VARIABLE_TYPE, 
					     e.getErrorCode());
		}
	}
	
	private List<JavaFieldData> createJavaFieldData(final String testApp) {
		return LanguageTemplateContainerFinder.findLanguageTemplateContainers(testApp);
	}

}