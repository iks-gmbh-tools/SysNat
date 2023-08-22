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

import static com.iksgmbh.sysnat.common.utils.SysNatConstants.METHOD_CALL_IDENTIFIER_BEHAVIOUR_DECLARATION;
import static com.iksgmbh.sysnat.common.utils.SysNatConstants.METHOD_CALL_IDENTIFIER_FILTER_DEFINITION;
import static com.iksgmbh.sysnat.common.utils.SysNatConstants.METHOD_CALL_IDENTIFIER_START_XX;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.SysNatJUnitTestClassGenerator;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.JavaCommand;
import com.iksgmbh.sysnat.domain.JavaCommand.CommandType;
import com.iksgmbh.sysnat.domain.JavaFieldData;

/**
 * Helper that builds complete JUnit classes ready for compilation.
 * This is done by injecting the java commands provides as input parameters
 * into predefined java file templates. 
 * There are two types of java file templates: 
 * a template file for JUnit test cases and 
 * template file for script files.
 * 
 * @author Reik Oberrath
 */
public class JavaFileBuilder 
{
	// private static final String REPORT_CREATOR = "com.iksgmbh.sysnat.helper.ReportCreator";

	private static final String PATH_TO_TEMPLATES = "../sysnat.testcase.generation/src/main/java/javafiletemplatepackage";

	private static final String[] IGNORE_IMPORT_TYPE_ARRAY = {"java.lang.Object",
			                                                  "java.lang.Integer", 
			                                                  "java.lang.Long", 
			                                                  "java.lang.String",
			                                                  "void"};
	private static final List<String> IGNORE_IMPORT_TYPES = Arrays.asList( IGNORE_IMPORT_TYPE_ARRAY );
	
	private HashMap<Filename, List<JavaCommand>> javaCommandCollection;
	private HashMap<File, String> toReturn = new HashMap<>();
	private String applicationUnderTest;
	private List<JavaFieldData> languageTemplateContainerJavaFields;
	private HashMap<String, String> scriptMappings = new HashMap<>();
	
	private JavaFileBuilder(final HashMap<Filename, List<JavaCommand>> aJavaCommandCollection,
			                final String applicationName,
			                final List<JavaFieldData> languageTemplateContainerJavaFields) 
	{
		this.javaCommandCollection = aJavaCommandCollection;
		this.applicationUnderTest = applicationName;
		this.languageTemplateContainerJavaFields = languageTemplateContainerJavaFields;
	}
	
	/**
	 * Generates java code for JUnit tests. 
	 * For each input file an output file object and its content is generated.
	 * 
	 * @param aJavaCommandCollection HashMap containing for each input file a list or block of java commands
	 * @param applicationName name of application under test
	 * @param languageTemplateContainerJavaFields
	 * @return java code as a list of test classes ready to be written to file and to be compiled
	 */
	public static HashMap<File, String> doYourJob(
			final HashMap<Filename, List<JavaCommand>> aJavaCommandCollection,
            final String applicationName,
            final List<JavaFieldData> languageTemplateContainerJavaFields) 
	{
		return new JavaFileBuilder(aJavaCommandCollection, 
				                   applicationName, 
				                   languageTemplateContainerJavaFields).createJavaFileContents();
	}
	
	private HashMap<File, String> createJavaFileContents() 
	{
		javaCommandCollection.keySet().stream()
		                     .filter(this::isScript)
		                     .forEach(this::buildScriptFile);
		
		createScriptMappingPropertiesFile();
		
		// build first scripts then test cases !
		
		javaCommandCollection.keySet().stream()
					         .filter(this::isTestCase)
					         .forEach(this::buildJUnitTestCaseFile);
		
		return toReturn;
	}
	
	/**
	 * This file is used during test runtime to find
	 * the location of a script file references in a XX.
	 */
	private void createScriptMappingPropertiesFile() 
	{
		StringBuffer sb = new StringBuffer();
		scriptMappings.forEach( (simpleName, fullName) -> sb.append(simpleName)
				                                            .append("=")
				                                            .append(pathToLowerCase(fullName, '.'))
				                                            .append(System.getProperty("line.separator")));
		String filename = SysNatFileUtil.findAbsoluteFilePath(System.getProperty("sysnat.nls.lookup.file"));
		SysNatFileUtil.writeFile(filename, sb.toString().trim());
	}

