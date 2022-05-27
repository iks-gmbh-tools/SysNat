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

import static com.iksgmbh.sysnat.common.utils.SysNatConstants.GREEN_HTML_COLOR;
import static com.iksgmbh.sysnat.common.utils.SysNatConstants.ORANGE_HTML_COLOR;
import static com.iksgmbh.sysnat.common.utils.SysNatConstants.RED_HTML_COLOR;
import static com.iksgmbh.sysnat.common.utils.SysNatConstants.WHITE_HTML_COLOR;
import static com.iksgmbh.sysnat.common.utils.SysNatConstants.YELLOW_HTML_COLOR;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.ASSERT_ERROR_TEXT;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.TECHNICAL_ERROR_TEXT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat._testcases.TestCaseFailed;
import com.iksgmbh.sysnat._testcases.TestCaseInactive;
import com.iksgmbh.sysnat._testcases.TestCaseInactive1Behaviour;
import com.iksgmbh.sysnat._testcases.TestCaseInactive1Feature;
import com.iksgmbh.sysnat._testcases.TestCaseInactive2Behaviour;
import com.iksgmbh.sysnat._testcases.TestCaseInactive2Feature;
import com.iksgmbh.sysnat._testcases.TestCaseOK;
import com.iksgmbh.sysnat._testcases.TestCaseWrong;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;

public class ReportCreatorClassLevelTest 
{
	private List<ExecutableExample> executableExamples = new ArrayList<ExecutableExample>();
	private String result;
	
	@Before
	public void setUp() {
		ExecutionRuntimeInfo.reset();
		ExecutionRuntimeInfo.setSysNatSystemProperty(SysNatConstants.TESTING_CONFIG_PROPERTY, "../sysnat.test.runtime.environment/src/test/resources/testSettingConfigs/settingsHomePageIKS.config");
	}
	
	@Test
	public void createReportForExecutionWithoutTests() 
	{
		// arrange
		ExecutionRuntimeInfo.getInstance();

		// act
		result = ReportCreator.createFullOverviewReport();

		// assert
		assertReportOverview(0, 0, 0, 0, 0, 0);
	}
	
	@Test
	public void createReportForExecutionWithOneInactivTest() 
	{
		// arrange
		ExecutionRuntimeInfo.getInstance();
		executableExamples.add(new TestCaseInactive());

		// act
		executeAllExamples();
		result = ReportCreator.createFullOverviewReport();
		//System.out.println(result);

		// assert
		assertReportOverview(1, 0, 1, 0, 0, 0);
	}
	
	@Test
	public void createReportForExecutionOnlyWithOneOkTest() 
	{
		// arrange
		ExecutionRuntimeInfo.getInstance();
		executableExamples.add(new TestCaseOK());

		// act
		executeAllExamples();
		result = ReportCreator.createFullOverviewReport();
		System.out.println(result);

		// assert
		assertReportOverview(1, 1, 0, 1, 0, 0);
	}
	
	@Test
	public void createReportForExecutionOnlyWithOneWrongTest() 
	{
		// arrange
		ExecutionRuntimeInfo.getInstance();
		executableExamples.add(new TestCaseWrong());

		// act
		executeAllExamples();
		result = ReportCreator.createFullOverviewReport();
		System.out.println(result);

		// assert
		assertReportOverview(1, 1, 0, 0, 1, 0);
	}
	
	@Test
	public void createReportForExecutionOnlyWithOneFailedTest() 
	{
		// arrange
		ExecutionRuntimeInfo.getInstance();
		executableExamples.add(new TestCaseFailed());

		// act
		executeAllExamples();
		result = ReportCreator.createFullOverviewReport();
		System.out.println(result);

		// assert
		assertReportOverview(1, 1, 0, 0, 0, 1);
	}
	
