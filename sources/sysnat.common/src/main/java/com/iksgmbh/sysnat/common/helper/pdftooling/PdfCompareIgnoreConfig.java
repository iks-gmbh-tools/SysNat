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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Data used to identify lines that have to be ignored for comparing.
 * The following means exist to identify date lines:
 * 
 * a) lines that contain a date value (either whole line is a date value or the line ends or starts with a date value)
 * b) lines that contain at least one of the defined character sequences
 * c) lines that start with at least one of the defined prefixes
 * d) lines that match at least on of the defined regex expressions
 * e) lines that match one of the defined line definition (scope-pageno-lineno-combinations)
 */
public class PdfCompareIgnoreConfig 
{
	public static enum ApplyIgnoreLineDefinitionScope { PDF1, PDF2, BOTH };
	
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/Comparer", Locale.getDefault());
	private static final boolean sysoutIgnoredLines = false;
	
	public List<DateFormat> dateformats;  
	public List<String> substrings;
	public List<String> prefixes;
	public List<String> regexPatterns;
	public List<String> lineDefinitions;


	// Builder Methods
	
	public PdfCompareIgnoreConfig withDateformats(List<DateFormat> aDateFormatList) {
		this.dateformats = aDateFormatList;
		return this;
	}

	public PdfCompareIgnoreConfig withSubstrings(List<String> aSubstringList) {
		this.substrings = aSubstringList;
		return this;
	}
	
	public PdfCompareIgnoreConfig withPrefixes(List<String> aPrefixeList) {
		this.prefixes = aPrefixeList;
		return this;
	}

	public PdfCompareIgnoreConfig withRegexPatterns(List<String> aRegexPatternList) {
		this.regexPatterns = aRegexPatternList;
		return this;
	}
	
	public PdfCompareIgnoreConfig withLineDefinitions(List<String> aLineDefinitionsList) {
		this.lineDefinitions = aLineDefinitionsList;
		return this;
	}

	
	// public Methods
	
	public List<String> getRegexPatterns() {
		return regexPatterns;
	}
	
	public boolean checkForDateLines() {
		return dateformats != null && ! dateformats.isEmpty();
	}

	public boolean checkForPrefixes() {
		return prefixes != null && ! prefixes.isEmpty();
	}
	
	public boolean checkForSubstrings() {
		return substrings != null && ! substrings.isEmpty();
	}

	public boolean checkForPatterns() {
		return regexPatterns != null && ! regexPatterns.isEmpty();
	}

	public boolean checkForLineDefinitions() {
		return lineDefinitions != null && ! lineDefinitions.isEmpty();
	}
	
	public boolean isThisLineToBeIgnoredForComparison(ApplyIgnoreLineDefinitionScope scope, int pageNo, int lineNo, String line) 
	{
		if ( checkForDateLines() && isDateLine(line)) 
		{
			return true;
		} 
		else if ( checkForPatterns()  && doesLineMatchAnyPattern(line)) 
		{
			return true;
		} 
		else if ( checkForSubstrings() && doesLineContainsAnySubstring(line)) 
		{
			return true;
		}
		else if ( checkForPrefixes() && doesLineStartsWithAnyPrefix(line)) 
		{
			return true;
		} 
		else if ( checkForLineDefinitions() && doesLineMatchAnyLineDefinition(scope, pageNo, lineNo)) 
		{
				return true;
		}
		
		return false;
	}

	/**
	 * @param pageNo number of page in file
	 * @param pageContent
	 * @param name of file the pageContent is read from
	 * @return pageContent without ignored lines as String 
	 */
	public String removeLinesToIgnore(ApplyIgnoreLineDefinitionScope scope, 
			                          int pageNo, 
			                          String pageContent, 
			                          String filename) 
	{
		if (sysoutIgnoredLines) {
			System.out.println("");
			System.out.println("Ignored lines in " + filename + ":");
		}
		String[] splitResult = pageContent.split("\\r?\\n");
		StringBuffer sb = new StringBuffer();
		int lineCounter = 0;
		for (String line : splitResult) 
		{
			lineCounter++;
			if (isThisLineToBeIgnoredForComparison(scope, pageNo, lineCounter, line) ) 
			{
				if (sysoutIgnoredLines) System.out.println(lineCounter + ": " + line);
			} else {
				sb.append(line).append(System.getProperty("line.separator"));
			}
		}
		return sb.toString().trim();
	}
	
	/**
	 * 
	 * @param pageNo number of page the lines are read from the input file
	 * @param page content as list of lines
	 * @return lines not ignored
	 */
	public List<String> removeLinesToIgnore(int pageNo, List<String> lines) 
	{
		final List<String> toReturn = new ArrayList<String>();
		int lineCounter = 0;
		for (String line : lines) 
		{
			if (isThisLineToBeIgnoredForComparison(ApplyIgnoreLineDefinitionScope.BOTH, pageNo, lineCounter, line) ) 
			{
				// do nothing
			} else {
				toReturn.add(line);
			}
		}
		
		return toReturn;
	}

