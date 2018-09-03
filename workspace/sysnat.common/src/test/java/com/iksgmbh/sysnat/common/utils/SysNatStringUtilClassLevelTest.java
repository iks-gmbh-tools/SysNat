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
package com.iksgmbh.sysnat.common.utils;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class SysNatStringUtilClassLevelTest 
{
	@Test
	public void returnsLeadingWhiteSpace() {
		assertEquals("White space", "     ", SysNatStringUtil.getLeadingWhiteSpace("     a"));
	}

	@Test
	public void countsOccurrences() 
	{
		final String searchSubString = "xyz";
		final String s = "abc" + System.getProperty("line.separator") +
		                 "abc " + searchSubString + "abc" + searchSubString + System.getProperty("line.separator") +
		                 searchSubString;
		
		assertEquals("Number of occurrences", 3 , SysNatStringUtil.countNumberOfOccurrences(s, searchSubString));
	}

	@Test
	public void findsDifferenceBetweenLists() 
	{
		// arrange
		List<String> smallList = new ArrayList<String>();
		smallList.add("A");
		smallList.add("B");
		List<String> bigList = new ArrayList<String>();
		bigList.add("C");
		bigList.add("A");
		bigList.add("D");
		bigList.add("B");
		bigList.add("E");
		
		// act
		final List<String> result = SysNatStringUtil.findDifferingElements(bigList, smallList);
		
		// assert
		assertEquals("Number of differing elements", 3, SysNatStringUtil.findDifferingElements(bigList, smallList).size());
		assertEquals("First differing Element", "C", result.get(0));
	}
	
	

	@Test
	public void cutsTrailingDigits() throws Exception 
	{
		// arrange
		final String text = "abc123";

		// act
		final String result = SysNatStringUtil.cutTrailingDigits(text);

		// assert
		assertEquals("Text", "abc", result);
	}	

	@Test
	public void cutsTrailingChars() throws Exception 
	{
		// arrange
		final String text = "abc_.-0123456789";
		char[] charsToCut = {'0','1','2','3','4','5','6','7','8','9', '-','_', '.' };

		// act
		final String result = SysNatStringUtil.cutTrailingChars(text, charsToCut);

		// assert
		assertEquals("Text", "abc", result);
	}
	
	@Test
	public void extractsTrailingDigits() throws Exception 
	{
		// arrange
		final String text1 = "abc_0_0123456789";
		final String text2 = "abc_0_0123456789a";

		// act
		final String result1 = SysNatStringUtil.extraxtTrailingDigits(text1);
		final String result2 = SysNatStringUtil.extraxtTrailingDigits(text2);

		// assert
		assertEquals("Text1", "0123456789", result1);
		assertEquals("Text2", "", result2);
	}
	
}