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
package com.iksgmbh.sysnat.language_templates.common;

import static com.iksgmbh.sysnat.common.utils.SysNatConstants.QUESTION_IDENTIFIER;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;
import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.common.exception.SysNatValidationException;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.helper.docval.DocumentComparer;
import com.iksgmbh.sysnat.helper.docval.DocumentStructureAnalyser;
import com.iksgmbh.sysnat.helper.docval.DocumentStructureVerifier;
import com.iksgmbh.sysnat.helper.docval.domain.DocumentCompareIgnoreConfig;
import com.iksgmbh.sysnat.helper.docval.domain.DocumentContent;
import com.iksgmbh.sysnat.helper.docval.domain.DocumentStructure;
import com.iksgmbh.sysnat.testdataimport.domain.DocumentContentCompareValidationRule;
import com.iksgmbh.sysnat.testdataimport.domain.DocumentContentCompareValidationRule.ComparisonRuleType;
import com.iksgmbh.sysnat.testdataimport.domain.DocumentContentSearchValidationRule;
import com.iksgmbh.sysnat.testdataimport.domain.DocumentValidationRule;

/**
 * Contains the implementation of language templates for the purpose of validating 
 * the content of document files.
 * If needed, they can be used in all test applications. 
 */
@LanguageTemplateContainer
public class LanguageTemplatesDocVal
{
	protected ExecutionRuntimeInfo executionInfo;
	protected ExecutableExample executableExample;

	public LanguageTemplatesDocVal(ExecutableExample aExecutableExample)
	{
		this.executableExample = aExecutableExample;
		this.executionInfo = ExecutionRuntimeInfo.getInstance();
	}

	// ###########################################################################
	//            L a n g u a g e   T e m p l a t e   M e t h o d s
	// ###########################################################################

	
	/**
	 * This method uses a String to identify the file to check.
	 * @param document as a String id
	 * @param expectedPageNumber
	 */
	@LanguageTemplate("Besteht das Dokument mit dem Namen ^^ aus ^^ Seite(n)?")
	@LanguageTemplate("Does document with name ^^ consists of ^^ page(s)?")
	public void doesDocumentWithNameContainExcactNumberOfPages(String documentName, int expectedPageNumber) 
	{
		File file = (File) executableExample.getTestObject(documentName);
		if (file == null) {
			throw new SysNatTestDataException("The document <b>" + documentName + "</b> is unknown and must be defined as return value before this instruction is called.");
		}
		doesDocumentContainExcactNumberOfPages(file, expectedPageNumber);
	}

	/**
	 * This method uses the file object of the dynamic test object.
	 * @param documentFile as a file
	 * @param expectedPageNumber
	 */
	@LanguageTemplate("Besteht das Dokument '' aus ^^ Seite(n)?")
	@LanguageTemplate("Does document '' consists of ^^ page(s)?")
	public void doesDocumentContainExcactNumberOfPages(File documentFile, int expectedPageNumber) 
	{
		final DocumentContent documentAnalyser = new DocumentContent(documentFile.getAbsolutePath());
		final boolean ok = expectedPageNumber == documentAnalyser.getNumberOfPages();
		final String question = "Does document <b>" + documentFile.getName() 
		                        + "</b> consist of <b>" + expectedPageNumber + "</b> page(s)" 
				                + QUESTION_IDENTIFIER;

		executableExample.answerQuestion(question, ok);	
	}
	
	@LanguageTemplate(value = "Entspricht das Dokument '' den Validationsregeln in ^^?")
	@LanguageTemplate(value = "Does document '' match the validation rules in ^^?")
	public void validateDocumentContent(final File documentFile,
							            final String validationFileName)
	{
		Hashtable<String, Properties> contentDefinitions = executableExample.importTestData(validationFileName);
		Optional<String> validationDataOptional = contentDefinitions.keySet().stream().findFirst();
		if ( ! validationDataOptional.isPresent() ) {
			throw new SysNatTestDataException("File <b>" + documentFile.getAbsolutePath() + "</b> not found.");
		}
		Properties validationData = contentDefinitions.get(validationDataOptional.get());
		File validationFile = executableExample.getTestDataImporter().getLastFilesLoaded().get(0);
		validationData.put("Filename", validationFile.getAbsolutePath());
		String keyOrderAsString = validationData.getProperty("RuleOrder");
		List<String> orderedValidationRuleNames = SysNatStringUtil.toList(keyOrderAsString, "|");
		validateContent(documentFile, orderedValidationRuleNames, validationData);
	}
	
	
	// ######################### 	T e x t   S e a r c h    ############################
	
