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
import java.util.Arrays;
import java.util.List;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;

/**
 * Provides methods to analyze the content of a single PDF file per page and line.
 * 
 * @author Reik Oberrath
 */
public class PdfPageContentAnalyser
{	
	private String pdfFileName;
    private List<String> pages;

	public PdfPageContentAnalyser(String aPdfFileName) {
		this.pdfFileName = aPdfFileName;
		this.init();
	}

	private void init()
    {
        try 
        {
            PdfReader pdfReader = new PdfReader(pdfFileName);
            int totalNumberOfPages = pdfReader.getNumberOfPages();
            pages = new ArrayList<>();
            
            for (int i = 1; i <= totalNumberOfPages; i++)  
            {
				String textFromPage;
				try {
					// reading some pdfs work only that way - reason unclear 
					textFromPage = PdfTextExtractor.getTextFromPage(pdfReader, i);
				} catch (Exception e) {
					// reading some pdfs work only that way - reason unclear
					textFromPage = PdfTextExtractor.getTextFromPage(pdfReader, i, new SimpleTextExtractionStrategy());  // do not reuse instance of SimpleTextExtractionStrategy() !!!
				}
				pages.add(textFromPage);
            }
        } catch (IOException e) {
        	throw new RuntimeException(e);
        }
    }

	public int getPageNumber() {
		return pages.size();
	}

	public boolean doesLineContain(int pageNumber, int lineNumber, String toSearch) {
		return getLine(pageNumber, lineNumber).contains(toSearch);
	}

	public boolean doesLineEquals(int pageNumber, int lineNumber, String toSearch) {
		return getLine(pageNumber, lineNumber).equals(toSearch);
	}

	public String getLine(int pageNumber, int lineNumber)
	{
		if (pageNumber < 1 || pageNumber > getPageNumber() ) {
			throw new IllegalArgumentException("Page number " + pageNumber + " out of range!");
		}

		List<String> linesOfPage = getLinesOfPage(pageNumber);
		int index = lineNumber - 1;
		return linesOfPage.get(index);

	}

	/**
	 * 
	 * @param pageNumber
	 * @param toSearch
	 * @return
	 */
	public boolean doesPageContain(int pageNumber, String toSearch) 
	{
		if (pageNumber < 1 || pageNumber > getPageNumber() ) {
			throw new IllegalArgumentException("Page number " + pageNumber + " out of range!");
		}
		
		int index = pageNumber - 1;
		return pages.get(index).contains(toSearch);
	}
	
	public List<String> getLinesOfPage(int pageNumber) 
	{
		if (pageNumber < 1 || pageNumber > getPageNumber() ) {
			throw new IllegalArgumentException("Page number " + pageNumber + " out of range!");
		}
		
		int index = pageNumber - 1;
		String pageContent = pages.get(index);
		pageContent = pageContent.replaceAll("\\r\\n", "\\n").replaceAll("\\r", "\\n");
		return Arrays.asList( pageContent.split("\\n") );
	}
	
	
	public boolean doesPageContainIgnoreWhiteSpace(int pageNumber, String toSearch) 
	{
		if (pageNumber < 1 || pageNumber > getPageNumber() ) {
			throw new IllegalArgumentException("Page number " + pageNumber + " out of range!");
		}
		
		int index = pageNumber - 1;
		return compress(pages.get(index)).contains( compress(toSearch) );
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

}