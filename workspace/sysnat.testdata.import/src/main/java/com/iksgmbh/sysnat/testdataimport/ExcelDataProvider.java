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

public class ExcelDataProvider
{
	// maybe used later on
//	private static final String ROTATION_MODE_DATASET_NAMES_IN_FIRST_ROW = "Dataset Names In First Row";
//	private static final String ROTATION_MODE_DATASET_NAMES_IN_FIRST_COLUMN = "Dataset Names In First Column";

	public static Hashtable<String, Properties> doYourJob(final File excelFile) 
	{
		final ExcelTableReader excelTableReader;
		try {
			excelTableReader = new ExcelTableReader(excelFile);
		} catch (IOException e) {
			e.printStackTrace();
			throw new SysNatTestDataException("Fehler beim Lesen von " + excelFile.getAbsolutePath());
		}
		
		final Cell firstCell = new Cell(1, 1);
		final String[][] matrixData = excelTableReader.getMatrix(firstCell);
		
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
//	private static String[][] rotate(String[][] originalMatrixData)
//	{
//		final String[][] toReturn = new String[originalMatrixData[0].length][originalMatrixData.length];
//		for (int origRow = 0; origRow < originalMatrixData.length; origRow++) 
//		{
//			for (int origCol = 0; origCol < originalMatrixData[0].length; origCol++) 
//			{
//				toReturn[origCol][origRow] = originalMatrixData[origRow][origCol];
//			}
//			
//		}
//		return toReturn;
//	}



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