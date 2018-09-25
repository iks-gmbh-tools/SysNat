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
package com.iksgmbh.sysnat.language_templates.helloworldspringboot;

import static com.iksgmbh.sysnat.common.utils.SysNatConstants.QUESTION_IDENTIFIER;

import java.util.HashMap;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.ExecutableExample;
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
 * @author Reik Oberrath
 */
@LanguageTemplateContainer
public class LanguageTemplatesHelloWorldSpringBootBasics implements LanguageTemplates
{	
	private ExecutableExample executableExample;
	private ExecutionRuntimeInfo executionInfo;
	private FormPageObject formPageObject;
	private ResultPageObject resultPageObject;
	private ErrorPageObject errorPageObject;
	
	public LanguageTemplatesHelloWorldSpringBootBasics(ExecutableExample aTestCase) 
	{
		this.executableExample = aTestCase;
		this.executionInfo = ExecutionRuntimeInfo.getInstance();
		this.formPageObject = new FormPageObject(aTestCase);
		this.resultPageObject = new ResultPageObject(aTestCase);
		this.errorPageObject = new ErrorPageObject(aTestCase);
	}

	private String getPageName() {
		return executableExample.getTextForElement("h2");
	}
	
	//##########################################################################################
	//                       I N T E R F A C E    M E T H O D S
	//##########################################################################################
	
	@Override
	public void doLogin(final HashMap<StartParameter,String> startParameter) 
	{
		executableExample.inputText("username", startParameter.get(StartParameter.LOGINID));
		executableExample.inputText("password", startParameter.get(StartParameter.PASSWORD));
		executableExample.clickButton("login_button");			
	}

	@Override
    public void doLogout() {
        executableExample.clickMenuHeader("Logout");
    }
    
	@Override
	public boolean isLoginPageVisible() {
		return executableExample.isElementReadyToUse("username");
	}

	@Override
	public boolean isStartPageVisible() 
	{
		return executableExample.isElementReadyToUse("greeting") 
			&& executableExample.isElementReadyToUse("//*[@id='navbar-inner']");
	}


	@Override
	public void gotoStartPage() 
	{
		try {
			if (executableExample.isElementReadyToUse("closeDialogButton")) {
				// close dialog that may have been opened but not closed by the previous test
				executableExample.clickButton("closeDialogButton");  
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
		executableExample.addReportMessage("Login performed with <b>" + username + "</b>.");		
	}

	@LanguageTemplate(value = "Is page ^^ visible?")
	public void isPageVisible(final String valueCandidate) 
	{
		final String expectedPage = executableExample.getTestDataValue(valueCandidate);
		final String actualPageName = getPageName();
		boolean ok = actualPageName.equals(expectedPage);
		String question = "Is page <b>" + expectedPage + "</b> visible" + QUESTION_IDENTIFIER;
		executableExample.answerQuestion(question, ok);
	}

	@LanguageTemplate(value = "Click menu item ^^.")
	public void clickMainMenuItem(final String valueCandidate) 
	{
		final String menuText = executableExample.getTestDataValue(valueCandidate);
		
		if (menuText.equals("Form Page"))  {
			executableExample.clickMenuHeader("Form Page");
		} else if (menuText.equals("Logout"))  {
			doLogout();
			executionInfo.setAlreadyLoggedIn(false);
		} else {
			executableExample.failWithMessage("Unknown menu item <b>" + menuText + "</b>.");
		}
		
		executableExample.addReportMessage("Menu item <b>" + menuText + "</b> has been clicked.");
	}

	@LanguageTemplate(value = "Relogin.")
	public void relogin() 
	{
		TestApplication testApp = executionInfo.getTestApplication();
		doLogin(testApp.getStartParameter());
		executableExample.addReportMessage("Login has been perfomed with DefaultLoginData.");
	}

	@LanguageTemplate(value = "Enter ^^ in text field ^^.")
	public void enterTextInField(String valueCandidate, String fieldName)
	{
		boolean ok = true;
		String value = executableExample.getTestDataValue(valueCandidate);
		String pageName = getPageName();
		
		if ("Form Page".equals(pageName)) {
			formPageObject.enterTextInField(fieldName, value);
		} else if ("Result Page".equals(pageName)) {
			resultPageObject.enterTextInField(fieldName, value);
		} else {
			ok = false;
		}
		
		if (ok) {
			executableExample.addReportMessage("In field <b>" + fieldName + "</b> the value <b>" + value + "</b> has been entered.");
		} else {
			executableExample.failWithMessage("Entering a value into a field is not supported for page <b>"+ pageName + "</b>.");
		}
	}

	@LanguageTemplate(value = "Click button ^^.")
	public void clickButton(String valueCandidate) 
	{
		final String buttonName = executableExample.getTestDataValue(valueCandidate);
		boolean ok = true;
		String pageName = getPageName();
		
		if ("Form Page".equals(pageName)) {
			formPageObject.clickButton(buttonName);
		} else if ("Result Page".equals(pageName)) {
			resultPageObject.clickButton(buttonName);
		} else if ("Error Page".equals(pageName)) {
			executableExample.clickButton("btnBack");
		} else {
			ok = false;
		}
		
		if (ok) {
			executableExample.addReportMessage("Button <b>" + buttonName + "</b> has beed clicked.");
		} else {
			executableExample.failWithMessage("Clicking a button is not supported for page <b>"+ pageName + "</b>.");
		}
	}


	@LanguageTemplate(value = "Select ^^ in selection field ^^.")
	public void choose(String valueCandidate, String fieldName) 
	{
		final String value = executableExample.getTestDataValue(valueCandidate);
		final String pageName = getPageName();
		boolean ok = true;		
		
		if ("Form Page".equals(pageName)) {
			formPageObject.chooseForCombobox(fieldName, value);
		} else {
			ok = false;
		}
		
		if (ok) {
			executableExample.addReportMessage("For field <b>" + fieldName + "</b> value <b>" + value + "</b> has been selected.");
		} else {
			executableExample.failWithMessage("Selecting a value is not supported for page <b>"+ pageName + "</b>.");
		}		
	}

	@LanguageTemplate(value = "Is the displayed text ^^ equal to ^^?")
	public void isDislayedTextCorrect(final String guiElementToRead, final String valueCandidate) 
	{
		final String expectedText = executableExample.getTestDataValue(valueCandidate);
		final String pageName = getPageName();
		
		String actualText = null;
		
		if ("Result Page".equals(pageName)) {
			actualText = resultPageObject.getText(guiElementToRead);
		} else if ("Error Page".equals(pageName)) {
			actualText = errorPageObject.getText(guiElementToRead);
		}
		
		if (actualText == null) {
			executableExample.failWithMessage("Element <b>"+ guiElementToRead + "</b> is not supported to be read from page <b>" + pageName + "</b>.");
		} else {
			boolean ok = actualText.equals(expectedText);
			String question = "Is the expected text (" + expectedText + ") equals to the actually displayed one (" + actualText + ")" + QUESTION_IDENTIFIER;
			executableExample.answerQuestion(question, ok);	
		}
	}
	
//  Needed?
//	@LanguageTemplate(value = "Convert data ^^ to ^^.")
//	public void convertTo(ObjectData oldObjectData, String nameOfNewObjectData) 
//	{
//		ObjectData newDataset = oldObjectData.dublicate(nameOfNewObjectData);
//		executableExample.getTestData().addObjectData(nameOfNewObjectData, newDataset);
//		//return newDataset;
//	}
//
//
}