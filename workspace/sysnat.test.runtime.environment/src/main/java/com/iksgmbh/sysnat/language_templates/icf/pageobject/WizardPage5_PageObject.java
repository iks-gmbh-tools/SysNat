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
public class WizardPage5_PageObject implements PageObject
{	
	private static final int WIZARD_PAGE_NUMBER = 5;

	private TestCase testCase;

	public WizardPage5_PageObject(TestCase aTestCase) {
		this.testCase = aTestCase;
	}

	@Override
	public String getPageName() {
		return getClass().getSimpleName().replaceAll("_PageObject", "");
	}

	@Override
	public void enterTextInField(String fieldName, String value) 
	{
		if ("Geburtsort".equalsIgnoreCase(fieldName)) {
			testCase.inputText("beratung.vertrag.beitragszahlung.legitimation.geburtsort", value);
		} else if ("Ausweisnummer".equalsIgnoreCase(fieldName)) {
				testCase.inputText("beratung.vertrag.beitragszahlung.legitimation.ausweisnummer", value);
		} else if ("Gültig bis".equalsIgnoreCase(fieldName)) {
			testCase.inputText("beratung.vertrag.beitragszahlung.legitimation.gueltigBis", value);
		} else if ("Ausst. Behörde und Ort".equalsIgnoreCase(fieldName)) {
			testCase.inputText("beratung.vertrag.beitragszahlung.legitimation.ausstellendeBehoerde", value);
		} else {	
			throwUnsupportedGuiEventException(GuiType.TextField, fieldName);
		}
	}

	@Override
	public void clickButton(String buttonName) 
	{
		if ("Weiter".equalsIgnoreCase(buttonName)) {
			testCase.clickElement("_eventId_next");
		} else {	
			throwUnsupportedGuiEventException(GuiType.Button, buttonName);
		}
	}

	@Override
	public void chooseForCombobox(String fieldName, String value) 
	{
		if ("Art des Ausweises".equalsIgnoreCase(fieldName)) {
			testCase.chooseFromComboBoxByValue("beratung.vertrag.beitragszahlung.legitimation.ausweisart", value);
		} else if ("Staatsangehörigkeit".equalsIgnoreCase(fieldName)) {
				testCase.chooseFromComboBoxByValue("beratung.vertrag.beitragszahlung.legitimation.staatsangehoerigkeit", value);
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
	
	public void chooseRadioButtons(String selection, String option) 
	{
		throwUnsupportedGuiEventException(GuiType.RadioButtonSelection, selection);
	}
	
	
}
