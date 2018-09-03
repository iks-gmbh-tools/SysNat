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
package com.iksgmbh.sysnat.language_templates.homepageiks.pageobject;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.GuiType;
import com.iksgmbh.sysnat.language_templates.PageObject;

/**
 * Maps names of GUI elements visible to the user to technical field IDs
 * and calls the gui controller to execute an action. 
 * 
 * @author Reik Oberrath
 */
public class ZeiterfassungPageObject implements PageObject
{	
	private static final String REPORT_FIELD_ID = "editForm:reportPrjNr";
	private ExecutableExample executableExample;

	public ZeiterfassungPageObject(ExecutableExample aExecutableExample) {
		this.executableExample = aExecutableExample;
	}

	@Override
	public String getPageName() {
		return getClass().getSimpleName().replaceAll("PageObject", "");
	}

	@Override
	public void enterTextInField(String fieldName, String value) 
	{
		if ("Tag".equals(fieldName)) {
			executableExample.inputText("editForm:reportDateInput", value);
		} else if ("Beginn".equals(fieldName)) {
			if (value.length() < 3) {
				value += ":00"; 
			}
			executableExample.inputText("editForm:reportVon", value);
		} else if ("Ende".equals(fieldName)) {
			if (value.length() < 3) {
				value += ":00"; 
			}
			executableExample.inputText("editForm:reportBis", value);
		} else if ("Pause".equals(fieldName)) {
			executableExample.inputText("editForm:reportPause", value);
		} else if ("Projekt".equals(fieldName)) {
			String textForElement = executableExample.getTextForElement(REPORT_FIELD_ID);
			if (textForElement.trim().length() == 0) {
				executableExample.inputText(REPORT_FIELD_ID, value);
			} else {
				if ( ! textForElement.equals(value) ) {
					throw new RuntimeException("This input field does not allow input by selenium command sendKey. "
							                    + "Use this field as selection/Combobox!");
				}
			}
		} else if ("Ort".equals(fieldName)) {
			executableExample.inputText("editForm:reportPrjOrtId", value);
		} else if ("TK-Schlüssel".equals(fieldName)) {
			executableExample.inputText("editForm:reportSch", value);
		} else if ("Bemerkung".equals(fieldName)) {
			executableExample.inputText("editForm:reportBemerk", value);
		} else {			
			throwUnsupportedGuiEventException(GuiType.TextField, fieldName);
		}
	}

	@Override
	public void clickButton(String buttonName) 
	{
		if ("Speichern".equals(buttonName)) {
			executableExample.clickButton("editForm:save");
		} else if ("?".equals(buttonName)) {
				executableExample.clickButton("editForm:showProjectDetails");
		} else {	
			throwUnsupportedGuiEventException(GuiType.Button, buttonName);
		}
	}

	public boolean isProjectFieldTextInputField() {
		return executableExample.getTagName(REPORT_FIELD_ID).equals("input");
	}

	@Override
	public String getText(String identifierOfGuiElementToRead) {
		throwUnsupportedGuiEventException(GuiType.ElementToReadText, identifierOfGuiElementToRead);
		return null;
	}

	@Override
	public void chooseForCombobox(String fieldName, String value) {
		if ("Projekt".equals(fieldName)) {
			executableExample.chooseFromComboBoxByValue(REPORT_FIELD_ID, value);
		} else {	
			throwUnsupportedGuiEventException(GuiType.ComboBox, fieldName);
		}
	}

	@Override
	public boolean isCurrentlyDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
}