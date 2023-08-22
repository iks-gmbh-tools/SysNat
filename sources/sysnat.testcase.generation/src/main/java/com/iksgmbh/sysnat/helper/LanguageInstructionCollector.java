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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.GenerationRuntimeInfo;
import com.iksgmbh.sysnat.common.exception.SysNatException.ErrorCode;
import com.iksgmbh.sysnat.common.utils.ExceptionHandlingUtil;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants;
import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.LanguageInstructionPattern;
import com.iksgmbh.sysnat.domain.TestApplication;
import com.iksgmbh.sysnat.utils.BDDKeywordUtil;
import com.iksgmbh.sysnat.utils.StageInstructionUtil;

/**
 * Collects all natural language instructions from all nlxx-files of the given test application.
 * 
 * @author Reik Oberrath
 */
public class LanguageInstructionCollector 
{
	private static final String SET_BDD_KEYWORD = "Set BDD-Keyword";
	private static final String COMMENT_SEPARATOR = "#";

	private String applicationName;

	public LanguageInstructionCollector(final String aApplicationName) {
		this.applicationName = aApplicationName;
	}

	/**
	 * Searches for all natural language test case files belonging to the given application,
	 * extracts their instruction lines and
	 * parses each line into a LanguageInstructionPattern.
	 * 
	 * @param applicationName
	 * @return List of LanguageInstructionPatterns for each nlxx-file found
	 */
	public static HashMap<Filename, List<LanguageInstructionPattern>> doYourJob(final String applicationName) 
	{
		return new LanguageInstructionCollector(applicationName).findAllNaturalLanguageInstruction();
	}

	/**
	 * Search first for scripts (nls), second for executable examples (nlxx)
	 * @return instruction files and their parsed content
     */
	protected HashMap<Filename, List<LanguageInstructionPattern>> findAllNaturalLanguageInstruction() 
	{
		final HashMap<Filename, List<LanguageInstructionPattern>> toReturn = new HashMap<>();
		final HashMap<Filename, List<String>> instructionLines = new HashMap<>();

		// step A: read instructionslines for each executable example file and each script file
		final List<File> nlsFiles = findInstructionFiles(".nls");
		GenerationRuntimeInfo.getInstance().setListOfKnownScriptNames(
				nlsFiles.stream().map(file -> file.getName()).collect(Collectors.toList())
		);
		final List<File> nlxxFiles = findInstructionFiles(".nlxx");
		nlsFiles.stream().forEach(file -> instructionLines.put(new Filename(file.getAbsolutePath()),  getInstructionLines(file)));
		nlxxFiles.stream().forEach(file -> instructionLines.put(new Filename(file.getAbsolutePath()),  getInstructionLines(file)));

		// step B: parse instructionslines to NaturalLanguageInstructionPatterns
		Set<Filename> keySet = instructionLines.keySet();
		for (Filename fileName : keySet) 
		{
			List<LanguageInstructionPattern> naturalLanguageInstructionPatterns = 
					instructionLines.get(fileName).stream()
                                    .map(line -> new LanguageInstructionPattern(line, fileName.value))
                                    .collect(Collectors.toList());
			toReturn.put(fileName, naturalLanguageInstructionPatterns);
		}
		
		return toReturn;
	}

	public static String getTestCaseDir() {
		return System.getProperty("sysnat.executable.examples.source.dir");
	}

	protected List<File> findInstructionFiles(final String fileExtension) 
	{
		TestApplication testApplication = ExecutionRuntimeInfo.getInstance().getTestApplication();
		final String mainDir = SysNatFileUtil.findAbsoluteFilePath(getTestCaseDir());
		List<File> directories = new ArrayList<>();
		directories.add(new File(mainDir, applicationName));
		
		if (fileExtension.equals(".nls")) 
		{
			// ignore nlxx files from element applications
			if (testApplication.isCompositeApplication()) {
				testApplication.getElementAppications().forEach(subdir -> directories.add(new File(mainDir, subdir)));
			}
		}
		
		return SysNatFileUtil.findUniqueFilesRecursively(fileExtension, directories);
	}

