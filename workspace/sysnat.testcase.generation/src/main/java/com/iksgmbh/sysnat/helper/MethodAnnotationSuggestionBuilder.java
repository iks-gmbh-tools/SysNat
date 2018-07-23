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
		instructionLine = instructionLine.replaceAll("\"", "^");
		final String annotationValueString = buildAnnotationValue(instructionLine);
		final String paramsString = buildParamsString(instructionLine);
		final String returnValueString = buildReturnValueString(instructionLine);
		
		return "@LanguageTemplate(value = \"" + annotationValueString + "\")" + System.getProperty("line.separator") +
		       "public " + returnValueString + " replaceByAnExpressiveMethodName(" + paramsString + ")" + System.getProperty("line.separator") +
		       "    // Implement here code that corresponds to the Language Template's promise." + System.getProperty("line.separator") +
		       "}";
	}

	static String buildReturnValueString(String s) 
	{
		int pos1 = s.indexOf('<');
		int pos2 = s.indexOf('>');
		
		if (pos1 > -1 && pos2 > pos1) {
			return "Object";
		} else {
			return "void";
		}
	}

	static String buildParamsString(String s) 
	{
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

	static String buildAnnotationValue(String s) 
	{
		String toReturn = deleteAllCharsBetween(s, "<", ">");
		
		boolean goOn = true;
		while (goOn) 
		{
			s=toReturn;
			toReturn = deleteAllCharsBetween(toReturn, "'", "'");
			goOn = ! toReturn.equals(s);
		}
		
		goOn = true;
		while (goOn) 
		{
			toReturn = deleteAllCharsBetween(toReturn, "^", "^");
			goOn = ! toReturn.equals(s);
			s=toReturn;
		}
		
		return toReturn;
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
