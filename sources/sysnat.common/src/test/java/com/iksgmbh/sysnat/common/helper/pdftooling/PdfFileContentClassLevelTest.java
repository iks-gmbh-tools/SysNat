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
	private PdfFileContent cut = new PdfFileContent("../sysnat.common/src/test/resources/PdfPageContentTestData/Pdf0.pdf");
	
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

    @Test
    public void appliesAllAnalysisMethodOfCUT() throws Exception
    {
    	cut = new PdfFileContent("../sysnat.common/src/test/resources/PdfPageContentTestData/IKS-Software-zum-Anfassen-Gibt-es-so-etwas.pdf");
    	
        assertEquals("Page Number", 8, cut.getPageNumber() );
        assertEquals("Page Number", 8, cut.findPageByTextIdentifier("Fazit") );

        assertFalse("Unexpected content.", cut.doesFileContain("Dieser Text darf nicht vorhanden sein.") );
        assertTrue("Unexpected content", cut.doesFileContain("Softwarequalität") );


        assertTrue("Unexpected Line", cut.doesLineEquals(1, 1, "Softwarequalität zum Anfassen: Gibt es so etwas?") );
        assertTrue("Unexpected Line", cut.doesLineEquals("Was ist Softwarequalität?", 4, "Gibt es so etwas?") );

        assertTrue("Unexpected Line", cut.doesLineContain(1, 2, "clean-coding-cosmos") );
        assertTrue("Unexpected Line", cut.doesLineContain("Was ist Softwarequalität?", 2, "clean-coding-cosmos") );

        assertFalse("Unexpected Page", cut.doesPageContain(2, "Abb. 4") );
        assertTrue("Unexpected Page", cut.doesPageContain(2, "Abb. 3") );
        assertTrue("Unexpected Page", cut.doesPageContain("Literatur & Links", "http://clean-coding-cosmos.de/der-entwicklerkosmos/softwarequalitaet-1") );
        assertTrue("Unexpected Page", cut.doesPageContain("Literatur & Links", "http://clean-coding-cosmos.de/der-entwicklerkosmos/softwarequalitaet-4") );

        assertTrue("Unexpected Page", cut.doesPageContainIgnoreWhiteSpace(3, "DasQualitätsmodell    in   Abbildung 3") );
        assertTrue("Unexpected Page", cut.doesPageContainIgnoreWhiteSpace("Tabelle 1:", "DasQualitätsmodell    in   Abbildung 3") );

        assertEquals("Line Number", 60, cut.getLinesOfPage("Abb. 4:").size() );
        assertEquals("Line Number", 263, cut.getLinesOfPage(4).size() );
    }
	
}
