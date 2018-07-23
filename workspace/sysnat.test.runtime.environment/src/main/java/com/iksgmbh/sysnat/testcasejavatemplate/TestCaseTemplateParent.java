package com.iksgmbh.sysnat.testcasejavatemplate;

import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.ERROR_KEYWORD;

import java.io.File;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.ExecutionRuntimeInfo.TestStatistics;
import com.iksgmbh.sysnat.TestCase;
import com.iksgmbh.sysnat.common.exception.SkipTestCaseException;
import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.common.exception.UnexpectedResultException;
import com.iksgmbh.sysnat.common.exception.UnsupportedGuiEventException;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.helper.ReportCreator;
import com.iksgmbh.sysnat.utils.SysNatUtil;

/**
 * This class represents the parent class for all TestCaseTemplates.
 * Each TestApplication needs its own TestCaseTemplate derived by this parent class
 * which contains all code shared by all TestCaseTemplates.
 * 
 * @author Reik Oberrath
 */
public abstract class TestCaseTemplateParent extends TestCase
{
	@Before
	public void setUp() 
	{
		super.setUp();
		System.out.println(SysNatConstants.SYS_OUT_SEPARATOR);
	}

	@After
	public void shutdown() 
	{
		if ( ! isSkipped() ) 
		{
			System.out.println("Done with test case in " + getExecDuration() + ".");
			
			final TestStatistics testStatistics = new ExecutionRuntimeInfo.TestStatistics(startTime, new DateTime());
			executionInfo.addTestStatistics(getTestID(), testStatistics );
			
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
		
		setTestID(null);
		System.out.println("Intermediate Result: " + executionInfo.getIntermediateResultLogText() + ".");
	}
	
	/**
	 * This method is filled by NatSpec with the concrete code that is defined
	 * by the .natspec file for which this template is instantiated. The comment
	 * <code>@MethodBody</code> is replaced with the generated code for all the
	 * sentences in the NatSpec scenario.
	 */
	protected void handleThrowable(Throwable t)  
	{
		try {
			throw t;
		} catch (SkipTestCaseException e) {
			finishSkippedTestCase(e.getSkipReason());
		} catch (UnexpectedResultException e) {
			takeScreenshot(SysNatUtil.getScreenshotFailureFileName(this.getClass().getSimpleName()));
			terminateWrongTestCase();
		} catch (SysNatTestDataException e) {
			takeScreenshot(SysNatUtil.getScreenshotFailureFileName(this.getClass().getSimpleName()));
			terminateWrongTestCase();
		} catch ( UnsupportedGuiEventException e) {
			e.printStackTrace();		
			takeScreenshot(SysNatUtil.getScreenshotErrorFileName(this.getClass().getSimpleName()));
			failWithMessage(ERROR_KEYWORD 
					        + ": Folgendes grafisches Element ist nicht verfügbar:" 
		                    + extractSelectorValue(e.getMessage())); 
		} catch (SysNatException e) {
			takeScreenshot(SysNatUtil.getScreenshotErrorFileName(this.getClass().getSimpleName()));
			failWithMessage(e.getMessage());
		} catch (Throwable e) {
			e.printStackTrace();
			takeScreenshot(SysNatUtil.getScreenshotErrorFileName(this.getClass().getSimpleName()));
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
		return SysNatUtil.doesTestBelongToApplicationUnderTest(this);
	}

}
