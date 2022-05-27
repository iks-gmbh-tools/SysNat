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

import static com.iksgmbh.sysnat.common.utils.SysNatConstants.BLACK_HTML_COLOR;
import static com.iksgmbh.sysnat.common.utils.SysNatConstants.BLUE_HTML_COLOR;
import static com.iksgmbh.sysnat.common.utils.SysNatConstants.GREEN_HTML_COLOR;
import static com.iksgmbh.sysnat.common.utils.SysNatConstants.ORANGE_HTML_COLOR;
import static com.iksgmbh.sysnat.common.utils.SysNatConstants.RED_HTML_COLOR;
import static com.iksgmbh.sysnat.common.utils.SysNatConstants.WHITE_HTML_COLOR;
import static com.iksgmbh.sysnat.common.utils.SysNatConstants.YELLOW_HTML_COLOR;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.ASSERT_ERROR_TEXT;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.ERROR_KEYWORD;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.NO_KEYWORD;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.PROBLEM_KEYWORD;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.utils.SysNatTestRuntimeUtil;

public class ReportCreator 
{	
	private static final String HTML_LIST_SEPARATOR = "&nbsp;&nbsp;&nbsp;&nbsp;#&nbsp;&nbsp;&nbsp;&nbsp;";

	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/ReportCreator", Locale.getDefault());

	public static final String FULL_REPORT_RESULT_FILENAME = "FullOverview.html"; 
	public static final String SHORT_REPORT_RESULT_FILENAME = "ShortOverview.html"; 
	public static final String DETAIL_RESULT_FILENAME = "ExecutionDetails.html"; 
	public static final String END_OF_ONETIME_PRECONDTIONS_COMMENT = "-Onetime-Precondition-Marker-Comment-For-Report-Builder-";
	public static final String START_OF_ONETIME_CLEANUPS_COMMENT = "-Onetime-Cleanup-Marker-Comment-For-Report-Builder-";
	
	private static final String HTML_TEMPLATE_DIR = "../sysnat.test.runtime.environment/src/main/resources/htmlTemplates"; 
	private static final String OVERVIEW_TEMPLATE = HTML_TEMPLATE_DIR + "/OverviewReport.htm.Template." + Locale.getDefault().getLanguage() + ".txt";
	private static final String DETAIL_TEMPLATE = HTML_TEMPLATE_DIR + "/SingleReport.htm.Template." + Locale.getDefault().getLanguage() + ".txt";
	private static final String GROUP_COUNTER = "<GroupCounter>";
	
	private enum ReportStatus { OK, FAILED, WRONG };
	
	private LinkedHashMap<String, String> sortedXXidGroupMap;  
	private LinkedHashMap<String, List<String>> executedXXGroups;  // contains list of XX for each group (both feature and behaviour)
	private HashMap<String, List<String>> reportMessagesOK;
	private HashMap<String, List<String>> reportMessagesWRONG;
	private HashMap<String, List<String>> reportMessagesFAILED;
	private HashMap<String, String> groupReportStatus;  // contains for each group the overall status (e.g. failed if at least one XX of the group failed)
	private List<String> knownFeatures;  // contains XXGroup-IDs that are defined as Features

	private int xxCounter = 0;
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

	ReportCreator() 
	{
		executionInfo = ExecutionRuntimeInfo.getInstance();
		sortedXXidGroupMap = executionInfo.getSortedXXidBehaviourMap();
		knownFeatures = executionInfo.getKnownFeatures();
		
		reportMessagesOK = executionInfo.getReportMessagesOK();
		reportMessagesWRONG = executionInfo.getReportMessagesWRONG();
		reportMessagesFAILED = executionInfo.getReportMessagesFAILED();

		executedXXGroups = collectXXGroupsInExecutionOrder();
		groupReportStatus = collectGroupReportStatus();
	}

	private HashMap<String, String> collectGroupReportStatus() 
	{
		final HashMap<String, String> toReturn = new HashMap<>();
		executedXXGroups.keySet().stream()
		                .filter(groupID -> ! groupID.equals(ExecutionRuntimeInfo.UNNAMED_XX_GROUP))
		                .forEach(groupID -> toReturn.put(groupID, determineGroupReportStatus(groupID)));
		return toReturn;
	}

