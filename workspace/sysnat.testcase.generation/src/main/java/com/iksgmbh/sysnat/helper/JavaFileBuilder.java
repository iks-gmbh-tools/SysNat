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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.JavaCommand;
import com.iksgmbh.sysnat.domain.JavaFieldData;

/**
 * Helper that builds complete JUnit classes ready for compilation.
 * 
 * @author Reik Oberrath
 */
public class JavaFileBuilder 
{
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
	
	private void createScriptMappingPropertiesFile() 
	{
		StringBuffer sb = new StringBuffer();
		scriptMappings.forEach( (simpleName, fullName) -> sb.append(simpleName)
				                                            .append("=")
				                                            .append(fullName)
				                                            .append(System.getProperty("line.separator")));
		SysNatFileUtil.writeFile(System.getProperty("sysnat.nls.lookup.file"), sb.toString().trim());
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
		String scriptTemplateContent = SysNatFileUtil.readTextFileToString(PATH_TO_TEMPLATES + "/ScriptTemplate.java");
		scriptTemplateContent = SysNatStringUtil.removeLicenceComment(scriptTemplateContent);
		final List<String> scriptTemplateLines = SysNatStringUtil.toListOfLines(scriptTemplateContent);
		final StringBuffer generatedCode = new StringBuffer();
		scriptTemplateLines.forEach(line -> checkPlaceHolderReplacement(generatedCode, line, placeHolderBlocks));
		final File targetFile = buildTargetFilename(instructionFilename);
		
		String fileContent = generatedCode.toString();
		fileContent = fileContent.replace("XY", applicationUnderTest); 
		fileContent = fileContent.replace("TO BE REPLACED: class java doc comment", "Autogenerated by SysNatTesting."); 
		fileContent = fileContent.replace("javafiletemplatepackage", getPackage(instructionFilename)); 
		
		fileContent = fileContent.replace("class ScriptTemplate", "class " + getSimpleFileName(instructionFilename));
		fileContent = fileContent.replace("public ScriptTemplate", "public " + getSimpleFileName(instructionFilename));

		toReturn.put(targetFile, fileContent);
		
		String scriptFullyQualifiedJavaName = SysNatStringUtil.cutExtension(instructionFilename.value).replaceAll("/", ".").replaceAll(" ", "");
		int pos = scriptFullyQualifiedJavaName.lastIndexOf('.');
		String simpleName = scriptFullyQualifiedJavaName.substring(pos+1);
		scriptMappings.put(simpleName, scriptFullyQualifiedJavaName);
	}

	private void buildJUnitTestCaseFile(final Filename instructionFilename)
	{
		String templateContent = SysNatFileUtil.readTextFileToString(PATH_TO_TEMPLATES + "/JUnitTestcaseTemplate.java");
		templateContent = SysNatStringUtil.removeLicenceComment(templateContent);
		final List<String> testcaseTemplateLines = SysNatStringUtil.toListOfLines(templateContent);
		final HashMap<String,List<String>> placeHolderBlocks = buildCommandBlocksForReplacements(instructionFilename);
		final StringBuffer generatedCode = new StringBuffer();
		testcaseTemplateLines.forEach(line -> checkPlaceHolderReplacement(generatedCode, line, placeHolderBlocks));
		final File testcaseFile = buildTargetFilename(instructionFilename);
		
		String fileContent = generatedCode.toString();
		fileContent = fileContent.replace("XY", applicationUnderTest); 
		fileContent = fileContent.replace("TO BE REPLACED: class java doc comment", "Autogenerated by SysNatTesting."); 
		fileContent = fileContent.replace("javafiletemplatepackage", getPackage(instructionFilename)); 
		fileContent = fileContent.replace("package ;", ""); 
		if (isTestcaseInactive(fileContent)) {
			fileContent = fileContent.replace("super.setUp();", "// super.setUp();     do not setUp inactive test cases !"); 
			fileContent = fileContent.replace("languageTemplatesCommon.startNewXX(", "setXXIdForInactiveTests("); 
		}
		
		fileContent = fileContent.replace("//import org.junit.Test;", "import org.junit.Test;"); 
		fileContent = fileContent.replace("JUnitTestCaseTemplate", "Executable Example"); 
		fileContent = fileContent.replace("class JUnitTestcaseTemplate", "class " + getSimpleFileName(instructionFilename));
		fileContent = fileContent.replace("//@Test", "@Test");		
		fileContent = fileContent.replace("//if (languageTemplateContainer", "if (" + findApplicationBasicLanguageTemplateContainer())	
		                         .replace("languageTemplateContainer", findApplicationBasicLanguageTemplateContainer());
		toReturn.put(testcaseFile, fileContent);
	}

