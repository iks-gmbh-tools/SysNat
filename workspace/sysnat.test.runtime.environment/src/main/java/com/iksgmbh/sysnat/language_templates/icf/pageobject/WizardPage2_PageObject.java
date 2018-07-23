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
public class WizardPage2_PageObject implements PageObject
{	
	private static final int WIZARD_PAGE_NUMBER = 2;

	private TestCase testCase;

	public WizardPage2_PageObject(TestCase aTestCase) {
		this.testCase = aTestCase;
	}

	@Override
	public String getPageName() {
		return getClass().getSimpleName().replaceAll("_PageObject", "");
	}

	@Override
	public void enterTextInField(String fieldName, String value) 
	{
		if ("monatlicher Beitrag".equalsIgnoreCase(fieldName)) {
			testCase.inputText("beitragMonatlich", value);
		} else if ("Frei verfügbares monatliches Einkommen".equalsIgnoreCase(fieldName)) {
			testCase.inputText("beratung.freiVerfuegbaresEinkommen", value);
		} else if ("Einmalbeitrag".equalsIgnoreCase(fieldName)) {
			testCase.inputText("beitragEinmal", value);
		} else if ("Geburtsdatum".equalsIgnoreCase(fieldName)) {
			testCase.inputText("versichertePerson.person.geburtsdatum", value);
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
	
	public void chooseRadioButtons(String selection, String option) 
	{
		if ("Sterbegeldversicherung".equalsIgnoreCase(selection)) 
		{
			if ("ja".equalsIgnoreCase(option)) {
				testCase.clickElement("beratung.versicherungVorhanden1");
			} else if ("nein".equalsIgnoreCase(option)) {				
				testCase.clickElement("beratung.versicherungVorhanden2");
			} else {
				throwUnsupportedGuiEventException(GuiType.RadioButtonSelection, selection);
			}
		} else if ("Jetzt gewünschter Beitrag".equalsIgnoreCase(selection)) 
		{
			if ("monatlicher Beitrag".equalsIgnoreCase(option)) {
				testCase.clickElement("beratung.versicherungVorhanden1");
			} else if ("Einmalbeitrag".equalsIgnoreCase(option)) {				
				testCase.clickElement("beratung.versicherungVorhanden2");
			} else {
				throwUnsupportedGuiEventException(GuiType.RadioButtonSelection, selection);
			}			
		} else {	
			throwUnsupportedGuiEventException(GuiType.RadioButtonSelection, selection);
		}
	}
	
	
}
