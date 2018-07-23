package com.iksgmbh.sysnat.helper;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

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
