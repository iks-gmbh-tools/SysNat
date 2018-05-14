package com.iksgmbh.sysnat.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.iksgmbh.sysnat.domain.NaturalLanguagePatternPart.NaturalLanguagePatternPartType;
import com.iksgmbh.sysnat.exception.SysNatException;
import com.iksgmbh.sysnat.exception.SysNatException.ErrorCode;

public class NaturalLanguageInstructionPatternTest 
{

	@Test
	public void createsSimpleNaturalLanguageInstruction() throws Exception 
	{
		// act
		final LanguageInstructionPattern instruction = new LanguageInstructionPattern("Simple Instruction", "xy.nltc");
		
		// assert
		assertEquals("Number of parts in instruction", 1, instruction.getNumberOfParts());
		assertEquals("pattern part type", NaturalLanguagePatternPartType.DEFAULT, instruction.getPart(0).type);
		assertEquals("instruction", "Simple Instruction", (String)instruction.getPart(0).value);
	}

	@Test
	public void createsNaturalLanguageInstructionWithOneStringParameter() throws Exception 
	{
		// act
		final LanguageInstructionPattern instruction = new LanguageInstructionPattern("Instruction with ^para'meter^.", "xy.nltc");
		
		// assert
		assertEquals("Number of parts in pattern", 3, instruction.getNumberOfParts());
		assertEquals("pattern part type", NaturalLanguagePatternPartType.DEFAULT, instruction.getPart(0).type);
		assertEquals("pattern part type", NaturalLanguagePatternPartType.PARAM_VALUE, instruction.getPart(1).type);
		assertEquals("pattern part type", NaturalLanguagePatternPartType.DEFAULT, instruction.getPart(2).type);
		assertEquals("pattern part", "Instruction with ", (String)instruction.getPart(0).value);
		assertEquals("parameter type", "para'meter", instruction.getPart(1).value);
		assertEquals("pattern part", ".", (String)instruction.getPart(2).value);
	}
	
	@Test
	public void createsNaturalLanguageInstructionWithThreeParameters() throws Exception 
	{
		// act
		final LanguageInstructionPattern instruction = new LanguageInstructionPattern("a^b^c^d^e^f^g", "xy.nltc");
		
		// assert
		assertEquals("Number of parts in pattern", 7, instruction.getNumberOfParts());
		assertEquals("pattern part type", NaturalLanguagePatternPartType.DEFAULT, instruction.getPart(0).type);
		assertEquals("pattern part type", NaturalLanguagePatternPartType.PARAM_VALUE, instruction.getPart(1).type);
		assertEquals("pattern part type", NaturalLanguagePatternPartType.DEFAULT, instruction.getPart(2).type);
		assertEquals("pattern part type", NaturalLanguagePatternPartType.PARAM_VALUE, instruction.getPart(3).type);
		assertEquals("pattern part type", NaturalLanguagePatternPartType.DEFAULT, instruction.getPart(4).type);
		assertEquals("pattern part type", NaturalLanguagePatternPartType.PARAM_VALUE, instruction.getPart(5).type);
		assertEquals("pattern part type", NaturalLanguagePatternPartType.DEFAULT, instruction.getPart(6).type);

		assertEquals("pattern part type", "a", instruction.getPart(0).value);
		assertEquals("pattern part type", "b", instruction.getPart(1).value);
		assertEquals("pattern part type", "c", instruction.getPart(2).value);
		assertEquals("pattern part type", "d", instruction.getPart(3).value);
		assertEquals("pattern part type", "e", instruction.getPart(4).value);
		assertEquals("pattern part type", "f", instruction.getPart(5).value);
		assertEquals("pattern part type", "g", instruction.getPart(6).value);
	}
	
