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
import java.util.Map;
import java.util.Optional;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.ApplicationLoginParameter;
import com.iksgmbh.sysnat.language_templates.LanguageTemplateBasics.PageChangeEvent.EventType;

import dev.failsafe.internal.util.Assert;

/**
 * Contains a description which minimum implementation is needed for a specific 
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
	protected ExecutableExample executableExample;
	protected ExecutionRuntimeInfo executionInfo;
	protected HashMap<String, PageChangeEvent> pageChangeEvents = new HashMap<>();  // buttonText, new page

	// abstract methods
	public abstract boolean isLoginPageVisible();
	
	public abstract void doLogin(Map<ApplicationLoginParameter,String> startParameter);
	
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
	
	protected void addPageChangeEvent(PageChangeEvent event) {
		pageChangeEvents.put(event.uiElementIdentifierTrigger, event);
	}
	
	@SuppressWarnings("unchecked")
	protected <T extends PageObject> T createAndRegister(final Class<T> aClass, 
			                                             final ExecutableExample anExecutableExample) 
	{
		try {			
			Constructor<T> constructor = aClass.getConstructor(ExecutableExample.class, this.getClass().getSuperclass());
			PageObject pageObject = constructor.newInstance(anExecutableExample, this);
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
		if (executableExample.getCurrentPage() == null) 
		{
			executableExample.getActiveGuiController().getGuiHandle();  // init guihandle!
			Optional<PageObject> result = knownPageObjects.stream()
			                                              .filter(page -> page.isCurrentlyDisplayed())
			                                              .findFirst();
			if (result.isPresent()) {
				setCurrentPage(result.get());
			} else {
				throw new SysNatException("The current page is not yet supported "
						+ "by the LanguageTemplateProvider of test application <b>" + executionInfo.getTestApplicationName() + "</b>.");
			}
		}
		
		return executableExample.getCurrentPage();
	}
	
	protected void setCurrentPage(PageObject aPageObject) {
		this.executableExample.setCurrentPage(aPageObject);
	}
	
	protected int toInteger(String intAsString, String valueName) {
		try {
			return Integer.valueOf(intAsString);
		} catch (NumberFormatException e) {
			executableExample.failWithMessage("Der Wert <b>" + valueName + "</b> ist keine Ganzzahl (<b>" + intAsString + "</b>).");
			return Integer.MIN_VALUE;
		}
	}

	protected double toDouble(String doubleAsString, String valueName) {
		try {
			return Double.valueOf(doubleAsString);
		} catch (NumberFormatException e) {
			executableExample.failWithMessage("Der Wert <b>" + valueName + "</b> ist keine Kommazahl (<b>" + doubleAsString + "</b>).");
			return Integer.MIN_VALUE;
		}
	}
	
	public boolean checkPageChange(String uiElementIdentifier, PageChangeEvent.EventType type)
	{
		PageChangeEvent pageChangeEvent = pageChangeEvents.get(uiElementIdentifier);
		if (pageChangeEvent == null) return false;

		if (uiElementIdentifier.equals("Akte anlegen")) {
			System.err.println();
		}
		
		if (pageChangeEvent.currentPage != null) {
			if (pageChangeEvent.currentPage != this.getCurrentPage()) return false;	
		}
		boolean noPageChange = pageChangeEvent.type != null && type != null && pageChangeEvent.type != type;
		if (noPageChange) return false;
		
		setCurrentPage(pageChangeEvent.nextPage);
		
		if (pageChangeEvent.millisToWait > 0) {
			executableExample.sleep(pageChangeEvent.millisToWait);
		}
		
		if (pageChangeEvent.uiElementIdentifierWaitForElement != null && ! pageChangeEvent.uiElementIdentifierWaitForElement.isEmpty()) {
			executableExample.waitUntilElementIsAvailable(pageChangeEvent.uiElementIdentifierWaitForElement);
		}
		
		return true;
	}
	
	// #################################################################################
	//                  standard implementations for GUI handling
	// #################################################################################
	
	/**
	 * @param buttonName
	 * @return false if no page change is detected
	 */
	protected boolean clickButtonWithCheckPageChange(String buttonName) 
	{
		getCurrentPage().clickButton(buttonName);
		executableExample.addReportMessage("Button <b>" + buttonName + "</b> has beed clicked.");
		return checkPageChange(buttonName, EventType.ButtonClick);
	}
	
	protected void clickDialogButton(String dialogName, String buttonName) 
	{
		getCurrentPage().clickDialogButton(dialogName, buttonName);
		executableExample.addReportMessage("Button <b>" + buttonName + "</b> in dialog <b>" + dialogName + "</b> has beed clicked.");
	}
	
	protected boolean switchTabToChangePage(final String tabName)
	{
		getCurrentPage().switchToTab(tabName);
		executableExample.addReportMessage("Display has been switched to tab <b>" + tabName + "</b>.");
		return checkPageChange(tabName, EventType.TabSwitch);
	}
	
	protected boolean switchTabToChangePage(final String tabbedPanelName, final String tabName)
	{
		getCurrentPage().switchToTab(tabbedPanelName, tabName);
		executableExample.addReportMessage("Display of <b>" + tabbedPanelName + "</b> has been switched to tab <b>" + tabName + "</b>.");
		return checkPageChange(tabName, EventType.TabSwitch);
	}
	
	
    /**
     * Waits until after button click until one of the possiblePages is available.
     * After a default number of seconds without availablity of any page,
     * the test executions fails.
     * 
     * @param buttonName
     * @param possiblePages (one of these is expected)
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
		
		boolean pageChanged = clickButtonWithCheckPageChange(buttonName);
		if (pageChanged) return getCurrentPage();              // page change found - possiblePages ignored
		if (possiblePages.isEmpty()) return getCurrentPage();  // no specific pages expected 
		
		// find new expected page by the additional following mechanism of expected next pages
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
	
		
	protected void selectMainMenuPath(final String menuPath) 
	{
		try {
			if (menuPath.contains("/")) {
				handleMenuPath(menuPath, "/");
			} else {
				handleMenuPath(menuPath, "-");
			}
		} catch (Exception e) {
			executableExample.failWithMessage("Invalid menu path <b>" + menuPath + "</b>.");  
		}
	}
	
	protected void handleMenuPath(String menuPath, String separator)
	{
		String[] splitResult = menuPath.split(separator);
		for (String menuItem : splitResult) {
			clickMainMenuItem(menuItem);
		}
	}	
	
	protected void clickMainMenuItem(final String mainMenuItem) 
	{
		getCurrentPage().clickLink(mainMenuItem);
		executableExample.addReportMessage("Menu item <b>" + mainMenuItem + "</b> has been clicked.");
		checkPageChange(mainMenuItem, EventType.MenuItemClick);
	}
	

	protected void isTextDislayed(final String guiElementToRead, final String valueCandidate) 
	{
		final String expectedText = executableExample.getTestDataValue(valueCandidate);
		final String actualText = getCurrentPage().getText(guiElementToRead);
		
		boolean ok = actualText.equals(expectedText);
		String question = "Is the expected text (" + expectedText + ") equal to the actually displayed one (" + actualText + ")" + QUESTION_IDENTIFIER;
		executableExample.answerQuestion(question, ok);	
	}
	
	protected void chooseInCombobox(String valueCandidate, String fieldName) 
	{
		final String value = executableExample.getTestDataValue(valueCandidate);
		getCurrentPage().chooseInCombobox(fieldName, value);
		executableExample.addReportMessage("For field <b>" + fieldName + "</b> value <b>" + value + "</b> has been selected.");
	}
	
	protected void enterTextInField(String valueCandidate, String fieldName)
	{
		String value = executableExample.getTestDataValue(valueCandidate);
		getCurrentPage().enterTextInTextField(fieldName, value);
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

	// #########################  C H E C K B O X  ####################################

	
	protected void ensureCheckboxIsTicked(String checkBoxDisplayName)
	{
		boolean stateChanged = getCurrentPage().assureTickInCheckBox(checkBoxDisplayName);
		
		if (stateChanged) {
			executableExample.addReportMessage("In der Checkbox <b>" + checkBoxDisplayName + "</b> wurde der Haken gesetzt.");
		} else {
			executableExample.addCommentToReport("In der Checkbox <b>" + checkBoxDisplayName + "</b> war der Haken bereits gesetzt.");
		}
	}	
	
	protected void tickCheckboxForCondition(String checkBoxDisplayName, String actual, String expected)
	{
		if (! actual.startsWith(SysNatConstants.DC) && ! expected.startsWith(SysNatConstants.DC) ) {
			executableExample.failWithMessage("Weder die Rechte noch die linke Seite der Wenn-Bedingung enthält eine Datenreferenz!");
		}
		
		actual = executableExample.getTestDataValue(actual);
		expected = executableExample.getTestDataValue(expected);
		
		if (actual != null && actual.equals(expected)) {
			executableExample.addCommentToReport("Decision for checkbox state: " + actual + " equals " + expected);
			ensureCheckboxIsTicked(checkBoxDisplayName);
		} else {
			executableExample.addCommentToReport("Decision for checkbox state: " + actual + " does not equal " + expected);
			ensureCheckboxIsUNticked(checkBoxDisplayName);
		}
	}
	
	protected void ensureCheckboxIsUNticked(String checkBoxDisplayName)
	{
		boolean stateChanged = getCurrentPage().assureNoTickInCheckBox(checkBoxDisplayName);
		
		if (stateChanged) {
			executableExample.addReportMessage("In der Checkbox <b>" + checkBoxDisplayName + "</b> wurde der Haken entfernt.");
		} else {
			executableExample.addCommentToReport("In der Checkbox <b>" + checkBoxDisplayName + "</b> war der Haken bereits entfernt.");
		}
	}
		
	// #################################################################################
	//                          P U B L I C    M E T H O D S 
	// #################################################################################

	public void showInfoBox(String message, int millisToShow)
	{
	      JOptionPane opt = new JOptionPane(message, 
	    		                            JOptionPane.INFORMATION_MESSAGE, 
	    		                            JOptionPane.DEFAULT_OPTION, 
	    		                            null, new Object[]{}); // no buttons
	      final JDialog dlg = opt.createDialog("Info");
	      new Thread(new Runnable() 
	      {
              public void run() {
                try {
                  Thread.sleep(millisToShow);
                  dlg.dispose();
                } catch ( Throwable th ) { th.printStackTrace(); }
              }
	      }).start();
	      dlg.setVisible(true);	
	}
	
	
	public void findCurrentPage() {
		setCurrentPage(null);
		getCurrentPage();
	}
	
	public ExecutableExample getExecutableExample() {
		return executableExample;
	}

	public static class PageChangeEvent 
	{
		public enum EventType { ButtonClick, MenuItemClick, TabSwitch };
		
		public String uiElementIdentifierTrigger;
		public PageObject nextPage;
		public PageObject currentPage;
		public EventType type;
		public String uiElementIdentifierWaitForElement;
		public int millisToWait;
		
		protected PageChangeEvent() {};
	}
	
	public static class PageChangeEventBuilder
	{
		private PageChangeEvent result = new PageChangeEvent();
		
		/**
		 * Component that triggers the page change 
		 * @param technical id of ui element 
		 * @return this
		 */
		public PageChangeEventBuilder on(String uiElement) {
			result.uiElementIdentifierTrigger = uiElement;
			return this;
		}
		
		/**
		 * Current page to switch from
		 * @param PageObject
		 * @return this
		 */
		public PageChangeEventBuilder from(PageObject page) {
			result.currentPage = page;
			return this;
		}	
		
		/**
		 * Next page to switch to
		 * @param PageObject
		 * @return this
		 */
		public PageChangeEventBuilder to(PageObject page) {
			result.nextPage = page;
			return this;
		}	
		
		/**
		 * Trigger for changing the page
		 * @param EventType
		 * @return this
		 */
		public PageChangeEventBuilder via(EventType type) {
			result.type = type;
			return this;
		}	

		/**
		 * Wait condition for next page
		 * @param value either millis as integer or uiElementId as String
		 * @return this
		 */
		public PageChangeEventBuilder waiting(Object value) 
		{
			if (value instanceof Integer) {
				result.millisToWait = (Integer)value;
			} else {
				try {
					int i = Integer.valueOf("" + value);
					result.millisToWait = i;
				} catch (Exception e) {
					result.uiElementIdentifierWaitForElement = (String)value;
				}
			}
			return this;
		}	
		
		/**
		 * Create PageChangeEvent
		 * @return
		 */
		public PageChangeEvent build() 
		{
			Assert.notNull(result.nextPage, "PageChangeEvent.nextPage");
			Assert.notNull(result.uiElementIdentifierTrigger, "PageChangeEvent.uiElementIdentifier");
			return result;
		}
	}
	
}