package com.iksgmbh.sysnat.helper;

import java.util.ArrayList;
import java.util.List;

import com.iksgmbh.sysnat.domain.NaturalLanguagePatternPart;
import com.iksgmbh.sysnat.domain.NaturalLanguagePatternPart.NaturalLanguagePatternPartType;
import com.iksgmbh.sysnat.exception.SysNatException.ErrorCode;
import com.iksgmbh.sysnat.utils.ExceptionHandlingUtil;
import com.iksgmbh.sysnat.utils.SysNatStringUtil;

/**
 * Used form LanguageTemplatePattern and LanguageInstructionPattern to
 * split an instruction line into its pattern in order to
 * make templates and instructions easier to compare.
 * 
 * @author Reik Oberrath
 */
public class LanguagePatternParser 
{
	public static final String PARAM_VARIABLE_IDENTIFIER = "'";
	public static final String PARAM_VALUE_IDENTIFIER_1 = "^";     // used in templates
	public static final String PARAM_VALUE_IDENTIFIER_2 = "\"";    // used in instructions
	public static final String RETURN_VALUE_START_IDENTIFIER = "<";
	public static final String RETURN_VALUE_END_IDENTIFIER = ">";
	
	private List<NaturalLanguagePatternPart> patternParts = new ArrayList<>();
	private Class<?>[] parameterTypes;
	private Class<?> returnType ;
		
	public LanguagePatternParser(final Class<?>[] parameterTypes, 
			                     final Class<?> returnType) 
	{
		this.parameterTypes = parameterTypes;
		this.returnType = returnType;
	}

	public static List<NaturalLanguagePatternPart> doYourJob(final String naturalLanguageLine, 
			                                                 final Class<?>[] parameterTypes,
			                                                 final Class<?> returnType) 
	{
		LanguagePatternParser languagePatternParser = new LanguagePatternParser(parameterTypes, returnType);
		languagePatternParser.parse(naturalLanguageLine);
		return languagePatternParser.getPatternParts();
	}

	private List<NaturalLanguagePatternPart> getPatternParts() {
		return patternParts;
	}

	private void parse(final String naturalLanguageLine) 
	{
		final char[] charArray = naturalLanguageLine.toCharArray();
		
		String currentPart = "";
		int parameterCount = 0;
		NaturalLanguagePatternPartType parseModus = NaturalLanguagePatternPartType.DEFAULT;
		for (char c : charArray) 
		{
			if (c == PARAM_VALUE_IDENTIFIER_1.charAt(0) || c == PARAM_VALUE_IDENTIFIER_2.charAt(0)) 
			{
				if (parseModus == NaturalLanguagePatternPartType.DEFAULT) 
				{
					verifyClosingParamValueIdentifier(naturalLanguageLine);
					addDefaultPart(currentPart);
					currentPart = "";					
					parseModus = NaturalLanguagePatternPartType.PARAM_VALUE;
				}
				else if (parseModus == NaturalLanguagePatternPartType.PARAM_VALUE)
				{
					addParamValuePart(parameterCount, currentPart);
					currentPart = "";					
					parseModus = NaturalLanguagePatternPartType.DEFAULT;
					parameterCount++;
				} 
				else 
				{
					currentPart += c;
				}
			} 
			else if (c == PARAM_VARIABLE_IDENTIFIER.charAt(0) ) 
			{
				if (parseModus == NaturalLanguagePatternPartType.DEFAULT) 
				{
					verifyClosingParamVariableIdentifier(naturalLanguageLine);
					addDefaultPart(currentPart);
					currentPart = "";					
					parseModus = NaturalLanguagePatternPartType.PARAM_VARIABLE;
				}
				else if (parseModus == NaturalLanguagePatternPartType.PARAM_VARIABLE)
				{
					addParamVariablePart(parameterCount, currentPart);
					currentPart = "";					
					parseModus = NaturalLanguagePatternPartType.DEFAULT;
					parameterCount++;
				}
				else 
				{
					currentPart += c;
				}
			}
			else if (c == RETURN_VALUE_START_IDENTIFIER.charAt(0) ) 
			{
				if (parseModus == NaturalLanguagePatternPartType.DEFAULT) 
				{
					verifyClosingReturnValueIdentifier(naturalLanguageLine);
					addDefaultPart(currentPart);
					currentPart = "";					
					parseModus = NaturalLanguagePatternPartType.RETURN_VALUE;
				} else {
					currentPart += c;
				}
			} 
			else if (c == RETURN_VALUE_END_IDENTIFIER.charAt(0)) 
			{
				if (parseModus == NaturalLanguagePatternPartType.RETURN_VALUE) 
				{					
					addReturnValuePart(returnType, currentPart);
					currentPart = "";					
					parseModus = NaturalLanguagePatternPartType.DEFAULT;
				} else {
					currentPart += c;
				}
			} 
			else 
			{
				currentPart += c;
			}
		}
		
		addDefaultPart(currentPart);
	}
	
