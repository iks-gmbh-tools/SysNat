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
package com.iksgmbh.sysnat.helper;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.GenerationRuntimeInfo;
import com.iksgmbh.sysnat.domain.JavaFieldData;

public class LanguageTemplateContainerFinderClassLevelTest 
{
	@Before
	public void setup() 
	{
		GenerationRuntimeInfo.reset();
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.languageTemplateContainer.source.dir", 
				"../sysnat.testcase.generation/src/test/java/com/iksgmbh/sysnat/test/testTemplateContainers");
		ExecutionRuntimeInfo.setSysNatSystemProperty("settings.config", "../sysnat.test.runtime.environment/src/test/resources/testSettingConfigs/settingsHomePageIKS.config");
		GenerationRuntimeInfo.getInstance();
	}
	
	@Test
	public void findsFieldDataOfLanguageTemplateContainers() 
	{
		// act
		final List<JavaFieldData> result = LanguageTemplateContainerFinder.findLanguageTemplateContainers("TestApplication2");
		
		// assert
		assertEquals("Number of fields", 2, result.size());
		
		assertEquals("Field Name", "languageTemplateContainer2TestImpl", result.get(0).name);
		assertEquals("Field Name", "languageTemplateContainerTestImpl", result.get(1).name);
		assertEquals("Field Name", "com.iksgmbh.sysnat.test.testTemplateContainers.testapplication2.LanguageTemplateContainer2TestImpl", result.get(0).type.getName());
		assertEquals("Field Name", "com.iksgmbh.sysnat.test.testTemplateContainers.testapplication2.LanguageTemplateContainerTestImpl", result.get(1).type.getName());
	}

}