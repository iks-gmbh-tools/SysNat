package com.iksgmbh.sysnat.language_templates.helloworldspringboot.pageobject;

import com.iksgmbh.sysnat.TestCase;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.GuiType;
import com.iksgmbh.sysnat.language_templates.PageObject;

/**
 * Maps names of GUI elements visible to the user to technical field IDs
 * and calls the gui controller to execute an action. 
 * 
 * @author Reik Oberrath
 */
public class ErrorPageObject implements PageObject
{	
	private TestCase testCase;

	public ErrorPageObject(TestCase aTestCase) {
		this.testCase = aTestCase;
	}

	@Override
	public String getPageName() {
		return getClass().getSimpleName().replaceAll("PageObject", "").replaceAll("HelloWorld", "");
	}

	@Override
	public void clickButton(String buttonName) {
		throwUnsupportedGuiEventException(GuiType.Button, buttonName);
	}

	@Override
	public void enterTextInField(String fieldName, String value) {
		throwUnsupportedGuiEventException(GuiType.TextField, fieldName);
	}

	@Override
	public String getText(String identifierOfGuiElementToRead) 
	{
		if ("ErrorMessage".equals(identifierOfGuiElementToRead)) {
			return testCase.getTextForElement("/html/body/form/table/tbody/tr/td[2]");
		} else {	
			throwUnsupportedGuiEventException(GuiType.ElementToReadText, identifierOfGuiElementToRead);
			return null;
		}
	}

	@Override
	public void chooseForCombobox(String fieldName, String value) {
		throwUnsupportedGuiEventException(GuiType.ComboBox, fieldName);
	}

	@Override
	public boolean isCurrentlyDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}
