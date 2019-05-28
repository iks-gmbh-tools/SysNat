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
import java.util.List;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;

/**
 * Reads and stores content of a single PDF file.
 * It uses the class {@link PdfPageContent} to store file content for each page separately. 
 * 
 * @author Reik Oberrath
 */
public class PdfFileContent
{	
	private String pdfFileName;
    private List<PdfPageContent> pageContentList;

	public PdfFileContent(String aPdfFileName) {
		this.pdfFileName = aPdfFileName;
		this.init();
	}

	
	// #################################################################
	//               P U B L I C    M E T H O D S
	// #################################################################

	public String getPdfFileName() {
		return pdfFileName;
	}
	
	public int getPageNumber() {
		return pageContentList.size();
	}
	
	public boolean doesFileContain(String text) {
		return pageContentList.stream().filter(pageContent -> pageContent.getOriginalPageContent().contains(text)).findFirst().isPresent();
	}

	public boolean doesLineContain(String pageTextId, int lineNumber, String toSearch) {
		return doesLineContain(findPageByTextIdentifier(pageTextId), lineNumber, toSearch);
	}

	public boolean doesLineContain(int pageNumber, int lineNumber, String toSearch) {
		String line = getLine(pageNumber, lineNumber);
		return line.contains(toSearch) || line.equals(toSearch);
	}

	public boolean doesLineEquals(String pageTextId, int lineNumber, String toSearch) {
		return doesLineEquals(findPageByTextIdentifier(pageTextId), lineNumber, toSearch);
	}

	public boolean doesLineEquals(int pageNumber, int lineNumber, String toSearch) {
		return getLine(pageNumber, lineNumber).equals(toSearch);
	}

	public String getLine(int pageNumber, int lineNumber)
	{
		checkPageNumber(pageNumber);

		List<String> linesOfPage = getLinesOfPage(pageNumber);
		int index = lineNumber - 1;
		return linesOfPage.get(index);

	}

	/**
	 * @param pageTextId text that identifies a page
	 * @param toSearch text to search for within the page
	 * @return true if toSearch is found on page <pageNumber>
	 */
	public boolean doesPageContain(String pageTextId, String toSearch) {
		return doesPageContain(findPageByTextIdentifier(pageTextId), toSearch);
	}
	
	/**
	 * @param pageNumber
	 * @param toSearch
	 * @return
	 */
	public boolean doesPageContain(int pageNumber, String toSearch) 
	{
		checkPageNumber(pageNumber);
		int index = pageNumber - 1;
		return pageContentList.get(index).getPageContentAsString().contains(toSearch);
	}
	
	public boolean doesPageContainIgnoreWhiteSpace(String pageTextId, String toSearch) {
		return doesPageContainIgnoreWhiteSpace(findPageByTextIdentifier(pageTextId), toSearch);
	}
	
	public boolean doesPageContainIgnoreWhiteSpace(int pageNumber, String toSearch) 
	{
		if (pageNumber < 1 || pageNumber > getPageNumber() ) {
			throw new IllegalArgumentException("Page number " + pageNumber + " out of range!");
		}
		
		int index = pageNumber - 1;
		String compressedPageContent = compress(pageContentList.get(index).getOriginalPageContent());
		return compressedPageContent.contains( compress(toSearch) );
	}		
	
	public boolean doesPageContain_IgnoreWhiteSpace(int pageNumber, String toSearch) 
	{
		checkPageNumber(pageNumber);
		int index = pageNumber - 1;
		String compressPageContent = compress(pageContentList.get(index).getPageContentAsString());
		return compressPageContent.contains( compress(toSearch) );
	}	
	

	public PdfPageContent getPageContent(int pageNo) {
		checkPageNumber(pageNo);
		return pageContentList.get(pageNo - 1);
	}

	public List<String> getLinesOfPage(int pageNumber) 
	{
		checkPageNumber(pageNumber);
		int index = pageNumber - 1;
		return pageContentList.get(index).getLines();
	}
	
	public List<String> getLinesOfPage(String pageTextId) {
		return getLinesOfPage(findPageByTextIdentifier(pageTextId));
	}	
	
	/**
	 * Searches for the frist page containing pageTextId and returns its number (starting with 1)
	 * @param pageTextId
	 * @return page number or -1 if pageTextId was not found
	 */
	public int findPageByTextIdentifier(String pageTextId)
	{
		if (isInteger(pageTextId)) {
			return Integer.valueOf(pageTextId);
		}
		int toReturn = 0;

		for (PdfPageContent pageContent: pageContentList) {
			if (pageContent.getOriginalPageContent().contains(pageTextId)) {
				return ++toReturn;
			}
			++toReturn;
		}

		return -1;
	}


	// #################################################################
	//               P R I V A T E    M E T H O D S
	// #################################################################
	
	
	private void init()
    {
        try 
        {
            PdfReader pdfReader = new PdfReader(pdfFileName);
            int totalNumberOfPages = pdfReader.getNumberOfPages();
            pageContentList = new ArrayList<>();
            
            for (int pageNo = 1; pageNo <= totalNumberOfPages; pageNo++)  
            {
				String pageContentAsString;
				try {
					// reading some pdfs work only that way - reason unclear 
					pageContentAsString = PdfTextExtractor.getTextFromPage(pdfReader, pageNo);
				} catch (Exception e) {
					// reading some pdfs work only that way - reason unclear
					pageContentAsString = PdfTextExtractor.getTextFromPage(pdfReader, pageNo, new SimpleTextExtractionStrategy());  // do not reuse instance of SimpleTextExtractionStrategy() !!!
				}
				pageContentList.add(new PdfPageContent(pageNo, pageContentAsString));
            }
        } catch (IOException e) {
        	throw new RuntimeException(e);
        }
    }
	
	private void checkPageNumber(int pageNumber) {
		if (pageNumber < 1 || pageNumber > getPageNumber() ) {
			throw new IllegalArgumentException("Page number " + pageNumber + " out of range!");
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

	private boolean isInteger(String s) {
		char[] chars = s.trim().toCharArray();
		for (Character c: chars) {
			if (! Character.isDigit(c)) {
				return false;
			}
		}
		return true;
	}
	
}
