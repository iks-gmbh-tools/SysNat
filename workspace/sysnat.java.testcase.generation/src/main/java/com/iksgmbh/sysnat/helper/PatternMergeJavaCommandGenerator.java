package com.iksgmbh.sysnat.helper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.JavaCommand;
import com.iksgmbh.sysnat.domain.LanguageInstructionPattern;
import com.iksgmbh.sysnat.domain.LanguageTemplatePattern;
import com.iksgmbh.sysnat.exception.SysNatException.ErrorCode;
import com.iksgmbh.sysnat.utils.ExceptionHandlingUtil;

/**
 * Creates java command by merging matching LanguageInstructionPatterns and LanguageTemplatePatterns.
 * 
 * @author Reik Oberrath
 */
public class PatternMergeJavaCommandGenerator 
{

	private HashMap<Filename, List<LanguageTemplatePattern>> languageTemplateCollection;
	private HashMap<Filename, List<LanguageInstructionPattern>> languageInstructionCollection;

	private LanguageInstructionPattern instructionPatternToMatch;
	private Filename instructionFileInProcess;
	private List<JavaCommand> javaCommandsForCurrentInstructionFile;
	private HashMap<Filename, List<JavaCommand>> toReturn;
	boolean matchFound = false;

	
	public PatternMergeJavaCommandGenerator(final HashMap<Filename, List<LanguageInstructionPattern>> aLanguageInstructionCollection,
											final HashMap<Filename, List<LanguageTemplatePattern>> aLanguageTemplateCollection) 
	{
		this.languageInstructionCollection = aLanguageInstructionCollection;
		this.languageTemplateCollection = aLanguageTemplateCollection;
	}

	public static HashMap<Filename, List<JavaCommand>> doYourJob(
			      final HashMap<Filename, List<LanguageTemplatePattern>> languageTemplateCollection,
			      final HashMap<Filename, List<LanguageInstructionPattern>> languageInstructionCollection) 
	{
		return new PatternMergeJavaCommandGenerator(languageInstructionCollection, languageTemplateCollection).matchAndMerge();
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
		javaCommandsForCurrentInstructionFile = new ArrayList<JavaCommand>();
		instructionPatternList.forEach(this::processInstructionPattern);
		toReturn.put(buildJavaFileName(instructionFileInProcess), javaCommandsForCurrentInstructionFile);
		
	}
	
	private void processInstructionPattern(final LanguageInstructionPattern instructionPattern) 
	{
		matchFound = false;
		instructionPatternToMatch = instructionPattern;
		languageTemplateCollection.forEach( this::processTemplateList );
	}

	private void processTemplateList(final Filename filename, 
			                         final List<LanguageTemplatePattern> languageTemplateList) 
	{
		languageTemplateList.forEach( this::processTemplatePattern );
	}

	private void processTemplatePattern(final LanguageTemplatePattern languageTemplatePattern) 
	{
		if ( ! matchFound && isMatching(languageTemplatePattern, instructionPatternToMatch)) {
			javaCommandsForCurrentInstructionFile.add( JavaCodeCreator.createJavaCommand(instructionPatternToMatch, languageTemplatePattern));
			matchFound = true;
		}
	}

	protected boolean isMatching(final LanguageTemplatePattern templatePattern,
			                     final LanguageInstructionPattern instructionPattern) 
	{
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
			switch (templatePattern.getPart(i).type) {
				case PARAM_VALUE:
				case PARAM_VARIABLE:
					final Class<?> parameterType = (Class<?>) templatePattern.getPart(i).value;
					final String parameterValue = (String) instructionPattern.getPart(i).value;
					if ( ! doTypeAndValueMatch(parameterType, parameterValue) ) {
						ExceptionHandlingUtil.throwClassifiedException(ErrorCode.NATURAL_LANGUAGE_INSTRUCTING_PARSING__PARAMETER_TYPE_MISMATCH, 
								                                       templatePattern.getMethodInfo(), instructionPattern.getOrigin());
					}
					break;

			default:
				break;
			}

		}
		return true;
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
		String name = instructionFileIn.value.replace("\\\\", "/").replace("\\", "/");  // normalize path
		int pos = name.indexOf(LanguageInstructionCollector.getTestCaseDir());
		if (pos == -1) {
			ExceptionHandlingUtil.throwException("Unexpected source of instruction file!");
		}
		name = name.substring(pos + LanguageInstructionCollector.getTestCaseDir().length() + 1);
		pos = name.lastIndexOf(".");
		name = name.substring(0, pos);
		pos = name.lastIndexOf("/");
		String simpleName = name.substring(pos + 1);
		name = name.substring(0, pos).toLowerCase() + "/" + simpleName + ".java";
		return new Filename(name);
	}

}