	@Test
	public void createReportForExecutionWithOKWrongAndFailedTest() 
	{
		// arrange
		ExecutionRuntimeInfo.getInstance();
		executableExamples.add(new TestCaseWrong());
		executableExamples.add(new TestCaseOK());
		executableExamples.add(new TestCaseFailed());

		// act
		executeAllExamples();
		result = ReportCreator.createFullOverviewReport();
		System.out.println(result);

		// assert
		assertReportOverview(3, 3, 0, 1, 1, 1);
		assertOrderOfTestsInDetails();
	}

	/**
	 * The execution order must reflect the order in details section of the overview report.
	 */
	private void assertOrderOfTestsInDetails()
	{
		List<String> lines = SysNatStringUtil.toListOfLines(result);
		boolean detailSectionReached = false;
		int lineNoTestCaseWrong = -1;
		int lineNoTestCaseOk = -1;
		int lineNoTestCaseFailed = -1;
		int lineCounter = 0;
		
		for (String line : lines) 
		{
			lineCounter++;
			if (line.contains("Detail")) {
				detailSectionReached = true;
			}
			
			if (detailSectionReached) 
			{
				if (line.contains("1. Orange Test:")) lineNoTestCaseWrong=lineCounter;
				if (line.contains("2. Green Test:")) lineNoTestCaseOk=lineCounter;
				if (line.contains("3. Red Test:")) lineNoTestCaseFailed=lineCounter;
			}
			
		}
		
		assertTrue(lineNoTestCaseWrong < lineNoTestCaseOk);
		assertTrue(lineNoTestCaseOk < lineNoTestCaseFailed);
	}

	@Test
	public void createReportForExecutionWithOKWrongFailedAndInactivTest() 
	{
		// arrange
		ExecutionRuntimeInfo.getInstance();
		executableExamples.add(new TestCaseFailed());
		executableExamples.add(new TestCaseOK());
		executableExamples.add(new TestCaseWrong());
		executableExamples.add(new TestCaseInactive());

		// act
		executeAllExamples();
		result = ReportCreator.createFullOverviewReport();
		System.out.println(result);

		// assert
		assertReportOverview(4, 3, 1, 1, 1, 1);
	}
	
	
	@Test
	public void createReportForIdenticalXXIDs() 
	{
		// arrange
		ExecutionRuntimeInfo.getInstance();
		executableExamples.add(new TestCaseOK());
		executableExamples.add(new TestCaseOK());

		// act
		executeAllExamples();
		result = ReportCreator.createFullOverviewReport();
		System.out.println(result);

		// assert
		assertReportOverview(2, 2, 0, 1, 0, 1);
		assertTrue("Expected error message not found.", result.contains("Fehler: Uneindeutige XXId"));
	}

