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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;

/**
 * Parses a list of lines containing a list of datasets in table format 
 * to a list of properties.
 * 
 * @author Reik Oberrath
 */
public class TableDataParser 
{
	public static final String TABLE_DATA_IDENTIFIER = "Datasets in ";
	public static final String CELL_SEPARATOR = "|";
	public static final String ROWS = "rows";
	public static final String COLUMNS = "columns";

	private String inputFile;
	
	private TableDataParser(final String aDataFile) {
		inputFile = aDataFile;
	}

	public static List<Properties> doYourJob(String aDatafile, String lines) 
	{
		String[] splitResult = lines.split(SysNatConstants.LINE_SEPARATOR);
		List<String> linesList = Arrays.asList(splitResult).stream().collect(Collectors.toList());
		return doYourJob( aDatafile, linesList );
	}

	public static List<Properties> doYourJob(String aDataFile, List<String> lines) {
		return new TableDataParser(aDataFile).parse(lines);
	}
	
	private List<Properties> parse(List<String> lines) 
	{
		String format = ROWS;  // DEFAULT
		if (lines.get(0).startsWith(TABLE_DATA_IDENTIFIER)) 
		{
			format = lines.get(0).substring(TABLE_DATA_IDENTIFIER.length());
			lines.remove(0);
		}

		checkTableComplete(lines);

		if (isTableDataFormatHorizontal(format)) {
			return horizontalTableDataToProperties(lines);
		}

		if (isTableDataFormatVertical(format)) {
			return verticalTableDataToProperties(lines);
			
		}
		
		throw new SysNatTestDataException("Unknown Table Data Format '" 
		          + format 
		          + ". Use " + COLUMNS + " or " + ROWS + ".");
	}

	private List<Properties> verticalTableDataToProperties(List<String> lines) 
	{
		final List<Properties> toReturn = new ArrayList<>();
		int columnNumber = getCellValues(lines.get(0)).size();

		
		for (int column = 1; column < columnNumber; column++) 
		{
			final Properties properties = new Properties();
			for (int row = 0; row < lines.size(); row++) 
			{
				List<String> values = getCellValues(lines.get(row));
				String fieldName = values.get(0);
				properties.setProperty(fieldName, values.get(column).trim());
			}
			toReturn.add(properties);
		}
		
		
		return toReturn;
}

	private void checkTableComplete(List<String> lines) 
	{
		int expectedCellNumber = getNumberOfCellsInRow(lines.get(0));
		if (expectedCellNumber < 1) {
			throw new SysNatTestDataException("No cells found in first line of table " 
		              + inputFile + "!");
		}
		
		int badLineNumber = 0;
		int lineCounter = 0;
		
		for (String line : lines) 
		{
			lineCounter++;
			int numberCells = getNumberOfCellsInRow(line);
			if (numberCells != expectedCellNumber) {
				badLineNumber++;
				System.err.println("Unexcpected cell number (" + numberCells + ") in line " 
				   + lineCounter + ". Expected: " + expectedCellNumber );
			}
		}
		
		if (badLineNumber > 0) {
			throw new SysNatTestDataException("Data table in file " 
		              + inputFile + " not complete!");
		}
	}

	private int getNumberOfCellsInRow(String line) {
		return SysNatStringUtil.countNumberOfOccurrences(line, CELL_SEPARATOR) - 1;
	}

	private List<Properties> horizontalTableDataToProperties(List<String> lines) 
	{
		List<String> fieldNames = getCellValues(lines.get(0));
		List<Properties> toReturn = new ArrayList<>();
		
		for (int i = 1; i < lines.size(); i++) 
		{
			final Properties properties = new Properties();
			List<String> values = getCellValues(lines.get(i));
			for (int column = 0; column < values.size(); column++) {
				properties.setProperty(fieldNames.get(column), values.get(column).trim());
			}
			toReturn.add(properties);
		}
		
		return toReturn;
	}

	private List<String> getCellValues(String line) 
	{
		line = cutEmbracingSeparators(line);
		String[] splitResult = line.split("\\" + CELL_SEPARATOR);
		return Arrays.asList(splitResult);
	}

	private String cutEmbracingSeparators(String line) {
		return line.substring(1, line.length()-1); 
	}

	private boolean isTableDataFormatHorizontal(String format) 
	{
		return format.equals(ROWS);
	}

	private boolean isTableDataFormatVertical(String format) {
		return format.equals(COLUMNS);
	}

	
}