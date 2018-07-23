package com.iksgmbh.sysnat.test.testTemplateContainers.testapplication;

import java.io.File;

import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;

@LanguageTemplateContainer
public class LanguageTemplateContainerTestImpl 
{
	@LanguageTemplate(value = "Natural language instruction without parameter.")
	@LanguageTemplate(value = "Natural language instruction without parameter. - Duplicate")
	@LanguageTemplate(value = "Natural language instruction without parameter. - Second Duplicate")
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

	@LanguageTemplate(value = "Create File <> with ^^.")
	public File anotherMethodWithReturnValue(String s1) {
		// do nothing
		return null;
	}

	@LanguageTemplate(value = "Create <>.")
	public Integer methodOnlyWithReturnValue() {
		// do nothing
		return null;
	}
	
	@LanguageTemplate(value = "Create with ^^.")
	public void methodWithReturnValueButNoReferenceInLanguageTemplate(String s) {
		// do nothing
	}
	
	@LanguageTemplate(value = "Active: ^^")
	public void methodWithStageInstruction(String s) {
		// do nothing
	}	
}
