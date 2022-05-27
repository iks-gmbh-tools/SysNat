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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebElement;

/**
 * Contains methods to access GUI in an technology independent way.
 *  
 * @author Reik Oberrath
 */
public interface GuiControl 
{
	// general functionalities
	void windowToFront();
	boolean init(Map<String, String> startParameter);
	Object getGuiHandle();
	void reloadGui();
	boolean openGUI();
	void closeGUI();
	File takeScreenShot();
	void resetMainFrameHandle();
	void setNextExecutionSpeedWaitState(int millis);
	
	// for text fields
	void enterTextInTextField(int index, String value);
	void enterTextInTextField(String text, String elementIdentifier);
	void enterTextInDialogTextField(String dialogTitle, String elementIdentifier, String text);
	void enterTextInDialogTextField(String dialogTitle, int index, String text);
	void inputEmail(String fieldName, String emailAdress);
	String getText(String elementIdentifier);
	void removeAllText(String elementIdentifier);
	void removeAllText(int index);
	void removeAllText(String dialogTitle, String elementIdentifier);
	boolean isTextFieldEnabled(String technicalId);
	boolean isTextFieldEditable(String technicalId);
	
	// for text areas
	void enterTextInDialogTextArea(String dialogTitle, String elementIdentifier, String text);
	
	// for tree 
	void expandKnotInTree(String treeName, String knotName);
	boolean containsTreeElement(String treeName, String elementName);

	// for buttons 
	void clickButton(String guiTextOrTechnicalId);
	void clickButton(int index, String buttonName);
	void clickButton(int index);
	void clickDialogButton(String dialogTitle, String guiTextOrTechnicalId);
	void clickDialogButton(String dialogTitle, int index);
	
	// for menus
	boolean clickMenuItem(String guiTextOrTechnicalId);	
	
	// for dialogs
	boolean isDialogAvailable(String dialogTitle);
	boolean isDialogAvailable(String dialogName, String message);
	void waitForDialogToClose(String dialogTitle);
	boolean waitForDialogToAppear(String dialogTitle, int timeoutInMillis);
	String checkForAnyDialog(int timeoutInMillis);
	
	// for dates
	void enterDateInDateField(String value, String elementIdentifier);
	void enterDateInDialogDateField(String dialogTitle, String value, String elementIdentifier);

	// for radio buttons and checkboxes
	boolean isSelected(String elementIdentifier);
	void clickRadioButton(String elementIdentifier);
	void clickRadioButton(int index);
	void clickDialogRadioButton(String dialogTitle, String elementIdentifier);
	/**
	 * @param elementIdentifier identifies a group of radio buttons
	 * @param position identifies a radio button within the group by button order
	 */
	void clickRadioButton(String elementIdentifier, int position);
	
	// for tables
	void selectAllRowsInTable(String tableId);
	void selectAllRowsInTable(int index);
	void clickRowInTable(String tableIndentifier, int rowNo);
	void clickRowInTable(int index, int rowNo);
	int getNumberOfRowsInTable(String tableIndentifier);
	int getNumberOfRowsInTable(int index);
	int getNumberOfColumnsInTable(String tableIndentifier);
	void clickTableCell(String tableIdentifier, int rowNo, int columnNo);
	void clickTab(String tabPanelIdentifier, int tabIndex);
	Rectangle getTableCellRectangle(String tableIdentifier, int rowNo, int columnNo);
	void doubleClickTableCell(String tableIdentifier, int rowNo, int columnNo);
	void doubleClickTableCell(int index, int rowNo, int columnNo);
	void clickTableRow(String tableIdentifier, int rowNo);
	Object getTableCellContent(String tableIndentifier, int rowNumber, int columnNumber);
	Object getTableCellContent(int index, int rowNumber, int columnNumber);
	List<String> getTableHeaders(String tableIndentifier);
	void editTableCell(String tableIndentifier, int rowNumber, int columnNumber, Object value);
	void editTableCell(int index, int rowNumber, int columnNumber, Object value);
	List<Integer> searchRowsInTable(String tableIdentifier, String searchCriteria);
	List<Integer> searchRowsInTable(int index, String searchCriteria);
	void selectRowInTable(String tableIdentifier, int row);
	List<Integer> searchRowsInDialogTable(String dialogTitle, String searchCriteria);
	void selectRowInDialogTable(String dialogTitle, int row);
	
	// for any GUI elements
	String clickElement(String elementIdentifier);
	String clickElement(String elementIdentifier, int timeoutInMillis);
	boolean isElementAvailable(String elementIdentifier, int timeoutMillis, boolean onlyEnabled);
	boolean isElementReadyToUse(String elementIdentifier);
	void clickElement(String tableName, String tableEntryText);
	void setFocusTo(String elementIdentifier);
	Point getCoordinates(String elementIdentifier);
	
	// for tabs
	String getSelectedTabName();
	void clickTab(String tabIdentifier, String tabName);
	
