package com.iksgmbh.sysnat._testcases.testscripts;

import org.junit.Ignore;

import com.iksgmbh.sysnat.TestCase;
import com.iksgmbh.sysnat.testcasejavatemplate.ScriptTemplateParent;


@Ignore
public class SimpleTestScript extends ScriptTemplateParent 
{
	public SimpleTestScript(TestCase callingTestCase) {
		super(callingTestCase);
	}
	
	@Override
	public void executeScript() {
		addReportMessage("Simple script executed."); 
	}
}
