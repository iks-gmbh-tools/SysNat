package com.iksgmbh.sysnat.helper;

import static com.iksgmbh.sysnat.utils.SysNatConstants.*;
import static com.iksgmbh.sysnat.utils.SysNatLocaleConstants.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.iksgmbh.sysnat.ExecutionInfo;
import com.iksgmbh.sysnat.TestCase;
import com.iksgmbh.sysnat.utils.SysNatConstants;

public class ReportCreator 
{
	private static final String REPORT_TEMPLATE = "resources/TestReport.htm.Template." 
                                                   + Locale.getDefault().getLanguage() + ".txt";
	private static final String[] REPORT_PARAGRAPH_KEYWORDS =  { ARRANGE_KEYWORD, ACT_KEYWORD, ASSERT_KEYWORD, CLEANUP_KEYWORD};
	
	private HashMap<String, List<String>> reportMessagesOK;
	private HashMap<String, List<String>> reportMessagesWRONG;
	private HashMap<String, List<String>> reportMessagesFAILED;

	private int testCounter = 0;
	private int messageCounter = 0;
	private ExecutionInfo executionInfo;

	public static String doYourJob() {
		return new ReportCreator().createTestReport();
	}
	
	private ReportCreator() 
	{
		executionInfo = ExecutionInfo.getInstance();
		reportMessagesOK = executionInfo.getReportMessagesOK();
		reportMessagesWRONG = executionInfo.getReportMessagesWRONG();
		reportMessagesFAILED = executionInfo.getReportMessagesFAILED();
	}

	private String createTestReport() 
	{
		String report = readReportTemplate();
		
		report = report.replace("PLACEHOLDER_PRODUCT", executionInfo.getAppUnderTest().name());
		report = report.replace("PLACEHOLDER_TARGET_ENV", executionInfo.getTargetEnv().name());
		report = report.replace("PLACEHOLDER_TIME", executionInfo.getStartPointOfTimeAsString());
		report = report.replace("PLACEHOLDER_DURATION", executionInfo.getExecutionDurationAsString());
		report = report.replace("PLACEHOLDER_CATEGORIES", executionInfo.getTestCategories());
		
		report = replacePlaceholdersOverallResult(report);
		report = replaceTestStatiticsPlaceholder(report);
		report = replaceFoundCategoriesPlaceholder(report);
		report = replaceExecutedTestsPlaceholder(report);
		
		report = report.replace("PLACEHOLDER_INACTIVE_TEST_CASES", executionInfo.getInactiveTestListAsString());
		
		report = report.replace("PLACEHOLDER_DETAILS", buildDetailPart());
		
		return report;
	}

	private String replaceFoundCategoriesPlaceholder(String report) 
	{
		final StringBuffer sb = new StringBuffer();

		executionInfo.getCategoriesFromCollection().forEach(s->sb.append(s).append(", "));

		String s = sb.toString();
		if (s.length() > 0) {
			report = report.replace("PLACEHOLDER_AVAILABLE_CATEGORIES", s.substring(0, s.length()-1));
		}

		return report;
	}
	
	private String replaceExecutedTestsPlaceholder(String report) 
	{
		if (reportMessagesOK.isEmpty()) {
			report = report.replace("PLACEHOLDER_GREEN_EXECUTED_TESTS", "");
		} else {
			report = report.replace("PLACEHOLDER_GREEN_EXECUTED_TESTS", createExecutedTestList(reportMessagesOK));
		}		
		
		if (reportMessagesWRONG.isEmpty()) {
			report = report.replace("PLACEHOLDER_ORANGE_EXECUTED_TESTS", "");
		} else {
			report = report.replace("PLACEHOLDER_ORANGE_EXECUTED_TESTS", createExecutedTestList(reportMessagesWRONG));
		}

		if (reportMessagesFAILED.isEmpty()) {
			report = report.replace("PLACEHOLDER_RED_EXECUTED_TESTS", "");
		} else {			
			report = report.replace("PLACEHOLDER_RED_EXECUTED_TESTS", createExecutedTestList(reportMessagesFAILED));
		}

		return report;
	}
	
	
	private CharSequence createExecutedTestList(HashMap<String, List<String>> messages) 
	{
		ExecutedTestData data = new ExecutedTestData(); 
		messages.keySet().forEach(s-> appendExecutedTestIds(data, s));		
		return data.sb.toString();
	}
	
	private class ExecutedTestData {
		StringBuffer sb;
		Integer counter;
		public ExecutedTestData() {
			this.sb = new StringBuffer();
			this.counter = 0;
		}
		
	}

	private void appendExecutedTestIds(ExecutedTestData data, String s) 
	{
		data.counter = data.counter + 1;
		
		if (data.counter == 1) {
			data.sb.append(s);
		} else {
			data.sb.append("&nbsp;&nbsp;&nbsp;&nbsp;#&nbsp;&nbsp;&nbsp;&nbsp;")
			       .append(s);
		} 
		
		if (data.counter == 3)
		{
			data.sb.append(System.getProperty("line.separator"))
			       .append("<br>")
			       .append(System.getProperty("line.separator"));			
			data.counter = 0;
		}
	}

