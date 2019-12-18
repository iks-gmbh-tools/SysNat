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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

import org.junit.Test;

public class DatFileReaderClassLevelTest 
{
	@Test
	public void throwsErrorForProperiesWithMultipleEqualSymbols() 
	{
		// arrange
		final String path = SysNatFileUtil.findAbsoluteFilePath("../sysnat.testdata.import/src/test/resources/testTestdata/"
				+ "multipleEuqalSymbolsInPropertyValuesNOK.dat");
		final File datFile = new File(path);
		
		try {
			// act
			DatFileReader.doYourJob(datFile);
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertTrue("unexpected error message", e.getMessage().contains("Error reading property"));
			assertTrue("unexpected error message", e.getMessage().contains("resources\\testTestdata\\multipleEuqalSymbolsInPropertyValuesNOK.dat"));
			assertTrue("unexpected error message", e.getMessage().contains("line 1 \"<b>property=valueWith=MustBeSurroundedByHyphen"));
		}	
	}

	@Test
	public void loadProperiesWithMultipleEqualSymbols() 
	{
		// arrange
		final String path = SysNatFileUtil.findAbsoluteFilePath("../sysnat.testdata.import/src/test/resources/testTestdata/"
				+ "multipleEuqalSymbolsInPropertyValuesOK.dat");
		final File datFile = new File(path);
		
		// act
		final List<Properties> testdata = DatFileReader.doYourJob(datFile);
		
		// arrange
		assertEquals("Number of datasets", 1, testdata.size());
		assertEquals("Number of datafields in dataset", 2, testdata.get(0).keySet().size());
		
		List<String> keys = new ArrayList<>();
		testdata.get(0).keySet().forEach(key -> keys.add((String) key));
		assertEquals("Property Value", "valueWith=MustBeSurroundedByHyphen", testdata.get(0).get(keys.get(0)));
		assertEquals("Property Value", "valueWith=MustBeSurroundedByHyphen", testdata.get(0).get(keys.get(1)));
	}
	
	
	@Test
	public void loadSingleDatasetFromSingleDatFiles() 
	{
		// arrange
		final String path = SysNatFileUtil.findAbsoluteFilePath("../sysnat.testdata.import/src/test/resources/testTestdata/"
				+ "singleDataset.dat");
		final File datFile = new File(path);
		
		// act
		final List<Properties> testdata = DatFileReader.doYourJob(datFile);
		
		// arrange
		assertEquals("Number of datasets", 1, testdata.size());
		assertEquals("Number of datafields in dataset", 2, testdata.get(0).keySet().size());
	}


	@Test
	public void loadMultipleDatasetsFromSingleDatFile() 
	{
		// arrange
		final String path = SysNatFileUtil.findAbsoluteFilePath("../sysnat.testdata.import/src/test/resources/testTestdata/"
				+ "threeDatasets.dat");

		final File datFile = new File(path);
		
		// act
		final List<Properties> testdata = DatFileReader.doYourJob(datFile);
		
		// arrange
		assertEquals("Number of datasets", 3, testdata.size());
		assertEquals("Number of datafields in dataset", 3, testdata.get(0).keySet().size());
	}



	@Test
	public void ignoresEmptyDatasetsInDatFiles() throws Exception 
	{
		// arrange
		final String path = SysNatFileUtil.findAbsoluteFilePath("../sysnat.testdata.import/src/test/resources/testTestdata/"
				+ "manyEmptyDatasets.dat");
		final File datFile = new File(path);
		
		// act
		final List<Properties> testdata = DatFileReader.doYourJob(datFile);
		
		// arrange
		assertEquals("Number of datasets", 1, testdata.size());
		assertEquals("Number of datafields in dataset", 1, testdata.get(0).keySet().size());
		
	}
	
	
}