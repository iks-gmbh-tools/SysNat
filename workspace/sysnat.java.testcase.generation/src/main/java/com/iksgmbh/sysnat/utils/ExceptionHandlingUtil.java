package com.iksgmbh.sysnat.utils;

import com.iksgmbh.sysnat.exception.SysNatException;
import com.iksgmbh.sysnat.exception.SysNatException.ErrorCode;

public class ExceptionHandlingUtil 
{	
	public static void throwException(final String errorMessage) {
		System.err.println(errorMessage);
		throw new SysNatException(errorMessage);
	}
	
	public static void throwClassifiedException(final ErrorCode errorCode, String... errorData) 
	{
		switch (errorCode) {
		case LANGUAGE_TEMPLATE_PARSING__MISSING_JAVA_RETURN_VALUE:
			System.err.println("LanguageTemplate \"" + errorData[1] + "\" of method \""
			+ errorData[0] + "\" indicates a return value of the java method which is missing!");
			throw new SysNatException(errorCode);

		case LANGUAGE_TEMPLATE_PARSING__MISSING_RETURN_VALUE_IN_PATTERN:
			System.err.println("LanguageTemplate \"" + errorData[1] + "\" of method \""
			+ errorData[0] + "\" misses to indicate a return value!");
			throw new SysNatException(errorCode);
			
		case LANGUAGE_TEMPLATE_PARSING__DOUBLE_RETURN_VALUE_IN_PATTERN:
			System.err.println("LanguageTemplate \"" + errorData[1] + "\" of method \""
			+ errorData[0] + "\" indicate more than one return values!");
			throw new SysNatException(errorCode);
			
		case LANGUAGE_TEMPLATE_PARSING__NUMBER_PARAMETER_MISMATCH:
			System.err.println("LanguageTemplate \"" + errorData[1] + "\" of method \""
			+ errorData[0] + "\" contains not the same number of parameter than the java method!");
			throw new SysNatException(errorCode);
			
		case NATURAL_LANGUAGE_INSTRUCTING_PARSING__DOUBLE_RETURN_VALUE_IDENTIFIER:
			System.err.println("Natural Language Instruction \"" + errorData[1] + "\" in file \""
			+ errorData[0] + "\" indicate two return values!");
			throw new SysNatException(errorCode);
			
		case LANGUAGE_TEMPLATE_PARSING__DUPLICATES:
			System.err.println("The LanguageTemplates of the following two java methods represent duplicates: \"" + errorData[1] + "\" and \""
			+ errorData[0] + "\"!");
			throw new SysNatException(errorCode);
			
		case NATURAL_LANGUAGE_INSTRUCTING_PARSING__PARAMETER_TYPE_MISMATCH:
			System.err.println("A match between Method (" + errorData[0] + ") and "
					           + "instruction line (" + errorData[1] + ") is found, but the "
					           + "parameter type is mismatching!");
			throw new SysNatException(errorCode);

		case NATURAL_LANGUAGE_INSTRUCTING_PARSING__EMPTY_PARAMETER_IDENTIFIER:
			System.err.println("The instruction line (" + errorData[0] + ") contains an empty parameter!");
			throw new SysNatException(errorCode);

		case NATURAL_LANGUAGE_INSTRUCTING_PARSING__EMPTY_RETURN_VALUE_IDENTIFIER:
			System.err.println("The instruction line (" + errorData[0] + ") contains an empty return value!");
			throw new SysNatException(errorCode);
			
		case NATURAL_LANGUAGE_PARSING__MISSING_CLOSING_PART_IDENTIFIER:
			if (errorData.length == 3) {
				System.err.println("The natural language statement \"" + errorData[2] + "\" in file \""
						+ errorData[1] + "\" is missing the closing character >" + errorData[0] + "<!");
			}
			throw new SysNatException(errorCode, errorData[0]);
			
		default:
			throwException("Unknown errorCode " + errorCode);
			break;
		} 
	}

}