	private String determineGroupReportStatus(String groupID) 
	{
		long failedNumber = executedXXGroups.get(groupID).stream().filter(xxid -> reportMessagesFAILED.keySet().contains(xxid)).count();
		if (failedNumber > 0) {
			return ReportStatus.FAILED.name() + "(" + failedNumber + "/" + executedXXGroups.get(groupID).size() + ")";
		}

		long wrongNumber = executedXXGroups.get(groupID).stream().filter(xxid -> reportMessagesWRONG.keySet().contains(xxid)).count();
		if (wrongNumber > 0) {
			return ReportStatus.WRONG.name() + "(" + wrongNumber + "/" + executedXXGroups.get(groupID).size() + ")";
		}
		
		long okNumber = executedXXGroups.get(groupID).stream().filter(xxid -> reportMessagesOK.keySet().contains(xxid)).count();
		return ReportStatus.OK.name() + "(" + okNumber + "/" + executedXXGroups.get(groupID).size() + ")";
	}

	private LinkedHashMap<String, List<String>> collectXXGroupsInExecutionOrder() 
	{
		final LinkedHashMap<String, List<String>> toReturn = new LinkedHashMap<>();
		sortedXXidGroupMap.keySet().forEach(xxid -> collectGroupedXXIDs(toReturn, xxid));
		sortedXXidGroupMap.keySet().forEach(xxid -> collectStandAloneXXIDs(toReturn, xxid));
		return toReturn;
	}

	private void collectStandAloneXXIDs(HashMap<String, List<String>> toReturn, String xxid) 
	{
		String behaviourId = sortedXXidGroupMap.get(xxid);

		if (behaviourId == null) {
			behaviourId = ExecutionRuntimeInfo.UNNAMED_XX_GROUP;
		}

		if (behaviourId != ExecutionRuntimeInfo.UNNAMED_XX_GROUP) {
			return;
		}
		
		if (! toReturn.containsKey(behaviourId)) {
			toReturn.put(behaviourId, new ArrayList<>());
		}
		
		toReturn.get(behaviourId).add(xxid);
	}
	
	private void collectGroupedXXIDs(HashMap<String, List<String>> toReturn, String xxid) 
	{
		String behaviourId = sortedXXidGroupMap.get(xxid);
		
		if (behaviourId == null || behaviourId == ExecutionRuntimeInfo.UNNAMED_XX_GROUP) {
			return;
		}
		
		if (! toReturn.containsKey(behaviourId)) {
			toReturn.put(behaviourId, new ArrayList<>());
		}
		
		toReturn.get(behaviourId).add(xxid);
	}

	private String createFullOverview() 
	{
		String path = SysNatFileUtil.findAbsoluteFilePath(OVERVIEW_TEMPLATE);
		String overviewReport = readReportTemplate(path);
		overviewReport = createOverview(overviewReport);
		overviewReport = overviewReport.replace("PLACEHOLDER_DETAILS", buildDetailSection());
		return overviewReport;
	}

	private String createShortOverview() 
	{
		String path = SysNatFileUtil.findAbsoluteFilePath(OVERVIEW_TEMPLATE);
		String overviewReport = readReportTemplate(path);		overviewReport = createOverview(overviewReport);
		overviewReport = overviewReport.replace("PLACEHOLDER_DETAILS", BUNDLE.getString("seeDetails"));
		return overviewReport;
	}

	private String createDetailReport(final ExecutableExample executableExample) 
	{
		String path = SysNatFileUtil.findAbsoluteFilePath(DETAIL_TEMPLATE);
		String report = readReportTemplate(path);
		String xxid = executableExample.getXXID();
		String color = GREEN_HTML_COLOR;
		if (executionInfo.getReportMessagesFAILED().containsKey(xxid)) {
			color = ORANGE_HTML_COLOR;
		} else if (executionInfo.getReportMessagesWRONG().containsKey(xxid)) {
			color = RED_HTML_COLOR;
		}
		return report.replace("PLACEHOLDER_DETAILS", getReportDetailsForXX(xxid, true, executableExample.getReportMessages(), color, false));
	}


