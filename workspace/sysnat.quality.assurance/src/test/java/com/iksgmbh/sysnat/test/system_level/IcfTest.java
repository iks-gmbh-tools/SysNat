
package com.iksgmbh.sysnat.test.system_level;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import com.iksgmbh.sysnat.SysNatTestCaseGenerator;
import com.iksgmbh.sysnat.SysNatTestingExecutor;

public class IcfTest 
{
	private Properties testSystemProperties = new Properties();
	
	//@Test
	public void runsSystemTestFor_ICF_Successfully() throws Exception
	{
		// arrange
//		testSystemProperties.setProperty("config.settings", "../sysnat.quality.assurance/src/test/resources/testSettingConfigs/helloWorldSpringBoot.config");
//		ExecutionRuntimeInfo.setSysNatSystemProperty("config.settings", "../sysnat.quality.assurance/src/test/resources/testSettingConfigs/helloWorldSpringBoot.config");
				
		// act
		SysNatTestCaseGenerator.doYourJob();
		final String result = SysNatTestingExecutor.startMavenCleanCompileTest(testSystemProperties);
		Thread.sleep(2000); // give maven time to execute tests

		// assert
		assertEquals("Maven result", SysNatTestingExecutor.MAVEN_OK, result);
	}
	
}
