package com.iksgmbh.sysnat.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.common.exception.SysNatException;

public class StageInstructionUtilClassLevelTest 
{
	private static String instructionFileName;
	
	@Before
	public void setup() {
		instructionFileName = StageInstructionUtil.KNOWN_STAGE_INSTRUCTIONS_FILE;
	}
	
	@After
	public void cleanup() {
		StageInstructionUtil.KNOWN_STAGE_INSTRUCTIONS_FILE = instructionFileName;
		StageInstructionUtil.knownStageInstructions = null;
	}
	
	@Test
	public void throwsExceptionForMissingHypen() 
	{
		// arrange
		StageInstructionUtil.KNOWN_STAGE_INSTRUCTIONS_FILE = "../sysnat.testcase.generation/src/test/resources/TestSageInstructions.config";
		StageInstructionUtil.knownStageInstructions = null;
		
		try {
			// act
			StageInstructionUtil.isStageInstruction("");
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", "Invalid instruction in ../sysnat.testcase.generation/src/test/resources/TestSageInstructions.config: Test-ID", 
					                      e.getMessage());
		}		
	}
	
	@Test
	public void parsesCurrentStageInstructions() 
	{
		// act
		boolean result1 = StageInstructionUtil.isStageInstruction("Active ");
		boolean result2 = StageInstructionUtil.isStageInstruction(":");
		boolean result3 = StageInstructionUtil.isStageInstruction(" Active: ");
		boolean result4 = StageInstructionUtil.isStageInstruction("Active:b");
		
		// assert
		assertFalse("Invalid StageInstruction expected.", result1);
		assertFalse("Invalid StageInstruction expected.", result2);
		assertTrue("Valid StageInstruction expected.", result3);
		assertTrue("Invalid StageInstruction expected.", result4);
	}
	
}
