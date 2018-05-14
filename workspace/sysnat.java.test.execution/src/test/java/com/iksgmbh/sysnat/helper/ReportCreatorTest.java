package com.iksgmbh.sysnat.helper;

import static com.iksgmbh.sysnat.utils.SysNatLocaleConstants.*;
import static com.iksgmbh.sysnat.utils.SysNatConstants.*;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.ExecutionInfo;
import com.iksgmbh.sysnat.TestCase;
import com.iksgmbh.sysnat._testcases.TestCaseFailed;
import com.iksgmbh.sysnat._testcases.TestCaseInactive;
import com.iksgmbh.sysnat._testcases.TestCaseOK;
import com.iksgmbh.sysnat._testcases.TestCaseWrong;
import com.iksgmbh.sysnat.helper.ReportCreator;

public class ReportCreatorTest 
{
	private List<TestCase> testCases = new ArrayList<TestCase>();
	private String result;
	
	@Before
	public void setUp() {
		ExecutionInfo.reset();
		ExecutionInfo.getInstance().setTestApplName("com");
	}
	
	@Test
	public void createReportForExecutionWithoutTests() 
	{
		// arrange
		ExecutionInfo.getInstance();

		// act
		result = ReportCreator.doYourJob();

		// assert
		assertReportOverview(0, 0, 0, 0, 0, 0);
	}
	
	@Test
	public void createReportForExecutionWithOneInactivTest() 
	{
		// arrange
		ExecutionInfo.getInstance();
		testCases.add(new TestCaseInactive());

		// act
		executeAllTestCases();
		result = ReportCreator.doYourJob();
		//System.out.println(result);

		// assert
		assertReportOverview(1, 0, 1, 0, 0, 0);
	}
	
	@Test
	public void createReportForExecutionOnlyWithOneOkTest() 
	{
		// arrange
		ExecutionInfo.getInstance();
		testCases.add(new TestCaseOK());

		// act
		executeAllTestCases();
		result = ReportCreator.doYourJob();
		System.out.println(result);

		// assert
		assertReportOverview(1, 1, 0, 1, 0, 0);
	}
	
	@Test
	public void createReportForExecutionOnlyWithOneWrongTest() 
	{
		// arrange
		ExecutionInfo.getInstance();
		testCases.add(new TestCaseWrong());

		// act
		executeAllTestCases();
		result = ReportCreator.doYourJob();
		System.out.println(result);

		// assert
		assertReportOverview(1, 1, 0, 0, 1, 0);
	}
	
	@Test
	public void createReportForExecutionOnlyWithOneFailedTest() 
	{
		// arrange
		ExecutionInfo.getInstance();
		testCases.add(new TestCaseFailed());

		// act
		executeAllTestCases();
		result = ReportCreator.doYourJob();
		System.out.println(result);

		// assert
		assertReportOverview(1, 1, 0, 0, 0, 1);
	}
	
	@Test
	public void createReportForExecutionWithOKWrongAndFailedTest() 
	{
		// arrange
		ExecutionInfo.getInstance();
		testCases.add(new TestCaseFailed());
		testCases.add(new TestCaseOK());
		testCases.add(new TestCaseWrong());

		// act
		executeAllTestCases();
		result = ReportCreator.doYourJob();
		System.out.println(result);

		// assert
		assertReportOverview(3, 3, 0, 1, 1, 1);
	}

	@Test
	public void createReportForExecutionWithOKWrongFailedAndInactivTest() 
	{
		// arrange
		ExecutionInfo.getInstance();
		testCases.add(new TestCaseFailed());
		testCases.add(new TestCaseOK());
		testCases.add(new TestCaseWrong());
		testCases.add(new TestCaseInactive());

		// act
		executeAllTestCases();
		result = ReportCreator.doYourJob();
		System.out.println(result);

		// assert
		assertReportOverview(4, 3, 1, 1, 1, 1);
	}
	
	@Test
	public void createReportForIdenticalTestIDs() 
	{
		// arrange
		ExecutionInfo.getInstance();
		testCases.add(new TestCaseOK());
		testCases.add(new TestCaseOK());

		// act
		executeAllTestCases();
		result = ReportCreator.doYourJob();
		System.out.println(result);

		// assert
		assertReportOverview(2, 2, 0, 1, 0, 1);
		assertTrue("Expected error message not found.", result.contains("Fehler: Uneindeutige Test-Id"));
	}
	
	private void executeAllTestCases() 
	{
		for (TestCase testCase : testCases) {
			testCase.executeTestCase();
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
		assertTrue("Report with unexpected product line", result.contains(">HelloWorldSpringBoot</span>"));
		
		assertTrue("Report with unexpected environment line", result.contains(">Zielumgebung:</span>"));
		assertTrue("Report with unexpected environment line", result.contains(">LOCAL</span>"));
		
		assertTrue("Report with unexpected timestamp line", result.contains(">Ausführungszeitpunkt:<"));
		assertTrue("Report with unexpected duration line", result.contains(">Ausführungsdauer:<"));
		
		assertTrue("Report with unexpected filter line", result.contains(">Kategorien-Filter:<"));
		
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
