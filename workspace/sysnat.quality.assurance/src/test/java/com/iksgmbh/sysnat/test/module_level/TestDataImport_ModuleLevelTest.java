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
