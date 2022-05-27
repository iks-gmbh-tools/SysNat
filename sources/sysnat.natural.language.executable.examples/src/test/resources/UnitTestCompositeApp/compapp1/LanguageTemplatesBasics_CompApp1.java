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
package com.iksgmbh.sysnat.language_templates.testapp1;

import java.util.*;

import com.iksgmbh.sysnat.*;
import com.iksgmbh.sysnat.annotation.*;
import com.iksgmbh.sysnat.common.exception.*;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.GuiType;
import com.iksgmbh.sysnat.guicontrol.GuiControl;
import com.iksgmbh.sysnat.guicontrol.impl.*;
import com.iksgmbh.sysnat.language_templates.LanguageTemplateBasics.PageChangeEvent.EventType;
import com.iksgmbh.sysnat.language_templates.*;
import com.iksgmbh.sysnat.language_templates.testapp1.*;
import com.iksgmbh.sysnat.language_templates.testapp1.pageobject.*;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.ApplicationLoginParameter;

/**
 * Implements all Natural Language instructions specific to TestApp1.
 * Some common instructions are available from the parent class.
 */
@LanguageTemplateContainer
public class LanguageTemplatesBasics_TestApp1 extends LanguageTemplateBasics
{
	private MainPageObject mainPageObject;

	public LanguageTemplatesBasics_TestApp1(ExecutableExample anXX) 
	{
		this.executableExample = anXX;
		this.executionInfo = ExecutionRuntimeInfo.getInstance();
		this.mainPageObject = createAndRegister(MainPageObject.class, executableExample);
		this.setCurrentPage(mainPageObject);  // if not static use: setCurrentPage(findCurrentPage());

		// define page change events
	}


	//##########################################################################################
	//                       I N T E R F A C E    M E T H O D S
	//##########################################################################################

	@Override
	public void doLogin(Map<ApplicationLoginParameter,String> startParameter)
	{ /* test application needs no login */ }

	@Override
    public void doLogout() {
        // executableExample.clickMenuHeader("Logout");
        // findCurrentPage();
    }

	@Override
	public boolean isLoginPageVisible() {
		return false;
	}

	@Override
	public boolean isStartPageVisible() {
		return mainPageObject.isCurrentlyDisplayed();
	}

	@Override
	public void gotoStartPage() {
		// Implement if needed
	}
	
	
	//##########################################################################################
	//                   L A N G U A G E   T E M P L A T E    M E T H O D S
	//##########################################################################################


	// ############################  Click instructions  ###################################

	@LanguageTemplate(value = "Click menu item ^^.")
	public void clickMainMenuItem(final String mainMenuItem) {
		super.clickMainMenuItem(mainMenuItem);
	}

	@LanguageTemplate(value = "Click button ^^.")
	public void clickButton(String buttonName) {
		List<PageObject> possiblePages = new ArrayList<>();
		// add possiblePages if known
		PageObject newPage = super.clickButtonToChangePage(buttonName, possiblePages , 0);
		setCurrentPage(newPage);
	}

	@LanguageTemplate(value = "In Dialog ^^ click button ^^.")
	public void clickDialogButton(String dialogName, String buttonName) {
		super.clickDialogButton(dialogName, buttonName);
	}

	@LanguageTemplate(value = "Click link ^^.")
	protected void clickLink(String linkText) {
		super.clickLink(linkText);
	}

	// #####################  Textfield and Combobox instructions  ###########################

	@LanguageTemplate(value = "Enter ^^ in field ^^.")
	public void enterTextInField(String valueCandidate, String fieldName) {
		super.enterTextInField(valueCandidate, fieldName);
	}

	@LanguageTemplate(value = "Select ^^ in box ^^.")
	public void chooseInCombobox(String valueCandidate, String fieldName) {
		super.chooseInCombobox(valueCandidate, fieldName);
	}

	// ##########################  checkbox instructions  ###################################

	@LanguageTemplate(value = "Tick checkbox ^^.")
	public void ensureCheckboxIsTicked(String checkBoxDisplayName) {
		super.ensureCheckboxIsTicked(checkBoxDisplayName);
	}

	@LanguageTemplate(value = "Tick checkbox ^^ if ^^=^^.")
	public void tickCheckboxForCondition(String checkBoxDisplayName, String actual, String expected) {
		super.tickCheckboxForCondition(checkBoxDisplayName, actual, expected);
	}

	@LanguageTemplate(value = "Untick checkbox ^^.")
	public void ensureCheckboxIsUNticked(String checkBoxDisplayName) {
		super.ensureCheckboxIsUNticked(checkBoxDisplayName);
	}

	// ############################  M I S C  ###################################

	@LanguageTemplate(value = "Is the displayed text ^^ equal to ^^?")
	public void isTextDislayed(final String guiElementToRead, final String valueCandidate) {
		super.isTextDislayed(guiElementToRead, valueCandidate);
	}

}