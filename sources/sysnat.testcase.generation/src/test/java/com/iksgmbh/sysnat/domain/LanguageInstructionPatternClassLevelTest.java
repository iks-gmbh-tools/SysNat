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
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.exception.SysNatException.ErrorCode;
import com.iksgmbh.sysnat.domain.NaturalLanguagePatternPart.NaturalLanguagePatternPartType;

public class LanguageInstructionPatternClassLevelTest 
{

	@Test
	public void createsSimpleNaturalLanguageInstruction() throws Exception 
	{
		// act
		final LanguageInstructionPattern instruction = new LanguageInstructionPattern("Simple Instruction", "xy.nlxx");
		
		// assert
		assertEquals("Number of parts in instruction", 1, instruction.getNumberOfParts());
		assertEquals("pattern part type", NaturalLanguagePatternPartType.DEFAULT, instruction.getPart(0).type);
		assertEquals("instruction", "Simple Instruction", (String)instruction.getPart(0).value);
	}

	@Test
	public void createsNaturalLanguageInstructionWithOneStringParameter() throws Exception 
	{
		// act
		final LanguageInstructionPattern instruction = new LanguageInstructionPattern("Instruction with ^para'meter^.", "xy.nlxx");
		
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
		final LanguageInstructionPattern instruction = new LanguageInstructionPattern("a^b^c^d^e^f^g", "xy.nlxx");
		
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
		final LanguageInstructionPattern instruction = new LanguageInstructionPattern("^p1^ = ^p2^", "xy.nlxx");
		
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
		final LanguageInstructionPattern instruction = new LanguageInstructionPattern("Create <Contract^>.", "xy.nlxx");
		
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
			new LanguageInstructionPattern("Create ^Contract.", "xy.nlxx");
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
			new LanguageInstructionPattern("Create 'Contract.", "xy.nlxx");
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
			new LanguageInstructionPattern("Create 'Contract.", "xy.nlxx");
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
			new LanguageInstructionPattern("Create <Contract1> and <Contract2>.", "xy.nlxx");
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
			new LanguageInstructionPattern("Create <>.", "xy.nlxx");
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", ErrorCode.NATURAL_LANGUAGE_INSTRUCTING_PARSING__EMPTY_RETURN_VALUE_IDENTIFIER, e.getErrorCode());
		}
	}

	@Test
	@Ignore
	public void throwsErrorForEmptyVariableIdentifier() throws Exception 
	{
		try {
			// act
			new LanguageInstructionPattern("Create ''.", "xy.nlxx");
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", ErrorCode.NATURAL_LANGUAGE_INSTRUCTING_PARSING__EMPTY_PARAMETER_IDENTIFIER, e.getErrorCode());
		}
	}
	
	@Test
	@Ignore
	public void throwsErrorForEmptyParameterIdentifier() throws Exception 
	{
		try {
			// act
			new LanguageInstructionPattern("Create ^^.", "xy.nlxx");
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", ErrorCode.NATURAL_LANGUAGE_INSTRUCTING_PARSING__EMPTY_PARAMETER_IDENTIFIER, e.getErrorCode());
		}
	}
	
	@Test
	public void returnsListOfParamVariableNames() throws Exception 
	{
		// act
		final LanguageInstructionPattern instruction = new LanguageInstructionPattern("Add 'A', 'BB', and 'CCC'.", "xy.nlxx");
		
		// assert
		final List<String> result = instruction.getParamVariableNames();
		assertEquals("Number of variable names", 3, result.size());
		assertEquals("Variable name", "A", result.get(0));
		assertEquals("Variable name", "BB", result.get(1));
		assertEquals("Variable name", "CCC", result.get(2));
	}

	@Test
	public void parsesInstructionLineWithStageInstruction() throws Exception 
	{
		// act
		final LanguageInstructionPattern instruction = new LanguageInstructionPattern("Active: true", "xy.nlxx");
				
		// assert
		assertEquals("Number of parts in pattern", 2, instruction.getNumberOfParts());
		assertEquals("Part value", "Active:", instruction.getPart(0).value);
		assertEquals("Part value", "true", instruction.getPart(1).value);
		assertEquals("Part type", "DEFAULT", instruction.getPart(0).type.name());
		assertEquals("Part type", "PARAM_VALUE", instruction.getPart(1).type.name());
	}

}