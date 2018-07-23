package com.iksgmbh.sysnat._testcases;

import static com.iksgmbh.sysnat.common.utils.SysNatConstants.NO_FILTER;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.ERROR_KEYWORD;

import org.junit.Ignore;

import com.iksgmbh.sysnat._testhelper.LanguageTemplatesCommonTestImpl;
import com.iksgmbh.sysnat._testhelper.TestCaseTestImpl;

@Ignore
public class TestCaseFailed extends TestCaseTestImpl 
{
	public TestCaseFailed()  {
		languageTemplatesCommon = new LanguageTemplatesCommonTestImpl(this);
	}
	@Override
	public void executeTestCase() 
	{
		languageTemplatesCommon.startNewTestCase("Red Test");
		languageTemplatesCommon.checkFilterCategory(NO_FILTER);
		failWithMessage(ERROR_KEYWORD + ": Test-Error"); 
	}
}