	@LanguageTemplate(value = "Enthält das Dokument '' den Text ^^?")
	@LanguageTemplate(value = "Does document '' contain the sequence ^^?")
	public void assertFileContainsText(final File documentFile,
									   final String text)
	{
		final DocumentContent documentFileContent = new DocumentContent(documentFile.getAbsolutePath());
		final String textToSearch = buildSearchText(text);
		boolean ok = documentFileContent.doesFileContain(textToSearch);
		String question = "Does document <b>" + documentFile.getName() + "</b> contain the sequence <b>" + textToSearch + "</b>" + QUESTION_IDENTIFIER;
		executableExample.answerQuestion(question, ok);
	}
	
	@LanguageTemplate(value = "Enthält das Dokument '' auf der Seite ^^ den Text ^^?")
	@LanguageTemplate(value = "Does document '' on page ^^ contain the sequence ^^?")
	public void assertFileContainsTextOnPage(final File documentFile,
										     final String pageIdentifier,
										     final String text)
	{
		final DocumentContent documentFileContent = new DocumentContent(documentFile.getAbsolutePath());
		final String textToSearch = buildSearchText(text);
		boolean ok = documentFileContent.doesPageContain(pageIdentifier, textToSearch);
	    try {
		      ok = documentFileContent.doesPageContain(pageIdentifier, textToSearch);
		   } catch (IllegalArgumentException e) {
		      executableExample.failWithMessage("Text zur Seitenidentifikation <b>" + pageIdentifier + "</b> "
		      		                            + "ist im Dokument <b>" + documentFile.getName() + "</b> nicht enthalten.");
		}
		String question = "Does document <b>" + documentFile.getName() + "</b> on page <b>" 
		                  + pageIdentifier + "</b> contain the sequence <b>" + textToSearch + "</b>" + QUESTION_IDENTIFIER;
		executableExample.answerQuestion(question, ok);
	}

	@LanguageTemplate(value = "Enthält das Dokument '' auf der Seite ^^ in Zeile ^^ den Text ^^?")
	@LanguageTemplate(value = "Does document '' on page ^^ in line ^^ contain the sequence ^^?")
	public void assertFileContainsTextInLine(final File documentFile,
										     final String pageIdentifier,
										     final int lineNo,
										     final String text)
	{
		final DocumentContent documentFileContent = new DocumentContent(documentFile.getAbsolutePath());
		final String textToSearch = buildSearchText(text);
		boolean ok = documentFileContent.doesLineContain(pageIdentifier, lineNo, textToSearch);
		String question = "Does document <b>" + documentFile.getName() + "</b> on page <b>" 
		                  + pageIdentifier + "</b> in line <b>" + lineNo 
		                  + "</b> contain the sequence <b>" + textToSearch + "</b>" + QUESTION_IDENTIFIER;
		executableExample.answerQuestion(question, ok);
	}

	@LanguageTemplate(value = "Enthält das Dokument '' auf der Seite mit dem Text ^^ den Text ^^?")
	@LanguageTemplate(value = "Does document '' on page with sequence ^^ contain the sequence ^^?")
	public void assertFileContainsTextOnRelativePage(final File documentFile,
										             final String pageIdentifier,
										             final String text)
	{
		final DocumentContent documentFileContent = new DocumentContent(documentFile.getAbsolutePath());
		final String textToSearch = buildSearchText(text);
		boolean ok = documentFileContent.doesPageContain(pageIdentifier, textToSearch);
		String question = "Does document <b>" + documentFile.getName() + "</b> on page with sequence <b>" 
		                  + pageIdentifier + "</b> contain the sequence <b>" + textToSearch + "</b>" + QUESTION_IDENTIFIER;
		executableExample.answerQuestion(question, ok);
	}

