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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;
import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.common.exception.SysNatValidationException;
import com.iksgmbh.sysnat.common.helper.pdftooling.PdfFileContent;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;

/**
 * Contains the implementation of language templates for the purpose of validating 
 * the content of PDF files.
 * If needed, they can be used in all test applications. 
 */
@LanguageTemplateContainer
public class LanguageTemplatesPDFValidation
{
	protected ExecutionRuntimeInfo executionInfo;
	protected ExecutableExample executableExample;

	public LanguageTemplatesPDFValidation(ExecutableExample aExecutableExample)
	{
		this.executableExample = aExecutableExample;
		this.executionInfo = ExecutionRuntimeInfo.getInstance();
	}

	// ###########################################################################
	//            L a n g u a g e   T e m p l a t e   M e t h o d s
	// ###########################################################################

	@LanguageTemplate(value = "Enthält das PDF '' den Text ^^?")
	@LanguageTemplate(value = "Does PDF '' contain ^^?")
	public void assertFileContainsText(final File pdfFile,
									   final String text)
	{
		final PdfFileContent pdfFileContent = new PdfFileContent(pdfFile.getAbsolutePath());
		final String textToSearch = buildSearchText(text);
		boolean ok = pdfFileContent.doesFileContain(textToSearch);
		String question = "Does PDF <b>" + pdfFile.getName() + "</b> contain <b>" + textToSearch + "</b>" + QUESTION_IDENTIFIER;
		executableExample.answerQuestion(question, ok);
	}


	@LanguageTemplate(value = "Enthält das PDF '' auf Seite ^^ den Text ^^?")
	@LanguageTemplate(value = "Does PDF '' on page ^^ contain ^^?")
	public void assertFileContainsTextOnPage(final File pdfFile,
										     final String pageIdentifier,
										     final String text)
	{
		final PdfFileContent pdfFileContent = new PdfFileContent(pdfFile.getAbsolutePath());
		final String textToSearch = buildSearchText(text);
		boolean ok = pdfFileContent.doesPageContain(pageIdentifier, textToSearch);
		String question = "Does PDF <b>" + pdfFile.getName() + "</b> on page <b>" 
		                  + pageIdentifier + "</b> contain <b>" + textToSearch + "</b>" + QUESTION_IDENTIFIER;
		executableExample.answerQuestion(question, ok);
	}

	@LanguageTemplate(value = "Enthält das PDF '' auf Seite ^^ in Zeile ^^ den Text ^^?")
	@LanguageTemplate(value = "Does PDF '' on page ^^ in line ^^ contain ^^?")
	public void assertFileContainsTextInLine(final File pdfFile,
										     final String pageIdentifier,
										     final int lineNo,
										     final String text)
	{
		final PdfFileContent pdfFileContent = new PdfFileContent(pdfFile.getAbsolutePath());
		final String textToSearch = buildSearchText(text);
		boolean ok = pdfFileContent.doesLineContain(pageIdentifier, lineNo, textToSearch);
		String question = "Does PDF <b>" + pdfFile.getName() + "</b> on page <b>" 
		                  + pageIdentifier + "</b> in line <b>" + lineNo 
		                  + "</b> contain <b>" + textToSearch + "</b>" + QUESTION_IDENTIFIER;
		executableExample.answerQuestion(question, ok);
	}

	@LanguageTemplate(value = "Entspricht das PDF '' den Validationsregeln in ^^?")
	@LanguageTemplate(value = "Does PDF '' match the validation rules in ^^?")
	public void assertPDFContentMatchesValidation(final File pdfFile,
											      final String validationFileName)
	{
		Hashtable<String, Properties> contentDefinitions = executableExample.importTestData(validationFileName);
		Optional<String> validationData = contentDefinitions.keySet().stream().findFirst();
		if ( ! validationData.isPresent() ) {
			throw new SysNatTestDataException("File <b>" + pdfFile.getAbsolutePath() + "</b> not found.");
		}
		Properties toValidate = contentDefinitions.get(validationData.get());
		String keyOrderAsString = toValidate.getProperty("keyOrder");
		List<String> orderedKeys = SysNatStringUtil.toList(keyOrderAsString, "|");
		orderedKeys.forEach(textToSearch -> validate(pdfFile, textToSearch, (String)toValidate.get(textToSearch)));
	}

	/**
	 * This method uses a String to identify the file to check.
	 * @param document as a String id
	 * @param expectedPageNumber
	 */
	@LanguageTemplate("Besteht das Dokument mit dem Namen ^^ aus ^^ Seite(n)?")
	@LanguageTemplate("Does PDF with name ^^ consists of ^^ page(s)?")
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
	 * @param pdfFile as a file
	 * @param expectedPageNumber
	 */
	@LanguageTemplate("Besteht das Dokument '' aus ^^ Seite(n)?")
	@LanguageTemplate("Does PDF '' consists of ^^ page(s)?")
	public void doesDocumentContainExcactNumberOfPages(File pdfFile, int expectedPageNumber) 
	{
		final PdfFileContent pdfAnalyser = new PdfFileContent(pdfFile.getAbsolutePath());
		final boolean ok = expectedPageNumber == pdfAnalyser.getPageNumber();
		final String question = "Does PDF <b>" + pdfFile.getName() 
		                        + "</b> consist of <b>" + expectedPageNumber + "</b> page(s)" 
				                + QUESTION_IDENTIFIER;

		executableExample.answerQuestion(question, ok);	
	}

	
	
	// ###########################################################################
	//                    P r i v a t e   M e t h o d s
	// ###########################################################################

	private void validate(File pdfFile, String textToSearch, String positionData)
	{
		if (positionData.isEmpty()) {
			assertFileContainsText(pdfFile, textToSearch);
		} else if (positionData.contains("::")) {
			String[] splitResult = positionData.split("::");
			String line = splitResult[1];
			int lineNo = -1;
			try {
				lineNo = Integer.valueOf(line);
			} catch (Exception e) {
				throw new SysNatTestDataException("The line <b>" + line + "</b> is not valid.");
			}
			assertFileContainsTextInLine(pdfFile, splitResult[0], lineNo, textToSearch);
		} else {
			assertFileContainsTextOnPage(pdfFile, positionData, textToSearch);
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