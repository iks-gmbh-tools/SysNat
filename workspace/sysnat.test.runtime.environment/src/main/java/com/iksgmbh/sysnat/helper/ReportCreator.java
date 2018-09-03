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

import static com.iksgmbh.sysnat.common.utils.SysNatConstants.BLUE_HTML_COLOR;
import static com.iksgmbh.sysnat.common.utils.SysNatConstants.GREEN_HTML_COLOR;
import static com.iksgmbh.sysnat.common.utils.SysNatConstants.ORANGE_HTML_COLOR;
import static com.iksgmbh.sysnat.common.utils.SysNatConstants.RED_HTML_COLOR;
import static com.iksgmbh.sysnat.common.utils.SysNatConstants.WHITE_HTML_COLOR;
import static com.iksgmbh.sysnat.common.utils.SysNatConstants.YELLOW_HTML_COLOR;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.ACT_KEYWORD;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.ARRANGE_KEYWORD;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.ASSERT_ERROR_TEXT;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.ASSERT_KEYWORD;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.CLEANUP_KEYWORD;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.ERROR_KEYWORD;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.NO_KEYWORD;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.TECHNICAL_ERROR_TEXT;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.YES_KEYWORD;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;

public class ReportCreator 
{	
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/ReportCreator", Locale.getDefault());

	public static final String FULL_REPORT_RESULT_FILENAME = "FullOverview.html"; 
	public static final String SHORT_REPORT_RESULT_FILENAME = "ShortOverview.html"; 
	public static final String DETAIL_RESULT_FILENAME = "ExecutionDetails.html"; 
	
	private static final String OVERVIEW_TEMPLATE = "../sysnat.test.runtime.environment/src/main/resources/TestReport.htm.Template." 
                                                     + Locale.getDefault().getLanguage() + ".txt";
	private static final String DETAIL_TEMPLATE = "../sysnat.test.runtime.environment/src/main/resources/TestReport.htm.Template." 
                                                   + Locale.getDefault().getLanguage() + ".txt";
	private static final String[] REPORT_PARAGRAPH_KEYWORDS =  { ARRANGE_KEYWORD, ACT_KEYWORD, ASSERT_KEYWORD, CLEANUP_KEYWORD};
	
	private HashMap<String, List<String>> reportMessagesOK;
	private HashMap<String, List<String>> reportMessagesWRONG;
	private HashMap<String, List<String>> reportMessagesFAILED;

	private int testCounter = 0;
	private int messageCounter = 0;
	private ExecutionRuntimeInfo executionInfo;

	public static String createFullOverviewReport() {
		return new ReportCreator().createFullOverview();
	}
	
	public static String createShortOverviewReport() {
		return new ReportCreator().createShortOverview();
	}
	
	public static String createSingleTestReport(final ExecutableExample testcase) {
		return new ReportCreator().createDetailReport(testcase);
	}

	private ReportCreator() 
	{
		executionInfo = ExecutionRuntimeInfo.getInstance();
		reportMessagesOK = executionInfo.getReportMessagesOK();
		reportMessagesWRONG = executionInfo.getReportMessagesWRONG();
		reportMessagesFAILED = executionInfo.getReportMessagesFAILED();
	}

	private String createFullOverview() 
	{
		String overviewReport = readReportTemplate(OVERVIEW_TEMPLATE);
		overviewReport = createOverview(overviewReport);
		overviewReport = overviewReport.replace("PLACEHOLDER_DETAILS", buildDetailPart());
		return overviewReport;
	}

	private String createShortOverview() 
	{
		String overviewReport = readReportTemplate(OVERVIEW_TEMPLATE);
		overviewReport = createOverview(overviewReport);
		overviewReport = overviewReport.replace("PLACEHOLDER_DETAILS", BUNDLE.getString("seeDetails"));
		return overviewReport;
	}

	private String createDetailReport(final ExecutableExample executableExample) 
	{
		String report = readReportTemplate(DETAIL_TEMPLATE);
		String xxid = executableExample.getXXID();
		String color = GREEN_HTML_COLOR;
		if (executionInfo.getReportMessagesFAILED().containsKey(xxid)) {
			color = ORANGE_HTML_COLOR;
		} else if (executionInfo.getReportMessagesWRONG().containsKey(xxid)) {
			color = RED_HTML_COLOR;
		}
		return report.replace("PLACEHOLDER_DETAILS", getReportDetailsForSingleTestcase(xxid, executableExample.getReportMessages(), color, false));
	}


