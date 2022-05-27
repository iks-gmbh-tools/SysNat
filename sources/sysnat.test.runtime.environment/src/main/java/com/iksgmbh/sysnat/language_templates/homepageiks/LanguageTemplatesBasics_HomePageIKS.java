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
import java.util.List;
import java.util.Map;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.ApplicationLoginParameter;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.language_templates.LanguageTemplateBasics;

/**
 * Contains the basic language templates for test application HomePageIKS.
 * 
 * It demonstrates both the implementation without PageObjects 
 * and the testing of an application without login.
 * 
 * Not using Page Objects is a bad idea, because its consequences are if-chains 
 * (see method isPageVisible).
 * 
 * @author Reik Oberrath
 */
@LanguageTemplateContainer
public class LanguageTemplatesBasics_HomePageIKS extends LanguageTemplateBasics
{	
	private ExecutableExample executableExample;
	@SuppressWarnings("unused")
	private ExecutionRuntimeInfo executionInfo;
	
	public LanguageTemplatesBasics_HomePageIKS(ExecutableExample aExecutableExample) 
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
	public void doLogin(final Map<ApplicationLoginParameter,String> startParameter) {
		// no login/logout for this test application
	}

	@Override
    public void doLogout() {
		// no login/logout for this test application
    }
    
	@Override
	public boolean isLoginPageVisible() {
		return false; // no login for this test application
	}

	@Override
	public boolean isStartPageVisible() {
		try {
			return executableExample.getTextForId("//div[@class='text hasImage  float_right']//p").startsWith("Wir stellen unser Wissen in Ihren Dienst.");
		} catch (Exception e) {
			return false;
		}
	}


	@Override
	public void gotoStartPage() {
		if ( ! isStartPageVisible() ) {
			executableExample.clickLink("Startseite");
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
		executableExample.sleep(1000);
		String xpath1 = "//h1[@class='ce_headline headline']";
		String xpath2 = "//div[@class='text']//h3";
		String xpath3 = "//div[@class='ce_form last tableless block']//h2";
		String xpath4 = "//div[@class='mod_breadcrumb breadcrumb block']//ul//li//span";
		String xpath5 = "//h1[@class='headline  float_right']";
		String xpath6 = "//h1[@class='ce_headline first last headline_italic  headline']";
		String xpath7 = "//h1[@class='ce_headline headline_italic  headline']";
		
		
		if (expectedPage.equals("Startseite"))  {
			ok = isStartPageVisible();
		} else if (expectedPage.equals("Unternehmen"))  {
			ok = executableExample.getTextForId(xpath2).equals("Mit Erfahrung, Mut und Begeisterung geben wir Ideen eine digitale Zukunft.");
		} else if (expectedPage.equals("Leistungen"))  {
			ok = executableExample.getTextForId(xpath5).equals("Maßgeschneiderte Lösungen");
		} else if (expectedPage.equals("Referenzen"))  {
			ok = executableExample.getTextForId(xpath6).equals("Jede Idee ist nur so gut, wie ihre Umsetzung");
		} else if (expectedPage.equals("Karriere"))  {
			ok = executableExample.getTextForId(xpath7).equals("Spezialisten mit Persönlichkeit");
		} else if (expectedPage.equals("Kontakt"))  {
			ok = executableExample.getTextForId(xpath3).equals("Schreiben Sie uns, wir freuen uns auf Ihre Nachricht!");
		} else if (expectedPage.equals("Blog"))  {
			ok = executableExample.getTextForId(xpath1).equals("Blog und Veröffentlichungen");
		} else if (expectedPage.equals("Fachartikel"))  {
			ok = executableExample.getTextForId(xpath4).equals("FACHARTIKEL");
		} else if (expectedPage.equals("News"))  {
			ok = executableExample.getTextForId(xpath1).equals("News und Terminübersicht");
		} else {
			executableExample.failWithMessage("Unbekannte Seite <b>" + expectedPage + "</b>.");
		}
		
		String question = "Is page <b>" + expectedPage + "</b> displayed" + QUESTION_IDENTIFIER;
		//String question = "Wurde die Seite <b>" + expectedPage + "</b> angezeigt" + QUESTION_IDENTIFIER;
		executableExample.answerQuestion(question, ok);
	}
	
	@LanguageTemplate(value = "Klicke Hauptmenüpunkt ^^.")
	@LanguageTemplate(value = "Click main menu item ^^.")
	public void clickMainMenuItem(String menuItemText) 
	{
		String technicalTitle = menuItemText;
		if (menuItemText.equals("Blog")) {
			technicalTitle = "Blog und Veröffentlichungen";
		}
		else if (menuItemText.equals("News")) {
			technicalTitle = "News und Terminübersicht";
		}
		else if (menuItemText.equals("Startseite")) {
			technicalTitle = "Individuelle Softwareentwicklung und IT-Beratung";
		}
		
		executableExample.clickLink("//a[@title='" + technicalTitle + "']", 2);
		//executableExample.addReportMessage("Der Hauptmenüpunkt <b>" + menuItemText + "</b> wurde geklickt.");
		executableExample.addReportMessage("Main menu item <b>" + menuItemText + "</b> has been clicked.");
	}

	
	@LanguageTemplate("Click link ^^.")
	@LanguageTemplate("Klicke Link ^^.")
	public void clickLink(String linkText) 
	{
		if (linkText.equals("Download Softwarequalität-zum-Anfassen.pdf")) {
			executableExample.clickLink("//li[@class='item ext-pdf']", "//div[@class='ce_text block']");
		} else if (linkText.equals("MainMenuSymbol")) {
				executableExample.clickLink("//div[@class='menu_hamburger']");
		} else if (linkText.equals("Softwarequalität zum Anfassen")) {
			executableExample.clickElement("//*[contains(text(), 'Softwarequalität zum Anfassen')]");
		} else if (linkText.equals("IKS-Fachartikel-Software-zum-Anfassen-Gibt-es-so-etwas.pdf")) {
			executableExample.clickElement("//*[contains(text(), 'IKS-Fachartikel-Software-zum-Anfassen-Gibt-es-so-etwas')]");
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
	
	@LanguageTemplate(value = "Open main menu.")
	@LanguageTemplate(value = "Öffne Hauptmenü.")
	public void openMainMenu() {
		clickLink("MainMenuSymbol");
	}

}