	private String createOverview(String report) 
	{
		String envDisplayName =  ExecutionRuntimeInfo.getInstance().getEnvironmentsMap().get(executionInfo.getTestEnvironmentName());
		if (envDisplayName == null) envDisplayName = "?";
		report = report.replace("PLACEHOLDER_PRODUCT", executionInfo.getTestApplicationName());
		report = report.replace("PLACEHOLDER_TARGET_ENV", envDisplayName);
		report = report.replace("PLACEHOLDER_TIME", executionInfo.getStartPointOfTimeAsString());
		report = report.replace("PLACEHOLDER_DURATION", executionInfo.getExecutionDurationAsString());
		report = report.replace("PLACEHOLDER_EXECUTION_FILTER", executionInfo.getTestExecutionFilter());
		
		report = replacePlaceholdersOverallResult(report);
		report = replaceTestStatiticsPlaceholder(report);
		report = replaceFoundFilterPlaceholder(report);
		report = replaceExecutedPlaceholders(report);
		
		String inactiveXXidList = buildHtmlList( executionInfo.getInactiveXXIDs() );
		report = report.replace("PLACEHOLDER_INACTIVE_TEST_CASES", inactiveXXidList);
		
		return report;
	}

	private String buildHtmlList(List<String> idList) 
	{
		if (idList.size() == 0) {
			return "-";
		}
		
		final List<String> inactiveList = createInactiveList(idList);
		final StringBuffer sb = new StringBuffer();
		int counter = 0;
		for (String xxid : inactiveList) 
		{
			if (counter > 0) {
				sb.append(HTML_LIST_SEPARATOR);
			}
			counter++;
			sb.append(xxid);
			
			if (counter == 3)
			{
				sb.append(System.getProperty("line.separator"))
				  .append("<br>")
				  .append(System.getProperty("line.separator"));			
				counter = 0;
			}
		}
		
		return sb.toString();
	}

	private List<String> createInactiveList(List<String> inactiveXXIDs) 
	{
		List<String> inactiveXXGroups = inactiveXXIDs.stream()
				                                     .map(xxid -> sortedXXidGroupMap.get(xxid))
				                                     .filter(behaviour -> behaviour != null)
				                                     .filter(behaviour -> ! ExecutionRuntimeInfo.UNNAMED_XX_GROUP.equals(behaviour))
				                                     .distinct()
                                                     .collect(Collectors.toList());
		
		List<String> standaloneXX = inactiveXXIDs.stream()
                                                 .filter(xxid -> sortedXXidGroupMap.get(xxid) == null 
                                                                 || ExecutionRuntimeInfo.UNNAMED_XX_GROUP.equals( sortedXXidGroupMap.get(xxid) ))
                                                 .collect(Collectors.toList());
	

		final List<String> inactiveList = new ArrayList<>();
		inactiveList.addAll(inactiveXXGroups);
		inactiveList.addAll(standaloneXX);
		Collections.sort(inactiveList);
		
		return inactiveList;
	}

	private String replaceFoundFilterPlaceholder(String report) 
	{
		final StringBuffer sb = new StringBuffer();

		executionInfo.getExecutionFilterFromMap().forEach(s->sb.append(s).append(", "));

		String s = sb.toString().trim();
		if (s.length() > 0) 
		{			
			s = s.substring(0, s.length()-1);
			if (s.startsWith(",")) {
				s = s.substring(1).trim();
			}
			report = report.replace("PLACEHOLDER_AVAILABLE_EXECUTION_FILTER", s);
		}
		
		return report;
	}
	
	String replaceExecutedPlaceholders(String report) 
	{
		report = replaceExecPlaceholder(report, ReportStatus.FAILED, "PLACEHOLDER_RED_EXECUTED_TESTS");
		report = replaceExecPlaceholder(report, ReportStatus.WRONG, "PLACEHOLDER_ORANGE_EXECUTED_TESTS");
		report = replaceExecPlaceholder(report, ReportStatus.OK, "PLACEHOLDER_GREEN_EXECUTED_TESTS");

		return report;
	}

