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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.iksgmbh.sysnat.common.utils.SysNatConstants.ApplyIgnoreLineDefinitionScope;
import com.iksgmbh.sysnat.helper.docval.domain.DocumentCompareIgnoreConfig;
import com.iksgmbh.sysnat.helper.docval.domain.DocumentContent;
import com.iksgmbh.sysnat.helper.docval.domain.PageContent;

/**
 * Provides methods to find differences between two documents
 * that must be available as files.
 * The first file must be defined in the constructor, 
 * the second one when calling a compare-method.
 * It uses the {@link DocumentContent} to access the document files.
 * 
 * @author Reik Oberrath
 */
public class DocumentComparer
{
	private static final String NO_DIFFERENCE_FOUND = "<No difference found>";
	private static final int SHOW_FIRST_DIFFERENCES_IN_REPORT = 7;
	private static final int MAX_DIFFERENCES_IN_REPORT = 12;
	private static final String DIFF_PAGE_MESSAGE = "The documents differ in their page numbers.";
	
	private String firstDocName;
	private String otherDocNameToCompare;
	private DocumentContent firstDocContent;
	private DocumentContent otherDocContent;
	private int diffCounter;
	private boolean addParanthesesNote;

	public static void main(String[] args)
	{
		try {
			String result = new DocumentComparer("C:\\Users\\OberratR\\AppData\\Roaming\\Client\\ImportExport\\SAPExportTest.xml")
					       .getFullDifferenceReport("C:\\dev\\Tools\\SysNatLight\\sources\\sysnat.test.execution\\..\\sysnat.natural.language.executable.examples\\testdata\\Client\\Validationsregeln\\..\\Erwartungswerte\\ExpectedSAPExportResultUSAkte.xml");
			System.out.println(result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public DocumentComparer(String aFileName) {
		this.firstDocName = aFileName;
		firstDocContent = new DocumentContent(firstDocName);
	}

	public DocumentComparer(File aFile) {
		this( aFile.getAbsolutePath() );
	}
	
	// #################################################################
	//               P U B L I C    M E T H O D S
	// #################################################################
	
	public int getPageNumber() {
		return firstDocContent.getNumberOfPages();
	}

	public List<Integer> getDifferingPages(String anotherDoc) throws IOException 
	{	
		final DocumentCompareIgnoreConfig ignoreConfig = new DocumentCompareIgnoreConfig();
		
		final List<PageContent> contentToCompare1 = createComparableContent(
				firstDocContent, ignoreConfig, ApplyIgnoreLineDefinitionScope.Doc1);
		final List<PageContent> contentToCompare2 = createComparableContent(
				getOtherContentAnalyser(anotherDoc), ignoreConfig, ApplyIgnoreLineDefinitionScope.Doc2);

		return getDifferingPages(contentToCompare1, contentToCompare2);
	}

	public List<Integer> getDifferingPages(final List<PageContent> contentToCompare1, 
                                           final List<PageContent> contentToCompare2) 
                                           throws IOException 
    {
            	return getDifferingPages(contentToCompare1, contentToCompare2, null);
    }

	public List<Integer> getDifferingPages(final List<PageContent> contentToCompare1, 
			                               final List<PageContent> contentToCompare2, 
			                               final HashMap<String, String> ignoreBetweenIdentifier) 
			                               throws IOException 
	{
		if (contentToCompare1.size() != contentToCompare2.size()) {
			return null;
		}

		final List<Integer> differingPages = new ArrayList<>();
		
		for (int pageNo = 1; pageNo <= contentToCompare1.size(); pageNo++) 
		{
			List<String> differenceList = getDifferenceList(contentToCompare1.get(pageNo-1), 
	                                                        contentToCompare2.get(pageNo-1),
	                                                        pageNo, 
	                                                        ignoreBetweenIdentifier);
			if ( ! differenceList.isEmpty() ) {
				
				differingPages.add(pageNo);
			}			
		}
		
		return differingPages;
	}

	private List<PageContent> createComparableContent(final DocumentContent fileContent, 
			                                             final DocumentCompareIgnoreConfig ignoreConfig,
			                                             final ApplyIgnoreLineDefinitionScope scope)
	{
		final List<PageContent> toReturn = new ArrayList<>();

		for (int pageNo = 1; pageNo <= fileContent.getNumberOfPages(); pageNo++) 
		{
			PageContent pageContent = fileContent.getPageContent(pageNo);
			pageContent.apply(ignoreConfig, scope);
			if (pageContent.getNumberOfLines() > 0) toReturn.add(pageContent);
		}
		
		return toReturn;
	}

	public String getDifferingPagesAsString(String anotherDoc) throws IOException 
	{
		List<Integer> diffPages = getDifferingPages(anotherDoc);
		String toReturn = differingPagesToReportLine(diffPages);
		return toReturn;
	}

	public boolean isContentIdenticalTo(String anotherDoc) {
		try {
			return getDifferingPages(anotherDoc).isEmpty();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getFirstDifference(String anotherDoc) throws IOException 
	{
		int pageCount = getOtherContentAnalyser(anotherDoc).getNumberOfPages();
		for (int pageNo = 1; pageNo <= pageCount; pageNo++) {
			String differenceOnPage = getFirstDifferenceOnPage(pageNo, anotherDoc);
			if ( ! NO_DIFFERENCE_FOUND.equals( differenceOnPage) ) {
				return differenceOnPage;
			}
		}
		System.out.println(NO_DIFFERENCE_FOUND);
		return NO_DIFFERENCE_FOUND;
	}

	
	public List<String> getAllDifferingWordsOnPage(int pageNo, String anotherDoc) throws IOException 
	{
		List<String> lines1 = firstDocContent.getPageContent(pageNo).getLines();
		List<String> lines2 = getOtherContentAnalyser(anotherDoc).getPageContent(pageNo).getLines();
		
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

	public List<String> getDifferingLinesOnPage(final String anotherDoc, 
                                                final int pageNo, 
                                                final DocumentCompareIgnoreConfig ignoreConfig) 
	{
		PageContent pageContent1 = firstDocContent.getPageContent(pageNo);
		pageContent1.apply(ignoreConfig, ApplyIgnoreLineDefinitionScope.Doc1);
		
		PageContent pageContent2 = getOtherContentAnalyser(anotherDoc).getPageContent(pageNo);
		pageContent2.apply(ignoreConfig, ApplyIgnoreLineDefinitionScope.Doc2);
		
		diffCounter = 0;
		return getDifferenceList(pageContent1, pageContent2, pageNo);
	}


	/**
	 * Detects all difference with applying any Ignore-Filter.
	 * 
	 * @param anotherDoc
	 * @return Result
	 * @throws IOException
	 */
	public String getFullDifferenceReport(final String anotherDoc) throws IOException {
		return getDifferenceReport(anotherDoc, new DocumentCompareIgnoreConfig());
	}
	
	/**
	 * Lines that are different between the documents
	 * but are defined as to be ignored will not be part
	 * of the difference report. 

	 * @param anotherDoc
	 * @param ignoreConfig
	 * @return difference report as String
	 * @throws IOException
	 */
	public String getDifferenceReport(final String anotherDoc, 
			                          final DocumentCompareIgnoreConfig ignoreConfig) throws IOException 
	{
		addParanthesesNote = false;
		final Map<Integer, List<String>> differences = getDifferences(anotherDoc, ignoreConfig); // new way
		
		if (differences == null) {
			return getDifferenceReport(anotherDoc, null, null, null);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		final List<Integer> differingPages = new ArrayList(differences.keySet());
		final List<String> differingLines = new ArrayList<>();

		differences.forEach((pageNo,diffLines) -> differingLines.addAll(diffLines));
		return getDifferenceReport(anotherDoc, differingPages, differingLines, ignoreConfig); 
	}

	public Map<Integer, List<String>> getDifferences(String anotherDoc, 
			                                         DocumentCompareIgnoreConfig ignoreConfig) 
			                                         throws IOException 
	{
		final List<PageContent> contentToCompare1 = createComparableContent(
				firstDocContent, ignoreConfig, ApplyIgnoreLineDefinitionScope.Doc1);
		final List<PageContent> contentToCompare2 = createComparableContent(
				getOtherContentAnalyser(anotherDoc), ignoreConfig, ApplyIgnoreLineDefinitionScope.Doc2);
		
		final Map<Integer, List<String>> toReturn = new HashMap<>();
		
		List<Integer> differingPages = getDifferingPages(contentToCompare1, contentToCompare2, ignoreConfig.ignoreBetweenIdentifier);
		if (differingPages == null) {
			return null;
		}
		
		diffCounter = 0;

		for (int i = 0; i < differingPages.size(); i++) 
		{
			int pageNo = differingPages.get(i);
			PageContent pageContent1 = contentToCompare1.get(pageNo-1);
			PageContent pageContent2 = contentToCompare2.get(pageNo-1);
			List<String> differenceList = getDifferenceList(pageContent1, pageContent2, pageNo, ignoreConfig.ignoreBetweenIdentifier);
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
			final String pageCompressed = compress( firstDocContent.getPageContent(pageNo).getPageContentAsString() );
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

	
	private DocumentContent getOtherContentAnalyser(String anotherDocFilename) 
	{
		if (! anotherDocFilename.equals(otherDocNameToCompare)) {
			otherDocNameToCompare = anotherDocFilename;
			otherDocContent = new DocumentContent(otherDocNameToCompare);
		}
		
		return otherDocContent;
	}

	private String differingPagesToReportLine(final List<Integer> diffPages) 
	{
		if (diffPages == null) {
			return DIFF_PAGE_MESSAGE;			
		}
		String toReturn = "";
		if (diffPages.size() == 1) {
			toReturn = "The documents differ on page " + diffPages.get(0) + ".";
		} else {
			toReturn = "The documents differ on pages ";
			for (int i = 0; i < diffPages.size()-2; i++) {
				toReturn += diffPages.get(i) + ", ";
			}
			toReturn += diffPages.get(diffPages.size()-2);
			toReturn += " and " + diffPages.get(diffPages.size()-1) + ".";
		}
		return toReturn;
	}

	private String getFirstDifferenceOnPage(int pageNo, String anotherDoc) throws IOException 
	{
		PageContent pageContent1 = firstDocContent.getPageContent(pageNo);
		PageContent pageContent2 = getOtherContentAnalyser(anotherDoc).getPageContent(pageNo);
		
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

	List<String> getDifferenceList(final PageContent pageContent1,
                                   final PageContent pageContent2, 
                                   final int pageNo) 
	{
		return getDifferenceList(pageContent1, pageContent2, pageNo, null);
	}


	List<String> getDifferenceList(final PageContent pageContent1,
			                       final PageContent pageContent2, 
			                       final int pageNo, 
			                       final HashMap<String, String> ignoreBetweenIdentifier) 
	{
		List<String> toReturn = new ArrayList<>();
		List<String> lines1 = pageContent1.getLines();
		List<String> lines2 = pageContent2.getLines();
	
		int numberOfLines = lines1.size(); 
		if (lines2.size() > numberOfLines) {
			numberOfLines = lines2.size(); 
		}
		
		String pageNo1 = "" + pageNo;
		String pageNo2 = "" + pageNo;
		if (pageContent1.getPageNumber() != pageContent2.getPageNumber()) 
		{
			// add original page number if differing between documents
			pageNo1 += "(" + pageContent1.getPageNumber() + ")";
			pageNo2 += "(" + pageContent2.getPageNumber() + ")";
			addParanthesesNote = true;
		}
	
		for (int i = 1; i <= numberOfLines; i++) 
		{

			String lineNo1;
			String lineNo2;
			String line1;
			String line2;
			
			if (i > lines1.size()) 
			{
				// no line comparison possible due mismatch in number of pages
				lineNo1 = "none";
				line1 = "-";
				int originalNumberOfLineInPage2 = pageContent2.getOriginalNumberOfLineInPage(i-1);
				lineNo2 = "" + originalNumberOfLineInPage2;
				//if (originalNumberOfLineInPage1 != originalNumberOfLineInPage2) lineNo2 = i + "(" + originalNumberOfLineInPage2 + ")";
				line2 = "[" + lines2.get(i-1) + "]";
			}
			else if (i > lines2.size()) 
			{
				// no line comparison possible due mismatch in number of pages
				int originalNumberOfLineInPage1 = pageContent1.getOriginalNumberOfLineInPage(i-1);
				lineNo1 = "" + originalNumberOfLineInPage1;
				//if (originalNumberOfLineInPage1 != originalNumberOfLineInPage2) lineNo1 = i + "(" + originalNumberOfLineInPage1 + ")";
				line1 = "[" + lines1.get(i-1) + "]";
				lineNo2 = "none";
				line2 = "-";
			}
			else if ( doLinesDiffer(lines1.get(i-1), lines2.get(i-1), ignoreBetweenIdentifier) ) 
			{
				int originalNumberOfLineInPage1 = pageContent1.getOriginalNumberOfLineInPage(i-1);
				int originalNumberOfLineInPage2 = pageContent2.getOriginalNumberOfLineInPage(i-1);
				lineNo1 = "" + originalNumberOfLineInPage1;
				if (originalNumberOfLineInPage1 != originalNumberOfLineInPage2) {
					lineNo1 = i + "(" + originalNumberOfLineInPage1 + ")";
					addParanthesesNote = true;			
				}
				line1 = "[" + lines1.get(i-1) + "]";
				lineNo2 = "" + originalNumberOfLineInPage2;
				if (originalNumberOfLineInPage1 != originalNumberOfLineInPage2) {
					lineNo2 = i + "(" + originalNumberOfLineInPage2 + ")";
					addParanthesesNote = true;			
				}
				line2 = "[" + lines2.get(i-1) + "]";
			} else {
				continue; // lines are equal, nothing to do here
			}
			
			diffCounter++;
			toReturn.add(diffCounter + ". Difference:");
			toReturn.add("Doc1, Page " + pageNo1 + ", Line " + lineNo1 + " : " + line1
					   + System.getProperty("line.separator")
					   + "Doc2, Page " + pageNo2 + ", Line " + lineNo2 + " : " + line2);

		}
		
		return toReturn;
	}


	private boolean doLinesDiffer(String line1, String line2, HashMap<String, String> ignoreBetweenIdentifier)
	{
		line1 = cutIgnoreCharsIfPresent(line1, ignoreBetweenIdentifier);
		line2 = cutIgnoreCharsIfPresent(line2, ignoreBetweenIdentifier);
		return ! line1.equals(line2);
	}
	

	public String cutIgnoreCharsIfPresent(String line, HashMap<String, String> ignoreBetweenIdentifier) 
	{
		if (ignoreBetweenIdentifier == null) return line;		
		final String[] lineWrapper = {line};
		ignoreBetweenIdentifier.entrySet().stream().forEach(e -> cutBeweenIdentifier(lineWrapper, e.getKey(), e.getValue()));
		return lineWrapper[0];
	}


	private void cutBeweenIdentifier(String[] lineWrapper, String startIdentifier, String endIdentifier)
	{
		String line = lineWrapper[0];
		int pos = line.indexOf(startIdentifier) + startIdentifier.length();
		
		if (pos > 0) 
		{
			String part1 = line.substring(0, pos);
			String part2 = line.substring(pos);
			pos = part2.indexOf(endIdentifier);
			if (pos > -1) {
				part2 = part2.substring(pos);
				lineWrapper[0] = part1 + part2;
			}
		}
	}

	private String getDifferenceReport(final String anotherDoc,
			                           final List<Integer> diffPages,
			                           final List<String> differingLines, 
			                           final DocumentCompareIgnoreConfig ignoreConfig) 
			                           throws IOException 
	{
		StringBuffer report = getReportHeader(anotherDoc, ignoreConfig);
		
		if (diffPages == null) {
			report.append(System.getProperty("line.separator"));
			return report.toString() + DIFF_PAGE_MESSAGE;			
		}

		if (diffPages.size() == 0) {
			return "";
		}
		
		report.append(System.getProperty("line.separator"));
		if (diffCounter < MAX_DIFFERENCES_IN_REPORT) {
			differingLines.forEach(line -> report.append(line).append(System.getProperty("line.separator")));
		} else {
			report.append( differingPagesToReportLine(diffPages)).append(System.getProperty("line.separator"));
			report.append( "There are " + diffCounter + " differing lines. ");
			report.append( "The first " + SHOW_FIRST_DIFFERENCES_IN_REPORT + " differences are ").append(System.getProperty("line.separator"));
			for (int i = 0; i < SHOW_FIRST_DIFFERENCES_IN_REPORT*2; i++) {  // times 2 due to two lines per difference !
				report.append(differingLines.get(i)).append(System.getProperty("line.separator"));
			}
		}
		
		
		if (addParanthesesNote) {
			report.append(System.getProperty("line.separator")
					      + "Values in parentheses represent original page and line numbers before applying the compare rules.");
			
		}

		return report.toString().trim(); 
	}

	private StringBuffer getReportHeader(final String anotherDoc, final DocumentCompareIgnoreConfig ignoreConfig) throws IOException
	{
		StringBuffer report = new StringBuffer("Differences between");
		report.append(System.getProperty("line.separator"));
		report.append("Doc1: ").append(new File(firstDocName).getCanonicalPath()).append(" (Number of pages: " + getPageNumber() + ")");
		report.append(" and");
		report.append(System.getProperty("line.separator"));
		report.append("Doc2: ").append(new File(anotherDoc).getCanonicalPath()).append(" (Number of pages: " + getOtherContentAnalyser(anotherDoc).getNumberOfPages() + ")");
		report.append(System.getProperty("line.separator"));
		report.append("--------------------------------------------------------------------");
		
		addIgnoreInfoIfNeeded(ignoreConfig, report);
		
		return report;
	}

	private void addIgnoreInfoIfNeeded(final DocumentCompareIgnoreConfig ignoreConfig, StringBuffer report)
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
	
}