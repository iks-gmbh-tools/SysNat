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

import static com.iksgmbh.sysnat.common.utils.SysNatConstants.METHOD_CALL_IDENTIFIER_BDD_KEYWORD_USAGE;
import static com.iksgmbh.sysnat.common.utils.SysNatConstants.METHOD_CALL_IDENTIFIER_BEHAVIOUR_DECLARATION;
import static com.iksgmbh.sysnat.common.utils.SysNatConstants.METHOD_CALL_IDENTIFIER_FILTER_DEFINITION;
import static com.iksgmbh.sysnat.common.utils.SysNatConstants.METHOD_CALL_IDENTIFIER_SET_ACTIVE_STATE;
import static com.iksgmbh.sysnat.common.utils.SysNatConstants.METHOD_CALL_IDENTIFIER_START_XX;
import static com.iksgmbh.sysnat.common.utils.SysNatConstants.METHOD_CALL_IDENTIFIER_TEST_DATA;
import static com.iksgmbh.sysnat.common.utils.SysNatConstants.METHOD_CALL_IDENTIFIER_TEST_PARAMETER_DEFINITION;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;

import com.iksgmbh.sysnat.GenerationRuntimeInfo;
import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.common.helper.ErrorPageLauncher;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.JavaCommand;
import com.iksgmbh.sysnat.domain.JavaCommand.CommandType;
import com.iksgmbh.sysnat.testdataimport.TableDataParser;
import com.iksgmbh.sysnat.testdataimport.TestDataImporter;

/**
 * Splits the content of a nlxx file in multiple JUnit java files 
 * if the nlxx file defines a group of XX (i.e. Behaiviour or feature).
 * 
 * Details:
 * Checks all Executable Examples (XX each represented by a list of JavaCommand 
 * parsed from the nlxx file) to define an XX Group.
 * A XX group is defined either a single XX for which a Test-Parameter is given 
 * or by a single software behaviour (feature) for which more than one XX are present.
 * If any type of a XX group is detected, for each XX a separate JUnit test class in generated.
 * For parameterized XX they only differ in the test data used, 
 * for Behaviours/Features they also differ in the executed instructions.
 * All JUnit java files belonging to the same XX group will be written together 
 * into a separate package.
 * 
 * @author Reik Oberrath
 */
public class XXGroupBuilder 
{
	public static final String BEHAVIOUR_CONSTANT_DECLARATION = "private static final String BEHAVIOUR_ID";

	private static final ResourceBundle ERR_MSG_BUNDLE = ResourceBundle.getBundle("bundles/ErrorMessages", Locale.getDefault());

	private HashMap<Filename, List<JavaCommand>> javaCommandCollectionRaw;
	private HashMap<Filename, List<JavaCommand>> javaCommandCollectionTemp = new HashMap<>();
	private HashMap<Filename, List<JavaCommand>> javaCommandCollectionResult = new HashMap<>();
	private HashMap<Filename, String> testParameter = new HashMap<>();
	private HashMap<Filename, String> xxGroups = new HashMap<>();
	private TestDataImporter testDataImporter;
	private String nameOfCurrentFile;
	private int xxCounter = 0;
	private List<JavaCommand> groupInstructions;
	
	private XXGroupBuilder(final HashMap<Filename, List<JavaCommand>> aJavaCommandCollectionRaw) 
	{
		this.javaCommandCollectionRaw = aJavaCommandCollectionRaw;
		this.testDataImporter = new TestDataImporter(GenerationRuntimeInfo.getInstance().getTestdataDir());
	}
	
	public static HashMap<Filename, List<JavaCommand>> doYourJob(final HashMap<Filename, List<JavaCommand>> aJavaCommandCollectionRaw) {
		return new XXGroupBuilder(aJavaCommandCollectionRaw).buildXXGroupsIfNecessary();
	}
	
