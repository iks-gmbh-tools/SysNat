package com.iksgmbh.sysnat.testcasejavatemplate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;
import com.iksgmbh.sysnat.language_templates.common.LanguageTemplatesBasics_de;
import com.iksgmbh.sysnat.language_templates.iksonline.LanguageTemplatesIksOnlineBasics;

/**
 * JUnitTestCaseTemplate for TestApplication 'IksOnline'.
 */
public class IksOnlineTestCaseTemplate extends TestCaseTemplateParent
{
	@LanguageTemplateContainer
	protected LanguageTemplatesBasics_de languageTemplatesBasics;
	
	@LanguageTemplateContainer
	protected LanguageTemplatesIksOnlineBasics languageTemplatesIksOnlineBasics;
	
	@Before
	public void setUp() 
	{
		super.setUp();
		languageTemplatesBasics = new LanguageTemplatesBasics_de(this);
		languageTemplatesIksOnlineBasics = new LanguageTemplatesIksOnlineBasics(this);
	}

	@After
	public void shutdown() 
	{
		if ( ! isSkipped() ) {
			languageTemplatesIksOnlineBasics.gotoStartPage();
		}
		languageTemplatesIksOnlineBasics = null;
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