	@Test
	public void createsNaturalLanguageInstructionWithTwoParameters() throws Exception 
	{
		// act
		final LanguageInstructionPattern instruction = new LanguageInstructionPattern("^p1^ = ^p2^", "xy.nltc");
		
		// assert
		assertEquals("Number of parts in pattern", 3, instruction.getNumberOfParts());
		assertEquals("pattern part type", NaturalLanguagePatternPartType.PARAM_VALUE, instruction.getPart(0).type);
		assertEquals("pattern part type", NaturalLanguagePatternPartType.DEFAULT, instruction.getPart(1).type);
		assertEquals("pattern part type", NaturalLanguagePatternPartType.PARAM_VALUE, instruction.getPart(2).type);
		assertEquals("pattern part", "p1", instruction.getPart(0).value);
		assertEquals("pattern part", " = ", instruction.getPart(1).value);
		assertEquals("pattern part", "p2", instruction.getPart(2).value);
	}
	
	@Test
	public void createsNaturalLanguagePatternWithReturnValue() throws Exception 
	{
		// act
		final LanguageInstructionPattern instruction = new LanguageInstructionPattern("Create <Contract^>.", "xy.nltc");
		
		// assert
		assertEquals("Number of parts in pattern", 3, instruction.getNumberOfParts());
		assertEquals("pattern part type", NaturalLanguagePatternPartType.DEFAULT, instruction.getPart(0).type);
		assertEquals("pattern part type", NaturalLanguagePatternPartType.RETURN_VALUE, instruction.getPart(1).type);
		assertEquals("pattern part type", NaturalLanguagePatternPartType.DEFAULT, instruction.getPart(2).type);
		assertEquals("pattern part", "Create ", instruction.getPart(0).value);
		assertEquals("pattern part", "Contract^", instruction.getPart(1).value);
		assertEquals("pattern part", ".", instruction.getPart(2).value);
	}

	@Test
	public void throwsErrorForMissingClosingValueParameterIdentifier() throws Exception 
	{
		try {
			// act
			new LanguageInstructionPattern("Create ^Contract.", "xy.nltc");
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", ErrorCode.NATURAL_LANGUAGE_PARSING__MISSING_CLOSING_PART_IDENTIFIER, e.getErrorCode());
		}
	}
	
	@Test
	public void throwsErrorForMissingClosingVariableParameterIdentifier() throws Exception 
	{
		try {
			// act
			new LanguageInstructionPattern("Create 'Contract.", "xy.nltc");
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", ErrorCode.NATURAL_LANGUAGE_PARSING__MISSING_CLOSING_PART_IDENTIFIER.name(), e.getErrorCode().name());
		}
	}

	@Test
	public void throwsErrorForMissingClosingReturnValueIdentifier() throws Exception 
	{
		try {
			// act
			new LanguageInstructionPattern("Create 'Contract.", "xy.nltc");
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", ErrorCode.NATURAL_LANGUAGE_PARSING__MISSING_CLOSING_PART_IDENTIFIER, e.getErrorCode());
		}
	}

	@Test
	public void throwsErrorForMissingDoubleReturnValueIdentifier() throws Exception 
	{
		try {
			// act
			new LanguageInstructionPattern("Create <Contract1> and <Contract2>.", "xy.nltc");
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", ErrorCode.NATURAL_LANGUAGE_INSTRUCTING_PARSING__DOUBLE_RETURN_VALUE_IDENTIFIER, e.getErrorCode());
		}
	}

	@Test
	public void throwsErrorForEmptyReturnValueIdentifier() throws Exception 
	{
		try {
			// act
			new LanguageInstructionPattern("Create <>.", "xy.nltc");
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", ErrorCode.NATURAL_LANGUAGE_INSTRUCTING_PARSING__EMPTY_RETURN_VALUE_IDENTIFIER, e.getErrorCode());
		}
	}

	@Test
	public void throwsErrorForEmptyVariableIdentifier() throws Exception 
	{
		try {
			// act
			new LanguageInstructionPattern("Create ''.", "xy.nltc");
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", ErrorCode.NATURAL_LANGUAGE_INSTRUCTING_PARSING__EMPTY_PARAMETER_IDENTIFIER, e.getErrorCode());
		}
	}
	
	@Test
	public void throwsErrorForEmptyParameterIdentifier() throws Exception 
	{
		try {
			// act
			new LanguageInstructionPattern("Create ^^.", "xy.nltc");
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", ErrorCode.NATURAL_LANGUAGE_INSTRUCTING_PARSING__EMPTY_PARAMETER_IDENTIFIER, e.getErrorCode());
		}
	}
}
