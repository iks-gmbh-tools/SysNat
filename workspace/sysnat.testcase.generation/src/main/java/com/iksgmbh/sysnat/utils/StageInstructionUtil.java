package com.iksgmbh.sysnat.utils;

import java.util.List;
import java.util.stream.Collectors;

import com.iksgmbh.sysnat.common.exception.SysNatConfigurationException;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

public class StageInstructionUtil 
{
	public static String KNOWN_STAGE_INSTRUCTIONS_FILE = "../sysnat.testcase.generation/src/main/resources/StageInstructions.config";

	protected static List<String> knownStageInstructions;
	
	public static boolean isStageInstruction(final String naturalLanguageLine) 
	{
		if (knownStageInstructions == null) {
			knownStageInstructions = loadKnownStageInstructions();
		}
		
		for (String stageInstruction : knownStageInstructions) {
			if (naturalLanguageLine.trim().startsWith(stageInstruction.trim())) {
				return true;
			}
		}
		
		return false;
	}

	private static List<String> loadKnownStageInstructions() 
	{
		 final List<String> content = SysNatFileUtil.readTextFile(KNOWN_STAGE_INSTRUCTIONS_FILE);
		 return content.stream().filter(StageInstructionUtil::isValidStageInstruction).collect(Collectors.toList());
	}
	
	private static boolean isValidStageInstruction(final String line) 
	{
		if (line.trim().isEmpty()) return false;
		if (line.trim().startsWith("#")) return false;
		if (line.trim().endsWith(":")) return true;
		throw new SysNatConfigurationException("Invalid instruction in " 
		                                       + KNOWN_STAGE_INSTRUCTIONS_FILE 
		                                       + ": " + line);
	}
}