	private String buildDetailPart() 
	{
		final StringBuffer sb = new StringBuffer();

		sb.append("<br>")
		  .append(System.getProperty("line.separator"))
		  .append(buildReportsAsHtml(reportMessagesFAILED, RED_HTML_COLOR))
		  .append(buildReportsAsHtml(reportMessagesWRONG, ORANGE_HTML_COLOR))
		  .append(buildReportsAsHtml(reportMessagesOK, GREEN_HTML_COLOR));
		
		return sb.toString();
	}

	private String buildReportsAsHtml(HashMap<String, List<String>> reportMessages, String color) 
	{
		final StringBuffer sb = new StringBuffer();
		
		for (String testId : reportMessages.keySet()) {
			sb.append( getHtmlReport(testId, reportMessages.get(testId), color) );
		}
		
		return sb.toString();
	}

	private String getHtmlReport(String testId, List<String> messages, String color) 
	{
		final StringBuffer sb = new StringBuffer();
		messageCounter = 0;
		testCounter++;
		
		sb.append("<b><span style='font-size:14.0pt;color:" + color + "'>" + testCounter + ". " + testId + ":</span></b>")
		  .append(System.getProperty("line.separator"))
		  .append("<br>")
		  .append(System.getProperty("line.separator"));

		for (String message : messages) {
			appendMessageLine(sb, message);
		}
		
		sb.append("<br>")
		  .append(System.getProperty("line.separator"))
		  .append("<hr/>")
		  .append(System.getProperty("line.separator"))
		  .append("<br>")
		  .append(System.getProperty("line.separator"));
		
		return sb.toString();
	}

	private void appendMessageLine(StringBuffer sb, String message) 
	{
		if (message.startsWith("//")) {
			appendCommentLine(sb, message);
			return;
		} 
		
		messageCounter++;
		message = testCounter + "." + messageCounter + " " + message;
		
		if (message.contains(YES_KEYWORD)) {
			sb.append(buildOkMessageLine(message));
		} else if (message.contains(NO_KEYWORD)) {
			sb.append(buildWrongMessageLine(message));
		} else if (message.contains(ERROR_KEYWORD)) {
			message = message + " " + TestCase.SMILEY_FAILED;
			sb.append("<span style='font-size:12.0pt;color:" + RED_HTML_COLOR + "'>" + message + "</span>");
		} else {
			sb.append("<span style='font-size:12.0pt'>" + message + "</span>");
		}
		
		sb.append(System.getProperty("line.separator"))
		  .append("<br>")
		  .append(System.getProperty("line.separator"));
	}

	private Object buildOkMessageLine(String message) 
	{
		if (message.contains(SysNatConstants.QUESTION_IDENTIFIER)) {
			message = message.replace(SysNatConstants.QUESTION_IDENTIFIER, "<SePaRaTor>");
			String[] splitResult = message.split("<SePaRaTor>"); 
			return "<span style='font-size:12.0pt'>" + splitResult[0] + SysNatConstants.QUESTION_IDENTIFIER + "</span>" + System.getProperty("line.separator") +
			       "<span style='font-size:12.0pt;color:" + GREEN_HTML_COLOR + "'>" + splitResult[1] + "</span>";
			
		} else {
			return "<span style='font-size:12.0pt;color:" + GREEN_HTML_COLOR + "'>" + message + "</span>";
		}
	}
	
	private Object buildWrongMessageLine(String message) 
	{
		if (message.contains(SysNatConstants.QUESTION_IDENTIFIER)) {
			message = message.replace(SysNatConstants.QUESTION_IDENTIFIER, "<SePaRaTor>");
			String[] splitResult = message.split("<SePaRaTor>"); 
			return "<span style='font-size:12.0pt'>" + splitResult[0] + SysNatConstants.QUESTION_IDENTIFIER + "</span>" + System.getProperty("line.separator") +
			       "<span style='font-size:12.0pt;color:" + ORANGE_HTML_COLOR + "'>" + splitResult[1] + "</span>";
			
		} else {
			return "<span style='font-size:12.0pt;color:" + ORANGE_HTML_COLOR + "'>" + message + "</span>";
		}
	}

	private void appendCommentLine(StringBuffer sb, String message) 
	{
		message = message.substring(2);
		if (isParagraphKeyword(message))  {
			sb.append("<br>").append(System.getProperty("line.separator"));
			sb.append("<i><span style='font-size:13.0pt;color:" + BLUE_HTML_COLOR + "'>" + message + "</span></i>");
		} else {
			sb.append("<i><span style='font-size:12.0pt;color:grey'>" + message + "</span></i>");
		}
		sb.append(System.getProperty("line.separator"))
		  .append("<br>")
		  .append(System.getProperty("line.separator"));
	}

