package com.iksgmbh.sysnat.helper.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class CompositeTestApplicationUpdaterClassLevelTest
{
	private CompositeTestApplicationUpdater cut = new CompositeTestApplicationUpdater();

	@Before
	public void setup() {
		CompositeTestApplicationUpdater.languageTemplatesContainerParentDir = "../sysnat.natural.language.executable.examples\\src\\test\\resources\\UnitTestCompositeApp/";
	}
	
	@Test
	public void readsLanguageTemplateContainer()
	{
		// act
		cut.readInputCode();

		// assert
		assertEquals("Number of element apps", 2, cut.inputCodeMap.size());
		List<String> apps = new ArrayList<>(cut.inputCodeMap.keySet());
		
		LinkedHashMap<String, List<String>> methodMap1 = cut.inputCodeMap.get(apps.get(0));
		assertEquals("Number of language template methods in " + apps.get(0), 
				     9, methodMap1.size());
		List<String> methods = new ArrayList<>(methodMap1.keySet());
		assertEquals("Method Declaration ", "public void clickMainMenuItem(final String mainMenuItem)", methods.get(0).trim());
		assertEquals("Method Declaration ", 1, methodMap1.get(methods.get(0)).size());
		assertEquals("Method Declaration ", "public void clickDialogButton(String dialogName, String buttonName)", methods.get(2).trim());
		assertEquals("Method Declaration ", 1, methodMap1.get(methods.get(2)).size());
		assertEquals("Method Declaration ", "	@LanguageTemplate(value = \"In Dialog ^^ click button ^^.\")", methodMap1.get(methods.get(2)).get(0));
		
		LinkedHashMap<String, List<String>> methodMap2 = cut.inputCodeMap.get(apps.get(1));
		assertEquals("Number of language template methods in " + apps.get(1), 
				     10, methodMap2.size());
		assertEquals("Method Declaration ", "public void ensureCheckboxIsTicked(String checkBoxDisplayName) ", methods.get(5));
		assertEquals("Method Declaration ", 1, methodMap1.get(methods.get(5)).size());	
	}

	@Test
	public void analysesLanguageTemplateMethods()
	{
		// arrange
		cut.readInputCode();
		
		// act
		boolean result = cut.joinCode();
		
		// assert
		assertTrue("There are luanguage templated duplicates!", result);
		assertEquals("Number of joined methods", 10, cut.joinedCodeMap.size());
	}

	@Test
	public void determinesLanguageTemplateMethodsOrigin()
	{
		// arrange
		cut.readInputCode();
		
		// act
		cut.joinCode();
		
		// assert
		assertEquals("Number of joined methods", 10, cut.methodOrigin.size());
		List<String> keys = new ArrayList<String>(cut.methodOrigin.keySet());
		assertEquals("Number of origins of joined method", 2, cut.methodOrigin.get(keys.get(0)).size());
		//assertEquals("Number of origins of joined method", 1, cut.methodOrigin.get(keys.get(21)).size());  TODO
	}
	

	@Test
	public void createsOutputCode()
	{
		// arrange
		cut.readInputCode();
		cut.joinCode();
		
		// act
		List<String> result = cut.createOutputCode();
		// result.forEach(System.out::println);
		
		// assert
		assertEquals("Number of generated lines", 82, result.size());
	}	

	@Test
	public void revealesNoChangeNecessary()
	{
		// arrange
		cut.readInputCode();
		cut.joinCode();
		List<String> code = cut.createOutputCode();
		
		// act
		boolean result = cut.isTargetUpdateNecessary(code);
		
		// assert
		assertFalse("Target update must not be necessary!", result);
	}
	
}
