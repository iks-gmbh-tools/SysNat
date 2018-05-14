package com.iksgmbh.sysnat.helper;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.LanguageInstructionPattern;
import com.iksgmbh.sysnat.utils.SysNatFileUtil;

/**
 * Collects all natural language instructions from all nltc-files of the given test application.
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
	 * @return List of LanguageInstructionPatterns for each nltc-file found
	 */
	public static HashMap<Filename, List<LanguageInstructionPattern>> doYourJob(final String applicationName) 
	{
		return new LanguageInstructionCollector(applicationName).findAllNaturalLanguageInstruction();

	}

	protected HashMap<Filename, List<LanguageInstructionPattern>> findAllNaturalLanguageInstruction() 
	{

		final HashMap<Filename, List<LanguageInstructionPattern>> toReturn = new HashMap<>();
		final HashMap<Filename, List<String>> instructionLines = new HashMap<>();

		// step A: read instructionslines for each test case file
		final List<File> nltcFiles = findTestCaseFiles();
		nltcFiles.stream().forEach(file -> instructionLines.put(new Filename(file.getAbsolutePath()),  getInstructionLines(file)));

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
		return System.getProperty("sysnat.testcase.source.dir");
	}

	protected List<File> findTestCaseFiles() 
	{
		final File testCaseDir = new File(getTestCaseDir(), applicationName);
		final FilenameFilter fileFilter = new FilenameFilter() {
			@Override public boolean accept(File dir, String name) {
				return name.endsWith(".nltc");
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
		if ( line.isEmpty() || line.startsWith(COMMENT_SEPARATOR)) return;
		int pos = line.indexOf(COMMENT_SEPARATOR);
		if (pos > 0) line = line.substring(0, pos).trim();
		instructions.add(line);
	}	
}
