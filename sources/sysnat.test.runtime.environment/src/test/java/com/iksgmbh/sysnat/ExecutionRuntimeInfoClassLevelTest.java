package com.iksgmbh.sysnat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.BrowserType;

public class ExecutionRuntimeInfoClassLevelTest
{

	@Before
	public void setUp() {
		ExecutionRuntimeInfo.reset();
	}

	@Test
	public void avoidsReportNameWithWrongTestApplicationName() 
	{
		// arrange
		ExecutionRuntimeInfo instance = ExecutionRuntimeInfo.getInstance();
		instance.setTestReportName("HomePageIKS-Production-All");
		instance.setTestApplicationName("HelloWorldSpringBoot");
		instance.setTestEnvironmentName("LOCAL");
		
		// act
		String result = instance.getReportFolderAsString();
		
		// assert
		assertTrue("Unexpected name of test report", result.contains("HelloWorldSpringBoot-LOCAL"));
		assertFalse("Unexpected name of test report", result.contains("HomePageIKS-Production"));
	}
	
	
	@Test
	public void readsConfigSettings() 
	{
		// arrange
		System.setProperty(SysNatConstants.TESTING_CONFIG_PROPERTY, "../sysnat.test.runtime.environment/src/test/resources/testSettingConfigs/settingsHomePageIKS.config");
		
		// act
		ExecutionRuntimeInfo instance = ExecutionRuntimeInfo.getInstance();
		
		// assert
		assertEquals("Browser", BrowserType.FIREFOX, instance.getTestBrowserType());
		assertEquals("Test Application Name", "HomePageIKS", instance.getTestApplicationName());
		assertEquals("Target Environment", "PRODUCTION", instance.getTestEnvironment().name());
		assertEquals("Execution speed", "SCHNELL", instance.getTestExecutionSpeed().name());
	}
	
	@Test
	public void ignoresWrongEnvironmentDefinitions() throws Exception
	{
		// arrange
		ExecutionRuntimeInfo executionRuntimeInfo = ExecutionRuntimeInfo.getInstance();
		final String oldPropertyValue = System.getProperty("sysnat.properties.path");
		
		// act
		System.setProperty("sysnat.properties.path", "../sysnat.natural.language.executable.examples/src/test/resources");
		executionRuntimeInfo.readConfiguredTestAppsAndTheirEnvironments();
		
		// cleanup
		System.setProperty("sysnat.properties.path", oldPropertyValue);
		
		// assert
		List<String> environments = executionRuntimeInfo.getTestAppEnvironmentsMap().get("UnitTestFakeApplication");
		assertEquals("Number of test applications", 2, environments.size());
	}


}
