package com.iksgmbh.sysnat._testcases;

import org.junit.Ignore;

import com.iksgmbh.sysnat._testhelper.LanguageTemplatesBasicsTestImpl;
import com.iksgmbh.sysnat._testhelper.TestCaseTestImpl;
import com.iksgmbh.sysnat.utils.SysNatConstants;

@Ignore
public class TestCaseWrong extends TestCaseTestImpl 
{
	public TestCaseWrong()  {
		languageTemplatesBasics = new LanguageTemplatesBasicsTestImpl(this);
	}
	
	@Override
	public void executeTestCase() 
	{
		languageTemplatesBasics.startNewTestCase("Orange Test");
		languageTemplatesBasics.checkFilterCategory(SysNatConstants.NO_FILTER);
		terminateWrongTestCase(); 
	}
}
