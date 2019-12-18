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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.iksgmbh.sysnat.common.utils.SysNatConstants.ApplyIgnoreLineDefinitionScope;

/**
 * Stores each line of a single page of a document with its original line number.
 * Gaps in line numbers may come into existence 
 * when {@link DocumentContent.apply(DocumentCompareIgnoreConfig)} has been used.
 *  
 * @author Reik Oberrath
 */
public class PageContent 
{
	private int pageNo;
	private String orignalPageContent;
	private LinkedHashMap<Integer, String> lines = new LinkedHashMap<>();

	public PageContent(int aPageNo) {
		this.pageNo = aPageNo;
	}

	public PageContent(int aPageNo, String pageContentAsString) 
	{
		this.orignalPageContent = pageContentAsString;
		this.pageNo = aPageNo;
		initMap(pageContentAsString);
	}

	// #################################################################
	//               P U B L I C    M E T H O D S
	// #################################################################
	
	
	public void addLine(int lineNo, String line) {
		lines.put(Integer.valueOf(lineNo), line);
	}

	public void addLines(List<String> lines) 
	{
		int lineCounter = 0;
		for (String line : lines) {
			lineCounter++;
			addLine(lineCounter, line);
		}
	}
	
	public int getPageNumber() {
		return pageNo;
	}

	public int getNumberOfLines() {
		return lines.size();
	}

	public String getOriginalPageContent() {
		return orignalPageContent;
	}
	
	public String getPageContentAsString() {
		final StringBuffer sb = new StringBuffer();
		getLines().forEach(line -> sb.append(line).append(System.getProperty("line.separator")));
		return sb.toString().trim();
	}
	
	/**
	 * @param ignoreConfig
	 * @param scope provides information whether this PDFPageContent 
	 *        is regarded as first or second in the comparison
	 */
	public void apply(DocumentCompareIgnoreConfig ignoreConfig, ApplyIgnoreLineDefinitionScope scope) 
	{
		List<Integer> lineNumbersToIgnore = ignoreConfig.getLinesToIgnore(this, scope);
		lineNumbersToIgnore.forEach(lineNo -> lines.remove(lineNo));
	}
	
	public List<String> getLines() 
	{
		final List<String> toReturn = new ArrayList<String>();
		toReturn.addAll(lines.values());
		return toReturn;
	}

	public int getOriginalNumberOfLineInPage(int index) {
		return (int) lines.keySet().toArray()[index];
	}

	// #################################################################
	//               P R I V A T E    M E T H O D S
	// #################################################################
	
	private void initMap(String pageContentAsString) 
	{
		String[] splitResult = pageContentAsString.split("\\r?\\n");
		int lineNo = 0;
		
		for (String line : splitResult) {
			lineNo++;
			lines.put(Integer.valueOf(lineNo), line.trim());
		}
	}
	
}