	@LanguageTemplate(value = "Enthält das Dokument '' auf der Seite mit dem Text ^^ in Zeile ^^ den Text ^^?")
	@LanguageTemplate(value = "Does document '' on page with sequence  ^^ in line ^^ contain the sequence ^^?")
	public void assertFileContainsTextInRelativeLine(final File documentFile,
										             final String pageIdentifier,
										             final int lineNo,
										             final String text)
	{
		final DocumentContent documentFileContent = new DocumentContent(documentFile.getAbsolutePath());
		final String textToSearch = buildSearchText(text);
		boolean ok = documentFileContent.doesLineContain(pageIdentifier, lineNo, textToSearch);
		String question = "Does document <b>" + documentFile.getName() + "</b> on page with sequence <b>" 
		                  + pageIdentifier + "</b> in line <b>" + lineNo 
		                  + "</b> contain the sequence <b>" + textToSearch + "</b>" + QUESTION_IDENTIFIER;
		executableExample.answerQuestion(question, ok);
	}
	
	
	@LanguageTemplate(value = "Entspricht das Dokument '' den Textsuche-Regeln in ^^?")
	@LanguageTemplate(value = "Does document '' match the text-search rules in ^^?")
	public void validateDocumentContentByTextSearch(final File documentFile,
							                        final String validationFileName)
	{
		validateDocumentContent(documentFile, validationFileName);
	}

	
	// ######################### 	S T R U C T U R E    C O M P A R I S O N    ############################
	
	
	@LanguageTemplate(value = "Entspricht die Struktur des Dokuments '' unter der Annahme der Strukturdaten in ^^ dem Inhalt der Textdatei ^^?")
	@LanguageTemplate(value = "Does structure of document '' in assumption of structure information from ^^ match content of text file ^^?")
	public void validateDocumentStructureByTextFileComparison(final File documentFile,
			                                                  final String structureInfoFileName,
							                                  final String validationTextFileName)
	{
		File textFile = SysNatFileUtil.findFileRecursively(validationTextFileName, new File(executionInfo.getTestdataDir()));
		String expectedStructure = SysNatFileUtil.readTextFileToString(textFile);
		
		File structureDatFile = SysNatFileUtil.findFileRecursively(structureInfoFileName, new File(executionInfo.getTestdataDir()));
		DocumentStructure actualStructure = DocumentStructureAnalyser.doYourJob(documentFile, structureDatFile);
		String expected = removePathInformation(expectedStructure);
		String actual = removePathInformation(actualStructure.toString());
		final boolean ok = expected.equals(actual);
		
		if ( ! ok) 
		{
			System.err.println();
			System.err.println();
			System.err.println("######################################################################");
			System.err.println("EXPECTED");
			System.err.println(expected);
			System.err.println("######################################################################");
			System.err.println("ACTUAL");
			System.err.println(actual);
			System.err.println("######################################################################");
			System.err.println();
		}
	
		final String question = "Does structure of document <b>" + documentFile.getName() 
		                        + "</b> in assumption of <b>" + structureInfoFileName + "</b> "
		                        + "match content of file <b>" + validationTextFileName + "</b>" 
				                + QUESTION_IDENTIFIER;
		executableExample.answerQuestion(question, ok);	
	}
	
	private String removePathInformation(String expectedStructure)
	{
		int pos1 = expectedStructure.indexOf("'");
		int pos2 = expectedStructure.lastIndexOf("'");
		String path = expectedStructure.substring(pos1+1, pos2);
		pos1 = path.lastIndexOf("\\");
		pos2 = path.lastIndexOf("(");
		if (pos2 == -1) pos2 = path.lastIndexOf(".");
		if (pos2 == -1) pos2 = path.length();
		
		return expectedStructure.replace(path, path.substring(pos1+1, pos2).trim());
	}

	@LanguageTemplate(value = "Entspricht die Struktur des Dokuments '' unter der Annahme der Strukturdaten in ^^ der Struktur ^^?")
	@LanguageTemplate(value = "Does structure of document '' in assumption of structure information from ^^ match structure ^^?")
	public void validateDocumentStructureByPredefinedStructure(final File documentFile,
			                                                   final String structureInfoFileName,
							                                   final String idOrNameOfStructure)
	{
		File structureDatFile = SysNatFileUtil.findFileRecursively(structureInfoFileName, new File(executionInfo.getTestdataDir()));
		String result = DocumentStructureVerifier.doYourJob(documentFile, structureDatFile, idOrNameOfStructure);
		final boolean ok = DocumentStructureVerifier.OK_VERIFIED == result;
		final String question = "Does structure of document <b>" + documentFile.getName() 
		                        + "</b> in assumption of <b>" + structureInfoFileName + "</b> match "
		                        + "structure <b>" + idOrNameOfStructure + "</b>" 
				                + QUESTION_IDENTIFIER;

		executableExample.answerQuestion(question, ok);	
	}