	private boolean isTestCase(final Filename filename) {
		return ! isScript(filename);
	}

	private boolean isScript(final Filename filename) {
		return filename.value.endsWith("Script.java");
	}

	private void buildScriptFile(final Filename instructionFilename)
	{
		final HashMap<String,List<String>> placeHolderBlocks = buildCommandBlocksForReplacements(instructionFilename);
		String filename = SysNatFileUtil.findAbsoluteFilePath(PATH_TO_TEMPLATES + "/ScriptTemplate.java");
		String scriptTemplateContent = SysNatFileUtil.readTextFileToString(filename);
		scriptTemplateContent = SysNatStringUtil.removeLicenceComment(scriptTemplateContent);
		final List<String> scriptTemplateLines = SysNatStringUtil.toListOfLines(scriptTemplateContent);
		final StringBuffer generatedCode = new StringBuffer();
		scriptTemplateLines.forEach(line -> checkPlaceHolderReplacement(generatedCode, line, placeHolderBlocks));
		final File targetFile = buildTargetFilename(instructionFilename);
		
		String fileContent = generatedCode.toString();
		fileContent = fileContent.replace("XY", applicationUnderTest); 
		fileContent = fileContent.replace("TO BE REPLACED: class java doc comment", "Autogenerated by SysNat."); 
		fileContent = fileContent.replace("javafiletemplatepackage", getPackage(instructionFilename)); 
		
		fileContent = fileContent.replace("class ScriptTemplate", "class " + getSimpleFileName(instructionFilename));
		fileContent = fileContent.replace("public ScriptTemplate", "public " + getSimpleFileName(instructionFilename));

		toReturn.put(targetFile, fileContent);
		
		String scriptFullyQualifiedJavaName = SysNatStringUtil.cutExtension(instructionFilename.value).replaceAll("/", ".").replaceAll(" ", "");
		int pos = scriptFullyQualifiedJavaName.lastIndexOf('.');
		String fileName = scriptFullyQualifiedJavaName.substring(pos+1);
		scriptMappings.put(fileName, scriptFullyQualifiedJavaName);
	}

	private void buildJUnitTestCaseFile(final Filename instructionFilename)
	{	
		if (instructionFilename.value.contains("SAP_Export")) {
			// System.out.println("");
		}				
		String filename = SysNatFileUtil.findAbsoluteFilePath(PATH_TO_TEMPLATES + "/JUnitTestcaseTemplate.java");
		String templateContent = SysNatFileUtil.readTextFileToString(filename);
		templateContent = SysNatStringUtil.removeLicenceComment(templateContent);
		final List<String> testcaseTemplateLines = SysNatStringUtil.toListOfLines(templateContent);
		final HashMap<String,List<String>> placeHolderBlocks = buildCommandBlocksForReplacements(instructionFilename);
		final StringBuffer generatedCode = new StringBuffer();
		testcaseTemplateLines.forEach(line -> checkPlaceHolderReplacement(generatedCode, line, placeHolderBlocks));
		final File testcaseFile = buildTargetFilename(instructionFilename);
		
		String fileContent = generatedCode.toString();
		fileContent = fileContent.replace("XY", applicationUnderTest); 
		fileContent = fileContent.replace("TO BE REPLACED: class java doc comment", "Autogenerated by SysNat."); 
		fileContent = fileContent.replace("javafiletemplatepackage", getPackage(instructionFilename)); 
		fileContent = fileContent.replace("package ;", ""); 
		if (isTestcaseInactive(fileContent)) {
			fileContent = fileContent.replace("super.setUp();", "super.initShutDownHook();"); // do not setUp inactive XX !
			fileContent = fileContent.replace("languageTemplatesCommon" + SysNatConstants.METHOD_CALL_IDENTIFIER_START_XX, "setXXIdForInactiveTests("); 
		}
		
		fileContent = fileContent.replace("//import org.junit.Test;", "import org.junit.Test;"); 
		fileContent = fileContent.replace("JUnitTestCaseTemplate", "Executable Example"); 
		fileContent = fileContent.replace("class JUnitTestcaseTemplate", "class " + getSimpleFileName(instructionFilename).replace(".java", ""));
		fileContent = fileContent.replace("//@Test", "@Test");		
		fileContent = fileContent.replace("//if (languageTemplateContainer", "if (" + findApplicationBasicLanguageTemplateContainer())	
		                         .replace("languageTemplateContainer", findApplicationBasicLanguageTemplateContainer());
		
		fileContent = addBehaviourConstantIfNecessary(instructionFilename.value, fileContent);
		toReturn.put(testcaseFile, fileContent);
	}
	
