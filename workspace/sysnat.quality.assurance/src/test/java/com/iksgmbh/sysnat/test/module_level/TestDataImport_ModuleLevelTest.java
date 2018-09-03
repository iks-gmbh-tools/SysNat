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
package com.iksgmbh.sysnat.test.module_level;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.test.helper.TestCaseForTestPurpose;

/**
 * Tests for the interaction of sysnat.test.runtime.environment and sysnat.testdata.import.
 * The interaction takes place by simple method calls.
 *  
 * @author Reik Oberrath
 */
public class TestDataImport_ModuleLevelTest 
{
	@Before
	public void setup() {
		ExecutionRuntimeInfo.reset();
	}
	
	@Test
	public void loadsDataFromDatFiles() throws Exception
	{
		// arrange
		ExecutionRuntimeInfo.setSysNatSystemProperty("settings.config", "src/test/resources/testSettingConfigs/HomePageIKS.config");
		ExecutionRuntimeInfo.setSysNatSystemProperty("Environment", "LOCAL");
		final ExecutionRuntimeInfo executionInfo = ExecutionRuntimeInfo.getInstance();
		assertEquals("Number of report message", 0, executionInfo.getReportMessagesOK().size());
		TestCaseForTestPurpose fakeTestCase = new TestCaseForTestPurpose();

		// act
		fakeTestCase.executeTestCase();

		// assert
		final List<String> reportMessages = executionInfo.getReportMessagesOK().get("TestCaseForTestPurpose");
		assertEquals("Number of report message", 1, reportMessages.size());
		assertEquals("Report message", "3 datasets have been loaded.", reportMessages.get(0));
		assertEquals("Value of field", "value22", fakeTestCase.getTestData().getValue("ManyDatasets_2", "fieldname22"));
	}
}