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

import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.JavaCommand;
import com.iksgmbh.sysnat.domain.LanguageInstructionPattern;
import com.iksgmbh.sysnat.domain.LanguageTemplatePattern;
import com.iksgmbh.sysnat.exception.SysNatException;
import com.iksgmbh.sysnat.exception.SysNatException.ErrorCode;

public class PatternMergeJavaCommandGeneratorTest 
{
	private PatternMergeJavaCommandGenerator cut = new PatternMergeJavaCommandGenerator(null, null);
	
	@Before
	public void setup() {
		GenerationRuntimeInfo.getInstance();
		System.setProperty("sysnat.testcase.source.dir", "./src/test/resources/testTestCases");
	}
	
	@Test
	public void buildsJavaFileName() throws Exception 
	{
		// act
		String result = cut.buildJavaFileName(new Filename("./src/test/resources/testTestCases/SubFolderTestApplication/Login/Test1.nltc")).value;
		
		// assert
		assertEquals("Name of java file", "subfoldertestapplication/login/Test1.java", result);
	}
	
	@Test
	public void findsNoMatchForSimplePattern() throws Exception 
	{
		// arrange
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.templateContainer.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("methodWithoutParameter");
		final LanguageTemplatePattern templatePattern = new LanguageTemplatePattern(method, new Filename("test"), "");
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
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.templateContainer.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("methodWithoutParameter");
		final LanguageTemplatePattern templatePattern = new LanguageTemplatePattern(method, new Filename("test"), "");
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
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.templateContainer.LanguageTemplateContainerTestImpl");
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
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.templateContainer.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("anotherMethodWithOneParameter", int.class);
		final LanguageTemplatePattern templatePattern = new LanguageTemplatePattern(method, new Filename("test"), "");
		final LanguageInstructionPattern instructionPattern = new LanguageInstructionPattern("Natural language instruction with \"1\" parameter.", "");
		
		// act
		boolean isMatching = cut.isMatching(templatePattern, instructionPattern);
		
		// assert
		assertTrue(isMatching);
	}

	@Test
	public void throwsExceptionForParameterTypeMisMatch() throws Exception 
	{
		// arrange
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.templateContainer.LanguageTemplateContainerTestImpl");
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
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.templateContainer.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("methodWithReturnValue", String.class);
		final LanguageTemplatePattern templatePattern = new LanguageTemplatePattern(method, new Filename("test"), "");
		final LanguageInstructionPattern instructionPattern = new LanguageInstructionPattern("Create 'r' with ^p^.", "");
		
		// act
		boolean isMatching = cut.isMatching(templatePattern, instructionPattern);
		
		// assert
		assertTrue(isMatching);
	}

	@Test
	public void findsPatternMatchOnlyWithReturnValue() throws Exception 
	{
		// arrange
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.templateContainer.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("methodOnlyWithReturnValue");
		final LanguageTemplatePattern templatePattern = new LanguageTemplatePattern(method, new Filename("test"), "");
		final LanguageInstructionPattern instructionPattern = new LanguageInstructionPattern("Create 'r'.", "");
		
		// act
		boolean isMatching = cut.isMatching(templatePattern, instructionPattern);
		
		// assert
		assertTrue(isMatching);
	}

	@Test
	public void findsPatternMatchWithFourParameter() throws Exception 
	{
		// arrange
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.templateContainer.LanguageTemplateContainerTestImpl");
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
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.templateContainer.LanguageTemplateContainerTestImpl");
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
		final Class<?> testCaseJavaTemplate = Class.forName("com.iksgmbh.sysnat.test.testcase_templates.ComplexInstructionTestApplication_TestCaseTemplate");
		final HashMap<Filename, List<LanguageTemplatePattern>> languageTemplateCollection = 
				LanguageTemplateCollector.doYourJob(testCaseJavaTemplate);
		final HashMap<Filename, List<LanguageInstructionPattern>> languageInstructionCollection = 
				LanguageInstructionCollector.doYourJob("ComplexInstructionTestApplication");
		
		// act
		final HashMap<Filename, List<JavaCommand>> result = PatternMergeJavaCommandGenerator.doYourJob(languageTemplateCollection, languageInstructionCollection);
		
		// assert
		assertEquals("Instruction file number", 1, result.size());
		
		final Filename key = result.keySet().iterator().next();
		assertEquals("Filename", "complexinstructiontestapplication/ComplexInstructionSequenz.java", key.value);
		
		final List<JavaCommand> javaCommands = result.get(key);
		assertEquals("number of commands", 5, javaCommands.size());
		
		assertEquals("java command", 
				     "Order auftrag_1 = languageTemplateContainerComplexInstructionTestApplication.createOrder();", 
				     javaCommands.get(0).value);
		assertEquals("java command", 
			         "Order auftrag_2 = languageTemplateContainerComplexInstructionTestApplication.createOrder();", 
			          javaCommands.get(1).value);
		assertEquals("java command", 
				     "languageTemplateContainerComplexInstructionTestApplication.addToOrder(\"Auftrag 1\", \"Wert A\");", 
				      javaCommands.get(2).value);
		assertEquals("java command", 
			         "languageTemplateContainerComplexInstructionTestApplication.addToOrder(\"Auftrag 2\", \"Wert B\");", 
			         javaCommands.get(3).value);
		assertEquals("java command", 
				     "Order auftrag_3 = languageTemplateContainerComplexInstructionTestApplication.toOrder(auftrag_1, auftrag_2);", 
				     javaCommands.get(4).value);
		
	}
	
}
