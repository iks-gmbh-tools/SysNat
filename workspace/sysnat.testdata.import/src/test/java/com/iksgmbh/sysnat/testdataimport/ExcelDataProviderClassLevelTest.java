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
	public void readsDatasetsFromExcelFile() throws IOException 
	{
		// arrange
		final File excelFile = new File("../sysnat.testdata.import/src/test/resources/testTestdata/ExcelTestData.xlsx");
		
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
		final File excelFile = new File("../sysnat.testdata.import/src/test/resources/testTestdata/EmptyDataFile.xlsx");
		
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
		final File excelFile = new File("../sysnat.testdata.import/src/test/resources/testTestdata/MissingDatasets.xlsx");
		
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
		final File excelFile = new File("../sysnat.testdata.import/src/test/resources/testTestdata/MissingFields.xlsx");
		
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
