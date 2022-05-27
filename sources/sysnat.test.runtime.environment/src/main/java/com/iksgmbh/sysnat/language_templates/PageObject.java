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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.GuiType;
import com.iksgmbh.sysnat.guicontrol.impl.SwingGuiController;
import com.iksgmbh.sysnat.helper.ExceptionHandler;

/**
 * Abstract description of a PageObject 
 * with some default methods to be used as default implementations in sub classes.
 *
 * @author Reik Oberrath
 */
public abstract class PageObject
{
    private static ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/PageObjectApi", Locale.getDefault());
    
    /**
     * Contains for each element in the page a GuiType specific 1:n mapping.
     * The mappings are used to find for an element in the GUI its technical id
     * that is used by the GuiControl to handle the element. To support different technical id
     * in different environments, a list of technical ids can be defined.
     * 
     * Note for web applications:
     * Ideally, the id represents the element id in the html code. However, it is not always available.
     * Therefore the id here may also represent the element's name, XPath, tagname, classtext or linktext.
     * The SeleniumGuiController tries to identify an element by all these means. In case, an id is not
     * unique, you can define here an id that looks like that: aName::anID 
     * In case the combination of name and id is unique, the SeleniumGuiController will use both
     * to identify the element you wish to address.
     */
    protected HashMap<GuiType, HashMap<String, List<String>>> idMappingCollection;

    /**
     * Instance must be set in concrete subclass.
     */
	protected ExecutableExample executableExample;
	protected LanguageTemplateBasics languageTemplateBasics;

    // abstract methods
    public abstract String getPageName();    
    public abstract boolean isCurrentlyDisplayed();
    
    
    public PageObject(LanguageTemplateBasics aLanguageTemplateBasics) {
    	this.languageTemplateBasics = aLanguageTemplateBasics;
    }
       
    protected List<String> createList(String... elements)
    {
        List<String> toReturn = new ArrayList<String>();
        for (String element : elements) {
            toReturn.add(element);
        }
        return toReturn;
    }
    
    protected String findTechnicalId(SysNatConstants.GuiType guiType, String elementName) {
    	return findTechnicalId(guiType, elementName, true);
    }
    
    protected String findTechnicalId(SysNatConstants.GuiType guiType, String elementName, boolean onlyEnabled)
    {
        final HashMap<String, List<String>> idMappings = idMappingCollection.get(guiType);
        
        if (idMappings == null) {
            throwUnsupportedGuiEventException(guiType, elementName, "UnsupportedGuiEventExceptionMessage");
        }
        
        final List<String> availableTechnicalIDs = idMappings.get(elementName);
        
        if (availableTechnicalIDs == null || availableTechnicalIDs.isEmpty()) {
            throwUnsupportedGuiEventException(guiType, elementName, "UnsupportedGuiEventExceptionMessage");
        }        
        
        for (String id: availableTechnicalIDs)
        {
        	if (ExecutionRuntimeInfo.getInstance().getTestApplication().isSwingApplication()) 
        	{
	        	if (guiType == GuiType.TextField) {
	                if (((SwingGuiController)executableExample.getActiveGuiController()).isElementAvailable(id, JTextField.class, 500, onlyEnabled)) {
	                    return id;
	                }	
				}
	        	if (guiType == GuiType.ComboBox) {
	                if (((SwingGuiController)executableExample.getActiveGuiController()).isElementAvailable(id, JComboBox.class, 500, onlyEnabled)) {
	                    return id;
	                }	
				}
        	}
        	if (executableExample.getActiveGuiController().isElementAvailable(id, 500, false)) {
        		return id;
        	}	
        }
        
        throwUnsupportedGuiEventException(guiType, elementName, "NonAccessibleGuiEventExceptionMessage");
        return null;
    }
    
    
    // ########################################################################
    //                        GUI  Handling  Methods
    // ########################################################################

