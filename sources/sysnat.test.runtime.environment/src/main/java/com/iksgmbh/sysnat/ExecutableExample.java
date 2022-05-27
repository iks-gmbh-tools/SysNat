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
package com.iksgmbh.sysnat;

import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.CATEGORY_BILDNACHWEIS;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.ERROR_KEYWORD;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.NO_KEYWORD;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.PICTURE_PROOF;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.YES_KEYWORD;
import static org.junit.Assert.fail;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;

import com.iksgmbh.sysnat.common.exception.SkipTestCaseException.SkipReason;
import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.exception.SysNatSuccessException;
import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.common.exception.UnexpectedResultException;
import com.iksgmbh.sysnat.common.exception.UnsupportedGuiEventException;
import com.iksgmbh.sysnat.common.helper.FileFinder;
import com.iksgmbh.sysnat.common.helper.HtmlLauncher;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.ResultLaunchOption;
import com.iksgmbh.sysnat.common.utils.SysNatDateUtil;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.domain.SysNatTestData;
import com.iksgmbh.sysnat.domain.TestApplication;
import com.iksgmbh.sysnat.guicontrol.GuiControl;
import com.iksgmbh.sysnat.guicontrol.impl.SeleniumGuiController;
import com.iksgmbh.sysnat.helper.ReportCreator;
import com.iksgmbh.sysnat.helper.WindowHelper;
import com.iksgmbh.sysnat.language_templates.LanguageTemplateBasics;
import com.iksgmbh.sysnat.language_templates.PageObject;
import com.iksgmbh.sysnat.testdataimport.TableDataParser;
import com.iksgmbh.sysnat.testdataimport.TestDataImporter;
import com.iksgmbh.sysnat.testresult.archiving.SysNatTestResultArchiver;

/**
 * Mother of all test classes that stores context information about the currentlx executed test.
 * 
 * All technical but technologically unspecific stuff belongs in here.
 * 
 * It knows the GuiControl and is therefore used by the PageObjects to trigger gui events.
 * It is also used by the LanguageTemplateContainers, e.g. to access test data.
 * 
 * @author Reik Oberrath
 */
abstract public class ExecutableExample
{
	public static final String SMILEY_FAILED = "&#x1F61E;";
	public static final String SMILEY_WRONG = "&#x1F612;";
	public static final String SMILEY_OK = "&#x1F60A;";
	
	private static final String YES = YES_KEYWORD + " " + SMILEY_OK;
	private static final String NO = NO_KEYWORD + " " + SMILEY_WRONG;

    protected ExecutionRuntimeInfo executionInfo = ExecutionRuntimeInfo.getInstance();
    protected DateTime startTime = new DateTime();
	protected SysNatTestData testDataSets = new SysNatTestData(); 	     // preexisting data imported from external data files
	protected HashMap<String, Object> testObjects = new HashMap<>();     // data created during test execution
    protected TestDataImporter testDataImporter;
	protected PageObject currentPage;

	private List<String> executionFilterList;
    private String XXID = null; // unique id of the executable example
	private boolean skipped = false;
	private boolean alreadyTerminated = false;
	private DateTime startDate = DateTime.now();
	private List<String> reportMessages = new ArrayList<>();
	private String scriptToExecute;
	private String bddKeyword = "";
	private String behaviourID;
	private GuiControl guiControlOfFocusedApp;
	private boolean acceptMissingValues = true;

	// abstract methods
    abstract public void executeTestCase();
    abstract public String getTestCaseFileName();
    abstract public Package getTestCasePackage();
    abstract public boolean doesTestBelongToApplicationUnderTest();
    
    public void setUp() {
    	init();
    }
    
	public void init() 
	{
		if (! executionInfo.isShutDownHookAdded()) {
			initShutDownHook();
		}
		
		if (executionInfo.isTestEnvironmentInitializionFailed()) {
			fail("Test application could not be started or test environment failed to be initialized correctly.");
		}

		if (executionInfo.isNoGuiMode()) return;
		
		if ( ! executionInfo.isTestEnvironmentInitialized()) 
		{
			boolean ok = initTestEnvironment();
			if ( ! ok ) {
				executionInfo.setTestEnvironmentInitializionFailed();
				fail("Test application could not be started or test environment failed to be initialized correctly.");
			}
		} else {
			reinitTestEnvironment();
		}
	}
	
	private void reinitTestEnvironment() 
	{
		if (System.getProperty("isWebApplication", "false").equals("true"))
		{
			executionInfo.getGuiControllerMap().forEach((key,value) -> value.reloadGui());  // prepare gui for next test
		}
		
		List<String> keys = new ArrayList<String>(executionInfo.getGuiControllerMap().keySet());
		guiControlOfFocusedApp = executionInfo.getGuiControllerMap().get(keys.get(0));
	}
	
	private boolean initTestEnvironment() 
	{
		List<String> dirList = new ArrayList<>();
		dirList.add(executionInfo.getTestdataDir());
		if (executionInfo.getTestApplication().isCompositeApplication()) 
		{
			String parent = System.getProperty("sysnat.testdata.import.directory");
			executionInfo.getTestApplication().getElementAppications().forEach(app -> dirList.add(parent + "/" + app));
		}
		testDataImporter = new TestDataImporter(dirList);

		boolean ok = ApplicationStarter.doYourJob(this, executionInfo.getTestApplication()) != null;
		if (! ok) return false;
		
		executionInfo.setTestEnvironmentInitialized();
		return true;
	}

