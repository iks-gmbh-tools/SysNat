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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

/**
 * Tests in this class actually test the PDFComparer class in combination with the PdfCompareIgnoreConfig class. 
 */
public class PdfFileComparerClassLevelTest 
{
	private PdfFileComparer cut = new PdfFileComparer("../sysnat.common/src/test/resources/PdfAnalyserTest/Pdf0.pdf");
	
	@Test
	public void analysesPdfWithoutWhitespace() throws Exception 
	{
		assertEquals("Page Number", 3, cut.getPageNumber() );	
		
		assertTrue( cut.doesPageContainIgnoreWhiteSpace(2, "Page B") );
		assertFalse( cut.doesPageContainIgnoreWhiteSpace(2, "linebreak") );
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
		
		cut = new PdfFileComparer( pdfs[1] );
		assertTrue("File contents of first two Pdfs are not equal.", cut.isContentIdenticalTo(pdfs[1]));
		
		for (int i = 2; i < pdfs.length; i++) {
			assertFalse("File contents of first and " + i + "th are not equal.", cut.isContentIdenticalTo(pdfs[i]));
		}
	}

	@Test
	public void returnsPageWiseDifferences() throws Exception
	{
		cut = new PdfFileComparer( "../sysnat.common/src/test/resources/PdfAnalyserTest/PDF_A.pdf" );
		String result = cut.getDifferingPagesAsString("../sysnat.common/src/test/resources/PdfAnalyserTest/PDF_B.pdf");
		assertEquals("Differences between PDFs", "PDFs unterscheiden sich auf den Seiten 2 und 3.", result);
	}

	@Test
	public void returnsMessageForDifferentPageNumbers() throws Exception
	{
		String result = cut.getDifferingPagesAsString("../sysnat.common/src/test/resources/PdfAnalyserTest/PDF_A.pdf");
		assertEquals("Differences between PDFs", "Die PDFs unterscheiden sich in der Seitenzahl.", result);
	}

	@Test
	public void returnsFirstDifference() throws Exception
	{
		cut = new PdfFileComparer( "../sysnat.common/src/test/resources/PdfAnalyserTest/PDF_A.pdf" );
		String result = cut.getFirstDifference("../sysnat.common/src/test/resources/PdfAnalyserTest/PDF_B.pdf");
		assertEquals("First difference between PDFs", "Seite 2, Zeile 2: [Two] # [2]", result);
	}

	@Test
	public void returnsAllDifferingWords() throws Exception
	{
		cut = new PdfFileComparer( "../sysnat.common/src/test/resources/PdfAnalyserTest/PDF_A.pdf" );
		List<String> result = cut.getAllDifferingWordsOnPage(2, "../sysnat.common/src/test/resources/PdfAnalyserTest/PDF_B.pdf");
		
		assertEquals("On page 2 is 'Two' not equal to '2'.", result.get(0));
		assertEquals("On page 2 is 'Five' not equal to '5'.", result.get(1));
	}

	@Test
	public void returnsAllDifferingLinesOnPage() throws Exception
	{
		// arrange
		cut = new PdfFileComparer( "../sysnat.common/src/test/resources/PdfAnalyserTest/PDF_A.pdf" );
		
		// act
		List<String> result = cut.getDifferingLinesOnPage("../sysnat.common/src/test/resources/PdfAnalyserTest/PDF_B.pdf", 
				                                          2, new PdfCompareIgnoreConfig());
		
		// assert
		assertEquals("Number of differing lines", 2, result.size()); 
		assertEquals("Seite 2, Zeile 2: [Two] # [2]", result.get(0)); 
		assertEquals("Seite 2, Zeile 5: [Five] # [5]", result.get(1));
	}

	@Test
	public void returnsDifferingLines_with_UnequalLineNumbers() throws Exception
	{
		int pageNo = 1;
		PdfPageContent pageContent1 = new PdfPageContent(pageNo);
		final List<String> linesA = new ArrayList<>();
		linesA.add("Line1");
		linesA.add("Line2a");
		linesA.add("Line3");
		pageContent1.addLines(linesA);
		
		PdfPageContent pageContent2 = new PdfPageContent(pageNo);
		final List<String> linesB = new ArrayList<>();
		linesB.add("Line1");
		linesB.add("Line2b");
		linesB.add("Line3");
		linesB.add("Line4");
		pageContent2.addLines(linesB);
		
		final List<String> result = cut.getDifferenceList(pageContent1, pageContent2, pageNo);
		assertEquals("Number of differing lines", 2, result.size());
		assertEquals("First Difference", "Seite 1, Zeile 2: [Line2a] # [Line2b]", result.get(0)); 
		assertEquals("Second Difference", "Seite 1, Zeile 4: [] # [Line4]", result.get(1)); 
	}

