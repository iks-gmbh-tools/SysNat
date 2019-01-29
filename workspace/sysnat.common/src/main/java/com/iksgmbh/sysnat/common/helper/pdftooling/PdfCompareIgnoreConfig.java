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

/**
 * Data used to identify lines that have to be ignored for comparing. 
 */
public class PdfCompareIgnoreConfig 
{
	private static final boolean sysoutIgnoredLines = false;
	
	public DateFormat dateformat;  
	public List<String> substrings;
	public List<String> prefixes;
	public List<String> patterns;
	
	
	public PdfCompareIgnoreConfig(DateFormat aDateformat, 
			            		  List<String> aSubstringList, 
			            		  List<String> aPrefixeList,
			            		  List<String> aPatternList) 
	{
		this.dateformat = aDateformat;
		if (dateformat != null) dateformat.setLenient(false);
		this.substrings = aSubstringList;
		this.prefixes = aPrefixeList;
		this.patterns = aPatternList;
	}
	
	public boolean checkForDateLines() {
		return dateformat != null;
	}

	public boolean checkForPrefixes() {
		return prefixes != null;
	}
	
	public boolean checkForSubstrings() {
		return substrings != null;
	}

	public boolean checkForPatterns() {
		return patterns != null;
	}

	public boolean ignoreThisLineForComparison(String line) 
	{
		if ( checkForDateLines() && isDateLine(line, dateformat)) 
		{
			return true;
		} 
		else if ( checkForPatterns()  && doesLineMatchAnyPattern(line.replaceAll(" ", ""), patterns)) 
		{
			return true;
		} 
		else if ( checkForPrefixes() && doesLineStartsWithAnyPrefix(line, prefixes)) 
		{
			return true;
		} 
		else if ( checkForSubstrings() && doesLineContainsAnySubstring(line, substrings)) 
		{
			return true;
		}
		
		return false;
	}

	public String applyIgnoreConfig(String page, String filename) 
	{
		if (sysoutIgnoredLines) {
			System.out.println("");
			System.out.println("Ignored lines in " + filename + ":");
		}
		String[] splitResult = page.split("\\r?\\n");
		StringBuffer sb = new StringBuffer();
		@SuppressWarnings("unused")
		int counter = 0;
		for (String line : splitResult) 
		{
			counter++;
			if (ignoreThisLineForComparison(line) ) 
			{
				if (sysoutIgnoredLines) System.out.println(counter + ": " + line);
			} else {
				sb.append(line).append(System.getProperty("line.separator"));
			}
		}
		return sb.toString().trim();
	}

	public List<String> removeLinesToIgnore(List<String> lines) 
	{
		final List<String> toReturn = new ArrayList<String>();
		for (String line : lines) 
		{
			if (ignoreThisLineForComparison(line) ) 
			{
				// do nothing
			} else {
				toReturn.add(line);
			}
		}
		
		return toReturn;
	}
	
	private boolean doesLineContainsAnySubstring(String lineWithoutSpace, List<String> substrings) 
	{
		for (String substring : substrings) 
		{
			if (lineWithoutSpace.contains(substring) || lineWithoutSpace.equals(substring)) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean doesLineStartsWithAnyPrefix(String lineWithoutSpace, List<String> prefixes) 
	{
		for (String prefix : prefixes) 
		{
			if (lineWithoutSpace.startsWith(prefix) || lineWithoutSpace.equals(prefix)) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean doesLineMatchAnyPattern(String lineWithoutSpace, List<String> patterns) 
	{
		for (String pattern : patterns) 
		{
			if (lineWithoutSpace.matches(pattern)) {
				return true;
			}
		}
		
		return false;
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

	
}
