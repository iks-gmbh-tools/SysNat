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

import com.iksgmbh.sysnat.language_templates.common.LanguageTemplatesCommon;
import com.iksgmbh.sysnat.test.language_container.CalculatorLanguageTemplatesContainer;
import com.iksgmbh.sysnat.testcasejavatemplate.SysNatTestCase;

public class BehaviourLevelInstructionsTestCase1Test extends SysNatTestCase
{
	private static final String BEHAVIOR_ID = "BehaviourId";

	private LanguageTemplatesCommon languageTemplatesCommon;
	private CalculatorLanguageTemplatesContainer calculatorLanguageTemplatesContainer;
	
	public BehaviourLevelInstructionsTestCase1Test() {
		executionInfo.register(BEHAVIOR_ID, 2);
	}
	
	@Before
	public void setup() 
	{
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
	
	private void prepareOnceIfNeeded()
	{
	    if (executionInfo.isFirstXXOfGroup(BEHAVIOR_ID))
	    {
	    	languageTemplatesCommon.createComment("This is a OneTimePrecondition instruction.");
	    }
	}
	
	@Override
	@Test
	public void executeTestCase() 
	{
		languageTemplatesCommon.declareXXGroupForBehaviour(BEHAVIOR_ID);
		prepareOnceIfNeeded();
		languageTemplatesCommon.startNewXX("XX1");
		calculatorLanguageTemplatesContainer.enterNumber("11");
		calculatorLanguageTemplatesContainer.enterNumber("22");
		calculatorLanguageTemplatesContainer.enterNumber("33");
		calculatorLanguageTemplatesContainer.calculateSum();
		calculatorLanguageTemplatesContainer.checkResult("99");
		cleanupOnceIfNeeded();
		
		closeCurrentTestCaseWithSuccess();
	}
	
	private void cleanupOnceIfNeeded()
	{
	    if (executionInfo.isLastXXOfGroup(BEHAVIOR_ID))
	    {
	    	languageTemplatesCommon.createComment("This is a OneTimeCleanup instruction.");
	    }
	}		
}