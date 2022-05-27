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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.ApplicationLoginParameter;
import com.iksgmbh.sysnat.domain.TestApplication;
import com.iksgmbh.sysnat.language_templates.LanguageTemplateBasics;
import com.iksgmbh.sysnat.language_templates.LanguageTemplateBasics.PageChangeEvent.EventType;
import com.iksgmbh.sysnat.language_templates.PageObject;
import com.iksgmbh.sysnat.language_templates.helloworldspringboot.pageobject.ErrorPageObject;
import com.iksgmbh.sysnat.language_templates.helloworldspringboot.pageobject.FormPageObject;
import com.iksgmbh.sysnat.language_templates.helloworldspringboot.pageobject.LoginPageObject;
import com.iksgmbh.sysnat.language_templates.helloworldspringboot.pageobject.ResultPageObject;

/**
 * Contains the basic language templates for the test application HelloWorldSpringBoot.
 * 
 * This class demonstrates the use of PageObjects and testing an application with login.
 * 
 * @author Reik Oberrath
 */
@LanguageTemplateContainer
public class LanguageTemplatesBasics_HelloWorldSpringBoot extends LanguageTemplateBasics
{	
	private LoginPageObject loginPageObject;
	private FormPageObject formPageObject;
	private ResultPageObject resultPageObject;
	private ErrorPageObject errorPageObject;
	
	public LanguageTemplatesBasics_HelloWorldSpringBoot(ExecutableExample anXX) 
	{
		this.executableExample = anXX;
		this.executionInfo = ExecutionRuntimeInfo.getInstance();		
		this.loginPageObject = createAndRegister(LoginPageObject.class, executableExample);
		this.formPageObject = createAndRegister(FormPageObject.class, executableExample);
		this.resultPageObject = createAndRegister(ResultPageObject.class, executableExample);
		this.errorPageObject = createAndRegister(ErrorPageObject.class, executableExample);
		
		// define page change events
		addPageChangeEvent(new PageChangeEventBuilder().from(loginPageObject).via(EventType.ButtonClick).on("Log in").to(formPageObject).build());
		addPageChangeEvent(new PageChangeEventBuilder().from(formPageObject).via(EventType.ButtonClick).on("Greet").to(resultPageObject).build());
		addPageChangeEvent(new PageChangeEventBuilder().from(resultPageObject).via(EventType.MenuItemClick).on("Form Page").to(formPageObject).build());
		// NOTE: The following event is valid for each possible current page!
		addPageChangeEvent(new PageChangeEventBuilder().via(EventType.MenuItemClick).on("Logout").to(loginPageObject).build());
		addPageChangeEvent(new PageChangeEventBuilder().from(errorPageObject).via(EventType.ButtonClick).on("Back").to(formPageObject).build());
		
	}

	private String getPageName() {
		return executableExample.getTextForElement("h2");
	}
	
	//##########################################################################################
	//                       I N T E R F A C E    M E T H O D S
	//##########################################################################################
	
	@Override
	public void doLogin(final Map<ApplicationLoginParameter,String> startParameter) 
	{
		loginPageObject.enterTextInTextField("Username", startParameter.get(ApplicationLoginParameter.LoginId));
		loginPageObject.enterTextInTextField("Password", startParameter.get(ApplicationLoginParameter.Password));
		loginPageObject.clickButton("Log in");			
	}

	@Override
    public void doLogout() {
        executableExample.clickMenuHeader("Logout");
    }
    
	@Override
	public boolean isLoginPageVisible() {
		return loginPageObject.isCurrentlyDisplayed();
	}

	@Override
	public boolean isStartPageVisible() {
		return formPageObject.isCurrentlyDisplayed();
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

	@LanguageTemplate(value = "Tell current page name.")
	public void tellCurrentPageName() {
		executableExample.addCommentToReport("Current page name is <b>" + getCurrentPage().getPageName() + "</b>.");		
	}

	@LanguageTemplate(value = "Login with ^^, ^^.")
	public void loginWith(String username, String password)  
	{
		final HashMap<ApplicationLoginParameter, String> startParameter = new HashMap<>();
		startParameter.put(ApplicationLoginParameter.LoginId, username);
		startParameter.put(ApplicationLoginParameter.Password, password);
		doLogin(startParameter);
		executableExample.addReportMessage("Login performed with <b>" + username + "</b>.");
		findCurrentPage(); // may error page will be displayed !
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
	public void clickMainMenuItem(final String mainMenuItem) 
	{
		if (mainMenuItem.equals("Form Page"))  {
			executableExample.clickMenuHeader("Form Page");
		} else if (mainMenuItem.equals("Logout"))  {
			doLogout();
			executionInfo.setAlreadyLoggedIn(false);
		} else {
			executableExample.failWithMessage("Unknown menu item <b>" + mainMenuItem + "</b>.");
		}

		checkPageChange(mainMenuItem, EventType.MenuItemClick);
		executableExample.addReportMessage("Menu item <b>" + mainMenuItem + "</b> has been clicked.");
	}

	@LanguageTemplate(value = "Relogin.")
	public void relogin() 
	{
		TestApplication testApp = executionInfo.getTestApplication();
		doLogin(testApp.getLoginParameter());
		executableExample.addReportMessage("Login has been perfomed with DefaultLoginData.");
	}

	@LanguageTemplate(value = "Enter ^^ in text field ^^.")
	public void enterTextInField(String valueCandidate, String fieldName) {
		super.enterTextInField(valueCandidate, fieldName);
	}

	@LanguageTemplate(value = "Click button ^^.")
	public void clickButtonToChangePage(String buttonName) 
	{ 
		List<PageObject> possiblePages = new ArrayList<>();
		if ("Greet".equals(buttonName)) 
		{
			possiblePages.add(resultPageObject);
			setCurrentPage(formPageObject);
			super.clickButtonToChangePage(buttonName, possiblePages , 0);
		} else {
			super.clickButtonWithCheckPageChange(buttonName);
		}
	}

	@LanguageTemplate(value = "Select ^^ in selection field ^^.")
	public void chooseInCombobox(String valueCandidate, String fieldName) {
		if ("Greeting".equals(fieldName)) {
			setCurrentPage(formPageObject);
		}
		super.chooseInCombobox(valueCandidate, fieldName);
	}

	@LanguageTemplate(value = "Is the displayed text ^^ equal to ^^?")
	public void isTextDislayed(final String guiElementToRead, final String valueCandidate) {
		super.isTextDislayed(guiElementToRead, valueCandidate);
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