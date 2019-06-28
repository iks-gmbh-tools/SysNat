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
package com.iksgmbh.sysnat.helper;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.ExecutableExample;

public class VirtualTestCase extends ExecutableExample 
{
	public VirtualTestCase(String id) {
		super.setXXID(id);
		executionInfo = ExecutionRuntimeInfo.getInstance();
		setGuiController(executionInfo.getGuiController());
	}
	
	@Override
	public void executeTestCase() {
	}

	@Override
	public String getTestCaseFileName() {
		return null;
	}

	@Override
	public Package getTestCasePackage() {
		return null;
	}

	@Override
	public boolean doesTestBelongToApplicationUnderTest() {
		return true;
	}

}