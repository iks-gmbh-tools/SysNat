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
public class WizardPage6_PageObject implements PageObject
{	
	private static final int WIZARD_PAGE_NUMBER = 6;

	private TestCase testCase;

	public WizardPage6_PageObject(TestCase aTestCase) {
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
		if ("Weiter".equalsIgnoreCase(buttonName)) {
			testCase.clickElement("_eventId_next");
		} else if ("Zusammenfassung drucken".equalsIgnoreCase(buttonName)) {
			testCase.clickElement("_eventId_druckenZusammenfassung");
		} else if ("mit AVB drucken".equalsIgnoreCase(buttonName)) {
				testCase.clickElement("confirmButton1");
		} else if ("Antrag drucken".equalsIgnoreCase(buttonName)) {
			testCase.clickElement("_eventId_druckenAntrag");
		} else {	
			throwUnsupportedGuiEventException(GuiType.Button, buttonName);
		}
	}

	@Override
	public void chooseForCombobox(String fieldName, String value) 
	{
		throwUnsupportedGuiEventException(GuiType.ComboBox, fieldName);
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
	
	public void chooseRadioButtons(String selection, String option) 
	{
		if ("Option Anwesenheit".equalsIgnoreCase(selection)) 
		{
			if ("ja".equalsIgnoreCase(option)) {
				testCase.clickElement("abschlussStatus.allePersonenAnwesend.ja");
			} else if ("nein".equalsIgnoreCase(option)) {				
				testCase.clickElement("abschlussStatus.allePersonenAnwesend.nein");
			} else {
				throwUnsupportedGuiEventException(GuiType.RadioButtonSelection, selection);
			}
		} 
		else if ("Option Unterschrift".equalsIgnoreCase(selection)) 
		{
			if ("ja".equalsIgnoreCase(option)) {
				testCase.clickElement("abschlussStatus.gedrucktUndUnterschrieben1");
			} else if ("nein".equalsIgnoreCase(option)) {				
				testCase.clickElement("abschlussStatus.gedrucktUndUnterschrieben2");
			} else {
				throwUnsupportedGuiEventException(GuiType.RadioButtonSelection, selection);
			}			
		} else {	
			throwUnsupportedGuiEventException(GuiType.RadioButtonSelection, selection);
		}
	}
	
	
}
