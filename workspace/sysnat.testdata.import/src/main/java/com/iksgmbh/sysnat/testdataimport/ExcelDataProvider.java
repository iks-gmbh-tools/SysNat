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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.testdataimport.ExcelTableReader.Cell;

/**
 * Builds datasets from a squared matrix read from an excel file (per default the first sheet of the file).

 * The matrix is defined by its upper left cell (the "root cell") - per default the cell A1.
 * All neighbouring non-emtpy cells to the right of the root cell belong to the "first row" of the matrix until the first emtpy cell is detected.  
 * All neighbouring non-emtpy cells below the root cell belong to the "first column" of the matrix until the first emtpy cell is detected.
 * The matrix is defined by the rectangle constructed by the first row and the first column.
 * 
 * Per default, datasets are organized in rows. That means, the dataset names are supposed to be represented by the first column.
 * The content of the root cells allows to indicate the opposite, i. e. the matrix is rotated, datasets are organized in columns and the dataset names are read from the first row.
 * To rotate the matrix, write 'rotate' or 'datasets in columns' into the root cell.
 * 
 * @author Reik Oberrath
 */
public class ExcelDataProvider
{
	private static final String ROTATION_MODE_DATASET_NAMES_IN_FIRST_ROW_1 = "Column is dataset";
	private static final String ROTATION_MODE_DATASET_NAMES_IN_FIRST_ROW_2 = "Datasets in columns";
	//private static final String ROTATION_MODE_DATASET_NAMES_IN_FIRST_COLUMN = "Row is Dataset";
	
	public static Hashtable<String, Properties> doYourJob(final File excelFile) 
	{
		return doYourJob(excelFile, 1, 1, 1);
	}
	
	public static Hashtable<String, Properties> doYourJob(final File excelFile, 
                                                          final int sheetNumber,
			                                              final int columnOfRootCell, 
			                                              final int rowOfRootCell) 
	{
		final ExcelTableReader excelTableReader;
		try {
			excelTableReader = new ExcelTableReader(excelFile, sheetNumber);
		} catch (IOException e) {
			e.printStackTrace();
			throw new SysNatTestDataException("Fehler beim Lesen von " + excelFile.getAbsolutePath());
		}
		
		final Cell rootCell = new Cell(columnOfRootCell, rowOfRootCell);
		final String rootCellContent = excelTableReader.getContent(rootCell);
		final String[][] matrixData;

		if (rootCellContent.equalsIgnoreCase(ROTATION_MODE_DATASET_NAMES_IN_FIRST_ROW_1) 
			|| rootCellContent.equalsIgnoreCase(ROTATION_MODE_DATASET_NAMES_IN_FIRST_ROW_2)
			|| rootCellContent.equalsIgnoreCase("rotate")) {
			matrixData = rotate( excelTableReader.getMatrix(rootCell) );
		} else {
			matrixData = excelTableReader.getMatrix(rootCell);
		}
		
		checkMatrixData(matrixData, excelFile.getAbsolutePath());
		
		final List<String> fieldNames = getFirstRowData(matrixData);
		final List<String> datasetNames = getFirstColumnData(matrixData);
		final Hashtable<String, Properties> testData = new Hashtable<>();
		final String excelFileName = SysNatStringUtil.cutExtension(excelFile.getName());
		
		for (int row = 1; row < matrixData.length; row++) 
		{
			Properties fieldValuePairs = new Properties();
			for (int column = 1; column < matrixData[row].length; column++) {
				fieldValuePairs.setProperty(fieldNames.get(column-1), matrixData[row][column]);
			}
			testData.put(excelFileName + "_" + datasetNames.get(row-1), fieldValuePairs);
		}
		
		return testData;
	}

	private static void checkMatrixData(final String[][] matrixData, 
			                            final String excelFileName) 
	{
		if (matrixData.length == 1) {
			throw new SysNatTestDataException("No test data found in " + excelFileName);
		}
		if (matrixData[0].length == 1) {
			throw new SysNatTestDataException("No test data found in " + excelFileName);
		}
		
	}

	// maybe used later on
	private static String[][] rotate(String[][] originalMatrixData)
	{
		final String[][] toReturn = new String[originalMatrixData[0].length][originalMatrixData.length];
		for (int origRow = 0; origRow < originalMatrixData.length; origRow++) 
		{
			for (int origCol = 0; origCol < originalMatrixData[0].length; origCol++) 
			{
				toReturn[origCol][origRow] = originalMatrixData[origRow][origCol];
			}
			
		}
		return toReturn;
	}



	private static List<String> getFirstRowData(final String[][] matrix)
	{
		final String[] firstRowData = matrix[0];
		final List<String> toReturn = new ArrayList<String>();
		
		for (int i = 1; i < firstRowData.length; i++) {
			toReturn.add(firstRowData[i]);
		}
		
		return toReturn;
	}
	
	private static List<String> getFirstColumnData(final String[][] matrix)
	{
		final List<String> toReturn = new ArrayList<String>();
		
		for (int i = 1; i < matrix.length; i++) 
		{			
			final String[] rowData = matrix[i];
			toReturn.add(rowData[0]);
		}
		
		return toReturn;
	}	

}