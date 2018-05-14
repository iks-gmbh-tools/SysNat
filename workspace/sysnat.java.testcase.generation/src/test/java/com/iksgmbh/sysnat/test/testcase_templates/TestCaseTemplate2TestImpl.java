package com.iksgmbh.sysnat.test.testcase_templates;

import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;
import com.iksgmbh.sysnat.test.templateContainer.LanguageTemplateContainer2TestImpl;
import com.iksgmbh.sysnat.test.templateContainer.LanguageTemplateContainerTestImpl;

/**
 * JUnitTestCaseTemplate for test purpose.
 */
public class TestCaseTemplate2TestImpl 
{
	@LanguageTemplateContainer
	protected LanguageTemplateContainerTestImpl languageTemplateContainer;

	@LanguageTemplateContainer
	protected LanguageTemplateContainer2TestImpl languageTemplateContainer2;
	
}
