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
	protected File executionDir = new File("../sysnat.test.execution/src/test");

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
		reportDir = new File(System.getProperty("sysnat.report.dir"));
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


}
