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

import java.io.File;
import java.util.List;

import org.openqa.selenium.WebDriver;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;
import com.iksgmbh.sysnat.common.domain.FileList;
import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

/**
 * Contains the implementation of language templates for the purpose of handling file downloads.
 * If needed, they can be used in all test applications. 
 */
@LanguageTemplateContainer
public class LanguageTemplatesDownload 
{
	public static final String NO_PDF_MESSAGE = "Problem: Es wurde keine PDF-Datei erzeugt!";

	private ExecutableExample executableExample;
	
	public LanguageTemplatesDownload(ExecutableExample aExecutableExample) {
		executableExample = aExecutableExample;
	}
	
	// ###########################################################################
	//            L a n g u a g e   T e m p l a t e   M e t h o d s
	// ###########################################################################
	
	@LanguageTemplate("List of PDF files in Download directory ^^ is saved as <>.")
	@LanguageTemplate("Die Liste der PDF-Dateien im Download-Verzeichnis ^^ als <> festgehalten.")
	public FileList getCurrentPdfFileList(String fileListName) 
	{
		FileList toReturn = SysNatFileUtil.findDownloadFiles("pdf");
		toReturn.setName(fileListName);
    	        
   		executableExample.addReportMessage("Es wurde die Liste der PDF-Dateien als <b>" + fileListName + "</b> festgehalten. "
   				                  + "Diese Liste umfasst <b>" + toReturn.size() + "</b> Dateien.");
   		return toReturn;
	}

	@LanguageTemplate("Is the current number of PDF file in the Download directory by ^^ larger than in ^^?")
	@LanguageTemplate("Ist die aktuelle Zahl der PDF-Dateien im Download-Verzeichnis um ^^ höher als in der ^^?")
	public void compareFileList(int number, FileList oldPdfFiles) 
	{
		FileList currentFileList = SysNatFileUtil.findDownloadFiles("pdf");
		boolean ok = oldPdfFiles.size() + number == currentFileList.size(); 
		String question = "Ist die aktuelle Zahl der PDF-Dateien im Download-Verzeichnis (" + currentFileList.size() + ") um "
				           + "<b>" + number + "</b> höher als in der <b>" + oldPdfFiles.getName() + "</b>? - ";
		executableExample.answerQuestion(question, ok);
	}

	@LanguageTemplate("The downloaded PDF is saved as <>.")
	@LanguageTemplate("Das heruntergeladene PDF wird als <> festgehalten.")
	public File storeLastPdfFile() 
	{
		File latestPdfFile = SysNatFileUtil.findRecentDownloadFile(2000);
		if (latestPdfFile == null) {
			throw new SysNatException("No PDF found that was recently downloaded.");
		}
		return latestPdfFile;
	}
	
	@LanguageTemplate("Close PDF window ^^.")
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

	// ###########################################################################
	//                    P r i v a t e   M e t h o d s
	// ###########################################################################
	
	private boolean doesWindowWithTitleExists(String windowTitlePart) 
	{
		final List<String> result = executableExample.executeCommandAndListOutput("tasklist /v");
		result.forEach(System.out::println);
		final String infoLine = result.stream().filter(line->line.contains(windowTitlePart)).findFirst().orElse("");
		return infoLine.length() > 0;
	}
	
}