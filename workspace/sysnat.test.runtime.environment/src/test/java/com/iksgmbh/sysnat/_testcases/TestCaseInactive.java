package com.iksgmbh.sysnat._testcases;

import org.junit.Ignore;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat._testhelper.LanguageTemplatesCommonTestImpl;
import com.iksgmbh.sysnat._testhelper.TestCaseTestImpl;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;

@Ignore
public class TestCaseInactive extends TestCaseTestImpl 
{
	public TestCaseInactive()  {
		languageTemplatesCommon = new LanguageTemplatesCommonTestImpl(this);
		ExecutionRuntimeInfo.getInstance().setApplicationStarted(true);
	}
	
	@Override
	public void executeTestCase() 
	{
		try {
			languageTemplatesCommon.startNewTestCase("Inactive Test");
			languageTemplatesCommon.checkFilterCategory(SysNatConstants.NO_FILTER);
			languageTemplatesCommon.setActiveState("nein");	
		} catch (Exception e) {
			// ignore
		}
	}
}
