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
package com.iksgmbh.sysnat.common.utils;

import java.util.Locale;
import java.util.ResourceBundle;

import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.exception.SysNatException.ErrorCode;
import com.iksgmbh.sysnat.common.helper.ErrorPageLauncher;

public class ExceptionHandlingUtil 
{	
	private static final ResourceBundle ERR_MSG_BUNDLE = ResourceBundle.getBundle("bundles/ErrorMessages", Locale.getDefault());

	public static void throwException(final String errorMessage) {
		System.err.println("ERROR: " + errorMessage);
		throw new SysNatException(errorMessage);
	}
	
	public static void throwClassifiedException(final ErrorCode errorCode, String... errorData) 
	{
		StringBuffer errorMessage;
		String message;
		
		switch (errorCode) 
		{
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
			message = "A match between Method <b>" + errorData[0] + "</b> and "
					           + "instruction line <b>" + errorData[1] + "</b> is found, but the "
					           + "parameter type is mismatching!";
			System.err.println(message);
			throw new SysNatException(errorCode);

		case NATURAL_LANGUAGE_INSTRUCTING_PARSING__EMPTY_PARAMETER_IDENTIFIER:
			message = "The instruction line <b>" + errorData[0] + "</b> defines a parameter without a value!";
			System.err.println(message);
			ErrorPageLauncher.doYourJob(message, "Use \"\" as value to define an empty parameter value.", ERR_MSG_BUNDLE.getString("GenerationError"));
			throw new SysNatException(errorCode);

		case NATURAL_LANGUAGE_INSTRUCTING_PARSING__EMPTY_RETURN_VALUE_IDENTIFIER:
			message = "The instruction line <b>" + errorData[0] + "</b> contains an empty return value!";
			System.err.println(message);
			ErrorPageLauncher.doYourJob(message, "", ERR_MSG_BUNDLE.getString("GenerationError"));
			throw new SysNatException(errorCode);
			
		case NATURAL_LANGUAGE_PARSING__MISSING_CLOSING_PART_IDENTIFIER:
			if (errorData.length == 3) {
			errorMessage = new StringBuffer(System.getProperty("line.separator"));
			errorMessage.append("The natural language statement <b>\"" + errorData[2] + "\"</b><br> in file <b>\""
					+ errorData[1] + "\"</b><br> is missing the closing character <b>" + errorData[0] + "</b>!");
			System.err.println(errorMessage.toString());			
			ErrorPageLauncher.doYourJob(errorMessage.toString(), "", ERR_MSG_BUNDLE.getString("GenerationError"));
			throw new SysNatException(errorCode);
			} else {
				throw new SysNatException(errorCode, errorData[0]);
			}
		case MATCHING_INSTRUCTION_AND_LANGUAGE_TEMPLATES__UNKNOWN_INSTRUCTION:
			
			errorMessage = new StringBuffer(System.getProperty("line.separator"));
			
			errorMessage.append("The natural language statement")
			            .append(System.getProperty("line.separator"))
					    .append(errorData[0])
			            .append(System.getProperty("line.separator"))
			            .append("in file")
			            .append(System.getProperty("line.separator"))
			            .append(errorData[1])
			            .append(System.getProperty("line.separator"))
			            .append("could not be mapped on any known LanguageTemplate!")
                        .append(System.getProperty("line.separator"))
                        .append(System.getProperty("line.separator"))
			            .append("You may wish to add a new method with a matching LanguageTemplate which could look like this:")
                        .append(System.getProperty("line.separator"))
                        .append(System.getProperty("line.separator"))
                        .append(errorData[2]);
			
			System.err.println(errorMessage.toString());			
			ErrorPageLauncher.doYourJob(errorData[3], errorData[4], ERR_MSG_BUNDLE.getString("GenerationError"));
			throw new SysNatException(errorCode);

		case JAVA_CODE_VERIFICATION__WRONG_VARIABLE_TYPE:
			errorMessage = new StringBuffer(System.getProperty("line.separator"));
			errorMessage.append("The executable example file <b>" + errorData[0] + "</b>")
								.append(" defines the variable <b>" + errorData[1] + "</b> with type <b>" + errorData[2] + "</b>")
								.append(" which is used as parameter in instruction <b>" + errorData[3] + "</b>")
								.append(" with a differing type <b>" + errorData[4] + "</b>!");
			System.err.println(errorMessage.toString());			
			ErrorPageLauncher.doYourJob(errorMessage.toString(), "", ERR_MSG_BUNDLE.getString("GenerationError"));
			throw new SysNatException(errorCode);
			
		default:
			throwException("Unknown errorCode " + errorCode);
			break;
		} 
	}

}