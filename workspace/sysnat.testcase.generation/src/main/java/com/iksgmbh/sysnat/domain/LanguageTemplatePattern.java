package com.iksgmbh.sysnat.domain;


import static com.iksgmbh.sysnat.helper.LanguagePatternParser.PARAM_VALUE_IDENTIFIER_1;
import static com.iksgmbh.sysnat.helper.LanguagePatternParser.PARAM_VALUE_IDENTIFIER_2;
import static com.iksgmbh.sysnat.helper.LanguagePatternParser.PARAM_VARIABLE_IDENTIFIER;
import static com.iksgmbh.sysnat.helper.LanguagePatternParser.RETURN_VALUE_END_IDENTIFIER;
import static com.iksgmbh.sysnat.helper.LanguagePatternParser.RETURN_VALUE_START_IDENTIFIER;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplates;
import com.iksgmbh.sysnat.common.exception.SysNatException.ErrorCode;
import com.iksgmbh.sysnat.common.utils.ExceptionHandlingUtil;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.helper.LanguagePatternParser;
import com.iksgmbh.sysnat.utils.StageInstructionUtil;

/**
 * Stores all information about an java method annotated by 'LanguageTemplate'.
 * This information is used for both 
 * - finding match with natural language instructions from nlxx-files and
 * - building the corresponding java code if a match is found.
 * 
 * @author Reik Oberrath
 */
public class LanguageTemplatePattern 
{
	/**
	 * Name of java file from which this template is read.
	 */
	private Filename containerFileName;
	private String containerFieldName;
	private String methodName;
	private String annotationValue;
	
	private List<NaturalLanguagePatternPart> patternParts;
	private Class<?>[] parameterTypes;
	private Class<?> returnType;

	public static List<LanguageTemplatePattern> createFrom(
			final Method method, 
            final Filename aContainerFileName,
            final String aContainerFieldName)
	{
		final List<LanguageTemplatePattern> toReturn = new ArrayList<>();
		final LanguageTemplates languageTemplates = method.getAnnotation(LanguageTemplates.class);
		final LanguageTemplate[] values = languageTemplates.value();

		for (LanguageTemplate languageTemplate : values) {
			toReturn.add( new LanguageTemplatePattern(method, aContainerFileName, aContainerFieldName, 
					                                  languageTemplate.value()));
		}
		
		return toReturn;
	}


	public LanguageTemplatePattern(final Method method, 
                                   final Filename aContainerFileName,
                                   final String aContainerFieldName) 
	{
		this(method, aContainerFileName, aContainerFieldName, 
			 method.getAnnotation(LanguageTemplate.class).value());
	}

	private LanguageTemplatePattern(final Method method, 
			                        final Filename aContainerFileName,
			                        final String aContainerFieldName,
			                        final String aAnnotationValue)
	{
//		if (aAnnotationValue.startsWith("Klick")) {
//			System.err.println(aAnnotationValue);
//		}

		this.containerFileName = aContainerFileName;
		this.containerFieldName = aContainerFieldName;
		this.methodName = method.getName();
		this.parameterTypes = method.getParameterTypes();
		this.annotationValue = aAnnotationValue;
		this.returnType = method.getReturnType();
		checkAnnotation(method, annotationValue, returnType);
		this.patternParts = LanguagePatternParser.doYourJob(annotationValue, parameterTypes, returnType);
	}
	

	
	private void checkAnnotation(final Method method, 
			                     final String annotationValue,
			                     final Class<?> returnType) 
	{
		// Check 1
		if (returnType == void.class && annotationValue.contains(RETURN_VALUE_START_IDENTIFIER + RETURN_VALUE_END_IDENTIFIER)) {
			ExceptionHandlingUtil.throwClassifiedException(
					ErrorCode.LANGUAGE_TEMPLATE_PARSING__MISSING_JAVA_RETURN_VALUE, getMethodName(method),
					annotationValue);
		}
		
		// Check 2
		if (returnType != void.class && SysNatStringUtil.countNumberOfOccurrences(annotationValue, RETURN_VALUE_START_IDENTIFIER) == 0) {
			ExceptionHandlingUtil.throwClassifiedException(
					ErrorCode.LANGUAGE_TEMPLATE_PARSING__MISSING_RETURN_VALUE_IN_PATTERN, getMethodName(method),
					annotationValue);
		}

		// Check 3
		int countNumberOfOccurrences = SysNatStringUtil.countNumberOfOccurrences(annotationValue, RETURN_VALUE_START_IDENTIFIER + RETURN_VALUE_END_IDENTIFIER);
		if (countNumberOfOccurrences > 1) {
			ExceptionHandlingUtil.throwClassifiedException(
					ErrorCode.LANGUAGE_TEMPLATE_PARSING__DOUBLE_RETURN_VALUE_IN_PATTERN, getMethodName(method),
					annotationValue);
		}

		// Check 4
		if ( ! StageInstructionUtil.isStageInstruction(annotationValue) ) 
		{
			countNumberOfOccurrences = SysNatStringUtil.countNumberOfOccurrences(annotationValue, PARAM_VALUE_IDENTIFIER_1 + PARAM_VALUE_IDENTIFIER_1);
			countNumberOfOccurrences += SysNatStringUtil.countNumberOfOccurrences(annotationValue,PARAM_VALUE_IDENTIFIER_2 + PARAM_VALUE_IDENTIFIER_2);
			countNumberOfOccurrences += SysNatStringUtil.countNumberOfOccurrences(annotationValue,PARAM_VARIABLE_IDENTIFIER + PARAM_VARIABLE_IDENTIFIER);
			if (countNumberOfOccurrences != method.getParameterCount()) {
				ExceptionHandlingUtil.throwClassifiedException(
						ErrorCode.LANGUAGE_TEMPLATE_PARSING__NUMBER_PARAMETER_MISMATCH, getMethodName(method),
						annotationValue);
			}
		}
	}
	
	private String getMethodName(final Method method) {
		return containerFileName.value + "." + method.getName();
	}


	public List<NaturalLanguagePatternPart> getParts() {
		return patternParts;
	}
	
	public int getNumberOfParts() {
		return patternParts.size();
	}

	public NaturalLanguagePatternPart getPart(int i) {
		return patternParts.get(i);
	}

	public String getMethodName() {
		return methodName;
	}
	
	public Filename getContainerFileName() {
		return containerFileName;
	}
	
	public String getContainerFieldName() {
		return containerFieldName;
	}

	public Class<?> getReturnType() {
		return returnType;
	}
	
	public String getMethodInfo() {
		String toReturn = containerFileName + "." + methodName + "(";
		for (Class<?> type : parameterTypes) {			
			toReturn += type.getSimpleName() + ",";
		}
		toReturn = toReturn.substring(0, toReturn.length()-1);
		return toReturn + ")";
	}
	
	@Override
	public String toString() {
		return annotationValue;
	}
	
	public boolean isIdentical(LanguageTemplatePattern otherPattern) 
	{
		boolean toReturn = false;
		
		if (getNumberOfParts() == otherPattern.getNumberOfParts()) 
		{
			toReturn = true;
			
			for (int i=0; i<getNumberOfParts(); i++) 
			{
				if (! getPart(i).isIdentical(otherPattern.getPart(i))) {
					return false;
				}
			}
		}
		
		return toReturn;
	}
	
	public String getAnnotationValue() {
		return annotationValue;
	}

	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}
	
}
