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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import com.iksgmbh.sysnat.GenerationRuntimeInfo;
import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.JavaCommand;
import com.iksgmbh.sysnat.testdataimport.TableDataParser;
import com.iksgmbh.sysnat.testdataimport.TestDataImporter;

/**
 * Splits the content of a nlxx file in multiple JUnit java files if the nlxx file defines a test series.
 * 
 * Details:
 * Checks all Executable Examples (XX) (each represented by a list of JavaCommand parsed from the nlxx file) to define a test series.
 * A test series is represented either by a parameterized XX or a by group of XXs defined for a Rule of software behaviour.
 * Rules have to be declared in the nlxx file by the stage instruction "Rule-ID".
 * If any type of test series is detected, for each test case within this series a separate JUnit test class in generated.
 * For parameterized tests they only differ in the test data used, for rule groups they also differ in the executed instructions.
 * All JUnit java files belonging to the same test series will be written together into a specific package created only for the test series.
 * 
 * @author Reik Oberrath
 */
public class TestSeriesBuilder 
{
	private static final String PARAMETER_IDENTIFIER_METHOD_CALL = ".applyTestParameter(";
	private static final String RULE_IDENTIFIER_METHOD_CALL = ".declareXXGroupForRule(";
	private static final String XXID_IDENTIFIER_METHOD_CALL = ".startNewXX(";
	
	private HashMap<Filename, List<JavaCommand>> javaCommandCollectionRaw;
	private HashMap<Filename, List<JavaCommand>> javaCommandCollectionTemp = new HashMap<>();
	private HashMap<Filename, List<JavaCommand>> javaCommandCollectionResult = new HashMap<>();
	private HashMap<Filename, String> testParameter = new HashMap<>();
	private HashMap<Filename, String> xxRuleGroups = new HashMap<>();
	private TestDataImporter testDataImporter;
	private String nameOfCurrentFile;
	private int xxCounter = 0;
	
	private TestSeriesBuilder(final HashMap<Filename, List<JavaCommand>> aJavaCommandCollectionRaw) {
		this.javaCommandCollectionRaw = aJavaCommandCollectionRaw;
		this.testDataImporter = new TestDataImporter(GenerationRuntimeInfo.getInstance().getTestdataDir());
	}
	
	public static HashMap<Filename, List<JavaCommand>> doYourJob(final HashMap<Filename, List<JavaCommand>> aJavaCommandCollectionRaw) {
		return new TestSeriesBuilder(aJavaCommandCollectionRaw).buildTestCaseSeriesIfNecessary();
	}
	
	private HashMap<Filename, List<JavaCommand>> buildTestCaseSeriesIfNecessary() 
	{
		// split XX with rule definition into separate XXs and put result into javaCommandCollectionTemp 
		javaCommandCollectionRaw.keySet().stream().filter(this::isNoScript)
		                                          .filter(this::doesXXCorrectlyDefineAnyRule)
                                                  .forEach(this::buildTestCaseSeriesForRuleGroup);

		// put XX without rule definition into javaCommandCollectionTemp 
		javaCommandCollectionRaw.keySet().stream().filter(filename -> ! xxRuleGroups.containsKey(filename))
                                                  .forEach(filename -> javaCommandCollectionTemp.put(filename, javaCommandCollectionRaw.get(filename)));
		
		// split XX with parameter definition into separate XXs and put result into javaCommandCollectionResult 
		javaCommandCollectionTemp.keySet().stream().filter(this::isNoScript)
                                                   .filter(this::isTestCaseParameterized)
		                                           .forEach(this::buildTestCaseSeriesForParameter);
		
		// put XX without parameter definition into javaCommandCollectionResult 
		javaCommandCollectionTemp.keySet().stream().filter(filename -> ! testParameter.containsKey(filename))
                                                   .forEach(filename -> javaCommandCollectionResult.put(filename, javaCommandCollectionTemp.get(filename)));

		return javaCommandCollectionResult;
	}

	private boolean isNoScript(Filename filename) {
		return ! filename.value.endsWith("Script.java");
	}
	
