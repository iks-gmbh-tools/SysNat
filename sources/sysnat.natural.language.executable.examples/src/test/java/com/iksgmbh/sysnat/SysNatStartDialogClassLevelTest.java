package com.iksgmbh.sysnat;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.common.utils.SysNatConstants.TargetEnv;
import com.iksgmbh.sysnat.testimpl.SysNatStartDialogTestImpl;

public class SysNatStartDialogClassLevelTest
{
	private SysNatStartDialog cut;
	
	@Before
	public void init() throws Exception
	{
		cut = new SysNatStartDialogTestImpl();
	}

	@Test
	public void doesNotAddLineThatDoesNotConfigureAnEnvironment()
	{
		// arrange
		ExecutionRuntimeInfo.getInstance();
		final String testApp = "UnitTestFakeApplication";
		final String oldPropertyValue = System.getProperty("sysnat.properties.path");
		System.setProperty("sysnat.properties.path", "../sysnat.natural.language.executable.examples/src/test/resources");
		final HashMap<String, List<TargetEnv>> configuredEnvironments = new HashMap<>();
		
		// act
		cut.addConfiguredEnvironments(testApp, configuredEnvironments);
		
		// cleanup
		System.setProperty("sysnat.properties.path", oldPropertyValue);
		
		// assert
		assertEquals("result", 2, configuredEnvironments.get(testApp).size());
	}

	@Test
	public void ignoresDoubleConfigLines()
	{
		// arrange
		ExecutionRuntimeInfo.getInstance();
		final String testApp = "HelloWorldSpringBoot";
		final HashMap<String, List<TargetEnv>> configuredEnvironments = new HashMap<>();
		
		// act
		cut.addConfiguredEnvironments(testApp, configuredEnvironments);
		
		// assert
		assertEquals("result", 1, configuredEnvironments.get(testApp).size());
	}
	
}
