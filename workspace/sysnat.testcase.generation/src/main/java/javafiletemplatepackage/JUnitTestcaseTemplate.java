/*
 * Copyright 2018 IKS Gesellschaft fuer Informations- und Kommunikationssysteme mbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	/* TO BE REPLACED: constants */
	
	/* TO BE REPLACED: fields for language template containers */
	
	/* TO BE REPLACED: constructor */
	
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
		/* TO BE REPLACED: technical cleanup */			
		super.shutdown();
	}

	/* TO BE REPLACED: business precondition */		
	
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
	
	/* TO BE REPLACED: business cleanup */			
}