	private String createOverview(String report) 
	{
		report = report.replace("PLACEHOLDER_PRODUCT", executionInfo.getTestApplicationName());
		report = report.replace("PLACEHOLDER_TARGET_ENV", executionInfo.getTargetEnv().name());
		report = report.replace("PLACEHOLDER_TIME", executionInfo.getStartPointOfTimeAsString());
		report = report.replace("PLACEHOLDER_DURATION", executionInfo.getExecutionDurationAsString());
		report = report.replace("PLACEHOLDER_CATEGORIES", executionInfo.getTestCategories());
		
		report = replacePlaceholdersOverallResult(report);
		report = replaceTestStatiticsPlaceholder(report);
		report = replaceFoundCategoriesPlaceholder(report);
		report = replaceExecutedTestsPlaceholder(report);
		
		report = report.replace("PLACEHOLDER_INACTIVE_TEST_CASES", executionInfo.getInactiveTestListAsString());
		
		return report;
	}

	private String replaceFoundCategoriesPlaceholder(String report) 
	{
		final StringBuffer sb = new StringBuffer();

		executionInfo.getCategoriesFromCollection().forEach(s->sb.append(s).append(", "));

		String s = sb.toString().trim();
		if (s.length() > 0) 
		{			
			s = s.substring(0, s.length()-1);
			if (s.startsWith(",")) {
				s = s.substring(1).trim();
			}
			report = report.replace("PLACEHOLDER_AVAILABLE_CATEGORIES", s);
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
		messages.keySet().forEach(s-> appendExecutedXXIds(data, s));		
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

	private void appendExecutedXXIds(ExecutedTestData data, String s) 
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
		  .append(buildReportDetailsForTestCaseGroup(reportMessagesFAILED, RED_HTML_COLOR))
		  .append(buildReportDetailsForTestCaseGroup(reportMessagesWRONG, ORANGE_HTML_COLOR))
		  .append(buildReportDetailsForTestCaseGroup(reportMessagesOK, GREEN_HTML_COLOR));
		
		return sb.toString();
	}

	private String buildReportDetailsForTestCaseGroup(final HashMap<String, List<String>> reportMessages, 
			                          final String color) 
	{
		final StringBuffer sb = new StringBuffer();
		
		final ArrayList<String> keys = new ArrayList<>(reportMessages.keySet());
		Collections.sort(keys);
		
		for (String xxid : keys) {
			sb.append( getReportDetailsForSingleTestcase(xxid, reportMessages.get(xxid), color, true) );
		}
		
		return sb.toString();
	}

	private String getReportDetailsForSingleTestcase(final String xxid, 
			                                         final List<String> messages, 
			                                         final String color,
			                                         final boolean withTestCounter) 
	{
		final StringBuffer sb = new StringBuffer();
		messageCounter = 0;
		testCounter++;
		
		String counterText = "";
		if (withTestCounter) {
			counterText = testCounter + ". ";
		}
		
		sb.append("<b><span style='font-size:14.0pt;color:" + color + "'>" + counterText + xxid + ":</span></b>")
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
			message = message + " " + ExecutableExample.SMILEY_FAILED;
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

	private String readReportTemplate(final String template) 
	{
		StringBuilder sb = new StringBuilder();
		BufferedReader buffReader = null;
		try {
			FileInputStream is = new FileInputStream(template);
			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			buffReader = new BufferedReader(isr);
			String line = buffReader.readLine();
			while(line != null) {
			    sb.append(line);
			    line = buffReader.readLine();
			    sb.append(System.getProperty("line.separator"));
			}
		} catch (FileNotFoundException e) {
            throw new RuntimeException("The following necessary file is missing: " 
		                               + new File(template).getAbsolutePath());
		} catch (Exception e) {
            throw new RuntimeException("Error reading File: " 
                                       + new File(template).getAbsolutePath());
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

	private String replaceTestStatiticsPlaceholder(String report) 
	{
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

	public static String getFullOverviewReportFilename() {
		return ExecutionRuntimeInfo.getInstance().getReportFolder().getAbsolutePath()
			   + "/" + FULL_REPORT_RESULT_FILENAME;
	}

	public static String getShortOverviewReportFilename() {
		return ExecutionRuntimeInfo.getInstance().getReportFolder().getAbsolutePath()
			   + "/" + SHORT_REPORT_RESULT_FILENAME;
	}

	public static String buildDetailReportFilename(final String name) {
		return ExecutionRuntimeInfo.getInstance().getReportFolder().getAbsolutePath()
			   + "/" + name + "/" + DETAIL_RESULT_FILENAME;
	}
	
}