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
package com.iksgmbh.sysnat.language_templates.sysnat;

import static com.iksgmbh.sysnat.common.utils.SysNatConstants.QUESTION_IDENTIFIER;

import java.util.Map;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.ApplicationLoginParameter;
import com.iksgmbh.sysnat.language_templates.LanguageTemplateBasics;
import com.iksgmbh.sysnat.language_templates.sysnat.pageobject.MainFramePageObject;

/**
 * Contains the basic language templates for the test application SysNat.
 * 
 * @author Reik Oberrath
 */
@LanguageTemplateContainer
public class LanguageTemplatesBasics_SysNat extends LanguageTemplateBasics
{	
	private MainFramePageObject mainFramePageObject;
	
	public LanguageTemplatesBasics_SysNat(ExecutableExample anXX) 
	{
		this.executableExample = anXX;
		this.executionInfo = ExecutionRuntimeInfo.getInstance();		
		this.mainFramePageObject = createAndRegister(MainFramePageObject.class, executableExample);
		this.findCurrentPage();
	}

	private String getPageName() {
		return this.executableExample.getCurrentPage().getPageName();
	}
	
	//##########################################################################################
	//                       I N T E R F A C E    M E T H O D S
	//##########################################################################################
	
	@Override public void doLogin(final Map<ApplicationLoginParameter,String> startParameter)	{ /* nothing to do */}
	@Override public void doLogout() { /* nothing to do */}
    
	@Override
	public boolean isLoginPageVisible() {
		return false;
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
				// TODO clickMainMenuItem("?"); // goto to Standard Start position for all test cases
			}
		} catch (Exception e) {
			// ignore
		}
	}
	
	
	//##########################################################################################
	//                   L A N G U A G E   T E M P L A T E    M E T H O D S
	//##########################################################################################

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
		try {
			executableExample.clickMenuHeader(mainMenuItem);
			executableExample.addReportMessage("Menu item <b>" + mainMenuItem + "</b> has been clicked.");
		} catch (Exception e) {
			executableExample.failWithMessage("Unknown menu item <b>" + mainMenuItem + "</b>.");  // TODO brauch man das ???
		}

	}

	@LanguageTemplate(value = "Enter ^^ in text field ^^.")
	@LanguageTemplate(value = "Gebe ^^ ins Textfeld ^^ ein.")
	public void enterTextInField(String valueCandidate, String fieldName) 
	{
		final String value = executableExample.getTestDataValue(valueCandidate);
		getCurrentPage().enterTextInTextField(fieldName, value);
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
		
}