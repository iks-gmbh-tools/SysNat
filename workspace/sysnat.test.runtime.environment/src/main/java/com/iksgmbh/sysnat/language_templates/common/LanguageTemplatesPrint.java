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
import java.util.List;

import org.openqa.selenium.WebDriver;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;
import com.iksgmbh.sysnat.common.domain.FileList;
import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.common.helper.pdftooling.PdfPageContentAnalyser;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

@LanguageTemplateContainer
public class LanguageTemplatesPrint 
{
	public static final String NO_PDF_MESSAGE = "Problem: Es wurde keine PDF-Datei erzeugt!";

	private ExecutableExample executableExample;
	
	public LanguageTemplatesPrint(ExecutableExample aExecutableExample) {
		executableExample = aExecutableExample;
	}
	
	@LanguageTemplate("Die Liste der PDF-Dateien im Download-Verzeichnis ^^ als <> festgehalten.")
	public FileList getCurrentPdfFileList(String fileListName) 
	{
		FileList toReturn = SysNatFileUtil.findDownloadFiles("pdf");
		toReturn.setName(fileListName);
    	        
   		executableExample.addReportMessage("Es wurde die Liste der PDF-Dateien als <b>" + fileListName + "</b> festgehalten. "
   				                  + "Diese Liste umfasst <b>" + toReturn.size() + "</b> Dateien.");
   		return toReturn;
	}

	@LanguageTemplate("Ist die aktuelle Zahl der PDF-Dateien im Download-Verzeichnis um ^^ höher als in der ^^?")
	public void compareFileList(int number, FileList oldPdfFiles) 
	{
		FileList currentFileList = SysNatFileUtil.findDownloadFiles("pdf");
		boolean ok = oldPdfFiles.size() + number == currentFileList.size(); 
		String question = "Ist die aktuelle Zahl der PDF-Dateien im Download-Verzeichnis (" + currentFileList.size() + ") um "
				           + "<b>" + number + "</b> höher als in der <b>" + oldPdfFiles.getName() + "</b>? - ";
		executableExample.answerQuestion(question, ok);
	}

	@LanguageTemplate("Die jüngste PDF-Datei im Download-Verzeichnis ^^ wird als <> festgehalten.")
	public File storeLastPdfFile(String fileName) 
	{
		FileList currentFileList = SysNatFileUtil.findDownloadFiles("pdf");
		long millisAtTestStart = ExecutionRuntimeInfo.getInstance().getStartPointOfTime().getTime();
		File latestPdfFile = SysNatFileUtil.findLatest(currentFileList, millisAtTestStart);
		if (latestPdfFile == null) {
			return null;
		}
		return latestPdfFile;
	}
	
	@LanguageTemplate("Enthält das Dokument ^^ genau ^^ Seite(n)?")
	public void doesDocumentWithNameContainExcactNumberOfPages(String documentName, int expectedPageNumber) 
	{
		File file = (File) executableExample.getTestObject(documentName);
		if (file == null) {
			throw new SysNatTestDataException("The document <b>" + documentName + "</b> is unknown and must be defined as return value before this instruction is called.");
		}
		doesDocumentContainExcactNumberOfPages(file, expectedPageNumber);
	}
	
	@LanguageTemplate("Enthält das Dokument '' genau ^^ Seite(n)?")
	public void doesDocumentContainExcactNumberOfPages(File document, int expectedPageNumber) 
	{
		final PdfPageContentAnalyser pdfAnalyser = new PdfPageContentAnalyser(document.getAbsolutePath());
		final boolean ok = expectedPageNumber == pdfAnalyser.getPageNumber();
		final String question = "Enhält das Dokument <b>" + document.getName() + "</b> genau <b>" + expectedPageNumber + "</b> Seite(n)" + QUESTION_IDENTIFIER;
		executableExample.answerQuestion(question, ok);	
	}

	@LanguageTemplate("Schließe das PDF-Fenster ^^.")
	public void closePDFWindow(String windowTitle)
	{
		executableExample.sleep(1000); // give system time 
		
		if ( ! doesWindowWithTitleExists(windowTitle) ) {
			System.err.println("Warning: Window with title" + windowTitle + " not found.");
			return;
		};
		
		executableExample.getGuiController().switchToLastWindow();		
		((WebDriver)executableExample.getGuiController().getWebDriver()).close();
		executableExample.getGuiController().switchToFirstWindow();		
 
		executableExample.sleep(1000); // give system time 
	}

	private boolean doesWindowWithTitleExists(String windowTitlePart) 
	{
		final List<String> result = executableExample.executeCommandAndListOutput("tasklist /v");
		result.forEach(System.out::println);
		final String infoLine = result.stream().filter(line->line.contains(windowTitlePart)).findFirst().orElse("");
		return infoLine.length() > 0;
	}
	
}