	private boolean isTestcaseInactive(String fileContent) {
		return fileContent.contains("languageTemplatesCommon.setActiveState(\"no\");");
	}

	private String findApplicationBasicLanguageTemplateContainer() 
	{
		return languageTemplateContainerJavaFields
				.stream()
		        .filter(javaFieldData -> javaFieldData.type.getSimpleName().startsWith("LanguageTemplates") 
		        		                 && javaFieldData.type.getSimpleName().endsWith("Basics")
		        		                 && ! javaFieldData.type.getSimpleName().equals("LanguageTemplatesCommon"))
		        .findFirst()
		        .orElse(new JavaFieldData("languageTemplatesCommon", null))
		        .name;
	}

	private HashMap<String, List<String>> buildCommandBlocksForReplacements(final Filename instructionFilename) 
	{
		final HashMap<String,List<String>> placeHolderBlocks = new HashMap<>();

		final List<String> importBlock = getImportBlock();
		placeHolderBlocks.put("/* TO BE REPLACED: imports */", importBlock);
		
		final List<String> fieldsBlock = getFieldBlock();
		placeHolderBlocks.put("/* TO BE REPLACED: fields for language template containers */", fieldsBlock);

		final List<String> commandBlock = getCommandBlockFor(instructionFilename);
		placeHolderBlocks.put("/* TO BE REPLACED: Command Block */", commandBlock);
		
		final List<String> setupBlock = getSetupBlock();
		placeHolderBlocks.put("/* TO BE REPLACED: field initialization */", setupBlock);
		
		final List<String> shutdownBlock = getTearDownBlock();
		placeHolderBlocks.put("/* TO BE REPLACED: cleanup */", shutdownBlock);

		return placeHolderBlocks;
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
		
		return typesToImport;
	}

	private void addReturnTypeIfNecessary(final List<String> typesToImport,
			                              final List<JavaCommand> javaCommandList) 
	{
		javaCommandList.stream()
		               .filter( javaCommand -> javaCommand.importType != null)
		               .map( javaCommand -> javaCommand.importType )
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

	private List<String> getSetupBlock() {
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
		return toReturn.replaceAll("/", ".");
	}

	private File buildTargetFilename(final Filename instructionFilename) 
	{
		final String targetDir = System.getProperty("sysnat.generation.target.dir");
		return new File (targetDir,  instructionFilename.value);
	}

	private List<String> getCommandBlockFor(final Filename instructionFilename) 
	{
		final List<JavaCommand> javaCommands = javaCommandCollection.get(instructionFilename);
		final List<String> toReturn = new ArrayList<>();
		
		javaCommands.forEach(javaCommand -> addToJavaFile(javaCommand.value, toReturn));

		return toReturn;
	}

	private void addToJavaFile(final String line, final List<String> javaFileContent) 
	{
		if (line.contains(".startNewTestPhase(\"Precondition") )
		{
			javaFileContent.add( "" );
			javaFileContent.add( "// precondition block" );
			javaFileContent.add(line);
		}
		else if (line.contains(".startNewTestPhase(\"Arrange") )
		{
			javaFileContent.add( "" );
			javaFileContent.add( "// arrange block" );
			javaFileContent.add(line);
		}
		else if (line.contains(".startNewTestPhase(\"Act") )
		{
			javaFileContent.add( "" );
			javaFileContent.add( "// act block" );
			javaFileContent.add(line);
		}
		else if (line.contains(".startNewTestPhase(\"Assert") )
		{
			javaFileContent.add( "" );
			javaFileContent.add( "// assert block" );
			javaFileContent.add(line);
		}
		else if (line.contains(".startNewTestPhase(\"Cleanup") )
		{
			javaFileContent.add( "" );
			javaFileContent.add( "// cleanup block" );
			javaFileContent.add(line);
		}
		else if (line.contains(" = " ) )
		{
			javaFileContent.add(line);
			javaFileContent.add(buildStoreDataObjectLine(line));
		} else {
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
					generatedCode.append(lineToCheckForReplacement).append(System.getProperty("line.separator"));
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