	private String replaceExecPlaceholder(String report, ReportStatus reportStatus, String placeholder) 
	{
		final List<String> idList = getGroupListWithStatus(reportStatus);
		idList.addAll( getStandAloneXXsWithStatus(reportStatus) );
		Collections.sort(idList);
		
		if (idList.isEmpty()) {
			report = report.replace(placeholder, "");
		} else {			
			report = report.replace(placeholder, buildHtmlList(idList));
		}
		return report;
	}
	
	private List<String> getStandAloneXXsWithStatus(ReportStatus reportStatus) 
	{
		final Set<String> xxids;
		
		if (reportStatus == ReportStatus.FAILED) {
			xxids = reportMessagesFAILED.keySet();
		} else if (reportStatus == ReportStatus.WRONG) {
			xxids = reportMessagesWRONG.keySet();
		} else {
			xxids = reportMessagesOK.keySet();
		}
		
		if (executedXXGroups.get(ExecutionRuntimeInfo.UNNAMED_XX_GROUP) == null) {
			return new ArrayList<>();
		}
		
		return executedXXGroups.get(ExecutionRuntimeInfo.UNNAMED_XX_GROUP)
				               .stream()
					           .filter(xxid -> xxids.contains(xxid))
					           .collect(Collectors.toList());
	}

	private List<String> getGroupListWithStatus(ReportStatus reportStatus) 
	{
		return groupReportStatus.keySet().stream()
				                .filter(groupID -> groupReportStatus.get(groupID).startsWith(reportStatus.name()))
				                .map(groupID -> getDisplayText(groupID) )
				                .collect(Collectors.toList());
	}

	private String getDisplayText(String groupID) 
	{
		String status = groupReportStatus.get(groupID);
		int pos = status.indexOf("(");
		return groupID + " " + status.substring(pos);
	}

	String buildDetailSection() 
	{
		final Map<String, String> reportDetails = new HashMap<String, String>();;

		buildReportDetailsForXXGroups(reportDetails);
		buildReportDetailsForStandAloneXX(reportDetails);
		
		return buildOrderedReportDetailSection(reportDetails);
	}
	
	private void buildReportDetailsForStandAloneXX(Map<String, String> reportDetails)
	{
		final StringBuffer sb = new StringBuffer();
		addHtmlHorizontalLine(sb);
		sb.append("<br>").append(System.getProperty("line.separator"));

		List<String> standaloneXXs = executedXXGroups.get(ExecutionRuntimeInfo.UNNAMED_XX_GROUP);
		
		if (standaloneXXs == null || standaloneXXs.isEmpty()) {
			return;
		}
		
		
		sb.append("<b><span style='font-size:16.0pt;color:" 
		           + BLACK_HTML_COLOR + "'>" 
				   + "Standalone Executable Examples</span></b>");
		sb.append(System.getProperty("line.separator"));
		sb.append("<br><br>").append(System.getProperty("line.separator"));
		
		
		xxCounter = 0;
		
		for (String xxid : standaloneXXs) 
		{
			if (executionInfo.getInactiveXXIDs().contains(xxid)) {
				continue;
			} else if (reportMessagesFAILED.containsKey(xxid)) {
				buildReportDetailsForStandAloneXX(xxid, reportMessagesFAILED.get(xxid), RED_HTML_COLOR, sb);
			} else if (reportMessagesWRONG.containsKey(xxid)) {
				buildReportDetailsForStandAloneXX(xxid, reportMessagesWRONG.get(xxid), ORANGE_HTML_COLOR, sb);
			} else {
				buildReportDetailsForStandAloneXX(xxid, reportMessagesOK.get(xxid), GREEN_HTML_COLOR, sb);
			}
		}
		
		reportDetails.put(ExecutionRuntimeInfo.UNNAMED_XX_GROUP, sb.toString());		
	}
	