	// ######################### 	W H O L E     C O N T E N T     C O M P A R I S O N    ############################

	
	@LanguageTemplate(value = "Entspricht der Inhalt des Dokuments '' den Inhaltsvergleichsregeln in ^^?")
	@LanguageTemplate(value = "Does content of document '' match the content-comparison rules in ^^?")
	public void validateDocumentContentByWholeContentComparison(final File documentFile,
							                                    final String validationFileName)
	{
		validateDocumentContent(documentFile, validationFileName);
	}

	@LanguageTemplate(value = "Entspricht der Inhalt des Dokuments '' exakt dem des Dokuments ^^?")
	@LanguageTemplate(value = "Does content of document '' and the one of document ^^ match exactly?")
	public void validateDocumentContentByExactComparison(final File documentFile,
							                             final String shouldBeDocumentPath)
	{
		File shouldBeDocument = SysNatFileUtil.findFileRecursively(shouldBeDocumentPath, new File(executionInfo.getTestdataDir()));
		final DocumentCompareIgnoreConfig emptyIgnoreConfig = new DocumentCompareIgnoreConfig();
		final String differenceReport = getDifferenceReport(documentFile, shouldBeDocument, emptyIgnoreConfig);
		boolean ok = differenceReport.length() == 0;

		if ( ! ok) 
		{
			System.err.println();
			System.err.println();
			System.err.println("######################################################################");
			System.err.println(differenceReport);
			System.err.println("######################################################################");
			System.err.println();
			System.err.println();
		}
		
		String question = "Does content of <b>" + documentFile.getName() + "</b> and content of <b>" 
		                  + shouldBeDocument.getName() + "</b> match exactly" + QUESTION_IDENTIFIER;
		
		if (!ok) {
			executableExample.addLinewiseToReportMessageAsComment(differenceReport);
		}
		
		executableExample.answerQuestion(question, ok);
	}
	
	
	// ###########################################################################
	//                    P r i v a t e   M e t h o d s
	// ###########################################################################

	private void validateContent(final File documentFile, 
			                     final List<String> orderedValidationRuleNames, 
			                     final Properties validationData)
	{
		String firstRuleAsString = (String) validationData.get(orderedValidationRuleNames.get(0));
		
		if ( ! DocumentValidationRule.isCompareRule(firstRuleAsString)) {
			validateByDocumentSearches(documentFile, orderedValidationRuleNames, validationData);
		} else {
			validateByDocumentComparison(documentFile, orderedValidationRuleNames, validationData);
		}
	}

	private void validateByDocumentSearches(final File documentFile,
	                                        final List<String> orderedValidationRuleNames,
	                                        final Properties validationData) 
	{
		orderedValidationRuleNames.forEach(ruleName -> validateDocumentBySearchRule(documentFile, (String)validationData.get(ruleName)));
	}

	private void validateByDocumentComparison(final File documentFile,
											  final List<String> orderedValidationRuleNames,
											  final Properties validationData)
	{
		final File shouldBeFile = getShouldBeFile(orderedValidationRuleNames, validationData);
		final DocumentCompareIgnoreConfig ignoreConfig = buildIgnoreConfig(orderedValidationRuleNames, validationData);
		final String differenceReport = getDifferenceReport(documentFile, shouldBeFile, ignoreConfig);
		boolean ok = differenceReport.length() == 0;

		if ( ! ok)
		{
			System.err.println();
			System.err.println();
			System.err.println("######################################################################");
			System.err.println(differenceReport);
			System.err.println("######################################################################");
			System.err.println();
			System.err.println();
		}	
		String question = "Does content of <b>" + documentFile.getName() + "</b> and content of <b>" 
		                  + shouldBeFile.getName() + "</b> match when applying <b>"
		                  + (new File(validationData.get("Filename").toString())).getName() + "</b>" + QUESTION_IDENTIFIER;
		
		if (!ok) {
			executableExample.addLinewiseToReportMessageAsComment(differenceReport);
		}
		
		executableExample.answerQuestion(question, ok);
	}

	private DocumentCompareIgnoreConfig buildIgnoreConfig(final List<String> orderedValidationRuleNames,
	                                                      final Properties validationData)
	{
		final DocumentCompareIgnoreConfig ignoreConfig = new DocumentCompareIgnoreConfig();
		orderedValidationRuleNames.forEach(ruleName -> addToIgnoreConfig((String)validationData.get(ruleName), ignoreConfig));
		return ignoreConfig;
	}


