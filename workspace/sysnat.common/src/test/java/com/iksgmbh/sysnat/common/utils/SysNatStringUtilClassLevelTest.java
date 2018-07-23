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
		char[] charsToCut = {'0','1','2','3','4','5','6','7','8','9','0','-','_', '.' };

		// act
		final String result = SysNatStringUtil.cutTrailingChars(text, charsToCut);

		// assert
		assertEquals("Text", "abc", result);
	}
}
