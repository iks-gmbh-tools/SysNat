package com.iksgmbh.sysnat._testcases;

import org.junit.Ignore;

import com.iksgmbh.sysnat._testhelper.LanguageTemplatesBasicsTestImpl;
import com.iksgmbh.sysnat._testhelper.TestCaseTestImpl;

@Ignore
public class TestCaseCallingMainScriptCallingSubscript extends TestCaseTestImpl 
{
	@Override
	public void executeTestCase() 
	{
		languageTemplatesBasics = new LanguageTemplatesBasicsTestImpl(this);
		languageTemplatesBasics.startNewTestCase("testID");
		languageTemplatesBasics.createSysNatTestData("Testdata_A");
		languageTemplatesBasics.setSingleTestDataValue("Name", "Jack_Jackson");
		try {
			languageTemplatesBasics.executeScriptWithData("MainTestScript");
		} catch (Throwable e) {
			// ignore
		}
	}
}
