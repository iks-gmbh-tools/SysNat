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
package com.iksgmbh.sysnat.helper;

public class MethodAnnotationSuggestionBuilder 
{
	private static final String REPLACEMENT_MARKER = "|#|";

	/**
	 * Constructs a suggestion for the SNT developer how a matching annotation value would look like.
	 * @param instructionLine 
	 * @return
	 */
	public static String buildAnnotationSuggestion(String instructionLine) 
	{
		final String annotationValueString = buildAnnotationValue(instructionLine);
		final String paramsString = buildParamsString(instructionLine);
		final String returnValueString = buildReturnValueString(instructionLine);
		
		return "@LanguageTemplate(value = \"" + annotationValueString + "\")" + System.getProperty("line.separator") +
		       "public " + returnValueString + " replaceByAnExpressiveMethodName(" + paramsString + ")" + System.getProperty("line.separator") +
		       "{" + System.getProperty("line.separator") +
		       "    // Implement here code that corresponds to the Language Template's promise." + System.getProperty("line.separator") +
		       "    // And don't forget to create a report message entry!" + System.getProperty("line.separator") +
		       "}";
	}

	public static String buildReturnValueString(String s) 
	{
		int pos1 = s.indexOf('<');
		int pos2 = s.indexOf('>');
		
		if (pos1 > -1 && pos2 > pos1) {
			return "Object";
		} else {
			return "void";
		}
	}

	public static String buildParamsString(String s) 
	{
		s = s.replaceAll("\"", "^");
		int pos1 = s.indexOf('^');
		int pos2 = s.indexOf('\'');
		String toReturn = "";
		
		while (pos1 != -1 || pos2 != -1) 
		{
			ParseParamResult result = parseNextParam(s, pos1, pos2);
			s = result.unparsedRest;
			toReturn += ", " + result.parsedParam;
			pos1 = s.indexOf('^');
			pos2 = s.indexOf('\'');
		}
		
		if (toReturn.length() > 0) {
			toReturn = toReturn.substring(2);
		}
		return toReturn;
	}

	private static ParseParamResult parseNextParam(String s, int pos1, int pos2) 
	{
		ParseParamResult result;
		
		if (pos1 == -1) {
			if (pos2 == -1) {
				return new ParseParamResult("", "");
			} else {
				result = parseNextParamByApostrophy(s);
			}
		} else {
			if (pos2 == -1) {
				result = parseNextParamByCaretSymbol(s);
			} else {
				if (pos1 < pos2) {
					result = parseNextParamByCaretSymbol(s);
				} else {
					result = parseNextParamByApostrophy(s);
				}
			}
		}
	
		return result;
	}

	private static ParseParamResult parseNextParamByCaretSymbol(String s) 
	{
		int pos = s.indexOf('^');
		s = s.substring(pos+1);
		pos = s.indexOf('^');
		return new ParseParamResult("String " + s.substring(0, pos), s.substring(pos+1));
	}

	private static ParseParamResult parseNextParamByApostrophy(String s) 
	{
		int pos = s.indexOf('\'');
		s = s.substring(pos+1);
		pos = s.indexOf('\'');
		return new ParseParamResult("Object " + s.substring(0, pos), s.substring(pos+1));
	}

	public static String buildAnnotationValue(String instruction) 
	{
		String toReturn = deleteAllCharsBetween(instruction, "<", ">");
		
		boolean goOn = true;
		while (goOn) 
		{
			toReturn = deleteAllCharsBetween(toReturn, "'", "'");
			goOn = toReturn.replace("''", "").contains("'");
		}
		
		goOn = true;
		while (goOn) 
		{
			toReturn = deleteAllCharsBetween(toReturn, "\"", "\"");
			goOn = toReturn.replace("\"\"", "").contains("\"");
		}
		
		return toReturn.replace("\"", "^");
	}

	static String deleteAllCharsBetween(String s, String id1, String id2) 
	{
		s = s.replace(id1+id2, REPLACEMENT_MARKER);
		
		int pos = s.indexOf(id1) + 1;
		final String toReturn;
		
		if (pos > 0) {
			String part1 = s.substring(0, pos);
			s = s.substring(pos);
			pos = s.indexOf(id2);
			String part2 = s.substring(pos);
			toReturn = part1 + part2;
		} else {
			toReturn = s;
		}
		
		
		return toReturn.replace(REPLACEMENT_MARKER, id1+id2);
	}

	static class ParseParamResult 
	{
		String unparsedRest;
		String parsedParam;
		
		public ParseParamResult(String parsedParam, String unparsedRest) {
			this.unparsedRest = unparsedRest;
			this.parsedParam = parsedParam;
		}
		
	}
}