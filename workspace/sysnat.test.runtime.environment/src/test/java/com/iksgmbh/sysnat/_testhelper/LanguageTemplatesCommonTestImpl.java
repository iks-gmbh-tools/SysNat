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
package com.iksgmbh.sysnat._testhelper;

import java.util.HashMap;

import org.junit.Before;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.WebLoginParameter;
import com.iksgmbh.sysnat.language_templates.LanguageTemplates;
import com.iksgmbh.sysnat.language_templates.common.LanguageTemplatesCommon;

public class LanguageTemplatesCommonTestImpl extends LanguageTemplatesCommon implements LanguageTemplates
{
	public LanguageTemplatesCommonTestImpl(ExecutableExample test) {
		super(test);
	}

	protected String getScriptDir() {
		return "com.iksgmbh.sysnat._testcases.testscripts";
	}
	
	@Before
	public boolean doesTestBelongToApplicationUnderTest() {
		return true;
	}

	@Override
	public void doLogout() {
	}

	@Override
	public boolean isStartPageVisible() {
		return false;
	}

	@Override
	public boolean isLoginPageVisible() {
		return false;
	}

	@Override
	public void gotoStartPage() {
	}

	@Override
	public void doLogin(HashMap<WebLoginParameter, String> startParameter) {
	}
	
}