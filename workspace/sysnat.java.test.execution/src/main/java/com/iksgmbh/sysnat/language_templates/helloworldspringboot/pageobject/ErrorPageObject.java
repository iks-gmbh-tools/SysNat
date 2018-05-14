package com.iksgmbh.sysnat.language_templates.helloworldspringboot.pageobject;

import com.iksgmbh.sysnat.TestCase;
import com.iksgmbh.sysnat.language_templates.PageObject;
import com.iksgmbh.sysnat.utils.SysNatConstants.GuiType;

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
	public void choose(String fieldName, String value) {
		throwUnsupportedGuiEventException(GuiType.ComboBox, fieldName);
	}

	@Override
	public String getText(String identifierOfGuiElementToRead) 
	{
		if ("greeting".equals(identifierOfGuiElementToRead)) {
			return testCase.getTextForElement("/html/body/p");
		} else {	
			throwUnsupportedGuiEventException(GuiType.ElementToReadText, identifierOfGuiElementToRead);
			return null;
		}
	}
}
