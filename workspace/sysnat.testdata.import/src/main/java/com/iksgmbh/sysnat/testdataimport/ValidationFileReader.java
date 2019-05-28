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

import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Reads a dat file and parses the content to list of properties.
 * 
 * @author Reik Oberrath
 */
public class ValidationFileReader
{
	private static final String PATTERN_SEARCH_WHOLE_DOCUMENT = "Das Dokument enthält den Text \"";
	private static final String PATTERN_SEARCH_IN_PAGE_VARIANTE1 = "Das Dokument enthält auf der Seite mit dem Text \"";
	private static final String PATTERN_SEARCH_IN_PAGE_VARIANTE2 = "Das Dokument enthält auf Seite ";
	private static final String PATTERN_SEARCH_IN_LINE = " in Zeile ";
	private static final String PATTERN_SEARCH_TEXT_IDENT = " den Text ";

	private File inputFile;

	ValidationFileReader(final File aDataFile) {
		inputFile = aDataFile;
	}
	
	public static Properties doYourJob(final File file)
	{
		return new ValidationFileReader(file).readData();
	}

	private Properties readData()
	{
		final Properties toReturn = new Properties();
		final List<String> lines = extractDataLines(SysNatFileUtil.readTextFile(inputFile));

		for (String line: lines) {
			addToProperties(line, toReturn);
		}

		return toReturn;
	}

	private void addToProperties(String line, Properties toReturn)
	{
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

	private List<String> extractValues(String line)
	{
		final List<String> lineValues = new ArrayList<>();
		String fullLine = line;
		String textToSearchFor = null;


		if (line.startsWith(PATTERN_SEARCH_WHOLE_DOCUMENT)) {
			textToSearchFor = line.substring(PATTERN_SEARCH_WHOLE_DOCUMENT.length());
			textToSearchFor = textToSearchFor.substring(0, textToSearchFor.length()-1);
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
		} else {
			throw new SysNatTestDataException("Zeile nicht verständlich: '" + fullLine + "'.");
		}

		if (line.startsWith(PATTERN_SEARCH_IN_LINE))
		{
			line = line.substring(PATTERN_SEARCH_IN_LINE.length());
			int pos = line.indexOf(' ');
			lineNo = line.substring(0, pos);
			line = line.substring(pos);
		}

		if (line.contains(PATTERN_SEARCH_TEXT_IDENT)) {
			textToSearchFor = line.substring(PATTERN_SEARCH_TEXT_IDENT.length());
			textToSearchFor = textToSearchFor.substring(1, textToSearchFor .length()-1);
		}


		lineValues.add(textToSearchFor.trim());
		lineValues.add(pageToSearchIn.trim());
		if (lineNo != null) lineValues.add(lineNo.trim());

		return lineValues;
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