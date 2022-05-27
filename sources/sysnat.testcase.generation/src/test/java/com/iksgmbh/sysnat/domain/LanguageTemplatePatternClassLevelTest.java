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
package com.iksgmbh.sysnat.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import org.junit.Test;

import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.exception.SysNatException.ErrorCode;
import com.iksgmbh.sysnat.domain.NaturalLanguagePatternPart.NaturalLanguagePatternPartType;

public class LanguageTemplatePatternClassLevelTest 
{

	@Test
	public void createsSimpleNaturalLanguagePattern() throws Exception 
	{
		// arrange 
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("methodWithoutParameter");
		
		// act
		final LanguageTemplatePattern languagePattern = LanguageTemplatePattern.createFrom(method, new Filename("test"), "").get(2);
		
		// assert
		assertEquals("Number of parts in pattern", 1, languagePattern.getNumberOfParts());
		assertEquals("pattern part type", NaturalLanguagePatternPartType.DEFAULT, languagePattern.getPart(0).type);
		assertEquals("pattern", "Natural language instruction without parameter. - Second Duplicate", (String)languagePattern.getPart(0).value);
	}

	
	@Test
	public void createsNaturalLanguagePatternWithOneStringPARAM_VALUE() throws Exception 
	{
		// arrange 
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("methodWithOneParameter", String.class);
		
		// act
		final LanguageTemplatePattern languagePattern = new LanguageTemplatePattern(method, new Filename("test"), "");
		
		// assert
		assertEquals("Number of parts in pattern", 3, languagePattern.getNumberOfParts());
		assertEquals("pattern part type", NaturalLanguagePatternPartType.DEFAULT, languagePattern.getPart(0).type);
		assertEquals("pattern part type", NaturalLanguagePatternPartType.PARAM_VALUE, languagePattern.getPart(1).type);
		assertEquals("pattern part type", NaturalLanguagePatternPartType.DEFAULT, languagePattern.getPart(2).type);
		assertEquals("pattern part", " Natural language instruction with ", (String)languagePattern.getPart(0).value);
		assertEquals("PARAM_VALUE type", String.class, languagePattern.getPart(1).value);
		assertEquals("pattern part", " parameter. ", (String)languagePattern.getPart(2).value);
	}

	@Test
	public void createsNaturalLanguagePatternWithOneIntPARAM_VALUE() throws Exception 
	{
		// arrange 
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("anotherMethodWithOneParameter", int.class);
		
		// act
		final LanguageTemplatePattern languagePattern = new LanguageTemplatePattern(method, new Filename("test"), "" );
		
		// assert
		assertEquals("Number of parts in pattern", 3, languagePattern.getNumberOfParts());
		assertEquals("PARAM_VALUE type", int.class, languagePattern.getPart(1).value);
	}


	@Test
	public void createsNaturalLanguagePatternWithFourParameters() throws Exception 
	{
		// arrange 
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("methodWithFourParameters", String.class, String.class, String.class, String.class);
		
		// act
		final LanguageTemplatePattern languagePattern = new LanguageTemplatePattern(method, new Filename("test"), "" );
		
		// assert
		assertEquals("Number of parts in pattern", 9, languagePattern.getNumberOfParts());
		assertEquals("pattern part type", NaturalLanguagePatternPartType.PARAM_VALUE, languagePattern.getPart(1).type);
		assertEquals("pattern part type", NaturalLanguagePatternPartType.PARAM_VALUE, languagePattern.getPart(3).type);
		assertEquals("pattern part type", NaturalLanguagePatternPartType.PARAM_VALUE, languagePattern.getPart(5).type);
		assertEquals("pattern part type", NaturalLanguagePatternPartType.PARAM_VALUE, languagePattern.getPart(7).type);
	}

	@Test
	public void createsNaturalLanguagePatternWithTwoParameters() throws Exception 
	{
		// arrange 
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("methodWithTwoParameters", String.class, String.class);
		
		// act
		final LanguageTemplatePattern languagePattern = new LanguageTemplatePattern(method, new Filename("test"), "" );
		
		// assert
		assertEquals("Number of parts in pattern", 3, languagePattern.getNumberOfParts());
		assertEquals("pattern part type", NaturalLanguagePatternPartType.PARAM_VALUE, languagePattern.getPart(0).type);
		assertEquals("pattern part type", NaturalLanguagePatternPartType.DEFAULT, languagePattern.getPart(1).type);
		assertEquals("pattern part type", NaturalLanguagePatternPartType.PARAM_VALUE, languagePattern.getPart(2).type);
		assertEquals("pattern part", " = ", languagePattern.getPart(1).value);
	}

