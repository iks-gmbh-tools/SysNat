package com.iksgmbh.sysnat.language_templates.icf.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.iksgmbh.sysnat.TestCase;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.GuiType;
import com.iksgmbh.sysnat.language_templates.PageObject;

/**
 * Maps names of GUI elements visible to the user to technical field IDs
 * and calls the gui controller to execute an action. 
 * 
 * @author Reik Oberrath
 */
public class WizardPage1_PageObject implements PageObject
{	
	private static final int WIZARD_PAGE_NUMBER = 1;

	private TestCase testCase;

	public WizardPage1_PageObject(TestCase aTestCase) {
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
		if ("Möglichkeiten >>".equalsIgnoreCase(buttonName)) {
			testCase.clickElement("_eventId_next");
		} else if ("Weiter".equalsIgnoreCase(buttonName)) {
				testCase.clickElement("_eventId_next");
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
	public boolean isCurrentlyDisplayed() 
	{
		final WebDriver webDriver = (WebDriver) testCase.getGuiController().getWebDriver();
		final WebElement webElement = webDriver.findElement(By.xpath("//div[@id='ProzessNavigation']/table/tbody/tr/td/table/tbody/tr/td[" + (WIZARD_PAGE_NUMBER+1) + "]/img"));
		return webElement.getAttribute("src").contains("/PBV-ICF/PBLebenAktiv/img/status/" + WIZARD_PAGE_NUMBER + "on.gif");
	}
	
}
