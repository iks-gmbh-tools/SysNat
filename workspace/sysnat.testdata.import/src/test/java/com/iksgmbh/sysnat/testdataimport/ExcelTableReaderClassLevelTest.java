package com.iksgmbh.sysnat.testdataimport;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.iksgmbh.sysnat.testdataimport.ExcelTableReader.Cell;

public class ExcelTableReaderClassLevelTest {

	@Test
	public void readsMatrixFromExcelFile() throws IOException 
	{
		// arrange
		final File excelFile = new File("../sysnat.testdata.import/src/test/resources/testTestdata/ExcelTestData.xlsx");
		final Cell firstCell = new Cell(1, 1);
		
		// act
		final String[][] matrix = (new ExcelTableReader(excelFile)).getMatrix(firstCell);
		
		// assert
		assertEquals("Cell value", "<Default Matrix Root Cell>", matrix[0][0]);
		assertEquals("Cell value", "Greeting", matrix[0][1]);
		assertEquals("Cell value", "Name", matrix[0][2]);
		assertEquals("Cell value", "GreetingValidationData1", matrix[1][0]);
		assertEquals("Cell value", "Hi", matrix[1][1]);
	}

}
