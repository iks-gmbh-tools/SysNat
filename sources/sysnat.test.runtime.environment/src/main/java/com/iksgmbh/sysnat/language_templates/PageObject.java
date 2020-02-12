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

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.GuiType;
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

    // abstract methods
    public abstract String getPageName();    
    public abstract boolean isCurrentlyDisplayed();
       
    protected List<String> createList(String... elements)
    {
        List<String> toReturn = new ArrayList<String>();
        for (String element : elements) {
            toReturn.add(element);
        }
        return toReturn;
    }
    
    protected String findTechnicalId(SysNatConstants.GuiType guiType, String elementName)
    {
        final HashMap<String, List<String>> idMappings = idMappingCollection.get(guiType);
        
        if (idMappings == null) {
            throwUnsupportedGuiEventException(guiType, elementName);
        }
        
        final List<String> availableTechnicalIDs = idMappings.get(elementName);
        
        if (availableTechnicalIDs == null) {
            throwUnsupportedGuiEventException(guiType, elementName);
        }        
        
        for (String id: availableTechnicalIDs)
        {
            if (executableExample.isElementAvailable(id)) {
                return id;
            }
        }
        
        throwUnsupportedGuiEventException(guiType, elementName);
        return null;
    }
    
    
    // ########################################################################
    //                        GUI  Handling  Methods
    // ########################################################################
     
    
    public void clickButton(String buttonName)
    {
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.Button, buttonName);
        executableExample.clickButton(technicalId);
    }
 
    public void enterTextInField(String fieldName, String value)
    {
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.TextField, fieldName);
        executableExample.inputText(technicalId, value);
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
 
    public void tickCheckbox(String checkBoxTitle)
    {
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.CheckBox, checkBoxTitle);
        executableExample.clickElement(technicalId);
    }
    
    public boolean isCheckboxTicked(String checkBoxTitle)
    {
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.CheckBox, checkBoxTitle);
        return executableExample.isCheckboxSelected(technicalId);
    }
    
    public void clickLink(String linkText) 
    {
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.Link, linkText);
        executableExample.clickElement(technicalId);
    }
 
    public String getText(String fieldName)
    {
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.ElementToReadText, fieldName);
        return executableExample.getTextForElement(technicalId);
    }
 
    public void throwUnsupportedGuiEventException(SysNatConstants.GuiType guiElementType, String elementName)
    {
        String elementType = BUNDLE.getString(guiElementType.name());
        if (elementType == null) {
            elementType = guiElementType.name();
        }        
        String errorMessage = BUNDLE.getString("UnsupportedGuiEventExceptionMessage")
                .replace("xx", elementName)
                .replace("yy", elementType)
                .replace("zz", getPageName());
        throw ExceptionHandler.createNewUnsupportedGuiEventException(errorMessage);
    }
}