	public List<Integer> getLinesToIgnore(PdfPageContent pageContent) {
		return getLinesToIgnore(pageContent, ApplyIgnoreLineDefinitionScope.BOTH);
	}
	
	
	/**
	 * 
	 * @param scope2 
	 * @param pageNo number of page the lines are read from the input file
	 * @param page content as list of lines
	 * @return lines not ignored
	 */
	public List<Integer> getLinesToIgnore(PdfPageContent pageContent, ApplyIgnoreLineDefinitionScope scope) 
	{
		final List<Integer> toReturn = new ArrayList<>();
		int lineCounter = 0;
		List<String> lines = pageContent.getLines();
		for (String line : lines) 
		{
			lineCounter++;
			if (isThisLineToBeIgnoredForComparison(scope, pageContent.getPageNumber(), lineCounter, line) ) {
				toReturn.add(pageContent.getOriginalNumberOfLineInPage(lineCounter-1));
			}
		}
		
		return toReturn;
	}
	
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer("There are following configurations defined to ignore:");
		sb.append(System.getProperty("line.separator"));
		int sum = 0;
		
		if (dateformats != null && dateformats.size() > 0) {
			sum += dateformats.size();
			sb.append(dateformats.size() + " Date Format(s)").append(System.getProperty("line.separator"));
		}
		
		if (substrings != null && substrings.size() > 0) {
			sum += substrings.size();			
			sb.append(substrings.size() + " substring(s)").append(System.getProperty("line.separator"));
		}
		
		if (prefixes != null && prefixes.size() > 0) {
			sum += prefixes.size();			
			sb.append(prefixes.size() + " prefix(es)").append(System.getProperty("line.separator"));
		}
		
		if (regexPatterns != null && regexPatterns.size() > 0) {			
			sum += regexPatterns.size();			
			sb.append(regexPatterns.size() + " regex pattern(s)").append(System.getProperty("line.separator"));
		}
		
		if (lineDefinitions != null && lineDefinitions.size() > 0) {
			sum += lineDefinitions.size();		
			
			String ignoredPagesInfo = "";
			int ignoredPages = getNumberOfIgnoredPages();
			if (ignoredPages == 1) {
				ignoredPagesInfo = " (from these " + ignoredPages + " causes a whole page to be ignored)";
			}
			else if (ignoredPages > 1) {
				ignoredPagesInfo = " (from these " + ignoredPages + " cause a whole page to be ignored)";
			}
			sb.append(lineDefinitions.size() + " line definition(s)")
			  .append(ignoredPagesInfo)
			  .append(System.getProperty("line.separator"));
		}
		
		if (sum == 0) {
			return "";
		}
		
		return sb.toString();
	}
	
	private int getNumberOfIgnoredPages()
	{
		return lineDefinitions.stream()
				              .filter(line -> line.endsWith("*"))
				              .collect(Collectors.counting())
				              .intValue();
	}

	// #####################################################################
	//                         private  methods
	// #####################################################################

	private boolean doesLineContainsAnySubstring(String lineWithoutSpace) 
	{
		for (String substring : substrings) 
		{
			if (lineWithoutSpace.contains(substring) || lineWithoutSpace.equals(substring)) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean doesLineStartsWithAnyPrefix(String lineWithoutSpace) 
	{
		for (String prefix : prefixes) 
		{
			if (lineWithoutSpace.startsWith(prefix) || lineWithoutSpace.equals(prefix)) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean doesLineMatchAnyPattern(String line) 
	{
		String lineWithoutSpace = line.replaceAll(" ", "");
		for (String pattern : regexPatterns) 
		{
			if (lineWithoutSpace.matches(pattern)) {
				return true;
			}
		}
		
		return false;
	}


	private boolean doesLineMatchAnyLineDefinition(ApplyIgnoreLineDefinitionScope scope, int pageNo, int lineNo) 
	{
		
		for (String lineDefinition : lineDefinitions) 
		{
			String lineNoAsString = "" + lineNo;
			if (lineDefinition.endsWith("*")) {
				lineNoAsString = "*";
			}
			
			String lineDefinitionIdentifier;
			if (lineDefinition.startsWith("BOTH")) {
				lineDefinitionIdentifier = buildLineDefinitionIdentifier(ApplyIgnoreLineDefinitionScope.BOTH, pageNo, lineNoAsString); 
			} else {
				lineDefinitionIdentifier = buildLineDefinitionIdentifier(scope, pageNo, lineNoAsString); 
			}
			
			if (lineDefinitionIdentifier.equals(lineDefinition)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	private String buildLineDefinitionIdentifier(ApplyIgnoreLineDefinitionScope scope, int pageNo, String lineNo) 
	{
		String toReturn = BUNDLE.getString("PAGE") + " " + pageNo + ", " + BUNDLE.getString("LINE") + " " + lineNo;
		if (scope == ApplyIgnoreLineDefinitionScope.BOTH) {
			return toReturn;
		}
		
		return scope.name() + ", " + toReturn;
	}
	
	private boolean isDateLine(String lineWithoutSpace) {
		return isDate(lineWithoutSpace) || startsWithDate(lineWithoutSpace) || endsWithDate(lineWithoutSpace); 
	}
	

	private boolean isDate(String s) 
	{
		return dateformats.stream()
				          .filter(dateformat -> isDateLine(s, dateformat))
				          .findFirst()
				          .isPresent();
	}	
	
	private boolean isDateLine(String lineWithoutSpace, DateFormat dateformat) 
	{
        try {
        	dateformat.parse(lineWithoutSpace);
            return true;
        } catch (Exception e) {
            return false;
        }	
	}

	private boolean startsWithDate(String lineWithoutSpace) 
	{
		if (lineWithoutSpace.length() < 11)
			return false;
		
		return isDate(lineWithoutSpace.substring(0,  10));
	}
	
	private boolean endsWithDate(String lineWithoutSpace) 
	{
		int length = lineWithoutSpace.length();
		if (length < 11)
			return false;
		
		return isDate(lineWithoutSpace.substring(length-10));
	}
	
}