	protected boolean isSkipped() {
		return skipped;
	}

	protected String getExecDuration() 
	{
		DateTime now = DateTime.now();
		long passedSeconds = (now.getMillis()-startDate.getMillis()) / 1000;
		long minutes = passedSeconds/60;
		long seconds = passedSeconds - (minutes * 60);
		return minutes + "min and " + seconds + "sec";
	}

	public void setReportMessages(List<String> messages) {
		reportMessages = messages;
	}

	public List<String> getReportMessages() {
		return reportMessages;
	}

	public void setXXID(String aXXID) {
		this.XXID = aXXID;
	}
	
	public String getXXID() {
		return XXID;
	}
    
	public void addReportMessage(String message) 
	{
		if (bddKeyword == null || bddKeyword.isEmpty()) {
			reportMessages.add(message.trim());  
		} else {
			// reportMessages.add(message.trim());  TODO Decide whether and how the BDD-Keyword enters the report
			reportMessages.add("<b>" + bddKeyword + "</b> " + message.trim()); 
		}
	}

	public void terminateTestCase(String message) 
	{
		if (! alreadyTerminated) {
			alreadyTerminated = true;
			fail( message );
		}
	}
	
	public void passTestCaseImmediately(String message)
	{
		if (message != null) addCommentToReport(message);
    	closeCurrentTestCaseWithSuccess();
    	throw new SysNatSuccessException();
	}

	public void passTestCaseImmediately() {
		passTestCaseImmediately(null);
	}
	

	public String extractSelectorValue(String message) 
	{
		String marker = "\"selector\":\"";
		int pos = message.indexOf(marker);
		
		if (pos == -1) 
		{
			marker = "Das Element mit der technischen Identifizierung";
			pos = message.indexOf(marker);
			if (pos == -1) {
				return message;
			}
			String toReturn = message.substring(pos + marker.length());
			pos = toReturn.indexOf("konnte auf der aktuellen Seite nicht gefunden werden.");
			toReturn = toReturn.substring(0, pos);
			return toReturn;
		} 
		else 
		{
			String toReturn = message.substring(pos + marker.length());
			pos = toReturn.indexOf('\"');
			toReturn = toReturn.substring(0, pos);
			return toReturn;
			
		}
	}
	
