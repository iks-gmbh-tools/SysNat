package com.iksgmbh.sysnat.helper;

import com.iksgmbh.sysnat.ExecutionInfo;
import com.iksgmbh.sysnat.TestCase;

public class VirtualTestCase extends TestCase 
{
	public VirtualTestCase(String id) {
		super.setTestID(id);
		executionInfo = ExecutionInfo.getInstance();
		setGuiController(executionInfo.getGuiController());
	}
	
	@Override
	public void executeTestCase() {
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