	// for checkboxes
	boolean isCheckBoxTicked(String chbId);
	boolean isCheckBoxTicked(int checkBoxIndex);
	boolean isDialogCheckBoxTicked(String dialogName, String technicalId);
	boolean isDialogCheckBoxTicked(String dialogName, int checkBoxIndex);
	/** @return true if checkbox was unticked  */
	boolean assureTickInCheckBox(String chbId);
	/** @return true if checkbox was unticked  */
	boolean assureTickInCheckBox(int checkBoxIndex);
	/** @return true if checkbox was unticked  */
	boolean assureTickInDialogCheckBox(String dialogName, String technicalId);
	/** @return true if checkbox was unticked  */
	boolean assureTickInDialogCheckBox(String dialogName, int checkBoxIndex);
	
	boolean assureNoTickInCheckBox(String chbId);
	boolean assureNoTickInCheckBox(int checkBoxIndex);
	boolean assureNoTickInDialogCheckBox(String dialogName, String technicalId);
	boolean assureNoTickInDialogCheckBox(String dialogName, int checkBoxIndex);
	
	// for comboboxes
	void selectComboboxEntry(String elementIdentifier, int index);
	void selectComboboxEntry(String elementIdentifier, String value);
	void selectComboboxEntry(int fieldIndex, int index);
	void selectComboboxEntry(int fieldIndex, String value);
	boolean isEntryInComboboxDropdownAvailable(String elementIdentifier, String value);
	String getSelectedComboBoxEntry(String elementIdentifier);
	
	// TextArea
	int getNumberOfLinesInTextArea(String areaID);
	void inputTextInTextArea(String value, String areaID);
	
	// Link
	void clickLink(String idToClick);
	void clickLink(String idToClick, String idToScrollIntoView);  // if idToClick is not in view
	void clickLink(String idToClick, int positionOfOccurrence);  // if link exists more than once on a page
	void clickLink(String idToClick, String idToScrollIntoView, int positionOfOccurrence); 
	
	// misc
	List<WebElement> getElements(String elementIdentifier);
	String getTagName(String elementIdentifier);
	boolean isTextCurrentlyDisplayed(String text);
	void waitUntilEnabledElementIsAvailable(String elementIdentifier);
	void waitUntilElementIsAvailable(String elementIdentifier); // includes disabled elements
	void waitUntilEnabledElementIsAvailable(String elementIdentifier, int timeoutInSeconds);	
	void waitUntilDialogElementIsAvailable(String dialogName, String elementIdentifier, int timeoutInSeconds);	
	boolean waitFileChooserIsVisible(int timeoutInMillis);
	void setFilePathInFileChooser(String filePath);

	
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
	void minimizeWindow();

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

	public default void clickOnScreenCoordinate(int x, int y) 
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

	public default boolean pressEnter()
	{
	   try {
	      Robot robot = new Robot();
	      robot.keyPress(KeyEvent.VK_ENTER);
	      robot.keyRelease(KeyEvent.VK_ENTER);
	      return true;
	   } catch (AWTException e) {
	      e.printStackTrace();
	      return false;
	   }
	}
	
	public default boolean pressTab()
	{
	   try {
	      Robot robot = new Robot();
	      robot.keyPress(KeyEvent.VK_TAB);
	      robot.keyRelease(KeyEvent.VK_TAB);
	      return true;
	   } catch (AWTException e) {
	      e.printStackTrace();
	      return false;
	   }
	}
	
	public default boolean pressAltShiftTab() 
	{
	   try {
		      Robot robot = new Robot();
		      robot.keyPress(KeyEvent.VK_ALT);
		      robot.keyPress(KeyEvent.VK_SHIFT);
		      robot.keyPress(KeyEvent.VK_TAB);
		      robot.keyRelease(KeyEvent.VK_TAB);
		      robot.keyRelease(KeyEvent.VK_SHIFT);
		      robot.keyRelease(KeyEvent.VK_ALT);
		      return true;
		   } catch (AWTException e) {
		      e.printStackTrace();
		      return false;
		   }
	}	

	public default boolean pressKey(char key) 
	{
	    try {
	    	int keyEvent = KeyEvent.getExtendedKeyCodeForChar(key);
	    	Robot robot = new Robot();
	    	press(key, keyEvent, robot);	      
	   } catch (AWTException e) {
	      e.printStackTrace();
	      return false;
	   }
	   
	   return true;
	}
	
	public default boolean pressAltWith(char key) 
	{
	    try {
	    	int keyEvent = KeyEvent.getExtendedKeyCodeForChar(key);
	    	Robot robot = new Robot();
	      
	    	robot.keyPress(KeyEvent.VK_ALT);
	    	press(key, keyEvent, robot);	      
	    	robot.keyRelease(KeyEvent.VK_ALT);
	   } catch (AWTException e) {
	      e.printStackTrace();
	      return false;
	   }
	   
	   return true;
	}

