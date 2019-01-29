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
import java.util.List;
import java.util.Properties;

import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

/**
 * Reads a dat file and parses the content to list of properties.
 * 
 * @author Reik Oberrath
 */
public class DatFileReader 
{
	private File inputFile;
	
	private DatFileReader(final File aDataFile) {
		inputFile = aDataFile;
	}
	
	public static List<Properties> doYourJob(final File file) 
	{
		return new DatFileReader(file).readData();
	}

	private List<Properties> readData() 
	{
		final List<String> lines = extractDataLines(SysNatFileUtil.readTextFile(inputFile));
		
		if (isDataInTableFormat(lines)) {
			return TableDataParser.doYourJob(inputFile.getAbsolutePath(), lines);
		}

		return readAsSingleProperties(lines);
	}

	private boolean isDataInTableFormat(final List<String> lines) {
		return lines.get(0).startsWith(TableDataParser.TABLE_DATA_IDENTIFIER);
	}


	private List<String> extractDataLines(List<String> fileContent) 
	{
		final List<String> dataLines = new ArrayList<>();
		
		fileContent.stream().filter(line -> ! line.isEmpty())
		                    .filter(line -> ! line.trim().startsWith("#"))
		                    .forEach(line -> dataLines.add(line));
		
		return dataLines;
	}

	private List<Properties> readAsSingleProperties(final List<String> lines) 
	{
		final List<Properties> toReturn = new ArrayList<>();
		
		Properties properties = new Properties();
		int lineCounter = 0;

		for (String line : lines) 
		{
			line = line.trim();
			lineCounter++;
			if (line.equals("") || line.startsWith("#")) {
				// ignore this line
			} else if (line.equals("-")) 
			{
				if ( ! properties.isEmpty()) {
					toReturn.add(properties);
					properties = new Properties();
				}
			} 
			else 
			{
				if (line.endsWith("=")) {
					properties.put(line.subSequence(0, line.length()-1), "");
				} else {
					String[] splitResult = line.split("=");
					if (splitResult.length != 2) {
						throw new SysNatTestDataException("Error reading property in <b>" 
					          + inputFile.getAbsolutePath()
	                          + "</b>, line " + lineCounter + " \"<b>" + line + "\"<b>.");
					} else {
						properties.put(splitResult[0], splitResult[1]);
					}
				}
			}
		}
		
		if ( ! properties.isEmpty()) 
			toReturn.add(properties);
		
		return toReturn;
	}

}