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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;

public class ExcelDataProviderClassLevelTest {

	@Test
	public void readsRotatedDatasetsFromExcelFile() throws IOException 
	{
		// arrange
		final File excelFile = new File("../sysnat.testdata.import/src/test/resources/testTestdata/excel/RotatedExcelTestData.xlsx");
		
		// act
		final Hashtable<String, Properties> result = ExcelDataProvider.doYourJob(excelFile);
		
		// assert
		assertEquals("Number of Datasets", 4, result.size());
		final List<String> sortedListOfDatasetNames = getSortedKeys(result);
		final String firstOrderedDatasetName = sortedListOfDatasetNames.get(0);
		assertEquals("DatasetName", "RotatedExcelTestData_GreetingValidationData1", firstOrderedDatasetName);
		assertEquals("DatasetName", "Stephen", result.get(firstOrderedDatasetName).getProperty("Name").toString());
	}
	
	
	@Test
	public void readsDatasetsFromExcelFile() throws IOException 
	{
		// arrange
		final File excelFile = new File("../sysnat.testdata.import/src/test/resources/testTestdata/excel/ExcelTestData.xlsx");
		
		// act
		final Hashtable<String, Properties> result = ExcelDataProvider.doYourJob(excelFile);
		
		// assert
		assertEquals("Number of Datasets", 4, result.size());
		final List<String> sortedListOfDatasetNames = getSortedKeys(result);
		assertEquals("DatasetName", "ExcelTestData_GreetingValidationData1", sortedListOfDatasetNames.get(0));
		assertEquals("DatasetName", "Stephen", result.get(sortedListOfDatasetNames.get(0)).getProperty("Name").toString());
	}

	@Test
	public void throwsErrorForEmptyMatrix() throws Exception 
	{		
		// arrange
		final File excelFile = new File("../sysnat.testdata.import/src/test/resources/testTestdata/excel/EmptyDataFile.xlsx");
		
		try {
			// act
			ExcelDataProvider.doYourJob(excelFile);
			fail("Expected exception not thrown!");
		} catch (SysNatTestDataException e) {
			// assert
			assertTrue("unexpected error message", e.getMessage().startsWith("No test data found in"));
		}
	}

	@Test
	public void throwsErrorForMissingDatasets() throws Exception 
	{		
		// arrange
		final File excelFile = new File("../sysnat.testdata.import/src/test/resources/testTestdata/excel/MissingDatasets.xlsx");
		
		try {
			// act
			ExcelDataProvider.doYourJob(excelFile);
			fail("Expected exception not thrown!");
		} catch (SysNatTestDataException e) {
			// assert
			assertTrue("unexpected error message", e.getMessage().startsWith("No test data found in"));
		}
	}
	
	@Test
	public void throwsErrorForMissingFields() throws Exception 
	{		
		// arrange
		final File excelFile = new File("../sysnat.testdata.import/src/test/resources/testTestdata/excel/MissingFields.xlsx");
		
		try {
			// act
			ExcelDataProvider.doYourJob(excelFile);
			fail("Expected exception not thrown!");
		} catch (SysNatTestDataException e) {
			// assert
			assertTrue("unexpected error message", e.getMessage().startsWith("No test data found in"));
		}
	}
	
	private List<String> getSortedKeys(final Hashtable<String, Properties> result) 
	{
		final ArrayList<String> keys = new ArrayList<>(result.keySet());
		Collections.sort( keys);
		return keys;
	}

}