	public void editTableCell(String tableIndentifier, int rowNumber, int columnNumber, Object value)	
	{
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.Table, tableIndentifier);
        executableExample.getActiveGuiController().editTableCell(technicalId, rowNumber, columnNumber, value);
	}
	
	public void clickCellInTable(String tableName, String columnName, int rowIndex)
	{
		final String technicalId = findTechnicalId(SysNatConstants.GuiType.Table, tableName);
        List<String> tableHeaders = executableExample.getActiveGuiController().getTableHeaders(technicalId);
        int columnIndex = getColumnIndex(columnName, tableHeaders);
        executableExample.getActiveGuiController().clickTableCell(technicalId, rowIndex+1, columnIndex+1);
	}
	
	public int countNumberOfRowsInTable(String tableName)
	{
		final String technicalId = findTechnicalId(SysNatConstants.GuiType.Table, tableName);
		return executableExample.getActiveGuiController().getNumberOfRowsInTable(technicalId);
	}
	
    public void throwUnsupportedGuiEventException(SysNatConstants.GuiType guiElementType, String elementName, String errorType)
    {
        String elementType = BUNDLE.getString(guiElementType.name());
        if (elementType == null) {
            elementType = guiElementType.name();
        }        
        new RuntimeException().printStackTrace();
        String errorMessage = BUNDLE.getString(errorType)
                .replace("xx", elementName)
                .replace("yy", elementType)
                .replace("zz", getPageName());
        throw ExceptionHandler.createNewUnsupportedGuiEventException(errorMessage);
    }
    
    public void clickButton(String buttonIdentifier)
    {
    	try {
    		final String technicalId = findTechnicalId(SysNatConstants.GuiType.Button, buttonIdentifier);
    		executableExample.clickButton(technicalId);
		} catch (Exception e) {
			executableExample.clickButton(buttonIdentifier);
		}
    	executableExample.sleep(1000);
    }
	
    public void clickDialogButton(String dialogTitle, String buttonName)
    {
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.Button, buttonName);
        executableExample.getActiveGuiController().clickDialogButton(dialogTitle, technicalId);
    }

    public void enterTextInTextField(String fieldName, String value)
    {
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.TextField, fieldName);
       	if (! executableExample.getActiveGuiController().isTextFieldEnabled(technicalId)) {
        	executableExample.failWithMessage("Das Textfeld <b>" + fieldName + "</b> ist nicht aktiv.");
    	}
    	if (! executableExample.getActiveGuiController().isTextFieldEditable(technicalId)) {
    		executableExample.failWithMessage("Das Textfeld <b>" + fieldName + "</b> ist nicht editierbar.");
    	}
    	executableExample.inputText(technicalId, value);
    }    
    
    public void enterTextInTextArea(String areaName, String value)
    {
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.TextArea, areaName);
        executableExample.inputTextInTextArea(technicalId, value);
    }    


    public void enterDateInDateField(String fieldName, String value)
    {
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.DateField, fieldName);
        executableExample.inputDate(technicalId, value);
    }
    
    public void chooseInCombobox(String fieldName, String value)
    {
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.ComboBox, fieldName);
        try {        	
        	executableExample.chooseFromComboBoxByValue(technicalId, value);
        } catch (NoSuchElementException e) {
            String errorMessage = BUNDLE.getString("UnknownComboboxEntry")
                    .replace("xx", fieldName)
                    .replace("yy", value);

        	throw ExceptionHandler.createNewUnsupportedGuiEventException(errorMessage);
        }    
     }
 
    public void selectRadioButtons(String selection, String option)
    {
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.RadioButtonSelection, selection + "::" + option);
        executableExample.clickElement(technicalId);
    }

    public void clickRadioButton(String radioButtonName)
    {
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.RadioButtonSelection, radioButtonName);
        executableExample.getActiveGuiController().clickRadioButton(technicalId);
    }
    
 // #########################  C H E C K B O X  ####################################
    
    public boolean isCheckboxTicked(String checkBoxDisplayName)
    {
    	final String technicalId = findTechnicalId(SysNatConstants.GuiType.CheckBox, checkBoxDisplayName);
    	return executableExample.getActiveGuiController().isCheckBoxTicked(technicalId);
    }

    public boolean isCheckboxTicked(int checkBoxIndex)
    {
    	return executableExample.getActiveGuiController().isCheckBoxTicked(checkBoxIndex);
    }
    
    public boolean isDialogCheckboxTicked(String dialogName, String checkBoxDisplayName)
    {
    	final String technicalId = findTechnicalId(SysNatConstants.GuiType.CheckBox, checkBoxDisplayName);
    	return executableExample.getActiveGuiController().isDialogCheckBoxTicked(dialogName, technicalId);
    }
    
    public boolean isDialogCheckboxTicked(String dialogName, int checkBoxIndex)
    {
    	return executableExample.getActiveGuiController().isDialogCheckBoxTicked(dialogName, checkBoxIndex);
    }
    
    public boolean assureTickInCheckBox(String checkBoxTitle)
    {
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.CheckBox, checkBoxTitle);
        return executableExample.getActiveGuiController().assureTickInCheckBox(technicalId);
    }
    
    public boolean assureTickInCheckBox(int checkBoxIndex)
    {
        return executableExample.getActiveGuiController().assureTickInCheckBox(checkBoxIndex);
    }  

    public boolean assureTickInDialogCheckBox(String dialogName, String checkBoxTitle)
    {
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.CheckBox, checkBoxTitle);
        return executableExample.getActiveGuiController().assureTickInDialogCheckBox(dialogName, technicalId);
    }    

    public boolean assureTickInDialogCheckBox(String dialogName, int checkBoxIndex)
    {
        return executableExample.getActiveGuiController().assureTickInDialogCheckBox(dialogName, checkBoxIndex);
    }    
    
	public boolean assureNoTickInCheckBox(String checkBoxDisplayName)
	{
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.CheckBox, checkBoxDisplayName);
        return executableExample.getActiveGuiController().assureNoTickInCheckBox(technicalId);
	}
	
	public boolean assureNoTickInCheckBox(int checkBoxIndex)
	{
        return executableExample.getActiveGuiController().assureNoTickInCheckBox(checkBoxIndex);
	}

	public boolean assureNoTickInDialogCheckBox(String dialogName, String checkBoxDisplayName)
	{
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.CheckBox, checkBoxDisplayName);
        return executableExample.getActiveGuiController().assureNoTickInDialogCheckBox(dialogName, technicalId);
	}

	public boolean assureNoTickInDialogCheckBox(String dialogName, int checkBoxIndex)
	{
        return executableExample.getActiveGuiController().assureNoTickInDialogCheckBox(dialogName, checkBoxIndex);
	}
	
	// #########################   M I S C   ####################################
	
    public void clickLink(String linkText) 
    {
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.Link, linkText);
        executableExample.clickElement(technicalId);
    }
 
    public void findInTree(String treeName, String elementName) {
    	executableExample.getActiveGuiController().containsTreeElement(treeName, elementName);
    }
    
    public void expandKnotInTree(String treeName, String knotName) {
    	executableExample.getActiveGuiController().expandKnotInTree(treeName, knotName);
    }
    
    
    /**
     * If is there only one tabbedPane 
     * @param title of the tab
     */
    public void switchToTab(String tabTitle) {
    	executableExample.switchToTab(null, tabTitle);
    }

    /**
     * If is there only one tabbedPane 
     * @param index of the tab
     */
    public void switchToTab(int tabIndex) {
    	executableExample.switchToTab(null, tabIndex);
    }

    public void switchToTab(String tabbedPaneIdentifier, String tabTitle) {
    	executableExample.switchToTab(tabbedPaneIdentifier, tabTitle);
    }

    public void switchToTab(String tabbedPaneIdentifier, int tabIndex) {
    	executableExample.switchToTab(tabbedPaneIdentifier, tabIndex);
    }
    
    
    /**
     * Reads content of a single field.
     * @param fieldName
     * @return field content
     */
    public String getText(String fieldName)
    {
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.ElementToReadText, fieldName, false);
        return executableExample.getTextForElement(technicalId);
    }

	public String getTextFromTable(String tableName, int colNumber, int rowNumber)
	{
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.Table, tableName);
		return executableExample.getTextFromTable(technicalId, rowNumber, colNumber);
	}

	public String getTextFromTable(String tableName, String columnName, int rowNumber)
	{
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.Table, tableName);
        List<String> tableHeaders = executableExample.getActiveGuiController().getTableHeaders(technicalId);
        int columnNumber = getColumnIndex(columnName, tableHeaders) + 1;
        
        if (columnNumber < 0) 
        {
        	executableExample.failWithMessage("In der Tabelle <b>" + tableName + "</b> wurde die Spalte <b>" + columnName + "</b> nicht gefunden.");
        	return null;
        }
        
        return this.getTextFromTable(tableName, columnNumber, rowNumber);
	}
	
	protected int getColumnIndex(String columnName, List<String> tableHeaders)
	{
		int columnNumber = -1;
        
        for (String header : tableHeaders) 
        {
        	columnNumber++;
        	if (header.equals(columnName)) {
        		break;
        	}
		}
		return columnNumber;
	}
	
	public int selectMatchingRowsInTable(String tableName, String columnName, String cellContent)
	{
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.Table, tableName);
        List<Integer> result = executableExample.getActiveGuiController().searchRowsInTable(technicalId, columnName + "==" + cellContent);
        result.forEach(row -> executableExample.getActiveGuiController().selectRowInTable(technicalId, row));
        return result.size();
	}
	
	public int selectMatchingRowsInTable(String dialogName, String tableName, String columnName, String cellContent) 
	{
		boolean ok = executableExample.getActiveGuiController().waitForDialogToAppear(dialogName, 5000);
		if (! ok) return -1;
		final String technicalId = findTechnicalId(SysNatConstants.GuiType.Table, tableName);
		List<Integer> result = executableExample.getActiveGuiController().searchRowsInDialogTable(dialogName, columnName + "==" + cellContent);
		result.forEach(row -> executableExample.getActiveGuiController().selectRowInDialogTable(technicalId, row));
        return result.size();
	}
	
	public int getFirstMatchingRowInTable(String tableName, String columnName, String cellContent)
	{
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.Table, tableName);
        List<Integer> result = executableExample.getActiveGuiController().searchRowsInTable(technicalId, columnName + "==" + cellContent);
        if (result.size() == 0) return -1;
        return result.get(0);
	}
	
	public Integer getFirstMatchingRowInTable(String tableName, String searchCriteria)
	{
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.Table, tableName);
        //List<String> tableHeaders = executableExample.getActiveGuiController().getTableHeaders(technicalId);
        List<Integer> result = executableExample.getActiveGuiController().searchRowsInTable(technicalId, searchCriteria);
        if (result.isEmpty()) {
        	return null;
        }
        return result.get(0);
	}		
	
	public int countMatchingRowsInTable(String tableName, String searchCriteria)
	{
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.Table, tableName);
        return executableExample.getActiveGuiController().searchRowsInTable(technicalId, searchCriteria).size();
	}	
	
	public int getNumberOfRowsFromTable(String tableName)
	{
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.Table, tableName);
		return executableExample.getActiveGuiController().getNumberOfRowsInTable(technicalId);
	}
	
	public void selectAllRowsInTable(String tableName) 
	{
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.Table, tableName);
        executableExample.getActiveGuiController().selectAllRowsInTable(technicalId);
	}
	
	public int getColumnNumberInTable(String tableName, String colName)
	{
		final String technicalId = findTechnicalId(SysNatConstants.GuiType.Table, tableName);
		List<String> result = executableExample.getActiveGuiController().getTableHeaders(technicalId);
		for (int i = 0; i < result.size(); i++) {
			if (result.get(i).equals(colName)) {
				return i;
			}
		}
		return -1;
	}
	
	public static int checkRowNumber(String rowNumber)
	{
		int row = 0;
		try {
			row = Integer.valueOf(rowNumber);
		} catch (Exception e) {
			throw new RuntimeException("Given RowNumber is no number!");
		}
		
		if (row < 1) {
			throw new RuntimeException("RowNumber smaller than 1 is not possible!");
		}
		return row;
	}
	
	
	public void removeAllText(String fieldName)
	{
		final String technicalId = findTechnicalId(SysNatConstants.GuiType.TextField, fieldName);
    	if (! executableExample.getActiveGuiController().isTextFieldEnabled(technicalId)) {
        	executableExample.failWithMessage("Das Textfeld <b>" + fieldName + "</b> ist nicht aktiv.");
    	}
    	if (! executableExample.getActiveGuiController().isTextFieldEditable(technicalId)) {
        	executableExample.failWithMessage("Das Textfeld <b>" + fieldName + "</b> ist nicht editierbar.");
    	}
    	executableExample.getActiveGuiController().removeAllText(technicalId);
	}
	
	public void waitUntilElementIsAvailable(SysNatConstants.GuiType type, String fieldName)
	{
		final String technicalId = findTechnicalId(type, fieldName);
		executableExample.waitUntilEnabledElementIsAvailable(technicalId);
	}
	
	public Object doubleClickTableCell(String tableName, int rowNo, int columnNo)
	{
		final String technicalId = findTechnicalId(SysNatConstants.GuiType.Table, tableName);
		executableExample.getActiveGuiController().doubleClickTableCell(technicalId, rowNo, columnNo);
		return null;
	}
	
	public List<Integer> searchRowsInTable(String tableName, String searchCriteria)
	{
		final String technicalId = findTechnicalId(SysNatConstants.GuiType.Table, tableName);
		return executableExample.getActiveGuiController().searchRowsInTable(technicalId, searchCriteria);
	}
	
	public Object getTableCellContent(String tableName, int rowNo, int columnNo)
	{
		final String technicalId = findTechnicalId(SysNatConstants.GuiType.Table, tableName);
		return executableExample.getActiveGuiController().getTableCellContent(technicalId, rowNo, columnNo);
	}
	
	public void selectRowInTableByNumber(String tableName, int rowNo)
	{
		final String technicalId = findTechnicalId(SysNatConstants.GuiType.Table, tableName);
		executableExample.getActiveGuiController().selectRowInTable(technicalId, rowNo);
	}

	public boolean isElementAvailable(SysNatConstants.GuiType type, String elementGuiName) 
	{
		try {			
			final String technicalId = findTechnicalId(type, elementGuiName);
			return executableExample.isElementAvailable(technicalId);
		} catch (Exception e) {
			return false;
		}
    }
}
