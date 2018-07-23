package com.iksgmbh.sysnat.helper;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.ExecutableExample;

public class VirtualTestCase extends ExecutableExample 
{
	public VirtualTestCase(String id) {
		super.setXXID(id);
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
