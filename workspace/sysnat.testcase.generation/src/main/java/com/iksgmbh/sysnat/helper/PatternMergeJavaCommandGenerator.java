package com.iksgmbh.sysnat.helper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.iksgmbh.sysnat.common.exception.SysNatException.ErrorCode;
import com.iksgmbh.sysnat.common.utils.ExceptionHandlingUtil;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.JavaCommand;
import com.iksgmbh.sysnat.domain.LanguageInstructionPattern;
import com.iksgmbh.sysnat.domain.LanguageTemplatePattern;

/**
 * Creates java command by merging matching LanguageInstructionPatterns and LanguageTemplatePatterns.
 * 
 * @author Reik Oberrath
 */
public class PatternMergeJavaCommandGenerator 
{
	private HashMap<Filename, List<LanguageTemplatePattern>> languageTemplateCollection;
	private HashMap<Filename, List<LanguageInstructionPattern>> languageInstructionCollection;
	private HashMap<String, Class<?>> availableParamVariables = new HashMap<>();

	private LanguageInstructionPattern instructionPatternToMatch;
	private Filename instructionFileInProcess;
	private List<JavaCommand> javaCommandsForCurrentInstructionFile;
	private HashMap<Filename, List<JavaCommand>> toReturn;
	private boolean matchFound = false;
	private String applicationUnderTest;

	
	public PatternMergeJavaCommandGenerator(final HashMap<Filename, List<LanguageInstructionPattern>> aLanguageInstructionCollection,
											final HashMap<Filename, List<LanguageTemplatePattern>> aLanguageTemplateCollection,
											final String applicationName) 
	{
		this.languageInstructionCollection = aLanguageInstructionCollection;
		this.languageTemplateCollection = aLanguageTemplateCollection;
		this.applicationUnderTest = applicationName;
	}

	public static HashMap<Filename, List<JavaCommand>> doYourJob(
			final HashMap<Filename, List<LanguageTemplatePattern>> languageTemplateCollection,
			final HashMap<Filename, List<LanguageInstructionPattern>> languageInstructionCollection,
			final String applicationUnderTest) 
	{
		return new PatternMergeJavaCommandGenerator(languageInstructionCollection, 
				                                    languageTemplateCollection, 
				                                    applicationUnderTest).matchAndMerge();
	}

	private HashMap<Filename, List<JavaCommand>> matchAndMerge() 
	{
		toReturn = new HashMap<>();
		
		languageInstructionCollection.forEach( this::processInstructionFile );
		
		return toReturn;
	}
	
	private void processInstructionFile(Filename filename, List<LanguageInstructionPattern> instructionPatternList) 
	{
		instructionFileInProcess = filename;
		javaCommandsForCurrentInstructionFile = new ArrayList<JavaCommand>();  // java commands are added to this list in processTemplatePattern
		availableParamVariables.clear();
		instructionPatternList.forEach(this::processInstructionPattern);
		toReturn.put(buildJavaFileName(instructionFileInProcess), javaCommandsForCurrentInstructionFile);	
	}
	
	private void processInstructionPattern(final LanguageInstructionPattern instructionPattern) 
	{
		matchFound = false;
		instructionPatternToMatch = instructionPattern;
		languageTemplateCollection.forEach( this::processTemplateList );
		
		if (! matchFound) {
			final String methodSuggestion = MethodAnnotationSuggestionBuilder.
					buildAnnotationSuggestion(instructionPattern.getInstructionLine());
			ExceptionHandlingUtil.throwClassifiedException(ErrorCode.MATCHING_INSTRUCTION_AND_LANGUAGE_TEMPLATES__UNKNOWN_INSTRUCTION, 
					                                       instructionPattern.getInstructionLine(), 
					                                       instructionPattern.getFileName(),
					                                       methodSuggestion);
		}
	}

	private void processTemplateList(final Filename filename, 
			                         final List<LanguageTemplatePattern> languageTemplateList) 
	{
		languageTemplateList.forEach( this::processTemplatePattern );
	}

	private void processTemplatePattern(final LanguageTemplatePattern languageTemplatePattern) 
	{
		if ( ! matchFound && isMatching(languageTemplatePattern, instructionPatternToMatch)) 
		{
			javaCommandsForCurrentInstructionFile.add( JavaCommandCreator.doYourJob(instructionPatternToMatch, languageTemplatePattern));
			matchFound = true;
		}
	}

