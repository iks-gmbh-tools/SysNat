package com.iksgmbh.sysnat;

import static com.iksgmbh.sysnat.helper.TestUtil.doesMessagesContain;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.iksgmbh.sysnat._testcases.TestCaseCallingMainScriptCallingSubscript;
import com.iksgmbh.sysnat._testcases.TestCaseCallingSimpleScript;
import com.iksgmbh.sysnat._testcases.TestCaseCallingUnknownScript;
import com.iksgmbh.sysnat._testcases.TestCaseUsingAmbiguousObjectData;
import com.iksgmbh.sysnat._testcases.TestCaseUsingMissingObjectData;
import com.iksgmbh.sysnat._testhelper.LanguageTemplatesBasicsTestImpl;
import com.iksgmbh.sysnat.domain.SysNatTestData;
import com.iksgmbh.sysnat.helper.VirtualTestCase;

public class LanguageTemplatesBasicsTest 
{

	@Test
	public void throwsExceptionForUnkownScript() throws Exception 
	{
		// arrange
		TestCase testCase = new TestCaseCallingUnknownScript();

		// act
		testCase.executeTestCase();

		// assert
		assertTrue("Expected error message not found.", doesMessagesContain(testCase.getReportMessages(), "Fehler: Ein Skript mit dem Namen <b>UnkownScript</b> ist nicht bekannt"));
	}

	@Test
	public void throwsExceptionForAmbiguousObjectData() throws Exception 
	{
		// arrange
		try {
			new TestCaseUsingAmbiguousObjectData().executeTestCase();
			fail("Expected exception was not thrown!");
		} catch (Exception e) {
			assertEquals("Error message", "Es gibt 2 Testdaten-Objekte. Zu welchem soll der Wert für <b>Name</b> hinzugefügt werden?", e.getMessage());
		}
	}
	
	@Test
	public void throwsExceptionForMissingObjectData() throws Exception 
	{
		// arrange
		try {
			TestCaseUsingMissingObjectData testCase = new TestCaseUsingMissingObjectData();
			testCase.setTestID("id");
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
		TestCase testCase = new TestCaseCallingSimpleScript();

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
		TestCase testCase = new TestCaseCallingMainScriptCallingSubscript();

		// act
		testCase.executeTestCase();

		// assert
		assertTrue("Expected error message not found.", doesMessagesContain(testCase.getReportMessages(), "Skript Start: <b>MainTestScript</b>"));
		assertTrue("Expected error message not found.", doesMessagesContain(testCase.getReportMessages(), "Skript Start: <b>SubTestScript</b>"));
		assertTrue("Expected error message not found.", doesMessagesContain(testCase.getReportMessages(), "Subscript executed for Jack Jackson (33)."));
		assertTrue("Expected error message not found.", doesMessagesContain(testCase.getReportMessages(), "Skript Ende: <b>SubTestScript</b>"));
		assertTrue("Expected error message not found.", doesMessagesContain(testCase.getReportMessages(), "Mainscript executed."));
		assertTrue("Expected error message not found.", doesMessagesContain(testCase.getReportMessages(), "Skript Ende: <b>MainTestScript</b>"));
	}

	@Test
	public void returnsTestsToExecute() 
	{
		LanguageTemplatesBasics cut = new LanguageTemplatesBasicsTestImpl(null);
		List<String> testCategoriesOfTestCase = new ArrayList<>();
		testCategoriesOfTestCase.add("A");
		testCategoriesOfTestCase.add("B");
		List<String> testCategoriesToExecute = new ArrayList<>();
		
		assertTrue( cut.isThisTestToExecute(testCategoriesOfTestCase, testCategoriesToExecute) );
		
		testCategoriesToExecute.add("A");
		assertTrue( cut.isThisTestToExecute(testCategoriesOfTestCase, testCategoriesToExecute) );
		
		testCategoriesToExecute.clear();
		testCategoriesToExecute.add("NICHT-C");
		assertTrue( cut.isThisTestToExecute(testCategoriesOfTestCase, testCategoriesToExecute) );
		
		testCategoriesToExecute.clear();
		testCategoriesToExecute.add("A");
		testCategoriesToExecute.add("NICHT-C");
		assertTrue( cut.isThisTestToExecute(testCategoriesOfTestCase, testCategoriesToExecute) );
		
		testCategoriesToExecute.clear();
		testCategoriesToExecute.add("X");
		testCategoriesToExecute.add("NICHT-Y");
		assertFalse( cut.isThisTestToExecute(testCategoriesOfTestCase, testCategoriesToExecute) );
		
		testCategoriesToExecute.clear();
		testCategoriesToExecute.add("D");
		testCategoriesToExecute.add("NICHT-B");
		assertFalse( cut.isThisTestToExecute(testCategoriesOfTestCase, testCategoriesToExecute) );
		
		testCategoriesToExecute.clear();
		testCategoriesToExecute.add("A");
		testCategoriesToExecute.add("NICHT-B");
		assertFalse( cut.isThisTestToExecute(testCategoriesOfTestCase, testCategoriesToExecute) );
		
		testCategoriesToExecute.clear();
		testCategoriesToExecute.add("X");
		assertFalse( cut.isThisTestToExecute(testCategoriesOfTestCase, testCategoriesToExecute) );	
		
		testCategoriesToExecute.clear();
		testCategoriesToExecute.add("NICHT-Y");
		assertTrue( cut.isThisTestToExecute(testCategoriesOfTestCase, testCategoriesToExecute) );	
	}
	
	@Test
	public void loadsMassDataAsPlainTextDatFiles() throws Exception 
	{
		LanguageTemplatesBasics cut = new LanguageTemplatesBasicsTestImpl( new VirtualTestCase("testId"));
		
		SysNatTestData result = cut.loadDataSets("Testdata");
		
		assertEquals( "Number of loaded datasets", 3, result.getAllObjectData().size() );	
	}
}
