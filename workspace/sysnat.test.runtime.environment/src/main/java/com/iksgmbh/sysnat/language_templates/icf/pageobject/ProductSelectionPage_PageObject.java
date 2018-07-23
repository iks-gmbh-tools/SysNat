package com.iksgmbh.sysnat.language_templates.icf.pageobject;

import com.iksgmbh.sysnat.TestCase;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.GuiType;
import com.iksgmbh.sysnat.language_templates.PageObject;

/**
 * Maps names of GUI elements visible to the user to technical field IDs
 * and calls the gui controller to execute an action. 
 * 
 * @author Reik Oberrath
 */
public class ProductSelectionPage_PageObject implements PageObject
{	
	private TestCase testCase;

	public ProductSelectionPage_PageObject(TestCase aTestCase) {
		this.testCase = aTestCase;
	}

	@Override
	public String getPageName() {
		return getClass().getSimpleName().replaceAll("_PageObject", "");
	}

	@Override
	public void enterTextInField(String fieldName, String value) 
	{
		throwUnsupportedGuiEventException(GuiType.TextField, fieldName);
	}

	@Override
	public void clickButton(String buttonName) 
	{
		if ("Anfrage senden".equalsIgnoreCase(buttonName)) {
			testCase.clickButton("submitButton1");
		} else {	
			throwUnsupportedGuiEventException(GuiType.Button, buttonName);
		}
	}

	@Override
	public void chooseForCombobox(String fieldName, String value) 
	{
		if ("aktionskennzeichen".equalsIgnoreCase(fieldName)) {
			testCase.chooseFromComboBoxByValue("request.produktaktionskennzeichen", value);
		} else {	
			throwUnsupportedGuiEventException(GuiType.ComboBox, fieldName);
		}
	}

	@Override
	public String getText(String identifierOfGuiElementToRead) {
		throwUnsupportedGuiEventException(GuiType.ElementToReadText, identifierOfGuiElementToRead);
		return null;
	}

	@Override
	public boolean isCurrentlyDisplayed() {
		return testCase.isElementReadyToUse("request.produktaktionskennzeichen");
	}

}
