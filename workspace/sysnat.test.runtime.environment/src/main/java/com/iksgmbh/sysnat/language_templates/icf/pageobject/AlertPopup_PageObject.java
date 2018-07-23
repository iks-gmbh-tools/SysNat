package com.iksgmbh.sysnat.language_templates.icf.pageobject;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.GuiType;
import com.iksgmbh.sysnat.guicontrol.SeleniumGuiController;
import com.iksgmbh.sysnat.language_templates.PageObject;

/**
 * Maps names of GUI elements visible to the user to technical field IDs
 * and calls the gui controller to execute an action. 
 * 
 * @author Reik Oberrath
 */
public class AlertPopup_PageObject implements PageObject
{	
	private static final String XPATH_TO_CLOSE_POPUP_BUTTON = "//div[@class='pure-button button closeOverlayAlert']";
	
	private ExecutableExample testCase;

	public AlertPopup_PageObject(ExecutableExample aTestCase) {
		this.testCase = aTestCase;
	}

	@Override
	public String getPageName() {
		return getClass().getSimpleName().replaceAll("_PageObject", "");
	}

	@Override
	public void clickButton(String buttonName) 
	{
		if ("Schließen".equalsIgnoreCase(buttonName)) {
			testCase.clickElement(XPATH_TO_CLOSE_POPUP_BUTTON);
		} else {	
			throwUnsupportedGuiEventException(GuiType.Button, buttonName);
		}
	}

	@Override
	public String getText(String identifierOfGuiElementToRead) {
		throwUnsupportedGuiEventException(GuiType.ElementToReadText, identifierOfGuiElementToRead);
		return null;
	}

	@Override
	public boolean isCurrentlyDisplayed() {
		return ((SeleniumGuiController)testCase.getGuiController()).isXPathAvailable(XPATH_TO_CLOSE_POPUP_BUTTON);
	}

	@Override
	public void enterTextInField(String fieldName, String value) {
		throwUnsupportedGuiEventException(GuiType.TextField, fieldName);	}

	@Override
	public void chooseForCombobox(String fieldName, String value) {
		throwUnsupportedGuiEventException(GuiType.ComboBox, fieldName);	
	}	
	
}
