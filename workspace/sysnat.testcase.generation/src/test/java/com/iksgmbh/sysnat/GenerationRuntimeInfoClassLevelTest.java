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
		assertEquals("Application under test", 3, result.getLoginParameter().size() );
	}


}