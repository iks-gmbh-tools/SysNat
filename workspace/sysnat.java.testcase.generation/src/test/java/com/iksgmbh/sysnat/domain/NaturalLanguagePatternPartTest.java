package com.iksgmbh.sysnat.domain;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.iksgmbh.sysnat.domain.NaturalLanguagePatternPart.NaturalLanguagePatternPartType;

public class NaturalLanguagePatternPartTest {

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