	List<String> getInstructionLines(File file)
	{
		if (file.getName().contains("SAP")) {
			// System.out.println("");
		}
		List<String> toReturn = new ArrayList<>();
		List<String> content = SysNatFileUtil.readTextFile(file);
		if (content.isEmpty()) {
			return toReturn;
		}
		String firstLine= content.get(0);
		final boolean isFeatureBased = firstLine.startsWith("Feature") || 
				                       firstLine.startsWith("Szenario") || 
				                       firstLine.startsWith("Scenario");

		boolean behaviourHeaderDetected = false;
		boolean lineInXXParsingSection = false;
		
		for (String line : content)
		{
			line = removeComment(line);
			if ( line.isEmpty() || line.startsWith(COMMENT_SEPARATOR)) { 
				continue;
			}
					
			if (isBehaviourLine(line))
			{
				behaviourHeaderDetected = true;
				extractBddInstruction(line.trim(), toReturn);
				continue;
			}

			if (isXXLine(line)) 
			{
				lineInXXParsingSection = true;
				extractBddInstruction(line.trim(), toReturn);
				continue;
			}

			if (behaviourHeaderDetected && ! lineInXXParsingSection) 
			{
				if (StageInstructionUtil.isStageInstruction(line)) {
					toReturn.add(line);
				}
			} 
			else 
			{
				if (line.isEmpty()) {
					continue;
				}
				
				if ( isFeatureBased ) {
					extractBddInstruction(line.trim(), toReturn);
				} else {
					toReturn.add(line);
				}
			}
		}

		toReturn = removeBddKeywordMethodCallsIfPossible(toReturn);
		toReturn = checkForSimpleScriptCallSyntax(toReturn);
		toReturn = checkForSingleTestDataValueSetter(toReturn, file.getName());

		return toReturn;
	}

	private boolean isXXLine(String line)
	{
		return line.startsWith("Scenario:")
			|| line.startsWith("Szenario:")
			|| line.startsWith("XX:")
			|| line.toLowerCase().startsWith("xxid:");
	}

	private boolean isBehaviourLine(String line)
	{
		return line.startsWith("Feature")
			   || line.startsWith("Behavior")
			   || line.startsWith("Behaviour")
			   || line.startsWith("Verhalten");
	}

	private List<String> checkForSingleTestDataValueSetter(List<String> instructions, String filename) 
	{
		final List<String> toReturn = new ArrayList<>();
		for (String line : instructions)
		{
			if ( line.endsWith("=\"\"") ||
				 (! line.contains("\"") &&
				  ! line.contains("<") && 
				  ! line.contains(">") && 
				  ! line.contains(":") && 
				  ! line.contains("'"))  )
			{
				if (line.trim().endsWith("=")) {
					ExceptionHandlingUtil.throwClassifiedException(ErrorCode.NATURAL_LANGUAGE_INSTRUCTING_PARSING__EMPTY_PARAMETER_IDENTIFIER, 
                            line, filename, "help");
				}
				String[] splitResult = line.split("=");
				if (splitResult.length == 2) 
				{
					String key = splitResult[0].trim();
					String value = splitResult[1].trim();
					if (value.equals("\"\"")) {
						value = "";
					}
					line = "\"" + key + "\" = \"" + value + "\"";
				}
			}
			toReturn.add(line);
		}

		return toReturn;
	}

	private String removeComment(String line) 
	{
		int pos = line.indexOf(COMMENT_SEPARATOR);
		if (pos > 0) line = line.substring(0, pos);
		return line.trim();
	}