	private HashMap<Filename, List<JavaCommand>> buildXXGroupsIfNecessary() 
	{
		// split XX of Behaviour into separate XXs and put result into javaCommandCollectionTemp
		javaCommandCollectionRaw.keySet().stream().filter(this::isNoScript)
		                                          .filter(this::doesXXFileCorrectlyDefineAnyBehaviour)
                                                  .forEach(this::buildXXGroupForBehaviour);

		// put XX without group into javaCommandCollectionTemp
		javaCommandCollectionRaw.keySet().stream().filter(filename -> ! xxGroups.containsKey(filename))
                                                  .forEach(filename -> javaCommandCollectionTemp.put(filename, javaCommandCollectionRaw.get(filename)));

		// split XX with parameter definition into separate XXs and put result into javaCommandCollectionResult
		javaCommandCollectionTemp.keySet().stream().filter(this::isNoScript)
                                                   .filter(this::isTestCaseParameterized)
		                                           .forEach(this::buildXXGroupForParameterizedXX);

		// put XX without parameter definition into javaCommandCollectionResult
		javaCommandCollectionTemp.keySet().stream().filter(filename -> ! testParameter.containsKey(filename))
                                                   .forEach(filename -> javaCommandCollectionResult.put(filename, javaCommandCollectionTemp.get(filename)));

		return javaCommandCollectionResult;
	}

	private boolean isNoScript(Filename filename) {
		return ! filename.value.endsWith("Script.java");
	}
	
	private void buildXXGroupForBehaviour(final Filename filename)
	{
		nameOfCurrentFile = filename.value;;
		xxCounter = 0;
		final List<JavaCommand> commands = javaCommandCollectionRaw.get(filename);
		final HashMap<CommandType, List<JavaCommand>> sortedCommands = sortCommandsForType(commands, filename);
		final HashMap<String, List<JavaCommand>> separatedXXs = cutXXGroupIntoSeparateXX(sortedCommands, filename);
		separatedXXs.keySet().forEach(xxid -> javaCommandCollectionTemp.put(createNewFilename(filename, xxid), 
				                                                            separatedXXs.get(xxid))); 
	}
	
	private HashMap<CommandType, List<JavaCommand>> sortCommandsForType(final List<JavaCommand> commands, 
			                                                            final Filename filename) 
	{
		final List<JavaCommand> constantsDeclaration = new ArrayList<>();
		final List<JavaCommand> oneTimePreconditions = new ArrayList<>();
		final List<JavaCommand> preconditions = new ArrayList<>();
		final List<JavaCommand> standards = new ArrayList<>();
		final List<JavaCommand> cleanups = new ArrayList<>();
		final List<JavaCommand> oneTimeCleanups = new ArrayList<>();
		
		String behaviourId = null;
		int groupDeclarationCounter = 0;
		int xxDeclarationCounter = 0;
		groupInstructions = new ArrayList<>();

		for (JavaCommand javaCommand : commands) 
		{
			if (javaCommand.value.endsWith(METHOD_CALL_IDENTIFIER_BDD_KEYWORD_USAGE + "\"Feature\");")) {
				groupInstructions.add(javaCommand);
				continue;
			}

			if (javaCommand.value.contains(METHOD_CALL_IDENTIFIER_BEHAVIOUR_DECLARATION)) 
			{
				groupDeclarationCounter++;
				if (groupDeclarationCounter > 1) {
					throw new SysNatTestDataException("Behaviour declaration must occur only once in '"
				               + filename.value + "'.");
				}
				if (standards.size() > 1) {
					throw new SysNatTestDataException("Behaviour declaration must occur before XXID declaration in '"
				               + filename.value + "'.");
				}
				groupInstructions.add(replaceFilenamePlaceholderIfPresent(filename, javaCommand, ""));
				behaviourId = extractXXGroup(javaCommand.value);
				continue;
			}

			if (javaCommand.value.contains(METHOD_CALL_IDENTIFIER_START_XX)) {
				xxDeclarationCounter++;
			}
			
			if (javaCommand.value.contains(METHOD_CALL_IDENTIFIER_FILTER_DEFINITION)) 
			{
				if (xxDeclarationCounter == 0 ) {
					groupInstructions.add(javaCommand);
					continue;				
				}
			}
			
			switch ( javaCommand.commandType ) 
			{
				case OneTimePrecondition:
					oneTimePreconditions.add(javaCommand);
					break;
	
				case Precondition:
					preconditions.add(javaCommand);
					break;
	
				case Standard:
					standards.add(javaCommand);
					break;
	
				case OneTimeCleanup:
					oneTimeCleanups.add(javaCommand);
					break;
	
				case Cleanup:
					cleanups.add(javaCommand);
					break;
				
				case Constant:
					 // ignore here
					 break;
				
				default:
				break;
			}
		}
		
		
		final JavaCommand xxGroupIdentifier = new JavaCommand(BEHAVIOUR_CONSTANT_DECLARATION + " = \"" 
		                                                      + behaviourId + "\";",
                                                              CommandType.Constant);
		constantsDeclaration.add(xxGroupIdentifier);

		final HashMap<CommandType, List<JavaCommand>> sortedCommands = new HashMap<>();

		sortedCommands.put(CommandType.Constant, constantsDeclaration);
		sortedCommands.put(CommandType.OneTimePrecondition, oneTimePreconditions);
		sortedCommands.put(CommandType.Precondition, preconditions);
		sortedCommands.put(CommandType.Standard, standards);
		sortedCommands.put(CommandType.Cleanup, cleanups);
		sortedCommands.put(CommandType.OneTimeCleanup, oneTimeCleanups);
		
		return sortedCommands;
	}

