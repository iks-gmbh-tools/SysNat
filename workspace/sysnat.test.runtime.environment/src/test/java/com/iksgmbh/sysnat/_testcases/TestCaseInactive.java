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
package com.iksgmbh.sysnat._testcases;

import org.junit.Ignore;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat._testhelper.LanguageTemplatesCommonTestImpl;
import com.iksgmbh.sysnat._testhelper.TestCaseTestImpl;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;

@Ignore
public class TestCaseInactive extends TestCaseTestImpl 
{
	public TestCaseInactive()  {
		languageTemplatesCommon = new LanguageTemplatesCommonTestImpl(this);
		ExecutionRuntimeInfo.getInstance().setApplicationStarted(true);
	}
	
	@Override
	public void executeTestCase() 
	{
		try {
			languageTemplatesCommon.startNewXX("Inactive Test");
			languageTemplatesCommon.checkFilterCategory(SysNatConstants.NO_FILTER);
			languageTemplatesCommon.setActiveState("nein");	
		} catch (Exception e) {
			// ignore
		}
	}
}