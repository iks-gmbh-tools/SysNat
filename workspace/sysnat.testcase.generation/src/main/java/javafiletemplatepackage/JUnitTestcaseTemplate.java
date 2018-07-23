//# This file serves as template to create JUnit test classes.
//# For this purpose, package and class name will be overwritten
//# and a blocks of java commands will be injected into execute method.
//# Each instance fields of this class will be scanned to be a LanguageTemplateContainer.
package javafiletemplatepackage;

//import org.junit.Test;
import org.junit.After;
import org.junit.Before;

import com.iksgmbh.sysnat.testcasejavatemplate.TestCaseTemplateParent;
/* TO BE REPLACED: imports */

/**
 * JUnitTestCaseTemplate for TestApplication 'XY'.
 * TO BE REPLACED: class java doc comment
 */
public class JUnitTestcaseTemplate extends TestCaseTemplateParent
{
	/* TO BE REPLACED: fields for language template containers */
	
	@Before
	public void setUp() 
	{
		super.setUp();
		/* TO BE REPLACED: field initialization */			
	}

	@After
	public void shutdown() 
	{
		if ( ! isSkipped() && executionInfo.isApplicationStarted()) {
			//if (languageTemplateContainer != null) languageTemplateContainer.gotoStartPage();
		}
		/* TO BE REPLACED: cleanup */			
		super.shutdown();
	}

	//# The comment line of this method will be replaced 
	//# when merging this template with the code generated
	//# from the natural language test case files.
	//@Test
	@Override
	public void executeTestCase() 
	{
		try {
			/* TO BE REPLACED: Command Block */		
			
			closeCurrentTestCaseWithSuccess();
		} catch (Throwable e) {
			super.handleThrowable(e);
		}
	}
}
