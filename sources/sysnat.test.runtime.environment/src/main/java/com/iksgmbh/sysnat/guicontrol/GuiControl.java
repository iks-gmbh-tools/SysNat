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
package com.iksgmbh.sysnat.guicontrol;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;

import org.openqa.selenium.WebElement;

/**
 * Contains methods to access GUI in an technology independent way.
 *  
 * @author Reik Oberrath
 */
public interface GuiControl 
{
	void loadPage(String url);
	void reloadCurrentPage();

	Object getWebDriver();
	void closeGUI();
	boolean openGUI();
	boolean init(String targetLoginUrl);
	File takeScreenShot();
	
	// for text fields and text areas
	void insertText(String text, String elementIndentifier);
	void inputEmail(String fieldName, String emailAdress);
	String getText(String elementIndentifier);
	
	// for radio buttons and checkboxes
	boolean isSelected(String elementIndentifier);
	
	/**
	 * @param elementIdentifier identifies a group of radio buttons
	 * @param position identifies a radio button within the group by button order
	 */
	void selectRadioButton(String elementIdentifier, int position);
	
	// for tables
	void selectRow(String elementIndentifier, int rowNo);
	String getTableCell(String tableIndentifier, int rowNumber, int columnNumber);
	int getNumberOfRows(String tableClass);
	int getNumberOfColumns(String tableClass);
	void clickTableCell(String tableIdentifier, int rowNo, int columnNo);
	String getTableCellText(String tableClassName, int tableNo, int columnNo, int rowNo);
	
	// for any GUI elements
	String clickElement(String elementIndentifier);
	String clickElement(String elementIndentifier, int timeoutInMillis);
	boolean isElementAvailable(String elementIndentifier);
	boolean isElementReadyToUse(String elementIndentifier);
	void clickElement(String tableName, String tableEntryText);
	
	// for tabs
	String getSelectedTabName();
	void clickTab(String tabIdentifier, String tabName);
	
	// for checkboxes
	boolean isCheckBoxTicked(String chbId);
	void assureTickInCheckBox(String chbId);
	void assureNoTickInCheckBox(String chbId);
	
	// for comboboxes
	void selectComboboxEntry(String elementIdentifier, int index);
	void selectComboboxEntry(String elementIdentifier, String value);
	boolean isEntryInComboboxDropdownAvailable(String elementIdentifier, String value);
	String getSelectedComboBoxEntry(String elementIdentifier);
	
	// misc
	int getNumberOfLinesInTextArea(String xpath);
	void clickLink(String elementIdentifier);
	List<WebElement> getElements(String elementIdentifier);
	String getTagName(String elementIdentifier);
	boolean isTextCurrentlyDisplayed(String text);
	void waitUntilElementIsAvailable(String elementIdentifier);
	void waitUntilElementIsAvailable(String elementIdentifier, int timeoutInSeconds);	

	/**
	 * @return true if text is not displayed or disappeared during maxSecondsToWait, false if not
	 */
	boolean waitToDisappear(String text, int maxSecondsToWait);
	
	// window stuff
	String getCurrentlyActiveWindowTitle();  // for selenium this is the tab title not the frame title (which not exist) !
	void switchToWindow(String windowID);
	void switchToLastWindow();
	int getNumberOfOpenApplicationWindows();
	void switchToFirstWindow();
	void maximizeWindow();

	public default void performDoubleClickOnPosition(int x, int y) 
	{
		try {
			Robot robot = new Robot();
			robot.mouseMove(x,y);           
			robot.mousePress(InputEvent.BUTTON1_MASK);
			robot.mouseRelease(InputEvent.BUTTON1_MASK);
			robot.mousePress(InputEvent.BUTTON1_MASK);
			robot.mouseRelease(InputEvent.BUTTON1_MASK);
		} catch (AWTException e) {
			e.printStackTrace();
		}

	}

	public default void performClickOnPosition(int x, int y) 
	{
		try {
			Robot robot = new Robot();
			robot.mouseMove(x,y);           
			robot.mousePress(InputEvent.BUTTON1_MASK);
			robot.mouseRelease(InputEvent.BUTTON1_MASK);
		} catch (AWTException e) {
			e.printStackTrace();
		}

	}

	public default void pressEnter()
	{
	   try {
	      Robot robot = new Robot();
	      robot.keyPress(KeyEvent.VK_ENTER);
	      robot.keyRelease(KeyEvent.VK_ENTER);
	   } catch (AWTException e) {
	      e.printStackTrace();
	   }
	}
}