package com.iksgmbh.sysnat._testcases.testscripts;

import org.junit.Ignore;

import com.iksgmbh.sysnat.TestCase;
import com.iksgmbh.sysnat._testhelper._NatSpecScriptTemplate;


@Ignore
public class SimpleTestScript extends _NatSpecScriptTemplate 
{
	public SimpleTestScript(TestCase callingTestCase) {
		super(callingTestCase);
	}
	
	@Override
	public void executeTestCase() {
		addReportMessage("Simple script executed."); 
	}
}