    public void takeScreenshot(String filename)
    {
    	if ( ! filename.endsWith(".png")) {
    		filename += ".png";
    	}
    	
        File scrFile = null;
        try {
            scrFile = getActiveGuiController().takeScreenShot();
        } catch (Exception e) {
            System.out.println("Error taking screenshot: " + e.getMessage());
            return;
        }

        try {
        	String subfolderPath = XXID + File.separator + filename;
        	subfolderPath = SysNatFileUtil.replaceInvalidFilenameChars(subfolderPath);
            String screenshotFileName = executionInfo.getReportFolder() + File.separator + subfolderPath;
			FileUtils.copyFile(scrFile, new File(screenshotFileName));
        } catch (Exception e) {
        	System.out.println("Error saving screenshot: " + e.getMessage());
        } finally {
            if (scrFile != null) {
                try {
					FileUtils.forceDelete(scrFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        }
    }
	
	public void closeCurrentTestCaseWithSuccess() 
	{
		if (reportMessages != null)  {
			System.out.println("Test Result: OK");
			executionInfo.addTestMessagesOK(getCheckedXXId(), reportMessages, behaviourID);
		}
	}

	public void terminateWrongTestCase() {
		System.out.println("Test Result: Failed Assertion");
		executionInfo.addTestMessagesWRONG(getCheckedXXId(), reportMessages, behaviourID);
		throw new UnexpectedResultException();
	}

	public void finishSkippedTestCase(SkipReason skipReason) 
	{
		skipped = true;
		XXID = null;
		reportMessages.clear();
		if (skipReason == SkipReason.ACTIVATION_STATE) {
			System.out.println("Skipped because set inactive.");
		}
		
		if (skipReason == SkipReason.EXECUTION_FILTER) {
			System.out.println("Skipped due to current execution filter(s) " + executionInfo.getExecutionFilterList());
		}
		
		if (skipReason == SkipReason.APPLICATION_TO_TEST) {
			System.out.println("Skipped due to current test application: " + executionInfo.getTestApplicationName());
		}	
	}

	/**
	 * Terminate XX due to a non-technical problem.
	 * @param message
	 */
	public void stopExecutionWithMessage(String message) 
	{
		addReportMessage("<b>" + SysNatLocaleConstants.PROBLEM_KEYWORD + ":</b> " + message);
		terminateWrongTestCase();
	}

	public void failWithMessage(String message) 
	{
		if (executionInfo.isXXIdAlreadyUsed(getCheckedXXId())) {
			return;
		}
		if ( ! message.contains(ERROR_KEYWORD) )  {
			message = ERROR_KEYWORD + ": " + message;
		}
		System.out.println("Test Result: Technical Error");
		reportMessages.add(message);
		executionInfo.addTestMessagesFAILED(getCheckedXXId(), reportMessages, behaviourID);
		terminateTestCase(message);
	}

	
	public LanguageTemplateBasics getApplicationSpecificLanguageTemplates(String applicationUnderTest) 
    {
    	final String testappl = applicationUnderTest.toLowerCase();
    	final String expectedClassName = "com.iksgmbh.sysnat.language_templates." + testappl + "."
                                         + "LanguageTemplatesBasics_" + applicationUnderTest;
    	try {
			final Class<?> type =  Class.forName(expectedClassName);
			final Constructor<?> constructor = type.getConstructor(ExecutableExample.class);
			final Object toReturn = constructor.newInstance(this);
			return (LanguageTemplateBasics) toReturn;
		} 
    	catch (ClassNotFoundException e) 
    	{
    		String errorMessage = "For TestApplication '" + executionInfo.getTestApplicationName() 
                                   + "' the expected LanguageTemplateContainer '" + expectedClassName + "' has not been found.";
            System.err.println(errorMessage);
            throw new UnsupportedGuiEventException(errorMessage);
    		
    	} 
    	catch (Exception e) 
    	{
    		String errorMessage = "An instance of the LanguageTemplateContainer (" + expectedClassName + ") "
    				                + "for TestApplication '" + executionInfo.getTestApplicationName() 
    		                        + ".java' could not be created.";
    		System.err.println(errorMessage);
    		throw new UnsupportedGuiEventException(errorMessage);
		}
	}
    
    public void sleep(int millis) 
    {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // ignore
        }
    }
    
	//##########################################################################################
	//               G U I   C O N T R O L   D E L E G A T I O N   M E T H O D S
	//##########################################################################################

	public void switchToTab(String tabPanelIdentifier, String tabTitle) {
		getActiveGuiController().clickTab(tabPanelIdentifier, tabTitle);
	}
	
	public void switchToTab(String tabPanelIdentifier, int tabIndex) {
		getActiveGuiController().clickTab(tabPanelIdentifier, tabIndex);
	}
	
    public void inputText(String fieldName, String value) {
    	getActiveGuiController().enterTextInTextField(value, fieldName);
    }
    
    public void inputTextInTextArea(String areaName, String value) {
    	getActiveGuiController().inputTextInTextArea(value, areaName);
    }
    
    public void inputText(int index, String value) {
    	getActiveGuiController().enterTextInTextField(index, value);
    }
    
    public void inputDate(String fieldName, String value) {
    	getActiveGuiController().enterDateInDateField(value, fieldName);
    }
    
    public void inputEmail(String fieldName, String emailAdress) {
    	getActiveGuiController().inputEmail(fieldName, emailAdress);
    }

	public void clickTableCellLink(String xPathToCell) {
		((SeleniumGuiController)getActiveGuiController()).clickTableCellLink(xPathToCell);
		
	}
    
    public void clickButton(String guiTextOrTechnicalId) 
    {
    	try {
    		getActiveGuiController().clickButton(guiTextOrTechnicalId);
		} catch (Exception e) {
			failWithMessage("Button <b>" + guiTextOrTechnicalId + "</b> konnte nicht geklickt werden.");
		}
    }
    
    public void clickButton(String id, int timeOutInMillis) {
    	clickElement(id, timeOutInMillis);
    }
    

    /**
     * Assumes that the text referenced by xpath consists of lines of text separated by '\n'
     * @param xpath
     * @return number of lines
     */
    public int getNumberOfSelectableEntries(String xpath)  {
    	return getActiveGuiController().getNumberOfLinesInTextArea(xpath);
    }

    public void selectEntry(String xpath, int index)  {
    	getActiveGuiController().selectComboboxEntry(xpath, index);
    }

    public String getTextForId(String id) {
    	return getActiveGuiController().getText(id);
    }
	
    public void clickLink(String valueCandidate)
    {
    	String value = getTestDataValue(valueCandidate);
    	getActiveGuiController().clickLink(value);
    }

    public void clickLink(String valueCandidate, String idToScrollIntoView)
    {
    	String value = getTestDataValue(valueCandidate);
    	getActiveGuiController().clickLink(value, idToScrollIntoView);
    }

    /**
     * If link occurs more than once, the positionOfOccurrence tells which occurrence to click.
     * @param valueCandidate
     * @param positionOfOccurrence
     */
    public void clickLink(String valueCandidate, int positionOfOccurrence)
    {
    	String value = getTestDataValue(valueCandidate);
    	getActiveGuiController().clickLink(value, positionOfOccurrence);
    }

    public void clickLinkIfPossible(String valueCandidate)
    {
       try {
          clickLink(valueCandidate);
       } catch (UnsupportedGuiEventException e) {
    	   addCommentToReport(e.getMessage());
       } catch (SysNatException e) {
          // ignore this action
       }
    } 

    public void chooseFromComboBoxByIndex(String id, int index) {
    	getActiveGuiController().selectComboboxEntry(id, index);
    }

    public void chooseFromComboBoxByValue(String id, String value) {
    	getActiveGuiController().selectComboboxEntry(id, value);
    }
    
    public void clickElement(final String elementAsString, int timeoutInMillis) {
    	clickElement(elementAsString, timeoutInMillis, elementAsString);
    }
    
    public void clickElement(final String elementAsString, int timeoutInMillis, String uiText)
    {
	    //String elementText = getTestDataValue(elementAsString);
	    //clickElement(elementText);
    	String errMsg = getActiveGuiController().clickElement(elementAsString, timeoutInMillis);
		if (errMsg != null) {
			failWithMessage(errMsg);
		}
		addReportMessage("Es wurde auf <b>" + uiText + "</b> geklickt.");
    }

    public void clickElement(final String elementAsString, String uiText)
    {
    	clickElement(elementAsString, executionInfo.getDefaultGuiElementTimeout(), uiText);
    }

    public void clickElement(final String elementAsString)
    {
    	clickElement(elementAsString, executionInfo.getDefaultGuiElementTimeout());
    }

    public boolean isElementReadyToUse(String elementId) {
    	return getActiveGuiController().isElementReadyToUse(elementId);
    }
    
    public boolean isElementAvailable(String elementId) {
    	return getActiveGuiController().isElementAvailable(elementId, 500, false);
    }    

	public int countNumberOfRowsInTable(String tableClass) {
		return getActiveGuiController().getNumberOfRowsInTable(tableClass);
	}

	public int countNumberOfColumnsInTable(String tableClass) {
		return getActiveGuiController().getNumberOfColumnsInTable(tableClass);
	}
	
	public void selectFromDropdown(String fieldName, String value) {
		getActiveGuiController().selectComboboxEntry(fieldName, value);
	}

	public boolean isEntryInComboboxDropdownAvailable(String elementIdentifier, String value) {
		return getActiveGuiController().isEntryInComboboxDropdownAvailable(elementIdentifier, value);
	}

	public void chooseOption(String elementIdentifier, int position) {
		getActiveGuiController().clickRadioButton(elementIdentifier, position);
	}

	public void clickFirstButtonsOf(String... buttonIDs) 
	{
		RuntimeException excectionToThrow = null;
		for (String buttonID : buttonIDs) {
			try {			
				String errMsg = getActiveGuiController().clickElement(buttonID);
				if (errMsg != null) {
					failWithMessage(errMsg);
				}
				return;
			} catch (RuntimeException e) {
				excectionToThrow = e;
			}
		}
		throw excectionToThrow;
	}

	public void clickCheckbox(String checkboxId) 
	{
		String errMsg = getActiveGuiController().clickElement(checkboxId);
		if (errMsg != null) {
			failWithMessage(errMsg);
		}
	}

	public boolean isTabActive(String tabId) {
		String selectedTabName = getSelectedTabName();
		return tabId.equals(selectedTabName);
	}
	
	public boolean isTabValid(String tabId) {
		return ((SeleniumGuiController) getActiveGuiController()).isTabValid(tabId);
	}
	
	public boolean isErrorTab(String tabId) 
	{
		return ((SeleniumGuiController) getActiveGuiController()).isErrorTab(tabId);
	}
	

	public String getTextForElement(String elementIdentifier) {
		return getActiveGuiController().getText(elementIdentifier);
	}

	public void clickElementInTable(String tableIdentifier, int rowNumber, int columnNumber) {
		getActiveGuiController().clickTableCell(tableIdentifier, rowNumber, columnNumber);
	}

	public void clickTab(String tabIdentifier, String tabName) {
		getActiveGuiController().clickTab(tabIdentifier, tabName);
	}
	
	private String getSelectedTabName() {
		return getActiveGuiController().getSelectedTabName();
	}
	
	public boolean isFailedLoginLabelVisible() {
		return isElementAvailable("validationErrors");
	}
	
	public void waitUntilEnabledElementIsAvailable(String elementIdentifier) {
		getActiveGuiController().waitUntilEnabledElementIsAvailable(elementIdentifier);
	}
	
	public void waitUntilElementIsAvailable(String elementIdentifier) {
		getActiveGuiController().waitUntilElementIsAvailable(elementIdentifier);
	}
	

	public void clickMenuHeader(String elementIndentifier, String uiText) 
	{
		try {
			getActiveGuiController().clickMenuItem(uiText);
		} catch (Exception e) {
			try {
				getActiveGuiController().clickMenuItem(elementIndentifier);
			} catch (Exception e2) {
				failWithMessage("Menüpunkt <b>" + uiText + "</b> ist nicht vorhanden.");
			}
		}
	}

	public void clickMenuHeader(String elementIndentifier) 
	{
		String errMsg = getActiveGuiController().clickElement(elementIndentifier);
		if (errMsg != null) {
			failWithMessage(errMsg);
		}
	}
	
	public void closeCurrentTab() {
		((SeleniumGuiController)getActiveGuiController()).closeCurrentTab();
	}
	
	public void downloadPdf() 
	{
		int numberOfOpenBrowserFrames = getActiveGuiController().getNumberOfOpenApplicationWindows();
		boolean isPdfDisplayedInBrowserTab = waitUntilPdfGenerationIsFinished(numberOfOpenBrowserFrames);
		if (isPdfDisplayedInBrowserTab)  // PDF is shown in browser tab, now download it
		{
			int numberOfPDFs = SysNatFileUtil.findDownloadFiles("PDF").size();
			startDownload();  // does not work always - reason unclear
			SysNatFileUtil.waitUntilNumberOfPdfFilesIs(numberOfPDFs + 1);
			
			closeCurrentTab();
		} else {
			// do nothing, PDF has been directly saved in download dir
		}
	}
	
	private boolean waitUntilPdfGenerationIsFinished(int originalNumberOfBrowserWindows) 
	{
		long startTime = new Date().getTime();
		boolean tryAgain = true;
		long waitPeriodInSeconds = 0;

		while (tryAgain) {
			try {
				int numberOfBrowserFrames = getActiveGuiController().getNumberOfOpenApplicationWindows();
				int numberOfOpenTabs = ((SeleniumGuiController)getActiveGuiController()).getNumberOfOpenTabs();
				if (originalNumberOfBrowserWindows + 1 == numberOfBrowserFrames) {
					getActiveGuiController().switchToLastWindow();
				}
				boolean isDownloadButtonAvailable = isElementAvailable("download");
				if ((numberOfOpenTabs == 2 || originalNumberOfBrowserWindows + 1 == numberOfBrowserFrames)
				        && isDownloadButtonAvailable) {
					return true;
				}
			} catch (Exception e) {
				// do nothing
			}

			sleep(1000);
			waitPeriodInSeconds = (new Date().getTime() - startTime) / 1000;
			if (waitPeriodInSeconds > executionInfo.getDefaultPrintTimeout()) {
				addReportMessage(
				        "<b>Der Druckversuch wurde nach " + waitPeriodInSeconds + " Sekunden abgebrochen!</b>");
				return false;
			}
		}
		
		return false;
	}
	
	public void setTickToCheckboxIfPossible(String checkboxId) 
	{
		if ( isElementAvailable(checkboxId) 
			 && getActiveGuiController().isElementReadyToUse(checkboxId) 
			 && guiControlOfFocusedApp.isCheckBoxTicked(checkboxId) ) 
		{
			clickCheckbox(checkboxId);
		}
	}
	
	public String[] getSelectableEntries(String id) {
		return getTextForId(id).split("\n");
	}
	
	public String getSelectedComboBoxEntry(String elementId) {
		return getActiveGuiController().getSelectedComboBoxEntry(elementId);
	}
	
	public String getTextFromTable(String tableIndentifier, int rowNumber, int columnNumber) {
		return getActiveGuiController().getTableCellContent(tableIndentifier, rowNumber, columnNumber).toString();
	}
	
	public String getTableCellContent(String tableIndentifier, String contentOfFirstCellInRow, int columnNumber) 
	{
		int numberOfRows = getActiveGuiController().getNumberOfRowsInTable(tableIndentifier);
		for (int rowNumber=1; rowNumber<=numberOfRows; rowNumber++) 
		{
			String cellContent = getTextFromTable(tableIndentifier, rowNumber, 1);
			if (cellContent.equals(contentOfFirstCellInRow))
			{
				return getTextFromTable(tableIndentifier, rowNumber, columnNumber);
			}
		}
		throw new UnsupportedGuiEventException("Tabelle <b>" + tableIndentifier + "</b> enthält "
				                               + "in der ersten Spalte keinen Eintrag für <b>" + contentOfFirstCellInRow + "</b>.");
	}
	
	
	//##########################################################################################
	//                       P R I V A T E    M E T H O D S
	//##########################################################################################


	private String getCheckedXXId() 
	{
		if (XXID == null)  {
			XXID = getTestCaseFileName();
			terminateTestCase("Für den Test " + XXID + " wurde keine XXID angegeben!");
		}
		return XXID;
	}

	protected void initShutDownHook() 
	{
		executionInfo.setShutDownHookAdded();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() 
			{
				System.out.println(SysNatConstants.SYS_OUT_SEPARATOR);

				if (executionInfo.isTestEnvironmentInitialized()) {
					createReportHtml();
				}
				
				if (executionInfo.getNumberOfAllExecutedXXs() == 0) {
					System.out.println("No test executed. Check execution filter and executable examples.");
				} else {
					System.out.println("Done with executing " + executionInfo.getTestReportName() + ".");
				}
		    	
	    		if (executionInfo.getGuiControllerMap() != null)  {
	    			executionInfo.getGuiControllerMap().forEach((key,value) -> value.closeGUI());
	    		}
			}
		});
	}
	
	
    /**
    * Creates a big overview report with all details and a short overview report for ALM archiving. 
     * Note: single detail reports for each test case have been already created in _NatSpecTemplate.shutdown()
    */
    private void createReportHtml() 
    {
        // full overview report
    	final String fullOverviewReportFilename = ReportCreator.getFullOverviewReportFilename();
    	try {
    		ReportCreator.createFullOverviewReport();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	final String fullReport = ReportCreator.createFullOverviewReport();
        SysNatFileUtil.writeFile(fullOverviewReportFilename, fullReport);

        ResultLaunchOption resultLaunchOption = ExecutionRuntimeInfo.getInstance().getResultLaunchOption();
		if (resultLaunchOption == ResultLaunchOption.Testing || resultLaunchOption == ResultLaunchOption.Both) {
			HtmlLauncher.doYourJob( fullOverviewReportFilename );
    	}
          
        // small report
        final String shortOverview = ReportCreator.createShortOverviewReport();
        SysNatFileUtil.writeFile( ReportCreator.getShortOverviewReportFilename(), shortOverview);
          
         if (executionInfo.isTestReportToArchive()) {
              SysNatTestResultArchiver.doYourJob( executionInfo.getReportFolder() );
         } else {
                System.out.println("Archiving test results is omitted.");
         }
         
         // save executed Scripts to document what exactly has been executed for further analysis
         copyExecutedNLFilesToReportDir();
         String zipFileName = executionInfo.getTestReportName() + "-" +
        		              executionInfo.getStartPointOfTimeAsFileStringForFileName() + ".zip";
         
         SysNatFileUtil.createZipFile(executionInfo.getReportFolder(), 
        		                      new File(executionInfo.getReportFolderAsString(), 
        		                    		   zipFileName));
    }

	private void copyExecutedNLFilesToReportDir() 
	{
		String reportDir = executionInfo.getReportFolder().getAbsolutePath();
        String scriptDir = reportDir + "/executedNLFiles";
        new File(scriptDir).mkdirs();
        
        final List<File> nlsFiles = collectExecutedNLFiles();
        nlsFiles.forEach(file -> SysNatFileUtil.copyFileToTargetDir(file.getAbsolutePath(), scriptDir));
	}
	
	private List<File> collectExecutedNLFiles() 
	{
		final List<File> toReturn = new ArrayList<>();
		List<String> executedNLFiles = executionInfo.getExecutedNLFiles();
		executedNLFiles.forEach(filename -> toReturn.add(findExecutedNLFile(filename)));
		toReturn.removeAll(Collections.singleton(null));
		return toReturn;
	}
	
	private File findExecutedNLFile(final String filename) 
	{
		String testCaseDir = executionInfo.getExecutableExampleDir() + "/" + executionInfo.getTestApplicationName();
		
		FilenameFilter scriptFileFilter = new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.equals(filename);
			}
		};
		
