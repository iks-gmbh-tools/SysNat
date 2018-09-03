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
package com.iksgmbh.sysnat;

import static com.iksgmbh.sysnat.helper.TestClassLevelTest.doesMessagesContain;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat._testcases.TestCaseCallingMainScriptCallingSubscript;
import com.iksgmbh.sysnat._testcases.TestCaseCallingSimpleScript;
import com.iksgmbh.sysnat._testcases.TestCaseCallingUnknownScript;
import com.iksgmbh.sysnat._testcases.TestCaseUsingMissingObjectData;
import com.iksgmbh.sysnat._testhelper.LanguageTemplatesCommonTestImpl;
import com.iksgmbh.sysnat.language_templates.common.LanguageTemplatesCommon;

public class LanguageTemplatesCommonClassLevelTest 
{
	
	@Before
	public void setup() {
		ExecutionRuntimeInfo.reset();
		ExecutionRuntimeInfo.setSysNatSystemProperty("sysnat.nls.lookup.file", "../sysnat.test.runtime.environment/src/test/resources/AvailableNaturalLanguageScripts.properties");
		ExecutionRuntimeInfo.getInstance();
	}
	
	@Test
	public void throwsExceptionForUnkownScript() throws Exception 
	{
		// arrange
		ExecutableExample executableExample = new TestCaseCallingUnknownScript();

		// act
		executableExample.executeTestCase();

		// assert
		assertTrue("Expected error message not found.", doesMessagesContain(executableExample.getReportMessages(), "Fehler: Ein Skript mit dem Namen <b>UnkownScript</b> ist nicht bekannt"));
	}
		
	@Test
	public void throwsExceptionForMissingObjectData() throws Exception 
	{
		// arrange
		try {
			TestCaseUsingMissingObjectData executableExample =  new TestCaseUsingMissingObjectData();
			executableExample.setXXID("id");
			executableExample.executeTestCase();
			fail("Expected exception was not thrown!");
		} catch (Exception e) {
			assertEquals("Error message", "Fehler: Es stehen keine Testdaten für das Skript <b>SimpleTestScript</b> zur Verfügung.", e.getMessage());
		}
	}
	
	
	@Test
	public void executesSimpleScript() throws Exception 
	{
		// arrange
		ExecutableExample executableExample =  new TestCaseCallingSimpleScript();

		// act
		executableExample.executeTestCase();

		// assert
		assertTrue("Expected error message not found.", doesMessagesContain(executableExample.getReportMessages(), "Skript Start: <b>SimpleTestScript</b>"));
		assertTrue("Expected error message not found.", doesMessagesContain(executableExample.getReportMessages(), "Simple script executed."));
		assertTrue("Expected error message not found.", doesMessagesContain(executableExample.getReportMessages(), "Skript Ende: <b>SimpleTestScript</b>"));
	}

	
	@Test
	public void executesScriptWithSubscriptAndTestData() throws Exception 
	{
		// arrange
		ExecutionRuntimeInfo.reset();
		ExecutionRuntimeInfo.setSysNatSystemProperty("sysnat.nls.lookup.file", "../sysnat.test.runtime.environment/src/test/resources/AvailableNaturalLanguageScripts.properties");
		ExecutionRuntimeInfo.setSysNatSystemProperty("settings.config", "../sysnat.test.runtime.environment/src/test/resources/testSettingConfigs/settingsHomePageIKS.config");
		ExecutionRuntimeInfo.setSysNatSystemProperty("sysnat.nls.lookup.file", "../sysnat.test.runtime.environment/src/test/resources/AvailableNaturalLanguageScripts.properties");
		ExecutionRuntimeInfo.setSysNatSystemProperty("sysnat.testdata.import.directory", "../sysnat.test.runtime.environment/src/test/resources/testData");
		ExecutionRuntimeInfo.getInstance().setTestApplicationName("HomePageIKS");
		ExecutionRuntimeInfo.getInstance();
		ExecutableExample executableExample =  new TestCaseCallingMainScriptCallingSubscript();
		
		// act
		executableExample.executeTestCase();

		// assert
		System.out.println(executableExample.getReportMessages());
		
		assertTrue("Expected error message not found.", doesMessagesContain(executableExample.getReportMessages(), "Skript Start: <b>MainTestScript</b>"));
		assertTrue("Expected error message not found.", doesMessagesContain(executableExample.getReportMessages(), "Skript Start: <b>SubTestScript</b>"));
		assertTrue("Expected error message not found.", doesMessagesContain(executableExample.getReportMessages(), "Subscript executed for aMenuName and aLink."));
		assertTrue("Expected error message not found.", doesMessagesContain(executableExample.getReportMessages(), "Skript Ende: <b>SubTestScript</b>"));
		assertTrue("Expected error message not found.", doesMessagesContain(executableExample.getReportMessages(), "Mainscript executed."));
		assertTrue("Expected error message not found.", doesMessagesContain(executableExample.getReportMessages(), "Skript Ende: <b>MainTestScript</b>"));
	}

