package com.iksgmbh.sysnat;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.common.utils.SysNatConstants.BrowserType;

public class ExecutionInfoClassLevelTest 
{
	@Before
	public void setUp() {
		ExecutionRuntimeInfo.reset();
	}
	
	@Test
	public void readsConfigSettings() 
	{
		// arrange
		ExecutionRuntimeInfo.setSysNatSystemProperty("settings.config", "../sysnat.test.runtime.environment/src/test/resources/testSettingConfigs/settingsHomePageIKS.config");
		
		// act
		ExecutionRuntimeInfo instance = ExecutionRuntimeInfo.getInstance();
		
		// assert
		assertEquals("Browser", BrowserType.FIREFOX, instance.getBrowserTypeToUse());
		assertEquals("Test Application Name", "HomePageIKS", instance.getTestApplicationName());
		assertEquals("Target Environment", "PRODUCTION", instance.getTargetEnv().name());
		assertEquals("Execution speed", "schnell", instance.getExecutionSpeed());
	}


}
