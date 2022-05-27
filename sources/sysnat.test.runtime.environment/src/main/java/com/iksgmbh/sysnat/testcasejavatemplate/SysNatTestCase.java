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
import org.netbeans.jemmy.TimeoutExpiredException;
import org.openqa.selenium.NoSuchElementException;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.ExecutionRuntimeInfo.TestStatistics;
import com.iksgmbh.sysnat.common.exception.SkipTestCaseException;
import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.exception.SysNatSuccessException;
import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.common.exception.UnexpectedResultException;
import com.iksgmbh.sysnat.common.exception.UnsupportedGuiEventException;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.helper.ReportCreator;
import com.iksgmbh.sysnat.utils.SysNatTestRuntimeUtil;

/**
 * This class represents a wrapper class of a Executable Example
 * which allows the Executable Example to be executed in the SysNat test runtime.
 * 
 * @author Reik Oberrath
 */
public abstract class SysNatTestCase extends ExecutableExample
{
	protected static Integer numberOfRemainingTestCaseStillToExecute;

	public void setUp() 
	{
		super.setUp();
		executionInfo.registerExecutedNLFile(getTestCaseFileName() + ".nlxx");
		System.out.println(SysNatConstants.SYS_OUT_SEPARATOR);
	}

	public void shutdown() 
	{
		if ( ! isSkipped() ) 
		{
			System.out.println("Done with test case in " + getExecDuration() + ".");
			
			final TestStatistics testStatistics = new ExecutionRuntimeInfo.TestStatistics(startTime, new DateTime());
			executionInfo.addTestStatistics(getXXID(), testStatistics );
			
			final String detailReportName = getTestCaseFileName();
			final String detailReportFilename = ReportCreator.buildDetailReportFilename(detailReportName);
			final File detailReportFile = new File(detailReportFilename);
			detailReportFile.getParentFile().mkdirs();
			SysNatFileUtil.writeFile(detailReportFile, ReportCreator.createSingleTestReport(this));
			String totalTimePast = executionInfo.getTotalTimePast();

			System.out.println("Time past: " + testStatistics.duration + " (this test), " + totalTimePast + " (total).");
		} 
		else 
		{
			System.out.println(ExecutionRuntimeInfo.getInstance().getTotalNumberOfXXs() + " test cases executed so far.");
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
					        + ": Folgendes grafisches Element ist nicht verfügbar: " 
		                    + extractSelectorValue(e.getMessage())); 
		} catch (SysNatException e) {
			takeScreenshot(SysNatTestRuntimeUtil.getScreenshotErrorFileName(this.getClass().getSimpleName()));
			failWithMessage(e.getMessage());
		} catch (TimeoutExpiredException e) {
			handleJemmyTimeOut(e);
		} catch (NoSuchElementException e) {
			takeScreenshot(SysNatTestRuntimeUtil.getScreenshotErrorFileName(this.getClass().getSimpleName()));
			String message = e.getMessage();
			int pos = message.indexOf("For documentation on this error, please visit: http://seleniumhq.org");
			if (pos == -1) failWithMessage(message);
			failWithMessage(message.substring(0, pos));
		} catch (AssertionError e) {
			throw e;
		} catch (SysNatSuccessException e) {
			return;
		} catch (Throwable e) 
		{
			if (e.getCause() instanceof TimeoutExpiredException) {
				handleJemmyTimeOut((TimeoutExpiredException) e.getCause());
			}
			if (e.getCause() instanceof AssertionError) {
				throw ((AssertionError)e.getCause());
			}
			e.printStackTrace();
			takeScreenshot(SysNatTestRuntimeUtil.getScreenshotErrorFileName(this.getClass().getSimpleName()));
			failWithMessage("Unerwarteter " + ERROR_KEYWORD + ": " 
		                    + e.getClass().getSimpleName() + ": " 
					        + System.getProperty("line.separator")
					        + e.getMessage());
		}		
	}

	private void handleJemmyTimeOut(TimeoutExpiredException e)
	{
		e.printStackTrace();
		takeScreenshot(SysNatTestRuntimeUtil.getScreenshotErrorFileName(this.getClass().getSimpleName()));
		String message = e.getMessage();
		String guiComponent = SysNatStringUtil.getSubstringBetween(message, "\"");
		if (guiComponent.contains("\"")) {
			guiComponent = SysNatStringUtil.getSubstringBetween(guiComponent, "\"");
		}
		failWithMessage("Das grafische Element mit der technischen ID <b>" + guiComponent + "</b> wurde in der erwarteten Zeit nicht angezeigt.");
	}

	@Override
	public String getTestCaseFileName() {
		String toReturn = this.getClass().getSimpleName();
		if (toReturn.endsWith("Test")) {
			toReturn = toReturn.substring(0, toReturn.length()-4);
		}
		if (toReturn.endsWith("_")) {
			toReturn = toReturn.substring(0, toReturn.length()-1);
		}
		return toReturn;
	}	
	
	@Override
	public Package getTestCasePackage() {
		return this.getClass().getPackage();  // these are typically lower case expressions!
	}

	@Override
	public boolean doesTestBelongToApplicationUnderTest() {
		return SysNatTestRuntimeUtil.doesTestBelongToApplicationUnderTest(this);
	}

	protected void setXXIdForInactiveTests(String xxid) {
		setXXID("InactiveTestExample");
		executionInfo.countAsExecuted(xxid, getBehaviorID());
		executionInfo.countAsExecuted(xxid);
	}

	protected void initNumberOfTestCases(int num)
	{
		if (numberOfRemainingTestCaseStillToExecute == null) {
			numberOfRemainingTestCaseStillToExecute = num;
		}
	}	
}