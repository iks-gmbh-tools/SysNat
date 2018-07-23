package com.iksgmbh.sysnat.language_templates.icf;

import static com.iksgmbh.sysnat.common.utils.SysNatConstants.QUESTION_IDENTIFIER;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.openqa.selenium.WebDriver;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;
import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.common.helper.PdfComparer;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.StartParameter;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.domain.SysNatTestData.SysNatDataset;
import com.iksgmbh.sysnat.guicontrol.SeleniumGuiController;
import com.iksgmbh.sysnat.language_templates.LanguageTemplates;
import com.iksgmbh.sysnat.language_templates.icf.pageobject.AlertPopup_PageObject;
import com.iksgmbh.sysnat.language_templates.icf.pageobject.ProductSelectionPage_PageObject;
import com.iksgmbh.sysnat.language_templates.icf.pageobject.ResultPageObject;
import com.iksgmbh.sysnat.language_templates.icf.pageobject.WizardPage1_PageObject;
import com.iksgmbh.sysnat.language_templates.icf.pageobject.WizardPage2_PageObject;
import com.iksgmbh.sysnat.language_templates.icf.pageobject.WizardPage3_PageObject;
import com.iksgmbh.sysnat.language_templates.icf.pageobject.WizardPage4_PageObject;
import com.iksgmbh.sysnat.language_templates.icf.pageobject.WizardPage5_PageObject;
import com.iksgmbh.sysnat.language_templates.icf.pageobject.WizardPage6_PageObject;
import com.iksgmbh.sysnat.utils.SysNatUtil;

/**
 * Contains the basic language templates for IKS-Online tests.
 * 
 * Note: Java interitance is not possible to use with NatSpec
 *       because SyntaxPattern methods of parent LanguageTemplates classes 
 *       are NOT available in NatSpec files.
 */
@LanguageTemplateContainer
public class LanguageTemplatesICFBasics implements LanguageTemplates
{	
	private ExecutableExample testCase;
	private ExecutionRuntimeInfo executionInfo;
	
	private ResultPageObject resultPageObject;
	private ProductSelectionPage_PageObject productSelectionPage;
	private AlertPopup_PageObject alertPopup;
	
	private WizardPage1_PageObject wizardPage1;
	private WizardPage2_PageObject wizardPage2;
	private WizardPage3_PageObject wizardPage3;
	private WizardPage4_PageObject wizardPage4;
	private WizardPage5_PageObject wizardPage5;
	private WizardPage6_PageObject wizardPage6;
	
	public LanguageTemplatesICFBasics(ExecutableExample aTestCase) 
	{
		this.testCase = aTestCase;
		this.executionInfo = ExecutionRuntimeInfo.getInstance();
		
		final WebDriver webDriver = ((SeleniumGuiController)testCase.getGuiController()).getWebDriver();
		webDriver.switchTo().defaultContent(); // you are now outside both frames
		webDriver.switchTo().frame("pbvIFrame");
		
		productSelectionPage = new ProductSelectionPage_PageObject(testCase);
		alertPopup = new AlertPopup_PageObject(testCase);
		resultPageObject = new ResultPageObject(aTestCase);
		
		wizardPage1 = new WizardPage1_PageObject(testCase);
		wizardPage2 = new WizardPage2_PageObject(testCase);
		wizardPage3 = new WizardPage3_PageObject(testCase);
		wizardPage4 = new WizardPage4_PageObject(testCase);
		wizardPage5 = new WizardPage5_PageObject(testCase);
		wizardPage6 = new WizardPage6_PageObject(testCase);
	}

	private String getNameOfCurrentlyDisplayedPage() 
	{
		if (productSelectionPage.isCurrentlyDisplayed()) {
			return productSelectionPage.getPageName();
		}
		if (wizardPage1.isCurrentlyDisplayed()) {
			return wizardPage1.getPageName();
		}
		if (wizardPage2.isCurrentlyDisplayed()) {
			return wizardPage2.getPageName();
		}
		if (wizardPage3.isCurrentlyDisplayed()) {
			return wizardPage3.getPageName();
		}
		if (wizardPage4.isCurrentlyDisplayed()) {	
			return wizardPage4.getPageName();
		}
		if (wizardPage5.isCurrentlyDisplayed()) {
			return wizardPage5.getPageName();
		}
		if (wizardPage6.isCurrentlyDisplayed()) {
			return wizardPage6.getPageName();
		}

		String filename = "ErrorUnsupportedPage.png";
		testCase.takeScreenshot(filename);
		throw new SysNatException("Currently displayed page is not (yet) supported by NatSpecTesting! See screenshot <b>" + filename + "</b>.");
	}
	
