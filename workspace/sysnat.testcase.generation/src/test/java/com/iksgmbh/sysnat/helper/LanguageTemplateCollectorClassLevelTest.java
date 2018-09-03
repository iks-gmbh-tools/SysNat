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
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.GenerationRuntimeInfo;
import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.exception.SysNatException.ErrorCode;
import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.JavaFieldData;
import com.iksgmbh.sysnat.domain.LanguageTemplatePattern;

public class LanguageTemplateCollectorClassLevelTest 
{
	private LanguageTemplateCollector cut;

	@Before
	public void setup() {
		GenerationRuntimeInfo.reset();
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.languageTemplateContainer.source.dir", 
                "../sysnat.testcase.generation/src/test/java/com/iksgmbh/sysnat/test/testTemplateContainers");
		GenerationRuntimeInfo.getInstance();
		cut = new LanguageTemplateCollector(
                getLanguageTemplateContainerJavaFields("TestApplication"));
	}
	
	private List<JavaFieldData> getLanguageTemplateContainerJavaFields(final String testApp) 
	{
		return LanguageTemplateContainerFinder.findLanguageTemplateContainers(testApp);
	}

	@Test
	public void findsAllLanguageTemplates() throws ClassNotFoundException 
	{
		// act
		HashMap<Filename, List<LanguageTemplatePattern>> result = cut.findAllLanguageTemplates();
		
		// assert
		assertEquals("number of NaturalLanguageContainer classes", 1, result.size());
		
		final Filename key = result.keySet().iterator().next();
		assertEquals("number of NaturalLanguagePattern", 11, result.get(key).size());
	}

	@Test
	public void throwsExceptionForLanguageTemplateDuplicate() throws ClassNotFoundException 
	{
		// arrange
		cut = new LanguageTemplateCollector(
				getLanguageTemplateContainerJavaFields("TestApplication2"));		
		try {
			// act
			cut.findAllLanguageTemplates();
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", ErrorCode.LANGUAGE_TEMPLATE_PARSING__DUPLICATES, e.getErrorCode());
		}
	}	

}