	@Test
	public void createReportWithInactiveTestsFeaturesAndBehaviour() 
	{
		// arrange
		ExecutionRuntimeInfo.getInstance();
		executableExamples.add(new TestCaseInactive());
		executableExamples.add(new TestCaseOK());
		executableExamples.add(new TestCaseInactive1Behaviour());
		executableExamples.add(new TestCaseInactive2Behaviour());
		executableExamples.add(new TestCaseInactive1Feature());
		executableExamples.add(new TestCaseInactive2Feature());

		// act
		executeAllExamples();
		result = ReportCreator.createFullOverviewReport();
		SysNatFileUtil.writeFile("./target/Test.html", result);

		// assert
		System.err.println(result);
		assertReportOverview(6, 1, 5, 1, 0, 0);
		assertReportContains(result, "background:#CDE301;" + 
                System.getProperty("line.separator") +
				"padding:0cm;mso-padding-alt:0cm 0cm 1.0pt 0cm'><span style='font-size:14.0pt;" +
                System.getProperty("line.separator") +
				"line-height:106%'>Inactive Behaviour&nbsp;&nbsp;&nbsp;&nbsp;#&nbsp;&nbsp;&nbsp;&nbsp;Inactive Feature&nbsp;&nbsp;&nbsp;&nbsp;#&nbsp;&nbsp;&nbsp;&nbsp;Inactive Test" +
                System.getProperty("line.separator") + "<br>");
	}

	
	@Test
	public void buildsDetailsHtmlSection_WithOnetimeInstructions() 
	{
		// arrange
		List<String> messages1 = new ArrayList<>();
		messages1.add("This is a OneTimePrecondition instruction.");
		messages1.add("This is another OneTimePrecondition instruction.");
		messages1.add(ReportCreator.END_OF_ONETIME_PRECONDTIONS_COMMENT);
		messages1.add("Something has been done.");
		createDataForExecutedXX("XX1", messages1);
		
		List<String> messages2 = new ArrayList<>();
		messages2.add("Something has been done.");
		messages2.add("Something else has been done.");
		createDataForExecutedXX("XX2", messages2);

		List<String> messages3 = new ArrayList<>();
		messages3.add("Something has been done.");
		messages3.add("Something else has been done.");
		messages3.add("Another thing has been done.");
		messages3.add(ReportCreator.START_OF_ONETIME_CLEANUPS_COMMENT);
		messages3.add("This is a OneTimeCleanup instruction.");
		messages3.add("This is another OneTimeCleanup instruction.");
		createDataForExecutedXX("XX3", messages3);

		// act
		result = new ReportCreator().buildDetailSection();

		// assert
		result = result.replaceAll("&nbsp;", "").replaceAll("#", " # ").trim();
		String expectedFileContent = SysNatFileUtil.readTextFileToString(
				"../sysnat.test.runtime.environment/src/test/resources/expectedReports/"
				+ "OnetimeBehaviorInstructionTest.txt");
        assertEquals("Expected report", expectedFileContent, result);
	}
	
	
	private void createDataForExecutedXX(String xxid, List<String> messages)
	{
		ExecutionRuntimeInfo.getInstance().addTestMessagesOK(xxid, messages, "aBehaviour");
		ExecutionRuntimeInfo.getInstance().countAsExecuted(xxid);
	}

	@Test
	public void buildsExcecutedHtmlSection_standard() 
	{
		// arrange
		ExecutionRuntimeInfo executionInfo = ExecutionRuntimeInfo.getInstance();
		List<String> messages = new ArrayList<>();
		executionInfo.addTestMessagesFAILED("FailedXX1", messages, null);
		executionInfo.addTestMessagesFAILED("FailedXX2a", messages, "aFailedFeature");
		executionInfo.addTestMessagesFAILED("FailedXX2b", messages, "aFailedFeature");
		executionInfo.addTestMessagesWRONG("WrongXX1", messages, null);
		executionInfo.addTestMessagesWRONG("WrongXX2", messages, "aFailedFeature");
		executionInfo.addTestMessagesWRONG("WrongXX3", messages, "aWrongBehaviour");
		executionInfo.addTestMessagesOK("OkXX1", messages, null);
		executionInfo.addTestMessagesOK("OkXX2", messages, "aFailedFeature");
		executionInfo.addTestMessagesOK("OkXX3", messages, "aWrongBehaviour");
		executionInfo.addTestMessagesOK("OkXX5", messages, "aOkGroup");
		executionInfo.addTestMessagesOK("OkXX6", messages, "aOkGroup");
		executionInfo.addTestMessagesOK("OkXX7", messages, null);
		executionInfo.addTestMessagesOK("OkXX8", messages, null);
		executionInfo.addTestMessagesOK("OkXX9", messages, null);
		executionInfo.addTestMessagesOK("OkXX10", messages, null);

		String report = "PLACEHOLDER_RED_EXECUTED_TESTS" + System.getProperty("line.separator") +
						"PLACEHOLDER_ORANGE_EXECUTED_TESTS" + System.getProperty("line.separator") +
						"PLACEHOLDER_GREEN_EXECUTED_TESTS" + System.getProperty("line.separator");
		
		// act
		result = new ReportCreator().replaceExecutedPlaceholders(report);

		// assert
		result = result.replaceAll("&nbsp;", "").replaceAll("#", " # ").trim();
		String expectedFileContent = SysNatFileUtil.readTextFileToString(
				"../sysnat.test.runtime.environment/src/test/resources/expectedReports/executionPlaceholderTest.txt");
        assertEquals("Expected report", expectedFileContent, result);
	}
	
