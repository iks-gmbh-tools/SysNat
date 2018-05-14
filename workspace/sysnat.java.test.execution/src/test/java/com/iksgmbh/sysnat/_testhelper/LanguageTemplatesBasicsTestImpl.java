package com.iksgmbh.sysnat._testhelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.Before;

import com.iksgmbh.sysnat.LanguageTemplatesBasics;
import com.iksgmbh.sysnat.TestCase;
import com.iksgmbh.sysnat.language_templates.LanguageTemplates;

public class LanguageTemplatesBasicsTestImpl extends LanguageTemplatesBasics implements LanguageTemplates
{
	public LanguageTemplatesBasicsTestImpl(TestCase test) {
		super(test, ResourceBundle.getBundle("LanguageTemplatesBasics", Locale.getDefault()));
	}

	protected String getScriptDir() {
		return "com.iksgmbh.sysnat._testcases.testscripts";
	}
	
	@Before
	public boolean doesTestBelongToApplicationUnderTest() {
		return true;
	}

	@Override
	public void doLogin(String... loginData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doLogout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isOverviewPageVisible() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLoginPageVisible() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void gotoStartPage() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> getScriptDirectories() {
		List<String> toReturn = new ArrayList<>();
		toReturn.add("com.iksgmbh.sysnat._testcases.testscripts");
		return toReturn;
	}
	
}