	private String addBehaviourConstantIfNecessary(String filename, String fileContent)
	{
		if (fileContent.contains("final String BEHAVIOUR_ID =")) return fileContent;
		if (! fileContent.contains("executionInfo.register(BEHAVIOUR_ID")) return fileContent;

		List<String> lines = SysNatStringUtil.toList(fileContent, System.getProperty("line.separator"));
		String match = lines.stream().filter(l -> l.contains("public class")).findFirst().get();
		int index = lines.indexOf(match) + 2;
		String behaviourID = SysNatJUnitTestClassGenerator.extractNlxxFileNameFromJavaFile(filename);
		lines.add(index, "\tprivate static final String BEHAVIOUR_ID = \"" + behaviourID + "\";");
		
		match = lines.stream().filter(l -> l.contains("try {")).findFirst().get();
		index = lines.indexOf(match) + 1;
		lines.add(index, "\t\t\tlanguageTemplatesCommon.declareXXGroupForBehaviour(BEHAVIOUR_ID);");
	
		return SysNatStringUtil.listToString(lines, System.getProperty("line.separator"));
	}


	private boolean isTestcaseInactive(String fileContent) {
		return fileContent.contains("languageTemplatesCommon.setActiveState(\"no\");");
	}

	private String findApplicationBasicLanguageTemplateContainer() 
	{
		return languageTemplateContainerJavaFields
				.stream()
		        .filter(javaFieldData -> javaFieldData.type.getSimpleName().startsWith("LanguageTemplates") 
		        		                 && javaFieldData.type.getSimpleName().startsWith("LanguageTemplatesBasics_")
		        		                 && ! javaFieldData.type.getSimpleName().equals("LanguageTemplatesCommon"))
		        .findFirst()
		        .orElse(new JavaFieldData("languageTemplatesCommon", null))
		        .name;
	}

	private HashMap<String, List<String>> buildCommandBlocksForReplacements(final Filename instructionFilename) 
	{	
		final HashMap<String,List<String>> placeHolderBlocks = new HashMap<>();

		final List<String> constantsBlock = getConstantsBlock(instructionFilename);
		placeHolderBlocks.put("/* TO BE REPLACED: constants */", constantsBlock);

		final List<String> importBlock = getImportBlock();
		placeHolderBlocks.put("/* TO BE REPLACED: imports */", importBlock);
		
		final List<String> fieldsBlock = getFieldBlock();
		placeHolderBlocks.put("/* TO BE REPLACED: fields for language template containers */", fieldsBlock);
		
		final List<String> fieldInitializationBlock = getFieldInitializationBlock();
		placeHolderBlocks.put("/* TO BE REPLACED: field initialization */", fieldInitializationBlock);

		final List<String> initNumberOfTestCasesBlock = getInitNumberOfTestCasesBlock();
		placeHolderBlocks.put("/* TO BE REPLACED: init number of test cases*/", initNumberOfTestCasesBlock);

		final List<String> shutdownBlock = getTearDownBlock();
		placeHolderBlocks.put("/* TO BE REPLACED: technical cleanup */", shutdownBlock);

		final List<String> constructorBlock = getConstructorBlock(instructionFilename);
		placeHolderBlocks.put("/* TO BE REPLACED: constructor */", constructorBlock);
		
		final List<String> preconditionMethodBlock = getPreconditionMethodBlock(instructionFilename);
		placeHolderBlocks.put("/* TO BE REPLACED: business precondition */", preconditionMethodBlock);
		
		final List<String> testExecutionBlock = getTestExecutionBlockFor(instructionFilename);
		placeHolderBlocks.put("/* TO BE REPLACED: Command Block */", testExecutionBlock);

		final List<String> cleanupMethodBlock = getCleanUpMethodBlock(instructionFilename);
		placeHolderBlocks.put("/* TO BE REPLACED: business cleanup */", cleanupMethodBlock);

		final List<String> nlxxFilepath = getNlxxFilepath(instructionFilename);
		placeHolderBlocks.put("/* TO BE REPLACED: nlxxFilepath */", nlxxFilepath);
		
		return placeHolderBlocks;
	}
	
