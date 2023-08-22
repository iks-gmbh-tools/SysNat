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
package com.iksgmbh.sysnat.utils;

import java.util.List;
import java.util.stream.Collectors;

import com.iksgmbh.sysnat.common.exception.SysNatConfigurationException;
import com.iksgmbh.sysnat.common.exception.SysNatException;
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
		final String filename = SysNatFileUtil.findAbsoluteFilePath(KNOWN_STAGE_INSTRUCTIONS_FILE);
		 final List<String> content = SysNatFileUtil.readTextFile(filename);
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

	public static String getContent(String s)
	{
		if (! isStageInstruction(s)) {
			throw new SysNatException("No stage instruction: " + s);
		}
		int pos = s.indexOf(":") + 1;
		return s.substring(pos).trim();
	}
}