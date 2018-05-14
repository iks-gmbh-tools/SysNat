package com.iksgmbh.sysnat.language_templates.helloworldspringboot;

import static com.iksgmbh.sysnat.utils.SysNatConstants.QUESTION_IDENTIFIER;
import static com.iksgmbh.sysnat.utils.SysNatStringUtil.replaceEmptyStringSymbol;

import java.util.ArrayList;
import java.util.List;

import com.iksgmbh.sysnat.ExecutionInfo;
import com.iksgmbh.sysnat.TestCase;
import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.domain.SysNatTestData.ObjectData;
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
public class LanguageTemplatesHelloWorldSpringBootBasics implements LanguageTemplates
{	
	private TestCase testCase;
	private ExecutionInfo executionInfo;
	private FormPageObject formPageObject;
	private ResultPageObject resultPageObject;
	private ErrorPageObject errorPageObject;
	
	public LanguageTemplatesHelloWorldSpringBootBasics(TestCase aTestCase) 
	{
		this.testCase = aTestCase;
		this.executionInfo = ExecutionInfo.getInstance();
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
	public void doLogin(String... loginData) 
	{
		testCase.inputText("username", loginData[0]);
		testCase.inputText("password", loginData[1]);
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
	public boolean isOverviewPageVisible() 
	{
		return testCase.isElementReadyToUse("greeting") 
			&& testCase.isElementReadyToUse("//*[@class='navbar-inner']/a");
	}


	@Override
	public void gotoStartPage() 
	{
		try {
			if (testCase.isElementReadyToUse("closeDialogButton")) {
				// close dialog that may have been opened but not closed by the previous test
				testCase.clickButton("closeDialogButton");  
			}
			if ( ! isOverviewPageVisible() )  {
				clickMainMenuItem("Form Page"); // goto to Standard Start position for all test cases
			}
		} catch (Exception e) {
			// ignore
		}
	}
	

	@Override
	public List<String> getScriptDirectories() 
	{
		List<String> toReturn = new ArrayList<>();
		toReturn.add("HelloWorldSpringBoot.Scripts");
		return toReturn;
	}
	
	//##########################################################################################
	//                       T E X T  S Y N T A X    M E T H O D S
	//##########################################################################################

	@LanguageTemplate(value = "Login with #1 , #2 .")
	public void loginWith(String username, String password)  
	{
		doLogin(username.trim(), password.trim());
		testCase.addReportMessage("Login performed with <b>" + username + "</b>.");		
	}

	@LanguageTemplate(value = "Is page #1 visible?")
	public void isPageVisible(String expectedPage) 
	{
		expectedPage = replaceEmptyStringSymbol(expectedPage);
		String actualPageName = getPageName();
		boolean ok = actualPageName.equals(expectedPage);
		String question = "Is page <b>" + expectedPage + "</b> visible" + QUESTION_IDENTIFIER;
		testCase.answerQuestion(question, ok);
	}

	@LanguageTemplate(value = "Click menu item #1 .")
	public void clickMainMenuItem(String menuText) 
	{
		menuText = replaceEmptyStringSymbol(menuText);
		
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
	public void relogin() {
		testCase.loginIntoHelloWorld();
		testCase.addReportMessage("Login has been perfomed with DefaultLoginData.");
	}

	@LanguageTemplate(value = "Enter in field #1 the equally named value from #2 .")
	public void enterTextInField(String fieldName, ObjectData objectData)
	{
		fieldName = replaceEmptyStringSymbol(fieldName);
		boolean ok = true;
		String value = objectData.getValueWithReplacedUnderscores(fieldName);
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

	@LanguageTemplate(value = "Click button #1 .")
	public void clickButton(String buttonName) 
	{
		buttonName = replaceEmptyStringSymbol(buttonName);
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


	@LanguageTemplate(value = "Select for field #1 the equally named value from #2 .")
	public void choose(String fieldName, ObjectData objectData) 
	{
		fieldName = replaceEmptyStringSymbol(fieldName);
		boolean ok = true;
		String value = objectData.getValue(fieldName);
		if (value == null) {
			testCase.failWithMessage("No value defined for key <b>" + fieldName + "</b> in data set <b>" + objectData.getName() + "</b>."); 
		}
		
		String pageName = getPageName();
		
		if ("Form Page".equals(pageName)) {
			formPageObject.choose(fieldName, value);
		} else {
			ok = false;
		}
		
		if (ok) {
			testCase.addReportMessage("For field <b>" + fieldName + "</b> value <b>" + value + "</b> has been selected.");
		} else {
			testCase.failWithMessage("Selecting a value from a field is not supported for page <b>"+ pageName + "</b>.");
		}		
	}

	@LanguageTemplate(value = "Is the displayed text #1 equal to #2 ?")
	public void isDislayedTextCorrect(String guiElementToRead, String expectedText) 
	{
		guiElementToRead = replaceEmptyStringSymbol(guiElementToRead);
		expectedText = replaceEmptyStringSymbol(expectedText);
		
		String actualText = null;
		String pageName = getPageName();
		
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

	@LanguageTemplate(value = "Is the displayed text #1 equal to the #2 in #3 ?")
	public void isDisplayedTextTheSameAsInTestData(String guiElementToRead, String fieldInObjectDataset, String objectDataName)
	{
		guiElementToRead = replaceEmptyStringSymbol(guiElementToRead);
		ObjectData objectData = testCase.getTestDataSets().getObjectData(objectDataName);
		if (objectData == null) {
			testCase.failWithMessage("No data set found for name <b>" + objectDataName + "</b>.");
		}
		String expectedText = (String) objectData.get(fieldInObjectDataset);
		if (expectedText == null) {
			testCase.failWithMessage("No value available for <b>" + fieldInObjectDataset + "</b> in data set <b>" + objectDataName + "</b>.");
		}
		String actualText = null;
		String pageName = getPageName();
		
		if ("Result Page".equals(pageName)) {
			actualText = resultPageObject.getText(guiElementToRead);
		} 
		
		if (actualText == null) {
			testCase.failWithMessage("Element <b>"+ guiElementToRead + "</b> is not supported to be read from page <b>" + pageName + "</b>.");
		} else {
			boolean ok = actualText.equals(expectedText);
			String question = "Is expected text (" + expectedText + ") equals to the actually displayed one (" + actualText + ")" + QUESTION_IDENTIFIER;
			testCase.answerQuestion(question, ok);	
		}
		
	}

	@LanguageTemplate(value = "Convert data #1 to #2 .")
	public ObjectData convertTo(ObjectData oldObjectData, String nameOfNewObjectData) {
		ObjectData newDataset = oldObjectData.dublicate(nameOfNewObjectData);
		testCase.getTestDataSets().addObjectData(nameOfNewObjectData, newDataset);
		return newDataset;
	}


}
