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
package com.iksgmbh.sysnat.helper.docval.domain;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.helper.docval.filereader.SysNatPdfReader;
import com.iksgmbh.sysnat.helper.docval.filereader.SysNatTextFileReader;

/**
 * Reads and stores content of a document file.
 * It uses the class {@link PageContent} to store file content for each page separately. 
 * Currently only PDF documents are supported.
 * 
 * @author Reik Oberrath
 */
public class DocumentContent
{	
	private final static List<String> TEXT_FILE_FORMATS = new ArrayList<>();
	
	static {
		TEXT_FILE_FORMATS.add("txt");
		TEXT_FILE_FORMATS.add("xml");
		TEXT_FILE_FORMATS.add("properties");
		TEXT_FILE_FORMATS.add("config");
		TEXT_FILE_FORMATS.add("ini");
	}
	
	private String fileName;
    private List<PageContent> pageContentList;

	public DocumentContent(String aFileName) {
		this.fileName = SysNatFileUtil.findAbsoluteFilePath(aFileName);
		this.init();
	}

	public DocumentContent(File aPdfFile) {
		this.fileName = aPdfFile.getAbsolutePath();
		this.init();
	}
	
	// #################################################################
	//               P U B L I C    M E T H O D S
	// #################################################################

	public String getPdfFileName() {
		return fileName;
	}
	
	public int getNumberOfPages() {
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
		if (pageNumber < 1 || pageNumber > getNumberOfPages() ) {
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
	

	public PageContent getPageContent(int pageNo) {
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

		for (PageContent pageContent: pageContentList) {
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
		if (fileName == null) {
			throw new SysNatTestDataException("No file defined.");
		}
		
		int pos = fileName.lastIndexOf(".");
		if (pos ==-1) {
			throw new SysNatTestDataException("Please use filename with extension "
					+ "(current filename is '" + fileName + "').");
		}
		
		String extension = fileName.substring(pos + 1);
		
		if (extension.equalsIgnoreCase("pdf")) {
			pageContentList = SysNatPdfReader.doYourJob(fileName);
		} else if (TEXT_FILE_FORMATS.contains(extension)) {
				pageContentList = SysNatTextFileReader.doYourJob(fileName);
		} else {
			throw new SysNatTestDataException("Unsupported file type: " + extension);
		}
    }
	
	private void checkPageNumber(int pageNumber) {
		if (pageNumber < 1 || pageNumber > getNumberOfPages() ) {
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
