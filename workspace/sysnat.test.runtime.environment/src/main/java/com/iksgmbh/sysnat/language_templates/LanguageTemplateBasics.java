/*
 * Copyright 2018 IKS Gesellschaft fuer Informations- und Kommunikationssysteme mbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iksgmbh.sysnat.language_templates;

import static com.iksgmbh.sysnat.common.utils.SysNatConstants.QUESTION_IDENTIFIER;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.WebLoginParameter;

/**
 * Contains an description which minimum implementation is needed for a specific 
 * SysNat test application.
 * 
 * In addition, it provides some methods for GUI handling to be used as default implementations 
 * in sub classes.
 *
 * @author Reik Oberrath
 */
public abstract class LanguageTemplateBasics
{	
	protected List<PageObject> knownPageObjects = new ArrayList<>();
	protected PageObject currentPage;
	protected ExecutableExample executableExample;
	protected ExecutionRuntimeInfo executionInfo;
	
	// abstract methods
	public abstract boolean isLoginPageVisible();
	
	public abstract void doLogin(HashMap<WebLoginParameter,String> startParameter);
	
	public abstract void doLogout();

	/**
	 * Returns true if the page is visible
	 * that is displayed after successfull login
	 */
	public abstract boolean isStartPageVisible();

	/**
	 * Used to reset the GUI after each test in order to assure that the next test
	 * will start from same initial point which the page displayed after login.
	 * To do this, popups may be closed that remain evenually open.
	 */
	public abstract void gotoStartPage();
	
	
	@SuppressWarnings("unchecked")
	protected <T extends PageObject> T createAndRegister(final Class<T> aClass, 
			                                             final LanguageTemplateBasics aLanguageTemplateBasics) 
	{
		try {			
			Constructor<T> constructor = aClass.getConstructor(LanguageTemplateBasics.class);
			PageObject pageObject = constructor.newInstance(aLanguageTemplateBasics);
			knownPageObjects.add(pageObject);
			return (T)pageObject;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected PageObject getCurrentPage()
	{
		if (currentPage == null) {
			Optional<PageObject> result = 
					knownPageObjects.stream()
			                        .filter(page -> page.isCurrentlyDisplayed())
			                        .findFirst();
			if (result.isPresent()) {
				currentPage = result.get();
			} else {
				throw new SysNatException("The current page is not yet known to the LanguageTemplateProvider.");
			}
		}
		
		return currentPage;
	}
	
	// #################################################################################
	//                  standard implementations for GUI handling
	// #################################################################################
	
	protected void clickButton(String buttonName) 
	{
		getCurrentPage().clickButton(buttonName);
		executableExample.addReportMessage("Button <b>" + buttonName + "</b> has beed clicked.");
	}
	
	protected void choose(String valueCandidate, String fieldName) 
	{
		final String value = executableExample.getTestDataValue(valueCandidate);
		getCurrentPage().chooseInCombobox(fieldName, value);
		executableExample.addReportMessage("For field <b>" + fieldName + "</b> value <b>" + value + "</b> has been selected.");
	}

	protected void isTextDislayed(final String guiElementToRead, final String valueCandidate) 
	{
		final String expectedText = executableExample.getTestDataValue(valueCandidate);
		final String actualText = getCurrentPage().getText(guiElementToRead);
		
		boolean ok = actualText.equals(expectedText);
		String question = "Is the expected text (" + expectedText + ") equals to the actually displayed one (" + actualText + ")" + QUESTION_IDENTIFIER;
		executableExample.answerQuestion(question, ok);	
	}
	
	protected void enterTextInField(String valueCandidate, String fieldName)
	{
		String value = executableExample.getTestDataValue(valueCandidate);
		getCurrentPage().enterTextInField(fieldName, value);
		executableExample.addReportMessage("In field <b>" + fieldName + "</b> the value <b>" + value + "</b> has been entered.");
	}

	
	// #################################################################################
	//                          P U B L I C    M E T H O D S 
	// #################################################################################
	
	
	public void resetCurrentPage() {
		currentPage = null;
	}
	
	public ExecutableExample getExecutableExample() {
		return executableExample;
	}
}