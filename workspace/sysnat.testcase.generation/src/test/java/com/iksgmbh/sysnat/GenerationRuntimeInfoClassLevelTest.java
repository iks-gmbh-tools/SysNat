package com.iksgmbh.sysnat;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.domain.TestApplication;

public class GenerationRuntimeInfoClassLevelTest 
{
	@Before
	public void setup() {
		GenerationRuntimeInfo.reset();
	}
	
	@Test
	public void loadsApplicationProperties() 
	{
		// arrange
		GenerationRuntimeInfo.setSysNatSystemProperty("settings.config", "../sysnat.testcase.generation/src/test/resources/testSettingConfigs/HelloWorldSpringBoot.config");
    	
		// act
		TestApplication result = GenerationRuntimeInfo.getInstance().getTestApplication();
		
		// arrange
		assertEquals("Application under test", "HelloWorldSpringBoot", result.getName() );
		assertEquals("isWebApplication", "true", "" + result.isWebApplication() );
		assertEquals("Application under test", 3, result.getStartParameter().size() );
	}


}
