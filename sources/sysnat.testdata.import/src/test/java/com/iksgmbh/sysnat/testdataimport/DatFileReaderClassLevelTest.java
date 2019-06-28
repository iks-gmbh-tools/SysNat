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
import java.util.List;
import java.util.Properties;

import org.junit.Test;

public class DatFileReaderClassLevelTest 
{
	
	@Test
	public void loadSingleDatasetFromSingleDatFiles() 
	{
		// arrange
		final File datFile = new File("../sysnat.testdata.import/src/test/resources/testTestdata/"
				+ "singleDataset.dat");
		
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
		final File datFile = new File("../sysnat.testdata.import/src/test/resources/testTestdata/"
				+ "threeDatasets.dat");
		
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
		final File datFile = new File("../sysnat.testdata.import/src/test/resources/testTestdata/"
				+ "manyEmptyDatasets.dat");
		
		// act
		final List<Properties> testdata = DatFileReader.doYourJob(datFile);
		
		// arrange
		assertEquals("Number of datasets", 1, testdata.size());
		assertEquals("Number of datafields in dataset", 1, testdata.get(0).keySet().size());
		
	}
	
	
}