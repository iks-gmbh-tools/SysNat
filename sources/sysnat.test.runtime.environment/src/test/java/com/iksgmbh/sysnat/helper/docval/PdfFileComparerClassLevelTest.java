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
package com.iksgmbh.sysnat.helper.docval;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.helper.docval.domain.DocumentCompareIgnoreConfig;
import com.iksgmbh.sysnat.helper.docval.domain.PageContent;

/**
 * Tests in this class actually test the PDFComparer class in combination with the PdfCompareIgnoreConfig class. 
 */
public class PdfFileComparerClassLevelTest 
{
	private static final String TEST_DATA_DIR = "../sysnat.test.runtime.environment/src/test/resources/PdfPageContentTestData";
	
	private DocumentComparer cut = new DocumentComparer(TEST_DATA_DIR + "/Pdf0.pdf");
	
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
		final String[] pdfs = { TEST_DATA_DIR + "/Pdf0.pdf",
								TEST_DATA_DIR + "/Pdf1.pdf",
								TEST_DATA_DIR + "/Pdf2.pdf",
								TEST_DATA_DIR + "/Pdf3.pdf",
								TEST_DATA_DIR + "/Pdf4.pdf",
								TEST_DATA_DIR + "/Pdf5.pdf",
								TEST_DATA_DIR + "/Pdf6.pdf" };
		
		cut = new DocumentComparer( pdfs[1] );
		assertTrue("File contents of first two Pdfs are not equal.", cut.isContentIdenticalTo(pdfs[1]));
		
