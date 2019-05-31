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
package com.iksgmbh.sysnat.testdataimport;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

/**
 * Reads a dat file and parses the content to list of properties.
 * 
 * @author Reik Oberrath
 */
public class ValidationFileReader
{
	static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/PDFContentValidation", Locale.getDefault());
	static final ResourceBundle BUNDLE_EN = ResourceBundle.getBundle("bundles/PDFContentValidation", Locale.ENGLISH);

	private static final String PATTERN_SEARCH_WHOLE_DOCUMENT = BUNDLE.getString("Pattern_FileContains") + " \"";
	private static final String PATTERN_SEARCH_IN_PAGE_VARIANTE1 = BUNDLE.getString("Pattern_PageContains_Variant1") + " \"";
	private static final String PATTERN_SEARCH_IN_PAGE_VARIANTE2 = BUNDLE.getString("Pattern_PageContains_Variant2") + " ";
	private static final String PATTERN_SEARCH_IN_LINE = " " + BUNDLE.getString("Pattern_LineContains") + " ";
	private static final String PATTERN_SEQUENCE = " " + BUNDLE.getString("Pattern_sequence") + " ";

	private static final String PATTERN_SEARCH_WHOLE_DOCUMENT_EN = BUNDLE_EN.getString("Pattern_FileContains") + " \"";
	private static final String PATTERN_SEARCH_IN_PAGE_VARIANTE1_EN = BUNDLE_EN.getString("Pattern_PageContains_Variant1") + " \"";
	private static final String PATTERN_SEARCH_IN_PAGE_VARIANTE2_EN = BUNDLE_EN.getString("Pattern_PageContains_Variant2") + " ";
	private static final String PATTERN_SEARCH_IN_LINE_EN = " " + BUNDLE_EN.getString("Pattern_LineContains") + " ";
	private static final String PATTERN_SEQUENCE_EN = " " + BUNDLE_EN.getString("Pattern_sequence") + " ";
	
	private File inputFile;

	ValidationFileReader(final File aDataFile) {
		inputFile = aDataFile;
	}
	
	public static LinkedHashMap<String, String> doYourJob(final File file)
	{
		return new ValidationFileReader(file).readData();
	}

	private LinkedHashMap<String, String> readData()
	{
		final LinkedHashMap<String, String> toReturn = new LinkedHashMap<>();
		final List<String> lines = extractDataLines(SysNatFileUtil.readTextFile(inputFile));

		for (String line: lines) {
			addToProperties(line, toReturn);
		}

		return toReturn;
	}

	void addToProperties(String line, LinkedHashMap<String, String> toReturn)
	{
		try {
			List<String> lineValues = extractValues(line);
			switch (lineValues.size())
			{
				case 1: toReturn.put(lineValues.get(0), "");
						break;
				case 2: toReturn.put(lineValues.get(0), lineValues.get(1));
						break;
				case 3: toReturn.put(lineValues.get(0), lineValues.get(1) + "::" + lineValues.get(2));
						break;
			}
		}
		catch (Exception e) 
		{
			//e.printStackTrace();
			String errorMessage = BUNDLE.getString("InvalidValidationRuleMessage");
			errorMessage = errorMessage.replace("XY", line);
			throw new SysNatTestDataException(errorMessage);
		}
	}

	private List<String> extractValues(String line)
	{
		final List<String> lineValues = new ArrayList<>();
		String textToSearchFor = null;

		if (line.startsWith(PATTERN_SEARCH_WHOLE_DOCUMENT)) {
			textToSearchFor = getTextToSearchFor(line, PATTERN_SEARCH_WHOLE_DOCUMENT);
			lineValues.add(textToSearchFor);
			return lineValues;
		}
		if (line.startsWith(PATTERN_SEARCH_WHOLE_DOCUMENT_EN)) {
			textToSearchFor = getTextToSearchFor(line, PATTERN_SEARCH_WHOLE_DOCUMENT_EN);
			lineValues.add(textToSearchFor);
			return lineValues;
		}

		String pageToSearchIn = null;
		String lineNo = null;

		if (line.startsWith(PATTERN_SEARCH_IN_PAGE_VARIANTE1)) {
			line = line.substring(PATTERN_SEARCH_IN_PAGE_VARIANTE1.length());
			int pos = line.indexOf('"');
			pageToSearchIn = line.substring(0, pos);
			line = line.substring(pos+1);
		} else if (line.startsWith(PATTERN_SEARCH_IN_PAGE_VARIANTE2)) {
			line = line.substring(PATTERN_SEARCH_IN_PAGE_VARIANTE2.length());
			int pos = line.indexOf(' ');
			pageToSearchIn = line.substring(0, pos);
			line = line.substring(pos);
		} else if (line.startsWith(PATTERN_SEARCH_IN_PAGE_VARIANTE1_EN)) {
			line = line.substring(PATTERN_SEARCH_IN_PAGE_VARIANTE1_EN.length());
			int pos = line.indexOf('"');
			pageToSearchIn = line.substring(0, pos);
			line = line.substring(pos);
		} else if (line.startsWith(PATTERN_SEARCH_IN_PAGE_VARIANTE2_EN)) {
			line = line.substring(PATTERN_SEARCH_IN_PAGE_VARIANTE2_EN.length());
			int pos = line.indexOf(' ');
			pageToSearchIn = line.substring(0, pos);
			line = line.substring(pos);
		}

		if (line.startsWith(PATTERN_SEARCH_IN_LINE))
		{
			line = line.substring(PATTERN_SEARCH_IN_LINE.length());
			int pos = line.indexOf(' ');
			lineNo = line.substring(0, pos);
			line = line.substring(pos);
		} 
		else if (line.startsWith(PATTERN_SEARCH_IN_LINE_EN)) 
		{
			line = line.substring(PATTERN_SEARCH_IN_LINE_EN.length());
			int pos = line.indexOf(' ');
			lineNo = line.substring(0, pos);
			line = line.substring(pos);
		}

		if (line.contains(PATTERN_SEQUENCE)) 
		{
			textToSearchFor = line.substring(PATTERN_SEQUENCE.length());
			int pos1 = textToSearchFor.indexOf('"');
			int pos2 = textToSearchFor.lastIndexOf('"');
			textToSearchFor = textToSearchFor.substring(pos1+1, pos2);
		} 
		else if (line.contains(PATTERN_SEQUENCE_EN)) 
		{
			textToSearchFor = line.substring(PATTERN_SEQUENCE_EN.length());
			int pos1 = textToSearchFor.indexOf('"');
			int pos2 = textToSearchFor.lastIndexOf('"');
			textToSearchFor = textToSearchFor.substring(pos1+1, pos2);
		}


		lineValues.add(textToSearchFor.trim());
		lineValues.add(pageToSearchIn.trim());
		if (lineNo != null) lineValues.add(lineNo.trim());

		return lineValues;
	}

	private String getTextToSearchFor(final String line, 
			                          final String pattern)
	{
		String textToSearchFor = line.substring(pattern.length());
		int pos = textToSearchFor.indexOf('"');
		return textToSearchFor.substring(0, pos);
	}


	private List<String> extractDataLines(List<String> fileContent) 
	{
		final List<String> dataLines = new ArrayList<>();
		
		fileContent.stream().filter(line -> ! line.isEmpty())
		                    .filter(line -> ! line.trim().startsWith("#"))
		                    .forEach(line -> dataLines.add(line));
		
		return dataLines;
	}

}