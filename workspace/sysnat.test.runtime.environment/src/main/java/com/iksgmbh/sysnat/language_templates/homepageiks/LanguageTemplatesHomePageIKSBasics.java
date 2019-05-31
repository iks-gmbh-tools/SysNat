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
package com.iksgmbh.sysnat.language_templates.homepageiks;

import static com.iksgmbh.sysnat.common.utils.SysNatConstants.QUESTION_IDENTIFIER;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.WebLoginParameter;
import com.iksgmbh.sysnat.language_templates.LanguageTemplates;

/**
 * Contains the basic language templates for IKS-Online tests.
 * 
 * @author Reik Oberrath
 */
@LanguageTemplateContainer
public class LanguageTemplatesHomePageIKSBasics implements LanguageTemplates
{	
	private ExecutableExample executableExample;
	@SuppressWarnings("unused")
	private ExecutionRuntimeInfo executionInfo;
	
	public LanguageTemplatesHomePageIKSBasics(ExecutableExample aExecutableExample) 
	{
		this.executableExample = aExecutableExample;
		this.executionInfo = ExecutionRuntimeInfo.getInstance();
	}

	@SuppressWarnings("unused")
	private String getPageName() 
	{
		int maxTries = 60;
		int tryCounter = 0;
		
		while (true) {			
			try {
				return executableExample.getTextForId("menuOben");
			} catch (Exception e) {
				if (tryCounter == maxTries) {
					throw e;
				}
				tryCounter++;
				executableExample.sleep(100);
			}
		}
	}
	
	//##########################################################################################
	//                       I N T E R F A C E    M E T H O D S
	//##########################################################################################
	
	@Override
	public void doLogin(final HashMap<WebLoginParameter,String> startParameter) {
		// no login/logout for IKS Homepage
	}

	@Override
    public void doLogout() {
		// no login/logout for IKS Homepage
    }
    
	@Override
	public boolean isLoginPageVisible() {
		return false; // no login for IKS Homepage
	}

	@Override
	public boolean isStartPageVisible() {
		try {
			return executableExample.getTextForId("//div[@class='content']//h1").equals("Projekte. Beratung. Spezialisten.");
		} catch (Exception e) {
			return false;
		}
	}


	@Override
	public void gotoStartPage() {
		if ( ! isStartPageVisible() ) {
			executableExample.clickLink("Home");
		}
	}

	
	//##########################################################################################
	//                   L A N G U A G E   T E M P L A T E    M E T H O D S
	//##########################################################################################

	
	@LanguageTemplate(value = "Is page ^^ displayed?")
	@LanguageTemplate(value = "Wird die Seite ^^ angezeigt?")
	public void isPageVisible(String expectedPage) 
	{
		boolean ok = true;
		executableExample.sleep(1500);
		
		if (expectedPage.equals("Home"))  {
			ok = isStartPageVisible();
		} else if (expectedPage.equals("Downloads"))  {
			ok = executableExample.getTextForId("//div[@class='downloads-title']//h1").equals("Vorträge");
		} else if (expectedPage.equals("Dienstleistungen"))  {
			ok = executableExample.getTextForId("//div[@class='content']//h1").equals("Wir erstellen individuelle Softwarelösungen");
		} else if (expectedPage.equals("Unternehmen"))  {
			ok = executableExample.getTextForId("//div[@class='content']//h1").equals("Wir entwickeln individuelle Softwarelösungen");
		} else if (expectedPage.equals("Karriere"))  {
			ok = executableExample.getTextForId("//div[@class='content']//h2").equals("Was macht IKS?");
		} else if (expectedPage.equals("Kontakt"))  {
			ok = executableExample.getTextForId("//div[@class='content']//h1").equals("Schreiben Sie uns, wir freuen uns auf Ihre Nachricht!");
		} else {
			executableExample.failWithMessage("Unbekannte Seite <b>" + expectedPage + "</b>.");
		}
		
		String question = "Is page <b>" + expectedPage + "</b> displayed" + QUESTION_IDENTIFIER;
		//String question = "Wurde die Seite <b>" + expectedPage + "</b> angezeigt" + QUESTION_IDENTIFIER;
		executableExample.answerQuestion(question, ok);
	}
	
	@LanguageTemplate(value = "Klicke Hauptmenüpunkt ^^.")
	@LanguageTemplate(value = "Click main menu item ^^.")
	public void clickMainMenuItem(String menuItemText) {
		executableExample.clickLink(menuItemText);
		//executableExample.addReportMessage("Der Hauptmenüpunkt <b>" + menuItemText + "</b> wurde geklickt.");
		executableExample.addReportMessage("Main menu item <b>" + menuItemText + "</b> has been clicked.");
	}

	
	@LanguageTemplate("Click link ^^.")
	@LanguageTemplate("Klicke Link ^^.")
	public void clickLink(String linkText) 
	{
		if (linkText.equals("IKS Software zum Anfassen Gibt es so etwas")) {
			executableExample.clickLink("//a[@href='/assets/downloads/IKS-Software-zum-Anfassen-Gibt-es-so-etwas.pdf']");
		} else {			
			executableExample.clickLink(linkText);
		}
		executableExample.addReportMessage("Link <b>" + linkText + "</b> has been clicked.");
		//executableExample.addReportMessage("Der Link <b>" + linkText + "</b> wurde geklickt.");
	}
	
	@LanguageTemplate("Das PDF wird als Dokument <> gespeichert.")
	@LanguageTemplate("PDF <> saved.")
	public File savePDF() 
	{
		List<File> findDownloadFiles1 = SysNatFileUtil.findDownloadFiles("PDF").getFiles();
		executableExample.downloadPdf();
		List<File> findDownloadFiles2 = SysNatFileUtil.findDownloadFiles("PDF").getFiles();
		findDownloadFiles2.removeAll(findDownloadFiles1); 
		return findDownloadFiles2.get(0);
	}

}