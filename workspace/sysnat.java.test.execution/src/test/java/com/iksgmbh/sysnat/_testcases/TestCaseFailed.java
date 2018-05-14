package com.iksgmbh.sysnat._testcases;

import static com.iksgmbh.sysnat.utils.SysNatConstants.NO_FILTER;
import static com.iksgmbh.sysnat.utils.SysNatLocaleConstants.ERROR_KEYWORD;

import org.junit.Ignore;

import com.iksgmbh.sysnat._testhelper.LanguageTemplatesBasicsTestImpl;
import com.iksgmbh.sysnat._testhelper.TestCaseTestImpl;

@Ignore
public class TestCaseFailed extends TestCaseTestImpl 
{
	public TestCaseFailed()  {
		languageTemplatesBasics = new LanguageTemplatesBasicsTestImpl(this);
	}
	@Override
	public void executeTestCase() 
	{
		languageTemplatesBasics.startNewTestCase("Red Test");
		languageTemplatesBasics.checkFilterCategory(NO_FILTER);
		failWithMessage(ERROR_KEYWORD + ": Test-Error"); 
	}
}
