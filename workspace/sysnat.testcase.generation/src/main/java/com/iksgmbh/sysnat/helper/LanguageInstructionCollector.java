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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.iksgmbh.sysnat.GenerationRuntimeInfo;
import com.iksgmbh.sysnat.common.helper.FileFinder;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants;
import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.LanguageInstructionPattern;
import com.iksgmbh.sysnat.utils.BDDKeywordUtil;

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
		final File testCaseDir = new File(getTestCaseDir(), applicationName);
		final FilenameFilter fileFilter = new FilenameFilter() {
			@Override public boolean accept(File dir, String name) {
				return name.endsWith(fileExtension);
			}
		};
		
		return FileFinder.searchFilesRecursively(testCaseDir, fileFilter);
	}

	List<String> getInstructionLines(File file)
	{
		final List<String> content = SysNatFileUtil.readTextFile(file);
		List<String> toReturn = new ArrayList<>();
		String  firstLine= content.get(0);
		final boolean isFeatureBased = firstLine.startsWith("Feature") || 
				                       firstLine.startsWith("Szenario") || 
				                       firstLine.startsWith("Scenario");

		boolean behaviourHeaderDetected = false;
		boolean firstXXDetected = false;
		
		for (String line : content)
		{
			line = removeComment(line);
			if ( line.isEmpty() || line.startsWith(COMMENT_SEPARATOR)) { 
				continue;
			}
					
			if (   line.startsWith("Feature")
				|| line.startsWith("Behavior")
				|| line.startsWith("Behaviour")
				|| line.startsWith("Verhalten"))
			{
				behaviourHeaderDetected = true;
				extractInstruction(line.trim(), toReturn);
				continue;
			}

			if (   line.startsWith("Scenario")
				|| line.startsWith("Szenario")
				|| line.startsWith("XX")
				|| line.startsWith("XXID")
				|| line.startsWith("XXId")) 
			{
				firstXXDetected = true;
				extractInstruction(line.trim(), toReturn);
				continue;
			}

			if (behaviourHeaderDetected && ! firstXXDetected) {
				// ignore meta info header lines
			} else {
				if (line.isEmpty()) {
					continue;
				}
				
				if ( isFeatureBased ) {
					extractInstruction(line.trim(), toReturn);
				} else {
					toReturn.add(line);
				}
			}
		}

		toReturn = removeBddKeywordMethodCallsIfPossible(toReturn);
		toReturn = checkForSimpleScriptCallSyntax(toReturn);
		toReturn = checkForSingleTestDataValueSetter(toReturn);

		return toReturn;
	}

	private List<String> checkForSingleTestDataValueSetter(List<String> instructions) 
	{
		final List<String> toReturn = new ArrayList<>();
		for (String line : instructions)
		{
			if ( ! line.contains("\"") && 
				 ! line.contains("<") && 
				 ! line.contains(">") && 
				 ! line.contains("'")  )
			{
				String[] splitResult = line.split("=");
				if (splitResult.length == 2) {
					line = "\"" + splitResult[0].trim() + "\" = \"" + splitResult[1].trim() + "\"";
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
	 * For convenience reasons, the following does the same: Create an order.
	 * This convenience functionality is implemented here.
     */
	private List<String> checkForSimpleScriptCallSyntax(List<String> instructions)
	{
		final List<String> toReturn = new ArrayList<>();
		for (String line : instructions)
		{
			if (isScriptCall(line)) {
				toReturn.add("\"" + line.substring(0, line.length()-1) + "\".");
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

	private boolean doesContainPlaceholder(String instruction) {
		return    instruction.contains("<")
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

	void extractInstruction(String line, List<String> instructions)
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