	private void buildTestCaseSeriesForRuleGroup(final Filename filename)
	{
		xxCounter = 0;
		final List<JavaCommand> commands = javaCommandCollectionRaw.get(filename);
		final HashMap<String, List<JavaCommand>> separatedXXs = cutXXRuleGroupIntoSeparateXX(commands, filename);
		separatedXXs.keySet().forEach(xxid -> javaCommandCollectionTemp.put(createNewFilename(filename, xxid), 
				                                                            separatedXXs.get(xxid))); 
	}
	
	private boolean containsFilenamePlaceholder(String value) {
		return value.contains("<filename>") || value.contains("<Dateiname>")
		       || value.equalsIgnoreCase("<filename>") || value.equalsIgnoreCase("<Dateiname>");
	}
	
	private Filename createNewFilename(Filename filename, String xxid) 
	{
		String xxRuleGroup = xxRuleGroups.get(filename);
		if (containsFilenamePlaceholder(xxRuleGroup)) {
			xxRuleGroup = extractXXIdFromFilename(filename.value);
		}
		
		int pos = filename.value.lastIndexOf('/');
		return new Filename(filename.value.substring(0, pos) + "/" + xxRuleGroup.toLowerCase() + "/" + xxid + ".java");
	}

	private HashMap<String, List<JavaCommand>> cutXXRuleGroupIntoSeparateXX(final List<JavaCommand> commandsOfGroup, 
			                                                                final Filename filename) 
	{
		final HashMap<String, List<JavaCommand>> separatedXXs = new HashMap<>();
		JavaCommand groupDeclarationCommand = null;
		String xxid = null;
		List<JavaCommand> commandListOfseparatedXX = null;
		
		for (JavaCommand javaCommand : commandsOfGroup) 
		{
			if (javaCommand.value.contains(RULE_IDENTIFIER_METHOD_CALL)) {
				groupDeclarationCommand = replaceFilenamePlaceholderIfPresent(filename, javaCommand, "");
			}
			
			if (javaCommand.value.contains(XXID_IDENTIFIER_METHOD_CALL)) 
			{
				if (xxid != null) {
					separatedXXs.put(xxid, commandListOfseparatedXX);
				}
				
				if (groupDeclarationCommand == null) {
					throw new SysNatTestDataException("Rule declaration must occur before XXID declaration in '" 
				               + filename.value + "'.");
				}
				commandListOfseparatedXX = new ArrayList<>();
				commandListOfseparatedXX.add(groupDeclarationCommand);
				xxid = extractXXID(javaCommand.value);
				if (containsFilenamePlaceholder(javaCommand.value)) {
					javaCommand = replaceFilenamePlaceholderIfPresent(filename, javaCommand, "_" + ++xxCounter);
				}
				xxid = extractXXID(javaCommand.value);
			}
			
			if (xxid != null) {
				commandListOfseparatedXX.add(javaCommand);
			}
			
		}

		if (xxid != null) {
			separatedXXs.put(xxid, commandListOfseparatedXX);
		}

		return separatedXXs;
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
					                        + extractXXIdFromFilename(filename.value)
					                        + replacementSuffix
					                        + groupDeclarationCommand.value.substring(pos2+1);
		}
		return groupDeclarationCommand;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void buildTestCaseSeriesForParameter(final Filename filename)
	{
		final List<JavaCommand> commands = javaCommandCollectionTemp.get(filename);
		final String testParameterValue = testParameter.get(filename);
		final Hashtable<String, Properties> datasets;
		final boolean tableDataMode;
		
		if ( testParameterValue.startsWith(TableDataParser.TABLE_DATA_IDENTIFIER)) 
		{
			List<Properties> testParameter = TableDataParser.doYourJob(filename.value, testParameterValue);
			datasets = new Hashtable<String, Properties>();
			for (int i = 0; i < testParameter.size(); i++) {
				datasets.put("TableDataParameter_" + (i+1), testParameter.get(i));
			}
			tableDataMode = true;
		} else {
			datasets = testDataImporter.loadTestdata(testParameterValue);
			tableDataMode = false;
		}
		
		final List<String> dataSetNames = new ArrayList(datasets.keySet());
		nameOfCurrentFile = filename.value;
		dataSetNames.forEach(dataSetName -> buildTestCase(dataSetName, datasets.get(dataSetName), tableDataMode, commands)); 
	}
	
