package com.iksgmbh.sysnat._testcases.testscripts;

import org.junit.Ignore;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat._testhelper.LanguageTemplatesCommonTestImpl;
import com.iksgmbh.sysnat.domain.SysNatTestData.SysNatDataset;
import com.iksgmbh.sysnat.testcasejavatemplate.ScriptTemplateParent;


@Ignore
public class SubTestScript extends ScriptTemplateParent 
{
	@SuppressWarnings("unused")
	private LanguageTemplatesCommonTestImpl languageTemplatesCommon;

	public SubTestScript(ExecutableExample callingTestCase) {
		super(callingTestCase);
	}

	@Override
	public void executeScript() {
		languageTemplatesCommon = new LanguageTemplatesCommonTestImpl(this);
		SysNatDataset objectData = getTestData().getDataSetForName("Testdata_A");
		addReportMessage("Subscript executed for " + objectData.getValue("Menu") 
		                  + " and " + objectData.getValue("Link") + "."); 
	}
}
