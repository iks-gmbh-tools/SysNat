package com.iksgmbh.sysnat.helper;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.TestCase;

public class VirtualTestCase extends TestCase 
{
	public VirtualTestCase(String id) {
		super.setTestID(id);
		executionInfo = ExecutionRuntimeInfo.getInstance();
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