	private String buildOrderedReportDetailSection(Map<String, String> reportDetails) 
	{
		final StringBuffer sb = new StringBuffer();
		Set<String> keySet = executedXXGroups.keySet();
		int testCaseCounter = 0;
		
		for (String groupId : keySet) 
		{
			testCaseCounter++;
			String detailReport = reportDetails.get(groupId).replaceAll(GROUP_COUNTER, "" + testCaseCounter);
			sb.append(detailReport);
		}
		
		return sb.toString();
	}
	

	private void buildReportDetailsForXXGroups(Map<String, String> reportDetails) 
	{
		final List<String> groupsIds = executedXXGroups.keySet().stream()
                                                       .filter(groupID -> ! groupID.equals(ExecutionRuntimeInfo.UNNAMED_XX_GROUP))
				                                       .collect(Collectors.toList());
		
		boolean standalone = false;
		boolean firstGroup = true;
		for (String groupID : groupsIds) 
		{
			final StringBuffer sb = new StringBuffer();
			
			if (firstGroup) {
				firstGroup = false;
			} else {
				addHtmlHorizontalLine(sb);
			}
			sb.append("<br>").append(System.getProperty("line.separator"));
			
			sb.append("<b><span style='font-size:16.0pt;color:" 
			           + getGroupStatusColor(groupID) + "'>" 
					   + GROUP_COUNTER + ". " + getGroupType(groupID) 
					   + ": " + groupID + "</span></b>");

			sb.append("<br>")
			  .append(System.getProperty("line.separator"))
			  .append("<br>")
			  .append(System.getProperty("line.separator"));

			final List<String> xxidsOfGroup = executedXXGroups.get(groupID);
			Collections.sort(xxidsOfGroup);
			final List<String> onetimePreconditionInstructions = extractOnetimePreconditionInstructions(xxidsOfGroup);
			final List<String> onetimeCleanupInstructions = extractOnetimeCleanupInstructions(xxidsOfGroup);
			
			if (onetimePreconditionInstructions.size() > 0) {
				addOnetimeBehaviourLevelInstructions(sb, onetimePreconditionInstructions);
				addHtmlXXGroupSeparationLine(sb);
			}
			
			xxCounter = 0;
			for (String xxid : xxidsOfGroup) 
			{
				xxCounter++;
				
				if (xxCounter > 1) {
					addHtmlXXSeparationLine(sb);
				}
				
				if (reportMessagesOK.containsKey(xxid)) {
					sb.append( getReportDetailsForXX(xxid,
							                         standalone,
							                         reportMessagesOK.get(xxid), 
							                         GREEN_HTML_COLOR, 
							                         true));
				} else if (reportMessagesWRONG.containsKey(xxid)) {
					sb.append( getReportDetailsForXX(xxid,  
							                         standalone,
							                         reportMessagesWRONG.get(xxid), 
							                         ORANGE_HTML_COLOR, 
							                         true));					
				} else if (reportMessagesFAILED.containsKey(xxid)) {
					sb.append( getReportDetailsForXX(xxid,
							                         standalone,
							                         reportMessagesFAILED.get(xxid), 
							                         RED_HTML_COLOR, 
							                         true));					
				}
			}

			if (onetimePreconditionInstructions.size() > 0) {
				addHtmlXXGroupSeparationLine(sb);
				addOnetimeBehaviourLevelInstructions(sb, onetimeCleanupInstructions);
			}
			
			sb.append("<br>")
			  .append(System.getProperty("line.separator"));
			reportDetails.put(groupID, sb.toString());
		}
	}

	private void addOnetimeBehaviourLevelInstructions(StringBuffer sb, List<String> onetimeCleanupInstructions) 
	{
		char numberingChar = 'a' - 1;
		for (String message : onetimeCleanupInstructions) {
			numberingChar++;
			sb.append("<span style='font-size:12.0pt'>" + numberingChar + ") " + message + "</span>");
			sb.append(System.getProperty("line.separator"))
			  .append("<br>")
			  .append(System.getProperty("line.separator"));
		}
	}

