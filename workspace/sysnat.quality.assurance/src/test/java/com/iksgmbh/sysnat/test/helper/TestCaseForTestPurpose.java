package com.iksgmbh.sysnat.test.helper;

import org.junit.Ignore;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.language_templates.common.LanguageTemplatesCommon;
import com.iksgmbh.sysnat.testdataimport.TestDataImporter;

@Ignore
public class TestCaseForTestPurpose extends ExecutableExample
{
	protected LanguageTemplatesCommon languageTemplatesCommon;
	
	public TestCaseForTestPurpose()  {
		languageTemplatesCommon = new LanguageTemplatesCommon(this);
		ExecutionRuntimeInfo executionInfo = ExecutionRuntimeInfo.getInstance();
		executionInfo.setApplicationStarted(true);
		testDataImporter = new TestDataImporter(executionInfo.getTestdataDir());
	}

	@Override
	public void executeTestCase() {
		languageTemplatesCommon.startNewTestCase("TestCaseForTestPurpose");
		languageTemplatesCommon.loadTestDatasets("ManyDatasets");
		addReportMessage(getTestData().size() + " datasets have been loaded.");
		closeCurrentTestCaseWithSuccess();
	}

	@Override
	public String getTestCaseFileName() {
		return null;
	}

	@Override
	public Package getTestCasePackage() {
		return null;
	}

	@Override
	public boolean doesTestBelongToApplicationUnderTest() {
		return true;
	}
}
