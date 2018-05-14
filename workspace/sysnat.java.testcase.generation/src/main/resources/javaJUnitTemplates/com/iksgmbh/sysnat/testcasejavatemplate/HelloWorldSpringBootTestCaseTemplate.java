package com.iksgmbh.sysnat.testcasejavatemplate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;
import com.iksgmbh.sysnat.language_templates.common.LanguageTemplatesBasics_en;
import com.iksgmbh.sysnat.language_templates.helloworldspringboot.LanguageTemplatesHelloWorldSpringBootBasics;

/**
 * JUnitTestCaseTemplate for TestApplication 'HelloWorldSpringBoot'.
 */
public class HelloWorldSpringBootTestCaseTemplate extends TestCaseTemplateParent
{
	@LanguageTemplateContainer
	protected LanguageTemplatesBasics_en languageTemplatesBasics;
	
	@LanguageTemplateContainer
	protected LanguageTemplatesHelloWorldSpringBootBasics languageTemplatesHelloWorldSpringBootBasics;

	@Before
	public void setUp() 
	{
		super.setUp();
		languageTemplatesBasics = new LanguageTemplatesBasics_en(this);
		languageTemplatesHelloWorldSpringBootBasics = new LanguageTemplatesHelloWorldSpringBootBasics(this);
	}

	@After
	public void shutdown() 
	{
		if ( ! isSkipped() ) {
			languageTemplatesHelloWorldSpringBootBasics.gotoStartPage();
		}
		languageTemplatesHelloWorldSpringBootBasics = null;
		languageTemplatesBasics = null;
		super.shutdown();
	}
	

	/**
	 * The comment line of this method will be replaced 
	 * when merging this template with the code generated
	 * from the natural language test case files.
	 */
	@Test
	@Override
	public void executeTestCase() 
	{
		try {
			/* TO BE REPLACED */			
		} catch (Throwable e) {
			// TODO: handle exception
		}
	}
}