	private List<String> getNlxxFilepath(Filename instructionFilename)
	{		
		String[] splitResult = instructionFilename.value.split("/");
		List<String> toReturn = new ArrayList<>();

		for (int i = 0; i<splitResult.length; i++) 
		{
			String element = splitResult[i];
			if (! element.equals(ExecutionRuntimeInfo.getInstance().getTestApplication().getName()) &&
				! element.equals(SysNatConstants.SCRIPT_DIR)) 
			{
				element = element.replace("Test.java", "");
				if (element.endsWith("_")) {
					element = element.substring(0, element.length()-1);
				}
				toReturn.add("toReturn.add(\"" + element + "\"); ");
			}
		}

		return toReturn;
	}

	private List<String> getConstantsBlock(Filename filename) 
	{
		List<JavaCommand> commands = javaCommandCollection.get(filename);

		return commands.stream()
			       .filter(command -> command.commandType == CommandType.Constant)
			       .map(command -> command.value)
	               .collect(Collectors.toList());
	}

	private List<String> getPreconditionMethodBlock(Filename filename) 
	{
		List<String> toReturn = new ArrayList<>();
		List<JavaCommand> commands = javaCommandCollection.get(filename);
		List<JavaCommand> onetimePreconditions = getOnetimePreconditions(commands);

		if ( ! onetimePreconditions.isEmpty() ) 
		{
			toReturn.add("private void prepareOnceIfNeeded()");
			toReturn.add("{");
			toReturn.add("    if (executionInfo.isFirstXXOfGroup(BEHAVIOUR_ID))");
			toReturn.add("    {");
			onetimePreconditions.forEach(javaCommand -> addToJavaFile("        " + javaCommand.value, toReturn));
			toReturn.add("    }");
			toReturn.add("}");
		}

		return toReturn;
	}

	private List<JavaCommand> getOnetimePreconditions(List<JavaCommand> commands) 
	{
		return commands.stream()
				       .filter(command -> command.commandType == CommandType.OneTimePrecondition)
		               .collect(Collectors.toList());
	}

	private List<String> getCleanUpMethodBlock(Filename filename) 
	{
		List<String> toReturn = new ArrayList<>();
		List<JavaCommand> commands = javaCommandCollection.get(filename);		
		List<JavaCommand> onetimeCleanups = getOnetimeCleanups(commands);

		if ( ! onetimeCleanups.isEmpty() ) {
			toReturn.add("private void cleanupOnceIfNeeded()");
			toReturn.add("{");
			toReturn.add("    if (executionInfo.isLastXXOfGroup(BEHAVIOUR_ID))");
			toReturn.add("    {");
			//toReturn.add("        languageTemplatesCommon.createComment(ReportCreator.START_OF_ONETIME_CLEANUPS_COMMENT);");
			onetimeCleanups.forEach(javaCommand -> addToJavaFile("        " + javaCommand.value, toReturn));
			toReturn.add("    }");
			toReturn.add("}");
		}

		return toReturn;
	}

	private List<JavaCommand> getOnetimeCleanups(List<JavaCommand> commands) {
		return commands.stream()
				       .filter(command -> command.commandType == CommandType.OneTimeCleanup)
		               .collect(Collectors.toList());
	}

