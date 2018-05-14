package com.iksgmbh.sysnat._testhelper;

import com.iksgmbh.sysnat.TestCase;
import com.iksgmbh.sysnat.language_templates.helloworldspringboot.LanguageTemplatesHelloWorldSpringBootBasics;
import com.iksgmbh.sysnat.utils.SysNatUtil;

/**
 * This class serves as a template for all classes that are generated for
 * NatSpec scenarios (.natspec files). It is recognized by NatSpec based on its
 * special name and applies to all scenarios in the same package and all sub
 * packages (unless there is another template class in one of the sub packages).
 * <p>
 */
public class _NatSpecScriptTemplate extends TestCase
{
	protected LanguageTemplatesBasicsTestImpl languageTemplatesBasics;
	protected LanguageTemplatesHelloWorldSpringBootBasics languageTemplatesHelloWorldSpringBootBasics;

	@SuppressWarnings("unused")
	private boolean isThisFileTheNatSpecTemplate = true;
	private String testCaseFileName;
	
	public _NatSpecScriptTemplate(TestCase callingTestCase) 
	{
		adoptData(callingTestCase);
		languageTemplatesBasics = new LanguageTemplatesBasicsTestImpl(this);
		languageTemplatesHelloWorldSpringBootBasics = new LanguageTemplatesHelloWorldSpringBootBasics(callingTestCase);
		isThisFileTheNatSpecTemplate = this.getClass().getSimpleName().startsWith("_");
	}
	
	private void adoptData(TestCase callingTestCase) 
	{
		testCaseFileName = callingTestCase.getTestCaseFileName();
		SysNatUtil.copyContextData(callingTestCase, this);
	}

	/**
	 * This method is filled by NatSpec with the concrete code that is defined
	 * by the .natspec file for which this template is instantiated. The comment
	 * <code>@MethodBody</code> is replaced with the generated code for all the
	 * sentences in the NatSpec scenario.
	 */
	public void executeTestCase()  
	{
		/* @MethodBody */
	}

	@Override
	public String getTestCaseFileName() {
		return testCaseFileName;
	}
	
	@Override
	public Package getTestCasePackage() {
		return this.getClass().getPackage();
	}

	@Override
	public boolean doesTestBelongToApplicationUnderTest() {
		return true;
	}
}