	private String readReportTemplate() 
	{
		StringBuilder sb = new StringBuilder();
		BufferedReader buffReader = null;
		try {
			FileInputStream is = new FileInputStream(REPORT_TEMPLATE);
			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			buffReader = new BufferedReader(isr);
			String line = buffReader.readLine();
			while(line != null) {
			    sb.append(line);
			    line = buffReader.readLine();
			    sb.append(System.getProperty("line.separator"));
			}
		} catch (Exception e) {
            throw new RuntimeException("The following necessary file is missing: " + REPORT_TEMPLATE);
		} finally {
			try {
				if (buffReader != null) buffReader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
	
	private String replacePlaceholdersOverallResult(String report) 
	{
		if ( ! executionInfo.getLoginOK() ) {
			report = report.replace("PLACEHOLDER_RESULT", "Login nicht möglich." + System.getProperty("line.separator") + "Bitte TargetUrl prüfen.");
			report = report.replace("PLACEHOLDER_BACKGROUND_COLOR_OVERALL_RESULT", RED_HTML_COLOR);
		} else {

			if (reportMessagesWRONG.size() + reportMessagesFAILED.size() == 0) 
			{
				if (executionInfo.getNumberOfAllExecutedTestCases() == 0) {
					report = report.replace("PLACEHOLDER_RESULT", "-");
				} else {
					report = report.replace("PLACEHOLDER_RESULT", "OK");
				}
				if (executionInfo.getNumberOfInactiveTests() > 0) {
					report = report.replace("PLACEHOLDER_BACKGROUND_COLOR_OVERALL_RESULT", YELLOW_HTML_COLOR);
				} else if (executionInfo.getNumberOfAllExecutedTestCases() > 0) {
					report = report.replace("PLACEHOLDER_BACKGROUND_COLOR_OVERALL_RESULT", GREEN_HTML_COLOR);
				} else {
					report = report.replace("PLACEHOLDER_BACKGROUND_COLOR_OVERALL_RESULT", WHITE_HTML_COLOR);
				}
			} else if ( reportMessagesFAILED.size() == 0 ) {
				report = report.replace("PLACEHOLDER_RESULT", ASSERT_ERROR_TEXT);
				report = report.replace("PLACEHOLDER_BACKGROUND_COLOR_OVERALL_RESULT", ORANGE_HTML_COLOR);
			} else {
				report = report.replace("PLACEHOLDER_RESULT", TECHNICAL_ERROR_TEXT);
				report = report.replace("PLACEHOLDER_BACKGROUND_COLOR_OVERALL_RESULT", RED_HTML_COLOR);
			}
			
		}

		return report;
	}

	private String replaceTestStatiticsPlaceholder(String report) {
		report = report.replace("PLACEHOLDER_N1", "" + executionInfo.getTotalNumberOfTestCases());
		report = report.replace("PLACEHOLDER_N2", "" + executionInfo.getNumberOfAllExecutedTestCases());
		
		report = report.replace("PLACEHOLDER_N3", "" + executionInfo.getNumberOfInactiveTests());
		if (executionInfo.getNumberOfInactiveTests() == 0)  {
			report = report.replace("PLACEHOLDER_BACKGROUND_INAKTIV_NUMBER", WHITE_HTML_COLOR);
		} else {
			report = report.replace("PLACEHOLDER_BACKGROUND_INAKTIV_NUMBER", YELLOW_HTML_COLOR);
		}
		
		report = report.replace("PLACEHOLDER_N4", "" + reportMessagesOK.size());
		if (reportMessagesOK.size() == 0)  {
			report = report.replace("PLACEHOLDER_BACKGROUND_OK_NUMBER", WHITE_HTML_COLOR);
		} else {
			report = report.replace("PLACEHOLDER_BACKGROUND_OK_NUMBER", GREEN_HTML_COLOR);
		}
		
		report = report.replace("PLACEHOLDER_N5", "" + reportMessagesWRONG.size());
		if (reportMessagesWRONG.size() == 0)  {
			report = report.replace("PLACEHOLDER_BACKGROUND_WRONG_NUMBER", WHITE_HTML_COLOR);
		} else {
			report = report.replace("PLACEHOLDER_BACKGROUND_WRONG_NUMBER", ORANGE_HTML_COLOR);
		}
		
		report = report.replace("PLACEHOLDER_N6", "" + reportMessagesFAILED.size());
		if (reportMessagesFAILED.size() == 0)  {
			report = report.replace("PLACEHOLDER_BACKGROUND_FAILED_NUMBER", WHITE_HTML_COLOR);
		} else {
			report = report.replace("PLACEHOLDER_BACKGROUND_FAILED_NUMBER", RED_HTML_COLOR);
		}
		return report;
	}


	private boolean isParagraphKeyword(String word)  
	{
		for (String keyword : REPORT_PARAGRAPH_KEYWORDS) {
			if (keyword.equals(word))  {
				return true;
			}
		}
		return false;
	}
}
