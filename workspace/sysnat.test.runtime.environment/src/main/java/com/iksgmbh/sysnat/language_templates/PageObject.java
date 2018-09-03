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

import java.util.Locale;
import java.util.ResourceBundle;

import com.iksgmbh.sysnat.common.utils.SysNatConstants.GuiType;
import com.iksgmbh.sysnat.helper.ExceptionHandler;

/**
 * Contains general methods needed by page objects.
 * 
 * @author Reik Oberrath
 */
public interface PageObject
{
	public static ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/PageObjectApi", Locale.getDefault());
	
	public default void throwUnsupportedGuiEventException(GuiType guiElementType, String elementName) 
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

	
	public String getPageName();
	public void clickButton(String buttonName);
	public void enterTextInField(String fieldName, String value);
	public void chooseForCombobox(String fieldName, String value);
	public String getText(String identifierOfGuiElementToRead);
	public boolean isCurrentlyDisplayed();
}