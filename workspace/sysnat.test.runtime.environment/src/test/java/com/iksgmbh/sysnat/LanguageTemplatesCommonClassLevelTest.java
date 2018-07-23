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
import com.iksgmbh.sysnat._testcases.TestCaseUsingAmbiguousObjectData;
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
		ExecutableExample testCase = new TestCaseCallingUnknownScript();

		// act
		testCase.executeTestCase();

		// assert
		assertTrue("Expected error message not found.", doesMessagesContain(testCase.getReportMessages(), "Fehler: Ein Skript mit dem Namen <b>UnkownScript</b> ist nicht bekannt"));
	}

	
	@Test
	public void throwsExceptionForAmbiguousObjectData() throws Exception 
	{
		// arrange
		ExecutionRuntimeInfo.reset();
		ExecutionRuntimeInfo.setSysNatSystemProperty("sysnat.nls.lookup.file", "../sysnat.test.runtime.environment/src/test/resources/AvailableNaturalLanguageScripts.properties");
		ExecutionRuntimeInfo.setSysNatSystemProperty("sysnat.testdata.import.directory", "../sysnat.test.runtime.environment/src/test/resources/testData");
		ExecutionRuntimeInfo.setSysNatSystemProperty("sysnat.testdata.import.directory", "HomePageIKS");
		ExecutionRuntimeInfo.getInstance().setTestApplicationName("HomePageIKS");
		ExecutionRuntimeInfo.getInstance();
				
		
		try {
			// act
			new TestCaseUsingAmbiguousObjectData().executeTestCase();
			fail("Expected exception was not thrown!");
		} catch (Exception e) {
			// assert
			assertEquals("Error message", "Es gibt 2 Testdaten-Objekte. Zu welchem soll der Wert für <b>Name</b> hinzugefügt werden?", e.getMessage());
		}
	}
	
	@Test
	public void throwsExceptionForMissingObjectData() throws Exception 
	{
		// arrange
		try {
			TestCaseUsingMissingObjectData testCase = new TestCaseUsingMissingObjectData();
			testCase.setXXID("id");
			testCase.executeTestCase();
			fail("Expected exception was not thrown!");
		} catch (Exception e) {
			assertEquals("Error message", "Fehler: Es stehen keine Testdaten für das Skript <b>SimpleTestScript</b> zur Verfügung.", e.getMessage());
		}
	}
	
	
	@Test
	public void executesSimpleScript() throws Exception 
	{
		// arrange
		ExecutableExample testCase = new TestCaseCallingSimpleScript();

		// act
		testCase.executeTestCase();

		// assert
		assertTrue("Expected error message not found.", doesMessagesContain(testCase.getReportMessages(), "Skript Start: <b>SimpleTestScript</b>"));
		assertTrue("Expected error message not found.", doesMessagesContain(testCase.getReportMessages(), "Simple script executed."));
		assertTrue("Expected error message not found.", doesMessagesContain(testCase.getReportMessages(), "Skript Ende: <b>SimpleTestScript</b>"));
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
		ExecutableExample testCase = new TestCaseCallingMainScriptCallingSubscript();
		
		// act
		testCase.executeTestCase();

		// assert
		System.out.println(testCase.getReportMessages());
		
		assertTrue("Expected error message not found.", doesMessagesContain(testCase.getReportMessages(), "Skript Start: <b>MainTestScript</b>"));
		assertTrue("Expected error message not found.", doesMessagesContain(testCase.getReportMessages(), "Skript Start: <b>SubTestScript</b>"));
		assertTrue("Expected error message not found.", doesMessagesContain(testCase.getReportMessages(), "Subscript executed for aMenuName and aLink."));
		assertTrue("Expected error message not found.", doesMessagesContain(testCase.getReportMessages(), "Skript Ende: <b>SubTestScript</b>"));
		assertTrue("Expected error message not found.", doesMessagesContain(testCase.getReportMessages(), "Mainscript executed."));
		assertTrue("Expected error message not found.", doesMessagesContain(testCase.getReportMessages(), "Skript Ende: <b>MainTestScript</b>"));
	}

	@Test
	public void returnsTestsToExecute() 
	{
		// arrange
		ExecutionRuntimeInfo.setSysNatSystemProperty("settings.config", "../sysnat.test.runtime.environment/src/test/resources/testSettingConfigs/settingsHomePageIKS.config");
		ExecutableExample testCase = new TestCaseCallingMainScriptCallingSubscript();
		LanguageTemplatesCommon cut = new LanguageTemplatesCommonTestImpl(testCase);
		List<String> testCategoriesOfTestCase = new ArrayList<>();
		testCategoriesOfTestCase.add("A");
		testCategoriesOfTestCase.add("B");
		List<String> testCategoriesToExecute = new ArrayList<>();

		// act
		boolean result1 = cut.isThisTestToExecute(testCategoriesOfTestCase, testCategoriesToExecute);

		testCategoriesToExecute.add("A");
		boolean result2 = cut.isThisTestToExecute(testCategoriesOfTestCase, testCategoriesToExecute);
		
		testCategoriesToExecute.clear();
		testCategoriesToExecute.add("NICHT-C");
		boolean result3 = cut.isThisTestToExecute(testCategoriesOfTestCase, testCategoriesToExecute);

		testCategoriesToExecute.clear();
		testCategoriesToExecute.add("A");
		testCategoriesToExecute.add("NICHT-C");
		boolean result4 = cut.isThisTestToExecute(testCategoriesOfTestCase, testCategoriesToExecute);
		
		testCategoriesToExecute.clear();
		testCategoriesToExecute.add("X");
		testCategoriesToExecute.add("NICHT-Y");
		boolean result5 = cut.isThisTestToExecute(testCategoriesOfTestCase, testCategoriesToExecute);

		testCategoriesToExecute.clear();
		testCategoriesToExecute.add("D");
		testCategoriesToExecute.add("NICHT-B");
		boolean result6 = cut.isThisTestToExecute(testCategoriesOfTestCase, testCategoriesToExecute);
		
		testCategoriesToExecute.clear();
		testCategoriesToExecute.add("A");
		testCategoriesToExecute.add("NICHT-B");
		boolean result7 = cut.isThisTestToExecute(testCategoriesOfTestCase, testCategoriesToExecute);
		
		testCategoriesToExecute.clear();
		testCategoriesToExecute.add("X");
		boolean result8 = cut.isThisTestToExecute(testCategoriesOfTestCase, testCategoriesToExecute);

		testCategoriesToExecute.clear();
		testCategoriesToExecute.add("NICHT-Y");
		boolean result9 = cut.isThisTestToExecute(testCategoriesOfTestCase, testCategoriesToExecute);
		
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
