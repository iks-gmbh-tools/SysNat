package com.iksgmbh.sysnat.language_templates.helloworldspringboot;

import static com.iksgmbh.sysnat.common.utils.SysNatConstants.QUESTION_IDENTIFIER;

import java.util.HashMap;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.TestCase;
import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.StartParameter;
import com.iksgmbh.sysnat.domain.TestApplication;
import com.iksgmbh.sysnat.language_templates.LanguageTemplates;
import com.iksgmbh.sysnat.language_templates.helloworldspringboot.pageobject.ErrorPageObject;
import com.iksgmbh.sysnat.language_templates.helloworldspringboot.pageobject.FormPageObject;
import com.iksgmbh.sysnat.language_templates.helloworldspringboot.pageobject.ResultPageObject;

/**
 * Contains the basic language templates for IKS-Online tests.
 * 
 * Note: Java interitance is not possible to use with NatSpec
 *       because SyntaxPattern methods of parent LanguageTemplates classes 
 *       are NOT available in NatSpec files.
 */
@LanguageTemplateContainer
public class LanguageTemplatesHelloWorldSpringBootBasics implements LanguageTemplates
{	
	private TestCase testCase;
	private ExecutionRuntimeInfo executionInfo;
	private FormPageObject formPageObject;
	private ResultPageObject resultPageObject;
	private ErrorPageObject errorPageObject;
	
	public LanguageTemplatesHelloWorldSpringBootBasics(TestCase aTestCase) 
	{
		this.testCase = aTestCase;
		this.executionInfo = ExecutionRuntimeInfo.getInstance();
		this.formPageObject = new FormPageObject(aTestCase);
		this.resultPageObject = new ResultPageObject(aTestCase);
		this.errorPageObject = new ErrorPageObject(aTestCase);
	}

	private String getPageName() {
		return testCase.getTextForElement("h2");
	}
	
	//##########################################################################################
	//                       I N T E R F A C E    M E T H O D S
	//##########################################################################################
	
	@Override
	public void doLogin(final HashMap<StartParameter,String> startParameter) 
	{
		testCase.inputText("username", startParameter.get(StartParameter.LOGINID));
		testCase.inputText("password", startParameter.get(StartParameter.PASSWORD));
		testCase.clickButton("login_button");			
	}

	@Override
    public void doLogout() {
        testCase.clickMenuHeader("Logout");
    }
    
	@Override
	public boolean isLoginPageVisible() {
		return testCase.isElementReadyToUse("username");
	}

	@Override
	public boolean isStartPageVisible() 
	{
		return testCase.isElementReadyToUse("greeting") 
			&& testCase.isElementReadyToUse("//*[@id='navbar-inner']");
	}


	@Override
	public void gotoStartPage() 
	{
		try {
			if (testCase.isElementReadyToUse("closeDialogButton")) {
				// close dialog that may have been opened but not closed by the previous test
				testCase.clickButton("closeDialogButton");  
			}
			if ( ! isStartPageVisible() )  {
				clickMainMenuItem("Form Page"); // goto to Standard Start position for all test cases
			}
		} catch (Exception e) {
			// ignore
		}
	}
	
	
	//##########################################################################################
	//                   L A N G U A G E   T E M P L A T E    M E T H O D S
	//##########################################################################################

	
	@LanguageTemplate(value = "Login with ^^, ^^.")
	public void loginWith(String username, String password)  
	{
		final HashMap<StartParameter, String> startParameter = new HashMap<>();
		startParameter.put(StartParameter.LOGINID, username);
		startParameter.put(StartParameter.PASSWORD, password);
		doLogin(startParameter);
		testCase.addReportMessage("Login performed with <b>" + username + "</b>.");		
	}

	@LanguageTemplate(value = "Is page ^^ visible?")
	public void isPageVisible(final String valueCandidate) 
	{
		final String expectedPage = testCase.getTestData().getValueFor(valueCandidate);
		final String actualPageName = getPageName();
		boolean ok = actualPageName.equals(expectedPage);
		String question = "Is page <b>" + expectedPage + "</b> visible" + QUESTION_IDENTIFIER;
		testCase.answerQuestion(question, ok);
	}

