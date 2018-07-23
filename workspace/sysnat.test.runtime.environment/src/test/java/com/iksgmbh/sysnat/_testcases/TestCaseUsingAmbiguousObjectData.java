package com.iksgmbh.sysnat._testcases;

import org.junit.Ignore;

import com.iksgmbh.sysnat._testhelper.LanguageTemplatesCommonTestImpl;
import com.iksgmbh.sysnat._testhelper.TestCaseTestImpl;
import com.iksgmbh.sysnat.testdataimport.TestDataImporter;

@Ignore
public class TestCaseUsingAmbiguousObjectData extends TestCaseTestImpl 
{
	public TestCaseUsingAmbiguousObjectData() {		
		testDataImporter = new TestDataImporter(executionInfo.getTestdataDir());
	}

	@Override
	public void executeTestCase() {
		languageTemplatesCommon = new LanguageTemplatesCommonTestImpl(this);
		languageTemplatesCommon.setDatasetObject("Testdata_A");
		languageTemplatesCommon.setDatasetObject("Testdata_B");
		languageTemplatesCommon.setSingleTestDataValue("Name", "Jack_Jackson");
	}
}
