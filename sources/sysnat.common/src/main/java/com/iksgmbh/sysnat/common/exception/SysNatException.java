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
package com.iksgmbh.sysnat.common.exception;

public class SysNatException extends RuntimeException 
{
	private static final long serialVersionUID = 1L;
	
	public enum ErrorCode { NATURAL_LANGUAGE_PARSING__MISSING_CLOSING_PART_IDENTIFIER,
							LANGUAGE_TEMPLATE_PARSING__MISSING_JAVA_RETURN_VALUE,
		                    LANGUAGE_TEMPLATE_PARSING__MISSING_RETURN_VALUE_IN_PATTERN, 
		                    LANGUAGE_TEMPLATE_PARSING__DOUBLE_RETURN_VALUE_IN_PATTERN, 
		                    LANGUAGE_TEMPLATE_PARSING__NUMBER_PARAMETER_MISMATCH,
		                    LANGUAGE_TEMPLATE_PARSING__DUPLICATES,
		                    NATURAL_LANGUAGE_INSTRUCTING_PARSING__DOUBLE_RETURN_VALUE_IDENTIFIER, 
		                    NATURAL_LANGUAGE_INSTRUCTING_PARSING__PARAMETER_TYPE_MISMATCH, 
		                    NATURAL_LANGUAGE_INSTRUCTING_PARSING__EMPTY_RETURN_VALUE_IDENTIFIER,
		                    NATURAL_LANGUAGE_INSTRUCTING_PARSING__EMPTY_PARAMETER_IDENTIFIER,
		                    MATCHING_INSTRUCTION_AND_LANGUAGE_TEMPLATES__UNKNOWN_INSTRUCTION, 
		                    JAVA_CODE_VERIFICATION__WRONG_VARIABLE_TYPE,
		                    };

	private ErrorCode errorCode;
	private String errorData;
	
	public SysNatException(String message) {
		super(message);
	}

	public SysNatException(ErrorCode aErrorCode) {
		super();
		this.errorCode = aErrorCode;
	}
	
	public SysNatException(ErrorCode aErrorCode, String errorData) {
		this(aErrorCode);
		this.errorData = errorData;
	}

	public SysNatException(String message, Exception e) {
		super(message, e);
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public String getErrorData() {
		return errorData;
	}

}