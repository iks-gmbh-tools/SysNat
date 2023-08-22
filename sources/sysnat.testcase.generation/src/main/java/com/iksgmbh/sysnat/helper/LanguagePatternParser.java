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

import java.util.ArrayList;
import java.util.List;

import com.iksgmbh.sysnat.common.exception.SysNatException.ErrorCode;
import com.iksgmbh.sysnat.common.utils.ExceptionHandlingUtil;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.domain.NaturalLanguagePatternPart;
import com.iksgmbh.sysnat.domain.NaturalLanguagePatternPart.NaturalLanguagePatternPartType;
import com.iksgmbh.sysnat.utils.StageInstructionUtil;

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
		final LanguagePatternParser languagePatternParser = new LanguagePatternParser(parameterTypes, returnType);
		if (StageInstructionUtil.isStageInstruction(naturalLanguageLine)) {
			languagePatternParser.parsePatternPartsFromStageInstruction(naturalLanguageLine);
		} else {	
			languagePatternParser.parse(naturalLanguageLine);
		}
		return languagePatternParser.getPatternParts();
	}

	private void parsePatternPartsFromStageInstruction(String naturalLanguageLine) 
	{
		int pos = naturalLanguageLine.indexOf(':');
		if (pos == -1 || naturalLanguageLine.trim().startsWith(":")) {
			throw new IllegalArgumentException("Line '" + naturalLanguageLine + "' is no valid stage instruction!");
		}
		
		String stageInstruction = naturalLanguageLine.substring(0, pos+1);
		String instructionContent = naturalLanguageLine.substring(pos+1);
		instructionContent = checkForTestDataPlaceholder(instructionContent);
		addDefaultPart(stageInstruction);

		if (naturalLanguageLine.endsWith(":")) {
			addParamValuePart(0, "-", naturalLanguageLine);
		} else {
			addParamValuePart(0, instructionContent.trim(), naturalLanguageLine);
		}		
	}

	private String checkForTestDataPlaceholder(String instructionContent)
	{
		String toReturn = instructionContent;
		String placeholderIdentifier = '"' + SysNatConstants.DC;
		int pos = toReturn.indexOf(placeholderIdentifier);
		String DC_Masking_Identifier = "<|>";
		
		while (pos > -1) {
			String s1 = toReturn.substring(0, pos+1);
			String s2 = toReturn.substring(pos+1);
			pos = s2.indexOf("\"");
			String placeholder = s2.substring(0, pos).replace(SysNatConstants.DC, DC_Masking_Identifier);
			toReturn = s1 + " + getTestDataValue(\"" + placeholder + "\") + \"" + s2.substring(pos+1);
			pos = toReturn.indexOf(placeholderIdentifier);
		}
		
		return toReturn.replace(DC_Masking_Identifier, SysNatConstants.DC);
	}

	private List<NaturalLanguagePatternPart> getPatternParts() {
		return patternParts;
	}

	private void parse(final String naturalLanguageLine) 
	{
		final char[] charArray = naturalLanguageLine.toCharArray();
		
		String currentPart = "" + "";
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
					addParamValuePart(parameterCount, currentPart, naturalLanguageLine);
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
					addParamVariablePart(parameterCount, currentPart, naturalLanguageLine);
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
			ExceptionHandlingUtil.throwClassifiedException(ErrorCode.NATURAL_LANGUAGE_PARSING__MISSING_CLOSING_PART_IDENTIFIER, 
					                                       "" + PARAM_VARIABLE_IDENTIFIER,
					                                       "?",  // TODO
					                                       naturalLanguageLine);  
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

	private void addParamValuePart(int paramCounter, 
			                       String valueInInstruction, 
			                       String naturalLanguageLine) 
	{
		if (parameterTypes != null) 
		{
			// in LanguageTemplatePattern case
			Class<?> parameterType = null;
			if (parameterTypes.length > paramCounter) {
				parameterType = parameterTypes[paramCounter];
			} else {
				ExceptionHandlingUtil.throwException("Number of parameter mismatch in method of language template '" 
			                                         + naturalLanguageLine+ "'.");
			}
			patternParts.add(new NaturalLanguagePatternPart(NaturalLanguagePatternPartType.PARAM_VALUE, parameterType));
		} else {
			// in LanguageInstructionPattern case
			patternParts.add(new NaturalLanguagePatternPart(NaturalLanguagePatternPartType.PARAM_VALUE, valueInInstruction));
		}
	}

	private void addParamVariablePart(int paramCounter, 
			                          String valueInInstruction,
			                          String naturalLanguageLine) 
	{
		if (parameterTypes != null) {
			// in LanguageTemplatePattern case
			Class<?> parameterType = null;
			if (parameterTypes.length > paramCounter) {
				parameterType = parameterTypes[paramCounter];
			} else {
				ExceptionHandlingUtil.throwException("Number of Parameter mismatch for instruction: " + naturalLanguageLine);
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