	@Test
	public void buildsDetailHtmlSectionForGreenBehaviour() 
	{
		// arrange
		ExecutionRuntimeInfo executionInfo = ExecutionRuntimeInfo.getInstance();
		List<String> messages = new ArrayList<>();
		messages.add("First action.");
		messages.add("Second action.");
		messages.add("Third action.");
		executionInfo.addTestMessagesOK("XX_A1", messages, "OkGroup1");
		executionInfo.addTestMessagesOK("XX_A2", messages, "OkGroup1");
		executionInfo.addTestMessagesOK("XX_B", messages, "OkGroup2");
		
		// act
		result = new ReportCreator().buildDetailSection();

		// assert
		String expectedFileContent = SysNatFileUtil.readTextFileToString(
				"../sysnat.test.runtime.environment/src/test/resources/expectedReports/DetailsWithTwoOkBehaviors.txt");
        assertEquals("Expected report", expectedFileContent.trim(), result.trim());
	}
		
	
	@Test
	public void buildsDetailHtmlSectionForBehaviourWithOneFailedAndOneWrongXX() 
	{
		// arrange
		ExecutionRuntimeInfo executionInfo = ExecutionRuntimeInfo.getInstance();
		List<String> messages = new ArrayList<>();
		messages.add("First action.");
		messages.add("Second action.");
		messages.add("Third action.");
		executionInfo.addTestMessagesOK("XX_A1", messages, "FailedGroup1");
		executionInfo.addTestMessagesFAILED("XX_A2", messages, "FailedGroup1");
		executionInfo.addTestMessagesWRONG("XX_A3", messages, "FailedGroup1");
		
		// act
		result = new ReportCreator().buildDetailSection();

		// assert
		String expectedFileContent = SysNatFileUtil.readTextFileToString(
				"../sysnat.test.runtime.environment/src/test/resources/expectedReports/DetailsForBehaviorWithFailedXX.txt");
        assertEquals("Expected report", expectedFileContent.trim(), result.trim());
	}

	@Test
	public void buildsDetailHtmlSectionForFeatureWithOneWrongXX() 
	{
		// arrange
		ExecutionRuntimeInfo executionInfo = ExecutionRuntimeInfo.getInstance();
		final String feature = "WrongGroup1";
		executionInfo.addToKnownFeatures(feature);
		List<String> messages = new ArrayList<>();
		messages.add("First action.");
		messages.add("Second action.");
		messages.add("Third action.");
		executionInfo.addTestMessagesOK("XX_A1", messages, feature);
		executionInfo.addTestMessagesWRONG("XX_A2", messages, feature);
		executionInfo.addTestMessagesOK("XX_A3", messages, feature);
		
		// act
		result = new ReportCreator().buildDetailSection();

		// assert
		String expectedFileContent = SysNatFileUtil.readTextFileToString(
				"../sysnat.test.runtime.environment/src/test/resources/expectedReports/DetailsForFeatureWithWrongXX.txt");
        assertEquals("Expected report", expectedFileContent.trim(), result.trim());
	}

