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

import java.util.HashMap;

import com.iksgmbh.sysnat.common.utils.SysNatConstants.WebLoginParameter;

/**
 * Contains methods to be implemented in the LanguageTemplate file that is specific to the TestApplication.
 * but must be available in common code.
 * 
 * @author Reik Oberrath
 */
public interface LanguageTemplates
{	
	boolean isLoginPageVisible();
	
	public void doLogin(HashMap<WebLoginParameter,String> startParameter);
	
	public void doLogout();

	/**
	 * Returns true if the page is visible
	 * that is displayed after successfull login
	 */
	public boolean isStartPageVisible();

	/**
	 * Used to reset the GUI after each test in order to assure that the next test
	 * will start from same initial point which the page displayed after login.
	 * To do this, popups may be closed that remain evenually open.
	 */
	public void gotoStartPage();
	
}