	private List<String> getConstructorBlock(Filename filename) 
	{
		List<JavaCommand> commands = javaCommandCollection.get(filename);
		List<String> constructorCode = new ArrayList<>();
		
		boolean constructorNeeded = 
		commands.stream().filter(command -> command.commandType == CommandType.OneTimePrecondition
		                         || command.commandType == CommandType.OneTimeCleanup
		                         || command.value.contains(SysNatConstants.METHOD_CALL_IDENTIFIER_BEHAVIOUR_DECLARATION))
		                 .findAny()
		                 .isPresent();
		
		if (constructorNeeded) 
		{
			String testName = getSimpleFileName(filename).replace(".java", "");
			String behaviourId = getBehaviourIdFor(filename);
			int xxGroupSize = getXXGroupSize(behaviourId);
			constructorCode.add("public " + testName + "() {");
			constructorCode.add("\texecutionInfo.register(BEHAVIOUR_ID, " + xxGroupSize + ");");
			constructorCode.add("}");
		} 
		
		return constructorCode;
	}

	private int getXXGroupSize(String behaviourId) 
	{
		return (int) javaCommandCollection.entrySet().stream()
				                          .filter(entry -> belongsToXXGroup(entry, behaviourId))
				                          .count();
	}

	private boolean belongsToXXGroup(final Entry<Filename, List<JavaCommand>> entry, 
			                         final String behaviourId) 
	{
		return entry.getValue().stream()
		              .filter(command -> command.commandType == CommandType.Constant)
		              .map(command -> command.value)
		              .filter(commandLine -> commandLine.startsWith(XXGroupBuilder.BEHAVIOUR_CONSTANT_DECLARATION))
		              .filter(commandLine -> commandLine.contains(behaviourId))
		              .findFirst().isPresent();
	}

	private String getBehaviourIdFor(Filename filename) 
	{
		List<JavaCommand> commands = javaCommandCollection.get(filename);
		JavaCommand declareBehaviorCommand = findDeclareBehaviorCommand(commands);
	
		if (declareBehaviorCommand == null) {
			return "";
		}
		
		String commandAsString = declareBehaviorCommand.value;
		int pos1 = commandAsString.indexOf("\"");
		int pos2 = commandAsString.lastIndexOf("\"");
		return commandAsString.substring(pos1+1, pos2);
	}

	private List<String> getFieldBlock() 
	{
		final List<String> toReturn = new ArrayList<>();
		languageTemplateContainerJavaFields.forEach(javaFieldData -> 
	                                                toReturn.add("protected " + javaFieldData.type.getSimpleName() + " " + javaFieldData.name + ";"));		
		return toReturn;
	}

	private List<String> getImportBlock() 
	{
		final List<String> toReturn = new ArrayList<>();
		languageTemplateContainerJavaFields.forEach(javaFieldData -> 
                                                    toReturn.add("import " + javaFieldData.type.getName()+ ";"));		

		final List<String> typesToImport = findImportTypes();
		typesToImport.forEach(s -> toReturn.add("import " + s+ ";"));
		return toReturn;
	}

	protected List<String> findImportTypes() 
	{
		final List<String> typesToImport = new ArrayList<>();
		
		javaCommandCollection.forEach( (filename, javaCommandList) -> 
		                                addReturnTypeIfNecessary(typesToImport, 
		                                  		                 javaCommandList));

		// ? is this neccesary:
//		javaCommandCollection.forEach( (filename, javaCommandList) -> 
//                                        addReportCreatorReturnTypeIfNecessary(typesToImport, 
//        		                        javaCommandList));
	
		return typesToImport;
	}

//	private void addReportCreatorReturnTypeIfNecessary(List<String> typesToImport,
//			                                           List<JavaCommand> javaCommandList) 
//	{
//		boolean add =  javaCommandList.stream()
//				                      .map(command -> command.commandType)
//		                              .filter(type -> type == CommandType.OneTimeCleanup 
//		                                           || type == CommandType.OneTimePrecondition)
//		                              .findFirst().isPresent();
//		
//		
//		if (add && ! typesToImport.contains(REPORT_CREATOR)) typesToImport.add(REPORT_CREATOR);
//	}

