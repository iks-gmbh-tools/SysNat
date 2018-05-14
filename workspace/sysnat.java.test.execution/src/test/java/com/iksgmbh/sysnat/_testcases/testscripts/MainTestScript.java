package com.iksgmbh.sysnat._testcases.testscripts;

import org.junit.Ignore;

import com.iksgmbh.sysnat.TestCase;
import com.iksgmbh.sysnat._testhelper.LanguageTemplatesBasicsTestImpl;
import com.iksgmbh.sysnat._testhelper._NatSpecScriptTemplate;
import com.iksgmbh.sysnat.language_templates.LanguageTemplates;


@Ignore
public class MainTestScript extends _NatSpecScriptTemplate 
{
	public MainTestScript(TestCase callingTestCase) {
		super(callingTestCase);
	}
	
	@Override
	public void executeTestCase() {
		languageTemplatesBasics = new LanguageTemplatesBasicsTestImpl(this);
		languageTemplatesBasics.executeScriptWithData("SubTestScript");
		addReportMessage("Mainscript executed."); 
	}
	
	protected LanguageTemplates getApplicationSpecificLangaugeTemplates() {
		return new LanguageTemplatesBasicsTestImpl(this);
	}
}
