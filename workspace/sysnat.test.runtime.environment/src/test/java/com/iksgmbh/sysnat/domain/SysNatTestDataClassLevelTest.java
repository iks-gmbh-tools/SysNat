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
package com.iksgmbh.sysnat.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.domain.SysNatTestData.SysNatDataset;

public class SysNatTestDataClassLevelTest 
{
	private SysNatTestData cut = new SysNatTestData();
	
	@Before
	public void setup() 
	{
		final SysNatDataset dataset1 = new SysNatDataset("TestDataset_1");
		dataset1.setProperty("aKey", "aValue");
		cut.addDataset(dataset1.getName(), dataset1 );
		
		final SysNatDataset dataset2 = new SysNatDataset("TestDataset_2");
		dataset2.setProperty("firstKey", "firstValue");
		dataset2.setProperty("anotherKey", "anotherValue");
		cut.addDataset(dataset2.getName(), dataset2 );
		
		cut.addEmptyDataset("EmptyDataSet"); 
	}
	
	@Test
	public void returnsAllDatasets() {
		assertEquals("Number of datasets", 3, cut.getAllDatasets().size());
	}

	@Test
	public void retunsDatasetForType() {
		assertEquals("Number of datasets", 2, cut.findDataSets("TestDataset").size());
	}

	@Test
	public void retunsKnownDataset() {
		assertEquals("Dataset Name", "EmptyDataSet", cut.findMatchingDataSet("EmptyDataSet").getName());
	}

	@Test
	public void retunsDatasetForIndex() {
		assertEquals("Dataset Name", "EmptyDataSet", cut.getDataSetForOrderNumber(3).getName());
	}
	
	@Test
	public void throwsExceptionForAmbiguousDataset() throws Exception 
	{
		try {
			// act
			cut.getValue("aFieldName");
			fail("Expected exception not thrown!");
		} catch (Exception e) {
			// assert
			assertEquals("Error Message", "Es gibt 3 Testdatensätze. "
					     + "Von welchem wird der Wert für <b>aFieldName</b> benötigt?", 
					     e.getMessage() );	
		}
	}
	
	@Test
	public void returnsValueForFieldnameWithFullReference() throws Exception 
	{
		// act
		final String result = cut.findValueForValueReference("TestDataset_2:firstKey");
		
		// assert
		assertEquals("value of field", "firstValue", result);
	}

	@Test
	public void returnsValueForSimpleFieldnameReference() throws Exception 
	{
		// arrange
		cut.setMarker(cut.getDataSet("TestDataset_2"));
		
		// act
		final String result = cut.findValueForValueReference(":firstKey");
		
		// assert
		assertEquals("value of field", "firstValue", result);
	}
	
	
	@Test
	public void returnsValueForFieldnameWithSimpleReference() throws Exception 
	{
		// arrange
		cut = new SysNatTestData();
		final SysNatDataset dataset = new SysNatDataset("TestDataset");
		dataset.setProperty("firstKey", "firstValue");
		dataset.setProperty("anotherKey", "anotherValue");
		cut.addDataset(dataset.getName(), dataset );
		
		// act
		final String result = cut.findValueForValueReference(":firstKey");
		
		// assert
		assertEquals("value of field", "firstValue", result);
	}
	
}