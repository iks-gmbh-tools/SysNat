package com.iksgmbh.sysnat.utils;

import java.util.LinkedHashMap;

import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;

public class NaturalLanguageAnalyseUtil
{
	public static final String PROBLEM = "Problem: ";
	public static final String UNKOWN_VALUE_PARAMETER = "Unkown value parameter: ";
	public static final String UNKOWN_TESTOBJECT_PARAMETER = "Unkown test object parameter: ";
	public static final String TEST_OBJECT_OPERATOR = ":";
	public static final String DATA_VALUE_OPERATOR = "=";
	
	
	/**
	 * Analy
	 * @param nl to analyse
	 * @param methodParameters (list of string values and/or test objects)
	 * @param string 
	 * @return analyse result dnull (for success) or Problem Message
	 */
	public static String analyseNL(String nl, String nexValueParameter, LinkedHashMap<String, String> methodParameters)
	{
		if (nl.trim().isEmpty()) {
			return PROBLEM + "Enter complete Natural Language Expression";
		}
		String result = checkAngleBrackets(nl);
		if (result.startsWith(PROBLEM)) return result;
		result = checkCarets(nl, methodParameters.size());
		if (result.startsWith(PROBLEM)) return result;
		
		boolean ok = false;
		while (! ok) 
		{
			boolean isTestObjectParameter = getNextParameterSymol(nl, methodParameters.size()).equals("'");
			if (isTestObjectParameter) {
				result = checkApostrophies(nl, nexValueParameter, methodParameters);
			} else {
				result = checkDoubleQoutes(nl, nexValueParameter, methodParameters);
			}
			nexValueParameter = "";  // set this now to empty for all following while loops
			if (result != null) 
			{
				if (result.startsWith(PROBLEM) || result.startsWith(UNKOWN_TESTOBJECT_PARAMETER) || result.startsWith(UNKOWN_VALUE_PARAMETER)) return result;
				return nl;
			}
		}
		
		return result;
	}

	private static String getNextParameterSymol(String s, int orderNumber)
	{
		for (int i = 0; i < orderNumber; i++) {
			s = cutNextParameter(s);
			
		}
		int pos1 = s.indexOf("\"");
		int pos2 = s.indexOf("'");
		
		if (pos2 == -1 || (pos1 > -1 && pos1 < pos2)) {
			return "\"";
		}
		
		return "'";
	}

	private static String checkAngleBrackets(String nl)
	{
		String s = nl;
		int pos1 = s.indexOf("<");
		if (pos1 == -1) {
			pos1 = s.indexOf(">");
			if (pos1 > -1) {
				return PROBLEM + "There is a single Angle Bracket (>)!";
			} else {
				return nl;
			}
		} else {
			int pos2 = s.indexOf(">");
			if (pos2 == -1) return PROBLEM + "There is a single Angle Bracket (<)!";
			if (pos2 < pos1) return PROBLEM + "Wrong order of Angle Bracket (> <)!";
		}
		
		s = SysNatStringUtil.cutFirstOccurences(s, "<", ">");
		if (s.contains("<") || s.contains(">")) {
			return PROBLEM + "There is more than one Angle Bracket (< or > or both)!";
		}

		return nl;
	}

	private static String checkCarets(String nl, int numKnownParameters)
	{
		String s = nl;
		s = s.replace("^^", "");
		if (s.contains("^")) {
			return PROBLEM + "There is a single caret (^)!";
		}
		
		s = nl;
		int caretCount = 0; 
		while (s.contains("^^"))
		{
			caretCount++;
			if (caretCount <= numKnownParameters) {
				s = SysNatStringUtil.cutFirstOccurences(s, "^^");
			} else {
				return PROBLEM + "Parameter Name for ^^ is missing!";
			}
		}
		
		return nl;
	}

	private static String checkApostrophies(String nl, String nexValueParameter, LinkedHashMap<String, String> valueParameters)
	{
		String s = nl;
		if (! s.contains("'")) return nl; 
		
		while (s.contains("'"))
		{
			int pos = s.indexOf("'");
			s = s.substring(pos+1);
			pos = s.indexOf("'");
			if (pos == -1) return PROBLEM + "There is a single apostrophy!";
			s = s.substring(pos+1);
		}
		
		
		s = nl;
		int parameterCount = 0;
		while (s.contains("\"") || s.contains("'")) 
		{
			parameterCount++;
			if (valueParameters.size() >= parameterCount) {
				s = cutNextParameter(s);
			} 
			else 
			{
				if (nexValueParameter.isEmpty()) {
					return UNKOWN_TESTOBJECT_PARAMETER + getNextValue(s);
				} else {
					valueParameters.put(nexValueParameter, TEST_OBJECT_OPERATOR + getNextValue(s));
					return null;
				}
			}
		}
		
		return nl;
	}


	private static String checkDoubleQoutes(String nl, String nexValueParameter, LinkedHashMap<String, String> valueParameters)
	{
		String s = nl;
		if (! s.contains("\"")) return nl; 
		
		while (s.contains("\""))
		{
			int pos = s.indexOf("\"");
			s = s.substring(pos+1);
			pos = s.indexOf("\"");
			if (pos == -1) return PROBLEM + "There is a single Double Qoute!";
			s = s.substring(pos+1);
		}
		
		s = nl;
		int parameterCount = 0;
		while (s.contains("\"") || s.contains("'")) 
		{
			parameterCount++;
			if (valueParameters.size() >= parameterCount) {
				s = cutNextParameter(s);
			} 
			else 
			{
				if (nexValueParameter.isEmpty()) {
					return UNKOWN_VALUE_PARAMETER + getNextValue(s);
				} else {
					valueParameters.put(nexValueParameter, DATA_VALUE_OPERATOR + getNextValue(s));
					return null;
				}
			}
		}
		
		return nl;
	}

	private static String cutNextParameter(String s)
	{
		int pos1 = s.indexOf("\"");
		int pos2 = s.indexOf("'");
		
		if (pos2 == -1 || (pos1 > -1 && pos1 < pos2)) {
			String parameter = s.substring(pos1+1);
			pos1 = parameter.indexOf("\"");
			parameter = parameter.substring(0, pos1);
			return s.replace("\"" + parameter + "\"", "^^");	
		}
		
		String parameter = s.substring(pos2+1);
		pos2 = parameter.indexOf("'");
		parameter = parameter.substring(0, pos2);
		return s.replace("'" + parameter + "'", "°°");	
		
	}


	private static String getNextValue(String s)
	{
		int pos1 = s.indexOf("\"");
		int pos2 = s.indexOf("'");
		
		if (pos2 == -1 || (pos1 > -1 && pos1 < pos2)) {
			String parameterValue = s.substring(pos1+1);
			pos1 = parameterValue.indexOf("\"");
			return parameterValue.substring(0, pos1);
		}
		
		String parameterValue = s.substring(pos2+1);
		pos2 = parameterValue.indexOf("'");
		return parameterValue.substring(0, pos2);
	}
	
}