	private String getDifferenceReport(final File documentFile,
								       final File shouldBeFile,
								       final DocumentCompareIgnoreConfig ignoreConfig)
	{
		DocumentComparer documentFileComparer = new DocumentComparer(documentFile); 
		try {
			return documentFileComparer.getDifferenceReport(shouldBeFile.getAbsolutePath(), ignoreConfig);
		} catch (IOException e) {
			throw new SysNatException("Error accessing " + documentFile.getAbsolutePath());
		}
	}
	
	private File getShouldBeFile(List<String> orderedValidationRuleNames, Properties validationData)
	{
		Optional<DocumentContentCompareValidationRule> ruleOptional = 
				orderedValidationRuleNames.stream()
		                                  .map(ruleName -> DocumentValidationRule.getCompareInstance((String)validationData.get(ruleName)))
		                                  .filter(rule -> rule.getType()==ComparisonRuleType.ShouldBeFile)
		                                  .findFirst();
		
		if ( ! ruleOptional.isPresent() ) {
			throw new SysNatValidationException("Validation file used to validate a document by comparison does not define a should-be-file.");
		}
		
		String filename = (String) ruleOptional.get().getValue();
		File shouldBeFile = new File(new File(validationData.get("Filename").toString()).getParent(), filename);
		if ( ! shouldBeFile.exists() ) {
			System.err.println("File not found: " + shouldBeFile.getAbsolutePath());
			throw new SysNatValidationException("Should-Be-File '" + filename + "' defined in validation file does not exist.");
		}
		
		return shouldBeFile;
	}
	
	private void addToIgnoreConfig(String ruleAsString, DocumentCompareIgnoreConfig ignoreConfig)
	{
		final DocumentContentCompareValidationRule rule = DocumentValidationRule.getCompareInstance(ruleAsString);
		
		switch (rule.getType()) {
			case ShouldBeFile: return;  // do nothing
			case Dateformat: ignoreConfig.addDateformat(new SimpleDateFormat(rule.getValue()));
			                 break;
			case Prefix:     ignoreConfig.addPrefix(rule.getValue());
            				 break;
			case Substring:  ignoreConfig.addSubstring(rule.getValue());
            				 break;
			case Regex:      ignoreConfig.addRegex(rule.getValue());
			                 break;
			case LineDefinition: ignoreConfig.addLineDefinition(rule.getValue());
			                     break;
		}
	}

	private void validateDocumentBySearchRule(File documentFile, String ruleAsString)
	{
		final DocumentContentSearchValidationRule rule = DocumentValidationRule.getSearchInstance(ruleAsString);
		
		switch (rule.getType()) 
		{
			case Contains:       assertFileContainsText(documentFile, rule.getExpectedContent());	
			                     break;
			case ContainsOnPage: assertFileContainsTextOnPage(documentFile, ""+rule.getPageNumber(), rule.getExpectedContent());
                                 break;
			case ContainsInLine: assertFileContainsTextInLine(documentFile, ""+rule.getPageNumber(), rule.getLineNumber(), rule.getExpectedContent());
                                 break;
			case ContainsOnRelativePage: assertFileContainsTextOnPage(documentFile, rule.getPageIdentifier(), rule.getExpectedContent());
                                         break;
			case ContainsInRelativeLine: assertFileContainsTextInLine(documentFile, rule.getPageIdentifier(), rule.getLineNumber(), rule.getExpectedContent());
                                         break;
		}
	}


	private String buildSearchText(String text)
	{
		List<String> placeholders = extractPlaceholders(text);

		for (String placeholder: placeholders)
		{
			String valueIdentifier = placeholder.substring(1, placeholder.length() - 1);
			text = text.replace(placeholder, getValue(valueIdentifier));
		}

		return text;
	}

	private String getValue(String valueIdentifier)
	{
		if ( ! executableExample.doesTestObjectExist(valueIdentifier) ) {
			throw new SysNatValidationException("The value <b>" + valueIdentifier + "</b> does not represent a known Dynamic Test Object.");
		}
		return (String) executableExample.getTestObject(valueIdentifier);
	}

	private List<String> extractPlaceholders(String text)
	{
		List<String> toReturn = new ArrayList<>();

		boolean goOn = true;

		while (goOn)
		{
			int pos1 = text.indexOf('<');
			int pos2 = text.indexOf('>') + 1;
			if (pos1 > -1 && pos2 > -1 && pos1 < pos2) {
				toReturn.add(text.substring(pos1, pos2));
				text = text.substring(pos2);
			} else {
				goOn = false;
			}
		}

		return toReturn;
	}
}