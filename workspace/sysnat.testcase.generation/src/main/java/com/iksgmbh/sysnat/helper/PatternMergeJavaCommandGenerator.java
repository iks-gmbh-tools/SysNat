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
package com.iksgmbh.sysnat.helper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.exception.SysNatException.ErrorCode;
import com.iksgmbh.sysnat.common.utils.ExceptionHandlingUtil;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants;
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
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/UserMessages", Locale.getDefault());
	private static final String TEST_DATA_COMMAND_TEMPLATE = "setDatasetObject(\"-\");";
	private static final String TEST_PARAMETER_COMMAND_TEMPLATE = "applyTestParameter(\"-\");";
	
	private HashMap<Filename, List<LanguageTemplatePattern>> languageTemplateCollection;
	private HashMap<Filename, List<LanguageInstructionPattern>> languageInstructionCollection;
	private HashMap<String, Class<?>> availableParamVariables = new HashMap<>();

	private LanguageInstructionPattern instructionPatternToMatch;
	private Filename instructionFileInProcess;
	private List<JavaCommand> javaCommandsForCurrentInstructionFile;
	private HashMap<Filename, List<JavaCommand>> toReturn;
	private boolean matchFound = false;
	private String applicationUnderTest;
	private String testDataMultiLineString = null;
	private boolean tableDataMode;
	
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
		
		if ( ! matchFound ) 
		{
			if ( testDataMultiLineString != null ) 
			{
				if (! testDataMultiLineString.isEmpty()) {
					testDataMultiLineString += SysNatConstants.LINE_SEPARATOR;
				} 
				testDataMultiLineString += instructionPatternToMatch.getPart(0).value.toString();
				matchFound = true;
			}
		} 
		else 
		{
			if ( ! tableDataMode && testDataMultiLineString != null) 
			{
				List<JavaCommand> commands = javaCommandsForCurrentInstructionFile.stream()
						                        .filter(command -> command.value.contains(TEST_DATA_COMMAND_TEMPLATE) 
						                        		        || command.value.contains(TEST_PARAMETER_COMMAND_TEMPLATE))
						                        .collect(Collectors.toList());
				if (commands.size() == 1)
				{
					JavaCommand testDataCommand = commands.get(0);
					testDataCommand.value = testDataCommand.value.toString().replace("-", testDataMultiLineString);
					testDataMultiLineString = null;
					tableDataMode = false;
				} else {
					throw new SysNatException("Error parsing TableData: Unexpected number of java command matches.");
				}
				
			}
		}
		
		if (! matchFound) {
			handleNoMatchFoundProblem(instructionPattern);
		}
	}

	private void handleNoMatchFoundProblem(final LanguageInstructionPattern instructionPattern) 
	{
		final String methodSuggestion = MethodAnnotationSuggestionBuilder.
				buildAnnotationSuggestion(instructionPattern.getInstructionLine());
		
		String userErrorMessage = 
		        System.getProperty("line.separator") + "<br>"
		        + BUNDLE.getString("MATCHING_INSTRUCTION_AND_LANGUAGE_TEMPLATES__UNKNOWN_INSTRUCTION__ERROR_MESSAGE");
		userErrorMessage = userErrorMessage.replace("x1", 
				                            instructionPattern.getInstructionLine());
		
		String file = instructionPattern.getFileName();
		int pos = file.lastIndexOf('\\') + 1;
		String filename = file.substring(pos);
		userErrorMessage = userErrorMessage.replace("x2", filename);
		userErrorMessage = userErrorMessage.replace("x3", ExecutionRuntimeInfo.getInstance().getTestApplicationName());

		String testApp = System.getProperty(SysNatLocaleConstants.TESTAPP_SETTING_KEY);
		String libraryFilename = System.getProperty("sysnat.help.command.list.file")
				                 .replace("<testapp>", testApp);
		String languageTemplatesList = BUNDLE.getString("LanguageTemplatesList");
		String link = "<a href=\"../" + libraryFilename + "\">" + languageTemplatesList + "</a>";
		
		String similarInstructions = getFindSimilarInstructions(instructionPattern.getInstructionLine());
		String userHelpMessage = "";
		
		if (! similarInstructions.equals("<br>")) {
			userHelpMessage += System.getProperty("line.separator") + "<br>"
					+ BUNDLE.getString("MATCHING_INSTRUCTION_AND_LANGUAGE_TEMPLATES__UNKNOWN_INSTRUCTION__HELP_MESSAGE_1")
			        + System.getProperty("line.separator") + "<br>"
					+ getFindSimilarInstructions(instructionPattern.getInstructionLine())
					+ System.getProperty("line.separator") + "<br>";
					
		}
		userHelpMessage += System.getProperty("line.separator") + "<br>"
				+ BUNDLE.getString("MATCHING_INSTRUCTION_AND_LANGUAGE_TEMPLATES__UNKNOWN_INSTRUCTION__HELP_MESSAGE_2")
				+ link + "."
				+ System.getProperty("line.separator") + "<br>"
				+ System.getProperty("line.separator") + "<br>"
				+ BUNDLE.getString("MATCHING_INSTRUCTION_AND_LANGUAGE_TEMPLATES__UNKNOWN_INSTRUCTION__HELP_MESSAGE_3")
		        + System.getProperty("line.separator") + "<br>";
		
		ExceptionHandlingUtil.throwClassifiedException(ErrorCode.MATCHING_INSTRUCTION_AND_LANGUAGE_TEMPLATES__UNKNOWN_INSTRUCTION, 
				                                       instructionPattern.getInstructionLine(), 
				                                       instructionPattern.getFileName(),
				                                       methodSuggestion,
				                                       userErrorMessage,
				                                       userHelpMessage);
	}

	private String getFindSimilarInstructions(String instructionLine) 
	{
		final String languageTemplateToCompare = MethodAnnotationSuggestionBuilder.buildAnnotationValue(instructionLine);
		List<String> similarLanguageTemplates = new ArrayList<>();
		languageTemplateCollection.entrySet().forEach(filename -> findSimilarInstructions(languageTemplateToCompare, similarLanguageTemplates, filename.getValue()));
		
		StringBuffer sb = new StringBuffer();
		sb.append(System.getProperty("line.separator") + "<br>");
		similarLanguageTemplates.forEach(template -> sb.append(template).append(System.getProperty("line.separator") + "<br>"));
		
		return sb.toString().trim();
	}

	private void findSimilarInstructions(String languageTemplateToCompare,
			                             List<String> similarLanguageTemplates, 
			                             List<LanguageTemplatePattern> knownLanguageTemplates) {
		knownLanguageTemplates.forEach(languageTemplate -> findSimilarInstruction(languageTemplate, languageTemplateToCompare, similarLanguageTemplates));
	}

	private void findSimilarInstruction(LanguageTemplatePattern languageTemplate, 
			                            String languageTemplateToCompare,
			                            List<String> similarLanguageTemplates) 
	{
		if (SysNatStringUtil.calcSimilatity(languageTemplateToCompare, languageTemplate.getAnnotationValue()) > 0.8d) {
			similarLanguageTemplates.add(languageTemplate.getAnnotationValue());
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
			tableDataMode = isTableDataMode();
			
			if (tableDataMode) {
				testDataMultiLineString = "";
			}
		}	
	}

	private boolean isTableDataMode() 
	{
		return (instructionPatternToMatch.getPart(0).value.toString().equals(SysNatConstants.TEST_DATA + ":")
				|| instructionPatternToMatch.getPart(0).value.toString().equals(SysNatConstants.TEST_PARAMETER + ":")
                ) && instructionPatternToMatch.getPart(1).value.toString().equals("-");
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
					
					if (s1.trim().equals("=")) 
					{
						if ( ! s2.trim().equals("=") ) {
							return false;
						}
					} else if ( ! s1.equals(s2) ) {
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
		String s = "Szenario";
		if (instructionPattern.getInstructionLine().contains(s)
			&& templatePattern.getAnnotationValue().contains(s))
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
		
		toReturn = SysNatStringUtil.toFileName(toReturn);
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