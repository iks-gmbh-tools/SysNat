package com.iksgmbh.sysnat.helper;

import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.domain.JavaCommand;
import com.iksgmbh.sysnat.domain.LanguageInstructionPattern;
import com.iksgmbh.sysnat.domain.LanguageTemplatePattern;
import com.iksgmbh.sysnat.domain.NaturalLanguagePatternPart;
import com.iksgmbh.sysnat.domain.NaturalLanguagePatternPart.NaturalLanguagePatternPartType;

/**
 * Helper to builds java commands by merging natural langiage instruction patterns 
 * and template patterns.
 *  
 * @author Reik Oberrath
 */
public class JavaCommandCreator 
{
	/**
	 * Merges a language instruction and a corresponding language template
	 * and transforms the result into a java method call.
	 * 
	 * @param instructionPattern a natural language instruction 
	 * @param templatePattern a natural language template
	 * @return a single line of java code that represents a method call
	 */
	public static JavaCommand doYourJob(final LanguageInstructionPattern instructionPattern,
			                            final LanguageTemplatePattern templatePattern) 
	{
		if (instructionPattern.getInstructionLine().startsWith("TestData")) {
			System.err.println("");
		}
		final StringBuffer sb = new StringBuffer();
		
		sb.append(getReturnValue(instructionPattern, templatePattern));
		sb.append(templatePattern.getContainerFieldName());
		sb.append(".");
		sb.append(templatePattern.getMethodName());
		sb.append("(");
		sb.append(getParameters(instructionPattern, templatePattern.getParameterTypes()));
		sb.append(");");
		
		return new JavaCommand(sb.toString(), templatePattern.getReturnType());

	}

	private static String getReturnValue(final LanguageInstructionPattern instructionPattern,
			                             final LanguageTemplatePattern templatePattern) 
	{
		final Class<?> returnType = templatePattern.getReturnType();
		if (void.class == returnType) {
			return "";
		}
		
		String variableName = toFieldName( instructionPattern.getReturnValueName());
		return returnType.getSimpleName() + " " + variableName + " = ";
	}

	private static String toFieldName(final String value) 
	{
		String variableName = SysNatStringUtil.replaceSpacesByUnderscore(value);
		return SysNatStringUtil.firstCharToLowerCase(variableName);
	}

	private static String getParameters(final LanguageInstructionPattern instructionPattern, 
			                            final Class<?>[] parameterTypes) 
	{
		int parameterCounter = 0;
		
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < instructionPattern.getNumberOfParts(); i++) 
		{
			final NaturalLanguagePatternPart part = instructionPattern.getPart(i);
			if (part.type == NaturalLanguagePatternPartType.PARAM_VALUE) {
				sb.append(getValueValue(part, parameterTypes[parameterCounter])).append(", ");
				parameterCounter++;
			}
			if (part.type == NaturalLanguagePatternPartType.PARAM_VARIABLE) {
				sb.append(getVariableValue(part)).append(", ");
				parameterCounter++;
			}
		}
		
		String toReturn = sb.toString();
		
		if (toReturn.isEmpty()) return "";
		return toReturn.substring(0, toReturn.length()-2);
	}

	private static String getVariableValue(NaturalLanguagePatternPart part) {
		return toFieldName(part.value.toString());
	}
	
	private static Object getValueValue(final NaturalLanguagePatternPart part, 
			                            final Class<?> parameterType) 
	{
		if (parameterType.getName().equals("java.lang.String")) {
			return "\"" + part.value.toString() + "\"";
		}
		return part.value.toString();
	}
}
