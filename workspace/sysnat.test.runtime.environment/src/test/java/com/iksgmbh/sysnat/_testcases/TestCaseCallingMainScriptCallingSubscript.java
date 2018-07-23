package com.iksgmbh.sysnat._testcases;

import org.junit.Ignore;

import com.iksgmbh.sysnat._testhelper.LanguageTemplatesCommonTestImpl;
import com.iksgmbh.sysnat._testhelper.TestCaseTestImpl;
import com.iksgmbh.sysnat.testdataimport.TestDataImporter;

@Ignore
public class TestCaseCallingMainScriptCallingSubscript extends TestCaseTestImpl 
{
	public TestCaseCallingMainScriptCallingSubscript() {		
		testDataImporter = new TestDataImporter(executionInfo.getTestdataDir());
	}

	@Override
	public void executeTestCase() 
	{
		languageTemplatesCommon = new LanguageTemplatesCommonTestImpl(this);
		languageTemplatesCommon.startNewTestCase("testID");
		languageTemplatesCommon.loadTestDatasets("Testdata_A");
		languageTemplatesCommon.setSingleTestDataValue("Menu", "aMenuName");
		try {
			languageTemplatesCommon.executeScriptWithData("MainTestScript");
		} catch (Throwable e) {
			// ignore
		}
	}
}
