package com.iksgmbh.sysnat.testcasejavatemplate;

import static com.iksgmbh.sysnat.utils.SysNatLocaleConstants.ERROR_KEYWORD;

import org.junit.After;
import org.junit.Before;

import com.iksgmbh.sysnat.ExecutionInfo;
import com.iksgmbh.sysnat.TestCase;
import com.iksgmbh.sysnat.exception.SkipTestCaseException;
import com.iksgmbh.sysnat.exception.SysNatException;
import com.iksgmbh.sysnat.exception.UnexpectedResultException;
import com.iksgmbh.sysnat.exception.UnsupportedGuiEventException;
import com.iksgmbh.sysnat.utils.SysNatConstants;
import com.iksgmbh.sysnat.utils.SysNatUtil;

/**
 * This class represents the parent class for all TestCaseTemplate.
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
		setTestID(null);
		if ( ! isSkipped() ) {
			System.out.println("Done with test case in " + getExecDuration() + ".");
		} else {
			System.out.println(ExecutionInfo.getInstance().getTotalNumberOfTestCases() + " test cases executed so far.");
		}
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
		return this.getClass().getSimpleName();
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