	private void addReturnTypeIfNecessary(final List<String> typesToImport,
			                              final List<JavaCommand> javaCommandList) 
	{
		javaCommandList.stream()
		               .filter( javaCommand -> javaCommand.returnType != null)
		               .map( javaCommand -> javaCommand.returnType )
		               .filter( typeName -> ! IGNORE_IMPORT_TYPES.contains(typeName) )
		               .forEach( typeName ->  addReturnTypeIfNoDuplicate(typeName, typesToImport) );
	}

	private void addReturnTypeIfNoDuplicate(String type, List<String> typesToImport) {
		if ( ! typesToImport.contains(type) ) 
			typesToImport.add(type);
	}

	private List<String> getTearDownBlock() 
	{
		final List<String> toReturn = new ArrayList<>();
		languageTemplateContainerJavaFields.forEach(javaFieldData -> 
												    toReturn.add(javaFieldData.name + " = null;"));
		return toReturn;
	}

	
	private List<String> getInitNumberOfTestCasesBlock() {
		final List<String> toReturn = new ArrayList<>();
		
		int num = 0;
		
		for (Filename key : javaCommandCollection.keySet()) {
			if ( ! key.value.endsWith(SysNatConstants.SCRIPT_SUFFIX + ".java")) num++;
		}
		
		toReturn.add("initNumberOfTestCases(" + num + ");");
		return toReturn;
	}
	
	private List<String> getFieldInitializationBlock() {
		final List<String> toReturn = new ArrayList<>();
		languageTemplateContainerJavaFields.forEach(javaFieldData -> 
		                                            toReturn.add(javaFieldData.name + " = new " + javaFieldData.type.getSimpleName() + "(this);"));
		return toReturn;
	}

	private String getSimpleFileName(final Filename instructionFilename) 
	{
		String toReturn = SysNatStringUtil.cutExtension(instructionFilename.value);
		int pos = toReturn.lastIndexOf("/");
		return toReturn.substring(pos + 1);
	}

	private String getPackage(final Filename instructionFilename) 
	{
		String toReturn = instructionFilename.value;
		int pos = toReturn.lastIndexOf("/");
		if (pos > 0) {
			toReturn = SysNatStringUtil.cutExtension(toReturn);
		}
		pos = toReturn.lastIndexOf("/");
		if (pos == -1) {
			toReturn = "";
		} else {
			toReturn = toReturn.substring(0, pos);			
		}
		toReturn = toReturn.replaceAll("/", ".");
		
		if (toReturn.endsWith(".")) {
			toReturn = toReturn.substring(0, toReturn.length()-1);
		}
		
		return toReturn.toLowerCase();
	}

	private File buildTargetFilename(final Filename instructionFilename) 
	{
		String targetDir = System.getProperty("sysnat.generation.target.dir");
		targetDir = SysNatFileUtil.findAbsoluteFilePath(targetDir);
		String filename = pathToLowerCase(instructionFilename);
		return new File (targetDir.toLowerCase(),  filename);
	}

	private String pathToLowerCase(final String path, char separator)
	{
		int pos = path.lastIndexOf(separator);
		String filename = path.substring(0, pos).toLowerCase();
		filename += path.substring(pos);
		return filename;
	}

	private String pathToLowerCase(final Filename instructionFilename) {
		return pathToLowerCase(instructionFilename.value, '/');
	}