	//##########################################################################################
	//                       I N T E R F A C E    M E T H O D S
	//##########################################################################################
	
	@Override
	public void doLogin(HashMap<StartParameter,String> startParameter) {
		// not needed for ICF
	}

	@Override
    public void doLogout() {
		// not needed for ICF
    }
    
	@Override
	public boolean isLoginPageVisible() {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public boolean isStartPageVisible() 
	{
		throw new UnsupportedOperationException("Not implemented yet.");
	}


	@Override
	public void gotoStartPage() 
	{
		try {
//			if (testCase.isElementReadyToUse("closeDialogButton")) {
//				// close dialog that may have been opened but not closed by the previous test
//				testCase.clickButton("closeDialogButton");  
//			}
			if ( ! isStartPageVisible() )  {
				//clickMainMenuItem("Form Page"); // goto to Standard Start position for all test cases
			}
		} catch (Exception e) {
			// ignore
		}
	}

	
	//##########################################################################################
	//                       T E X T  S Y N T A X    M E T H O D S
	//##########################################################################################

	
	@LanguageTemplate("Klicke Link ^^.")
	public void clickLink(String linkText) {
		testCase.clickLink(linkText);
	}

	@LanguageTemplate("Gebe den Betrag ^^ ein.")
	public void enterAmount(String value) {
		testCase.inputText("request.produktbeitrag", value);
	}

	@LanguageTemplate("Klicke die Schaltfläche ^^.")
	public void clickButton(String buttonName) 
	{
		if (alertPopup.isCurrentlyDisplayed()) {
			alertPopup.clickButton(buttonName);
		} else if (productSelectionPage.isCurrentlyDisplayed()) {
			productSelectionPage.clickButton(buttonName);
		} else if (wizardPage1.isCurrentlyDisplayed()) {
			wizardPage1.clickButton(buttonName);
		} else if (wizardPage2.isCurrentlyDisplayed()) {
				wizardPage2.clickButton(buttonName);
		} else if (wizardPage3.isCurrentlyDisplayed()) {
			wizardPage3.clickButton(buttonName);
		} else if (wizardPage4.isCurrentlyDisplayed()) {
			wizardPage4.clickButton(buttonName);
		} else if (wizardPage5.isCurrentlyDisplayed()) {
			wizardPage5.clickButton(buttonName);
		} else if (wizardPage6.isCurrentlyDisplayed()) {
			wizardPage6.clickButton(buttonName);
		} else {
			throw new SysNatException("Buttons not supported on page " + getNameOfCurrentlyDisplayedPage() + ".");
		}

		testCase.addReportMessage("Die Schaltfläche <b>" + buttonName + "</b> wurde geklickt.");		
	}

	@LanguageTemplate("Ist auf der aktuellen Seite der Text ^^ zu sehen?")
	public void istAufDerAktuellenSeiteDerTextZuSehen(String text) 
	{
		resultPageObject.isCurrentlyDisplayed();
		boolean ok = testCase.isTextCurrentlyDisplayed(text);
				
		String question = "War der Text <b>" + text + "</b> auf der aktuellen Seite zu sehen" + QUESTION_IDENTIFIER;
		testCase.answerQuestion(question, ok);
	}
	
	@LanguageTemplate("Wird ein Popup angezeigt, der den Text ^^ enthält?")
	public void isPopupVisible(String text) 
	{
		boolean ok = testCase.isTextCurrentlyDisplayed(text);
		
		if (text.equals("zu jung")) {
			System.out.println("");
		}
		
		String question = "War der Text <b>" + text + "</b> auf der aktuellen Seite zu sehen" + QUESTION_IDENTIFIER;
		testCase.answerQuestion(question, ok);
	}
	

	@LanguageTemplate("Wähle für die Auswahlbox ^^ die Option ^^.")
	public void chooseOptionForSelectionbox(String auswahlbox, String option) 
	{
		if (productSelectionPage.isCurrentlyDisplayed()) {
			productSelectionPage.chooseForCombobox(auswahlbox, option);
		} else if (wizardPage5.isCurrentlyDisplayed()) {
			wizardPage5.chooseForCombobox(auswahlbox, option);
		} else {			
			throw new SysNatException("Selection boxes not supported on page " + getNameOfCurrentlyDisplayedPage() + ".");		
		}
		
		testCase.addReportMessage("In der Auswahlbox <b>" + auswahlbox + "</b> wurde die Option " + option + " ausgwählt.");		
	}

	@LanguageTemplate("Gebe ins Feld ^^ den Wert ^^ ein.")
	public void enterText(String fieldName, String value)
	{
		if (wizardPage2.isCurrentlyDisplayed()) {
			wizardPage2.enterTextInField(fieldName, value);
		} else if (wizardPage5.isCurrentlyDisplayed()) {
				wizardPage5.enterTextInField(fieldName, value);
		} else {			
			throw new SysNatException("Textfields not supported on page " + getNameOfCurrentlyDisplayedPage() + ".");		
		}

		testCase.addReportMessage("Ins Textfeld <b>" + fieldName + "</b> wurde der Text <b>" + value + "</b> eingegeben.");	
	}

	@LanguageTemplate("Schließe das PDF-Fenster für ^^.")
	public void schliesseFensterPDF(String name) {
		closePDFWindow(name);
	}

	@LanguageTemplate("Wähle für die ^^ die Option ^^.")
	public void chooseRadioButtons(String selection, String option) 
	{
		if (wizardPage2.isCurrentlyDisplayed()) {
			wizardPage2.chooseRadioButtons(selection, option);
		} else if (wizardPage6.isCurrentlyDisplayed()) {
				wizardPage6.chooseRadioButtons(selection, option);
		} else {			
			throw new SysNatException("RadioButtons not supported on page " + getNameOfCurrentlyDisplayedPage() + ".");		
		}
		
		testCase.addReportMessage("Für der Auswahl <b>" + selection + "</b> wurde die Option <b>" + option + "</b> ausgewählt.");		
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

	@LanguageTemplate("Entspricht der Inhalt des Dokuments '' unter Berücksichtung von ^^ fachlich dem von ^^?")
	public void doesDocumentMatchFromBusinessPointOfView(File document, String allowedDifferencesType, String expectedPDF) 
	{
		
		boolean ok = false;
		try {
			final String expectedPDFFile = executionInfo.getTestdataDir() + "/" + expectedPDF;
			final PdfComparer pdfAnalyser = new PdfComparer(document.getAbsolutePath());
			final SysNatDataset allowedDifferences = testCase.getTestData().getDataSet(allowedDifferencesType);
			final List<String> linePrefixesToIgnore = SysNatUtil.buildAllowedDifferencesList(allowedDifferences);
			final String differenceReport = pdfAnalyser.getBusinessDifferenceReport(expectedPDFFile, linePrefixesToIgnore);
			ok = differenceReport.isEmpty();
			
			if (! ok) {
				testCase.addLinewiseToReportMessageAsComment(differenceReport);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new SysNatTestDataException("Fehler beim Lesen von " + expectedPDF + " oder " + document.getAbsolutePath());
		}
		
		String question = "Entspricht der Inhalt des Dokuments <b>" + document.getName() + "</b> unter Berücksichtigung von <b>" +
				           allowedDifferencesType + "</b> fachlich dem von <b>" + expectedPDF + "</b>" + QUESTION_IDENTIFIER;
		testCase.answerQuestion(question, ok);	
	}
	
	@LanguageTemplate("Ist der Inhalt des Dokuments '' mit dem von ^^ identisch?")
	public void doesDocumentMatchExactly(File document, String expectedPDF) 
	{
		PdfComparer pdfAnalyser = new PdfComparer(document.getAbsolutePath());
		
		boolean ok = false;
		try {
			String differenceReport = pdfAnalyser.getFullDifferenceReport(executionInfo.getTestdataDir() + "/" + expectedPDF);
			ok = differenceReport.isEmpty();
			if (! ok) {
				testCase.addLinewiseToReportMessageAsComment(differenceReport);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new SysNatTestDataException("Fehler beim Lesen von " + expectedPDF + " oder " + document.getAbsolutePath());
		}
		
		String question = "Ist der Inhalt des Dokuments <b>" + document.getName() + "</b> mit dem von "
				           + "<b>" + expectedPDF + "</b> identisch" + QUESTION_IDENTIFIER;
		testCase.answerQuestion(question, ok);	
	}
	
	public boolean closePDFWindow(String windowTitle)
	{
		testCase.sleep(1000); // give system time 
		
		if ( ! doesWindowWithTitleExists(windowTitle) ) {
			return false;
		};
		
		testCase.getGuiController().switchToLastWindow();		
		((WebDriver)testCase.getGuiController().getWebDriver()).close();
		testCase.getGuiController().switchToFirstWindow();		
 
		testCase.sleep(1000); // give system time 
		return true;
	}

	private boolean doesWindowWithTitleExists(String windowTitlePart) 
	{
		final List<String> result = testCase.executeCommandAndListOutput("tasklist /v");
		//result.forEach(System.out::println);
		final String infoLine = result.stream().filter(line->line.contains("pdf-icf-") && line.contains("-"+ windowTitlePart)).findFirst().orElse("");
		return infoLine.length() > 0;
	}
	
	static class WindowInfo 
	{
		String applicationName;
		String pid;
		String title;
		
		public WindowInfo(String applicationName, String pid, String title) {
			this.applicationName = applicationName;
			this.pid = pid;
			this.title = title;
		}

		@Override
		public String toString() {
			return "WindowInfo [applicationName=" + applicationName + ", pid=" + pid + ", title=" + title + "]";
		}
	}

	@LanguageTemplate("Wird die Ergebnisseite angezeigt?")
	public void wirdDieErgebnisseiteAngezeigt() 
	{
		while (testCase.isTextCurrentlyDisplayed("Vielen Dank.")) {
			testCase.sleep(500);
		}
		
		boolean ok = resultPageObject.isCurrentlyDisplayed();
		String question = "Wird die Ergebnisseite angezeigt" + QUESTION_IDENTIFIER;
		testCase.answerQuestion(question, ok);	
	}
	
	@LanguageTemplate("Entsprechen die angezeigten Daten unter Berücksichtung von ^^ fachlich dem Inhalt der Datei ^^?")
	public void areResultDataOk(String allowedDifferencesType, String expectedFilename) 
	{
		final SysNatDataset allowedDifferences = testCase.getTestData().getDataSet(allowedDifferencesType);
		final List<String> linePrefixesToIgnore = SysNatUtil.buildAllowedDifferencesList(allowedDifferences);
		
		final String text = resultPageObject.getText("Textarea").replaceAll(System.getProperty("line.separator"), "\n");
		List<String> actualResult = Arrays.asList( text.split("\\n") );
		actualResult = SysNatUtil.removeLinesToBeIgnoreByPrefix(actualResult, linePrefixesToIgnore);
		
		final String testdataDir = System.getProperty("sysnat.testdata.import.directory") + "/" + executionInfo.getTestApplicationName();
		List<String> expectedResult = SysNatFileUtil.readTextFile( testdataDir + "/" + expectedFilename);
		expectedResult = SysNatUtil.removeLinesToBeIgnoreByPrefix(expectedResult, linePrefixesToIgnore);
		
		
		final List<String> differences = new ArrayList<>();
		
		if (actualResult.size() == expectedResult.size()) {
			for (int i = 0; i < expectedResult.size(); i++) {
				String line = expectedResult.get(i);
				if (! line.equals(actualResult.get(i))) {
					if (differences.isEmpty()) {
						differences.add("Unterschiede der XMLs:");
					}
					differences.add(line + " # " + actualResult.get(i));
				}
			}
		} else {
			differences.add("Unterschiede zwischen den XMLs:");
			differences.add("Erwartetes XML (" + expectedResult.size() + ") hat andere Zeilenzahl "
					        + "als das aktuelle XML (" + actualResult.size() + ")!");
		}

		boolean ok = differences.isEmpty();
		
		if (! ok) {
			testCase.addLinewiseToReportMessageAsComment(differences);
		}

		String question = "Entsprechen die angezeigten Daten unter Berücksichtigung von <b>" + allowedDifferencesType 
				         + "</b> den erwarteten Daten aus Datei <b>" + expectedFilename + "</b>" + QUESTION_IDENTIFIER;
		testCase.answerQuestion(question, ok);	
	}

}