	@Test
	public void buildsDetailHtmlSectionForSixStandaloneXX() 
	{
		// arrange
		ExecutionRuntimeInfo executionInfo = ExecutionRuntimeInfo.getInstance();
		List<String> messages = new ArrayList<>();
		messages.add("First action.");
		messages.add("Second action.");
		messages.add("Third action.");
		executionInfo.addTestMessagesOK("XX_A1", messages, null);
		executionInfo.addTestMessagesWRONG("XX_A2", messages, null);
		executionInfo.addTestMessagesFAILED("XX_A3", messages, null);
		executionInfo.addTestMessagesOK("XX_A4", messages, null);
		executionInfo.addTestMessagesWRONG("XX_A5", messages, null);
		executionInfo.addTestMessagesFAILED("XX_A6", messages, null);
		
		// act
		result = new ReportCreator().buildDetailSection();

		// assert
		String expectedFileContent = SysNatFileUtil.readTextFileToString(
				"../sysnat.test.runtime.environment/src/test/resources/expectedReports/DetailsForStandAloneXX.txt");
        assertEquals("Expected report", expectedFileContent.trim(), result.trim());
	}
	
	@Test
	public void buildsDetailHtmlSectionWithBehaviourFeatureAndStandaloneXX() 
	{
		// arrange
		ExecutionRuntimeInfo executionInfo = ExecutionRuntimeInfo.getInstance();
		executionInfo.addToKnownFeatures("myFeature");
		List<String> messages = new ArrayList<>();
		messages.add("First action.");
		messages.add("Second action.");
		messages.add("Third action.");
		executionInfo.addTestMessagesOK("XX_A1", messages, "myFeature");
		executionInfo.addTestMessagesOK("XX_A2", messages, "myFeature");
		executionInfo.addTestMessagesOK("XX_A3", messages, "myBehaviour");
		executionInfo.addTestMessagesOK("XX_A4", messages, "myBehaviour");
		executionInfo.addTestMessagesOK("XX_A5", messages, null);
		executionInfo.addTestMessagesOK("XX_A6", messages, null);
		
		// act
		result = new ReportCreator().buildDetailSection();

		// assert
		String expectedFileContent = SysNatFileUtil.readTextFileToString(
				"../sysnat.test.runtime.environment/src/test/resources/expectedReports/DetailsForMixedSituation.txt");
        assertEquals("Expected report", expectedFileContent.trim(), result.trim());
	}
	
	private void assertReportContains(String report, String expected) {
		assertTrue("Unexpected report content.", report.contains(expected));		
	}	

	
	private void executeAllExamples() 
	{
		for (ExecutableExample executableExample : executableExamples) {
			try { executableExample.executeTestCase(); } catch(Exception e) {};
		}
	}

