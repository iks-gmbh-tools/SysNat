package com.iksgmbh.sysnat;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.testimpl.SysNatDialogTestImpl;

public class SysNatStartDialogClassLevelTest
{
	@Test
	public void findsEnvironmentOfSysNatDefaultApps() throws Exception
	{
		// arrange
		ExecutionRuntimeInfo.reset();
		System.setProperty(SysNatConstants.SYSNAT_DUMMY_TEST_RUN, "true");
		ExecutionRuntimeInfo executionRuntimeInfo = ExecutionRuntimeInfo.getInstance();
		
		// act
		new SysNatDialogTestImpl();
		
		// assert
		assertEquals("Number of test applications", 2, executionRuntimeInfo.getTestAppEnvironmentsMap().size());
		
		List<String> keys = new ArrayList<>(executionRuntimeInfo.getTestAppEnvironmentsMap().keySet());
		int expectedHomePageIKSEnvironments = 1;
		int expectedHelloWorldEnvironments = 1;
		
		if (keys.get(0).equals("HomePageIKS")) {
			assertEquals("number of environments", expectedHomePageIKSEnvironments, executionRuntimeInfo.getTestAppEnvironmentsMap().get(keys.get(0)).size());
			assertEquals("number of environments", expectedHelloWorldEnvironments, executionRuntimeInfo.getTestAppEnvironmentsMap().get(keys.get(1)).size());
		} else {
			assertEquals("number of environments", expectedHelloWorldEnvironments, executionRuntimeInfo.getTestAppEnvironmentsMap().get(keys.get(0)).size());
			assertEquals("number of environments", expectedHomePageIKSEnvironments, executionRuntimeInfo.getTestAppEnvironmentsMap().get(keys.get(1)).size());
		}
	}
	
}
