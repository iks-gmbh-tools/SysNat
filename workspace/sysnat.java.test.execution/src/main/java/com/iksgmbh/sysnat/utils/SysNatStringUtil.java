package com.iksgmbh.sysnat.utils;

import static com.iksgmbh.sysnat.utils.SysNatConstants.*;

import java.util.ArrayList;
import java.util.List;

public class SysNatStringUtil 
{
	
	public static String replaceSpacesByUnderscore(final String s) {
		return s.replaceAll(" ", "_");
	}
	
	public static String firstCharToLowerCase(final String s) {
		final String firstChar = "" + s.charAt(0);
		return firstChar.toLowerCase() + s.substring(1);
	}
	
	public static List<String> getTestCategoriesAsList(String testCategoriesAsString, String testId) 
	{
		List<String> toReturn = new ArrayList<>();
		
		if (testCategoriesAsString != null)  
		{
			String[] splitResult = testCategoriesAsString.split(",");
			if (splitResult.length == 1 && splitResult[0].trim().length() == 0) {				
				toReturn.add(NO_FILTER);
				testCategoriesAsString = NO_FILTER;
			} else 
			{				
				for (String category : splitResult) 
				{
					category = category.replace('_', ' ');
					if (SysNatLocaleConstants.FROM_PACKAGE.equals(category)) 
					{
						String[] splitResult2 = testId.split("_");
						for (String category2 : splitResult2) {
							toReturn.add(category2);
						}
					} else {
						toReturn.add(category);
					}
				}
			}
		}
		
		return toReturn;
	}

    public static String replaceEmptyStringSymbol(String value) {
		return value.replace('_', ' ');
	}

    public static String cutPrefix(String s, String prefix) {
		return s.substring(prefix.length());
	}

	public static String firstCharTuUpper(String s) {
		char[] stringArray = s.toCharArray();
		stringArray[0] = Character.toUpperCase(stringArray[0]);
		return new String(stringArray);
	}

	public static int countNumberOfOccurrences(String s, String searchSubString) 
	{
		int countToReturn = 0;
		
		while (s.contains(searchSubString)) {
			countToReturn++;
			int pos = s.indexOf(searchSubString);
			s = s.substring(pos + searchSubString.length());
		}
		
		return countToReturn;
	}
}
