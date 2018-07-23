package com.iksgmbh.sysnat._testhelper;

import org.junit.Ignore;

import com.iksgmbh.sysnat.language_templates.LanguageTemplates;
import com.iksgmbh.sysnat.testcasejavatemplate.TestCaseTemplateParent;


@Ignore
public class TestCaseTestImpl extends TestCaseTemplateParent 
{	
	protected LanguageTemplatesCommonTestImpl languageTemplatesCommon;
	protected boolean throwExceptionOnFailing = false;
	
	@Override
	public void terminateTestCase(String message) {
		if (throwExceptionOnFailing) throw new RuntimeException(message);
	}	
	
	@Override
	public boolean doesTestBelongToApplicationUnderTest() {
		return true;
	}
	
	public LanguageTemplates getApplicationSpecificLanguageTemplates() {
		return new LanguageTemplatesCommonTestImpl(this);
	}

	@Override
	public void executeTestCase() {
		// TODO Auto-generated method stub
		
	}
}
