package com.iksgmbh.sysnat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.iksgmbh.sysnat.domain.TestApplication;

public class SysNatTestExecutorTest 
{
	private SysNatTestCaseGenerator cut = new SysNatTestCaseGenerator();
	
	@Test
	public void findsApplicationUnderTest() 
	{
		// act
		TestApplication result = cut.findApplicationUnderTest();
		
		// arrange
		assertEquals("Application under test", "HelloWorldSpringBoot", result.getName() );
		assertEquals("isWebApplication", "true", "" + result.isWebApplication() );
		assertEquals("Application under test", 3, result.getStartParameter().size() );
	}
	
	@Test
	public void findsJavaJUnitTemplates() 
	{
		// act
		Class<?> result = cut.findJavaJUnitTemplateFor("HelloWorldSpringBoot");
		
		// arrange
		assertNotNull(result);
	}
		
}
