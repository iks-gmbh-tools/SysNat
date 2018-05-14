package com.iksgmbh.sysnat.helper;

import static org.junit.Assert.*;

import org.junit.Test;

public class PdfAnalyserTest 
{
	private PdfAnalyser cut = new PdfAnalyser("resources/PdfAnalyserTest.pdf");
	
	@Test
	public void analysesPdfWithWhitespace() throws Exception 
	{
		assertEquals("Page Number", 3, cut.getPageNumber() );

		assertTrue( cut.doesPageContain(1, "1") );
		assertFalse( cut.doesPageContain(1, "X") );
		
		assertTrue( cut.doesPageContain(2, "Page B") );
		assertFalse( cut.doesPageContain(2, "linebreak") );
		
		assertTrue( cut.doesPageContain(3, "III") );
		assertFalse( cut.doesPageContain(3, "Y") );
	}
	
	@Test
	public void analysesPdfWithoutWhitespace() throws Exception 
	{
		assertEquals("Page Number", 3, cut.getPageNumber() );	
		
		assertTrue( cut.doesPageContainIgnoreWhiteSpace(2, "Page B") );
		assertFalse( cut.doesPageContainIgnoreWhiteSpace(2, "linebreak") );
	}
	
	@Test
	public void throwsExceptionForWrongPageBumber() throws Exception 
	{
		try {
			cut.doesPageContain(0, "1");
			fail("Expected message not found!");
		} catch (Exception e) {
			assertEquals("Error Message", "Page number 0 out of range!", e.getMessage() );	
		}
		
		try {
			cut.doesPageContain(4, "1");
			fail("Expected message not found!");
		} catch (Exception e) {
			assertEquals("Error Message", "Page number 4 out of range!", e.getMessage() );	
	}

		
	}
}
