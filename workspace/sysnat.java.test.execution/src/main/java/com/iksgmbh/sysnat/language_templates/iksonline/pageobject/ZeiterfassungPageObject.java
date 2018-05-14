package com.iksgmbh.sysnat.language_templates.iksonline.pageobject;

import com.iksgmbh.sysnat.TestCase;
import com.iksgmbh.sysnat.language_templates.PageObject;
import com.iksgmbh.sysnat.utils.SysNatConstants.GuiType;

/**
 * Maps names of GUI elements visible to the user to technical field IDs
 * and calls the gui controller to execute an action. 
 * 
 * @author Reik Oberrath
 */
public class ZeiterfassungPageObject implements PageObject
{	
	private static final String REPORT_FIELD_ID = "editForm:reportPrjNr";
	private TestCase testCase;

	public ZeiterfassungPageObject(TestCase aTestCase) {
		this.testCase = aTestCase;
	}

	@Override
	public String getPageName() {
		return getClass().getSimpleName().replaceAll("PageObject", "");
	}

	@Override
	public void enterTextInField(String fieldName, String value) 
	{
		if ("Tag".equals(fieldName)) {
			testCase.inputText("editForm:reportDateInput", value);
		} else if ("Beginn".equals(fieldName)) {
			if (value.length() < 3) {
				value += ":00"; 
			}
			testCase.inputText("editForm:reportVon", value);
		} else if ("Ende".equals(fieldName)) {
			if (value.length() < 3) {
				value += ":00"; 
			}
			testCase.inputText("editForm:reportBis", value);
		} else if ("Pause".equals(fieldName)) {
			testCase.inputText("editForm:reportPause", value);
		} else if ("Projekt".equals(fieldName)) {
			String textForElement = testCase.getTextForElement(REPORT_FIELD_ID);
			if (textForElement.trim().length() == 0) {
				testCase.inputText(REPORT_FIELD_ID, value);
			} else {
				if ( ! textForElement.equals(value) ) {
					throw new RuntimeException("This input field does not allow input by selenium command sendKey. "
							                    + "Use this field as selection/Combobox!");
				}
			}
		} else if ("Ort".equals(fieldName)) {
			testCase.inputText("editForm:reportPrjOrtId", value);
		} else if ("TK-Schlüssel".equals(fieldName)) {
			testCase.inputText("editForm:reportSch", value);
		} else if ("Bemerkung".equals(fieldName)) {
			testCase.inputText("editForm:reportBemerk", value);
		} else {			
			throwUnsupportedGuiEventException(GuiType.TextField, fieldName);
		}
	}

	@Override
	public void clickButton(String buttonName) 
	{
		if ("Speichern".equals(buttonName)) {
			testCase.clickButton("editForm:save");
		} else if ("?".equals(buttonName)) {
				testCase.clickButton("editForm:showProjectDetails");
		} else {	
			throwUnsupportedGuiEventException(GuiType.Button, buttonName);
		}
	}

	@Override
	public void choose(String fieldName, String value) 
	{
		if ("Projekt".equals(fieldName)) {
			testCase.chooseFromComboBoxByValue(REPORT_FIELD_ID, value);
		} else {	
			throwUnsupportedGuiEventException(GuiType.ComboBox, fieldName);
		}
	}

	public boolean isProjectFieldTextInputField() {
		return testCase.getTagName(REPORT_FIELD_ID).equals("input");
	}

	@Override
	public String getText(String identifierOfGuiElementToRead) {
		throwUnsupportedGuiEventException(GuiType.ElementToReadText, identifierOfGuiElementToRead);
		return null;
	}
	
}