	@LanguageTemplate(value = "Click menu item ^^.")
	public void clickMainMenuItem(final String valueCandidate) 
	{
		final String menuText = testCase.getTestData().getValueFor(valueCandidate);
		
		if (menuText.equals("Form Page"))  {
			testCase.clickMenuHeader("Form Page");
		} else if (menuText.equals("Logout"))  {
			doLogout();
			executionInfo.setAlreadyLoggedIn(false);
		} else {
			testCase.failWithMessage("Unknown menu item <b>" + menuText + "</b>.");
		}
		
		testCase.addReportMessage("Menu item <b>" + menuText + "</b> has been clicked.");
	}

	@LanguageTemplate(value = "Relogin.")
	public void relogin() 
	{
		TestApplication testApp = executionInfo.getTestApplication();
		doLogin(testApp.getStartParameter());
		testCase.addReportMessage("Login has been perfomed with DefaultLoginData.");
	}

	@LanguageTemplate(value = "Enter ^^ in text field ^^.")
	public void enterTextInField(String valueCandidate, String fieldName)
	{
		boolean ok = true;
		String value = testCase.getTestData().getValueFor(valueCandidate);
		String pageName = getPageName();
		
		if ("Form Page".equals(pageName)) {
			formPageObject.enterTextInField(fieldName, value);
		} else if ("Result Page".equals(pageName)) {
			resultPageObject.enterTextInField(fieldName, value);
		} else {
			ok = false;
		}
		
		if (ok) {
			testCase.addReportMessage("In field <b>" + fieldName + "</b> the value <b>" + value + "</b> has been entered.");
		} else {
			testCase.failWithMessage("Entering a value into a field is not supported for page <b>"+ pageName + "</b>.");
		}
	}

	@LanguageTemplate(value = "Click button ^^.")
	public void clickButton(String valueCandidate) 
	{
		final String buttonName = testCase.getTestData().getValueFor(valueCandidate);
		boolean ok = true;
		String pageName = getPageName();
		
		if ("Form Page".equals(pageName)) {
			formPageObject.clickButton(buttonName);
		} else if ("Result Page".equals(pageName)) {
			resultPageObject.clickButton(buttonName);
		} else if ("Error Page".equals(pageName)) {
			testCase.clickButton("btnBack");
		} else {
			ok = false;
		}
		
		if (ok) {
			testCase.addReportMessage("Button <b>" + buttonName + "</b> has beed clicked.");
		} else {
			testCase.failWithMessage("Clicking a button is not supported for page <b>"+ pageName + "</b>.");
		}
	}


	@LanguageTemplate(value = "Select ^^ in selection field ^^.")
	public void choose(String valueCandidate, String fieldName) 
	{
		final String value = testCase.getTestData().getValueFor(valueCandidate);
		final String pageName = getPageName();
		boolean ok = true;		
		
		if ("Form Page".equals(pageName)) {
			formPageObject.chooseForCombobox(fieldName, value);
		} else {
			ok = false;
		}
		
		if (ok) {
			testCase.addReportMessage("For field <b>" + fieldName + "</b> value <b>" + value + "</b> has been selected.");
		} else {
			testCase.failWithMessage("Selecting a value is not supported for page <b>"+ pageName + "</b>.");
		}		
	}

	@LanguageTemplate(value = "Is the displayed text ^^ equal to ^^?")
	public void isDislayedTextCorrect(final String guiElementToRead, final String valueCandidate) 
	{
		final String expectedText = testCase.getTestData().getValueFor(valueCandidate);
		final String pageName = getPageName();
		
		String actualText = null;
		
		if ("Result Page".equals(pageName)) {
			actualText = resultPageObject.getText(guiElementToRead);
		} else if ("Error Page".equals(pageName)) {
			actualText = errorPageObject.getText(guiElementToRead);
		}
		
		if (actualText == null) {
			testCase.failWithMessage("Element <b>"+ guiElementToRead + "</b> is not supported to be read from page <b>" + pageName + "</b>.");
		} else {
			boolean ok = actualText.equals(expectedText);
			String question = "Is the expected text (" + expectedText + ") equals to the actually displayed one (" + actualText + ")" + QUESTION_IDENTIFIER;
			testCase.answerQuestion(question, ok);	
		}
	}
	
//  Needed?
//	@LanguageTemplate(value = "Convert data ^^ to ^^.")
//	public void convertTo(ObjectData oldObjectData, String nameOfNewObjectData) 
//	{
//		ObjectData newDataset = oldObjectData.dublicate(nameOfNewObjectData);
//		testCase.getTestData().addObjectData(nameOfNewObjectData, newDataset);
//		//return newDataset;
//	}
//
//
}
