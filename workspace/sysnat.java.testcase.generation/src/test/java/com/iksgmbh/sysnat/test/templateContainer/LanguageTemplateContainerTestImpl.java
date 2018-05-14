package com.iksgmbh.sysnat.test.templateContainer;

import com.iksgmbh.sysnat.annotation.LanguageTemplate;

public class LanguageTemplateContainerTestImpl 
{
	@LanguageTemplate(value = "Natural language instruction without parameter.")
	public void methodWithoutParameter() {
		// do nothing
	}
	
	@LanguageTemplate(value = " Natural language instruction with ^^ parameter. ")
	public void methodWithOneParameter(String s) {
		// do nothing
	}

	@LanguageTemplate(value = "Natural language instruction with ^^ parameter.")
	public void anotherMethodWithOneParameter(int i) {
		// do nothing
	}

	@LanguageTemplate(value = "Natural ^^ language ^^ instruction ^^ with ^^ parameters.")
	public void methodWithFourParameters(String s1, String s2, String s3, String s4) {
		// do nothing
	}

	@LanguageTemplate(value = "^^ = ^^")
	public void methodWithTwoParameters(String s1, String s2) {
		// do nothing
	}

	@LanguageTemplate(value = "Create <> with ^^.")
	public Integer methodWithReturnValue(String s1) {
		// do nothing
		return null;
	}

	@LanguageTemplate(value = "Create <>.")
	public Integer methodOnlyWithReturnValue() {
		// do nothing
		return null;
	}
}
