/* Copyright 2018 IKS Gesellschaft fuer Informations- und Kommunikationssysteme mbH
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
package com.iksgmbh.sysnat.test.calculatortestapp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.language_templates.common.LanguageTemplatesCommon;
import com.iksgmbh.sysnat.test.language_container.CalculatorLanguageTemplatesContainer;
import com.iksgmbh.sysnat.testcasejavatemplate.TestCaseTemplateParent;

public class BDDKeywordTestCaseTest extends TestCaseTemplateParent
{
	private LanguageTemplatesCommon languageTemplatesCommon;
	private CalculatorLanguageTemplatesContainer calculatorLanguageTemplatesContainer;
	
	@Before
	public void setup() {
		languageTemplatesCommon = new LanguageTemplatesCommon(this);
		calculatorLanguageTemplatesContainer = new CalculatorLanguageTemplatesContainer(this);
		executionInfo.setTestApplicationName("com");
		super.setUp();
	}
	
	@After
	public void shutdown() 
	{
		super.shutdown();
	}
	
	
	@Override
	@Test
	public void executeTestCase() 
	{
		String testCaseName = this.getClass().getSimpleName();
		languageTemplatesCommon.startNewXX(testCaseName);
		languageTemplatesCommon.setBddKeyword("Feature");
		languageTemplatesCommon.declareXXGroupForBehaviour("<filename>");
		languageTemplatesCommon.setBddKeyword("Scenario");
		languageTemplatesCommon.startNewXX("XX1");
		languageTemplatesCommon.setBddKeyword("Given");
		calculatorLanguageTemplatesContainer.enterNumber("1");
		languageTemplatesCommon.setBddKeyword("Given");
		calculatorLanguageTemplatesContainer.enterNumber("2");
		languageTemplatesCommon.setBddKeyword("When");
		calculatorLanguageTemplatesContainer.calculateSum();
		languageTemplatesCommon.setBddKeyword("Then");
		calculatorLanguageTemplatesContainer.checkResult("3");
		
		closeCurrentTestCaseWithSuccess();
	}
}