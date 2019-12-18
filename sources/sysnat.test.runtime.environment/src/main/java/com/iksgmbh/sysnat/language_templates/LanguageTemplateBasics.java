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
import java.util.Date;
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
			                                             final ExecutableExample anExecutableExample) 
	{
		try {			
			Constructor<T> constructor = aClass.getConstructor(ExecutableExample.class);
			PageObject pageObject = constructor.newInstance(anExecutableExample);
			knownPageObjects.add(pageObject);
			return (T)pageObject;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Searches which current page is displayed.
	 * ATTENSION: Avoid this time consuming method with currentPage=nulll by setting the currentPage 
	 *            in the code context specific. This speeds test execution greatly!
	 *            
	 * @return current page displayed
	 */
	protected PageObject getCurrentPage()
	{
		if (currentPage == null) 
		{
			Optional<PageObject> result = knownPageObjects.stream()
			                                              .filter(page -> page.isCurrentlyDisplayed())
			                                              .findFirst();
			if (result.isPresent()) {
				setCurrentPage(result.get());
			} else {
				throw new SysNatException("The current page is not yet known to the LanguageTemplateProvider.");
			}
		}
		
		return currentPage;
	}
	
	protected void setCurrentPage(PageObject aPageObject) {
		this.currentPage = aPageObject;
	}
	
	// #################################################################################
	//                  standard implementations for GUI handling
	// #################################################################################
	
	protected void clickButton(String buttonName) 
	{
		getCurrentPage().clickButton(buttonName);
		executableExample.addReportMessage("Button <b>" + buttonName + "</b> has beed clicked.");
	}
	
    /**
     * Waits until after button click until one of the possiblePages is available.
     * After a default number of seconds without availablity of any page,
     * the test executions fails.
     * 
     * @param buttonName
     * @param possiblePages
     * @param indexOfExpectedPageInList
     * @return pageObject after the button click
     */
	protected PageObject clickButtonToChangePage(final String buttonName,
	                                             final List<PageObject> possiblePages,
	                                             final int indexOfExpectedPageInList)
	{
		if (possiblePages == null) {
			return null;
		}
		
		clickButton(buttonName);

		boolean goOn = true;
		Optional<PageObject> optionalNextPage = null;
		long startTime = new Date().getTime();
		long waitPeriodInMillis = 0;
		
		while ( goOn ) 
		{
			executableExample.sleep(executionInfo.getMillisToWaitForAvailabilityCheck());
			optionalNextPage = possiblePages.stream().filter(page -> page.isCurrentlyDisplayed()).findFirst();
			waitPeriodInMillis = (new Date().getTime() - startTime);
			goOn = ! optionalNextPage.isPresent() || waitPeriodInMillis < executionInfo.getDefaultGuiElementTimeout();
		}

		if ( ! optionalNextPage.isPresent() ) 
		{
			executableExample.failWithMessage("Erwartete Seite <b>" 
			        + possiblePages.get(indexOfExpectedPageInList).getPageName()
			        + "</b> wird nicht angezeigt. Seitenwechsel wurde nach " + waitPeriodInMillis
			        + " Sekunden abgebrochen.");
		}

		return optionalNextPage.get();
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
		String question = "Is the expected text (" + expectedText + ") equal to the actually displayed one (" + actualText + ")" + QUESTION_IDENTIFIER;
		executableExample.answerQuestion(question, ok);	
	}
	
	protected void enterTextInField(String valueCandidate, String fieldName)
	{
		String value = executableExample.getTestDataValue(valueCandidate);
		getCurrentPage().enterTextInField(fieldName, value);
		executableExample.addReportMessage("In field <b>" + fieldName + "</b> the value <b>" + value + "</b> has been entered.");
	}
	
	protected void clickLink(String linkText)
    {
	   linkText = executableExample.getTestDataValue(linkText);
	   PageObject currentlyDisplayedPage = getCurrentPage();

	   try {
	      currentlyDisplayedPage.clickLink(linkText);
	      executableExample.addReportMessage("Der Link <b>" + linkText + "</b> wurde geklickt.");
	   } catch (Exception e) {
	        throw new SysNatException("Der Link <b>" + linkText + "</b> ist nicht bekannt oder konnte nicht geklickt werden.");
	   }
    }
  

	
	// #################################################################################
	//                          P U B L I C    M E T H O D S 
	// #################################################################################
	
	
	public void resetCurrentPage() {
		setCurrentPage(null);
	}
	
	public ExecutableExample getExecutableExample() {
		return executableExample;
	}
}