	public default boolean pressStrgAltWith(char key) 
	{
	    try {
	    	int keyEvent = KeyEvent.getExtendedKeyCodeForChar(key);
	    	Robot robot = new Robot();
	    	
	    	robot.keyPress(KeyEvent.VK_CONTROL);
	    	robot.keyPress(KeyEvent.VK_ALT);
	    	press(key, keyEvent, robot);	      
	    	robot.keyRelease(KeyEvent.VK_ALT);
	    	robot.keyRelease(KeyEvent.VK_CONTROL);	      
	   } catch (AWTException e) {
	      e.printStackTrace();
	      return false;
	   }
	   
	   return true;
	}
	
	public default boolean pressStrgWith(char key) 
	{
	    try {
	    	int keyEvent = KeyEvent.getExtendedKeyCodeForChar(key);
	    	Robot robot = new Robot();

	    	robot.keyPress(KeyEvent.VK_CONTROL);
	    	press(key, keyEvent, robot);	      
	    	robot.keyRelease(KeyEvent.VK_CONTROL);
	   } catch (AWTException e) {
	      e.printStackTrace();
	      return false;
	   }
	   
	   return true;
	}
	
	public default void press(char key, int keyEvent, Robot robot)
	{
		if (Character.isUpperCase(key)) {
			robot.keyPress(KeyEvent.VK_SHIFT);
		}
		      
		robot.keyPress(keyEvent);
		robot.keyRelease(keyEvent);
     
		if (Character.isUpperCase(key)) {
			robot.keyRelease(KeyEvent.VK_SHIFT);
		}
	}	
	
	public default void typeText(String text) throws AWTException 
	{ 
		char[] charArray = text.toCharArray();
		Robot robot = new Robot();
		for (int i = 0; i < charArray.length; i++) {
			int key = charArray[i];
			int keyEvent = KeyEvent.getExtendedKeyCodeForChar(key);
			press(charArray[i], keyEvent, robot);
		}
	}
	
	public default boolean pressAltTab() 
	{
	   try {
		      Robot robot = new Robot();
		      robot.keyPress(KeyEvent.VK_ALT);
		      robot.keyPress(KeyEvent.VK_TAB);
		      robot.keyRelease(KeyEvent.VK_TAB);
		      robot.keyRelease(KeyEvent.VK_ALT);
		      return true;
		   } catch (AWTException e) {
		      e.printStackTrace();
		      return false;
		   }
	}
	
	public default boolean pressFunctionKey(String functionKey, boolean withAlt, boolean withStrg) 
	{
	   try {
		   int keyEvent;
		   if ("F1".equals(functionKey)) {
			   keyEvent = KeyEvent.VK_F1;
		   } else if ("F2".equals(functionKey)) {
			   keyEvent = KeyEvent.VK_F2;
		   } else if ("F3".equals(functionKey)) {
			   keyEvent = KeyEvent.VK_F3;
		   } else if ("F4".equals(functionKey)) {
			   keyEvent = KeyEvent.VK_F4;
		   } else if ("F5".equals(functionKey)) {
			   keyEvent = KeyEvent.VK_F5;
		   } else if ("F6".equals(functionKey)) {
			   keyEvent = KeyEvent.VK_F6;
		   } else if ("F7".equals(functionKey)) {
			   keyEvent = KeyEvent.VK_F7;
		   } else if ("F8".equals(functionKey)) {
			   keyEvent = KeyEvent.VK_F8;
		   } else if ("F9".equals(functionKey)) {
			   keyEvent = KeyEvent.VK_F9;
		   } else if ("F10".equals(functionKey)) {
			   keyEvent = KeyEvent.VK_F10;
		   } else if ("F11".equals(functionKey)) {
			   keyEvent = KeyEvent.VK_F11;
		   } else if ("F12".equals(functionKey)) {
			   keyEvent = KeyEvent.VK_F12;
		   } else {
			   return false;
		   }
		   
		   Robot robot = new Robot();
		   if (withAlt) robot.keyPress(KeyEvent.VK_ALT);
		   if (withStrg) robot.keyPress(KeyEvent.VK_CONTROL);
		   robot.keyPress(keyEvent);
		   robot.keyRelease(keyEvent);
		   if (withStrg) robot.keyRelease(KeyEvent.VK_CONTROL);
		   if (withAlt) robot.keyRelease(KeyEvent.VK_ALT);
		      
	   } catch (AWTException e) {
	      e.printStackTrace();
		   return false;
	   }
	   
	   return true;
	}	
	
	
	public default boolean press(int keyEventId)
	{
	   try {
	      Robot robot = new Robot();
	      robot.keyPress(keyEventId);
	      robot.keyRelease(keyEventId);
	   } catch (AWTException e) {
	      e.printStackTrace();
	      return false;
	   }
	   return true;
	}	
	

	/**
	 * Set Thread to sleep.
	 * @param seconds
	 */
	public default void wait(double seconds) 
	{		
		try {
			Thread.sleep((long) (1000 * seconds));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
	}

	/**
	 * Set Thread to sleep.
	 * @param seconds
	 */
	public default void wait(int millis) 
	{		
		try {
			Thread.sleep((long) (millis));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
	}

}