		for (int i = 2; i < pdfs.length; i++) {
			assertFalse("File contents of first and " + i + "th are not equal.", cut.isContentIdenticalTo(pdfs[i]));
		}
	}

	@Test
	public void returnsPageWiseDifferences() throws Exception
	{
		cut = new DocumentComparer( TEST_DATA_DIR + "/PDF_A.pdf" );
		String result = cut.getDifferingPagesAsString(TEST_DATA_DIR + "/PDF_B.pdf");
		assertEquals("Differences between PDFs", "The documents differ on pages 2 and 3.", result);
	}

	@Test
	public void returnsMessageForDifferentPageNumbers() throws Exception
	{
		String result = cut.getDifferingPagesAsString(TEST_DATA_DIR + "/PDF_A.pdf");
		assertEquals("Differences between PDFs", "The documents differ in their page numbers.", result);
	}

	@Test
	public void returnsFirstDifference() throws Exception
	{
		cut = new DocumentComparer( TEST_DATA_DIR + "/PDF_A.pdf" );
		String result = cut.getFirstDifference(TEST_DATA_DIR + "/PDF_B.pdf");
		assertEquals("First difference between PDFs", 
				     "1. Difference:" 
		             + System.getProperty("line.separator")  
		             + "Doc1, Page 2, Line 2 : [Two]" 
		             + System.getProperty("line.separator")
				     + "Doc2, Page 2, Line 2 : [2]", result);
	}

	@Test
	public void returnsAllDifferingWords() throws Exception
	{
		cut = new DocumentComparer( TEST_DATA_DIR + "/PDF_A.pdf" );
		List<String> result = cut.getAllDifferingWordsOnPage(2, TEST_DATA_DIR + "/PDF_B.pdf");
		
		assertEquals("On page 2 is 'Two' not equal to '2'.", result.get(0));
		assertEquals("On page 2 is 'Five' not equal to '5'.", result.get(1));
	}

	@Test
	public void returnsAllDifferingLinesOnPage() throws Exception
	{
		// arrange
		cut = new DocumentComparer( TEST_DATA_DIR + "/PDF_A.pdf" );
		
		// act
		List<String> result = cut.getDifferingLinesOnPage(TEST_DATA_DIR + "/PDF_B.pdf", 
				                                          2, new DocumentCompareIgnoreConfig());
		
		// assert
		assertEquals("Number of differing lines", 4, result.size()); 
		assertEquals("1. Difference:", result.get(0)); 
		assertEquals("Doc1, Page 2, Line 2 : [Two]" + System.getProperty("line.separator") +
				     "Doc2, Page 2, Line 2 : [2]", result.get(1));
		assertEquals("2. Difference:", result.get(2)); 
		assertEquals("Doc1, Page 2, Line 5 : [Five]" + System.getProperty("line.separator") +
			         "Doc2, Page 2, Line 5 : [5]", result.get(3));
	}

	@Test
	public void returnsDifferingLines_with_UnequalLineNumbers() throws Exception
	{
		int pageNo = 1;
		PageContent pageContent1 = new PageContent(pageNo);
		final List<String> linesA = new ArrayList<>();
		linesA.add("Line1");
		linesA.add("Line2a");
		linesA.add("Line3");
		pageContent1.addLines(linesA);
		
		PageContent pageContent2 = new PageContent(pageNo);
		final List<String> linesB = new ArrayList<>();
		linesB.add("Line1");
		linesB.add("Line2b");
		linesB.add("Line3");
		linesB.add("Line4");
		pageContent2.addLines(linesB);
		
		final List<String> result = cut.getDifferenceList(pageContent1, pageContent2, pageNo);
		assertEquals("Number of differing lines", 4, result.size());
		assertEquals("First Difference", "Doc1, Page 1, Line 2 : [Line2a]" + 
		                                 System.getProperty("line.separator") + 
				                         "Doc2, Page 1, Line 2 : [Line2b]", result.get(1)); 
		assertEquals("Second Difference", "Doc1, Page 1, Line none : -" +
				                          System.getProperty("line.separator") +
				                          "Doc2, Page 1, Line 4 : [Line4]", result.get(3)); 
	}

	@Test
	public void returnsDifferingLines_with_EmptyPage() throws Exception
	{
		int pageNo = 1;
		PageContent pageContent1 = new PageContent(pageNo);
		final List<String> linesA = new ArrayList<>();
		linesA.add("Line1");
		linesA.add("Line2");
		pageContent1.addLines(linesA);

		PageContent pageContent2 = new PageContent(pageNo);

		final List<String> result = cut.getDifferenceList(pageContent1, pageContent2, pageNo);
		assertEquals("Number of differing lines", 4, result.size());
		assertEquals("First Difference Line", "1. Difference:", result.get(0)); 
		assertEquals("Second Difference Line", 
				     "Doc1, Page 1, Line 1 : [Line1]" + System.getProperty("line.separator") +
				     "Doc2, Page 1, Line none : -", result.get(1)); 
		assertEquals("First Difference Line", "2. Difference:", result.get(2)); 
		assertEquals("Second Difference Line", 
				     "Doc1, Page 1, Line 2 : [Line2]" + System.getProperty("line.separator") +
				     "Doc2, Page 1, Line none : -", result.get(3)); 
	}
	
	
	@Test
	public void returnsFullDifferenceReport() throws Exception
	{
		// arrange
		cut = new DocumentComparer( TEST_DATA_DIR + "/PDF_A.pdf" );
		
		// act
		String result = cut.getFullDifferenceReport(TEST_DATA_DIR + "/PDF_B.pdf");
		
		// assert
		String expectedFileContent = SysNatFileUtil.readTextFileToString(
						TEST_DATA_DIR + "/expectedDifferenceReport.txt");
		assertEquals("Difference report", cutLocalPath(expectedFileContent), cutLocalPath(result));
	}

	private String cutLocalPath(String filecontent) 
	{
		int pos1 = filecontent.indexOf("Doc1");
		String s = filecontent.substring(pos1);
		int pos2 = s.indexOf("\\sources\\");
		filecontent = filecontent.substring(0, pos1) + s.substring(pos2);
		
		pos1 = filecontent.indexOf("Doc2");
		s = filecontent.substring(pos1);
		pos2 = s.indexOf("\\sources\\");
		filecontent = filecontent.substring(0, pos1) + s.substring(pos2);
		return filecontent;
	}


	@Test
	public void returnsDifferenceReport_IgnoreWithDateAndRegex() throws Exception
	{
		// arrange
		cut = new DocumentComparer( TEST_DATA_DIR + "/PDF_C.pdf" );
		final List<DateFormat> dateFormatsToIgnore = new ArrayList<>();
		dateFormatsToIgnore.add(new SimpleDateFormat("MM-yyyy"));
		dateFormatsToIgnore.add(DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMAN));
		final List<String> regexToIgnore = new ArrayList<>();
		regexToIgnore.add("[0-9]{3}");
		regexToIgnore.add("X{2,255}");
		DocumentCompareIgnoreConfig ignoreConfig = new DocumentCompareIgnoreConfig().withDateformats(dateFormatsToIgnore)
				                                                          .withRegexPatterns(regexToIgnore);

		// act
		String result = cut.getDifferenceReport(TEST_DATA_DIR + "/PDF_D.pdf",
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
		cut = new DocumentComparer( TEST_DATA_DIR + "/PDF_C.pdf" );
		final List<String> lineDefinitionsToIgnore = new ArrayList<>();
		lineDefinitionsToIgnore.add("Seite 1, Zeile 1");
		lineDefinitionsToIgnore.add("Seite 1, Zeile 2");
		lineDefinitionsToIgnore.add("Seite 1, Zeile 3");
		lineDefinitionsToIgnore.add("Seite 1, Zeile 4");
		lineDefinitionsToIgnore.add("Seite 1, Zeile 5");
		lineDefinitionsToIgnore.add("Seite 2, Zeile 6");  // this line must not have an effect !
		DocumentCompareIgnoreConfig ignoreConfig = new DocumentCompareIgnoreConfig().withLineDefinitions(lineDefinitionsToIgnore);

		// act
		String result = cut.getDifferenceReport(TEST_DATA_DIR + "/PDF_D.pdf",
				                                ignoreConfig);
		
		// assert
		// assert
		String expectedFileContent = SysNatFileUtil.readTextFileToString(
						TEST_DATA_DIR + "/expectedDifferenceReport_PDF_C_D.txt");
		assertEquals("Difference report", cutLocalPath(expectedFileContent), cutLocalPath(result));
	}
	
	@Test
	public void returnsDifferenceReport_AssymetricIgnoreWithLineDefinitions_onLineLevel() throws Exception
	{
		// arrange
		cut = new DocumentComparer( TEST_DATA_DIR + "/PDF_Assymetric_Compare1.pdf" );
		DocumentComparer cut2 = new DocumentComparer( TEST_DATA_DIR + "/PDF_Assymetric_Compare2.pdf" );
		final List<String> lineDefinitionsToIgnore = new ArrayList<>();
		lineDefinitionsToIgnore.add("Doc1:1:1");
		lineDefinitionsToIgnore.add("Doc2:2:1");
		DocumentCompareIgnoreConfig ignoreConfig = new DocumentCompareIgnoreConfig().withLineDefinitions(lineDefinitionsToIgnore);

		// act
		String result1 = cut.getDifferenceReport(TEST_DATA_DIR + "/PDF_Assymetric_Compare2.pdf",
				                                ignoreConfig);
		String result2 = cut2.getDifferenceReport(TEST_DATA_DIR + "/PDF_Assymetric_Compare1.pdf",
                                                  ignoreConfig);
		
		// assert
		assertTrue("Difference report is not empty!", result1.isEmpty());
		assertFalse("Difference report is empty!", result2.isEmpty());
	}	

	@Test
	public void returnsDifferenceReport_AssymetricIgnoreWithLineDefinitions_onPageLevel() throws Exception
	{
		// arrange
		cut = new DocumentComparer( TEST_DATA_DIR + "/PDF_Assymetric_Compare3.pdf" );  // this is PDF1
		DocumentComparer cut2 = new DocumentComparer( TEST_DATA_DIR + "/PDF_Assymetric_Compare4.pdf" );
		final List<String> lineDefinitionsToIgnore = new ArrayList<>();
		lineDefinitionsToIgnore.add("Doc1:3:*");
		DocumentCompareIgnoreConfig ignoreConfig = new DocumentCompareIgnoreConfig().withLineDefinitions(lineDefinitionsToIgnore);

		// act
		String result1 = cut.getDifferenceReport(TEST_DATA_DIR + "/PDF_Assymetric_Compare4.pdf", // this is PDF2
				                                ignoreConfig);
		String result2 = cut2.getDifferenceReport(TEST_DATA_DIR + "/PDF_Assymetric_Compare3.pdf", // PDF order exchanged
                                                  ignoreConfig);
		
		// assert
		System.err.println(result1);
		assertTrue("Difference report is not empty!", result1.isEmpty());
		System.err.println(result1);
		assertFalse("Difference report is empty!", result2.isEmpty());
	}	

	@Test
	public void returnsDifferenceReport_AssymetricIgnoreWithLineDefinitions_onBothPageAndLineLevel() throws Exception
	{
		// arrange
		cut = new DocumentComparer( TEST_DATA_DIR + "/PDF_Assymetric_Compare1.pdf" );  // this is PDF1
		final List<String> lineDefinitionsToIgnore = new ArrayList<>();
		lineDefinitionsToIgnore.add("Doc1:1:1");
		lineDefinitionsToIgnore.add("Doc2:3:*");
		DocumentCompareIgnoreConfig ignoreConfig = new DocumentCompareIgnoreConfig().withLineDefinitions(lineDefinitionsToIgnore);

		// act
		String result = cut.getDifferenceReport(TEST_DATA_DIR + "/PDF_Assymetric_Compare5.pdf", // this is PDF2
				                                ignoreConfig);
		
		// assert
		String expectedFileContent = SysNatFileUtil.readTextFileToString(
				TEST_DATA_DIR + "/expectedAssymetricDifferenceReport.txt");
		assertEquals("Difference report", cutLocalPath(expectedFileContent), cutLocalPath(expectedFileContent));
	}
	
	@Test
	public void returnsDifferenceReport_IgnoreWithPrefixesAndSubstrings() throws Exception
	{
		// arrange
		cut = new DocumentComparer( TEST_DATA_DIR + "/PDF_A.pdf" );
		final List<String> linePrefixesToIgnore = new ArrayList<>();
		linePrefixesToIgnore.add("Gree");
		linePrefixesToIgnore.add("5");
		final List<String> substringsToIgnore = new ArrayList<>();
		substringsToIgnore.add("iv");
		substringsToIgnore.add("Two");
		substringsToIgnore.add("2");
		DocumentCompareIgnoreConfig ignoreConfig = new DocumentCompareIgnoreConfig().withSubstrings(substringsToIgnore)
				                                                          .withPrefixes(linePrefixesToIgnore);

		// act
		String result = cut.getDifferenceReport(TEST_DATA_DIR + "/PDF_B.pdf",
				                                ignoreConfig);
		
		// assert
		if (! result.isEmpty()) {
			System.out.println(result);
		}
		assertTrue("Difference report is not empty!", result.isEmpty());
	}
	
	@Test
	public void returnsDifferencesReportWithIgnoreBetweenComparison() throws Exception
	{
		cut = new DocumentComparer( TEST_DATA_DIR + "/CompareBetweenTest1.xml" );
		final HashMap<String,String>  ignoreBetweenIdentifier = new HashMap<>();
		ignoreBetweenIdentifier.put("<?", "?>");
		DocumentCompareIgnoreConfig ignoreConfig = new DocumentCompareIgnoreConfig().withIgnoreBetweenIdentifier(ignoreBetweenIdentifier);
		
		
		String result = cut.getDifferenceReport(TEST_DATA_DIR + "/CompareBetweenTest2.xml", ignoreConfig);
		assertTrue("", result.isEmpty());
		
		ignoreBetweenIdentifier.clear();
		result = cut.getDifferenceReport(TEST_DATA_DIR + "/CompareBetweenTest2.xml", ignoreConfig);
		assertTrue("Unexpected result", result.contains("1. Difference:"));
		
		ignoreBetweenIdentifier.put("=", "=");
		ignoreConfig = new DocumentCompareIgnoreConfig().withIgnoreBetweenIdentifier(ignoreBetweenIdentifier);
		result = cut.getDifferenceReport(TEST_DATA_DIR + "/CompareBetweenTest2.xml", ignoreConfig);
		assertTrue("Unexpected result", result.contains("1. Difference:"));
	}
	
	
}