	private boolean containsFilenamePlaceholder(String value) {
		return value.equalsIgnoreCase(SysNatLocaleConstants.PLACEHOLDER_FILENAME) 
			   || value.equalsIgnoreCase(SysNatLocaleConstants.PLACEHOLDER_FILENAME_EN);
	}
	
	private Filename createNewFilename(Filename filename, String xxid) 
	{
		String xxGroup = xxGroups.get(filename);
		if (containsFilenamePlaceholder(xxGroup)) {
			xxGroup = SysNatStringUtil.extractXXIdFromFilename(filename.value);
		}
		
		int pos = filename.value.lastIndexOf('/');
		if (pos == -1) pos = filename.value.length();
		
		String name = filename.value.substring(0, pos) + "/" + xxGroup + "/" + xxid + "_Test.java";
		name = SysNatFileUtil.replaceInvalidFilenameChars(name);
		return new Filename(name);
	}

	private HashMap<String, List<JavaCommand>> cutXXGroupIntoSeparateXX(
			final HashMap<CommandType, List<JavaCommand>> sortedCommands,
			final Filename filename)
	{
		if (filename.value.contains("NeueWiedervorlageInEloErzeugenTest")) {
			//System.out.println("");
		}				

		final LinkedHashMap<String, List<JavaCommand>> separatedXXs = new LinkedHashMap<>();
		final List<JavaCommand> standardCommandsOfGroup = sortedCommands.get(CommandType.Standard);

		List<JavaCommand> standardCommandsOfseparatedXX = new ArrayList<>();
		JavaCommand hangoverCommand = null;
		
		String xxid = null;
		
		for (JavaCommand javaCommand : standardCommandsOfGroup) 
		{
			if (javaCommand.value.contains(METHOD_CALL_IDENTIFIER_BDD_KEYWORD_USAGE)) 
			{
				if (javaCommand.value.contains("Scenario") || javaCommand.value.contains("Szenario")) {
					hangoverCommand = javaCommand;
				} else {
					standardCommandsOfseparatedXX.add(javaCommand);
				}
				continue;
			}
			
			if (javaCommand.value.contains(METHOD_CALL_IDENTIFIER_START_XX)) 
			{
				if (xxid != null) 
				{
					final List<JavaCommand> completeCommandsOfseparatedXX = buildCompleteCommandList(sortedCommands, standardCommandsOfseparatedXX);
					if (separatedXXs.keySet().contains(xxid)) {
						ErrorPageLauncher.doYourJob("The ID <b>" + xxid + "<\b> for XX (or name of Feature) is not unique.", 
			                     "Please change ID of XX (or name of feature) .",
			                     ERR_MSG_BUNDLE.getString("GenerationError"));
					}
					
					separatedXXs.put(xxid, completeCommandsOfseparatedXX);
					standardCommandsOfseparatedXX = new ArrayList<>();
				}
				if (hangoverCommand != null) standardCommandsOfseparatedXX.add(hangoverCommand);
		
				if (containsFilenamePlaceholder(javaCommand.value)) {
					javaCommand = replaceFilenamePlaceholderIfPresent(filename, javaCommand, "__" + ++xxCounter);
				}
				xxid = extractXXID(javaCommand.value);
			}
			
			if (xxid != null) {
				standardCommandsOfseparatedXX.add(javaCommand);
			}
		}

		if (xxid != null) {
			final List<JavaCommand> completeCommandsOfseparatedXX = buildCompleteCommandList(sortedCommands, standardCommandsOfseparatedXX);
			separatedXXs.put(xxid, completeCommandsOfseparatedXX);
		}

		return separatedXXs;
	}

