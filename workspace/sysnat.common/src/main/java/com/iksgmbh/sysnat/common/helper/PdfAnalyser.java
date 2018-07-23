package com.iksgmbh.sysnat.common.helper;

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
public class PdfAnalyser
{	
	private String pdfFileName;
    private List<String> pages;

	public PdfAnalyser(String aPdfFileName) {
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

