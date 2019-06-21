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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.common.helper.pdftooling.PdfCompareIgnoreConfig.ApplyIgnoreLineDefinitionScope;

/**
 * Provides methods to find differences between two PDF files.
 * The first file must be defined in the constructor, 
 * the second one when calling a compare-method.
 * It uses the {@link PdfFileContent} to access the PDF files.
 * 
 * @author Reik Oberrath
 */
public class PdfFileComparer
{
	private static final String NO_DIFFERENCE_FOUND = "<No difference found>";

	private static final String PAGE_IDENTIFIER = "Seite";

	private static final int MAX_DIFFERENCES_IN_REPORT = 100; // set to 10 after end of test phase
	
	private String firestPdfFileName;
	private String otherPdfFileNameToCompare;
	private PdfFileContent firstPdfFileContent;
	private PdfFileContent otherPdfFileContent;
	private int diffCounter;

	public PdfFileComparer(String aPdfFileName) {
		this.firestPdfFileName = aPdfFileName;
		firstPdfFileContent = new PdfFileContent(firestPdfFileName);
	}

	// #################################################################
	//               P U B L I C    M E T H O D S
	// #################################################################
	
	public int getPageNumber() {
		return firstPdfFileContent.getPageNumber();
	}

	public List<Integer> getDifferingPages(String anotherPdf) throws IOException 
	{	
		final PdfCompareIgnoreConfig ignoreConfig = new PdfCompareIgnoreConfig();
		
		final List<PdfPageContent> contentToComparePDF1 = createComparableContent(
				firstPdfFileContent, ignoreConfig, ApplyIgnoreLineDefinitionScope.PDF1);
		final List<PdfPageContent> contentToComparePDF2 = createComparableContent(
				getOtherContentAnalyser(anotherPdf), ignoreConfig, ApplyIgnoreLineDefinitionScope.PDF2);

		return getDifferingPages(contentToComparePDF1, contentToComparePDF2);
	}
	
	public List<Integer> getDifferingPages(final List<PdfPageContent> contentToComparePDF1, 
			                               final List<PdfPageContent> contentToComparePDF2) 
			                               throws IOException 
	{
		if (contentToComparePDF1.size() != contentToComparePDF2.size()) {
			return null;
		}

		final List<Integer> differingPages = new ArrayList<>();
		
		for (int pageNo = 1; pageNo <= contentToComparePDF1.size(); pageNo++) 
		{
			if ( ! getDifferenceList(contentToComparePDF1.get(pageNo-1), 
					                 contentToComparePDF2.get(pageNo-1),
					                 pageNo).isEmpty() ) 
			{
				differingPages.add(pageNo);
			}			
		}
		
		return differingPages;
	}

	private List<PdfPageContent> createComparableContent(final PdfFileContent pdfFileContent, 
			                                             final PdfCompareIgnoreConfig ignoreConfig,
			                                             final ApplyIgnoreLineDefinitionScope scope)
	{
		final List<PdfPageContent> toReturn = new ArrayList<>();

		for (int pageNo = 1; pageNo <= pdfFileContent.getPageNumber(); pageNo++) 
		{
			PdfPageContent pageContent = pdfFileContent.getPageContent(pageNo);
			pageContent.apply(ignoreConfig, scope);
			if (pageContent.getNumberOfLines() > 0) toReturn.add(pageContent);
		}
		
		return toReturn;
	}

	public String getDifferingPagesAsString(String anotherPdf) throws IOException 
	{
		List<Integer> diffPages = getDifferingPages(anotherPdf);
		String toReturn = differingPagesToReportLine(diffPages);
		return toReturn;
	}

