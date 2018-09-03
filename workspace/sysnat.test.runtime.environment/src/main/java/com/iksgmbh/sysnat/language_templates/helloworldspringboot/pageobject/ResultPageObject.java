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
package com.iksgmbh.sysnat.language_templates.helloworldspringboot.pageobject;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.GuiType;
import com.iksgmbh.sysnat.language_templates.PageObject;

/**
 * Maps names of GUI elements visible to the user to technical field IDs
 * and calls the gui controller to execute an action. 
 * 
 * @author Reik Oberrath
 */
public class ResultPageObject implements PageObject
{	
	private ExecutableExample executableExample;

	public ResultPageObject(ExecutableExample aTestCase) {
		this.executableExample = aTestCase;
	}

	@Override
	public String getPageName() {
		return getClass().getSimpleName().replaceAll("PageObject", "").replaceAll("HelloWorld", "");
	}

	@Override
	public void clickButton(String buttonName) {
		throwUnsupportedGuiEventException(GuiType.Button, buttonName);
	}

	@Override
	public void enterTextInField(String fieldName, String value) {
		throwUnsupportedGuiEventException(GuiType.TextField, fieldName);
	}

	@Override
	public String getText(String identifierOfGuiElementToRead) 
	{
		if ("GreetingResult".equals(identifierOfGuiElementToRead)) {
			return executableExample.getTextForElement("/html/body/p");
		} else {	
			throwUnsupportedGuiEventException(GuiType.ElementToReadText, identifierOfGuiElementToRead);
			return null;
		}
	}

	@Override
	public void chooseForCombobox(String fieldName, String value) {
		throwUnsupportedGuiEventException(GuiType.ComboBox, fieldName);
	}

	@Override
	public boolean isCurrentlyDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}