package com.iksgmbh.sysnat.language_templates.icf.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.iksgmbh.sysnat.TestCase;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.GuiType;
import com.iksgmbh.sysnat.language_templates.PageObject;

/**
 * Maps names of GUI elements visible to the user to technical field IDs
 * and calls the gui controller to execute an action. 
 * 
 * @author Reik Oberrath
 */
public class ResultPageObject implements PageObject
{	
	@SuppressWarnings("unused")
	private static final int WIZARD_PAGE_NUMBER = 6;

	private TestCase testCase;

	public ResultPageObject(TestCase aTestCase) {
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
		throwUnsupportedGuiEventException(GuiType.Button, buttonName);
	}

	@Override
	public void chooseForCombobox(String fieldName, String value) 
	{
		throwUnsupportedGuiEventException(GuiType.ComboBox, fieldName);
	}

	@Override
	public String getText(String identifierOfGuiElementToRead) {
		if (identifierOfGuiElementToRead.equals("Textarea")) {
			return ((WebDriver)testCase.getGuiController().getWebDriver()).findElement(By.xpath("//textarea")).getText();
		} else {
			throwUnsupportedGuiEventException(GuiType.ElementToReadText, identifierOfGuiElementToRead);
		}
		return null;
	}

	@Override
	public boolean isCurrentlyDisplayed() 
	{
		try {
			((WebDriver)testCase.getGuiController().getWebDriver()).findElement(By.xpath("//textarea"));
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public void chooseRadioButtons(String selection, String option) 
	{
		throwUnsupportedGuiEventException(GuiType.RadioButtonSelection, selection);
	}
	
	
}
