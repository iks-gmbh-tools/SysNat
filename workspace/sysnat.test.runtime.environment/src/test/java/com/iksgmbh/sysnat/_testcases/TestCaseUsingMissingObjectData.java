package com.iksgmbh.sysnat._testcases;

import org.junit.Ignore;

import com.iksgmbh.sysnat._testhelper.LanguageTemplatesCommonTestImpl;
import com.iksgmbh.sysnat._testhelper.TestCaseTestImpl;

@Ignore
public class TestCaseUsingMissingObjectData extends TestCaseTestImpl 
{
	public TestCaseUsingMissingObjectData() {
		super();
		throwExceptionOnFailing = true;
	}

	@Override
	public void executeTestCase() {
		languageTemplatesCommon = new LanguageTemplatesCommonTestImpl(this);
		languageTemplatesCommon.executeScriptWithData("SimpleTestScript");
	}
}
