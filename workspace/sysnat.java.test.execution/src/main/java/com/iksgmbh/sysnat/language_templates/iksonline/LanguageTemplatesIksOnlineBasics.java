package com.iksgmbh.sysnat.language_templates.iksonline;

import static com.iksgmbh.sysnat.utils.SysNatConstants.QUESTION_IDENTIFIER;
import static com.iksgmbh.sysnat.utils.SysNatStringUtil.replaceEmptyStringSymbol;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.iksgmbh.sysnat.ExecutionInfo;
import com.iksgmbh.sysnat.TestCase;
import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.domain.BigDecimalValue;
import com.iksgmbh.sysnat.domain.SysNatTestData.ObjectData;
import com.iksgmbh.sysnat.exception.UnsupportedGuiEventException;
import com.iksgmbh.sysnat.language_templates.LanguageTemplates;
import com.iksgmbh.sysnat.language_templates.iksonline.pageobject.ZeiterfassungPageObject;

/**
 * Contains the basic language templates for IKS-Online tests.
 * 
 * Note: Java interitance is not possible to use with NatSpec
 *       because SyntaxPattern methods of parent LanguageTemplates classes 
 *       are NOT available in NatSpec files.
 */
public class LanguageTemplatesIksOnlineBasics implements LanguageTemplates
{	
	private static final String MenuOptionStarteiteSeleniumID = "//*[@id='leftmenu']/ul/li[1]/a";
	private TestCase testCase;
	private ExecutionInfo executionInfo;
	private ZeiterfassungPageObject zeitErfassungPageObject;
	
