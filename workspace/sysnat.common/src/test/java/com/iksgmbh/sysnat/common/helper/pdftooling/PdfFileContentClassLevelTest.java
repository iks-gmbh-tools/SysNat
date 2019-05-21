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
package com.iksgmbh.sysnat.common.helper.pdftooling;

import static org.junit.Assert.*;

import org.junit.Test;

public class PdfFileContentClassLevelTest 
{
	private PdfFileContent cut = new PdfFileContent("../sysnat.common/src/test/resources/PdfAnalyserTest/Pdf0.pdf");
	
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
	public void throwsExceptionForWrongPageNumber() throws Exception 
	{
		try {
			cut.doesPageContain(0, "Page");
			fail("Expected exception not thrown!");
		} catch (Exception e) {
			assertEquals("Error Message", "Page number 0 out of range!", e.getMessage() );	
		}
		
		try {
			cut.doesPageContain(4, "1");
			fail("Expected exception not thrown!");
		} catch (Exception e) {
			assertEquals("Error Message", "Page number 4 out of range!", e.getMessage() );	
		}
	}

}