	private List<JavaCommand> buildCompleteCommandList(final HashMap<CommandType, List<JavaCommand>> sortedCommands,
			                                           final List<JavaCommand> standardCommandsOfseparatedXX) 
	{
		final List<JavaCommand> toReturn = new ArrayList<>();
		toReturn.addAll(sortedCommands.get(CommandType.Constant));
		toReturn.addAll(groupInstructions);
		if (containsFilterCommand(groupInstructions)) 
		{
			if (containsFilterCommand(standardCommandsOfseparatedXX)) {
				JavaCommand groupFilterCommand = removeFilterCommand(toReturn);
				joinFilterCommand(standardCommandsOfseparatedXX, groupFilterCommand);
			}
		}
		
		if ( ! sortedCommands.get(CommandType.OneTimePrecondition).isEmpty() ) {
			toReturn.add(new JavaCommand("languageTemplatesCommon.createComment(\"Start of OneTimePrecondition\");", CommandType.OneTimePrecondition));			
			toReturn.addAll(sortedCommands.get(CommandType.OneTimePrecondition));
			toReturn.add(new JavaCommand("languageTemplatesCommon.createComment(\"End of OneTimePrecondition\");", CommandType.OneTimePrecondition));			
			toReturn.add(new JavaCommand("languageTemplatesCommon.createComment(\"\");", CommandType.OneTimePrecondition));
		}
		
		injectPreconditions(standardCommandsOfseparatedXX, sortedCommands.get(CommandType.Precondition));
		toReturn.addAll(standardCommandsOfseparatedXX);
		toReturn.addAll(sortedCommands.get(CommandType.Cleanup));
		
		if ( ! sortedCommands.get(CommandType.OneTimeCleanup).isEmpty() ) {
			toReturn.add(new JavaCommand("languageTemplatesCommon.createComment(\"\");", CommandType.OneTimeCleanup));
			toReturn.add(new JavaCommand("languageTemplatesCommon.createComment(\"Start of OneTimeCleanup\");", CommandType.OneTimeCleanup));
			toReturn.addAll(sortedCommands.get(CommandType.OneTimeCleanup));
			toReturn.add(new JavaCommand("languageTemplatesCommon.createComment(\"End of OneTimeCleanup\");", CommandType.OneTimeCleanup));			
		}
		
		return toReturn;
	}

	private void injectPreconditions(List<JavaCommand> standardCommandsOfseparatedXX, 
			                         List<JavaCommand> preconditions)
	{
		String firstBddKeywordCommand = getFirstBddKeywordCommand(standardCommandsOfseparatedXX); 
		int pos1 = getIndex(standardCommandsOfseparatedXX, METHOD_CALL_IDENTIFIER_START_XX);
		int pos2 = getIndex(standardCommandsOfseparatedXX, METHOD_CALL_IDENTIFIER_TEST_PARAMETER_DEFINITION);
		int pos3 = getIndex(standardCommandsOfseparatedXX, METHOD_CALL_IDENTIFIER_FILTER_DEFINITION);
		int pos4 = getIndex(standardCommandsOfseparatedXX, METHOD_CALL_IDENTIFIER_TEST_DATA);
		int pos5 = getIndex(standardCommandsOfseparatedXX, METHOD_CALL_IDENTIFIER_SET_ACTIVE_STATE);
		int pos = pos1;
		if (pos2 > pos) pos = pos2;
		if (pos3 > pos) pos = pos3;
		if (pos4 > pos) pos = pos4;
		if (pos5 > pos) pos = pos5;
		
		for (int i = preconditions.size()-1; i >= 0; i--) {
			standardCommandsOfseparatedXX.add(pos, preconditions.get(i));
			if (firstBddKeywordCommand != null) 
			{
				String commandString = buildBddGivenCommand(firstBddKeywordCommand);
				JavaCommand command = new JavaCommand(commandString);
				standardCommandsOfseparatedXX.add(pos, command);
			}
		}
	}

