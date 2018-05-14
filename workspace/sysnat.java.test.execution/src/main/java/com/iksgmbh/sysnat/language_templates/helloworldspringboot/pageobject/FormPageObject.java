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
public class FormPageObject implements PageObject
{	
	private TestCase testCase;

	public FormPageObject(TestCase aTestCase) {
		this.testCase = aTestCase;
	}

	@Override
	public String getPageName() {
		return getClass().getSimpleName().replaceAll("PageObject", "").replaceAll("HelloWorld", "");
	}

	@Override
	public void enterTextInField(String fieldName, String value) 
	{
		if ("Name".equals(fieldName)) {
			testCase.inputText("name", value);
		} else {			
			throwUnsupportedGuiEventException(GuiType.TextField, fieldName);
		}
	}

	@Override
	public void clickButton(String buttonName) 
	{
		if ("Greet".equals(buttonName)) {
			testCase.clickButton("btnGreet");
		} else {	
			throwUnsupportedGuiEventException(GuiType.Button, buttonName);
		}
	}

	@Override
	public void choose(String fieldName, String value) 
	{
		if ("Greeting".equals(fieldName)) {
			testCase.chooseFromComboBoxByValue("greeting", value);
		} else {	
			throwUnsupportedGuiEventException(GuiType.ComboBox, fieldName);
		}
	}

	@Override
	public String getText(String identifierOfGuiElementToRead) {
		throwUnsupportedGuiEventException(GuiType.ElementToReadText, identifierOfGuiElementToRead);
		return null;
	}
	
}