	public boolean isContentIdenticalTo(String anotherPdf) {
		try {
			return getDifferingPages(anotherPdf).isEmpty();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getFirstDifference(String anotherPdf) throws IOException 
	{
		int pageCount = getOtherContentAnalyser(anotherPdf).getPageNumber();
		for (int pageNo = 1; pageNo <= pageCount; pageNo++) {
			String differenceOnPage = getFirstDifferenceOnPage(pageNo, anotherPdf);
			if ( ! NO_DIFFERENCE_FOUND.equals( differenceOnPage) ) {
				return differenceOnPage;
			}
		}
		System.out.println(NO_DIFFERENCE_FOUND);
		return NO_DIFFERENCE_FOUND;
	}

	
	public List<String> getAllDifferingWordsOnPage(int pageNo, String anotherPdf) throws IOException 
	{
		List<String> lines1 = firstPdfFileContent.getPageContent(pageNo).getLines();
		List<String> lines2 = getOtherContentAnalyser(anotherPdf).getPageContent(pageNo).getLines();
		
		int maxWords = lines2.size();
		if (lines1.size() < maxWords) {
			maxWords = lines1.size();
		}
		
		List<String> toReturn = new ArrayList<>();
		for (int i = 0; i < maxWords; i++) 
		{
			if ( ! lines1.get(i).equals(lines2.get(i)) ) {
				String diff = "On page " + pageNo + " is '" + lines1.get(i) + "' not equal to '" + lines2.get(i) + "'.";
				toReturn.add(diff);
				//System.out.println(diff);
			}
			
		}
		return toReturn;
	}

	public List<String> getDifferingLinesOnPage(final String anotherPdf, 
                                                final int pageNo, 
                                                final PdfCompareIgnoreConfig ignoreConfig) 
	{
		PdfPageContent pageContent1 = firstPdfFileContent.getPageContent(pageNo);
		pageContent1.apply(ignoreConfig, ApplyIgnoreLineDefinitionScope.PDF1);
		
		PdfPageContent pageContent2 = getOtherContentAnalyser(anotherPdf).getPageContent(pageNo);
		pageContent2.apply(ignoreConfig, ApplyIgnoreLineDefinitionScope.PDF2);
		
		diffCounter = 0;
		return getDifferenceList(pageContent1, pageContent2, pageNo);
	}


	/**
	 * Detects all difference with applying any Ignore-Filter.
	 * 
	 * @param anotherPdf
	 * @return Result
	 * @throws IOException
	 */
	public String getFullDifferenceReport(final String anotherPdf) throws IOException {
		return getDifferenceReport(anotherPdf, new PdfCompareIgnoreConfig());
	}
	
	/**
	 * Lines that are different between the PDFs but contain one (or more) of the substrings to ignore
	 * will be ignored by the difference report. 
	 */
	public String getDifferenceReport(final String anotherPdf, 
			                          final PdfCompareIgnoreConfig ignoreConfig) throws IOException 
	{
		final Map<Integer, List<String>> differences = getDifferences(anotherPdf, ignoreConfig); // new way
		
		if (differences == null) {
			return getDifferenceReport(anotherPdf, null, null, null);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		final List<Integer> differingPages = new ArrayList(differences.keySet());
		final List<String> differingLines = new ArrayList<>();
		
		differences.forEach((pageNo,diffLines) -> differingLines.addAll(diffLines));
		return getDifferenceReport(anotherPdf, differingPages, differingLines, ignoreConfig); 
	}

	public Map<Integer, List<String>> getDifferences(String anotherPdf, 
			                                         PdfCompareIgnoreConfig ignoreConfig) 
			                                         throws IOException 
	{
		final List<PdfPageContent> contentToComparePDF1 = createComparableContent(
				firstPdfFileContent, ignoreConfig, ApplyIgnoreLineDefinitionScope.PDF1);
		final List<PdfPageContent> contentToComparePDF2 = createComparableContent(
				getOtherContentAnalyser(anotherPdf), ignoreConfig, ApplyIgnoreLineDefinitionScope.PDF2);
		
		final Map<Integer, List<String>> toReturn = new HashMap<>();
		
		List<Integer> differingPages = getDifferingPages(contentToComparePDF1, contentToComparePDF2);
		if (differingPages == null) {
			return null;
		}
		
		diffCounter = 0;

		for (int i = 0; i < differingPages.size(); i++) 
		{
			int pageNo = differingPages.get(i);
			PdfPageContent pageContent1 = contentToComparePDF1.get(pageNo-1);
			PdfPageContent pageContent2 = contentToComparePDF2.get(pageNo-1);
			List<String> differenceList = getDifferenceList(pageContent1, pageContent2, pageNo);
			toReturn.put(new Integer(pageNo), differenceList);
		}
		
		return toReturn;
	}
	


	public boolean doesPageContainIgnoreWhiteSpace(int pageNo, String toSearch) 
	{
		if (pageNo < 1 || pageNo > getPageNumber() ) {
			throw new IllegalArgumentException("Page number " + pageNo + " out of range!");
		}
		
		try {
			final String pageCompressed = compress( firstPdfFileContent.getPageContent(pageNo).getPageContentAsString() );
			final String searchStringCompressed = compress( toSearch );
			return pageCompressed.contains(searchStringCompressed);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	private String compress(String text) 
	{
		char[] charArray = text.toCharArray();
		final StringBuffer sb = new StringBuffer();
		
		for (char c : charArray) 
		{
			if (c != ' ' && c > 30) {
				sb.append(c);
			}
		}
		
		return sb.toString();
	}
	
	
	// #################################################################
	//               P R I V A T E    M E T H O D S
	// #################################################################

	
	private PdfFileContent getOtherContentAnalyser(String anotherPdfFilename) 
	{
		if (! anotherPdfFilename.equals(otherPdfFileNameToCompare)) {
			otherPdfFileNameToCompare = anotherPdfFilename;
			otherPdfFileContent = new PdfFileContent(otherPdfFileNameToCompare);
		}
		
		return otherPdfFileContent;
	}

	private String differingPagesToReportLine(List<Integer> diffPages) 
	{
		if (diffPages == null) {
			return "Die PDFs unterscheiden sich in der Seitenzahl.";			
		}
		String toReturn = "";
		if (diffPages.size() == 1) {
			toReturn = "PDFs unterscheiden sich auf Seite " + diffPages.get(0) + ".";
		} else {
			toReturn = "PDFs unterscheiden sich auf den Seiten ";
			for (int i = 0; i < diffPages.size()-2; i++) {
				toReturn += diffPages.get(i) + ", ";
			}
			toReturn += diffPages.get(diffPages.size()-2);
			toReturn += " und " + diffPages.get(diffPages.size()-1) + ".";
		}
		return toReturn;
	}

	private String getFirstDifferenceOnPage(int pageNo, String anotherPdf) throws IOException 
	{
		PdfPageContent pageContent1 = firstPdfFileContent.getPageContent(pageNo);
		PdfPageContent pageContent2 = getOtherContentAnalyser(anotherPdf).getPageContent(pageNo);
		
		List<String> differenceList = getDifferenceList(pageContent1, pageContent2, pageNo);
		
		if (differenceList.isEmpty()) {
			return NO_DIFFERENCE_FOUND;
		}
		
		StringBuffer sb = new StringBuffer();
		String line = differenceList.get(0);
		int lineCounter = 0;
		while ( ! line.startsWith("2. ")) {
			sb.append(line).append(System.getProperty("line.separator"));
			lineCounter++;
			line = differenceList.get(lineCounter);
		}
		
		return sb.toString().trim();
	}


	List<String> getDifferenceList(final PdfPageContent pageContent1,
			                       final PdfPageContent pageContent2, 
			                       final int pageNo) 
	{
		List<String> toReturn = new ArrayList<>();
		List<String> lines1 = pageContent1.getLines();
		List<String> lines2 = pageContent2.getLines();
	
		int numberOfLines = lines1.size(); 
		if (lines2.size() > numberOfLines) {
			numberOfLines = lines2.size(); 
		}
	
		for (int i = 1; i <= numberOfLines; i++) 
		{
			String pageNo1 = pageNo + "(" + pageContent1.getPageNumber() + ")";
			String pageNo2 = pageNo + "(" + pageContent2.getPageNumber() + ")";
			String lineNo1;
			String lineNo2;
			String line1;
			String line2;
			
			if (i > lines1.size()) 
			{
				lineNo1 = "none";
				line1 = "-";
				lineNo2 = i + "(" + pageContent2.getOriginalNumberOfLineInPage(i-1) + ")";
				line2 = "[" + lines2.get(i-1) + "]";
			}
			else if (i > lines2.size()) {
				lineNo1 = i + "(" + pageContent1.getOriginalNumberOfLineInPage(i-1) + ")";
				line1 = "[" + lines1.get(i-1) + "]";
				lineNo2 = "none";
				line2 = "-";
			}
			else if ( ! lines1.get(i-1).equals(lines2.get(i-1)) ) {
				lineNo1 = i + "(" + pageContent1.getOriginalNumberOfLineInPage(i-1) + ")";
				line1 = "[" + lines1.get(i-1) + "]";
				lineNo2 = i + "(" + pageContent2.getOriginalNumberOfLineInPage(i-1) + ")";
				line2 = "[" + lines2.get(i-1) + "]";
			} else {
				continue;
			}
			
			diffCounter++;
			toReturn.add(diffCounter + ". Difference:");
			toReturn.add("PDF1, Page " + pageNo1 + ", Line " + lineNo1 + " : " + line1
					   + System.getProperty("line.separator")
					   + "PDF2, Page " + pageNo2 + ", Line " + lineNo2 + " : " + line2);

		}
		
		return toReturn;
	}


	private String getDifferenceReport(final String anotherPdf,
			                           final List<Integer> diffPages,
			                           final List<String> differingLines, 
			                           final PdfCompareIgnoreConfig ignoreConfig) 
			                           throws IOException 
	{
		StringBuffer report = getReportHeader(anotherPdf, ignoreConfig);
		
		if (diffPages == null) {
			report.append(System.getProperty("line.separator"));
			return report.toString() + "Die PDFs unterscheiden sich in der Seitenzahl.";			
		}

		if (diffPages.size() == 0) {
			return "";
		}
		
		report.append(System.getProperty("line.separator"));
		if (differingLines.size() < MAX_DIFFERENCES_IN_REPORT) {
			differingLines.forEach(line -> report.append(line).append(System.getProperty("line.separator")));
		} else {
			report.append( differingPagesToReportLine(diffPages)).append(System.getProperty("line.separator"));
			report.append( "Es gibt insgesamt " + differingLines.size() + " unterschiedliche Zeilen. ");
			report.append( "Die ersten sind: ").append(System.getProperty("line.separator"));
			for (int i = 0; i < MAX_DIFFERENCES_IN_REPORT; i++) {
				report.append(differingLines.get(i)).append(System.getProperty("line.separator"));
			}
		}
		
		return report.toString().trim(); 
	}

	private StringBuffer getReportHeader(final String anotherPdf, final PdfCompareIgnoreConfig ignoreConfig)
	{
		StringBuffer report = new StringBuffer("Unterschiede zwischen");
		report.append(System.getProperty("line.separator"));
		report.append(firestPdfFileName).append(" (Seitenzahl: " + getPageNumber() + ")");
		report.append(System.getProperty("line.separator"));
		report.append("und");
		report.append(System.getProperty("line.separator"));
		report.append(anotherPdf).append(" (Seitenzahl: " + getOtherContentAnalyser(anotherPdf).getPageNumber() + ")");
		report.append(System.getProperty("line.separator"));
		report.append("--------------------------------------------------------------------");
		
		addIgnoreInfoIfNeeded(ignoreConfig, report);
		
		return report;
	}

	private void addIgnoreInfoIfNeeded(final PdfCompareIgnoreConfig ignoreConfig, StringBuffer report)
	{
		if (ignoreConfig != null)
		{
			String ignoreInfo = ignoreConfig.toString();
			if ( ! ignoreInfo.isEmpty() ) {
				report.append(System.getProperty("line.separator"));
				report.append(ignoreInfo);
				report.append("--------------------------------------------------------------------");
			}
		}
	}
	
	Integer getPageNumberFromLine(String line) 
	{
		int pos1 = line.indexOf(PAGE_IDENTIFIER) + PAGE_IDENTIFIER.length();
		int pos2 = line.indexOf(":");
		
		if (pos1 == -1 || pos2 == -1) {
			throw new SysNatTestDataException("Cannot parse line number from <b>" + line + "</b>.");
		}
		
		int pos3 = line.indexOf(",");
		
		if (pos3 !=-1 && pos2 > pos3) {
			pos2 = pos3;
		}
		String returnCandidate = line.substring(pos1, pos2).trim();
				
		try {
			return Integer.valueOf(returnCandidate);
		} catch (Exception e) {
			throw new SysNatTestDataException("Cannot parse line number from <b>" + line + "</b>.");
		}
	}
}