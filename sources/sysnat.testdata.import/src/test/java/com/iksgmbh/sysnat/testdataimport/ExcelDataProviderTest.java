package com.iksgmbh.sysnat.testdataimport;

import static org.junit.Assert.*;

import java.awt.Point;

import org.junit.Test;

public class ExcelDataProviderTest
{

	@Test
	public void translatesCellNameInCoordinates()
	{
		Point result1 = ExcelDataProvider.toCoordinates("A1");
		Point result2 = ExcelDataProvider.toCoordinates("Z26");
		Point result3 = ExcelDataProvider.toCoordinates("ZZA126");
		
		assertEquals("X-Coordinate", 1, result1.x);
		assertEquals("Y-Coordinate", 1, result1.y);
		assertEquals("X-Coordinate", 26, result2.x);
		assertEquals("Y-Coordinate", 26, result2.y);
		assertEquals("X-Coordinate", 53, result3.x);
		assertEquals("Y-Coordinate", 126, result3.y);
	}

	@Test
	public void throwsExceptionForMissingColumnNumber()
	{
		try {
			ExcelDataProvider.toCoordinates("123");
			fail("Expected exception not thrown!");
		} catch (Exception e) {
			// assert
			assertEquals("Error Message", "The Excel cell definition 123 contains no valid column identifier.", e.getMessage() );	
		}		
	}

	
	@Test
	public void throwsExceptionForMissingRowNumber()
	{
		try {
			ExcelDataProvider.toCoordinates("AA");
			fail("Expected exception not thrown!");
		} catch (Exception e) {
			// assert
			assertEquals("Error Message", "The Excel cell definition AA contains no valid row number.", e.getMessage() );	
		}		
	}

	@Test
	public void throwsExceptionForInvalidCellName()
	{
		try {
			ExcelDataProvider.toCoordinates("+++");
			fail("Expected exception not thrown!");
		} catch (Exception e) {
			// assert
			assertEquals("Error Message", "The Excel cell definition +++ is invalid.", e.getMessage() );	
		}		
	}
	
}
