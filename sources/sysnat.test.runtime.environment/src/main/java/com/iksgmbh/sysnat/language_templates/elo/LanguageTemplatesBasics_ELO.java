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
package com.iksgmbh.sysnat.language_templates.elo;

import static com.iksgmbh.sysnat.common.utils.SysNatConstants.QUESTION_IDENTIFIER;

import java.util.HashMap;
import java.util.Map;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.ApplicationLoginParameter;
import com.iksgmbh.sysnat.domain.TestApplication;
import com.iksgmbh.sysnat.guicontrol.SwingGuiControl;
import com.iksgmbh.sysnat.language_templates.LanguageTemplateBasics;
import com.iksgmbh.sysnat.language_templates.elo.pageobject.LoginPageObject;
import com.iksgmbh.sysnat.language_templates.elo.pageobject.MainFramePageObject;

/**
 * Contains the basic language templates for the test application HelloWorldSpringBoot.
 * 
 * This class demonstrates the use of PageObjects and testing an application with login.
 * 
 * @author Reik Oberrath
 */
@LanguageTemplateContainer
public class LanguageTemplatesBasics_ELO extends LanguageTemplateBasics
{	
	private LoginPageObject loginPageObject;
	private MainFramePageObject mainFramePageObject;
	
	public LanguageTemplatesBasics_ELO(ExecutableExample anXX) 
	{
		this.executableExample = anXX;
		this.executionInfo = ExecutionRuntimeInfo.getInstance();		
		this.loginPageObject = createAndRegister(LoginPageObject.class, executableExample);
		this.mainFramePageObject = createAndRegister(MainFramePageObject.class, executableExample);
	}

	public String getPageName() {
		return "???";
	}
	
	public void toFront() {
		((SwingGuiControl)executableExample.getActiveGuiController()).windowToFront();
	}
	
	
	//##########################################################################################
	//                       I N T E R F A C E    M E T H O D S
	//##########################################################################################
	
	@Override
	public void doLogin(final Map<ApplicationLoginParameter,String> loginParameter) 
	{
		String username = loginPageObject.getText("Benutzername");
		String target = loginParameter.get(ApplicationLoginParameter.LoginId);
		if (! username.equals(target)) {
			loginPageObject.enterTextInTextField("Benutzername", loginParameter.get(ApplicationLoginParameter.LoginId));
		}
		loginPageObject.enterTextInTextField("Passwort", loginParameter.get(ApplicationLoginParameter.Password));
		loginPageObject.clickButton("Login");	
		
		findCurrentPage();
	}

	@Override
    public void doLogout() {
        executableExample.clickMenuHeader("Datei");
        executableExample.clickMenuHeader("Datei-Beenden");
        findCurrentPage();
    }
    
	@Override
	public boolean isLoginPageVisible() {
		return loginPageObject.isCurrentlyDisplayed();
	}