	/**
	 * Scripts are called using the "^^." language template, e.g. "Create an order".
	 * This convenience functionality is implemented here.
	 * For convenience reasons, the following does the same: Create an order.
     */
	private List<String> checkForSimpleScriptCallSyntax(List<String> instructions)
	{
		final List<String> toReturn = new ArrayList<>();
		for (String line : instructions)
		{
			String instruction = line;
			if (StageInstructionUtil.isStageInstruction(line)) {
				instruction = StageInstructionUtil.getContent(line);
			}
			if (isScriptCall(instruction)) 
			{
				instruction = "\"" + instruction.substring(0, instruction.length()-1) + "\"."; 
				if (StageInstructionUtil.isStageInstruction(line)) {
					String toReplace = StageInstructionUtil.getContent(line);
					instruction = line.replace(toReplace, instruction);
				}			
				toReturn.add(instruction);
			} else {
				toReturn.add(line);
			}
		}

		return toReturn;
	}

	private boolean isScriptCall(String instruction)
	{
		if (doesContainPlaceholder(instruction)) {
			return false;
		}

		return isKnownScript(instruction);
	}

	private boolean isKnownScript(String instruction)
	{
		if (! instruction.trim().endsWith(".")) {
			instruction += ".";
		}
		return GenerationRuntimeInfo.getInstance().getListOfKnownScriptNames().contains(instruction + "nls");
	}

	private boolean doesContainPlaceholder(String instruction) 
	{
		return instruction.contains("<")
			   || instruction.contains(">")
			   || instruction.contains("\"")
			   || instruction.contains("'");
	}

	/**
	 * If no BDD-Keywords are used, the SET_BDD_KEYWORD method call are needless
	 * and removed here.
     */
	private List<String> removeBddKeywordMethodCallsIfPossible(List<String> instructions)
	{
		boolean areBDDKeyWordsInUse = instructions.stream()
				                                  .filter(line -> line.startsWith(SET_BDD_KEYWORD))
				                                  .findAny()
				                                  .isPresent();
		if (areBDDKeyWordsInUse) {
			return instructions;
		}

		return instructions.stream()
				           .filter(line -> ! line.startsWith(SET_BDD_KEYWORD))
					       .collect(Collectors.toList());
	}

	void extractBddInstruction(String line, List<String> instructions)
	{
		if (BDDKeywordUtil.startsWithBDDKeyword(line))
		{
			LineData lineData = getLineData(line);
			translateInSysNatCommands(lineData);
			instructions.add(SET_BDD_KEYWORD + " \"" + lineData.bddKeyword + "\".");
			instructions.add(lineData.instruction.trim());
		} else {
			instructions.add(line.trim());
		}
	}

	private void translateInSysNatCommands(LineData lineData) 
	{
		if (lineData.bddKeyword.equals("Feature")) {
			lineData.instruction = "Behaviour: " + lineData.instruction;
		} else if (lineData.bddKeyword.equals(SysNatLocaleConstants.SCENARIO_KEYWORD)
				|| lineData.bddKeyword.equals("Szenario")
				|| lineData.bddKeyword.equals("Scenario")) {
			lineData.instruction = "XXID: " + lineData.instruction;
		} else if (lineData.bddKeyword.equals("EinmalHintergrund")) {
			lineData.instruction = "EinmalVoraussetzung: " + lineData.instruction;
		} else if (lineData.bddKeyword.equals("OneTimeBackground")) {
			lineData.instruction = "OneTimePrecondition: " + lineData.instruction;
		} else if (lineData.bddKeyword.equals("Hintergrund")) {
			lineData.instruction = "Voraussetzung: " + lineData.instruction;
		} else if (lineData.bddKeyword.equals("Background")) {
			lineData.instruction = "Precondition: " + lineData.instruction;
		}
	}

	private LineData getLineData(String line)
	{
		int pos = line.indexOf(' ');
		String bddKeyword = line.substring(0, pos).trim();

		if (bddKeyword.endsWith(":")) {
			bddKeyword = bddKeyword.substring(0, bddKeyword.length()-1);
		}
		return new LineData(line.substring(pos+1), bddKeyword);
	}

	class LineData {
		String instruction;
		String bddKeyword;

		LineData(String anInstruction, String aKeyword) {
			instruction = anInstruction;
			bddKeyword = aKeyword;
		}

		@Override
		public String toString() {
			return "LineData{" +
					"instruction='" + instruction + '\'' +
					", bddKeyword='" + bddKeyword + '\'' +
					'}';
		}
	}
}