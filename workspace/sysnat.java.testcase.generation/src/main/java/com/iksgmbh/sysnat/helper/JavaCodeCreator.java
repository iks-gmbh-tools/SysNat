package com.iksgmbh.sysnat.helper;

import com.iksgmbh.sysnat.domain.JavaCommand;
import com.iksgmbh.sysnat.domain.LanguageInstructionPattern;
import com.iksgmbh.sysnat.domain.LanguageTemplatePattern;
import com.iksgmbh.sysnat.domain.NaturalLanguagePatternPart;
import com.iksgmbh.sysnat.domain.NaturalLanguagePatternPart.NaturalLanguagePatternPartType;
import com.iksgmbh.sysnat.utils.SysNatStringUtil;

public class JavaCodeCreator 
{
	public static JavaCommand createJavaCommand(final LanguageInstructionPattern instructionPattern,
			                                    final LanguageTemplatePattern templatePattern) 
	{
		final StringBuffer sb = new StringBuffer();
		
		sb.append(getReturnValue(instructionPattern, templatePattern));
		sb.append(templatePattern.getContainerFieldName());
		sb.append(".");
		sb.append(templatePattern.getMethodName());
		sb.append("(");
		sb.append(getParameters(instructionPattern));
		sb.append(");");
		
		return new JavaCommand(sb.toString());

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

	private static String getParameters(LanguageInstructionPattern instructionPattern) 
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < instructionPattern.getNumberOfParts(); i++) 
		{
			final NaturalLanguagePatternPart part = instructionPattern.getPart(i);
			if (part.type == NaturalLanguagePatternPartType.PARAM_VALUE) {
				sb.append(getValueValue(part)).append(", ");
			}
			if (part.type == NaturalLanguagePatternPartType.PARAM_VARIABLE) {
				sb.append(getVariableValue(part)).append(", ");
			}
		}
		
		String toReturn = sb.toString();
		
		if (toReturn.isEmpty()) return "";
		return toReturn.substring(0, toReturn.length()-2);
	}

	private static String getVariableValue(NaturalLanguagePatternPart part) {
		return toFieldName(part.value.toString());
	}
	
	private static Object getValueValue(NaturalLanguagePatternPart part) {
		if (part.value instanceof String) {
			return "\"" + part.value.toString() + "\"";
		}
		return part.value.toString();
	}
}
