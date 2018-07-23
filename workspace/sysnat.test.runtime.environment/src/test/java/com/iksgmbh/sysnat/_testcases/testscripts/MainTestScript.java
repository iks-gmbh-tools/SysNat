package com.iksgmbh.sysnat._testcases.testscripts;

import org.junit.Ignore;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat._testhelper.LanguageTemplatesCommonTestImpl;
import com.iksgmbh.sysnat.language_templates.LanguageTemplates;
import com.iksgmbh.sysnat.testcasejavatemplate.ScriptTemplateParent;


@Ignore
public class MainTestScript extends ScriptTemplateParent 
{
	private LanguageTemplatesCommonTestImpl languageTemplatesCommon;

	public MainTestScript(ExecutableExample callingTestCase) {
		super(callingTestCase);
	}
	
	@Override
	public void executeScript() {
		languageTemplatesCommon = new LanguageTemplatesCommonTestImpl(this);
		languageTemplatesCommon.executeScriptWithData("SubTestScript");
		addReportMessage("Mainscript executed."); 
	}
	
	public LanguageTemplates getApplicationSpecificLanguageTemplates() {
		return new LanguageTemplatesCommonTestImpl(this);
	}
}
