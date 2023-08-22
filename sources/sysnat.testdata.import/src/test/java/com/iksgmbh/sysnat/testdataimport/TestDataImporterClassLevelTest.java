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
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;

public class TestDataImporterClassLevelTest 
{
	private TestDataImporter cut = new TestDataImporter("../sysnat.testdata.import/src/test/resources/testTestdata");
	
	@Test
	public void findsDatFileToLoad() 
	{
		// arrange
		cut.testdataId = "singleDataset";
		
		// act
		final List<File> result = cut.findFilesToLoad();
		
		// arrange
		if (result.size() != 1) {
			result.forEach(System.err::println);
		}
		assertEquals("Number of files", 1, result.size());
	}
	
	@Test
	public void findsExcelFileToLoad() 
	{
		// arrange
		cut.testdataId = "ExcelTestData";
		
		// act
		final List<File> result = cut.findFilesToLoad();
		
		// arrange
		if (result.size() != 1) {
			result.forEach(System.err::println);
		}
		assertEquals("Number of files", 1, result.size());
	}
	
	@Test
	public void findsAllFilesToLoad() 
	{
		// arrange
		cut.testdataId = "BigTestDataSeriesPart";
		
		// act
		final List<File> result = cut.findFilesToLoad();
		
		// arrange
		assertEquals("Number of files", 4, result.size());
	}


	@Test
	public void throwsExceptionForMissingTestData() throws Exception 
	{
		// arrange
		System.setProperty("sysnat.report.dir", "target");
		System.setProperty(SysNatConstants.TEST_REPORT_NAME_SETTING_KEY, "TestReport");
		System.setProperty(SysNatConstants.SYSNAT_DUMMY_TEST_RUN, "false");
		
		try {
			// act
			cut.loadTestdata("unknown");
			fail("Expected exception not thrown!");
		} catch (SysNatTestDataException e) {
			// assert
			assertEquals("Error Message", "<b>unknown</b> is not found in ../sysnat.testdata.import/src/test/resources/testTestdata.", e.getMessage() );	
		}
	}


	@Test
	public void loadDatasetsFromMultipleDatFiles() throws Exception 
	{
		// arrange
		final String testdataId = "singleDatasetSeries";
		
		// act
		final Hashtable<String, Properties> result = cut.loadTestdata(testdataId);
		
		// arrange
		assertEquals("Number of datasets", 2, result.size());
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final List<String> keys = new ArrayList(result.keySet());
		Collections.sort(keys);
		final String key1 = keys.get(0);
		final String key2 = keys.get(1);
		assertEquals("DatasetID", testdataId + "__1", key1);
		assertEquals("DatasetID", testdataId + "__2", key2);
		assertEquals("Number of datafields in dataset", 2, result.get(key1).size());
		assertEquals("Number of datafields in dataset", 1, result.get(key2).size());
	}

	@Test
	public void loadMultipleDatasetsFromMultipleDatFiles() throws Exception 
	{

		// arrange
		final String testdataId = "multipleDatasetSeries";
		
		// act
		final Hashtable<String, Properties> result = cut.loadTestdata(testdataId);
		
		// arrange
		assertEquals("Number of datasets", 7, result.size());
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final List<String> keys = new ArrayList(result.keySet());
		Collections.sort(keys);
		final String key1 = keys.get(0);
		final String key2 = keys.get(1);
		final String key3 = keys.get(2);
		final String key4 = keys.get(3);
		final String key5 = keys.get(4);
		final String key6 = keys.get(5);
		final String key7 = keys.get(6);

		assertEquals("DatasetID", testdataId + "__1__1", key1);
		assertEquals("DatasetID", testdataId + "__1__2", key2);
		assertEquals("DatasetID", testdataId + "__2__1", key3);
		assertEquals("DatasetID", testdataId + "__2__2", key4);
		assertEquals("DatasetID", testdataId + "__3__1", key5);
		assertEquals("DatasetID", testdataId + "__3__2", key6);
		assertEquals("DatasetID", testdataId + "__3__3", key7);
	}
	
	@Test
	public void loadDatasetsFromDatAndExcelFiles() throws Exception 
	{
		// arrange
		final String testdataId = "BigTestDataSeriesPart";
		
		// act
		final Hashtable<String, Properties> result = cut.loadTestdata(testdataId);
		
		// arrange
		assertEquals("Number of datasets", 16, result.size());
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final List<String> keys = new ArrayList(result.keySet());
		Collections.sort(keys);
		assertEquals("DatasetID", "BigTestDataSeriesPart1__GreetingValidationData1", keys.get(0));
		assertEquals("DatasetID", "BigTestDataSeriesPart1__GreetingValidationData2", keys.get(1));
		assertEquals("DatasetID", "BigTestDataSeriesPart1__GreetingValidationData3", keys.get(2));
		assertEquals("DatasetID", "BigTestDataSeriesPart1__GreetingValidationData4", keys.get(3));
		assertEquals("DatasetID", "BigTestDataSeriesPart2__GreetingValidationData1", keys.get(4));
		assertEquals("DatasetID", "BigTestDataSeriesPart2__GreetingValidationData2", keys.get(5));
		assertEquals("DatasetID", "BigTestDataSeriesPart2__GreetingValidationData3", keys.get(6));
		assertEquals("DatasetID", "BigTestDataSeriesPart2__GreetingValidationData4", keys.get(7));
		assertEquals("DatasetID", "BigTestDataSeriesPart3__1", keys.get(8));
		assertEquals("DatasetID", "BigTestDataSeriesPart3__2", keys.get(9));
		assertEquals("DatasetID", "BigTestDataSeriesPart3__3", keys.get(10));
		assertEquals("DatasetID", "BigTestDataSeriesPart3__4", keys.get(11));
		assertEquals("DatasetID", "BigTestDataSeriesPart4__1", keys.get(12));
		assertEquals("DatasetID", "BigTestDataSeriesPart4__2", keys.get(13));
		assertEquals("DatasetID", "BigTestDataSeriesPart4__3", keys.get(14));
		assertEquals("DatasetID", "BigTestDataSeriesPart4__4", keys.get(15));
		
		for (int i = 0; i < 8; i++) {
			// data sets read from excel file have one attribute more 
			// i.e. 4 instead of 3 due to TestDataImporter.EXCEL_ID 
			String message = "Number of fields in " + (i+1) + "th dataset";
			assertEquals(message, 4, result.get(keys.get(i)).size());
		}

		for (int i = 8; i < keys.size(); i++) {
			String message = "Number of fields in " + (i+1) + "th dataset";
			assertEquals(message, 3, result.get(keys.get(i)).size());
		}
		
		
		assertEquals("Fieldname", "Hi Stephen!", result.get(keys.get(0)).get("GreetingResult"));
		assertEquals("Fieldname", "Hello Lisa!", result.get(keys.get(5)).get("GreetingResult"));
		assertEquals("Fieldname", " Good Day Dean!", result.get(keys.get(10)).get("GreetingResult"));
		assertEquals("Fieldname", " Hey Dora!", result.get(keys.get(15)).get("GreetingResult"));
		
	}
	
}