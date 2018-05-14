package com.iksgmbh.sysnat._testcases.testscripts;

import org.junit.Ignore;

import com.iksgmbh.sysnat.TestCase;
import com.iksgmbh.sysnat._testhelper.LanguageTemplatesBasicsTestImpl;
import com.iksgmbh.sysnat._testhelper._NatSpecScriptTemplate;
import com.iksgmbh.sysnat.domain.SysNatTestData.ObjectData;


@Ignore
public class SubTestScript extends _NatSpecScriptTemplate 
{
	
	public SubTestScript(TestCase callingTestCase) {
		super(callingTestCase);
	}

	@Override
	public void executeTestCase() {
		languageTemplatesBasics = new LanguageTemplatesBasicsTestImpl(this);
		ObjectData objectData = languageTemplatesBasics.getDatasetObject("Testdata");
		addReportMessage("Subscript executed for " + objectData.getValueWithReplacedUnderscores("Name") 
		                  + " (" + objectData.getValue("Age") + ")."); 
	}
}