	protected boolean isMatching(final LanguageTemplatePattern templatePattern,
			                     final LanguageInstructionPattern instructionPattern) 
	{
		//logInput(templatePattern, instructionPattern);
		
		if (templatePattern.getNumberOfParts() != instructionPattern.getNumberOfParts()) {			
			return false;
		}

		// check part types
		for (int i=0; i<templatePattern.getNumberOfParts(); i++) 
		{
			if (templatePattern.getPart(i).type != instructionPattern.getPart(i).type) {
				return false;
			}
		}
		
		// check default part values
		for (int i=0; i<templatePattern.getNumberOfParts(); i++) 
		{
			switch (templatePattern.getPart(i).type) {
				case DEFAULT:
					final String s1 = (String) templatePattern.getPart(i).value;
					final String s2 = (String) instructionPattern.getPart(i).value;
					if ( ! s1.equals(s2) ) {
						return false;
					}
					break;
			default:
				break;
			}

		}

		// check parameter part values
		for (int i=0; i<templatePattern.getNumberOfParts(); i++) 
		{
			switch (templatePattern.getPart(i).type) 
			{
				case PARAM_VALUE:
					final Class<?> parameterType = (Class<?>) templatePattern.getPart(i).value;
					final String parameterValue = (String) instructionPattern.getPart(i).value;
					if ( ! doTypeAndValueMatch(parameterType, parameterValue) ) {
						ExceptionHandlingUtil.throwClassifiedException(ErrorCode.NATURAL_LANGUAGE_INSTRUCTING_PARSING__PARAMETER_TYPE_MISMATCH, 
								templatePattern.getMethodInfo(), instructionPattern.getOrigin());
					}
					break;
					
				case PARAM_VARIABLE:
					final Class<?> parameterTypeInTemplate = (Class<?>) templatePattern.getPart(i).value;
					final String parameterValueInInstruction = (String) instructionPattern.getPart(i).value;
					if (! availableParamVariables.containsKey(parameterValueInInstruction)) {
						ExceptionHandlingUtil.throwClassifiedException(
								ErrorCode.JAVA_CODE_VERIFICATION__UNKNOWN_VARIABLE_NAME, 
								instructionFileInProcess.value, parameterValueInInstruction);
					}
					final Class<?> typeOfParamVariable = availableParamVariables.get(parameterValueInInstruction);
					if ( typeOfParamVariable !=  parameterTypeInTemplate) {
						ExceptionHandlingUtil.throwClassifiedException(
								ErrorCode.JAVA_CODE_VERIFICATION__WRONG_VARIABLE_TYPE, 
								instructionFileInProcess.value, 
								parameterValueInInstruction, 
								typeOfParamVariable.getName(),
								instructionPatternToMatch.getInstructionLine(),
								parameterTypeInTemplate.getName()
								);
					}
					break;
					
				case RETURN_VALUE:
					availableParamVariables.put(instructionPattern.getReturnValueName(), templatePattern.getReturnType());
					
				default:
					break;
			}

		}
		
		return true;
	}

	protected void logInput(final LanguageTemplatePattern templatePattern,
			                final LanguageInstructionPattern instructionPattern) 
	{
		String s = "Active";
		if (instructionPattern.getInstructionLine().startsWith(s))
			//&& templatePattern.getAnnotationValue().startsWith(s)) 
		{
			System.err.println(instructionPattern.getInstructionLine() + " ### " + templatePattern.getAnnotationValue());
		}
		if (instructionPattern.getInstructionLine().startsWith(s)
			&& templatePattern.getAnnotationValue().startsWith(s)) 
		{
			System.out.println("");
		}
	}

	private boolean doTypeAndValueMatch(Class<?> parameterType, String parameterValue) 
	{
		try {
			if (parameterType == int.class || parameterType == Integer.class) {
				Integer.valueOf(parameterValue);
			}		
			if (parameterType == long.class || parameterType == Long.class) {
				Long.valueOf(parameterValue);
			}		
			if (parameterType == BigDecimal.class) {
				new BigDecimal(parameterValue);
			}		
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}

	protected Filename buildJavaFileName(final Filename instructionFileIn) 
	{
		String filenameSuffix = "Script";
		if (instructionFileIn.value.endsWith(".nlxx")) {
			filenameSuffix = "";
		}
		String result = normalizePath(instructionFileIn); 
		result = cutDirectory(result);
		result = SysNatStringUtil.cutExtension(result);
		String simpleName = cutPackageDir(result);
		String packageDir = cutSimpleName(result);
		String toReturn = applicationUnderTest.toLowerCase() + "/" 
		                  + packageDir + "/" 
				          + simpleName + filenameSuffix;
		if (instructionFileIn.value.endsWith(".nlxx")) {
			toReturn += "Test.java";
		} else {
			toReturn += ".java";  // for scripts
		}
		
		toReturn = SysNatStringUtil.replaceGermanUmlauts(toReturn)
				                   .replaceAll("[\\W]&&[^.]", ""); // removes special chars
		return new Filename(toReturn);
	}

	private String cutSimpleName(String s) 
	{
		int pos = s.lastIndexOf("/");
		if (pos == -1) {
			return "";
		}
		return s.substring(0, pos).toLowerCase();
	}

	private String cutPackageDir(String s) 
	{
		int pos = s.lastIndexOf("/");
		if (pos == -1) {
			return s;
		}
		return s.substring(pos + 1);
	}

	private String cutDirectory(String s) 
	{
		int pos = s.indexOf(applicationUnderTest);
		if (pos == -1) {
			ExceptionHandlingUtil.throwException("Unexpected source of instruction file!");
		}
		
		int length = applicationUnderTest.length();
		
		return s.substring(pos + length + 1);
	}

	private String normalizePath(final Filename instructionFileIn) {
		return instructionFileIn.value.replace("\\\\", "/").replace("\\", "/");
	}

}
