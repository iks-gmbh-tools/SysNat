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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.iksgmbh.sysnat.common.helper.pdftooling.PdfComparer;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

@Ignore
public class PdfComparerClassLevelTest 
{
	private PdfComparer cut = new PdfComparer("../sysnat.common/src/test/resources/PdfAnalyserTest/Pdf0.pdf");
	
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
	public void comparesSimplePdfFiles() throws Exception 
	{
		final String[] pdfs = { "../sysnat.common/src/test/resources/PdfAnalyserTest/Pdf0.pdf",
								"../sysnat.common/src/test/resources/PdfAnalyserTest/Pdf1.pdf",
								"../sysnat.common/src/test/resources/PdfAnalyserTest/Pdf2.pdf",
								"../sysnat.common/src/test/resources/PdfAnalyserTest/Pdf3.pdf",
								"../sysnat.common/src/test/resources/PdfAnalyserTest/Pdf4.pdf",
								"../sysnat.common/src/test/resources/PdfAnalyserTest/Pdf5.pdf",
								"../sysnat.common/src/test/resources/PdfAnalyserTest/Pdf6.pdf" };
		
		cut = new PdfComparer( pdfs[1] );
		assertTrue("File contents of first two Pdfs are not equal.", cut.contentEquals(pdfs[1]));
		
		for (int i = 2; i < pdfs.length; i++) {
			assertFalse("File contents of first and " + i + "th are not equal.", cut.contentEquals(pdfs[i]));
		}
	}
	
	@Test
	public void comparesCivPdfFiles() throws Exception
	{
		cut = new PdfComparer( "../sysnat.common/src/test/resources/PdfAnalyserTest/CIV05_Privat_Antrag_Vertrag_01129750_Karaagacli_Hamit_013040280.pdf" );
		assertTrue("File contents of first two Pdfs are not equal.", cut.contentEquals("../sysnat.common/src/test/resources/PdfAnalyserTest/CIV05_Privat_Antrag_Vertrag_01129750_Karaagacli_Hamit_013040280.pdf"));
		assertFalse("File contents of first two Pdfs are not equal.", cut.contentEquals("../sysnat.common/src/test/resources/PdfAnalyserTest/CIV05_Privat_Antrag_Vertrag_01129751_Karaagacli_Hamit_013040280.pdf"));
	}

	@Test
	public void returnsPageWiseDifferences() throws Exception
	{
		cut = new PdfComparer( "../sysnat.common/src/test/resources/PdfAnalyserTest/CIV05_Privat_Antrag_Vertrag_01129750_Karaagacli_Hamit_013040280.pdf" );
		String result = cut.getDifferingPagesAsString("../sysnat.common/src/test/resources/PdfAnalyserTest/CIV05_Privat_Antrag_Vertrag_01129751_Karaagacli_Hamit_013040280.pdf");
		assertEquals("Differences between PDFs", "PDFs unterscheiden sich auf den Seiten 1, 3, 4, 5, 7, 13, 21, 22, 24, 27, 29, 30, 31, 33, 35, 37 und 39.", result);
	}

	@Test
	public void returnsMessageForDifferentPageNumbers() throws Exception
	{
		String result = cut.getDifferingPagesAsString("../sysnat.common/src/test/resources/PdfAnalyserTest/CIV05_Privat_Antrag_Vertrag_01129751_Karaagacli_Hamit_013040280.pdf");
		assertEquals("Differences between PDFs", "Die PDFs unterscheiden sich in der Seitenzahl.", result);
	}

	@Test
	public void returnsFirstDifference() throws Exception
	{
		cut = new PdfComparer( "../sysnat.common/src/test/resources/PdfAnalyserTest/CIV05_Privat_Antrag_Vertrag_01129750_Karaagacli_Hamit_013040280.pdf" );
		String result = cut.getFirstDifference("../sysnat.common/src/test/resources/PdfAnalyserTest/CIV05_Privat_Antrag_Vertrag_01129751_Karaagacli_Hamit_013040280.pdf");
		assertEquals("First difference between PDFs", "On page 1 is '1129751' not equal to '1129750'.", result);
	}

	@Test
	public void returnsAllDifferingWords() throws Exception
	{
		cut = new PdfComparer( "../sysnat.common/src/test/resources/PdfAnalyserTest/CIV05_Privat_Antrag_Vertrag_01129750_Karaagacli_Hamit_013040280.pdf" );
		List<String> result = cut.getAllDifferingWordsOnPage(1, "../sysnat.common/src/test/resources/PdfAnalyserTest/CIV05_Privat_Antrag_Vertrag_01129751_Karaagacli_Hamit_013040280.pdf");
		
		assertEquals("On page 1 is '1129751' not equal to '1129750'.", result.get(0));
		assertEquals("On page 1 is '77,61' not equal to '77,59'.", result.get(1));
		assertEquals("On page 1 is '149,10' not equal to '149,05'.", result.get(2));
		assertEquals("On page 1 is '175,55' not equal to '175,49'.", result.get(3));
		assertEquals("On page 1 is '29.709' not equal to '29.700'.", result.get(4));
		assertEquals("On page 1 is '48.268' not equal to '48.250'.", result.get(5));
		assertEquals("On page 1 is '125,04' not equal to '125,00'.", result.get(6)); 		
	}