	@Override
	public boolean isStartPageVisible() {
		return mainFramePageObject.isCurrentlyDisplayed();
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
		final HashMap<ApplicationLoginParameter, String> startParameter = new HashMap<>();
		startParameter.put(ApplicationLoginParameter.LoginId, username);
		startParameter.put(ApplicationLoginParameter.Password, password);
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
	@LanguageTemplate(value = "Öffne Hauptmenü ^^.")
	@LanguageTemplate(value = "Klicke Hauptmenüpunkt ^^.")
	public void clickMainMenuItem(final String mainMenuItem) 
	{
		if (mainMenuItem.equals("Beenden"))  {
			doLogout();
			executionInfo.setAlreadyLoggedIn(false);
			setCurrentPage(loginPageObject);
			return;
		}
		
		if (mainMenuItem.equals("Akte")) {
			executableExample.clickMenuHeader("MenuAkteBearbeiten", mainMenuItem);
			setCurrentPage(mainFramePageObject);
			return;
		}

		if (mainMenuItem.equals("Neue Suche")) {
			executableExample.clickMenuHeader("MenuAkteBearbeiten-Neue Suche", mainMenuItem);
			setCurrentPage(mainFramePageObject);
			return;
		}
		
		try {
			executableExample.clickMenuHeader(mainMenuItem);
			executableExample.addReportMessage("Menu item <b>" + mainMenuItem + "</b> has been clicked.");
		} catch (Exception e) {
			executableExample.failWithMessage("Unknown menu item <b>" + mainMenuItem + "</b>.");
		}

	}

	@LanguageTemplate(value = "Relogin.")
	public void relogin() 
	{
		TestApplication testApp = executionInfo.getTestApplication();
		doLogin(testApp.getLoginParameter());
		executableExample.addReportMessage("Login has been perfomed with DefaultLoginData.");
		setCurrentPage(mainFramePageObject);
	}

	@LanguageTemplate(value = "Enter ^^ in text field ^^.")
	@LanguageTemplate(value = "Gebe ^^ ins Textfeld ^^ ein.")
	public void enterTextInField(String valueCandidate, String fieldName) 
	{
		final String value = executableExample.getTestDataValue(valueCandidate);
		mainFramePageObject.enterTextInTextField(fieldName, value);
		executableExample.addReportMessage("Es wurde <b>" + value + "</b> in das Feld <b>" 
		                                   + fieldName + "</b> eingegeben.");
	}

	@LanguageTemplate(value = "Click button ^^.")
	@LanguageTemplate(value = "Klicke den Button ^^.")
	public void clickButtonToChangePage(String buttonName) 
	{
		getCurrentPage().clickButton(buttonName);
		findCurrentPage();
	}

	@LanguageTemplate(value = "Is the displayed text ^^ equal to ^^?")
	public void isTextDislayed(final String guiElementToRead, final String valueCandidate) {
		super.isTextDislayed(guiElementToRead, valueCandidate);
	}


	@LanguageTemplate(value = "Klicke Toolbar-Button ^^.")
	public void clickToolBarButton(String buttonDisplayName) 
	{
		executableExample.getActiveGuiController().clickButton(buttonDisplayName);
		executableExample.addReportMessage("Der Toolbar-Button <b>" + buttonDisplayName + "</b> wurde geklickt.");
	}	
	

	@LanguageTemplate(value = "Stelle sicher, dass die Checkbox ^^ angehakt ist.")
	public void ensureCheckboxIsTicked(String checkBoxDisplayName)
	{
		boolean stateChanged;
		stateChanged = executableExample.getActiveGuiController().assureTickInCheckBox(checkBoxDisplayName);
		
		if (stateChanged) {
			executableExample.addReportMessage("Die Checkbox <b>" + checkBoxDisplayName + "</b> wurde angehakt.");
		} else {
			executableExample.addCommentToReport("Die Checkbox <b>" + checkBoxDisplayName + "</b> war bereits angehakt.");
		}
	}
	
	@LanguageTemplate(value = "Stelle sicher, dass die Checkbox ^^ nicht angehakt ist.")
	public void ensureCheckboxIsUnticked(String checkBoxDisplayName)
	{
		boolean stateChanged;
		stateChanged = executableExample.getActiveGuiController().assureNoTickInCheckBox(checkBoxDisplayName);
		
		if (stateChanged) {
			executableExample.addReportMessage("Der Haken in Checkbox <b>" + checkBoxDisplayName + "</b> wurde entfernt.");
		} else {
			executableExample.addCommentToReport("Die Checkbox <b>" + checkBoxDisplayName + "</b> war bereits ohne Haken.");
		}
	}
	
	
	@LanguageTemplate(value = "Warte bis der Dialog ^^ nicht mehr angezeigt wird.")
	public void waitForDialogToClose(String dialogTitle)
	{
		executableExample.getActiveGuiController().waitForDialogToClose(dialogTitle);
		setCurrentPage(mainFramePageObject);
	}	
	
	
	@LanguageTemplate(value = "Klicke im Dialog ^^ den Button ^^.")
	public void clickButtonInDialog(String dialogName, String buttonName)
	{
		((SwingGuiControl)executableExample.getActiveGuiController()).clickDialogButton(dialogName, buttonName);
	}	
}