	@Test
	public void returnsTestsToExecute() 
	{
		// arrange
		ExecutionRuntimeInfo.setSysNatSystemProperty("settings.config", "../sysnat.test.runtime.environment/src/test/resources/testSettingConfigs/settingsHomePageIKS.config");
		ExecutableExample executableExample =  new TestCaseCallingMainScriptCallingSubscript();
		LanguageTemplatesCommon cut = new LanguageTemplatesCommonTestImpl(executableExample);
		List<String> testCategoriesOfExecutableExample =  new ArrayList<>();
		testCategoriesOfExecutableExample.add("A");
		testCategoriesOfExecutableExample.add("B");
		List<String> testCategoriesToExecute = new ArrayList<>();

		// act
		boolean result1 = cut.isThisTestToExecute(testCategoriesOfExecutableExample, testCategoriesToExecute);

		testCategoriesToExecute.add("A");
		boolean result2 = cut.isThisTestToExecute(testCategoriesOfExecutableExample, testCategoriesToExecute);
		
		testCategoriesToExecute.clear();
		testCategoriesToExecute.add("NICHT-C");
		boolean result3 = cut.isThisTestToExecute(testCategoriesOfExecutableExample, testCategoriesToExecute);

		testCategoriesToExecute.clear();
		testCategoriesToExecute.add("A");
		testCategoriesToExecute.add("NICHT-C");
		boolean result4 = cut.isThisTestToExecute(testCategoriesOfExecutableExample, testCategoriesToExecute);
		
		testCategoriesToExecute.clear();
		testCategoriesToExecute.add("X");
		testCategoriesToExecute.add("NICHT-Y");
		boolean result5 = cut.isThisTestToExecute(testCategoriesOfExecutableExample, testCategoriesToExecute);

		testCategoriesToExecute.clear();
		testCategoriesToExecute.add("D");
		testCategoriesToExecute.add("NICHT-B");
		boolean result6 = cut.isThisTestToExecute(testCategoriesOfExecutableExample, testCategoriesToExecute);
		
		testCategoriesToExecute.clear();
		testCategoriesToExecute.add("A");
		testCategoriesToExecute.add("NICHT-B");
		boolean result7 = cut.isThisTestToExecute(testCategoriesOfExecutableExample, testCategoriesToExecute);
		
		testCategoriesToExecute.clear();
		testCategoriesToExecute.add("X");
		boolean result8 = cut.isThisTestToExecute(testCategoriesOfExecutableExample, testCategoriesToExecute);

		testCategoriesToExecute.clear();
		testCategoriesToExecute.add("NICHT-Y");
		boolean result9 = cut.isThisTestToExecute(testCategoriesOfExecutableExample, testCategoriesToExecute);
		
		// assert
		assertTrue( result1 );
		assertTrue( result2 );
		assertTrue( result3 );
		assertTrue( result4 );
		assertFalse( result5 );
		assertFalse( result6 );
		assertFalse( result7 );
		assertFalse( result8 );
		assertTrue( result9 );
	}
	
}