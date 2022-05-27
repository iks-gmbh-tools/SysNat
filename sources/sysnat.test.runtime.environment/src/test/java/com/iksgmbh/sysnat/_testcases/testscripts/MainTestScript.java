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
package com.iksgmbh.sysnat._testcases.testscripts;

import org.junit.Ignore;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat._testhelper.LanguageTemplatesCommonTestImpl;
import com.iksgmbh.sysnat.testcasejavatemplate.SysNatScript;


@Ignore
public class MainTestScript extends SysNatScript 
{
	private LanguageTemplatesCommonTestImpl languageTemplatesCommon;

	public MainTestScript(ExecutableExample callingTestCase) {
		super(callingTestCase);
	}
	
	@Override
	public void executeScript() {
		languageTemplatesCommon = new LanguageTemplatesCommonTestImpl(this);
		languageTemplatesCommon.executeScriptWithData("SubTestScript");
		addReportMessage("Mainscript executed."); 
	}
	

}