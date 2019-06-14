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
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.test.helper.TestCaseForTestPurpose;

/**
 * Tests for the interaction of sysnat.test.runtime.environment and sysnat.testdata.import.
 * The interaction takes place by simple method calls.
 *  
 * @author Reik Oberrath
 */
public class TestDataImport_ModuleLevelTest 
{
	private static final String testFolder = "../sysnat.natural.language.executable.examples/testdata/HomePageIKS";
	
	@Before
	public void setup() {
		ExecutionRuntimeInfo.reset();
	}
	
	@Test
	public void loadsMultipleDatasetsFromSingleDatFiles() throws Exception
	{
		// arrange
		String testDataIdentifier = "ManyDatasets";
		String dataFileName = testDataIdentifier + ".dat";
		SysNatFileUtil.deleteFile(testFolder + "/" + dataFileName);
		ExecutionRuntimeInfo.setSysNatSystemProperty("settings.config", "src/test/resources/testSettingConfigs/HomePageIKS.config");
		ExecutionRuntimeInfo.setSysNatSystemProperty("Environment", "LOCAL");
		final ExecutionRuntimeInfo executionInfo = ExecutionRuntimeInfo.getInstance();
		assertEquals("Number of report message", 0, executionInfo.getReportMessagesOK().size());
		TestCaseForTestPurpose fakeTestCase = new TestCaseForTestPurpose(testDataIdentifier);
		SysNatFileUtil.copyTextFileToTargetDir("../sysnat.quality.assurance/src/test/resources/testdata/ImportTests", 
				                               dataFileName, testFolder);
		

		// act
		fakeTestCase.executeTestCase();

		// cleanup
		SysNatFileUtil.deleteFile(testFolder + "/" + dataFileName);
		
		// assert
		final List<String> reportMessages = executionInfo.getReportMessagesOK().get("TestCaseForTestPurpose");
		assertEquals("Number of report message", 1, reportMessages.size());
		assertEquals("Report message", "3 datasets have been loaded.", reportMessages.get(0));
		assertEquals("Value of field", "value11", fakeTestCase.getTestData().getValue(testDataIdentifier + "_1", "fieldname11"));
		assertEquals("Value of field", "value22", fakeTestCase.getTestData().getValue(testDataIdentifier + "_2", "fieldname22"));
		assertEquals("Value of field", "value31", fakeTestCase.getTestData().getValue(testDataIdentifier + "_3", "fieldname31"));
	}
	
	@Test
	public void loadsMultipleDatasetsFromDatFileSeries() throws Exception
	{
		// arrange
		String testDataIdentifier = "DistributedDatasetSeries";
		String dataFileName1 = testDataIdentifier + "_1.dat";
		String dataFileName2 = testDataIdentifier + "_2.dat";
		SysNatFileUtil.deleteFile(testFolder + "/" + dataFileName1);
		SysNatFileUtil.deleteFile(testFolder + "/" + dataFileName2);
		ExecutionRuntimeInfo.setSysNatSystemProperty("settings.config", "src/test/resources/testSettingConfigs/HomePageIKS.config");
		ExecutionRuntimeInfo.setSysNatSystemProperty("Environment", "LOCAL");
		final ExecutionRuntimeInfo executionInfo = ExecutionRuntimeInfo.getInstance();
		assertEquals("Number of report message", 0, executionInfo.getReportMessagesOK().size());
		TestCaseForTestPurpose fakeTestCase = new TestCaseForTestPurpose(testDataIdentifier);
		SysNatFileUtil.copyTextFileToTargetDir("../sysnat.quality.assurance/src/test/resources/testdata/ImportTests", 
				                               dataFileName1, testFolder);
		SysNatFileUtil.copyTextFileToTargetDir("../sysnat.quality.assurance/src/test/resources/testdata/ImportTests", 
                                               dataFileName2, testFolder);
		

		// act
		fakeTestCase.executeTestCase();

		// cleanup
		SysNatFileUtil.deleteFile(testFolder + "/" + dataFileName1);
		SysNatFileUtil.deleteFile(testFolder + "/" + dataFileName2);
		
		// assert
		final List<String> reportMessages = executionInfo.getReportMessagesOK().get("TestCaseForTestPurpose");
		assertEquals("Number of report message", 1, reportMessages.size());
		assertEquals("Report message", "3 datasets have been loaded.", reportMessages.get(0));
		assertEquals("Value of field", "value11", fakeTestCase.getTestData().getValue(testDataIdentifier + "_1", "fieldname11"));
		assertEquals("Value of field", "value22", fakeTestCase.getTestData().getValue(testDataIdentifier + "_2_1", "fieldname22"));
		assertEquals("Value of field", "value31", fakeTestCase.getTestData().getValue(testDataIdentifier + "_2_2", "fieldname31"));
	}

	@Test
	public void loadsMultipleDatasetsFromExcelFile() throws Exception {
		testDataLoadingFromExcelFile("ExcelData");
	}

	@Test
	public void loadsMultipleDatasetsFromRotatedExcelFile() throws Exception {
		testDataLoadingFromExcelFile("RotatedExcelData");
	}
	
	private void testDataLoadingFromExcelFile(String testDataIdentifier) throws Exception
	{
		// arrange
		String dataFileName = testDataIdentifier + ".xlsx";
		SysNatFileUtil.deleteFile(testFolder + "/" + dataFileName);
		ExecutionRuntimeInfo.setSysNatSystemProperty("settings.config", "src/test/resources/testSettingConfigs/HomePageIKS.config");
		ExecutionRuntimeInfo.setSysNatSystemProperty("Environment", "LOCAL");
		final ExecutionRuntimeInfo executionInfo = ExecutionRuntimeInfo.getInstance();
		assertEquals("Number of report message", 0, executionInfo.getReportMessagesOK().size());
		TestCaseForTestPurpose fakeTestCase = new TestCaseForTestPurpose(testDataIdentifier);
		SysNatFileUtil.copyBinaryFile("../sysnat.quality.assurance/src/test/resources/testdata/ImportTests/" + dataFileName, 
				                      testFolder + "/" + dataFileName);
		

		// act
		fakeTestCase.executeTestCase();

		// cleanup
		SysNatFileUtil.deleteFile(testFolder + "/" + dataFileName);
		
		// assert
		final List<String> reportMessages = executionInfo.getReportMessagesOK().get("TestCaseForTestPurpose");
		assertEquals("Number of report message", 1, reportMessages.size());
		assertEquals("Report message", "3 datasets have been loaded.", reportMessages.get(0));
		assertEquals("Value of field", "value12", fakeTestCase.getTestData().getValue(testDataIdentifier + "_dataset1", "fieldname2"));
		assertEquals("Value of field", "value23", fakeTestCase.getTestData().getValue(testDataIdentifier + "_dataset2", "fieldname3"));
		assertEquals("Value of field", "value31", fakeTestCase.getTestData().getValue(testDataIdentifier + "_dataset3", "fieldname1"));
	}
	
}