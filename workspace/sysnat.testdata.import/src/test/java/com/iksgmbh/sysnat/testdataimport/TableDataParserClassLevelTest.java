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
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import com.iksgmbh.sysnat.common.exception.SysNatException;

/**
 * Calls DatFileReader to test TableDataParser.
 * 
 * @author Reik Oberrath
 */
public class TableDataParserClassLevelTest 
{

	@Test
	public void throwsExceptionForUnkownTableFormat() 
	{
		// arrange
		final File datTableFile = new File("../sysnat.testdata.import/src/test/resources/testTestdata/datTables/"
				+ "UnknownFormatDataTable.dat");
		
		try {
			// act
			DatFileReader.doYourJob(datTableFile);
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", "Unknown Table Data Format '?. Use columns or rows.", e.getMessage());
		}
		
	}

	@Test
	public void throwsExceptionForIncompleteDataTable() 
	{
		// arrange
		final File datTableFile = new File("../sysnat.testdata.import/src/test/resources/testTestdata/datTables/"
				+ "IncompleteDataTable.dat");
		
		try {
			// act
			DatFileReader.doYourJob(datTableFile);
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertTrue("unexpected error message", e.getMessage().endsWith("not complete!"));
		}
		
	}
	
	@Test
	public void throwsExceptionForMissingCellSeparator() 
	{
		// arrange
		final File datTableFile = new File("../sysnat.testdata.import/src/test/resources/testTestdata/datTables/"
				+ "MissingCellSeparatorDataTable.dat");
		
		try {
			// act
			DatFileReader.doYourJob(datTableFile);
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertTrue("unexpected error message", e.getMessage().endsWith("not complete!"));
		}
		
	}

	
	@Test
	public void loadDatasetFromVerticalDatTable() 
	{
		// arrange
		final File datTableFile = new File("../sysnat.testdata.import/src/test/resources/testTestdata/datTables/VerticalDataTable.dat");
		
		// act
		final List<Properties> testdata = DatFileReader.doYourJob(datTableFile);
		
		// arrange
		assertEquals("Number of datasets", 4, testdata.size());
		assertEquals("Number of datafields in dataset", 3, testdata.get(0).keySet().size());
	}

	@Test
	public void loadDatasetFromHorizontalDatTable() 
	{
		// arrange
		final File datTableFile = new File("../sysnat.testdata.import/src/test/resources/testTestdata/datTables/HoritontalDataTable.dat");
		
		// act
		final List<Properties> testdata = DatFileReader.doYourJob(datTableFile);
		
		// arrange
		assertEquals("Number of datasets", 4, testdata.size());
		assertEquals("Number of datafields in dataset", 3, testdata.get(0).keySet().size());
	}
	
	
}