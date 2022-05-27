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
package com.iksgmbh.sysnat.language_templates.elo.pageobject;

import java.util.HashMap;
import java.util.List;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.GuiType;
import com.iksgmbh.sysnat.guicontrol.impl.SwingGuiController;
import com.iksgmbh.sysnat.language_templates.LanguageTemplateBasics;
import com.iksgmbh.sysnat.language_templates.PageObject;

/**
 * Implements actions that can be applied to this page.
 * Some standard actions are available from the parent class.
 * To use them, the idMappings must be defined.
 * 
 * @author Reik Oberrath
 */
public class LoginPageObject extends PageObject
{
	private static final String LoginDialogTitle = "ELO Anmeldung";
	
	private SwingGuiController guiControl;
	
	public LoginPageObject(ExecutableExample executableExample, LanguageTemplateBasics aLanguageTemplateBasics) 
	{
		super(aLanguageTemplateBasics);
		this.executableExample = executableExample;
		this.idMappingCollection = new HashMap<GuiType, HashMap<String, List<String>>>();
		
		HashMap<String, List<String>> idMappings = new HashMap<String, List<String>>();
		idMappings.put("Benutzername", createList("txtUsername"));
		idMappings.put("Passwort", createList("txtPasswort"));
		idMappingCollection.put(GuiType.TextField, idMappings);		

		idMappings = new HashMap<String, List<String>>();
		idMappings.put("Benutzername", createList("txtUsername"));
		idMappingCollection.put(GuiType.ElementToReadText, idMappings);		
		
    	guiControl = (SwingGuiController) executableExample.getActiveGuiController();
	}

	@Override
	public String getPageName() {
		return getClass().getSimpleName().replaceAll("Object", "");
	}

	@Override
	public boolean isCurrentlyDisplayed() {
		return executableExample.isElementAvailable("Password");
	}

	@Override
    public void clickButton(String buttonName) {
        guiControl.clickDialogElement(LoginDialogTitle, buttonName);
    }
	
	@Override
    public String getText(String fieldName)
    {
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.ElementToReadText, fieldName);
        return guiControl.getTextFromDialog(LoginDialogTitle, technicalId);
    }	
    
	@Override
    public void enterTextInTextField(String fieldName, String value)
    {
        final String technicalId = findTechnicalId(SysNatConstants.GuiType.TextField, fieldName);
        guiControl.enterTextInDialogTextField(LoginDialogTitle, technicalId, value);
    }
	
    protected String findTechnicalId(SysNatConstants.GuiType guiType, String elementName)
    {
        final HashMap<String, List<String>> idMappings = idMappingCollection.get(guiType);
        
        if (idMappings == null) {
            throwUnsupportedGuiEventException(guiType, elementName, "UnsupportedGuiEventExceptionMessage");
        }
        
        final List<String> availableTechnicalIDs = idMappings.get(elementName);
        
        if (availableTechnicalIDs == null) {
            throwUnsupportedGuiEventException(guiType, elementName, "UnsupportedGuiEventExceptionMessage");
        }        
        
        for (String id: availableTechnicalIDs)
        {
        	SwingGuiController guiControl = (SwingGuiController) executableExample.getActiveGuiController();
            if (guiControl.isDialogElementAvailable(LoginDialogTitle, id)) {
                return id;
            }
        }
        
        throwUnsupportedGuiEventException(guiType, elementName, "NonAccessibleGuiEventExceptionMessage");
        return null;
    }
    
}
