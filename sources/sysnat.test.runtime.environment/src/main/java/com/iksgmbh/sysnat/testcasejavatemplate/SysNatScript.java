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
package com.iksgmbh.sysnat.testcasejavatemplate;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.utils.SysNatTestRuntimeUtil;

/**
 * This class is a wrapper for implementations of natural language scripts (.nls files). 
 * For technical reasons it is a subclass of ExecutableExample,
 * although it is not executable on its own.
 */
public abstract class SysNatScript extends ExecutableExample
{
	private String testCaseFileName;
		
	public SysNatScript(ExecutableExample callingTestCase) {
		adoptContextDataFrom(callingTestCase);
	}
	
	public abstract void executeScript();
	
	protected void adoptContextDataFrom(ExecutableExample callingExecutableExample) 
	{
		testCaseFileName = callingExecutableExample.getTestCaseFileName();
		SysNatTestRuntimeUtil.copyContextData(callingExecutableExample, this);
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


	@Override
	public void executeTestCase() {
		// do nothing here for scripts
	}

}