	@Test
	public void returnsDifferingLines_with_EmptyPage() throws Exception
	{
		int pageNo = 1;
		PdfPageContent pageContent1 = new PdfPageContent(pageNo);
		final List<String> linesA = new ArrayList<>();
		linesA.add("Line1");
		linesA.add("Line2");
		pageContent1.addLines(linesA);

		PdfPageContent pageContent2 = new PdfPageContent(pageNo);
		
		final List<String> result = cut.getDifferenceList(pageContent1, pageContent2, pageNo);
		assertEquals("Number of differing lines", 2, result.size());
		assertEquals("First Difference", "Seite 1, Zeile 1: [Line1] # []", result.get(0)); 
		assertEquals("Second Difference", "Seite 1, Zeile 2: [Line2] # []", result.get(1)); 
	}
	
	
	@Test
	public void returnsFullDifferenceReport() throws Exception
	{
		// arrange
		cut = new PdfFileComparer( "../sysnat.common/src/test/resources/PdfAnalyserTest/PDF_A.pdf" );
		
		// act
		String result = cut.getFullDifferenceReport("../sysnat.common/src/test/resources/PdfAnalyserTest/PDF_B.pdf");
		
		// assert
		String expectedFileContent = SysNatFileUtil.readTextFileToString(
						"../sysnat.common/src/test/resources/PdfAnalyserTest/expectedDifferenceReport.txt");
		assertEquals("Difference report", expectedFileContent, result);
	}

	@Test
	public void returnsDifferenceReport_IgnoreWithDateAndRegex() throws Exception
	{
		// arrange
		cut = new PdfFileComparer( "../sysnat.common/src/test/resources/PdfAnalyserTest/PDF_C.pdf" );
		final List<DateFormat> dateFormatsToIgnore = new ArrayList<>();
		dateFormatsToIgnore.add(new SimpleDateFormat("MM-yyyy"));
		dateFormatsToIgnore.add(DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMAN));
		final List<String> regexToIgnore = new ArrayList<>();
		regexToIgnore.add("[0-9]{3}");
		regexToIgnore.add("X{2,255}");
		PdfCompareIgnoreConfig ignoreConfig = new PdfCompareIgnoreConfig().withDateformats(dateFormatsToIgnore)
				                                                          .withRegexPatterns(regexToIgnore);

		// act
		String result = cut.getDifferenceReport("../sysnat.common/src/test/resources/PdfAnalyserTest/PDF_D.pdf",
				                                ignoreConfig);
		
		// assert
		if (! result.isEmpty()) {
			System.out.println(result);
		}
		assertTrue("Difference report is not empty!", result.isEmpty());
	}
	
	@Test
	public void returnsDifferenceReport_IgnoreWithLineDefinitions() throws Exception
	{
		// arrange
		cut = new PdfFileComparer( "../sysnat.common/src/test/resources/PdfAnalyserTest/PDF_C.pdf" );
		final List<String> lineDefinitionsToIgnore = new ArrayList<>();
		lineDefinitionsToIgnore.add("Seite 1, Zeile 1");
		lineDefinitionsToIgnore.add("Seite 1, Zeile 2");
		lineDefinitionsToIgnore.add("Seite 1, Zeile 3");
		lineDefinitionsToIgnore.add("Seite 1, Zeile 4");
		lineDefinitionsToIgnore.add("Seite 1, Zeile 5");
		lineDefinitionsToIgnore.add("Seite 2, Zeile 6");  // this line must not have an effect !
		PdfCompareIgnoreConfig ignoreConfig = new PdfCompareIgnoreConfig().withLineDefinitions(lineDefinitionsToIgnore);

		// act
		String result = cut.getDifferenceReport("../sysnat.common/src/test/resources/PdfAnalyserTest/PDF_D.pdf",
				                                ignoreConfig);
		
		// assert
		String expected = "Seite 1, Zeile 6: [321] # [999]";
		if (! result.endsWith(expected)) {
			System.out.println(result);
		}
		assertTrue("Unexpected difference!", result.endsWith(expected));
	}

	
	@Test
	public void returnsDifferenceReport_IgnoreWithPrefixesAndSubstrings() throws Exception
	{
		// arrange
		cut = new PdfFileComparer( "../sysnat.common/src/test/resources/PdfAnalyserTest/PDF_A.pdf" );
		final List<String> linePrefixesToIgnore = new ArrayList<>();
		linePrefixesToIgnore.add("Gree");
		linePrefixesToIgnore.add("5");
		final List<String> substringsToIgnore = new ArrayList<>();
		substringsToIgnore.add("iv");
		substringsToIgnore.add("Two");
		substringsToIgnore.add("2");
		PdfCompareIgnoreConfig ignoreConfig = new PdfCompareIgnoreConfig().withSubstrings(substringsToIgnore)
				                                                          .withPrefixes(linePrefixesToIgnore);

		// act
		String result = cut.getDifferenceReport("../sysnat.common/src/test/resources/PdfAnalyserTest/PDF_B.pdf",
				                                ignoreConfig);
		
		// assert
		if (! result.isEmpty()) {
			System.out.println(result);
		}
		assertTrue("Difference report is not empty!", result.isEmpty());
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