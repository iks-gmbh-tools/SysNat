package com.iksgmbh.sysnat._testcases;

import org.junit.Ignore;

import com.iksgmbh.sysnat._testhelper.LanguageTemplatesBasicsTestImpl;
import com.iksgmbh.sysnat._testhelper.TestCaseTestImpl;

@Ignore
public class TestCaseCallingSimpleScript extends TestCaseTestImpl 
{
	@Override
	public void executeTestCase() 
	{
		languageTemplatesBasics = new LanguageTemplatesBasicsTestImpl(this);
		languageTemplatesBasics.executeScript("SimpleTestScript");
	}
}