	private List<String> getTestExecutionBlockFor(final Filename instructionFilename) 
	{
		List<String> toReturn = new ArrayList<>();
		List<JavaCommand> commands = javaCommandCollection.get(instructionFilename);
		int index = findIndexOfCommandThatMatch(commands, METHOD_CALL_IDENTIFIER_BEHAVIOUR_DECLARATION); 
		if (index > -1) 
		{
			JavaCommand declareGroupCommand = commands.remove(index);
			addToJavaFile(declareGroupCommand.value, toReturn);
		}
		
		index = findIndexOfCommandThatMatch(commands, METHOD_CALL_IDENTIFIER_START_XX);
		if (index > -1) 
		{
			JavaCommand startXXCommand = commands.remove(index);
			addToJavaFile(startXXCommand.value, toReturn);
			
			JavaCommand filterCommand;
			index = findIndexOfCommandThatMatch(commands, METHOD_CALL_IDENTIFIER_FILTER_DEFINITION);
			if (index > -1) 
			{
				filterCommand = commands.remove(index);
			} else {
				String value = "languageTemplatesCommon" + METHOD_CALL_IDENTIFIER_FILTER_DEFINITION + "\"" + SysNatConstants.NO_FILTER +  "\");";
				filterCommand = new JavaCommand(value, CommandType.Standard);
			}
			addToJavaFile(filterCommand.value, toReturn);
		}
				
		List<JavaCommand> testExeCommands = extractTestExecutionCommands(commands);
		JavaCommand declareBehaviorCommand = findDeclareBehaviorCommand(testExeCommands);
		JavaCommand defineFeatureCommand = findDefineFeatureCommand(testExeCommands);
		
		if (declareBehaviorCommand != null) 
		{
			if (defineFeatureCommand != null) {
				testExeCommands.remove(defineFeatureCommand);
				addToJavaFile(defineFeatureCommand.value, toReturn);
			}
			testExeCommands.remove(declareBehaviorCommand);
			addToJavaFile(declareBehaviorCommand.value, toReturn);
		}
		
		boolean oneTimePreconditionPresent = isOneTimePreconditionPresent(commands);
		boolean oneTimeCleanupsPresent = isOnetimeCleanupPresent(commands);
		
		if (oneTimePreconditionPresent) {
			toReturn.add("prepareOnceIfNeeded();");
		}

		testExeCommands.forEach(javaCommand -> addToJavaFile(javaCommand.value, javaCommand.testObjectParameters, toReturn));

		if ( oneTimeCleanupsPresent ) {
			toReturn.add("cleanupOnceIfNeeded();");
		}

		return toReturn;
	}

	private int findIndexOfCommandThatMatch(List<JavaCommand> commands, String substring)
	{
		int count = -1;
		for (JavaCommand javaCommand : commands) 
		{
			count++;
			if (javaCommand.value.contains(substring)) 
			{
				return count;
			}
		}

		return -1;
	}

	private JavaCommand findDefineFeatureCommand(List<JavaCommand> commands) 
	{
		return commands.stream()
			       .filter(command -> command.value.contains("setBddKeyword(\"Feature\")"))
			       .findFirst()
			       .orElse(null);
	}

	private JavaCommand findDeclareBehaviorCommand(List<JavaCommand> commands) 
	{
		return commands.stream()
				       .filter(command -> command.value.contains(METHOD_CALL_IDENTIFIER_BEHAVIOUR_DECLARATION))
				       .findFirst()
				       .orElse(null);
	}
	
	private List<JavaCommand> extractTestExecutionCommands(List<JavaCommand> commands) 
	{
		List<JavaCommand> toReturn = new ArrayList<>();

		commands.stream()
		        .filter(command -> command.commandType != CommandType.OneTimePrecondition)
		        .filter(command -> command.commandType != CommandType.OneTimeCleanup)
		        .filter(command -> command.commandType != CommandType.Constant)
		        .forEach(command -> toReturn.add(command));

		return toReturn;
	}

	private boolean isOnetimeCleanupPresent(final List<JavaCommand> commands) {
		return ! getOnetimeCleanups(commands).isEmpty();
	}

	private boolean isOneTimePreconditionPresent(final List<JavaCommand> commands) {
		return ! getOnetimePreconditions(commands).isEmpty();
	}

	private void addToJavaFile(final String line, final List<String> javaFileContent) 
	{
		addToJavaFile(line, new ArrayList<>(), javaFileContent);
	}

