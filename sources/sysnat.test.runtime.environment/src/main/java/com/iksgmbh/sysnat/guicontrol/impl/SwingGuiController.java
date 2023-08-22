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
package com.iksgmbh.sysnat.guicontrol.impl;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.tree.TreePath;

import org.assertj.core.util.Arrays;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.image.ScreenshotTaker;
import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;
import org.openqa.selenium.WebElement;

import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatDateUtil;
import com.iksgmbh.sysnat.guicontrol.SwingGuiControl;
import com.iksgmbh.sysnat.guicontrol.impl.swing.AbstractJemmyOperator;
import com.iksgmbh.sysnat.guicontrol.impl.swing.JemmyButtonOperator;
import com.iksgmbh.sysnat.guicontrol.impl.swing.JemmyCheckboxOperator;
import com.iksgmbh.sysnat.guicontrol.impl.swing.JemmyComboBoxOperator;
import com.iksgmbh.sysnat.guicontrol.impl.swing.JemmyComponentOperator;
import com.iksgmbh.sysnat.guicontrol.impl.swing.JemmyDialogOperator;
import com.iksgmbh.sysnat.guicontrol.impl.swing.JemmyLabelOperator;
import com.iksgmbh.sysnat.guicontrol.impl.swing.JemmyMenuItemOperator;
import com.iksgmbh.sysnat.guicontrol.impl.swing.JemmyRadioButtonOperator;
import com.iksgmbh.sysnat.guicontrol.impl.swing.JemmyTabbedPaneOperator;
import com.iksgmbh.sysnat.guicontrol.impl.swing.JemmyTableOperator;
import com.iksgmbh.sysnat.guicontrol.impl.swing.JemmyTextAreaOperator;
import com.iksgmbh.sysnat.guicontrol.impl.swing.JemmyTextfieldOperator;
import com.iksgmbh.sysnat.guicontrol.impl.swing.JemmyTreeOperator;

public class SwingGuiController extends AbstractGuiControl implements SwingGuiControl 
{
	private ScreenshotTaker screenshotTaker = new ScreenshotTaker();
	private JFrameOperator swingMainFrameHandle; 
	private JDialogOperator currentDialogHandle;
	private JemmyTableOperator currentTableHandle;
	private String currentDialogName;
	private String currentTableName;
	private String frameTitle;
	private static int problemcount;

	public boolean init(Map<String, String> startParameter)
	{
		this.frameTitle = startParameter.get(SysNatConstants.SwingStartParameter.MainFrameTitle.name());
		String javaStartClass = startParameter.get(SysNatConstants.SwingStartParameter.JavaStartClass.name());
		if (javaStartClass == null) {
			System.err.println("Start of Swing application failed. No " + SysNatConstants.SwingStartParameter.JavaStartClass + " defined.");
			return false;
		}
		
        System.out.println("------- JavaStartClass: " + this.getClass().getClassLoader().getResource(javaStartClass.replace('.', '/') + ".class"));

		try {
			CompletableFuture.runAsync(() -> startMainClass(javaStartClass));
			return openGUI();
		} catch (Exception e) {
			System.err.println("Start of Swing application failed: " + e.getMessage());
			return false;
		}
	}

	private void startMainClass(String guiMainClassAsString)
	{
		ClassReference mainClassInstance;
		try {
			mainClassInstance = new ClassReference(guiMainClassAsString);
		} catch (ClassNotFoundException e) {
			System.err.println("Start of Swing application failed. Java class '" + guiMainClassAsString + "'  ");
			System.exit(1);
			return;
		}
		
		try {
			if (System.getProperty("runGuiInSeparateThread", "true").equalsIgnoreCase("true")) {
				GuiActionRunner.execute(() -> mainClassInstance.startApplication());
			} else {
				mainClassInstance.startApplication();
			}
					
		} catch (Throwable e) {
			System.err.println("Start of Swing application failed. " + e.getMessage());
			System.exit(1);
			return;
		}
	}