	private List<String> extractOnetimeCleanupInstructions(List<String> xxidsOfGroup) 
	{
		List<String> toReturn = new ArrayList<>();
		xxidsOfGroup.forEach(xxid -> addOnetimeCleanups(reportMessagesOK.get(xxid), toReturn));
		return toReturn;
	}

	private void addOnetimeCleanups(List<String> list, List<String> toReturn) 
	{
		if (list == null) return;
		int index = getIndexInList(START_OF_ONETIME_CLEANUPS_COMMENT, list);
		if (index > -1) {
			for (int i = index+1; i < list.size(); i++) {
				toReturn.add(list.get(i));
			}
			for (int i = 0; i < index; i++) {
				list.remove(list.get(index));
			}
		}
		
	}

	private List<String> extractOnetimePreconditionInstructions(List<String> xxidsOfGroup) 
	{
		List<String> toReturn = new ArrayList<>();
		xxidsOfGroup.forEach(xxid -> addOnetimePreconditions(reportMessagesOK.get(xxid), toReturn));
		return toReturn;
	}

	private void addOnetimePreconditions(List<String> list, List<String> toReturn) 
	{
		if (list == null) return;
		int index = getIndexInList(END_OF_ONETIME_PRECONDTIONS_COMMENT, list);
		if (index > -1) {
			for (int i = 0; i < index; i++) {
				toReturn.add(list.get(i));
			}
			for (int i = index; i >= 0; i--) {
				list.remove(list.get(i));
			}
		}
		
	}

