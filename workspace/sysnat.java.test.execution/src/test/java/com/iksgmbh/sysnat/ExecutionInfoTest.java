package com.iksgmbh.sysnat;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.utils.SysNatConstants.BrowserType;

public class ExecutionInfoTest 
{
	@Before
	public void setUp() {
		ExecutionInfo.reset();
	}
	
	@Test
	public void readProperties() 
	{
		// act
		ExecutionInfo instance = ExecutionInfo.getInstance();
		
		// assert
		assertEquals("Default Browser", BrowserType.FIREFOX, instance.getBrowserTypeToUse());
		assertEquals("Target Login URL", "http://localhost:8097/hello/world/login", instance.getTargetLoginUrl());
		assertEquals("Geschwindigkeit der Testausführung", "fast", "" + instance.getExecutionSpeed());
	}


}