	@Test
	public void createsNaturalLanguagePatternWithReturnValue() throws Exception 
	{
		// arrange 
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("methodWithReturnValue", String.class);
		
		// act
		final LanguageTemplatePattern languagePattern = new LanguageTemplatePattern(method, new Filename("test"), "" );
		
		// assert
		assertEquals("Number of parts in pattern", 5, languagePattern.getNumberOfParts());
		assertEquals("pattern part type", NaturalLanguagePatternPartType.DEFAULT, languagePattern.getPart(0).type);
		assertEquals("pattern part type", NaturalLanguagePatternPartType.RETURN_VALUE, languagePattern.getPart(1).type);
		assertEquals("pattern part type", NaturalLanguagePatternPartType.DEFAULT, languagePattern.getPart(2).type);
		assertEquals("pattern part type", NaturalLanguagePatternPartType.PARAM_VALUE, languagePattern.getPart(3).type);
		assertEquals("pattern part type", NaturalLanguagePatternPartType.DEFAULT, languagePattern.getPart(4).type);
		assertEquals("return type", Integer.class, languagePattern.getPart(1).value);
	}
	
	@Test
	public void throwsErrorForMissingReturnTypeOfJavaMethod() throws Exception 
	{		
		// arrange 
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.badapplication.LanguageTemplateContainerFailingTestImpl");
		final Method method = c.getMethod("methodWithoutReturnValue", String.class);
		
		try {
			// act
			new LanguageTemplatePattern(method, new Filename("test"), "" );
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", ErrorCode.LANGUAGE_TEMPLATE_PARSING__MISSING_JAVA_RETURN_VALUE.name(), e.getErrorCode().name());
		}
	}
		
	@Test
	public void throwsErrorForMissingReturnTypeInNaturalLanguagePattern() throws Exception 
	{		
		// arrange 
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.badapplication.LanguageTemplateContainerFailingTestImpl");
		final Method method = c.getMethod("methodWithReturnValue", String.class);
		
		try {
			// act
			new LanguageTemplatePattern(method, new Filename("test"), "" );
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", ErrorCode.LANGUAGE_TEMPLATE_PARSING__MISSING_RETURN_VALUE_IN_PATTERN, e.getErrorCode());
		}
	}
	
	@Test
	public void throwsErrorForDoubleReturnTypeInNaturalLanguagePattern() throws Exception 
	{		
		// arrange 
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.badapplication.LanguageTemplateContainerFailingTestImpl");
		final Method method = c.getMethod("methodAnnotatedWithTwoReturnValues");
		
		try {
			// act
			new LanguageTemplatePattern(method, new Filename("test"), "" );
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", ErrorCode.LANGUAGE_TEMPLATE_PARSING__DOUBLE_RETURN_VALUE_IN_PATTERN.name(), e.getErrorCode().name());
		}
	}
	
	@Test
	public void throwsErrorForParameterMismatch() throws Exception 
	{		
		// arrange 
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.badapplication.LanguageTemplateContainerFailingTestImpl");
		final Method method = c.getMethod("methodAnnotatedWithTwoParameters", String.class);
		
		try {
			// act
			new LanguageTemplatePattern(method, new Filename("test"), "" );
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", ErrorCode.LANGUAGE_TEMPLATE_PARSING__NUMBER_PARAMETER_MISMATCH, e.getErrorCode());
		}
	}	
	
	@Test
	public void comparesDifferentPatterns() throws Exception 
	{
		// arrange 
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication.LanguageTemplateContainerTestImpl");
		final Method method1 = c.getMethod("methodWithReturnValue", String.class);
		final LanguageTemplatePattern languagePattern1 = new LanguageTemplatePattern(method1, new Filename("test1"), "" );
		final Method method2 = c.getMethod("anotherMethodWithOneParameter", int.class);
		final LanguageTemplatePattern languagePattern2 = new LanguageTemplatePattern(method2, new Filename("test2"), "" );
		
		// act
		boolean isIdentical = languagePattern1.isIdentical(languagePattern2);
		
		// assert
		assertFalse(isIdentical);
	}

	@Test
	public void comparesIdenticalPatterns() throws Exception 
	{
		// arrange 
		final Class<?> c1 = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication2.LanguageTemplateContainerTestImpl");
		final Method method1 = c1.getMethod("methodWithFourParameters", String.class, String.class, String.class, String.class);
		final LanguageTemplatePattern languagePattern1 = new LanguageTemplatePattern(method1, new Filename("test1"), "" );
		final Class<?> c2 = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication2.LanguageTemplateContainer2TestImpl");
		final Method method2 = c2.getMethod("anotherMethodWithFourParameters", String.class, String.class, String.class, String.class);
		final LanguageTemplatePattern languagePattern2 = new LanguageTemplatePattern(method2, new Filename("test1"), "" );
		
		// act
		boolean isIdentical = languagePattern1.isIdentical(languagePattern2);
		
		// assert
		assertTrue(isIdentical);
	}

	@Test
	public void parsesStageInstructionPattern() throws Exception 
	{
		// arrange 
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("methodWithStageInstruction", String.class);
		
		// act
		final LanguageTemplatePattern languagePattern = new LanguageTemplatePattern(method, new Filename("test1"), "" );
		
		// assert
		assertEquals("Number of pattern parts", 2, languagePattern.getNumberOfParts());
		assertEquals("Part value", "Active:", languagePattern.getPart(0).value);
		assertEquals("Part value", String.class, languagePattern.getPart(1).value);
		assertEquals("Part type", "DEFAULT", languagePattern.getPart(0).type.name());
		assertEquals("Part type", "PARAM_VALUE", languagePattern.getPart(1).type.name());
	}
	
}