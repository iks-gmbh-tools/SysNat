package com.iksgmbh.sysnat.testcasejavatemplate;

import com.iksgmbh.sysnat.TestCase;
import com.iksgmbh.sysnat.utils.SysNatUtil;

/**
 * This class is the parent of all script templates (.nls files). 
 * For technical reasons it is a subclass of TestCase,
 * although it is not executable on its own.
 */
public abstract class ScriptTemplateParent extends TestCase
{
	private String testCaseFileName;
	
	public ScriptTemplateParent(TestCase callingTestCase) {
		adoptContextDataFrom(callingTestCase);
	}
	
	public abstract void executeScript();
	
	protected void adoptContextDataFrom(TestCase callingTestCase) 
	{
		testCaseFileName = callingTestCase.getTestCaseFileName();
		SysNatUtil.copyContextData(callingTestCase, this);
	}

	@Override
	public String getTestCaseFileName() {
		return testCaseFileName;
	}
	
	@Override
	public Package getTestCasePackage() {
		return this.getClass().getPackage();
	}

	@Override
	public boolean doesTestBelongToApplicationUnderTest() {
		return true;
	}


	@Override
	public void executeTestCase() {
		// do nothing here for scripts
	}

}
