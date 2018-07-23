package com.iksgmbh.sysnat._testcases;

import org.junit.Ignore;

import com.iksgmbh.sysnat._testhelper.LanguageTemplatesCommonTestImpl;
import com.iksgmbh.sysnat._testhelper.TestCaseTestImpl;

@Ignore
public class TestCaseCallingUnknownScript extends TestCaseTestImpl 
{

	@Override
	public void executeTestCase() {
		languageTemplatesCommon = new LanguageTemplatesCommonTestImpl(this);
		languageTemplatesCommon.executeScript("UnkownScript");
	}
}