	private void addToJavaFile(final String line, List<Object> testObjectParameters, final List<String> javaFileContent) 
	{
		if (line.contains(".startNewTestPhase(") )
		{
			handleNewTestPhase(line, javaFileContent);
		}
		else if (line.contains(SysNatConstants.METHOD_CALL_IDENTIFIER_STORE_TEST_OBJECT) )
		{
			javaFileContent.add(line);
			javaFileContent.add(buildStoreDataObjectLine(line));
		} else {
			if (testObjectParameters != null) 
			{
				// remove not needed command if necessary
				for (Object testObjectParam : testObjectParameters) 
				{
					List<String> toRemove = new ArrayList<>();
					for (String commandLine : javaFileContent) {
						int pos = commandLine.indexOf("(");
						if (pos == -1) continue;
						String parameterString = commandLine.substring(pos);
						if (commandLine.startsWith("storeTestObject(") && parameterString.contains(testObjectParam.toString().toLowerCase())) {
							toRemove.add(commandLine);
						}
					}
					javaFileContent.removeAll(toRemove);
				}
			}
			javaFileContent.add(line);
		}

	}

	private void handleNewTestPhase(String line, List<String> javaFileContent) 
	{
		if (line.contains("Precondition") )
		{
			javaFileContent.add( "" );
			javaFileContent.add( "// precondition block" );
			javaFileContent.add(line);
		}
		else if (line.contains("Arrange") || line.contains("Vorbereitung"))
		{
			javaFileContent.add( "" );
			javaFileContent.add( "// arrange block" );
			javaFileContent.add(line);
		}
		else if (line.contains("Act") || line.contains("Durchführung"))
		{
			javaFileContent.add( "" );
			javaFileContent.add( "// act block" );
			javaFileContent.add(line);
		}
		else if (line.contains("Assert") || line.contains("Überprüfung"))
		{
			javaFileContent.add( "" );
			javaFileContent.add( "// assert block" );
			javaFileContent.add(line);
		}
		else if (line.contains("Cleanup") )
		{
			javaFileContent.add( "" );
			javaFileContent.add( "// cleanup block" );
			javaFileContent.add(line);
		}
	}

	private String buildStoreDataObjectLine(String line)
	{
		int pos1 = line.indexOf(' ');
		int pos2 = line.indexOf('=');
		String testObjectName = line.substring(pos1, pos2).trim();
		return "storeTestObject(\""  + testObjectName + "\", " + testObjectName + ");";
	}

	private void checkPlaceHolderReplacement(final StringBuffer generatedCode, 
			                                 final String lineToCheckForReplacement, 
			                                 final HashMap<String,List<String>> placeholderBlocks) 
	{
		if (lineToCheckForReplacement.trim().startsWith("//#")) {
			return; // ignore these lines
		}
		
		final Iterator<String> iterator = placeholderBlocks.keySet().iterator();
		boolean goOn = true;
		String toBeReplacedMarker = iterator.next();
		
		while (goOn) 
		{
			final List<String> commandBlock = placeholderBlocks.get(toBeReplacedMarker);
			goOn = checkPlaceHolderReplacement(generatedCode, lineToCheckForReplacement, toBeReplacedMarker, commandBlock);
			if (goOn) {
				if ( iterator.hasNext() ) {
					toBeReplacedMarker = iterator.next();
				} else {
					goOn = false;
					generatedCode.append(lineToCheckForReplacement)
					             .append(System.getProperty("line.separator"));
				}
			}
		}
	}

	private boolean checkPlaceHolderReplacement(final StringBuffer generatedCode,
			                                    final String lineToCheckForReplacement,
			                                    final String toBeReplacedMarker, 
                                                final List<String> commandBlock)
	{
		if (lineToCheckForReplacement.trim().equals(toBeReplacedMarker)) {
			commandBlock.forEach(command -> appendLine(generatedCode, command, lineToCheckForReplacement));
			return false;
		} else {
			return true;
		}
	}

	private void appendLine(StringBuffer sb, String newLine, String oldLine) 
	{
		final String leadingWhiteSpace;
		if (newLine.trim().equals("")) {
			leadingWhiteSpace = "";
		} else {
			leadingWhiteSpace = SysNatStringUtil.getLeadingWhiteSpace(oldLine);
		}
		sb.append(leadingWhiteSpace)
          .append(newLine)
          .append(System.getProperty("line.separator"));
	}
}