	private void buildTestCase(final String datasetName,
			                   final Properties dataset, 
			                   final boolean tableDataMode,                   
			                   final List<JavaCommand> commands) 
	{
		final List<JavaCommand> newCommands = new ArrayList<>();
		String datasetId = getDatasetUniqueIdentifier(datasetName);
		String xxid = "";
		
		for (JavaCommand javaCommand : commands) 
		{
			if (javaCommand.value.contains( "startNewXX" )) {
				xxid = extractXXID(javaCommand.value);
				newCommands.add(new JavaCommand("languageTemplatesCommon.startNewXX(\"" + xxid + "_" + datasetId + "\");"));
			} else if (javaCommand.value.contains(PARAMETER_IDENTIFIER_METHOD_CALL)) {
				if (tableDataMode) {
					newCommands.add(new JavaCommand("languageTemplatesCommon.importTestData(\"" + toTableData(dataset) + "\");"));
				} else {
					newCommands.add(new JavaCommand("languageTemplatesCommon.importTestData(\"" + datasetName + "\");"));
				}
			} else {
				newCommands.add(javaCommand);
			}
		}

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
		final String filenameWithPath = packagePath + compressedXXid + "_"
				                        + datasetId + "_Test.java";

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
	 * @return true if Rule declaration exists and is correct
	 * @throws SysNatTestDataException if Rule declaration is wrong
	 */
	private boolean doesXXCorrectlyDefineAnyRule(final Filename filename)
	{
		final List<JavaCommand> commands = javaCommandCollectionRaw.get(filename);	
		boolean xxGroupIdentifierPresent = false;
		int counterXXId = 0;
		
		for (JavaCommand javaCommand : commands) 
		{
			if (javaCommand.value.contains( XXID_IDENTIFIER_METHOD_CALL )) {
				counterXXId++;
			}
			
			if (javaCommand.value.contains( RULE_IDENTIFIER_METHOD_CALL )) 
			{
				xxGroupIdentifierPresent = true;
				if (xxRuleGroups.containsKey(filename)) {
					throw new SysNatTestDataException("Only one Rule can be declared in '" +
				                                       filename.value + "'.");
				}
				xxRuleGroups.put(filename, extractXXGroup(javaCommand.value));
			}
		}

		if (counterXXId == 0) {
			throw new SysNatTestDataException("Missing XXID declaration in '" +
                    filename.value + "'.");
		}
		
		if (counterXXId > 1 && ! xxGroupIdentifierPresent) {
			throw new SysNatTestDataException("Missing Rule declaration in '" +
                    filename.value + "'.");
		}
		
		if (xxRuleGroups.containsKey(filename)) {			
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
			if (javaCommand.value.contains( PARAMETER_IDENTIFIER_METHOD_CALL )) 
			{
				testParameter.put(filename, extractTestParameter(javaCommand.value));
				return true;
			}
		}
		
		return false;
	}
	
	
	private String extractTestParameter(String javaCommand) 
	{
		final int pos = javaCommand.indexOf(PARAMETER_IDENTIFIER_METHOD_CALL) + PARAMETER_IDENTIFIER_METHOD_CALL.length() + 1;
		return javaCommand.substring(pos, javaCommand.length()-3).trim();
	}

	private String extractXXGroup(String javaCommand) 
	{
		final int pos = javaCommand.indexOf(RULE_IDENTIFIER_METHOD_CALL) + RULE_IDENTIFIER_METHOD_CALL.length() + 1;
		return javaCommand.substring(pos, javaCommand.length()-3).trim();
	}

	private String extractXXID(String javaCommand) 
	{
		final int pos = javaCommand.indexOf(XXID_IDENTIFIER_METHOD_CALL) + XXID_IDENTIFIER_METHOD_CALL.length() + 1;
		return javaCommand.substring(pos, javaCommand.length()-3).trim();
	}

	private String extractXXIdFromFilename(String filename) 
	{
		String toReturn = SysNatStringUtil.cutExtension(filename);
		int pos = toReturn.replaceAll("\\\\", "/").lastIndexOf("/") + 1;
		toReturn = toReturn.substring(pos);
		if (toReturn.endsWith("Test")) {
			toReturn = toReturn.substring(0, toReturn.length()-4);
		}
		return toReturn;
	}

}