	private int getIndexInList(String searchString, List<String> list) 
	{
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).equals(searchString)) {
				return i;
			}
		}
		
		return -1;
	}

	private String getGroupType(String groupID) 
	{
		if (knownFeatures.contains(groupID)) {
			return "Feature";
		}
		return "Behaviour";
	}

	private String getGroupStatusColor(String groupID) 
	{
		String color = GREEN_HTML_COLOR;
		if (groupReportStatus.get(groupID).startsWith(ReportStatus.WRONG.name())) {
			color = ORANGE_HTML_COLOR;
		} else if (groupReportStatus.get(groupID).startsWith(ReportStatus.FAILED.name())) {
			color = RED_HTML_COLOR;
		}
		return color;
	}

	private void buildReportDetailsForStandAloneXX(final String xxid,
			                                       final List<String> reportMessages, 
			                                       final String color,
			                                       final StringBuffer sb) 
	{
		xxCounter++;
		
		if (xxCounter > 1) {
			addHtmlXXGroupSeparationLine(sb);
		}
		
		if (sortedXXidGroupMap.get(xxid).equals(ExecutionRuntimeInfo.UNNAMED_XX_GROUP)) {				
			sb.append( getReportDetailsForXX(xxid, true, reportMessages, color, true) );
		}
	}

	private String getReportDetailsForXX(final String xxid,
			                             final boolean standalone,
			                             final List<String> messages, 
			                             final String color,
			                             final boolean withTestCounter) 
	{
		final StringBuffer sb = new StringBuffer();
		String htmlTab = "";
		messageCounter = 0;
		
		String counterText = "";
		if (withTestCounter) {
			if (standalone) {
				counterText = xxCounter + ". ";
			} else {
				htmlTab = "&nbsp;&nbsp;&nbsp;&nbsp;";
				
				if ( knownFeatures.contains( sortedXXidGroupMap.get(xxid) ) ) {
					counterText = GROUP_COUNTER + "." + xxCounter + " Scenario: ";
				} else {					
					counterText = GROUP_COUNTER + "." + xxCounter + " XXID: ";
				}
			}
		}
		
		sb.append(htmlTab + "<b><span style='font-size:14.0pt;color:" + color + "'>" + counterText + xxid + ":</span></b>")
		  .append(System.getProperty("line.separator"))
		  .append("<br>")
		  .append(System.getProperty("line.separator"));

		for (String message : messages) {
			appendDetailMessageLine(sb, message, htmlTab, standalone);
		}
		
		return sb.toString();
	}
	
	private void addHtmlHorizontalLine(final StringBuffer sb) {
		sb.append("<hr/>").append(System.getProperty("line.separator"));
	}
	
	private void addHtmlDashedHorizontalLine(final StringBuffer sb) {
		sb.append("<hr style=\"border-style: dashed\">");
	}
	
	private void addHtmlXXSeparationLine(final StringBuffer sb) 
	{
		sb.append("<br>")
		  .append(System.getProperty("line.separator"));
		  
		addHtmlDashedHorizontalLine(sb);  
		  
		sb.append(System.getProperty("line.separator"))
		  .append("<br>")
		  .append(System.getProperty("line.separator"));
	}

	private void addHtmlXXGroupSeparationLine(final StringBuffer sb) 
	{
		sb.append("<br>")
		  .append(System.getProperty("line.separator"));
		  
		addHtmlHorizontalLine(sb);  
		  
		sb.append("<br>")
		  .append(System.getProperty("line.separator"));
	}

	private void appendDetailMessageLine(StringBuffer sb, String message, String htmlTab, boolean standalone) 
	{
		if (message.startsWith("//")) {
			appendCommentLine(sb, message);
			return;
		} 
		
		messageCounter++;
		
		if (standalone) {
			message = xxCounter + "." + messageCounter + " " + message;
		} else {
			message = GROUP_COUNTER + "." + xxCounter + "." + messageCounter + " " + message;
		}
		
		if (message.contains(YES_KEYWORD)) {
			sb.append(htmlTab + buildOkMessageLine(message));
		} else if (message.contains(NO_KEYWORD)) {
			sb.append(htmlTab + buildWrongMessageLine(message));
		} else if (message.contains(PROBLEM_KEYWORD + ":")) {
			sb.append(htmlTab + buildWrongMessageLine(message));
		} else if (message.contains(ERROR_KEYWORD) && ! message.contains("<b>" + ERROR_KEYWORD + "</b>")) {
			message = message + " " + ExecutableExample.SMILEY_FAILED;
			sb.append("<span style='font-size:12.0pt;color:" + RED_HTML_COLOR + "'>" + htmlTab +  message + "</span>");
		} else {
			sb.append("<span style='font-size:12.0pt'>" + htmlTab + message + "</span>");
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
		if (SysNatTestRuntimeUtil.isTestPhaseKeyword(message))  {
			sb.append("<br>").append(System.getProperty("line.separator"));
			sb.append("<i><span style='font-size:13.0pt;color:" + BLUE_HTML_COLOR + "'>" + message + "</span></i>");
		} else {
			if (message.startsWith("Doc1: ") || message.startsWith("Doc2: ")) message = reformatToFileHyperLink(message);
			sb.append("<i><span style='font-size:12.0pt;color:grey'>" + message + "</span></i>");
		}
		sb.append(System.getProperty("line.separator"))
		  .append("<br>")
		  .append(System.getProperty("line.separator"));
	}

	private String reformatToFileHyperLink(String message)
	{
		int pos1 = message.indexOf(" ");
		int pos2 = message.indexOf(" (Number of pages:");
		
		String filePath = message.substring(pos1, pos2).trim();
		String filename = new File(filePath).getName();
		
		String href = "<a href=\"file:///" + filePath + "\">" + filename + "</a>";
		
		String trailer = "";
		if (pos2 > -1) {
			trailer = message.substring(pos2);
		}
		return message.substring(0, pos1+1) + href + trailer;
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
				if (executionInfo.getNumberOfAllExecutedXXs() == 0) {
					report = report.replace("PLACEHOLDER_RESULT", "-");
				} else {
					report = report.replace("PLACEHOLDER_RESULT", "OK");
				}
				if (executionInfo.getNumberOfInactiveTests() > 0) {
					report = report.replace("PLACEHOLDER_BACKGROUND_COLOR_OVERALL_RESULT", YELLOW_HTML_COLOR);
				} else if (executionInfo.getNumberOfAllExecutedXXs() > 0) {
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
		report = report.replace("PLACEHOLDER_N1", "" + executionInfo.getTotalNumberOfXXs());
		report = report.replace("PLACEHOLDER_N2", "" + executionInfo.getNumberOfAllExecutedXXs());
		
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