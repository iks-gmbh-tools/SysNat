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

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.LanguageTemplatePattern;

public class CommandLibraryCreatorClassLevelTest {

	@Test
	public void createsLibrary_FileContent_ForOneLanguageTemplateContainer() throws Exception 
	{
		// arrange
		final HashMap<Filename, List<LanguageTemplatePattern>> languageTemplateCollection = new HashMap<>();
		final List<LanguageTemplatePattern> patternList = new ArrayList<>();
		final Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication.LanguageTemplateContainerTestImpl");
		final Method method = c.getMethod("methodWithoutParameter");
		final LanguageTemplatePattern templatePattern = LanguageTemplatePattern.createFrom(method, new Filename("test"), "").get(0);
		patternList.add(templatePattern);
		languageTemplateCollection.put(new Filename("LanguageTemplateContainerTestImpl"), patternList);
		
		// act
		String result = CommandLibraryCreator.buildFileContent(languageTemplateCollection);
		
		// assert
		String expectedContent = "Instructions read from LanguageTemplateContainerTestImpl" 
		                          + System.getProperty("line.separator") 
				                  + CommandLibraryCreator.CONTAINER_NAME_UNDERLINE 
		                          + System.getProperty("line.separator") 
				                  + "Natural language instruction without parameter.";
		assertEquals("Content of command library file", expectedContent, result);
	}

	@Test
	public void createsLibrary_File_ForTwoLanguageTemplateContainers() throws Exception 
	{
		// arrange
		final HashMap<Filename, List<LanguageTemplatePattern>> languageTemplateCollection = new HashMap<>();
		List<LanguageTemplatePattern> patternList = new ArrayList<>();
		Class<?> c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication.LanguageTemplateContainerTestImpl");
		Method method = c.getMethod("methodWithoutParameter");
		LanguageTemplatePattern templatePattern = LanguageTemplatePattern.createFrom(method, new Filename("test"), "").get(0);
		patternList.add(templatePattern);
		languageTemplateCollection.put(new Filename("LanguageTemplateContainerTestImpl"), patternList);
		patternList = new ArrayList<>();
		c = Class.forName("com.iksgmbh.sysnat.test.testTemplateContainers.testapplication2.LanguageTemplateContainer2TestImpl");
		method = c.getMethod("anotherMethodWithFourParameters", String.class, String.class, String.class, String.class);
		templatePattern = new LanguageTemplatePattern(method, new Filename("test"), "");
		patternList.add(templatePattern);
		languageTemplateCollection.put(new Filename("LanguageTemplateContainer2TestImpl"), patternList);
		
		// act
		String result = CommandLibraryCreator.buildFileContent(languageTemplateCollection);
		
		// assert
		String expectedContent = "Instructions read from LanguageTemplateContainer2TestImpl" 
				                + System.getProperty("line.separator") 
								+ "-------------------------------------------------------" 
				                + System.getProperty("line.separator") 
								+ "Natural ^^ language ^^ instruction ^^ with ^^ parameters."
				                + System.getProperty("line.separator") 
								+ "" 
				                + System.getProperty("line.separator") 
								+ "Instructions read from LanguageTemplateContainerTestImpl"
				                + System.getProperty("line.separator") 
								+ "-------------------------------------------------------"
				                + System.getProperty("line.separator") 
								+ "Natural language instruction without parameter.";
		assertEquals("Content of command library file", expectedContent, result);
	}
	
	
}