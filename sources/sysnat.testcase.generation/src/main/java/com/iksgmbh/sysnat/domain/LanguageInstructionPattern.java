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
package com.iksgmbh.sysnat.domain;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.exception.SysNatException.ErrorCode;
import com.iksgmbh.sysnat.common.utils.ExceptionHandlingUtil;
import com.iksgmbh.sysnat.domain.NaturalLanguagePatternPart.NaturalLanguagePatternPartType;
import com.iksgmbh.sysnat.helper.LanguagePatternParser;

/**
 * Stores all information about a single natural language instruction read from a nlxx-file.
 * Each line of code in a ntlc file is supposed to represent a natural language instruction.
 * This line is parsed in this class into parts so that it can be compared with a NaturalLanguagePattern
 * in order to find matches.
 * 
 * @author Reik Oberrath
 */
public class LanguageInstructionPattern 
{
	/**
	 * Name of nlxx-file from which this instruction is read.
	 */
	private String fileName;
	private String instructionLine;
	private String metaInfo;
	
	private List<NaturalLanguagePatternPart> patternParts;

	public LanguageInstructionPattern(final String aInstructionLine, 
                                      final String aFileName)
    {
		this(aInstructionLine, aFileName, "");
    }

	public LanguageInstructionPattern(final String aInstructionLine, 
			                          final String aFileName,
			                          final String aMetaInfo)
	{
		this.fileName = aFileName;
		this.instructionLine = aInstructionLine;
		this.metaInfo = aMetaInfo;
		
		try {
			this.patternParts = LanguagePatternParser.doYourJob(instructionLine, null, null);
		} catch (SysNatException e) 
		{
			if (e.getErrorCode() == ErrorCode.NATURAL_LANGUAGE_PARSING__MISSING_CLOSING_PART_IDENTIFIER) 
			{
				ExceptionHandlingUtil.throwClassifiedException(ErrorCode.NATURAL_LANGUAGE_PARSING__MISSING_CLOSING_PART_IDENTIFIER, 
						e.getErrorData(), fileName, instructionLine);
			}
		}
		
		validatePattern();
	}
	
	private void validatePattern() 
	{
		// check 1
		int numberOfReturnValues = patternParts.stream()
				                   .filter(pp -> pp.type == NaturalLanguagePatternPartType.RETURN_VALUE)
				                   .collect(Collectors.toList()).size();

		
		if (numberOfReturnValues > 1) {
			ExceptionHandlingUtil.throwClassifiedException(ErrorCode.NATURAL_LANGUAGE_INSTRUCTING_PARSING__DOUBLE_RETURN_VALUE_IDENTIFIER, 
					fileName, instructionLine);
		}

		// check 2
		patternParts.stream()
                    .filter(pp -> pp.type != NaturalLanguagePatternPartType.DEFAULT)
                    .forEach(this::checkForEmptyPartValue);

	}
	
	private void checkForEmptyPartValue(NaturalLanguagePatternPart part) 
	{
		if (part.value == null || part.value.toString().trim().length() == 0) 
		{
			if (part.type == NaturalLanguagePatternPartType.PARAM_VARIABLE || 
				part.type == NaturalLanguagePatternPartType.PARAM_VALUE	) {
//					ExceptionHandlingUtil.throwClassifiedException(
//							ErrorCode.NATURAL_LANGUAGE_INSTRUCTING_PARSING__EMPTY_PARAMETER_IDENTIFIER, 
//							getOrigin());
				
			}
			if (part.type == NaturalLanguagePatternPartType.RETURN_VALUE) {
				ExceptionHandlingUtil.throwClassifiedException(
						ErrorCode.NATURAL_LANGUAGE_INSTRUCTING_PARSING__EMPTY_RETURN_VALUE_IDENTIFIER, 
						getOrigin());
			
			}
		}
	}

	public String getFileName() {
		return fileName;
	}

	public String getMetaInfo() {
		return metaInfo;
	}

	public String getReturnValueName() 
	{
		Optional<String> returnValueName = patternParts.stream()
				                                       .filter(part -> part.type == NaturalLanguagePatternPartType.RETURN_VALUE)
				                                       .map(part -> part.value.toString())
				                                       .findFirst();
		if (returnValueName.isPresent()) {
			return returnValueName.get();
		}
		
		return null;
	}

	public int getNumberOfParts() {
		return patternParts.size();
	}

	public NaturalLanguagePatternPart getPart(int i) {
		return patternParts.get(i);
	}
	
	@Override
	public String toString() {
		return instructionLine;
	}
	
	public String getOrigin() {
		return fileName + " - " + instructionLine; 
	}

	public String getInstructionLine() {
		return instructionLine; 
	}

	public List<String> getParamVariableNames() 
	{
		return patternParts.stream().filter(part -> part.type == NaturalLanguagePatternPartType.PARAM_VARIABLE)
                .map(part -> part.value.toString())
                .collect(Collectors.toList());
	}

}