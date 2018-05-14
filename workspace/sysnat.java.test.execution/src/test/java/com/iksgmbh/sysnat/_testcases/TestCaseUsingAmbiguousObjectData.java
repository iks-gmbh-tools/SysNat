package com.iksgmbh.sysnat._testcases;

import org.junit.Ignore;

import com.iksgmbh.sysnat._testhelper.LanguageTemplatesBasicsTestImpl;
import com.iksgmbh.sysnat._testhelper.TestCaseTestImpl;

@Ignore
public class TestCaseUsingAmbiguousObjectData extends TestCaseTestImpl 
{

	@Override
	public void executeTestCase() {
		languageTemplatesBasics = new LanguageTemplatesBasicsTestImpl(this);
		languageTemplatesBasics.getDatasetObject("Testdata_A");
		languageTemplatesBasics.getDatasetObject("Testdata_B");
		languageTemplatesBasics.setSingleTestDataValue("Name", "Jack_Jackson");
	}
}