	private void assertReportOverview(
			  final int numberOfAllTestCases,
			  final int numberOfAllExecutedTestCases,
			  final int numberOfInactivTestCases,
			  final int numberOfSuccessfullTestCases,
			  final int numberOfWrongTestCases,
			  final int numberOfFailedTestCases)
	{
		assertTrue("Report with unexpected product line", result.contains(">Test-Applikation:</span>"));
		assertTrue("Report with unexpected product line", result.contains(">HomePageIKS</span>"));
		
		assertTrue("Report with unexpected environment line", result.contains(">Zielumgebung:</span>"));
		assertTrue("Report with unexpected environment line", result.contains(">PRODUCTION</span>"));
		
		assertTrue("Report with unexpected timestamp line", result.contains(">Ausführungszeitpunkt:<"));
		assertTrue("Report with unexpected duration line", result.contains(">Ausführungsdauer:<"));
		
		assertTrue("Report with unexpected filter line", result.contains(">Ausführungsfilter:<"));
		
		assertTrue("Report with unexpected result line", result.contains(">Gesamtergebnis:<"));
		String resultCompressed = result.replaceAll(" ", "").replaceAll("\\n", "").replaceAll("\\r", "");
		String resultWithoutLineBreaks = result.replaceAll("  ", "").replaceAll("\\n", "").replaceAll("\\r", "");
		if (numberOfFailedTestCases > 0)  {
			assertTrue("Report with unexpected result line", resultWithoutLineBreaks.contains("background:" + RED_HTML_COLOR + "'>" + TECHNICAL_ERROR_TEXT + "<"));
		} else if (numberOfWrongTestCases > 0)  {
			assertTrue("Report with unexpected result line", resultWithoutLineBreaks.contains("background:" + ORANGE_HTML_COLOR + "'>" + ASSERT_ERROR_TEXT + "<"));
		} else if (numberOfInactivTestCases > 0 && numberOfAllExecutedTestCases > 0) {
			assertTrue("Report with unexpected result line", resultWithoutLineBreaks.contains("background:" + YELLOW_HTML_COLOR + "'>" + "OK" + "<"));
		} else if (numberOfInactivTestCases == 0 && numberOfAllExecutedTestCases > 0 ) {
			assertTrue("Report with unexpected result line", resultWithoutLineBreaks.contains("background:" + GREEN_HTML_COLOR + "'>OK<"));
		} else if (numberOfInactivTestCases > 0 && numberOfAllExecutedTestCases == 0) {
			assertTrue("Report with unexpected result line", resultCompressed.contains("background:" + YELLOW_HTML_COLOR + "'>-<"));
		} else {
			assertTrue("Report with unexpected result line", resultCompressed.contains("background:" + WHITE_HTML_COLOR + "'>-<"));
		}
		
		assertTrue("Report with unexpected Statistics line 1", result.contains("Anzahl gefundener Tests:<"));
		assertTrue("Report with unexpected Statistics line 1", result.replaceAll(" ", "").contains("'>" + numberOfAllTestCases + "</span>"));
		
		assertTrue("Report with unexpected Statistics line 2", result.contains("Anzahl durchgeführter Tests:</span>"));
		assertTrue("Report with unexpected Statistics line 2", result.replaceAll(" ", "").contains("'>" + numberOfAllExecutedTestCases + "</span>"));
		
		String backgroundColor = WHITE_HTML_COLOR;
		if (numberOfInactivTestCases > 0) backgroundColor = YELLOW_HTML_COLOR;
		assertTrue("Report with unexpected Statistics line 3", resultWithoutLineBreaks.contains("background:" + backgroundColor + "'>Anzahl inaktiver Tests:<"));
		assertTrue("Report with unexpected Statistics line 3", resultCompressed.contains("background:" + backgroundColor + "'>" + numberOfInactivTestCases + "<"));
		
		backgroundColor = WHITE_HTML_COLOR;
		if (numberOfSuccessfullTestCases > 0) backgroundColor = GREEN_HTML_COLOR;
		assertTrue("Report with unexpected Statistics line 4", resultWithoutLineBreaks.contains("background:" + backgroundColor + "'>Anzahl erfolgreicher Tests:<"));
		assertTrue("Report with unexpected Statistics line 4", resultWithoutLineBreaks.contains(";background:" + backgroundColor + "'>" + numberOfSuccessfullTestCases + "</span>"));
		
		backgroundColor = WHITE_HTML_COLOR;
		if (numberOfWrongTestCases > 0) backgroundColor = ORANGE_HTML_COLOR;
		assertTrue("Report with unexpected Statistics line 5", resultWithoutLineBreaks.contains("background:" + backgroundColor + "'>Anzahl fachlich fehlgeschlagener Tests:<"));
		assertTrue("Report with unexpected Statistics line 5", resultWithoutLineBreaks.contains(";background:" + backgroundColor + "'>" + numberOfWrongTestCases + "</span>"));
		
		backgroundColor = WHITE_HTML_COLOR;
		if (numberOfFailedTestCases > 0) backgroundColor = RED_HTML_COLOR;
		assertTrue("Report with unexpected Statistics line 6", resultWithoutLineBreaks.contains("background:" + backgroundColor + "'>Anzahl technisch fehlgeschlagener Tests:<"));
		assertTrue("Report with unexpected Statistics line 6", resultWithoutLineBreaks.contains(";background:" + backgroundColor + "'>" + numberOfFailedTestCases + "</span>"));
	}
}