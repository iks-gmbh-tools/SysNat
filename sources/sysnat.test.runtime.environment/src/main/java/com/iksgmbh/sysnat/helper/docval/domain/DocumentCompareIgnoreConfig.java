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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.iksgmbh.sysnat.common.utils.SysNatConstants.ApplyIgnoreLineDefinitionScope;

/**
 * This data class stores information to identify lines that have to be ignored for comparing two documents.
 * The following means exist to identify date lines:
 * 
 * a) lines that contain a date value (either whole line is a date value or the line ends or starts with a date value)
 * b) lines that contain at least one of the defined character sequences
 * c) lines that start with at least one of the defined prefixes
 * d) lines that match at least on of the defined regex expressions
 * e) lines that match one of the defined line definition (scope-pageno-lineno-combinations)
 */
public class DocumentCompareIgnoreConfig 
{
	private static final boolean sysoutIgnoredLines = false;
	
	public List<DateFormat> dateformats;  
	public List<String> substrings;
	public List<String> prefixes;
	public HashMap<String,String> ignoreBetweenIdentifier;
	public List<String> regexPatterns;
	public List<String> lineDefinitions;
	
	public static void main(String[] args) 
	{
		String s = "Für den Dokumentenvergleich wird Zeile 1 auf Seite 2 im ersten Dokument ignoriert.";
		String r = "Für den Dokumentenvergleich wird Zeile .* auf Seite .* im ersten Dokument ignoriert.";
		System.out.println(s.matches(r));
		 
//		String r = "bu.*bu.*bu";
//		System.out.println("bbu".matches(r));
//		System.out.println("bububu".matches(r));
//		System.out.println("bu buaaaabu".matches(r));
//		System.out.println("buabu".matches(r));
//		System.out.println("buaaaabu ".matches(r));
	}


	// Builder Methods
	
	public DocumentCompareIgnoreConfig withDateformats(List<DateFormat> aDateFormatList) {
		this.dateformats = aDateFormatList;
		return this;
	}

	public DocumentCompareIgnoreConfig withSubstrings(List<String> aSubstringList) {
		this.substrings = aSubstringList;
		return this;
	}
	
	public DocumentCompareIgnoreConfig withPrefixes(List<String> aPrefixeList) {
		this.prefixes = aPrefixeList;
		return this;
	}

	public DocumentCompareIgnoreConfig withRegexPatterns(List<String> aRegexPatternList) {
		this.regexPatterns = aRegexPatternList;
		return this;
	}
	
	public DocumentCompareIgnoreConfig withLineDefinitions(List<String> aLineDefinitionsList) {
		this.lineDefinitions = aLineDefinitionsList;
		return this;
	}
	
	public DocumentCompareIgnoreConfig withIgnoreBetweenIdentifier(HashMap<String,String>  someIgnoreBetweenIdentifier) {
		this.ignoreBetweenIdentifier = someIgnoreBetweenIdentifier;
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
		else if ( checkForPatterns()  && doesLineMatchAnyRegexPattern(line)) 
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

	public List<Integer> getLinesToIgnore(PageContent pageContent) {
		return getLinesToIgnore(pageContent, ApplyIgnoreLineDefinitionScope.BOTH);
	}
	
	
	/**
	 * 
	 * @param scope2 
	 * @param pageNo number of page the lines are read from the input file
	 * @param page content as list of lines
	 * @return lines not ignored
	 */
	public List<Integer> getLinesToIgnore(PageContent pageContent, ApplyIgnoreLineDefinitionScope scope) 
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
		
		if (lineDefinitions != null && lineDefinitions.size() > 0) 
		{
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
		
		if (ignoreBetweenIdentifier != null && ignoreBetweenIdentifier.size() > 0) {			
			sum += ignoreBetweenIdentifier.size();			
			sb.append(ignoreBetweenIdentifier.size() + " IgnoreBetweenIdentifier").append(System.getProperty("line.separator"));
		}
		
		
		if (sum == 0) {
			return "";
		}
		
		return sb.toString();
	}

	public void addDateformat(DateFormat aDateFormat) 
	{
		if (dateformats == null) dateformats = new ArrayList<DateFormat>();
		this.dateformats.add(aDateFormat);
	}

	public void addSubstring(String aSubstring) {
		if (substrings == null) substrings = new ArrayList<String>();
		this.substrings.add(aSubstring);
	}
	
	public void addPrefix(String aPrefix) {
		if (prefixes == null) prefixes = new ArrayList<String>();
		this.prefixes.add(aPrefix);
	}

	public void addIgnoreBetween(String startIdentifier, String endIdentifier) {
		if (ignoreBetweenIdentifier == null) ignoreBetweenIdentifier = new HashMap<>();
		this.ignoreBetweenIdentifier.put(startIdentifier, endIdentifier);
	}
	
	public void addRegex(String aRegex) {
		if (regexPatterns == null) regexPatterns = new ArrayList<String>();
		this.regexPatterns.add(aRegex);
	}
	
	public void addLineDefinition(String aLineDefinition) {
		if (lineDefinitions == null) lineDefinitions = new ArrayList<String>();
		this.lineDefinitions.add(aLineDefinition);
	}	
	
	// #####################################################################
	//                         private  methods
	// #####################################################################
	
	private int getNumberOfIgnoredPages()
	{
		return lineDefinitions.stream()
				              .filter(line -> line.endsWith("*"))
				              .collect(Collectors.counting())
				              .intValue();
	}

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
	
	private boolean doesLineMatchAnyRegexPattern(String line) 
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
			if (lineDefinition.endsWith(":*")) {
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
	
	
	public static String buildLineDefinitionIdentifier(ApplyIgnoreLineDefinitionScope scope, 
			                                           int pageNo, 
			                                           String lineNo) 
	{
		// must match DocumentContentCompareValidationRule.buildLineDefinitionIdentifier !!!
		return scope.name() + ":" + pageNo + ":" + lineNo;
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