	private void verifyClosingParamValueIdentifier(final String naturalLanguageLine) 
	{
		int countNumberOfOccurrences = SysNatStringUtil.countNumberOfOccurrences(naturalLanguageLine, PARAM_VALUE_IDENTIFIER_1);
		if (countNumberOfOccurrences%2 != 0) {
			ExceptionHandlingUtil.throwClassifiedException(ErrorCode.NATURAL_LANGUAGE_PARSING__MISSING_CLOSING_PART_IDENTIFIER, "" + PARAM_VALUE_IDENTIFIER_1);  
		}
		
		countNumberOfOccurrences = SysNatStringUtil.countNumberOfOccurrences(naturalLanguageLine, PARAM_VALUE_IDENTIFIER_2);
		if (countNumberOfOccurrences%2 != 0) {
			ExceptionHandlingUtil.throwClassifiedException(ErrorCode.NATURAL_LANGUAGE_PARSING__MISSING_CLOSING_PART_IDENTIFIER, "" + PARAM_VALUE_IDENTIFIER_2);  
		}
	}
	
	private void verifyClosingParamVariableIdentifier(final String naturalLanguageLine) 
	{
		
		int countNumberOfOccurrences = SysNatStringUtil.countNumberOfOccurrences(naturalLanguageLine, PARAM_VARIABLE_IDENTIFIER);
		if (countNumberOfOccurrences%2 != 0) {
			ExceptionHandlingUtil.throwClassifiedException(ErrorCode.NATURAL_LANGUAGE_PARSING__MISSING_CLOSING_PART_IDENTIFIER, "" + PARAM_VARIABLE_IDENTIFIER);  
		}
	}
	
	private void verifyClosingReturnValueIdentifier(final String naturalLanguageLine) 
	{
		int countNumberOfOccurrences1 = SysNatStringUtil.countNumberOfOccurrences(naturalLanguageLine, RETURN_VALUE_START_IDENTIFIER);
		int countNumberOfOccurrences2 = SysNatStringUtil.countNumberOfOccurrences(naturalLanguageLine, RETURN_VALUE_START_IDENTIFIER);
		if (countNumberOfOccurrences1 > countNumberOfOccurrences2) {
			ExceptionHandlingUtil.throwClassifiedException(ErrorCode.NATURAL_LANGUAGE_PARSING__MISSING_CLOSING_PART_IDENTIFIER, "" + RETURN_VALUE_START_IDENTIFIER);  
		}
	}

	private void addDefaultPart(String text) 
	{
		if (text != null && ! text.isEmpty()) {
			patternParts.add(new NaturalLanguagePatternPart(NaturalLanguagePatternPartType.DEFAULT, text));
		}
	}

	private void addParamValuePart(int paramCounter, String valueInInstruction) 
	{
		if (parameterTypes != null) 
		{
			// in LanguageTemplatePattern case
			Class<?> parameterType = null;
			if (parameterTypes.length > paramCounter) {
				parameterType = parameterTypes[paramCounter];
			} else {
				ExceptionHandlingUtil.throwException("Number of Parameter mismatch!");
			}
			patternParts.add(new NaturalLanguagePatternPart(NaturalLanguagePatternPartType.PARAM_VALUE, parameterType));
		} else {
			// in LanguageInstructionPattern case
			patternParts.add(new NaturalLanguagePatternPart(NaturalLanguagePatternPartType.PARAM_VALUE, valueInInstruction));
		}
	}

	private void addParamVariablePart(int paramCounter, String valueInInstruction) 
	{
		
		if (parameterTypes != null) {
			// in LanguageTemplatePattern case
			Class<?> parameterType = null;
			if (parameterTypes.length > paramCounter) {
				parameterType = parameterTypes[paramCounter];
			} else {
				ExceptionHandlingUtil.throwException("Number of Parameter mismatch!");
			}
			patternParts.add(new NaturalLanguagePatternPart(NaturalLanguagePatternPartType.PARAM_VARIABLE, parameterType));
		} else {
			// in LanguageInstructionPattern case
			patternParts.add(new NaturalLanguagePatternPart(NaturalLanguagePatternPartType.PARAM_VARIABLE, valueInInstruction));
		}
	}
	
	
	private void addReturnValuePart(final Object returnValueTypeOfMethodWithTemplate, 
			                        final String valueInInstruction) 
	{
		if (returnValueTypeOfMethodWithTemplate != null) {
			// in LanguageTemplatePattern case
			patternParts.add(new NaturalLanguagePatternPart(NaturalLanguagePatternPartType.RETURN_VALUE, returnValueTypeOfMethodWithTemplate));
		} else {
			// in LanguageInstructionPattern case
			patternParts.add(new NaturalLanguagePatternPart(NaturalLanguagePatternPartType.RETURN_VALUE, valueInInstruction));
		}
	}	
}
