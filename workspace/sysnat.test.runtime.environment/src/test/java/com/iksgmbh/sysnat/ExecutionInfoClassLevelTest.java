/*
 * Copyright 2018 IKS Gesellschaft fuer Informations- und Kommunikationssysteme mbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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