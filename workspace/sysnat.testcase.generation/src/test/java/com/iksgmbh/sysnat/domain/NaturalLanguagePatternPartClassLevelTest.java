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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.iksgmbh.sysnat.domain.NaturalLanguagePatternPart.NaturalLanguagePatternPartType;

public class NaturalLanguagePatternPartClassLevelTest {

	@Test
	public void comparesIdenticalDefaultParts() 
	{
		// arrange
		NaturalLanguagePatternPart p1 = new NaturalLanguagePatternPart(NaturalLanguagePatternPartType.DEFAULT, "test");
		NaturalLanguagePatternPart p2 = new NaturalLanguagePatternPart(NaturalLanguagePatternPartType.DEFAULT, "test");
		
		// act
		boolean isIdentical = p1.isIdentical(p2);
		
		// assert
		assertTrue(isIdentical);
	}

	@Test
	public void comparesIdenticalParameterValueParts() 
	{
		// arrange
		NaturalLanguagePatternPart p1 = new NaturalLanguagePatternPart(NaturalLanguagePatternPartType.PARAM_VALUE, "test");
		NaturalLanguagePatternPart p2 = new NaturalLanguagePatternPart(NaturalLanguagePatternPartType.PARAM_VALUE, "test");
		
		// act
		boolean isIdentical = p1.isIdentical(p2);
		
		// assert
		assertTrue(isIdentical);
	}

	@Test
	public void comparesIdenticalParameterVariableParts() 
	{
		// arrange
		NaturalLanguagePatternPart p1 = new NaturalLanguagePatternPart(NaturalLanguagePatternPartType.PARAM_VARIABLE, "test");
		NaturalLanguagePatternPart p2 = new NaturalLanguagePatternPart(NaturalLanguagePatternPartType.PARAM_VARIABLE, "test");
		
		// act
		boolean isIdentical = p1.isIdentical(p2);
		
		// assert
		assertTrue(isIdentical);
	}
	
	@Test
	public void comparesDifferentReturnValueParts() 
	{
		// arrange
		NaturalLanguagePatternPart p1 = new NaturalLanguagePatternPart(NaturalLanguagePatternPartType.RETURN_VALUE, String.class);
		NaturalLanguagePatternPart p2 = new NaturalLanguagePatternPart(NaturalLanguagePatternPartType.RETURN_VALUE, int.class);
		
		// act
		boolean isIdentical = p1.isIdentical(p2);
		
		// assert
		assertFalse(isIdentical);
	}

	@Test
	public void comparesDifferentPartTypes() 
	{
		// arrange
		NaturalLanguagePatternPart p1 = new NaturalLanguagePatternPart(NaturalLanguagePatternPartType.RETURN_VALUE, "test");
		NaturalLanguagePatternPart p2 = new NaturalLanguagePatternPart(NaturalLanguagePatternPartType.PARAM_VARIABLE, "test");
		
		// act
		boolean isIdentical = p1.isIdentical(p2);
		
		// assert
		assertFalse(isIdentical);
	}
}