	private String buildBddGivenCommand(String bddKeywordCommand)
	{
		int pos1 = bddKeywordCommand.indexOf('"') + 1;
		int pos2 = bddKeywordCommand.lastIndexOf('"');
		return bddKeywordCommand.substring(0, pos1) + "Given" + bddKeywordCommand.substring(pos2);
	}

	private String getFirstBddKeywordCommand(List<JavaCommand> standardCommandsOfseparatedXX)
	{
		Optional<JavaCommand> match = standardCommandsOfseparatedXX.stream()
				   .filter(command -> command.value.contains(METHOD_CALL_IDENTIFIER_BDD_KEYWORD_USAGE))
				   .findFirst();
		
		if (match.isPresent()) {
			return match.get().value;
		}
		return null;
	}

	private int getIndex(List<JavaCommand> commandList, String methodName)
	{
		int i = 0;
		for (JavaCommand command : commandList) {
			if (command.value.contains(methodName)) {
				return i;
			}
			i++;
		}
		return -1;
	}

	private JavaCommand replaceFilenamePlaceholderIfPresent(final Filename filename, 
			                                                final JavaCommand javaCommand,
			                                                final String replacementSuffix) 
	{
		JavaCommand groupDeclarationCommand;
		groupDeclarationCommand = javaCommand;
		if (containsFilenamePlaceholder(groupDeclarationCommand.value)) 
		{
			int pos1 = groupDeclarationCommand.value.indexOf('<');
			int pos2 = groupDeclarationCommand.value.indexOf('>');
			groupDeclarationCommand.value = groupDeclarationCommand.value.substring(0, pos1)
					                        + SysNatStringUtil.extractXXIdFromFilename(filename.value)
					                        + replacementSuffix
					                        + groupDeclarationCommand.value.substring(pos2+1);
		}
		return groupDeclarationCommand;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void buildXXGroupForParameterizedXX(final Filename filename)
	{
		final List<JavaCommand> commands = javaCommandCollectionTemp.get(filename);
		final String testParameterValue = testParameter.get(filename);
		final Hashtable<String, Properties> datasets;
		final boolean tableDataMode;
		
		String behaviourID = extractNlxxFileNameFromJavaFile(filename.value);
		JavaCommand constant = new JavaCommand("private static final String BEHAVIOUR_ID = \"" + behaviourID + "\";",
				                                CommandType.Constant);
		commands.add(0, constant);

		if ( testParameterValue.startsWith(TableDataParser.TABLE_DATA_IDENTIFIER)) 
		{
			List<Properties> testParameter = TableDataParser.doYourJob(filename.value, testParameterValue);
			datasets = new Hashtable<String, Properties>();
			for (int i = 0; i < testParameter.size(); i++) {
				datasets.put("TableDataParameter_" + (i+1), testParameter.get(i));
			}
			tableDataMode = true;
		} 
		else 
		{
			datasets = testDataImporter.loadTestdata(testParameterValue);
			tableDataMode = false;
		}
		
		final List<String> dataSetNames = new ArrayList(datasets.keySet());
		nameOfCurrentFile = filename.value;
		dataSetNames.forEach(dataSetName -> buildTestCase(testParameterValue, dataSetName, datasets.get(dataSetName), tableDataMode, commands)); 
	}
	
	private String extractNlxxFileNameFromJavaFile(String value) 
	{
		int pos1 = value.lastIndexOf("/") + 1;
		int pos2 = value.lastIndexOf("Test.");
		if (pos1 == 1 || pos2 == -1) {
			return value;
		}
		return value.substring(pos1, pos2);
	}

	private void buildTestCase(final String testParameterValue, 
			                   final String datasetName,
			                   final Properties dataset, 
			                   final boolean tableDataMode,                   
			                   final List<JavaCommand> commands) 
	{
		final List<JavaCommand> newCommands = new ArrayList<>();
		String datasetId = getDatasetUniqueIdentifier(datasetName);
		String xxid = "";

		for (JavaCommand javaCommand : commands) 
		{
			if (javaCommand.value.contains( METHOD_CALL_IDENTIFIER_START_XX )) {
				xxid = extractXXID(javaCommand.value);
				newCommands.add(new JavaCommand("languageTemplatesCommon" + SysNatConstants.METHOD_CALL_IDENTIFIER_START_XX 
						                        + "\"" + xxid + "__" + datasetId + "\");"));
			}
			else if (javaCommand.value.contains(METHOD_CALL_IDENTIFIER_TEST_PARAMETER_DEFINITION)) 
			{
				if (tableDataMode) {
					newCommands.add(new JavaCommand("languageTemplatesCommon.setTestData(\"" + toTableData(dataset) + "\");"));
				} else {
					newCommands.add(new JavaCommand("languageTemplatesCommon.setTestData(\"" + datasetName + "\");"));
				}
			} 
			else 
			{
				if (javaCommand.value.contains(":") && ! testParameterValue.contains("|")) {
					newCommands.add(new JavaCommand(javaCommand.value.replace(testParameterValue + ":", datasetName + ":")));
				} else {
					newCommands.add(javaCommand);
				}
			}
		}
		newCommands.add(new JavaCommand("languageTemplatesCommon" + METHOD_CALL_IDENTIFIER_BEHAVIOUR_DECLARATION + "\"" + xxid + "\");"));
		javaCommandCollectionResult.put(buildTargetFilename(nameOfCurrentFile, xxid, datasetId), newCommands);
	}

	private Filename buildTargetFilename(final String inputFilename, 
			                             final String xxid, 
			                             final String datasetId) 
	{
		int pos = inputFilename.lastIndexOf('/');
		String packagePath = "";
		String compressedXXid = xxid.replaceAll(" ", "");
		if (pos > -1) packagePath = inputFilename.substring(0, pos) + "/" + compressedXXid + "/";
		
		String id = "xlsx__";
		pos = datasetId.indexOf(id);
		String testId = datasetId;
		if (pos > -1) {
			testId = datasetId.substring(pos + id.length());
		}
		
		String filenameWithPath = packagePath + compressedXXid + "__"
				                        + testId + "_Test.java";

		filenameWithPath = SysNatFileUtil.replaceInvalidFilenameChars(filenameWithPath);
		return new Filename(filenameWithPath);
	}

	private String getDatasetUniqueIdentifier(final String datasetName) {
		String datasetId = SysNatStringUtil.extraxtTrailingDigits(datasetName);
		if (datasetId.isEmpty()) {
			datasetId = datasetName;
		}
		return datasetId;
	}

	private String toTableData(Properties dataset) 
	{
		String toReturn = TableDataParser.TABLE_DATA_IDENTIFIER +
				          TableDataParser.COLUMNS;
		Enumeration<Object> keys = dataset.keys();
		List<String> fieldNames = new ArrayList<>();
		List<String> values = new ArrayList<>();
		
		while (keys.hasMoreElements()) {
			String fieldname = (String) keys.nextElement();
			fieldNames.add(fieldname);
			values.add(dataset.getProperty(fieldname));
		}

		for (int i = 0; i < fieldNames.size(); i++) {
			toReturn += SysNatConstants.LINE_SEPARATOR 
			          + TableDataParser.CELL_SEPARATOR + fieldNames.get(i) 
	                  + TableDataParser.CELL_SEPARATOR + values.get(i)
			          + TableDataParser.CELL_SEPARATOR;
			          
		}
		
		return toReturn;
	}

	/**
	 * Analyses commands from an nlxx file
	 * @param filename
	 * @return true if Behaviour declaration exists and is correct
	 * @throws SysNatTestDataException if Behaviour declaration is wrong
	 */
	private boolean doesXXFileCorrectlyDefineAnyBehaviour(final Filename filename)
	{
		final List<JavaCommand> commands = javaCommandCollectionRaw.get(filename);	
		boolean xxGroupIdentifierPresent = false;
		int counterXXId = 0;
		
		for (JavaCommand javaCommand : commands) 
		{
			if (javaCommand.value.contains( METHOD_CALL_IDENTIFIER_START_XX )) {
				counterXXId++;
			}
			
			if (javaCommand.value.contains(METHOD_CALL_IDENTIFIER_BEHAVIOUR_DECLARATION))
			{
				xxGroupIdentifierPresent = true;
				if (xxGroups.containsKey(filename)) {
					throw new SysNatTestDataException("Only one Behaviour can be declared in '" +
				                                       filename.value + "'.");
				}
				xxGroups.put(filename, extractXXGroup(javaCommand.value));
			}
		}

		if (counterXXId == 0) {
			throw new SysNatTestDataException("Missing XXID declaration in '" +
                    filename.value + "'.");
		}
		
		if (counterXXId > 1 && ! xxGroupIdentifierPresent) 
		{
			String link = "https://github.com/iks-github/SysNat/wiki/What-is-a-nlxx-file%3F";
			ErrorPageLauncher.doYourJob("Without Behaviour declaration only one executable example " 
					                     + "is allowed in the same nlxx file.", 
					                     "To have more than one, add line 'Behaviour: &lt;unique name of behaviour&gt;' "
					                     + "at the top of the nlxx file. Read more about "
					                     + " <a href=\"" + link + "\">behaviours</a>.",
					                     ERR_MSG_BUNDLE.getString("GenerationError"));
			throw new SysNatTestDataException("Missing Behaviour declaration in '" +
                    filename.value + "'.");
		}
		
		if (xxGroups.containsKey(filename)) {
			return true;
		}
		
		return false;
	}

	private boolean isTestCaseParameterized(final Filename filename)
	{
		final List<JavaCommand> commands = javaCommandCollectionTemp.get(filename);
		System.out.println(filename.value);
		
		for (JavaCommand javaCommand : commands) 
		{
			if (javaCommand.value.contains( METHOD_CALL_IDENTIFIER_TEST_PARAMETER_DEFINITION )) 
			{
				testParameter.put(filename, extractTestParameter(javaCommand.value));
				return true;
			}
		}
		
		return false;
	}
	
	
	private String extractTestParameter(String javaCommand) 
	{
		final int pos = javaCommand.indexOf(METHOD_CALL_IDENTIFIER_TEST_PARAMETER_DEFINITION) + METHOD_CALL_IDENTIFIER_TEST_PARAMETER_DEFINITION.length() + 1;
		return javaCommand.substring(pos, javaCommand.length()-3).trim();
	}

	private String extractXXGroup(String javaCommand) 
	{
		final int pos = javaCommand.indexOf(METHOD_CALL_IDENTIFIER_BEHAVIOUR_DECLARATION) + METHOD_CALL_IDENTIFIER_BEHAVIOUR_DECLARATION.length() + 1;
		return javaCommand.substring(pos, javaCommand.length()-3).trim();
	}

	private String extractXXID(String javaCommand) 
	{
		final int pos = javaCommand.indexOf(METHOD_CALL_IDENTIFIER_START_XX) + METHOD_CALL_IDENTIFIER_START_XX.length() + 1;
		String toReturn = javaCommand.substring(pos, javaCommand.length()-3).trim();
		if (toReturn.equals(SysNatLocaleConstants.PLACEHOLDER_FILENAME) 
			|| toReturn.equals(SysNatLocaleConstants.PLACEHOLDER_FILENAME_EN)) {
			return SysNatStringUtil.extractXXIdFromFilename(nameOfCurrentFile);
		}
		return toReturn;
	}

	private Optional<JavaCommand> searchFilterCommand(List<JavaCommand> commands)
	{
		return commands.stream()
			           .filter(c -> c.value.contains(METHOD_CALL_IDENTIFIER_FILTER_DEFINITION))
			           .findFirst();
	}
	
	private boolean containsFilterCommand(List<JavaCommand> commands) {
		return searchFilterCommand(commands).isPresent();
	}
	
	private JavaCommand removeFilterCommand(List<JavaCommand> commands)
	{
		Optional<JavaCommand> findFirst = searchFilterCommand(commands);
		if (findFirst.isPresent()) {
			commands.remove(findFirst.get());
			return findFirst.get();
		}
		
		return null;
	}
	
	private void joinFilterCommand(List<JavaCommand> commands, JavaCommand otherFilterCommand)
	{
		Optional<JavaCommand> findFirst = searchFilterCommand(commands);
		if (findFirst.isPresent()) {
			JavaCommand filterCommand = findFirst.get();
			String s1 = SysNatStringUtil.extractTextBetween(otherFilterCommand.value, "\"", "\"");
			String s2 = SysNatStringUtil.extractTextBetween(filterCommand.value, "\"", "\"");
			filterCommand.value = filterCommand.value.replace(s2, s1 + "," + s2);  
		}
	}	
	
}