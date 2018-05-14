package com.iksgmbh.sysnat.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.LanguageTemplatePattern;
import com.iksgmbh.sysnat.exception.SysNatException;
import com.iksgmbh.sysnat.exception.SysNatException.ErrorCode;

public class LanguageTemplateCollectorTest 
{
	private static Class<?> INPUT_CLASS = getInputClass();
	private LanguageTemplateCollector cut = new LanguageTemplateCollector(INPUT_CLASS);

	@Test
	public void findsAllLanguageTemplates() throws ClassNotFoundException 
	{
		// act
		HashMap<Filename, List<LanguageTemplatePattern>> result = cut .findAllLanguageTemplates();
		
		// arrange
		assertEquals("number of NaturalLanguageContainer classes", 1, result.size());
		
		final Filename key = result.keySet().iterator().next();
		assertEquals("number of NaturalLanguagePattern", 7, result.get(key).size());
	}
	

	@Test
	public void throwsExceptionForLanguageTemplateDuplicate() throws ClassNotFoundException 
	{
		// arrange
		cut = new LanguageTemplateCollector(Class.forName("com.iksgmbh.sysnat.test.testcase_templates.TestCaseTemplate2TestImpl"));
		
		try {
			// act
			cut.findAllLanguageTemplates();
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", ErrorCode.LANGUAGE_TEMPLATE_PARSING__DUPLICATES, e.getErrorCode());
		}
	}	

	private static Class<?> getInputClass() 
	{
		try {
			return Class.forName("com.iksgmbh.sysnat.test.testcase_templates.TestCaseTemplateTestImpl");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