		List<File> result = FileFinder.searchFilesRecursively(new File(testCaseDir), scriptFileFilter);
		
		if (result.size() != 1) {
			System.err.println("Cannot find unique file '" + filename + "' in directory: " + testCaseDir);
			return null;
		}
		
		return result.get(0);
	}
	
	public void answerQuestion(final String question, final boolean  ok)
	{
		if (ok) { 
			addReportMessage(question + YES); 
		} else {
			addReportMessage(question + NO);
			throw new UnexpectedResultException();
		}
	}
	
	public int getNumberOfBrowserWindows() {
		return ((SeleniumGuiController)getActiveGuiController()).getNumberOfBrowserWindows();
	}

	public void waitUntilNumberOfBrowserWindowsIs(int expectedNumber) 
	{
	    int numberOfBrowserWindows = getNumberOfBrowserWindows();
	    int secondCounter = 0;
	    
	    while (numberOfBrowserWindows > expectedNumber) 
	    {
	    	sleep(1000);
	    	secondCounter++;
	    	numberOfBrowserWindows = getNumberOfBrowserWindows();
	    	if (secondCounter > executionInfo.getDefaultPrintTimeout()) {
	    		addReportMessage("<b>Der Druckversuch wurde nach " + executionInfo.getDefaultPrintTimeout() + " Sekunden abgebrochen!</b>");
	    		return;
	    	}
	    }
    	
	    addReportMessage("//Der Test musste " + secondCounter + " Sekunden auf den Druck warten.");
	}
	
	private void startDownload() 
	{
		int numberOfAvailableWindows1 = WindowHelper.getNumberOfAvailableWindows();
		((SeleniumGuiController)getActiveGuiController()).clickDownloadButton();
		sleep(2000); // give time for open dialog to appear and to set focus on the OK button
		int numberOfAvailableWindows2 = WindowHelper.getNumberOfAvailableWindows();
		if (numberOfAvailableWindows1 < numberOfAvailableWindows2) {
			sendPressTabEvent();
			sleep(1000);
			sendPressTabEvent();
			sleep(1000);		 	
			sendPressSpaceEvent();  // this ticks the checkbox "Diese Aktion für alle Dateien dieses Typs auführen"
			sleep(1000);
			sendPressTabEvent();
			sleep(1000);
			sendPressEnterEvent(); // this handles the open dialog by activating the ok button
			sleep(1000);
		}
		
	}

	public void doMouseClickOn(int x, int y) 
	{
		try {
			Robot robot = new Robot();
			robot.mouseMove(x, y);
			robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
		} catch (AWTException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * Executes a single key stroke
	 * @param event KeyEvent.VK constant
	 */
	public void sendKeyEvent(int event) 
	{
		try {
			Robot robot = new Robot();
			robot.keyPress(event);
			robot.keyRelease(event);
		} catch (AWTException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void sendPressTabEvent() {
		sendKeyEvent(KeyEvent.VK_TAB);
	}
	
	private void sendPressEnterEvent() {
		sendKeyEvent(KeyEvent.VK_ENTER);
	}

	private void sendPressSpaceEvent() {
		sendKeyEvent(KeyEvent.VK_SPACE);
	}
	
	protected void sendPressTabBackwardsEvent() 
	{
		try {
			Robot robot = new Robot();
			robot.keyPress(KeyEvent.VK_SHIFT);
			robot.keyRelease(KeyEvent.VK_TAB);
			robot.keyRelease(KeyEvent.VK_TAB);
			robot.keyRelease(KeyEvent.VK_SHIFT);
		} catch (AWTException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getPictureProofName() 
	{
		final String fileNamePrefix = getTestCaseFileName() + PICTURE_PROOF;
		final String reportFolder = ExecutionRuntimeInfo.getInstance().getReportFolder().getAbsolutePath();
		final int numberOfExistingBildnachweise = 
				SysNatFileUtil.getNumberOfFilesStartingWith(fileNamePrefix,
						                                    reportFolder);
		return fileNamePrefix + (numberOfExistingBildnachweise + 1);
	}
	
	public boolean hasBildnachweisCategory() 
	{
		for (String filter : getExecutionFilterList()) {
			if (CATEGORY_BILDNACHWEIS.equals(filter)) {
				return true;
			}
		}
		
		return false;
	}
	
	public List<String> buildExecutionFilterList(String executionFilterOfThisTestCase) 
	{
		setExecutionFilter(SysNatStringUtil.getExecutionFilterAsList(executionFilterOfThisTestCase, getXXID()));
		return getExecutionFilterList();
	}
	
	public List<String> getExecutionFilterList() {
		return executionFilterList;
	}
	
	public void setExecutionFilter(List<String> filters) {
		this.executionFilterList = filters;
	}

	public void setActiveGuiController(GuiControl aGuiControl) {
		guiControlOfFocusedApp = aGuiControl;
	}

	/**
	 * 
	 * @param appName
	 * @return false if current guiControl belongs to appName - true for a real switch 
	 */
	public boolean setActiveGuiControllerFor(String appName) 
	{
		Optional<String> candidate = executionInfo.getGuiControllerMap().keySet().stream().filter(key -> key.equals(appName)).findFirst();
		if (candidate.isPresent()) {
			GuiControl guiControl = executionInfo.getGuiControllerMap().get(candidate.get());
			if (guiControl == guiControlOfFocusedApp) {
				return false;
			}
			guiControlOfFocusedApp = guiControl; 
			return true;
		}
		
		if (executionInfo.getTestApplication().getElementAppications().contains(appName)) 
		{
			TestApplication testAppToStart = new TestApplication(appName);
			ApplicationStarter.doYourJob(this, testAppToStart);
			guiControlOfFocusedApp = executionInfo.getGuiControllerMap().get(appName);
			return true;
		}		
		
		throw new SysNatException("Unsupported application: " + appName);
	}
	
	public GuiControl getActiveGuiController() 
	{
		if (guiControlOfFocusedApp == null) { 
//		if (! executionInfo.isTestEnvironmentInitialized()) {
			init();
		}
		return guiControlOfFocusedApp;
	}

	public List<String> getPackageNames() 
	{
		final List<String> toReturn = new ArrayList<String>();
		Package packageOfTestClass = getTestCasePackage();
		if (packageOfTestClass == null) {
			return toReturn;
		}
		
		String[] splitResult = getTestCasePackage().getName().split("\\.");
		
		for (String string : splitResult) {
			toReturn.add(string);
		}
		return toReturn;
	}
	
	public void setTestData(final SysNatTestData data) {
		testDataSets = data;
	}
	
	public SysNatTestData getTestData() {
		return testDataSets;
	}
	
	public String getTestDataValue(final String origValueCandidate) 
	{
		String valueCandidate = origValueCandidate;
		if (valueCandidate.startsWith(SysNatConstants.ENVIRONMENT_SPECIFIC_TEST_VALUE) ) 
		{
			String env = executionInfo.getDisplayName(executionInfo.getTestEnvironmentName(), executionInfo.getTestApplication());
			valueCandidate = valueCandidate.replace(SysNatConstants.ENVIRONMENT_SPECIFIC_TEST_VALUE, env);
			valueCandidate = SysNatConstants.DC + SysNatConstants.DC + valueCandidate;
		}

		if ( ! valueCandidate.contains(SysNatConstants.DC) ) {
			// valueCandidate is a hard coded value no fieldname reference
			return replaceDatePlaceHolder(valueCandidate);
		}

		String valueReference = valueCandidate;
		if (valueReference.startsWith(SysNatConstants.DC)) 
		{
			String fieldName = valueReference.substring(SysNatConstants.DC.length());
			String toReturn = testDataSets.findValueForValueReference(fieldName);
			if (toReturn == null) 
			{
				 List<String> result = testDataSets.getSynonmysFor(fieldName);
				 for (String alternativFieldName : result) 
				 {
					 toReturn = testDataSets.findValueForValueReference(alternativFieldName);
					 if (toReturn != null) break;
				}
				if (! acceptMissingValues) {
					 throw new SysNatTestDataException("Für das Feld <b>" + fieldName + "</b> "
					 		                         + "steht kein Wert in den angegebenen Testdaten zur Verfügung!");
				}
			}
			return replaceDatePlaceHolder(toReturn);
		}
		
		String[] splitResult = valueReference.split(SysNatConstants.DC);
		
		if (splitResult.length != 2) {
			throw new SysNatTestDataException("Cannot parse reference to value: " + valueReference);
		}

		String datasetName = testDataSets.findDatasetNameForDatasetReference(splitResult[0]);
		String fieldName = splitResult[1];
		String toReturn = testDataSets.getValue(datasetName, fieldName);
		return replaceDatePlaceHolder(toReturn);
	}
	
	public String getTestDataValueNullSafe(final String valueCandidate) 
	{
		String value = getTestDataValue(valueCandidate);;
		if (value == null) {
			failWithMessage("For " + valueCandidate + " no value could by found in the defined TestData.");
		}
		return value;
	}	

	private String replaceDatePlaceHolder(String toReturn)
	{
		if (toReturn == null) {
			return null;
		}
		DateTime now = new DateTime();
		
		while (toReturn.contains("<heute>")) {
			toReturn = toReturn.replace("<heute>", SysNatDateUtil.toStringWithLeadingZerosIfNeeded(now.getDayOfMonth()) + "." 
                    + SysNatDateUtil.toStringWithLeadingZerosIfNeeded(now.getMonthOfYear()) + "." 
                    + now.getYear());
		}
		
		String searchPattern = "<heute";
		while (toReturn.contains(searchPattern)) 
		{
			int pos1 = toReturn.indexOf(searchPattern);
			String s = toReturn.substring(pos1 + searchPattern.length());
			int pos2 = s.indexOf("T>");
			
			if (pos2 == -1) return toReturn;
			String intAsString = s.substring(0, pos2);
			DateTime newDate = null;
			try {
				Integer numberOfDaysToShift = Integer.valueOf(intAsString);
				newDate = now.plusDays(numberOfDaysToShift);
			} catch (Exception e) {
				return toReturn;
			}
			
			pos2 = toReturn.indexOf(">");
			String toReplace = toReturn.substring(pos1, pos2+1);
			
			toReturn = toReturn.replace(toReplace, SysNatDateUtil.toStringWithLeadingZerosIfNeeded(newDate.getDayOfMonth()) + "." 
			                                       + SysNatDateUtil.toStringWithLeadingZerosIfNeeded(newDate.getMonthOfYear()) + "." 
					                               + newDate.getYear());
		}
		
		return toReturn;
	}
	
	public boolean isTextCurrentlyDisplayed(String text) {
		return getActiveGuiController().isTextCurrentlyDisplayed(text);
	}	
	
	public Hashtable<String, Properties> importTestData(final String testdata) 
	{
		final Hashtable<String, Properties> loadedDatasets;
		
		if (testdata.contains(SysNatConstants.LINE_SEPARATOR)) 
		{
			List<Properties> datasets = TableDataParser.doYourJob(getTestCaseFileName(), testdata);
			loadedDatasets = new Hashtable<>();
			for (int i = 0; i < datasets.size(); i++) {
				loadedDatasets.put("TestData" + (i+1), datasets.get(i));
			}
		} 
		else {
			loadedDatasets = getTestDataImporter().loadTestdata(testdata);
			reportMessages.add("The data file <b>" + testdata + "</b> has been imported "
					           + "with <b>" + loadedDatasets.size() + "</b> dataset(s).");
		}
		loadedDatasets.forEach( (datasetName, dataset) -> testDataSets.addDataset(datasetName, dataset));	
		
		return loadedDatasets;
	}
	
	public TestDataImporter getTestDataImporter() 
	{
		if (testDataImporter == null) {
			testDataImporter = new TestDataImporter(executionInfo.getTestdataDir());
		}
		return testDataImporter;
	}

	public void addLinewiseToReportMessageAsComment(String toReport) 
	{
		String[] splitResult = toReport.split(System.getProperty("line.separator"));
		addLinewiseToReportMessageAsComment(Arrays.asList(splitResult));
	}

	public void addLinewiseToReportMessageAsComment(List<String> commentLines) 
	{
		for (String line : commentLines) {
			addCommentToReport(line);
		}
	}

	public void addCommentToReport(String commentLine) {
		addReportMessage("//" + commentLine);
	}
	
	public List<String> executeCommandAndListOutput(String command) 
	{
		List<String> toReturn = new ArrayList<>();
		try {
			Process p = Runtime.getRuntime().exec("cmd /c " + command);
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";			
			while ((line = reader.readLine())!= null) {
				toReturn.add(line);
			}
			p.waitFor();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return toReturn;
	}
	
	public void setScriptToExecute(String scriptName) {
		this.scriptToExecute = scriptName;
	}

	public String getScriptToExecute() {
		return scriptToExecute;
	}

	public void setBddKeyword(String bddKeyword) {
		this.bddKeyword = bddKeyword;
	}


	public void storeTestObject(String name, Object o) {
		testObjects.put(name.toUpperCase(), o);
	}

	public Object getTestObject(String name) {
		Object toReturn = testObjects.get(name.toUpperCase());
		if (toReturn == null)
		{
			toReturn = "";
		}
		return toReturn;
	}

	public HashMap<String, Object> getTestObjects() {
		return testObjects;
	}

	public boolean doesTestObjectExist(String objectName) {
		return getTestObject(objectName) != null;
	}
	
	public void setTestObjects(HashMap<String, Object> hashMap) {
		testObjects = hashMap;
	}

	public void setBehaviorID(String aBehaviourID) 
	{
		behaviourID = aBehaviourID;
		if (bddKeyword != null && ! bddKeyword.isEmpty() ) {
			executionInfo.addToKnownFeatures(aBehaviourID);
		}
	}
	
	public String getBehaviorID() {
		return behaviourID;
	}
	
	
	public List<String> getNlxxFilePathAsList() {
		return new ArrayList<>();  // to be overwritten !
	}
    
    public PageObject getCurrentPage() {
		return currentPage;
	}
    
	public void setCurrentPage(PageObject currentPage) {
		this.currentPage = currentPage;
	}
}