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

import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
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

	protected HashMap<Filename, List<LanguageInstructionPattern>> findAllNaturalLanguageInstruction() 
	{
		final HashMap<Filename, List<LanguageInstructionPattern>> toReturn = new HashMap<>();
		final HashMap<Filename, List<String>> instructionLines = new HashMap<>();

		// step A: read instructionslines for each executable example file and each script file
		final List<File> nlsFiles = findInstructionFiles(".nls");
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

	private List<String> getInstructionLines(File file) 
	{
		List<String> content = SysNatFileUtil.readTextFile(file);
		List<String> toReturn = new ArrayList<>();
		content.forEach(line -> extractInstruction(line.trim(), toReturn));
		return toReturn;
	}

	private void extractInstruction(String line, List<String> instructions) 
	{
		if ( line.isEmpty() || line.startsWith(COMMENT_SEPARATOR)) 
			return;
		
		int pos = line.indexOf(COMMENT_SEPARATOR);
		if (pos > 0) line = line.substring(0, pos).trim();
		
		if (BDDKeywordUtil.startsWithBDDKeyword(line)) {
			line = transformToSysNatScriptCall(line);
		}
		
		instructions.add(line);
	}

	private String transformToSysNatScriptCall(String line) {
		// TODO Auto-generated method stub
		return line;
	}

}