	@Override
	public Object getGuiHandle() 
	{
		if (swingMainFrameHandle == null) {
			try {
				swingMainFrameHandle = new JFrameOperator(frameTitle);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return swingMainFrameHandle;
	}
	
	@Override
	public JFrameOperator getMainFrameHandle() {
		return (JFrameOperator) getGuiHandle();
	}
	
	@Override
	public void resetMainFrameHandle() {
		swingMainFrameHandle = null;
		getGuiHandle();
	}
	
	
	@Override
	public void windowToFront() {
		Date startTime = new Date();
		swingMainFrameHandle.toFront();
		checkExecutionSpeed(startTime);
	}
	
	@Override
	public void reloadGui() {
		// TODO
	}

	@Override
	public boolean openGUI() {
		return true; // nothing to do here for swing applications
	}

	@Override
	public void closeGUI()
	{
		try {
			// swingMainFrameHandle.close();  does not return, reason unclear
		} catch (Exception e) {
			// ignore exception
		}
	}
	
	
	@Override
	public File takeScreenShot() 
	{
		File imageFolder = new File(System.getProperty("java.io.tmpdir"));
		String imagePath = imageFolder.getAbsoluteFile().getAbsolutePath() + File.separator + "Screenshot.png";
		screenshotTaker.saveDesktopAsPng(imagePath);
		return new File(imagePath);
	}

	@Override
	public Point getCoordinates(String elementIdentifier) {
		return findComponent(swingMainFrameHandle, elementIdentifier).getLocationOnScreen();
	}

	@Override
	public void expandKnotInTree(String treeName, String knotName)
	{
		Date startTime = new Date();
		Object[] path = {knotName};
		TreePath treePath = new TreePath(path);
		new JemmyTreeOperator(swingMainFrameHandle, new NameComponentChooser(treeName)).getOperator().expandPath(treePath);
		checkExecutionSpeed(startTime);
	}

	@Override
	public boolean isDialogAvailable(String dialogName, String message)
	{
		boolean ok = checkDialog(dialogName);
		if (!ok) 
		{
			int indexCount = 0;
			while (ok) 
			{
				try {
					String textFromLabel = readTextFromLabel(currentDialogHandle, indexCount);
					if (textFromLabel.contains(message) || textFromLabel.equals(message)) {
						return true;
					}
					
				} catch (Exception e) {
					ok = false;
				}
			}
		}
		
		return ok;
	}
	
	
	@Override
	public boolean containsTreeElement(String treeName, String elementName)
	{
		Object[] path = {elementName};
		TreePath treePath = new TreePath(path);
		try {
			new JemmyTreeOperator(swingMainFrameHandle,0).getOperator().hasBeenExpanded(treePath);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void clearTextfield(String technicalTextFieldName) {
		clearTextfield(swingMainFrameHandle, technicalTextFieldName);
	}
	
	public void clearDialogTextfield(String dialogName, String technicalTextFieldName) 
	{
		checkDialog(dialogName);
		clearTextfield(currentDialogHandle, technicalTextFieldName);
	}
	

	@Override
	public void inputTextInTextArea(String value, String areaID) {
		Date startTime = new Date();
		new JemmyTextAreaOperator(swingMainFrameHandle, new NameComponentChooser(areaID)).typeText(value);
		checkExecutionSpeed(startTime);
	}

	@Override
	public void enterTextInTextField(int index, String text) {
		Date startTime = new Date();
		new JemmyTextfieldOperator(swingMainFrameHandle, index).getOperator().typeText(text);
		checkExecutionSpeed(startTime);
	}
	
	@Override
	public void enterTextInTextField(String text, String elementIdentifier)
	{
		Date startTime = new Date();
		enterInTextfield(swingMainFrameHandle, elementIdentifier, text);
		checkExecutionSpeed(startTime);
	}
	
	@Override
	public void removeAllText(String dialogTitle, String technicalTextFieldName)
	{
		Date startTime = new Date();
		checkDialog(dialogTitle);
		new JemmyTextfieldOperator(currentDialogHandle, new NameComponentChooser(technicalTextFieldName)).clear();
		checkExecutionSpeed(startTime);
	}
	
	@Override
	public void removeAllText(int index)
	{
		Date startTime = new Date();
		JemmyTextfieldOperator operator = new JemmyTextfieldOperator(swingMainFrameHandle, index);
		operator.clear();
		checkExecutionSpeed(startTime);	
	}

	
	@Override
	public void removeAllText(String technicalTextFieldName) {
		Date startTime = new Date();
		new JemmyTextfieldOperator(swingMainFrameHandle, new NameComponentChooser(technicalTextFieldName)).clear();
		checkExecutionSpeed(startTime);
	}
	
	@Override
	public boolean isTextFieldEnabled(String elementIdentifier)
	{
		return new JemmyTextfieldOperator(swingMainFrameHandle, new NameComponentChooser(elementIdentifier)).isEnabled();
	}

	@Override
	public boolean isTextFieldEditable(String elementIdentifier)
	{
		return new JemmyTextfieldOperator(swingMainFrameHandle, new NameComponentChooser(elementIdentifier)).isEditable();
	}	
	
	public void enterInTextfield(String elementIdentifier, String text) {
		enterInTextfield(swingMainFrameHandle, elementIdentifier, text);
	}

	@Override
	public void enterTextInDialogTextField(String dialogName, String elementIdentifier, String text) 
	{
		Date startTime = new Date();
		checkDialog(dialogName);
		enterInTextfield(currentDialogHandle, elementIdentifier, text);
		checkExecutionSpeed(startTime);
	}	

	@Override
	public void enterTextInDialogTextField(String dialogName, int index, String text) 
	{
		Date startTime = new Date();
		checkDialog(dialogName);
		enterInTextfield(currentDialogHandle, index, text);
		checkExecutionSpeed(startTime);
	}	
	
	public void enterInDialogTextfield(String dialogName, int indexOnDataMask, String text) {
		checkDialog(dialogName);
		JemmyTextfieldOperator textfieldHandle = new JemmyTextfieldOperator(currentDialogHandle, indexOnDataMask);
		textfieldHandle.setText(text);
	}
	
	@Override
	public void enterTextInDialogTextArea(String dialogName, String elementIdentifier, String text)
	{
		Date startTime = new Date();
		checkDialog(dialogName);
		new JemmyTextAreaOperator(currentDialogHandle, new NameComponentChooser(elementIdentifier)).typeText(text);
		checkExecutionSpeed(startTime);
	}

	@Override
	public void inputEmail(String fieldName, String emailAdress)
	{
		Date startTime = new Date();
		// TODO Auto-generated method stub
		checkExecutionSpeed(startTime);
	}
	
	@Override
	public void enterDateInDialogDateField(String dialogTitle, String value, String elementIdentifier)
	{
		Date startTime = new Date();
		enterTextInDialogTextField(dialogTitle, value, elementIdentifier);
		checkExecutionSpeed(startTime);
	}
	
	@Override
	public void enterDateInDateField(String value, String elementIdentifier) {
		Date startTime = new Date();
		enterTextInTextField(value, elementIdentifier);
		checkExecutionSpeed(startTime);
	}

	@Override
	public String getText(String elementIdentifier) 
	{
		Date startTime = new Date();
		try {
			JComponentOperator c = findComponent(swingMainFrameHandle, elementIdentifier);
			if (c.getSource() instanceof JTextField) return readFromTextfield(swingMainFrameHandle, elementIdentifier);
			if (c.getSource() instanceof JLabel) return readTextFromLabel(swingMainFrameHandle, elementIdentifier);
			if (c.getSource() instanceof JComboBox<?>) return readTextFromCombobox(swingMainFrameHandle, elementIdentifier);
		}
		finally {
			checkExecutionSpeed(startTime);
		}
		throw new RuntimeException("Cannot find element to read text from: " + elementIdentifier);
	}
	
	
	public String getTextFromCombobox(int index) {
		return new JemmyComboBoxOperator(swingMainFrameHandle, index).getSelectedItem();
	}	

	public String getTextFromDialog(String dialogName, String elementIdentifier) 
	{
		checkDialog(dialogName);
		JComponentOperator c = findComponent(currentDialogHandle, elementIdentifier); 		
		if (c.getSource() instanceof JTextField) return readFromTextfield(currentDialogHandle, elementIdentifier);
		if (c.getSource() instanceof JLabel) return readTextFromLabel(currentDialogHandle, elementIdentifier);
		throw new RuntimeException("Cannot find in dialog the element to read text from: " + elementIdentifier);
	}
	
	@Override
	public void clickDialogButton(String dialogName, int index)
	{
		Date startTime = new Date();
		boolean dialogAvailable = checkDialog(dialogName);
		if (! dialogAvailable) throw new RuntimeException("Dialog <b>" + dialogName + "</b> is not available!");
		new JemmyButtonOperator(currentDialogHandle, index).push();
		checkExecutionSpeed(startTime);
	}
	
	@Override
	public void clickButton(String guiTextOrTechnicalId)
	{
		Date startTime = new Date();
		JButtonOperator buttonOperator = getButtonOperator(swingMainFrameHandle, guiTextOrTechnicalId);
		buttonOperator.setFocusPainted(true);
		buttonOperator.push();
		checkExecutionSpeed(startTime);
	}
	
	@Override
	public void clickButton(int index)
	{
		JButtonOperator buttonOperator = getButtonOperator(swingMainFrameHandle, index);
		if (buttonOperator != null) {
			buttonOperator.push();
		}
	}
	
	@Override
	public void clickButton(int index, String buttonName)
	{
		JButtonOperator buttonOperator = getButtonOperator(swingMainFrameHandle, index);
		if (buttonOperator != null && buttonOperator.getText().equals(buttonName)) {
			buttonOperator.push();
		}
	}	
	
	@Override
	public void clickDialogButton(String dialogName, String guiTextOrTechnicalId) {
		Date startTime = new Date();
		clickDialogButton(dialogName, guiTextOrTechnicalId, 1000);
		checkExecutionSpeed(startTime);
	}

	@Override
	public void clickDialogButton(String dialogName, String guiTextOrTechnicalId, long timeout)
	{
		Date startTime = new Date();
		boolean dialogAvailable = checkDialog(dialogName);
		if (! dialogAvailable) {
			System.out.println("#################################################################");
			System.out.println("###################### cannot click button: unknown dialog " + dialogName);
			System.out.println("#################################################################");
			throw new RuntimeException("Dialog <b>" + dialogName + "</b> is not available!");
		}
		JButtonOperator buttonOperator = getButtonOperator(currentDialogHandle, guiTextOrTechnicalId, timeout);
		if (buttonOperator == null) {
			System.out.println("#################################################################");
			System.out.println("###################### cannot find dialog button: " + dialogName + "." + guiTextOrTechnicalId);
			System.out.println("#################################################################");
			throw new RuntimeException("On dialog " + dialogName + " is button " + guiTextOrTechnicalId + " not available!");
		}
		
		System.out.println("#################################################################");
		System.out.println("###################### button " + guiTextOrTechnicalId + "status: " + buttonOperator.isEnabled() 
		+ " " + buttonOperator.isVisible()+ " " + buttonOperator.isValid() + " " + buttonOperator.isDisplayable()
		+ " " + buttonOperator.isShowing() + " " + buttonOperator.isFocusPainted());
		System.out.println("#################################################################");
		
		buttonOperator.push();
		checkExecutionSpeed(startTime);
	}
	
	public void resetTableHandle() {
		currentTableHandle = null;
		currentTableName = null;
	}
	
	public void resetDialogHandle() {
		currentDialogHandle = null;
		currentDialogName = null;
	}
	

	@Override
	public boolean clickDialogButtonIfAvailable(String dialogName, String guiTextOrTechnicalId, long timeout, boolean resetDialogHandle)
	{
		Date startTime = new Date();
		try {			
			Date now = new Date();
			boolean dialogAvailable = isDialogAvailable(dialogName);
			while ( SysNatDateUtil.getDiffInMillis(startTime, now) < timeout && ! dialogAvailable) {
				wait(0.1);
				now = new Date();
				dialogAvailable = isDialogAvailable(dialogName);
			}
			if ( ! dialogAvailable) return false;
			
			try {
				clickDialogButton(dialogName, guiTextOrTechnicalId, timeout);
			} catch (Exception e) {
				return false;
			}
			
			if (resetDialogHandle) currentDialogHandle = null;
			return true;
		} finally {
			checkExecutionSpeed(startTime);
		}
	}
	
	@Override
	public boolean clickDialogButtonIfAvailable(String dialogName, String buttonName)
	{
		Date startTime = new Date();
		try {			
			return clickDialogButtonIfAvailable(dialogName, buttonName, 1000);
		} finally {			
			checkExecutionSpeed(startTime);
		}
	}

	
	@Override
	public boolean clickDialogButtonIfAvailable(String dialogName, String guiTextOrTechnicalId, long timeout) 
	{
		Date startTime = new Date();
		try {			
			return clickDialogButtonIfAvailable(dialogName, guiTextOrTechnicalId, timeout, true);
		} finally {			
			checkExecutionSpeed(startTime);
		}
	}
	


	@Override
	public boolean isSelected(String elementIdentifier)
	{
		Date startTime = new Date();
		try {			
			return false;
		} finally {			
			checkExecutionSpeed(startTime);
		}
	}

	@Override
	public void clickRadioButton(String elementIdentifier, int position)
	{
		Date startTime = new Date();
		// TODO
		checkExecutionSpeed(startTime);
	}
	

	@Override
	public void clickDialogRadioButton(String dialogTitle, String elementIdentifier)
	{
		Date startTime = new Date();
		checkDialog(dialogTitle);
		new JemmyRadioButtonOperator(currentDialogHandle, new NameComponentChooser(elementIdentifier)).push();
		checkExecutionSpeed(startTime);
	}

	@Override
	public void clickRadioButton(String elementIdentifier) {
		Date startTime = new Date();
		new JemmyRadioButtonOperator(swingMainFrameHandle, new NameComponentChooser(elementIdentifier)).push();
		checkExecutionSpeed(startTime);
	}

	@Override
	public void clickRadioButton(int index) {
		Date startTime = new Date();
		new JemmyRadioButtonOperator(swingMainFrameHandle, 2).push();
		checkExecutionSpeed(startTime);
	}


	@Override
	public void waitForDialogToClose(String dialogIdentifier)
	{
		boolean goOnWaiting = true;
		while (goOnWaiting) 
		{
			wait(0.1);
			JemmyDialogOperator dialogHandle = new JemmyDialogOperator(100);
			goOnWaiting = dialogHandle.getOperator() != null;
			if (goOnWaiting) {
				goOnWaiting =  dialogIdentifier.equals(dialogHandle.getOperator().getTitle()) 
						       || dialogIdentifier.equals(dialogHandle.getOperator().getName());
			}
		}
		currentDialogHandle = null;
		currentDialogName = null;
	}

	@Override
	public Rectangle getTableCellRectangle(String tableIdentifier, int rowNo, int columnNo)
	{
		if (rowNo < 1) throw new IllegalArgumentException();
		if (columnNo < 1) throw new IllegalArgumentException();
		checkTable(tableIdentifier);
		return currentTableHandle.getOperator().getCellRect(rowNo-1, columnNo-1, false);
	}

	@Override
	public List<Integer> searchRowsInTable(String tableIdentifier, String searchCriteria)
	{
		loadTable(tableIdentifier);
		return currentTableHandle.search(searchCriteria);
	}

	@Override
	public List<Integer> searchRowsInTable(int index, String searchCriteria)
	{
		return new JemmyTableOperator(swingMainFrameHandle, index).search(searchCriteria);
	}
	
	@Override
	public List<Integer> searchRowsInDialogTable(String dialogTitle, String searchCriteria)
	{
		checkDialog(dialogTitle);;
		return new JemmyTableOperator(currentDialogHandle, 0).search(searchCriteria);
	}

	@Override
	public void selectRowInDialogTable(String dialogTitle, int columnNumber)
	{
		Date startTime = new Date();
		checkDialog(dialogTitle);
		new JemmyTableOperator(currentDialogHandle, 0).selectRow(columnNumber);
		checkExecutionSpeed(startTime);
	}

	@Override
	public void selectRowInTable(String tableIdentifier, int rowNumber)
	{
		Date startTime = new Date();
		if (rowNumber < 1) throw new IllegalArgumentException();
		checkTable(tableIdentifier);
		currentTableHandle.selectRow(rowNumber);
		checkExecutionSpeed(startTime);
	}

	@Override
	public void editTableCell(String tableIdentifier, int rowNumber, int columnNumber, Object value)
	{
		Date startTime = new Date();
		if (rowNumber < 1) throw new IllegalArgumentException();
		if (columnNumber < 1) throw new IllegalArgumentException();
		checkTable(tableIdentifier);
		currentTableHandle.editCellValue(value, rowNumber, columnNumber);
		checkExecutionSpeed(startTime);
	}

	@Override
	public void editTableCell(int index, int rowNumber, int columnNumber, Object value)
	{
		Date startTime = new Date();
		if (rowNumber < 1) throw new IllegalArgumentException();
		if (columnNumber < 1) throw new IllegalArgumentException();
		new JemmyTableOperator(swingMainFrameHandle, index).editCellValue(value, rowNumber, columnNumber);
		checkExecutionSpeed(startTime);
	}
	
	
	@Override
	public void selectAllRowsInTable(String tableIdentifier)
	{
		Date startTime = new Date();
		checkTable(tableIdentifier);
		currentTableHandle.selectAll();
		checkExecutionSpeed(startTime);
	}

	@Override
	public void selectAllRowsInTable(int index) {
		Date startTime = new Date();
		new JemmyTableOperator(swingMainFrameHandle, index).selectAll();
		checkExecutionSpeed(startTime);
	}
	
	@Override
	/**
	 * @param row starting with 1
	 */
	public void clickRowInTable(String tableIdentifier, int rowNumber)
	{
		Date startTime = new Date();
		checkTable(tableIdentifier);
		 
		if (rowNumber < 1) throw new IllegalArgumentException();
		currentTableHandle.clickRow(rowNumber - 1);
		checkExecutionSpeed(startTime);
	}

	@Override
	/**
	 * @param row starting with 1
	 */
	public void clickRowInTable(int index, int rowNumber)
	{
		Date startTime = new Date();
		if (rowNumber < 1) throw new IllegalArgumentException();
		new JemmyTableOperator(swingMainFrameHandle, index).clickRow(rowNumber - 1);
		checkExecutionSpeed(startTime);
	}

	@Override
	public List<String> getTableHeaders(String tableIdentifier)
	{
		checkTable(tableIdentifier);
		return Arrays.asList(currentTableHandle.getTableHeaderData()).stream()
				.map(o -> o.toString()).collect(Collectors.toList());
	}

	@Override
	public Object getTableCellContent(String tableIdentifier, int rowNumber, int columnNumber)
	{
		checkTable(tableIdentifier);
		if (rowNumber < 1) throw new IllegalArgumentException();
		if (columnNumber < 1) throw new IllegalArgumentException();
		return currentTableHandle.getValueAt(rowNumber-1, columnNumber-1);
	}

	@Override
	public Object getTableCellContent(int index, int rowNumber, int columnNumber)
	{
		if (rowNumber < 1) throw new IllegalArgumentException();
		if (columnNumber < 1) throw new IllegalArgumentException();
		Object toReturn = new JemmyTableOperator(swingMainFrameHandle, index).getValueAt(rowNumber-1, columnNumber-1);
		if (toReturn == null) toReturn = "";
		return toReturn;
	}
	@Override
	public int getNumberOfRowsInTable(String tableIdentifier)
	{
		checkTable(tableIdentifier);
		return currentTableHandle.getRowCount();
	}
	
	@Override
	public int getNumberOfRowsInTable(int index)
	{
		return new JemmyTableOperator(swingMainFrameHandle, index).getRowCount();
	}	
	
	
	@Override
	public int getNumberOfRowsOfDialogTable(String dialogName, String tableIdentifier)
	{
		checkDialogTable(dialogName, tableIdentifier);
		return currentTableHandle.getRowCount();
	}

	@Override
	public int getNumberOfColumnsInTable(String tableIdentifier)
	{
		checkTable(tableIdentifier);
		return currentTableHandle.getColumnCount();
	}
	
	@Override
	public int getNumberOfColumnsOfDialogTable(String dialogName, String tableIdentifier)
	{
		checkDialogTable(dialogName, tableIdentifier);
		return currentTableHandle.getColumnCount();
	}

	
	@Override
	public void clickTableCell(String tableIdentifier, int rowNumber, int columnNumber) 
	{
		Date startTime = new Date();
		checkTable(tableIdentifier);
		if (rowNumber < 1) throw new IllegalArgumentException();
		if (columnNumber < 1) throw new IllegalArgumentException();
		currentTableHandle.clickOnCell(rowNumber-1, columnNumber-1);
		checkExecutionSpeed(startTime);
	}
	
	@Override
	public void clickDialogTableCell(String dialogName, String tableIdentifier, int rowNumber, int columnNumber) 
	{
		Date startTime = new Date();
		checkDialogTable(dialogName, tableIdentifier);
		if (rowNumber < 1) throw new IllegalArgumentException();
		if (columnNumber < 1) throw new IllegalArgumentException();
		currentTableHandle.clickOnCell(rowNumber-1, columnNumber-1);
		checkExecutionSpeed(startTime);
	}
	

	@Override
	public void doubleClickTableCell(String tableIdentifier, int rowNumber, int columnNumber) 
	{
		Date startTime = new Date();
		checkTable(tableIdentifier);
		if (rowNumber < 1) throw new IllegalArgumentException();
		if (columnNumber < 1) throw new IllegalArgumentException();
		currentTableHandle.getOperator().clickOnCell(rowNumber-1, columnNumber-1, 2);
		checkExecutionSpeed(startTime);
	}
	
	@Override
	public void doubleClickTableCell(int index, int rowNumber, int columnNumber) 
	{
		Date startTime = new Date();
		if (rowNumber < 1) throw new IllegalArgumentException();;
		if (columnNumber < 1) throw new IllegalArgumentException();
		new JemmyTableOperator(this.swingMainFrameHandle, index).getOperator().clickOnCell(rowNumber-1, columnNumber-1, 2);
		checkExecutionSpeed(startTime);
	}
	

	@Override
	public void clickTableRow(String tableIdentifier, int rowNumber) 
	{
		Date startTime = new Date();
		checkTable(tableIdentifier);
		if (rowNumber < 1) throw new IllegalArgumentException();
		currentTableHandle.clickRow(rowNumber-1);
		checkExecutionSpeed(startTime);
	}

	/**
	 * @return error message
	 */
	@Override
	public String clickElement(String elementIdentifier)
	{
		Date startTime = new Date();
		try {
			return clickElement(elementIdentifier, 10);
		} finally {
			checkExecutionSpeed(startTime);
		}
	}

	public String clickDialogElement(String dialogName, String elementIdentifier)
	{
		checkDialog(dialogName);
		findComponent(currentDialogHandle, elementIdentifier).clickMouse();
		return null;
	}
	
	/**
	 * @return error message
	 */
	@Override
	public String clickElement(String elementIdentifier, int timeoutInMillis)
	{
		Date startTime = new Date();
		findComponent(swingMainFrameHandle, elementIdentifier, (long)timeoutInMillis).clickMouse();
		checkExecutionSpeed(startTime);
		return null;
	}

	@Override
	public Component getElement(String elementIdentifier)
	{
		Date startTime = new Date();
		try {
			return findComponent(swingMainFrameHandle, elementIdentifier, 1000).getSource();
		} finally {
			checkExecutionSpeed(startTime);
		}
	}
	

	@Override
	public boolean isDialogAvailable(String dialogName)
	{
		currentDialogName = null; 
		currentDialogHandle = null;
		return checkDialog(dialogName);
	}

	
	@Override
	public boolean isElementAvailable(String elementIdentifier, int timeoutMillis, boolean onlyEnabled)
	{
		if (isElementAvailable(elementIdentifier, null, timeoutMillis, onlyEnabled)) {
			return true;
		}
		JemmyDialogOperator dialogOperator = new JemmyDialogOperator(timeoutMillis);
		if (dialogOperator.getOperator() == null) {
			return false;
		}
		JComponentOperator operator = findComponent(dialogOperator.getOperator(), elementIdentifier);
		return operator != null && operator.isShowing();
	}
	
	public boolean isElementAvailable(String elementIdentifier, Class<? extends JComponent> type, long timeout, boolean onlyEnabled)
	{
		try {
			if (type == JTextField.class) {
				/*
				  JTextFieldOperator operator = new JemmyTextfieldOperator(swingMainFrameHandle, new NameComponentChooser(elementIdentifier), timeout).getOperator();
				  With this isShowing in isEnabledComponentAvailable returns false which is unfortunately wrong! 	
				 */
				int index = findIndexForTextfield(elementIdentifier);
				JTextFieldOperator operator = new JTextFieldOperator(swingMainFrameHandle,index);
				if (onlyEnabled) {
					return isEnabledComponentAvailable(operator);
				} else {
					return isComponentAvailable(operator);
				}
			}
			if (type == JComboBox.class) {

				int index = findIndexForCombobox(elementIdentifier);
				JComboBoxOperator operator = new JComboBoxOperator(swingMainFrameHandle,index);
				if (onlyEnabled) {
					return isEnabledComponentAvailable(operator);
				} else {
					return isComponentAvailable(operator);
				}
			}		
			return isComponentAvailable(elementIdentifier, timeout, onlyEnabled);
		} catch (Exception e) {
			return false;
		}
	}

	
	public boolean isDialogElementAvailable(String dialogName, String elementIdentifier) {
		return isDialogElementAvailable(dialogName, elementIdentifier, 1000);
	}
	
	public boolean isDialogElementAvailable(String dialogName, String elementIdentifier, long timeout)
	{
		checkDialog(dialogName);
		try { 
			JComponentOperator result = findComponent(currentDialogHandle, elementIdentifier, timeout);
			return result != null;
		}
		catch (Exception e) 
		{ 
			try {
				JButtonOperator buttonOperator = getButtonOperator(currentDialogHandle, elementIdentifier);
				return buttonOperator != null;	
			} catch (Exception e2) {
				return false;
			}
		}
	}

	@Override
	public boolean isElementReadyToUse(String elementIdentifier)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clickElement(String tableName, String tableEntryText)
	{
		Date startTime = new Date();
		// TODO Auto-generated method stub
		checkExecutionSpeed(startTime);
	}

	@Override
	public String getSelectedTabName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clickTab(String tabPanelIdentifier, String tabName) 
	{
		Date startTime = new Date();
		if (tabPanelIdentifier == null)
		{
			int index = 0;
			boolean ok = false;
			JemmyTabbedPaneOperator tabbedPaneOperator = null;
			while (! ok) {
				try {
					tabbedPaneOperator = new JemmyTabbedPaneOperator(swingMainFrameHandle, index);
					JTabbedPane source = ((JTabbedPane) tabbedPaneOperator.getOperator().getSource());
					int num = source.getTabCount();
					for (int i = 0; i < num; i++) {
						if (source.getTitleAt(i).equals(tabName)) {
							ok = true;
						};
					}
					index++;
				} catch (Exception e) {
					ok = true;
				}
			}
			if (tabbedPaneOperator != null)	tabbedPaneOperator.gotoTab(tabName);
			
		} else {
			new JemmyTabbedPaneOperator(swingMainFrameHandle, tabPanelIdentifier).gotoTab(tabName);
		}

		checkExecutionSpeed(startTime);
	}
	
	@Override
	public void clickTab(String tabPanelIdentifier, int tabNumber) 
	{
		Date startTime = new Date();
		if (tabPanelIdentifier == null)
		{
			int index = 0;
			boolean ok = false;
			JemmyTabbedPaneOperator tabbedPaneOperator = null;
			while (! ok) {
				try {
					tabbedPaneOperator = new JemmyTabbedPaneOperator(swingMainFrameHandle, index);
					JTabbedPane source = ((JTabbedPane) tabbedPaneOperator.getOperator().getSource());
					int num = source.getTabCount();
					ok = num >= tabNumber;
					index++;
				} catch (Exception e) {
					ok = true;
				}
			}
			if (tabbedPaneOperator != null)	tabbedPaneOperator.gotoTab(tabNumber);
		} else {
			new JemmyTabbedPaneOperator(swingMainFrameHandle, tabPanelIdentifier).gotoTab(tabNumber);
		}
		checkExecutionSpeed(startTime);
	}
	
	// #########################  C H E C K B O X  ####################################
	
	@Override
	public boolean isCheckBoxTicked(int checkBoxIndex) 
	{
		Date startTime = new Date();
		try {
			return isCheckboxTicked(swingMainFrameHandle, checkBoxIndex);
		} finally {
			checkExecutionSpeed(startTime);
		}
	}
	
	@Override
	public boolean isCheckBoxTicked(String technicalCheckboxName)
	{
		Date startTime = new Date();
		try {
			return isCheckboxTicked(swingMainFrameHandle, technicalCheckboxName);
		} finally {
			checkExecutionSpeed(startTime);
		}
	}
	
	@Override
	public boolean isDialogCheckBoxTicked(String dialogName, String technicalCheckboxName)
	{
		Date startTime = new Date();
		checkDialog(dialogName);
		try {
			return isCheckboxTicked(currentDialogHandle, technicalCheckboxName);
		} finally {
			checkExecutionSpeed(startTime);
		}
	}
	
	@Override
	public boolean isDialogCheckBoxTicked(String dialogName, int checkBoxIndex)
	{
		Date startTime = new Date();
		checkDialog(dialogName);
		try {
			return isCheckboxTicked(currentDialogHandle, checkBoxIndex);
		} finally {
			checkExecutionSpeed(startTime);
		}
	}

	
	@Override
	public boolean assureTickInCheckBox(String technicalCheckboxName) 
	{
		Date startTime = new Date();
		try {
			return tickCheckboxIfUnticked(swingMainFrameHandle, technicalCheckboxName);
		} finally {
			checkExecutionSpeed(startTime);
		}
	}

	@Override
	public boolean assureTickInCheckBox(int checkBoxIndex) 
	{
		Date startTime = new Date();
		try {
			return tickCheckboxIfUnticked(swingMainFrameHandle, checkBoxIndex);
		} finally {
			checkExecutionSpeed(startTime);
		}
	}	

	@Override
	public boolean assureTickInDialogCheckBox(String dialogName, String technicalCheckboxName)
	{
		Date startTime = new Date();
		checkDialog(dialogName);
		try {
			return tickCheckboxIfUnticked(currentDialogHandle, technicalCheckboxName);
		} finally {
			checkExecutionSpeed(startTime);
		}
	}
	

	@Override
	public boolean assureTickInDialogCheckBox(String dialogName, int checkBoxIndex)
	{
		Date startTime = new Date();
		checkDialog(dialogName);
		try {
			return tickCheckboxIfUnticked(currentDialogHandle, checkBoxIndex);
		} finally {
			checkExecutionSpeed(startTime);
		}
	}	

	
	@Override
	public boolean assureNoTickInCheckBox(String technicalCheckboxName) {
		Date startTime = new Date();
		try {
			return untickDialogCheckboxIfTicked(swingMainFrameHandle, technicalCheckboxName);
		} finally {
			checkExecutionSpeed(startTime);
		}
	}
	
	@Override
	public boolean assureNoTickInCheckBox(int checkBoxIndex) {
		Date startTime = new Date();
		try {
			return untickDialogCheckboxIfTicked(swingMainFrameHandle, checkBoxIndex);
		} finally {
			checkExecutionSpeed(startTime);
		}
	}
	
	@Override
	public boolean assureNoTickInDialogCheckBox(String dialogName, String technicalCheckboxName)
	{
		Date startTime = new Date();
		checkDialog(dialogName);
		try {
			return untickDialogCheckboxIfTicked(currentDialogHandle, technicalCheckboxName);
		} finally {
			checkExecutionSpeed(startTime);
		}
	}

	@Override
	public boolean assureNoTickInDialogCheckBox(String dialogName, int checkBoxIndex)
	{
		Date startTime = new Date();
		checkDialog(dialogName);
		try {
			return untickDialogCheckboxIfTicked(currentDialogHandle, checkBoxIndex);
		} finally {
			checkExecutionSpeed(startTime);
		}
	}
	
	// #########################  C O M B O B O X  ####################################


	@Override
	public void selectComboboxEntry(int elementIndex, int option)
	{
		Date startTime = new Date();
		JemmyComboBoxOperator comboboxOperator = new JemmyComboBoxOperator(swingMainFrameHandle, elementIndex);
		comboboxOperator.selectItem(option);
		checkExecutionSpeed(startTime);
	}

	@Override
	public void selectComboboxEntry(int elementIndex, String option)
	{
		Date startTime = new Date();
		JemmyComboBoxOperator comboboxOperator = new JemmyComboBoxOperator(swingMainFrameHandle, elementIndex);
		
		if (comboboxOperator.getSelectedItem().toString().equals(option)) {
			return;
		}
		
		int itemCount = comboboxOperator.getOperator().getItemCount();
		boolean ok = false;
		for (int i = 0; i < itemCount; i++) 
		{
			ok = comboboxOperator.getOperator().getItemAt(i).equals(option);
			if (ok) break;
		}
		if (! ok) {
			throw new RuntimeException("Die Combobox mit dem Index '" + elementIndex + "' beinhaltet nicht die Option '" + option + "'");
		}
		comboboxOperator.selectItem(option);
		checkExecutionSpeed(startTime);
	}
	
	
	@Override
	public void selectComboboxEntry(String elementIdentifier, int option)
	{
		Date startTime = new Date();
		JemmyComboBoxOperator comboboxOperator = new JemmyComboBoxOperator(swingMainFrameHandle, new NameComponentChooser(elementIdentifier));
		comboboxOperator.selectItem(option);
		checkExecutionSpeed(startTime);
	}

	@Override
	public void selectComboboxEntry(String elementIdentifier, String option)
	{
		Date startTime = new Date();
		JemmyComboBoxOperator comboboxOperator = new JemmyComboBoxOperator(swingMainFrameHandle, new NameComponentChooser(elementIdentifier));
		
		if (comboboxOperator.getSelectedItem().toString().equals(option)) {
			return;
		}
		
		int itemCount = comboboxOperator.getOperator().getItemCount();
		boolean ok = false;
		for (int i = 0; i < itemCount; i++) 
		{
			ok = comboboxOperator.getOperator().getItemAt(i).equals(option);
			if (ok) break;
		}
		if (! ok) {
			throw new RuntimeException("Die Combobox '" + elementIdentifier + "' beinhaltet nicht die Option '" + option + "'");
		}
		comboboxOperator.selectItem(option);
		checkExecutionSpeed(startTime);
	}

	@Override
	public boolean isEntryInComboboxDropdownAvailable(String elementIdentifier, String value)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getSelectedComboBoxEntry(String elementIdentifier)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumberOfLinesInTextArea(String xpath)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void clickLink(String idToClick)
	{
		Date startTime = new Date();
		// TODO Auto-generated method stub		
		checkExecutionSpeed(startTime);
	}

	@Override
	public void clickLink(String idToClick, String idToScrollIntoView)
	{
		Date startTime = new Date();
		// TODO Auto-generated method stub
		
		checkExecutionSpeed(startTime);
	}

	@Override
	public void clickLink(String idToClick, int positionOfOccurrence)
	{
		Date startTime = new Date();
		// TODO Auto-generated method stub
		
		checkExecutionSpeed(startTime);
	}

	@Override
	public void clickLink(String idToClick, String idToScrollIntoView, int positionOfOccurrence)
	{
		Date startTime = new Date();
		// TODO Auto-generated method stub
		
		checkExecutionSpeed(startTime);
	}

	@Override
	public List<WebElement> getElements(String elementIdentifier)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTagName(String elementIdentifier)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isTextCurrentlyDisplayed(String text)
	{
		return false;
	}

	public void waitUntilMainFrameIsAvailable() {
		getGuiHandle();
	}

	@Override
	public boolean waitFileChooserIsVisible(int timeoutInMillis) {
		Date startTime = new Date();
		try {
			return JFileChooserOperator.findJFileChooser() != null;
		} catch (Exception e) {
			return false;
		} finally {
			checkExecutionSpeed(startTime);
		}
	}
	
	@Override
	public void setFilePathInFileChooser(String filePath)
	{
		Date startTime = new Date();
		JFileChooserOperator.findJFileChooser().setSelectedFile(new File(filePath));
		checkExecutionSpeed(startTime);
	}
	

	@Override
	public void waitUntilEnabledElementIsAvailable(String elementIdentifier) {
		waitUntilEnabledElementIsAvailable(elementIdentifier, 1);
	}
	
	@Override
	public void waitUntilElementIsAvailable(String elementIdentifier)
	{
		JComponentOperator c = findComponent(swingMainFrameHandle, elementIdentifier, 1000);
		if (c == null) throw new SysNatException("Expected GUI element not found: " + elementIdentifier);
		if (isComponentAvailable(c)) return;  
		throw new SysNatException("GUI element is not available: " + elementIdentifier);
	}
	
	
	@Override
	public void waitUntilDialogElementIsAvailable(String dialogName, String elementIdentifier, int timeoutInSeconds)
	{
		checkDialog(dialogName);
		waitUntilElementIsAvailable(currentDialogHandle, elementIdentifier, timeoutInSeconds);
	}
	
	@Override
	public String checkForAnyDialog(int timeoutInMillis)
	{
		resetDialogHandle();
		String toReturn = null;
		JemmyDialogOperator dialogHandle = new JemmyDialogOperator(timeoutInMillis);
		
		if (dialogHandle.getOperator() != null) 
		{
			toReturn = dialogHandle.getOperator().getTitle();
			if (toReturn == null || toReturn.isEmpty()) 
			{
				String name = dialogHandle.getOperator().getName();
				if (name != null) {
					toReturn = name;
				}
			}
		}

		currentDialogName = toReturn;
		currentDialogHandle = dialogHandle.getOperator();
		return toReturn;
	}
	
	@Override
	public boolean waitForDialogToAppear(String dialogName, int timeoutInMillis)
	{
		Date startTime = new Date();
		JemmyDialogOperator dialogHandle = null;
		boolean ok = false;
		
		try {
			dialogHandle = new JemmyDialogOperator(dialogName, timeoutInMillis);
			ok = dialogHandle.getOperator().getTitle().equals(dialogName);
		} catch (Exception e) {
			dialogHandle = new JemmyDialogOperator(timeoutInMillis);
			ok = dialogHandle != null && dialogHandle.getOperator() != null && dialogName.equals(dialogHandle.getOperator().getName());
		}
		
		if (ok) {
			System.out.println("#################################################################");
			System.out.println("###################### found dialog " + dialogName);
			System.out.println("#################################################################");

			currentDialogHandle = dialogHandle.getOperator();
			currentDialogName = dialogName;
		} else {
			if ("Inka Frage".equals(dialogName)) {
				problemcount++;
				if (problemcount == 1) {
					System.out.println("");
				}
			}
			System.err.println("#################################################################");
			System.err.println("###################### unknown dialog " + dialogName);
			System.err.println("#################################################################");
			currentDialogHandle = null;
			currentDialogName = null;
		}
		
		checkExecutionSpeed(startTime);
		return ok;
	}	
	
	public int findIndexOfComponent(String name, Class<? extends JComponent> type) 
	{
		if (type == JTextField.class) return findIndexForTextfield(name);
		if (type == JComboBox.class) return findIndexForCombobox(name);
	
		throw new RuntimeException("findIndexOfComponent does not yet support " + type.getName());
	}
	
	private int findIndexForTextfield(String name)
	{
		int index = -1;
		boolean goon = true;
		while (goon) 
		{
			index++;
			try {				
				JTextFieldOperator result = new JTextFieldOperator(swingMainFrameHandle, index);
				if (result.getName() != null && result.getName().equals(name)) {
					return index;
				}
			} catch (Exception e) {
				goon = false;
			}
		}
		return -1;
	}
	
	private int findIndexForCombobox(String name)
	{
		int index = -1;
		boolean goon = true;
		while (goon) 
		{
			index++;
			try {				
				JComboBoxOperator result = new JComboBoxOperator(swingMainFrameHandle, index);
				if (result != null && result.getName().equals(name)) {
					return index;
				}
			} catch (Exception e) {
				goon = false;
			}
		}
		return -1;
	}

	public void waitUntilEnabledElementIsAvailable(String elementIdentifier, int timeoutInSeconds) {
		waitUntilElementIsAvailable(swingMainFrameHandle, elementIdentifier, timeoutInSeconds);
	}

	@Override
	public boolean waitToDisappear(String text, int maxSecondsToWait)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getCurrentlyActiveWindowTitle()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void switchToWindow(String windowID)
	{
		Date startTime = new Date();
		// TODO Auto-generated method stub
		
		checkExecutionSpeed(startTime);
	}

	@Override
	public void switchToLastWindow()
	{
		Date startTime = new Date();
		// TODO Auto-generated method stub
		
		checkExecutionSpeed(startTime);
	}

	@Override
	public int getNumberOfOpenApplicationWindows()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void switchToFirstWindow()
	{
		Date startTime = new Date();
		// TODO Auto-generated method stub
		checkExecutionSpeed(startTime);
	}

	public String readTextFromLabel(ContainerOperator handle, String technicalLabelName) {
		return new JemmyLabelOperator(handle, new NameComponentChooser(technicalLabelName)).getText();
	}
	
	public String readTextFromCombobox(ContainerOperator handle, String technicalLabelName) {
		return new JemmyComboBoxOperator(handle, new NameComponentChooser(technicalLabelName)).getSelectedItem();
	}

	public String readTextFromLabel(ContainerOperator handle, int index) {
		return new JemmyLabelOperator(handle, index).getText();
	}

	@Override
	public void maximizeWindow() 
	{
		if (getMainFrameHandle() != null) {
			getMainFrameHandle().maximize();
			//getMainFrameHandle().setState(Frame.MAXIMIZED_BOTH);
		}
	}
	
	@Override
	public void minimizeWindow() 
	{
		if (getMainFrameHandle() != null) {
			getMainFrameHandle().setState(Frame.ICONIFIED);
		}
	}
	
	
	@Override
    public boolean clickMenuItem(final String elementAsString) {
		Date startTime = new Date();
		JMenuItemOperator operator = new JemmyMenuItemOperator(swingMainFrameHandle, elementAsString,  AbstractJemmyOperator.TIMEOUT).getOperator();
		operator.setSelected(true);
		operator.setFocusPainted(true);
		if (! operator.isEnabled()) {
			return false;
		}
		operator.clickMouse();
		checkExecutionSpeed(startTime);
		return true;
    }


	@Override
	public void setFocusTo(String elementIdentifier) {
		Date startTime = new Date();
		findComponent(swingMainFrameHandle, elementIdentifier).grabFocus();
		checkExecutionSpeed(startTime);
	}
	
	// ####################################################################################################
	//                             P r i v a t e   M e t h o d s
	// ####################################################################################################

	private JComponentOperator findComponent(ContainerOperator handle, String technicalComponentName) {
		return findComponent(handle, technicalComponentName, AbstractJemmyOperator.TIMEOUT);
	}

	private JComponentOperator findComponent(ContainerOperator handle, String technicalComponentName, long timeout) {
		return new JemmyComponentOperator(handle, new NameComponentChooser(technicalComponentName), timeout).getOperator();
	}
	
	private boolean checkDialog(String dialogName) 
	{
		if (dialogName != currentDialogName || currentDialogHandle == null) {
			return waitForDialogToAppear(dialogName, 1000);
		}
		return true;
	}
	
	private void checkTable(String tableIdentifier)
	{
		System.out.println("#################################################################");
		System.out.println("###################### checkTable start " + tableIdentifier);
		System.out.println("#################################################################");
		if (tableIdentifier != currentTableName || currentTableHandle == null) {
			loadTable(tableIdentifier);
		}
		System.out.println("#################################################################");
		System.out.println("###################### checkTable end " + tableIdentifier);
		System.out.println("#################################################################");
	}
	
	private void loadTable(String tableIdentifier)
	{
		currentTableHandle = new JemmyTableOperator(swingMainFrameHandle, new NameComponentChooser(tableIdentifier));
		currentTableName = tableIdentifier;
	}
	
	
	private void checkDialogTable(String dialogName, String tableIdentifier)
	{
		checkDialog(dialogName);
		if (tableIdentifier != currentTableName || currentTableHandle == null) {
			currentTableHandle = new JemmyTableOperator(currentDialogHandle, new NameComponentChooser(tableIdentifier));
			currentTableName = tableIdentifier;
		}
	}

	
	private void clearTextfield(ContainerOperator handle, String technicalTextFieldName) 
	{
		JemmyTextfieldOperator textfieldHandle = 
				new JemmyTextfieldOperator(handle, new NameComponentChooser(technicalTextFieldName));
		textfieldHandle.clear();
	}
	
	private void enterInTextfield(ContainerOperator handle, String technicalTextFieldName, String text) 
	{
		if (handle == null) handle = new JFrameOperator();
		JemmyTextfieldOperator textfieldHandle = new JemmyTextfieldOperator(handle,
				new NameComponentChooser(technicalTextFieldName), 1000);
		textfieldHandle.setText(text);
	}
	
	private void enterInTextfield(ContainerOperator handle, int index, String text) 
	{
		if (handle == null) handle = new JFrameOperator();
		JemmyTextfieldOperator textfieldHandle = new JemmyTextfieldOperator(handle, index);
		textfieldHandle.setText(text);
	}
	
	private String readFromTextfield(ContainerOperator handle, String technicalTextFieldName) {
		JemmyTextfieldOperator textfieldHandle = new JemmyTextfieldOperator(handle,
				new NameComponentChooser(technicalTextFieldName));
		return textfieldHandle.getText();

	}
	
	private JButtonOperator getButtonOperator(ContainerOperator handle, String guiTextOrTechnicalId) {
		if (handle == null) handle = new JFrameOperator();
		return getButtonOperator(handle, guiTextOrTechnicalId, 1000);
	}
	
	private JButtonOperator getButtonOperator(ContainerOperator handle, int index) {
		if (handle == null) return null;
		return new JemmyButtonOperator(handle, index).getOperator();
	}
	
	private JButtonOperator getButtonOperator(ContainerOperator handle, String guiTextOrTechnicalId, long timeout) 
	{
		try {
			JemmyButtonOperator buttonOperator = new JemmyButtonOperator(handle, guiTextOrTechnicalId, timeout);
			if (buttonOperator==null || buttonOperator.getOperator() != null) {
				return buttonOperator.getOperator();
			}
		} catch (Exception e) {
			JemmyButtonOperator buttonOperator = new JemmyButtonOperator(handle, new NameComponentChooser(guiTextOrTechnicalId), timeout);
			if (buttonOperator==null || buttonOperator.getOperator() != null) {
				return buttonOperator.getOperator();
			}
		}
		return null;
	}

	private boolean isCheckboxTicked(ContainerOperator handle, String technicalCheckboxName) {
		JCheckBoxOperator checkboxOperator = new JCheckBoxOperator(handle, new NameComponentChooser(technicalCheckboxName));
		return checkboxOperator.isSelected();
	}

	private boolean isCheckboxTicked(ContainerOperator handle, int index) {
		JCheckBoxOperator checkboxOperator = new JCheckBoxOperator(handle, index);
		return checkboxOperator.isSelected();
	}

	private boolean untickDialogCheckboxIfTicked(ContainerOperator handle, String technicalCheckboxName) {
		try {
			JemmyCheckboxOperator checkboxOperator = new JemmyCheckboxOperator(handle, new NameComponentChooser(technicalCheckboxName));
			return untickDialogCheckboxIfTicked(checkboxOperator);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private boolean untickDialogCheckboxIfTicked(ContainerOperator handle, int index) {
		try {
			JemmyCheckboxOperator checkboxOperator = new JemmyCheckboxOperator(handle, index);
			return untickDialogCheckboxIfTicked(checkboxOperator);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private boolean untickDialogCheckboxIfTicked(JemmyCheckboxOperator checkboxOperator)
	{
		if (checkboxOperator.isSelected()) {
			checkboxOperator.click();
			return true;
		}
		return false;
	}

	private boolean tickCheckboxIfUnticked(ContainerOperator handle, String technicalCheckboxName) {
		try {
			JemmyCheckboxOperator checkboxOperator = new JemmyCheckboxOperator(handle, new NameComponentChooser(technicalCheckboxName));
			return tickCheckboxIfUnticked(checkboxOperator);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	
	private boolean tickCheckboxIfUnticked(JemmyCheckboxOperator checkboxOperator)
	{
		if ( ! checkboxOperator.isSelected()) {
			checkboxOperator.click();
			return true;
		}
		return false;
	}

	private boolean tickCheckboxIfUnticked(ContainerOperator handle, int index) {
		try {
			JemmyCheckboxOperator checkboxOperator = new JemmyCheckboxOperator(handle, index);
			return tickCheckboxIfUnticked(checkboxOperator);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void waitUntilElementIsAvailable(ContainerOperator handle, String elementIdentifier, int timeoutInSeconds)
	{
		JComponentOperator c = findComponent(handle, elementIdentifier, timeoutInSeconds*1000);
		if (c == null) throw new SysNatException("Expected GUI element not found: " + elementIdentifier);
		if (c.getSource() instanceof JLabel) {
			if (isComponentAvailable(c)) return;
		} else {
			if (isEnabledComponentAvailable(c)) return;  
		}

		throw new SysNatException("GUI element is not available: " + elementIdentifier);
	}

	private boolean isComponentAvailable(String elementIdentifier, long timeout, boolean onlyEnabled)
	{
		JComponentOperator operator = null;
		try {
			operator = findComponent(swingMainFrameHandle, elementIdentifier, timeout);
		} catch (Exception e) { /* Ignore */ }
		
		if (operator == null) {
			try {
				operator = findComponent(new JemmyDialogOperator(500).getOperator(), elementIdentifier, timeout);
			} catch (Exception e) { /* Ignore */ }
		}
		
		if (operator == null) return false;
		 		
		if (onlyEnabled) {
			return isEnabledComponentAvailable(operator);
		} else {
			return isComponentAvailable(operator);
		}
	}
	
	private boolean isEnabledComponentAvailable(JComponentOperator c) {
		return c.isEnabled() && c.isVisible() && c.isValid() && c.isDisplayable() && c.isShowing();
	}
	
	private boolean isComponentAvailable(JComponentOperator c) {
		return c.isVisible() && c.isValid() && c.isDisplayable();
	}

	public void analyseCurrentTextFields() {
		int index = -1;
		while (true) {
			index++;
			try {
				JTextFieldOperator operator = new JTextFieldOperator(swingMainFrameHandle, index);
				System.err.println(index + ". " + operator.getName() + "/" + operator.getText() + " - " 
				+ isEnabledComponentAvailable(operator));
			} catch (Exception e) {
				return;
			}
		}
	}
	
	public void analyseCurrentComboboxes() {
		int index = -1;
		while (true) {
			index++;
			try {
				JComboBoxOperator operator = new JComboBoxOperator(swingMainFrameHandle, index);
				System.err.println(index + ". " + operator.getName() + "/" + operator.getSelectedItem() + " - " 
				+ isEnabledComponentAvailable(operator));
			} catch (Exception e) {
				return;
			}
		}
	}
	
	public void clickFileChooserButton(String buttonIdentifier)
	{
		Date startTime = new Date();;
		JButton button = (JButton) findContainerInContainer(JFileChooserOperator.findJFileChooser().getParent(), JButton.class, buttonIdentifier);
		if (button != null) {
			button.doClick();
		} else {
			throw new RuntimeException("Button <b>" + buttonIdentifier + "</b> not found!");
		}
		//JFileChooserOperator.findJFileChooser().approveSelection(); does not work correctly, possible Jemmy bug, creates no output file
		//JFileChooserOperator.findJFileChooser().accept(file)approveSelection(); this may work correctly, but file is not available here
		checkExecutionSpeed(startTime);
	}

	/**
	 * Searches rescursively in all embedded containers for a wanted component.
	 * Note: effectively the methods works on components, but technically all components are containers.
	 * 
	 * @param component: input container
	 * @param componentType: type of component to search for
	 * @param componentText: text of component to search for
	 * @return search result: wanted component or null
	 */
	private Container findContainerInContainer(Container component, Class<? extends Container> componentType, String componentText)
	{
		Method method;
		try {
			method = component.getClass().getMethod("getComponents");
		} catch (NoSuchMethodException e) {
			return null;
		} catch (SecurityException e) {
			throw new RuntimeException("Error finding GUI element");
		}
		
		Component[] children;
		try {
			children = (Component[]) method.invoke(component);
		} catch (Exception e) {
			throw new RuntimeException("Error finding GUI element");
		}
		
		for (Component child : children) 
		{
			if (child instanceof JButton) {
				if (componentText.equals(getText(child))) {
					return (Container) child;
				}
			} else if (child instanceof Container) {
				Container result = findContainerInContainer((Container)child, componentType, componentText);
				if (result != null) return result;
			}
		}
		
		return null;
	}

	private String getText(Component result)
	{
		try {
			Method method = result.getClass().getMethod("getText");
			return (String) method.invoke(result);
		} catch (NoSuchMethodException e) {
			return null;
		} catch (Exception e) {
			throw new RuntimeException("Error finding GUI element");
		}
	}

}