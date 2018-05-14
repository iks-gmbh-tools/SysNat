package com.iksgmbh.sysnat._testhelper;

import static com.iksgmbh.sysnat.utils.SysNatLocaleConstants.ERROR_KEYWORD;

import java.util.NoSuchElementException;

import org.junit.After;
import org.junit.Before;

import com.iksgmbh.sysnat.ExecutionInfo;
import com.iksgmbh.sysnat.TestCase;
import com.iksgmbh.sysnat.exception.SkipTestCaseException;
import com.iksgmbh.sysnat.exception.SysNatException;
import com.iksgmbh.sysnat.exception.UnexpectedResultException;
import com.iksgmbh.sysnat.language_templates.iksonline.LanguageTemplatesIksOnlineBasics;
import com.iksgmbh.sysnat.utils.SysNatUtil;
import com.iksgmbh.sysnat.utils.SysNatConstants;

/**
 * This class serves as a template for all classes that are generated for
 * NatSpec scenarios (.natspec files). It is recognized by NatSpec based on its
 * special name and applies to all scenarios in the same package and all sub
 * packages (unless there is another template class in one of the sub packages).
 * <p>
 * This particular template is a JUnit test case where the concrete steps of the
 * test are filled by the NatSpec code generator (see {@link #executeScript()}).
 */
public class _NatSpecTestCaseTemplate extends TestCase
{
	protected LanguageTemplatesBasicsTestImpl languageTemplatesBasics;
	protected LanguageTemplatesIksOnlineBasics languageTemplatesIksOnlineBasics;

	private boolean isThisFileTheNatSpecTemplate = true;
	
	@Before
	public void setUp() 
	{
		isThisFileTheNatSpecTemplate = this.getClass().getSimpleName().startsWith("_");
		if ( ! isThisFileTheNatSpecTemplate ) 
		{
			super.setUp();
			System.out.println(SysNatConstants.SYS_OUT_SEPARATOR);
			languageTemplatesBasics = new LanguageTemplatesBasicsTestImpl(this);
			languageTemplatesIksOnlineBasics = new LanguageTemplatesIksOnlineBasics(this);
		}
	}

	@After
	public void shutdown() 
	{
		if ( ! isThisFileTheNatSpecTemplate ) 
		{
			if ( ! isSkipped() ) {
				languageTemplatesIksOnlineBasics.gotoStartPage();
			}
			languageTemplatesIksOnlineBasics = null;
			languageTemplatesBasics = null;
			setTestID(null);
			if ( ! isSkipped() ) {
				System.out.println("Done with test case in " + getExecDuration() + ".");
			} else {
				System.out.println(ExecutionInfo.getInstance().getTotalNumberOfTestCases() + " test cases executed so far.");
			}
		}
	}
	
	/**
	 * This method is filled by NatSpec with the concrete code that is defined
	 * by the .natspec file for which this template is instantiated. The comment
	 * <code>@MethodBody</code> is replaced with the generated code for all the
	 * sentences in the NatSpec scenario.
	 */
	public void executeTestCase()  
	{
		try {
			/* @MethodBody */
			
			if ( ! isThisFileTheNatSpecTemplate) {
				closeCurrentTestCaseWithSuccess();
			}
		} catch (SkipTestCaseException e) {
			finishSkippedTestCase(e.getSkipReason());
		} catch (UnexpectedResultException e) {
			takeScreenshot(SysNatUtil.getScreenshotFailureFileName(this.getClass().getSimpleName()));
			terminateWrongTestCase();
		} catch (SysNatException e) {
			takeScreenshot(SysNatUtil.getScreenshotErrorFileName(this.getClass().getSimpleName()));
			failWithMessage(e.getMessage());
		} catch ( NoSuchElementException nse) {
			nse.printStackTrace();		
			takeScreenshot(SysNatUtil.getScreenshotErrorFileName(this.getClass().getSimpleName()));
			failWithMessage(ERROR_KEYWORD 
					        + ": Folgendes grafisches Element ist nicht verfügbar:" 
		                    + extractSelectorValue(nse.getMessage())); 
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
