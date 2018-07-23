package com.iksgmbh.sysnat.guicontrol;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
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
	String clickElement(String elementIndentifier, int timeoutInSeconds);
	boolean isElementAvailable(String elementIndentifier);
	boolean isElementReadyToUse(String elementIndentifier);
	void clickElement(String tableName, String tableEntryText);
	
	// for tabs
	String getSelectedTabName();
	void clickTab(String tabIdentifier, String tabName);
	
	
	// for comboboxes
	void selectComboboxEntry(String elementIdentifier, int index);
	void selectComboboxEntry(String elementIdentifier, String value);
	boolean isEntryInComboboxDropdownAvailable(String elementIdentifier, String value);
	String getSelectedComboBoxEntry(String elementIdentifier);
	
	// misc
	int getNumberOfLinesInTextArea(String xpath);
	void waitUntilElementIsAvailable(String elementIdentifier);
	void clickLink(String elementIdentifier);
	List<WebElement> getElements(String elementIdentifier);
	String getTagName(String elementIdentifier);
	
	// window stuff
	String getCurrentlyActiveWindowTitle();  // for selenium this is the tab title not the frame title (which not exist) !
	void switchToWindow(String windowID);
	void switchToLastWindow();
	int getNumberOfOpenTabs();  // in all frames !
	void switchToFirstWindow();


	boolean isTextCurrentlyDisplayed(String text);
	
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
	
}
