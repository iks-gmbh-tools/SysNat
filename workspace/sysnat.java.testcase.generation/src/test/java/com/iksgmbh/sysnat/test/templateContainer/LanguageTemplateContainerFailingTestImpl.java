package com.iksgmbh.sysnat.test.templateContainer;

import com.iksgmbh.sysnat.annotation.LanguageTemplate;

public class LanguageTemplateContainerFailingTestImpl 
{

	/*
	 * This is supposed to fail, because the return value is expected
	 * to be mentioned in the NaturalLanguagePattern by ''.
	 */
	@LanguageTemplate(value = "Create with ^^.")
	public Integer methodWithReturnValue(String s) {
		// do nothing
		return null;
	}

	/*
	 * This is supposed to fail, because the return value indicated 
	 * in the NaturalLanguagePatten is actually missing.
	 */
	@LanguageTemplate(value = "Create <> with ^^.")
	public void methodWithoutReturnValue(String s) {
		// do nothing
	}

	/*
	 * This is supposed to fail, because the return value is indicated twice
	 * in the NaturalLanguagePatten.
	 */
	@LanguageTemplate(value = "Create <> with <>.")
	public String methodAnnotatedWithTwoReturnValues() {
		// do nothing
		return null;
	}
	
	/*
	 * This is supposed to fail, because the parameter count in NaturalLanguagePatten
	 * and java method mismatch.
	 */
	@LanguageTemplate(value = "Do ^^ with ^^.")
	public void methodAnnotatedWithTwoParameters(String s) {
		// do nothing
	}

}
