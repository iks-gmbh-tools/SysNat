package com.iksgmbh.sysnat.common.helper.pdftooling;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.iksgmbh.sysnat.common.helper.pdftooling.PdfCompareIgnoreConfig.ApplyIgnoreLineDefinitionScope;

/**
 * Stores each line of a single page with the original line numbers.
 * Gaps in line numbers may come into existence when {@link PdfPageContent.apply(PdfCompareIgnoreConfig)} has been called.
 *  
 * @author Reik Oberrath
 */
public class PdfPageContent 
{
	private int pageNo;
	private String orignalPageContent;
	private LinkedHashMap<Integer, String> lines = new LinkedHashMap<>();


	public PdfPageContent(int aPageNo) {
		this.pageNo = aPageNo;
	}

	public PdfPageContent(int aPageNo, String pageContentAsString) 
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
	public void apply(PdfCompareIgnoreConfig ignoreConfig, ApplyIgnoreLineDefinitionScope scope) 
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
