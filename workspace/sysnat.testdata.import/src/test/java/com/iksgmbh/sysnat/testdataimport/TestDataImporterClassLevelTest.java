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
	public void loadSingleDatasetFromSingleDatFiles() 
	{
		// arrange
		cut.testdataId = "singleDataset";
		
		// act
		final List<File> filesToLoad = cut.findFilesToLoad();
		final List<Properties> testdata = cut.loadDatasetsFromDatFile(filesToLoad.get(0));
		
		// arrange
		assertEquals("Number of datasets", 1, testdata.size());
		assertEquals("Number of datafields in dataset", 2, testdata.get(0).keySet().size());
	}

	@Test
	public void throwsExceptionForMissingTestData() throws Exception 
	{
		try {
			cut.loadTestdata("unknown");
			fail("Expected exception not thrown!");
		} catch (SysNatTestDataException e) {
			assertEquals("Error Message", "Zu 'unknown' wurden in ../sysnat.testdata.import/src/test/resources/testTestdata keine Testdaten-Dateien gefunden.", e.getMessage() );	
		}
	}

	@Test
	public void loadMultipleDatasetsFromSingleDatFile() 
	{
		// arrange
		cut.testdataId = "threeDatasets";
		
		// act
		final List<File> filesToLoad = cut.findFilesToLoad();
		final List<Properties> testdata = cut.loadDatasetsFromDatFile(filesToLoad.get(0));
		
		// arrange
		assertEquals("Number of datasets", 3, testdata.size());
		assertEquals("Number of datafields in dataset", 3, testdata.get(0).keySet().size());
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
		assertEquals("DatasetID", testdataId + "_1", key1);
		assertEquals("DatasetID", testdataId + "_2", key2);
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

		assertEquals("DatasetID", testdataId + "_1_1", key1);
		assertEquals("DatasetID", testdataId + "_1_2", key2);
		assertEquals("DatasetID", testdataId + "_2_1", key3);
		assertEquals("DatasetID", testdataId + "_2_2", key4);
		assertEquals("DatasetID", testdataId + "_3_1", key5);
		assertEquals("DatasetID", testdataId + "_3_2", key6);
		assertEquals("DatasetID", testdataId + "_3_3", key7);
	}

	@Test
	public void ignoresEmptyDatasetsInDatFiles() throws Exception 
	{
		// arrange
		cut.testdataId = "manyEmptyDatasets";
		
		// act
		final List<File> filesToLoad = cut.findFilesToLoad();
		final List<Properties> testdata = cut.loadDatasetsFromDatFile(filesToLoad.get(0));
		
		// arrange
		assertEquals("Number of datasets", 1, testdata.size());
		assertEquals("Number of datafields in dataset", 1, testdata.get(0).keySet().size());
		
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
		assertEquals("DatasetID", "BigTestDataSeriesPart1_GreetingValidationData1", keys.get(0));
		assertEquals("DatasetID", "BigTestDataSeriesPart1_GreetingValidationData2", keys.get(1));
		assertEquals("DatasetID", "BigTestDataSeriesPart1_GreetingValidationData3", keys.get(2));
		assertEquals("DatasetID", "BigTestDataSeriesPart1_GreetingValidationData4", keys.get(3));
		assertEquals("DatasetID", "BigTestDataSeriesPart2_GreetingValidationData1", keys.get(4));
		assertEquals("DatasetID", "BigTestDataSeriesPart2_GreetingValidationData2", keys.get(5));
		assertEquals("DatasetID", "BigTestDataSeriesPart2_GreetingValidationData3", keys.get(6));
		assertEquals("DatasetID", "BigTestDataSeriesPart2_GreetingValidationData4", keys.get(7));
		assertEquals("DatasetID", "BigTestDataSeriesPart3_1", keys.get(8));
		assertEquals("DatasetID", "BigTestDataSeriesPart3_2", keys.get(9));
		assertEquals("DatasetID", "BigTestDataSeriesPart3_3", keys.get(10));
		assertEquals("DatasetID", "BigTestDataSeriesPart3_4", keys.get(11));
		assertEquals("DatasetID", "BigTestDataSeriesPart4_1", keys.get(12));
		assertEquals("DatasetID", "BigTestDataSeriesPart4_2", keys.get(13));
		assertEquals("DatasetID", "BigTestDataSeriesPart4_3", keys.get(14));
		assertEquals("DatasetID", "BigTestDataSeriesPart4_4", keys.get(15));
		
		for (int i = 0; i < keys.size(); i++) {
			String message = "Number of fields in " + (i+1) + "th dataset";
			assertEquals(message, 3, result.get(keys.get(i)).size());
		}

		assertEquals("Fieldname", "Hi Stephen!", result.get(keys.get(0)).get("GreetingResult"));
		assertEquals("Fieldname", "Hello Lisa!", result.get(keys.get(5)).get("GreetingResult"));
		assertEquals("Fieldname", " Good Day Dean!", result.get(keys.get(10)).get("GreetingResult"));
		assertEquals("Fieldname", " Hey Dora!", result.get(keys.get(15)).get("GreetingResult"));
		
	}
	
}
