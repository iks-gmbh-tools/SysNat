package com.iksgmbh.sysnat._testhelper;

import java.util.HashMap;

import org.junit.Before;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.StartParameter;
import com.iksgmbh.sysnat.language_templates.LanguageTemplates;
import com.iksgmbh.sysnat.language_templates.common.LanguageTemplatesCommon;

public class LanguageTemplatesCommonTestImpl extends LanguageTemplatesCommon implements LanguageTemplates
{
	public LanguageTemplatesCommonTestImpl(ExecutableExample test) {
		super(test);
	}

	protected String getScriptDir() {
		return "com.iksgmbh.sysnat._testcases.testscripts";
	}
	
	@Before
	public boolean doesTestBelongToApplicationUnderTest() {
		return true;
	}

	@Override
	public void doLogout() {
	}

	@Override
	public boolean isStartPageVisible() {
		return false;
	}

	@Override
	public boolean isLoginPageVisible() {
		return false;
	}

	@Override
	public void gotoStartPage() {
	}

	@Override
	public void doLogin(HashMap<StartParameter, String> startParameter) {
	}
	
}