	@Test
	public void returnsAllDifferingLines() throws Exception
	{
		// arrange
		cut = new PdfComparer( "../sysnat.common/src/test/resources/PdfAnalyserTest/CIV05_Privat_Antrag_Vertrag_01129750_Karaagacli_Hamit_013040280.pdf" );
		
		// act
		List<String> result = cut.getDifferingLinesOnPage("../sysnat.common/src/test/resources/PdfAnalyserTest/CIV05_Privat_Antrag_Vertrag_01129751_Karaagacli_Hamit_013040280.pdf", 
				                                          3, new PdfCompareIgnoreConfig(null, null, null, null));
		
		// assert
		assertEquals("Seite 3, Zeile 3: [Antragsnummer 1129750 ] # [Antragsnummer 1129751 ]", result.get(0)); 
		assertEquals("Seite 3, Zeile 14: [   lebenslange Garantierente 77,59 EUR] # [   lebenslange Garantierente 77,61 EUR]", result.get(1));
		assertEquals("Seite 3, Zeile 18: [   Garantiekapital 29.700 EUR] # [   Garantiekapital 29.709 EUR]", result.get(2));
		assertEquals("Seite 3, Zeile 24: [Beitrag monatlich 125,00 EUR ] # [Beitrag monatlich 125,04 EUR ]", result.get(3));
	}

	@Test
	public void returnsDifferingLines_with_UnequalLineNumbers() throws Exception
	{
		cut = new PdfComparer( "../sysnat.common/src/test/resources/PdfAnalyserTest/CIV05_Privat_Antrag_Vertrag_01129750_Karaagacli_Hamit_013040280.pdf" );
		final List<String> linesA = new ArrayList<>();
		linesA.add("Line1");
		linesA.add("Line2a");
		linesA.add("Line3");
		final List<String> linesB = new ArrayList<>();
		linesB.add("Line1");
		linesB.add("Line2b");
		linesB.add("Line3");
		linesB.add("Line4");
		
		final List<String> result = cut.getDifferingLines(linesA, linesB, 1);
		assertEquals("Number of differing lines", 2, result.size());
		assertEquals("First Difference", "Seite 1, Zeile 2: [Line2a] # [Line2b]", result.get(0)); 
		assertEquals("Second Difference", "Seite 1, Zeile 4: [] # [Line4]", result.get(1)); 
	}

	@Test
	public void returnsDifferingLines_with_EmptyPage() throws Exception
	{
		cut = new PdfComparer( "../sysnat.common/src/test/resources/PdfAnalyserTest/CIV05_Privat_Antrag_Vertrag_01129750_Karaagacli_Hamit_013040280.pdf" );
		final List<String> linesA = new ArrayList<>();
		linesA.add("Line1");
		linesA.add("Line2");
		final List<String> linesB = new ArrayList<>();
		
		final List<String> result = cut.getDifferingLines(linesA, linesB, 1);
		assertEquals("Number of differing lines", 2, result.size());
		assertEquals("First Difference", "Seite 1, Zeile 1: [Line1] # []", result.get(0)); 
		assertEquals("Second Difference", "Seite 1, Zeile 2: [Line2] # []", result.get(1)); 
	}
	
	
	@Test
	public void returnsFullDifferenceReport() throws Exception
	{
		// arrange
		cut = new PdfComparer( "../sysnat.common/src/test/resources/PdfAnalyserTest/CIV05_Privat_Antrag_Vertrag_01129750_Karaagacli_Hamit_013040280.pdf" );
		
		// act
		String result = cut.getFullDifferenceReport("../sysnat.common/src/test/resources/PdfAnalyserTest/CIV05_Privat_Antrag_Vertrag_01129751_Karaagacli_Hamit_013040280.pdf");
		
		// assert
		String expectedFileContent = 
				SysNatFileUtil.readTextFileToString(
						"../sysnat.common/src/test/resources/PdfAnalyserTest/expectedDifferenceReport.txt");
		assertEquals("Difference report", expectedFileContent, result);
	}

	@Test
	public void returnsDifferenceReport_IgnoreWithPrefixes() throws Exception
	{
		// arrange
		cut = new PdfComparer( "../sysnat.common/src/test/resources/PdfAnalyserTest/CIV05_Privat_Antrag_Vertrag_01129750_Karaagacli_Hamit_013040280.pdf" );
		final List<String> linePrefixesToIgnore = new ArrayList<String>();
		linePrefixesToIgnore.add("Seite 1");
		linePrefixesToIgnore.add("Seite 3, Zeile 3:");
		PdfCompareIgnoreConfig ignoreConfig = new PdfCompareIgnoreConfig(null, null, linePrefixesToIgnore, null);

		// act
		String result = cut.getDifferenceReport("../sysnat.common/src/test/resources/PdfAnalyserTest/CIV05_Privat_Antrag_Vertrag_01129751_Karaagacli_Hamit_013040280.pdf",
				                                ignoreConfig);
		
		// assert
		String expectedFileContent = 
				SysNatFileUtil.readTextFileToString(
						"../sysnat.common/src/test/resources/PdfAnalyserTest/expectedBusinessDifferenceReport.txt");
		assertEquals("Difference report", expectedFileContent, result);

	}

	@Test
	public void parsesPageNumberFromLine() throws Exception
	{
		Integer result = cut.getPageNumberFromLine("Seite 3: abc");
		assertEquals("Page number", 3, result.intValue());
		
		result = cut.getPageNumberFromLine("Seite 2, Zeile 3: abc");
		assertEquals("Page number", 2, result.intValue());
	}
	
}