package com.iksgmbh.sysnat._testhelper;

import org.junit.Ignore;

import com.iksgmbh.sysnat.language_templates.LanguageTemplates;


@Ignore
public class TestCaseTestImpl extends _NatSpecTestCaseTemplate 
{	
	protected boolean throwExceptionOnFailing = false;
	
	@Override
	public void terminateTestCase(String message) {
		if (throwExceptionOnFailing) throw new RuntimeException(message);
	}	
	
	@Override
	public boolean doesTestBelongToApplicationUnderTest() {
		return true;
	}
	
	protected LanguageTemplates getApplicationSpecificLangaugeTemplates() {
		return new LanguageTemplatesBasicsTestImpl(this);
	}
}
