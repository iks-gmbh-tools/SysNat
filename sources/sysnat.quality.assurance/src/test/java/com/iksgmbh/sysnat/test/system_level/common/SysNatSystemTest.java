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
package com.iksgmbh.sysnat.test.system_level.common;

import static org.junit.Assert.assertTrue;

import java.io.File;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.GenerationRuntimeInfo;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.helper.ReportCreator;

public class SysNatSystemTest 
{
	protected File reportDir;
	protected String settingsConfigToUseInSystemTest = null;
	protected File executionDir = createFile("../sysnat.test.execution/src/test");

	protected static void sleep(int millis) 
	{
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	
	protected void setup()
	{
		ExecutionRuntimeInfo.reset();
		GenerationRuntimeInfo.reset();
		if (settingsConfigToUseInSystemTest != null) {
			setSettingsConfig(settingsConfigToUseInSystemTest);
		}
		
		ExecutionRuntimeInfo.getInstance();
		GenerationRuntimeInfo.getInstance();
		reportDir = createFile(System.getProperty("sysnat.report.dir"));
		SysNatFileUtil.deleteFolder(reportDir);
		reportDir.mkdir();
		SysNatFileUtil.deleteFolder(executionDir);
		System.setProperty("sysnat.autolaunch.report", "false");
	}
	
	private void setSettingsConfig(String value) {
		System.setProperty("settings.config", value);
		ExecutionRuntimeInfo.setSysNatSystemProperty("settings.config", value);
	}
	

	protected File getFullOverviewOfCurrentReport() 
	{
		final File[] filelist = reportDir.listFiles();
		assertTrue("More than one reports exist in report dir!", filelist.length == 1);
		return new File(filelist[0], ReportCreator.FULL_REPORT_RESULT_FILENAME);
	}
	
	
	protected String getHtmlReportSnippet(int expectedNumberSuccessfullyExecutedTests,
			                              String color) {
		return "background:" + color + "'>" + expectedNumberSuccessfullyExecutedTests+ "</span";
	}

	protected static File createFile(String path) {
		path = SysNatFileUtil.findAbsoluteFilePath(path);
		return new File(path);
	}
}