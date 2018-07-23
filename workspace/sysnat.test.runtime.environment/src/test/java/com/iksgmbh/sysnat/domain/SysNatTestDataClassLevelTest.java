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
		
		cut.addEmptyObjectData("EmptyDataSet");
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
		assertEquals("Dataset Name", "EmptyDataSet", cut.getDataSetForName("EmptyDataSet").getName());
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
		final String result = cut.getValueFor("TestDataset_2:firstKey");
		
		// assert
		assertEquals("value of field", "firstValue", result);
	}

	@Test
	public void returnsValueForFieldnameWithSimpleReference() throws Exception 
	{
		// arrange
		cut = new SysNatTestData();
		final SysNatDataset dataset2 = new SysNatDataset("TestDataset_2");
		dataset2.setProperty("firstKey", "firstValue");
		dataset2.setProperty("anotherKey", "anotherValue");
		cut.addDataset(dataset2.getName(), dataset2 );
		
		// act
		final String result = cut.getValueFor(":firstKey");
		
		// assert
		assertEquals("value of field", "firstValue", result);
	}
	
}
