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
package com.iksgmbh.sysnat.language_templates.unittestapp.pageobject;

import java.util.*;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.GuiType;
import com.iksgmbh.sysnat.guicontrol.impl.*;
import com.iksgmbh.sysnat.language_templates.*;

/**
 * Implements actions that can be applied to this page.
 * Some standard actions are available from the parent class.
 * To use them, the idMappings must be defined.
 */

public class NewPageNamePageObject extends PageObject
{
	public NewPageNamePageObject(ExecutableExample executableExample, LanguageTemplateBasics aLanguageTemplateBasics)
	{
		super(aLanguageTemplateBasics);
		this.executableExample = executableExample;
		this.idMappingCollection = new HashMap<GuiType, HashMap<String, List<String>>>();

		HashMap<String, List<String>> idMappings = new HashMap<String, List<String>>();
		idMappingCollection.put(GuiType.TextField, idMappings);		

		idMappings = new HashMap<String, List<String>>();
		idMappingCollection.put(GuiType.Button, idMappings);
	}

	@Override
	public String getPageName() {
		return getClass().getSimpleName().replaceAll("Object", "");
	}

	@Override
	public boolean isCurrentlyDisplayed() {
	    return true;
	    // String uniqueComponentTechnicalId = "?";  TODO specify unique component of this page
		// return executableExample.getActiveGuiController().isElementAvailable(uniqueComponentTechnicalId, 500, true);
	}
}