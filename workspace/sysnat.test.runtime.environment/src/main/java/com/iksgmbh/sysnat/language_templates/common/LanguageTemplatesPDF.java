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

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;
import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.common.exception.SysNatValidationException;
import com.iksgmbh.sysnat.common.helper.pdftooling.PdfFileContent;
import java.io.File;
import java.util.List;
import java.util.*;

/**
 * Contains the implementation of the PDF content validation.
 */
@LanguageTemplateContainer
public class LanguageTemplatesPDF
{
	protected ExecutionRuntimeInfo executionInfo;
	protected ExecutableExample executableExample;

	public LanguageTemplatesPDF(ExecutableExample aExecutableExample)
	{
		this.executableExample = aExecutableExample;
		this.executionInfo = ExecutionRuntimeInfo.getInstance();
	}


	public void assertFileContainsText(final File pdfFile,
									   final String text)
	{
		final PdfFileContent pdfFileContent = new PdfFileContent(pdfFile.getAbsolutePath());
		final String textToSearch = buildSearchText(text);
		boolean ok = pdfFileContent.doesFileContain(textToSearch);
		String question = "Enthält das Dokument " + pdfFile.getName() + " den Text \"" + textToSearch + "\"? - ";
		executableExample.answerQuestion(question, ok);
	}


	public void assertFileContainsTextOnPage(final File pdfFile,
										     final String pageIdentifier,
										     final String text)
	{
		final PdfFileContent pdfFileContent = new PdfFileContent(pdfFile.getAbsolutePath());
		final String textToSearch = buildSearchText(text);
		boolean ok = pdfFileContent.doesPageContain(pageIdentifier, textToSearch);
		String question = "Enthält das Dokument " + pdfFile.getName() + " auf der Seite '" + pageIdentifier + "' den Text \"" + textToSearch + "\"? - ";
		executableExample.answerQuestion(question, ok);
	}

	public void assertFileContainsTextInLine(final File pdfFile,
										     final String pageIdentifier,
										     final int lineNo,
										     final String text)
	{
		final PdfFileContent pdfFileContent = new PdfFileContent(pdfFile.getAbsolutePath());
		final String textToSearch = buildSearchText(text);
		boolean ok = pdfFileContent.doesLineContain(pageIdentifier, lineNo, textToSearch);
		String question = "Enthält das Dokument " + pdfFile.getName() + " auf der Seite '" + pageIdentifier + "' in der Zeile " + lineNo + " den Text \"" + textToSearch + "\"? - ";
		executableExample.answerQuestion(question, ok);
	}

	public void assertFileContainsValidationTexts(final File pdfFile,
											      final String validationFileName)
	{
		Hashtable<String, Properties> contentDefinitions = executableExample.importTestData(validationFileName);
		Optional<String> validationData = contentDefinitions.keySet().stream().findFirst();
		if ( ! validationData.isPresent() ) {
			throw new SysNatTestDataException("Datei '" + pdfFile.getAbsolutePath() + " nicht gefunden.");
		}
		Properties toValidate = contentDefinitions.get(validationData.get());
		Set<String> propertyNames = toValidate.stringPropertyNames();
		List<String> textsToSearch = new ArrayList<>();
		textsToSearch.addAll(propertyNames);
		Collections.sort(textsToSearch);

		textsToSearch.forEach(textToSearch -> validate(pdfFile, textToSearch, (String)toValidate.get(textToSearch)));
	}

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
				throw new SysNatTestDataException("Die Angabe '" + line + " ist keine Zeilenangabe.");
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
			throw new SysNatValidationException("Die Angabe <" + valueIdentifier + "> stellt keinen bekannten Wert dar.");
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