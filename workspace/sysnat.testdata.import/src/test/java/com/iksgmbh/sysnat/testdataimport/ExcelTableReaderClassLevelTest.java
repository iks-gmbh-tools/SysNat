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
		final File excelFile = new File("../sysnat.testdata.import/src/test/resources/testTestdata/excel/ExcelTestData.xlsx");
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