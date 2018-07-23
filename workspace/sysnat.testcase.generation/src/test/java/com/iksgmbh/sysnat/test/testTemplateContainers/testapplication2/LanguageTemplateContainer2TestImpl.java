package com.iksgmbh.sysnat.test.testTemplateContainers.testapplication2;

import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;

@LanguageTemplateContainer
public class LanguageTemplateContainer2TestImpl 
{
	/*
	 * This is supposed to fail when this class is used together with LanguageTemplateContainerTestImpl, 
	 * because the naturalLanguagePattern is not unique
	 */
	@LanguageTemplate(value = "Natural ^^ language ^^ instruction ^^ with ^^ parameters.")
	public void anotherMethodWithFourParameters(String s1, String s2, String s3, String s4) {
		// do nothing
	}

}
