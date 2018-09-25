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
package com.iksgmbh.sysnat.testcasejavatemplate;

import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.ERROR_KEYWORD;

import java.io.File;

import org.joda.time.DateTime;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.ExecutionRuntimeInfo.TestStatistics;
import com.iksgmbh.sysnat.common.exception.SkipTestCaseException;
import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.common.exception.UnexpectedResultException;
import com.iksgmbh.sysnat.common.exception.UnsupportedGuiEventException;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.helper.ReportCreator;
import com.iksgmbh.sysnat.utils.SysNatTestRuntimeUtil;

/**
 * This class represents the parent class for all TestCaseTemplates.
 * Each TestApplication needs its own TestCaseTemplate derived by this parent class
 * which contains all code shared by all TestCaseTemplates.
 * 
 * @author Reik Oberrath
 */
public abstract class TestCaseTemplateParent extends ExecutableExample
{
	public void setUp() 
	{
		super.setUp();
		System.out.println(SysNatConstants.SYS_OUT_SEPARATOR);
	}

	public void shutdown() 
	{
		if ( ! isSkipped() ) 
		{
			System.out.println("Done with test case in " + getExecDuration() + ".");
			
			final TestStatistics testStatistics = new ExecutionRuntimeInfo.TestStatistics(startTime, new DateTime());
			executionInfo.addTestStatistics(getXXID(), testStatistics );
			
			final String reportName = executionInfo.buildDefaultReportName();
			final String detailReportFilename = ReportCreator.buildDetailReportFilename(reportName);
			final File detailReportFile = new File(detailReportFilename);
			detailReportFile.getParentFile().mkdirs();
			SysNatFileUtil.writeFile(detailReportFile, ReportCreator.createSingleTestReport(this));
			String totalTimePassed = executionInfo.getTotalTimePassed();

			System.out.println("Time passed: " + testStatistics.duration + " (this test), " + totalTimePassed + " (total).");
		} 
		else 
		{
			System.out.println(ExecutionRuntimeInfo.getInstance().getTotalNumberOfTestCases() + " test cases executed so far.");
		}
		
		setXXID(null);
		System.out.println("Intermediate Result: " + executionInfo.getIntermediateResultLogText() + ".");
	}
	
	protected void handleThrowable(Throwable t)  
	{
		try {
			throw t;
		} catch (SkipTestCaseException e) {
			finishSkippedTestCase(e.getSkipReason());
		} catch (UnexpectedResultException e) {
			takeScreenshot(SysNatTestRuntimeUtil.getScreenshotFailureFileName(this.getClass().getSimpleName()));
			terminateWrongTestCase();
		} catch (SysNatTestDataException e) {
			takeScreenshot(SysNatTestRuntimeUtil.getScreenshotFailureFileName(this.getClass().getSimpleName()));
			failWithMessage(e.getMessage());
		} catch ( UnsupportedGuiEventException e) {
			e.printStackTrace();		
			takeScreenshot(SysNatTestRuntimeUtil.getScreenshotErrorFileName(this.getClass().getSimpleName()));
			failWithMessage(ERROR_KEYWORD 
					        + ": Folgendes grafisches Element ist nicht verfügbar:" 
		                    + extractSelectorValue(e.getMessage())); 
		} catch (SysNatException e) {
			takeScreenshot(SysNatTestRuntimeUtil.getScreenshotErrorFileName(this.getClass().getSimpleName()));
			failWithMessage(e.getMessage());
		} catch (Throwable e) {
			e.printStackTrace();
			takeScreenshot(SysNatTestRuntimeUtil.getScreenshotErrorFileName(this.getClass().getSimpleName()));
			failWithMessage("Unerwarteter " + ERROR_KEYWORD + ": " 
		                    + e.getClass().getSimpleName() + ": " 
					        + System.getProperty("line.separator")
					        + e.getMessage());
		}		
	}

	@Override
	public String getTestCaseFileName() {
		String toReturn = this.getClass().getSimpleName();
		if (toReturn.endsWith("Test")) {
			toReturn = toReturn.substring(0, toReturn.length()-4);
		}
		return toReturn;
	}	
	
	@Override
	public Package getTestCasePackage() {
		return this.getClass().getPackage();
	}
	
	@Override
	public boolean doesTestBelongToApplicationUnderTest() {
		return SysNatTestRuntimeUtil.doesTestBelongToApplicationUnderTest(this);
	}

	protected void setXXIdForInactiveTests(String xxid) {
		setXXID("InactiveTestExample");
		executionInfo.countTestCase();
		executionInfo.countExcecutedTestCase();
	}
}