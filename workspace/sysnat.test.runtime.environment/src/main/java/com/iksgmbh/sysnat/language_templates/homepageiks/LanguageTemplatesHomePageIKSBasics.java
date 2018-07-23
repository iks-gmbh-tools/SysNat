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
import com.iksgmbh.sysnat.common.utils.SysNatConstants.StartParameter;
import com.iksgmbh.sysnat.language_templates.LanguageTemplates;
import com.iksgmbh.sysnat.language_templates.homepageiks.pageobject.ZeiterfassungPageObject;

/**
 * Contains the basic language templates for IKS-Online tests.
 * 
 * Note: Java interitance is not possible to use with NatSpec
 *       because SyntaxPattern methods of parent LanguageTemplates classes 
 *       are NOT available in NatSpec files.
 */
@LanguageTemplateContainer
public class LanguageTemplatesHomePageIKSBasics implements LanguageTemplates
{	
	private ExecutableExample testCase;
	@SuppressWarnings("unused")
	private ExecutionRuntimeInfo executionInfo;
	private ZeiterfassungPageObject zeitErfassungPageObject;
	
	public LanguageTemplatesHomePageIKSBasics(ExecutableExample aTestCase) 
	{
		this.testCase = aTestCase;
		this.executionInfo = ExecutionRuntimeInfo.getInstance();
		this.zeitErfassungPageObject = new ZeiterfassungPageObject(aTestCase);
	}

	private String getPageName() 
	{
		int maxTries = 60;
		int tryCounter = 0;
		
		while (true) {			
			try {
				return testCase.getTextForId("menuOben");
			} catch (Exception e) {
				if (tryCounter == maxTries) {
					throw e;
				}
				tryCounter++;
				testCase.sleep(100);
			}
		}
	}
	
	//##########################################################################################
	//                       I N T E R F A C E    M E T H O D S
	//##########################################################################################
	
	@Override
	public void doLogin(final HashMap<StartParameter,String> startParameter) {
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
			return testCase.getTextForId("//div[@class='content']//h1").equals("Projekte. Beratung. Spezialisten.");
		} catch (Exception e) {
			return false;
		}
	}


	@Override
	public void gotoStartPage() {
		if ( ! isStartPageVisible() ) {
			testCase.clickLink("Home");
		}
	}

	
	//##########################################################################################
	//                   L A N G U A G E   T E M P L A T E    M E T H O D S
	//##########################################################################################

	
	@LanguageTemplate(value = "Wird die Seite ^^ angezeigt?")
	public void isPageVisible(String expectedPage) 
	{
		boolean ok = true;
		testCase.sleep(1500);
		
		if (expectedPage.equals("Home"))  {
			ok = isStartPageVisible();
		} else if (expectedPage.equals("Downloads"))  {
			ok = testCase.getTextForId("//div[@class='downloads-title']//h1").equals("Vorträge");
		} else if (expectedPage.equals("Dienstleistungen"))  {
			ok = testCase.getTextForId("//div[@class='content']//h1").equals("Wir erstellen individuelle Softwarelösungen");
		} else if (expectedPage.equals("Unternehmen"))  {
			ok = testCase.getTextForId("//div[@class='content']//h1").equals("Wir entwickeln individuelle Softwarelösungen");
		} else if (expectedPage.equals("Karriere"))  {
			ok = testCase.getTextForId("//div[@class='content']//h2").equals("Was macht IKS?");
		} else if (expectedPage.equals("Kontakt"))  {
			ok = testCase.getTextForId("//div[@class='content']//h1").equals("Schreiben Sie uns, wir freuen uns auf Ihre Nachricht!");
		} else {
			testCase.failWithMessage("Unbekannte Seite <b>" + expectedPage + "</b>.");
		}
		
		String question = "Wurde die Seite <b>" + expectedPage + "</b> angezeigt" + QUESTION_IDENTIFIER;
		testCase.answerQuestion(question, ok);
	}
	
	@LanguageTemplate(value = "Klicke Hauptmenüpunkt ^^.")
	public void clickMainMenuItem(String menuItemText) {
		testCase.clickLink(menuItemText);
		testCase.addReportMessage("Der Hauptmenüpunkt <b>" + menuItemText + "</b> wurde geklickt.");
	}

	@LanguageTemplate(value = "Klicke die Schaltfläche ^^.")
	public void clickButton(String buttonName) 
	{
		boolean ok = true;
		String pageName = getPageName();
		
		if ("Zeiterfassung".equals(pageName)) {
			zeitErfassungPageObject.clickButton(buttonName);
		} else {
			ok = false;
		}
		
		if (ok) {
			testCase.addReportMessage("Button <b>" + buttonName + "</b> wurde geklickt.");
		} else {
			testCase.failWithMessage("Die aktuelle Datenmaske (siehe Screenshot) ist unbekannt.");
		}
	}

	@LanguageTemplate(value = "Download PDF ^^ als <>.")
	public File downloadPDF(String pdfFilename)
	{
		return null;
	}

	@LanguageTemplate(value = "Enthält '' auf Seite ^^ den Text ^^?")
	public void checkPdfContent(final File pdfFile, 
			                    final String pageNumber, 
			                    final String expectedText)
	{
	    // to be implemented
	}
	
	@LanguageTemplate("Klicke Link ^^.")
	public void clickLink(String linkText) 
	{
		if (linkText.equals("IKS Software zum Anfassen Gibt es so etwas")) {
			testCase.clickLink("//a[@href='/assets/downloads/IKS-Software-zum-Anfassen-Gibt-es-so-etwas.pdf']");
		} else {			
			testCase.clickLink(linkText);
		}
		testCase.addReportMessage("Der Link <b>" + linkText + "</b> wurde geklickt.");
	}
	
	@LanguageTemplate("Das PDF wird als Dokument <> gespeichert.")
	public File savePDF() 
	{
		List<File> findDownloadFiles1 = SysNatFileUtil.findDownloadFiles("PDF").getFiles();
		testCase.downloadPdf();
		List<File> findDownloadFiles2 = SysNatFileUtil.findDownloadFiles("PDF").getFiles();
		findDownloadFiles2.removeAll(findDownloadFiles1); 
		return findDownloadFiles2.get(0);
	}

}