	public LanguageTemplatesIksOnlineBasics(TestCase aTestCase) 
	{
		this.testCase = aTestCase;
		this.executionInfo = ExecutionInfo.getInstance();
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
	public void doLogin(String... loginData) 
	{
		testCase.inputText("j_username", loginData[0]);
		testCase.inputText("j_password", loginData[1]);
		testCase.clickButton("submit");			
	}

	@Override
    public void doLogout() {
        testCase.clickMenuHeader("//*[@id='leftmenu']/ul/li[16]/a");
    }
    
	@Override
	public boolean isLoginPageVisible() {
		return testCase.isElementReadyToUse("j_username");
	}

	@Override
	public boolean isOverviewPageVisible() 
	{
		return testCase.isElementReadyToUse("welcomeText") 
			&& testCase.isElementReadyToUse(MenuOptionStarteiteSeleniumID);
	}


	@Override
	public void gotoStartPage() 
	{
		try {
			if (testCase.isElementReadyToUse("closeButtons")) {
				// close dialog that may have been opened but not closed by the previous test
				testCase.clickButton("closeButtons");  
			}
			if ( ! isOverviewPageVisible() )  {
				clickMainMenuItem("Startseite"); // goto to Standard Start position for all test cases
			}
		} catch (Exception e) {
			// ignore
		}
	}
	
	@Override
	public List<String> getScriptDirectories() 
	{
		List<String> toReturn = new ArrayList<>();
		toReturn.add("IksOnline.Skripte");
		return toReturn;
	}
	
	
	//##########################################################################################
	//                       T E X T  S Y N T A X    M E T H O D S
	//##########################################################################################

	@LanguageTemplate(value = "Login mit #1, #2, #3 .")
	public void loginWith(String dealer, String seller, String password)  
	{
		doLogin(dealer.trim(), seller.trim(), password.trim());
		testCase.addReportMessage("Login mit <b>" + dealer + "</b> und <b>" + seller + "</b> wurde ausgeführt.");		
	}

	@LanguageTemplate(value = "Wird die #1 angezeigt?")
	public void isPageVisible(String expectedPage) 
	{
		expectedPage = replaceEmptyStringSymbol(expectedPage);
		boolean ok = true;
		
		if (expectedPage.equals("Loginseite"))  {
			ok = isLoginPageVisible();
		} else if (expectedPage.equals("Startseite"))  {
			ok = isOverviewPageVisible();
		} else {
			testCase.failWithMessage("Unbekannte Seite <b>" + expectedPage + "</b>.");
		}
		
		String question = "Wurde die <b>" + expectedPage + "</b> angezeigt" + QUESTION_IDENTIFIER;
		testCase.answerQuestion(question, ok);
	}

	@LanguageTemplate(value = "Klicke Hauptmenüpunkt #1 .")
	public void clickMainMenuItem(String menuText) 
	{
		menuText = replaceEmptyStringSymbol(menuText);
		
		if (menuText.equals("Startseite"))  {
			testCase.clickMenuHeader(MenuOptionStarteiteSeleniumID);
		} else if (menuText.equals("Einsatzberichte"))  {
				testCase.clickMenuHeader("//*[@id='leftmenu']/ul/li[2]/a");
		} else if (menuText.equals("Ausloggen"))  {
			doLogout();
			executionInfo.setAlreadyLoggedIn(false);
		} else {
			testCase.failWithMessage("Unbekannter Hauptmenüpunkt <b>" + menuText + "</b>.");
		}
		
		testCase.addReportMessage("Hauptmenüpunkt <b>" + menuText + "</b> wurde geklickt.");
	}

	@LanguageTemplate(value = "Relogin.")
	public void relogin() {
		testCase.loginIntoIksOnline();
	}

	@LanguageTemplate(value = "Gebe in Feld #1 den gleichnamigen Wert aus den #2 ein.")
	public void enterTextInField(String fieldName, ObjectData objectData)
	{
		boolean ok = true;
		String value = objectData.getValueWithReplacedUnderscores(fieldName);
		String pageName = getPageName();
		
		if ("Zeiterfassung".equals(pageName)) {
			zeitErfassungPageObject.enterTextInField(fieldName, value);
		} else {
			ok = false;
		}
		
		if (ok) {
			testCase.addReportMessage("In Feld <b>" + fieldName + "</b> wurde der Wert <b>" + value + "</b> eingegeben.");
		} else {
			testCase.failWithMessage("Die aktuelle Datenmaske (siehe Screenshot) ist unbekannt.");
		}
	}

	@LanguageTemplate(value = "Klicke die Schaltfläche #1 .")
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

	@LanguageTemplate(value = "Es wird die Stundenzahl für Projekt #1 aus der Projektübersicht als #2 festgehalten.")
	public BigDecimalValue storeSumOfHours(String projectName, String variableName) 
	{
		String sumOfHours;
		try {
			sumOfHours = testCase.getTableCell("summeryTable:tb", projectName, 2).replace(",", ".");
		} catch (UnsupportedGuiEventException e) {
			sumOfHours = "0";
		}
		BigDecimalValue toReturn = new BigDecimalValue(new BigDecimal(sumOfHours ));
	    testCase.addReportMessage("Der Wert <b>" + sumOfHours + "</b> wurde als <b>"  + variableName + "</b> festgehalten.");
		return toReturn;
	}

	@LanguageTemplate(value = "Ist die Differenz von #1 und #2 gleich #3 ?")
	public void checkDiffInSumHours(BigDecimalValue neueStundenZahl, BigDecimalValue alteStundenZahl, String expectedResult) 
	{
		BigDecimal diff = neueStundenZahl.value.subtract(alteStundenZahl.value);
		BigDecimal expected = new BigDecimal(expectedResult);
		boolean ok = expected.compareTo(diff) == 0;
		String question = "Ist die Differenz der neuen (" + neueStundenZahl.value + ") und alten Stundenzahl "
				          + " (" + alteStundenZahl.value + ") gleich <b>" 
                  		  + expectedResult + "</b> " + QUESTION_IDENTIFIER;
		testCase.answerQuestion(question, ok);
	}

	@LanguageTemplate(value = "Wähle im Feld #1 den gleichnamigen Wert aus den #2 aus.")
	public void choose(String fieldName, ObjectData objectData) 
	{
		boolean ok = true;
		String value = objectData.getValue(fieldName + "");
		String pageName = getPageName();
		
		if ("Zeiterfassung".equals(pageName)) {
			zeitErfassungPageObject.choose(fieldName, value);
		} else {
			ok = false;
		}
		
		if (ok) {
			testCase.addReportMessage("In Feld <b>" + fieldName + "</b> wurde der Wert <b>" + value + "</b> ausgewählt.");
		} else {
			testCase.failWithMessage("Die aktuelle Datenmaske (siehe Screenshot) ist unbekannt.");
		}
		
	}


	@LanguageTemplate(value = "Wenn Feld Projekt kein Auswahlfeld ist, dann klicke die Schaltfläche '?'.")
	public void checkProjectField() 
	{
		boolean ok = true;
		boolean clickNecessary = false;
		String pageName = getPageName();
		
		if ("Zeiterfassung".equals(pageName)) {
			clickNecessary = zeitErfassungPageObject.isProjectFieldTextInputField();
			if (clickNecessary) {
				zeitErfassungPageObject.clickButton("?");
			}
		} else {
			ok = false;
		}
		
		if (ok) 
		{
			if (clickNecessary) {				
				testCase.addReportMessage("Die Schaltfläsche <b>?</b> wurde geklickt.");
			}
		} else {
			testCase.failWithMessage("Die aktuelle Datenmaske (siehe Screenshot) enthält kein Feld 'Projekt'.");
		}
		
	}
	
}
