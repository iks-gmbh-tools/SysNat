package com.iksgmbh.sysnat._testcases;

import org.junit.Ignore;

import com.iksgmbh.sysnat._testhelper.LanguageTemplatesBasicsTestImpl;
import com.iksgmbh.sysnat._testhelper.TestCaseTestImpl;
import com.iksgmbh.sysnat.utils.SysNatConstants;

@Ignore
public class TestCaseInactive extends TestCaseTestImpl 
{
	public TestCaseInactive()  {
		languageTemplatesBasics = new LanguageTemplatesBasicsTestImpl(this);
	}
	
	@Override
	public void executeTestCase() 
	{
		try {
			languageTemplatesBasics.startNewTestCase("Inactive Test");
			languageTemplatesBasics.checkFilterCategory(SysNatConstants.NO_FILTER);
			languageTemplatesBasics.setActiveState("nein");	
		} catch (Exception e) {
			// ignore
		}
	}
}
