package com.iksgmbh.sysnat.exception;

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

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public String getErrorData() {
		return errorData;
	}

}
