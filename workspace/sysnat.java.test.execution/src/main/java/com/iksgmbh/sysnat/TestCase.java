package com.iksgmbh.sysnat;

import static com.iksgmbh.sysnat.utils.SysNatConstants.REPORT_FILENAME_TEMPLATE;
import static com.iksgmbh.sysnat.utils.SysNatLocaleConstants.CATEGORY_BILDNACHWEIS;
import static com.iksgmbh.sysnat.utils.SysNatLocaleConstants.ERROR_KEYWORD;
import static com.iksgmbh.sysnat.utils.SysNatLocaleConstants.NO_KEYWORD;
import static com.iksgmbh.sysnat.utils.SysNatLocaleConstants.PICTURE_PROOF;
import static com.iksgmbh.sysnat.utils.SysNatLocaleConstants.YES_KEYWORD;
import static org.junit.Assert.fail;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.openqa.selenium.NoSuchElementException;

import com.iksgmbh.sysnat.domain.SysNatTestData;
import com.iksgmbh.sysnat.domain.SysNatTestData.ObjectData;
import com.iksgmbh.sysnat.exception.SkipTestCaseException.SkipReason;
import com.iksgmbh.sysnat.exception.TestDataException;
import com.iksgmbh.sysnat.exception.UnexpectedResultException;
import com.iksgmbh.sysnat.exception.UnsupportedGuiEventException;
import com.iksgmbh.sysnat.guicontrol.GuiControl;
import com.iksgmbh.sysnat.guicontrol.SeleniumGuiController;
import com.iksgmbh.sysnat.helper.PopupHandler;
import com.iksgmbh.sysnat.helper.ReportCreator;
import com.iksgmbh.sysnat.helper.WindowHelper;
import com.iksgmbh.sysnat.language_templates.LanguageTemplates;
import com.iksgmbh.sysnat.language_templates.helloworldspringboot.LanguageTemplatesHelloWorldSpringBootBasics;
import com.iksgmbh.sysnat.language_templates.iksonline.LanguageTemplatesIksOnlineBasics;
import com.iksgmbh.sysnat.utils.SysNatConstants;
import com.iksgmbh.sysnat.utils.SysNatConstants.AppUnderTest;
import com.iksgmbh.sysnat.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.utils.SysNatStringUtil;

/**
 * Mother of all NatSpec test classes in this project
 * and referenced in "_NatSpecTemplate.java".
 * 
 * All technical but technological unspecific stuff belongs in here.
 * 
 * @author Reik Oberrath
 */
abstract public class TestCase
{
	public static final String SMILEY_FAILED = "&#x1F61E;";
	public static final String TEST_DATA_DIR = "testData";
	
	private static final String SMILEY_WRONG = "&#x1F612;";
	private static final String SMILEY_OK = "&#x1F60A;";
	private static final String YES = YES_KEYWORD + " " + SMILEY_OK;
	private static final String NO = NO_KEYWORD + " " + SMILEY_WRONG;

    protected ExecutionInfo executionInfo = ExecutionInfo.getInstance();
    protected SysNatTestData testDataSets = new SysNatTestData(); 	// Container for data used within a test or script
    
    private List<String> testCategories;
    private String testID = null;
	private boolean skipped = false;
	private boolean alreadyTerminated = false;
	private GuiControl guiController;
	private DateTime startDate = DateTime.now();
	private List<String> reportMessages = new ArrayList<>(); 
	
    abstract public void executeTestCase();
    abstract public String getTestCaseFileName();
    abstract public Package getTestCasePackage();
    abstract public boolean doesTestBelongToApplicationUnderTest();
    
	public void setUp() 
	{
		if (executionInfo.getGuiController() == null) 
		{
			setGuiController(new SeleniumGuiController());
			executionInfo.setGuiController( getGuiController() );
			initShutDownHook();
			login();
			PopupHandler.setTestCase(this);
		} else {
			setGuiController(executionInfo.getGuiController());
		}
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

	public void setTestID(String aTestId) {
		this.testID = aTestId;
	}
	
	public String getTestID() {
		return testID;
	}
    
	public void addReportMessage(String message) {
		reportMessages.add(message);
	}

	public void terminateTestCase(String message) 
	{
		if (! alreadyTerminated) {
			alreadyTerminated = true;
			fail( message );
		}
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
            scrFile = getGuiController().takeScreenShot();
        } catch (Exception e) {
            System.out.println("Error taking screenshot: " + e.getMessage());
            return;
        }

        try {
            FileUtils.copyFile(scrFile, new File(executionInfo.getScreenShotDir() + File.separator + filename));
        } catch (Exception e) {
        	System.out.println("Error saving screenshot: " + e.getMessage());
        } finally {
            if (scrFile != null) {
                FileUtils.deleteQuietly(scrFile);
            }
        }
    }
	
	public void closeCurrentTestCaseWithSuccess() 
	{
		if (reportMessages != null)  {
			System.out.println("Test Result: OK");
			executionInfo.addTestMessagesOK(getCheckedTestId(), reportMessages);
		}
	}

	public void terminateWrongTestCase() {
		System.out.println("Test Result: Failed Assertion");
		executionInfo.addTestMessagesWRONG(getCheckedTestId(), reportMessages);
	}

	public void finishSkippedTestCase(SkipReason skipReason) 
	{
		skipped = true;
		testID = null;
		reportMessages.clear();
		if (skipReason == SkipReason.ACTIVATION_STATE) {
			System.out.println("Skipped because set inactive.");
		}
		
		if (skipReason == SkipReason.CATEGORY_FILTER) {
			System.out.println("Skipped due to current category filter(s) " + executionInfo.getTestCategoriesToExecute());
		}
		
		if (skipReason == SkipReason.APPLICATION_TO_TEST) {
			System.out.println("Skipped due to current test application: " + executionInfo.getAppUnderTest().name());
		}	
	}
	
	public void failWithMessage(String message) 
	{
		if (executionInfo.isTestIdAlreadyUsed(getCheckedTestId())) {
			return;
		}
		if ( ! message.contains(ERROR_KEYWORD) )  {
			message = ERROR_KEYWORD + ": " + message;
		}
		System.out.println("Test Result: Technical Error");
		reportMessages.add(message);
		executionInfo.addTestMessagesFAILED(getCheckedTestId(), reportMessages);
		terminateTestCase(message);
	}

	protected LanguageTemplates getApplicationSpecificLangaugeTemplates() 
    {
    	final AppUnderTest appUnderTest = executionInfo.getAppUnderTest();
    	
    	if (appUnderTest == AppUnderTest.IksOnline) {
    		return (LanguageTemplates) new LanguageTemplatesIksOnlineBasics(this);
    	} else if (appUnderTest == AppUnderTest.HelloWorldSpringBoot) {
    		return (LanguageTemplates) new LanguageTemplatesHelloWorldSpringBootBasics(this);
    	} else {    		
    		String errorMessage = "For TestApplication '" + executionInfo.getAppUnderTest() 
    		+ "' there is not yet supported in TestCase.getApplicationSpecificLangaugeTemplates().";
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

	
    public void inputText(String fieldName, String value) {
    	getGuiController().insertText(value, fieldName);
    }
    
    public void inputEmail(String fieldName, String emailAdress) {
    	getGuiController().inputEmail(fieldName, emailAdress);
    }

	public void clickTableCellLink(String xPathToCell) {
		((SeleniumGuiController)getGuiController()).clickTableCellLink(xPathToCell);
		
	}
    
    public void clickButton(String id) {
    	clickElement(id);
    }
    
    public void clickButton(String id, int timeOutInSeconds) {
    	clickElement(id, timeOutInSeconds);
    }
    

    /**
     * Assumes that the text referenced by xpath consists of lines of text separated by '\n'
     * @param xpath
     * @return number of lines
     */
    public int getNumberOfSelectableEntries(String xpath)  {
    	return getGuiController().getNumberOfLinesInTextArea(xpath);
    }

    public void selectEntry(String xpath, int index)  {
    	getGuiController().selectComboboxEntry(xpath, index);
    }

    public void selectSuitableConditionAndCalculate()
    {
        int numberEntries = getNumberOfSelectableEntries("id('setCondition')");
        boolean ok = false;
        int optionIndex = -1;
        do  {
            optionIndex++;
            selectEntry("id('setCondition')", optionIndex);
            clickButton("calculate"); //  Berechnen
            try {
                getGuiController().getText("//tbody[@id='calc_results']/tr[1]/td[1]");
                ok = true;
            } catch (NoSuchElementException e) {
                ok = false;
                if (optionIndex > numberEntries) {
                    throw new RuntimeException("No suitable condition found.");
                }
            }
        } while (! ok);
    }

    public String getTextForId(String id) {
    	return getGuiController().getText(id);
    }

    public void clickLink(String id, String stepName) {
        clickLink(id);
    }
    
    private void login() 
    {
    	getGuiController().init(executionInfo.getTargetLoginUrl());

    	if (executionInfo.getAppUnderTest() == AppUnderTest.IksOnline) {    		
    		loginIntoIksOnline();
    	} else if (executionInfo.getAppUnderTest() == AppUnderTest.HelloWorldSpringBoot) {    		
        	loginIntoHelloWorld();
        } else {
    		String errorMessage = "Method TestCase.login() does not yet support a login for TestApplication '" 
                                  + executionInfo.getAppUnderTest() + "'.";
    		System.err.println(errorMessage);
    		throw new UnsupportedGuiEventException(errorMessage);
    	}
    }
    
	public void loginIntoIksOnline() {
		performStandardLogin();	
	}
	
	public void loginIntoHelloWorld() {
		performStandardLogin(); 
	}
	
	public void performStandardLogin() 
	{
		String username = executionInfo.getLoginDataForDefaultLogin("username");
		String pwd = executionInfo.getLoginDataForDefaultLogin("pw");
		
		try {
			LanguageTemplates languageTemplates = getApplicationSpecificLangaugeTemplates();
			languageTemplates.doLogin(username, pwd);
			executionInfo.setAlreadyLoggedIn( languageTemplates.isOverviewPageVisible() );  
		} catch (NoSuchElementException e) {
			executionInfo.setLoginFailed();
		}
	}
	
    public void clickLink(String id) {
        getGuiController().clickLink(id);
    }

    public void chooseFromComboBoxByIndex(String id, int index) {
    	getGuiController().selectComboboxEntry(id, index);
    }

    public void chooseFromComboBoxByValue(String id, String value) {
    	getGuiController().selectComboboxEntry(id, value);
    }
    
    public void clickElement(final String elementAsString, int timeoutInSeconds)
    {
    	String errMsg = getGuiController().clickElement(elementAsString, timeoutInSeconds);
		if (errMsg != null) {
			failWithMessage(errMsg);
		}
    }
    
    public void clickElement(final String elementAsString)
    {
    	String errMsg = getGuiController().clickElement(elementAsString);
		if (errMsg != null) {
			failWithMessage(errMsg);
		}
    }

    public boolean isElementReadyToUse(String elementId) {
    	return getGuiController().isElementReadyToUse(elementId);
    }
    
    public boolean isElementAvailable(String elementId) {
    	return getGuiController().isElementAvailable(elementId);
    }

	public int countNumberOfRowsInTable(String tableClass) {
		return getGuiController().getNumberOfRows(tableClass);
	}

	public int countNumberOfColumnsInTable(String tableClass) {
		return getGuiController().getNumberOfColumns(tableClass);
	}
	
	public void selectFromDropdown(String fieldName, String value) {
		getGuiController().selectComboboxEntry(fieldName, value);
	}

	public boolean isEntryInComboboxDropdownAvailable(String elementIdentifier, String value) {
		return getGuiController().isEntryInComboboxDropdownAvailable(elementIdentifier, value);
	}

	public void chooseOption(String elementIdentifier, int position) {
		getGuiController().selectRadioButton(elementIdentifier, position);
	}

	public void clickFirstButtonsOf(String... buttonIDs) 
	{
		RuntimeException excectionToThrow = null;
		for (String buttonID : buttonIDs) {
			try {			
				String errMsg = getGuiController().clickElement(buttonID);
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

	public boolean isCheckboxSelected(String checkboxId) 
	{
		return getGuiController().isSelected(checkboxId);
	}


	public void clickCheckbox(String checkboxId) 
	{
		String errMsg = getGuiController().clickElement(checkboxId);
		if (errMsg != null) {
			failWithMessage(errMsg);
		}
	}

	public boolean isTabActive(String tabId) {
		String selectedTabName = getSelectedTabName();
		return tabId.equals(selectedTabName);
	}
	
	public boolean isTabValid(String tabId) {
		return ((SeleniumGuiController) getGuiController()).isTabValid(tabId);
	}
	
	public boolean isErrorTab(String tabId) 
	{
		return ((SeleniumGuiController) getGuiController()).isErrorTab(tabId);
	}
	

	public String getTextForElement(String elementIdentifier) {
		return getGuiController().getText(elementIdentifier);
	}

	public void clickElementInTable(String tableIdentifier, int rowNumber, int columnNumber) {
		getGuiController().getTableCell(tableIdentifier, rowNumber, columnNumber);
	}

	public String getPageName() 
	{
		try {
			return "Antragserfassung - " + getSelectedTabName();
		} catch (Exception e) {
			// do nothing
		}
		
		try {
			return getTextForElement("h1"); 
		} catch (Exception e) {
			// do nothing
		}
		
		return "Unbekannt";
	}

	public void clickTab(String tabIdentifier, String tabName) {
		getGuiController().clickTab(tabIdentifier, tabName);
	}
	
	private String getSelectedTabName() {
		return getGuiController().getSelectedTabName();
	}
	
	public boolean isFailedLoginLabelVisible() {
		return getGuiController().isElementAvailable("validationErrors");
	}
	public void waitUntilElementIsAvailable(String elementIdentifier) {
		getGuiController().waitUntilElementIsAvailable(elementIdentifier);
		
	}
	
	public void clickMenuHeader(String elementIndentifier) 
	{
		String errMsg = getGuiController().clickElement(elementIndentifier);
		if (errMsg != null) {
			failWithMessage(errMsg);
		}
	}
	
	public void closeCurrentTab() {
		((SeleniumGuiController)getGuiController()).closeCurrentTab();
	}
	
	public void downloadContractFor(String printVariant) 
	{
		getGuiController().clickElement("contractLinkRow", printVariant);
		sleep(100);
		waitUntilPdfGenerationIsFinished();
		if (getGuiController().getNumberOfOpenTabs() == 2)  // PDF is shown in browser tab, now download it
		{
			int numberOfPDFs = SysNatFileUtil.findDownloadFiles("PDF").size();
			getGuiController().getCurrentlyActiveWindowTitle();
			startDownload();  // does not work always - reason unclear
			SysNatFileUtil.waitUntilNumberOfPdfFilesIs(numberOfPDFs + 1);
			closeCurrentTab();
		} else {
			// do nothing, PDF has been directly saved in download dir
		}
	}
	
	private void waitUntilPdfGenerationIsFinished() 
	{
		getGuiController().switchToLastWindow();
		//System.err.println(guiController.getCurrentlyActiveWindowTitle());
		long startTime = new Date().getTime();
		boolean isDownloadButtonAvailable = getGuiController().isElementAvailable("download");
		int numberOfOpenTabs = getGuiController().getNumberOfOpenTabs();
	    int secondCounter = 0;
	    
	    while (numberOfOpenTabs == 2 && ! isDownloadButtonAvailable) 
	    {
	    	secondCounter++;
			isDownloadButtonAvailable = getGuiController().isElementAvailable("download");  // tries a second to find this element
			numberOfOpenTabs = getGuiController().getNumberOfOpenTabs();
	    	if (secondCounter > executionInfo.getDefaultPrintTimeout()) {
	    		addReportMessage("<b>Der Druckversuch wurde nach " + executionInfo.getDefaultPrintTimeout() + " Sekunden abgebrochen!</b>");
	    		return;
	    	}
	    }
    	
	    long waitPeriodInSeconds = (new Date().getTime() - startTime) / 1000;
	    addReportMessage("//Dieser Test musste " + waitPeriodInSeconds + " Sekunden auf den Druck warten.");
	}
	
	public void setTickToCheckboxIfPossible(String checkboxId) 
	{
		if ( getGuiController().isElementAvailable(checkboxId) 
			 && getGuiController().isElementReadyToUse(checkboxId) 
			 && isCheckboxSelected(checkboxId) ) 
		{
			clickCheckbox(checkboxId);
		}
	}
	
	public String[] getSelectableEntries(String id) {
		return getTextForId(id).split("\n");
	}
	
	public String getSelectedComboBoxEntry(String elementId) {
		return getGuiController().getSelectedComboBoxEntry(elementId);
	}
	
	public String getTableCell(String tableIndentifier, int rowNumber, int columnNumber) {
		return getGuiController().getTableCell(tableIndentifier, rowNumber, columnNumber);
	}
	
	public String getTableCell(String tableIndentifier, String contentOfFirstCellInRow, int columnNumber) 
	{
		int numberOfRows = getGuiController().getNumberOfRows(tableIndentifier);
		for (int rowNumber=1; rowNumber<=numberOfRows; rowNumber++) 
		{
			String cellContent = getGuiController().getTableCell(tableIndentifier, rowNumber, 1);
			if (cellContent.equals(contentOfFirstCellInRow))
			{
				return getGuiController().getTableCell(tableIndentifier, rowNumber, columnNumber);
			}
		}
		throw new UnsupportedGuiEventException("Tabelle <b>" + tableIndentifier + "</b> enthält "
				                               + "in der ersten Spalte keinen Eintrag für <b>" + contentOfFirstCellInRow + "</b>.");
	}
	
	
	//##########################################################################################
	//                       P R I V A T E    M E T H O D S
	//##########################################################################################


	private String getCheckedTestId() 
	{
		if (testID == null)  {
			testID = getTestCaseFileName();
			terminateTestCase("Für den Test " + testID + " wurde keine TestId angegeben!");
		}
		return testID;
	}

	private void initShutDownHook() 
	{
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() 
			{
				System.out.println(SysNatConstants.SYS_OUT_SEPARATOR);

		    	createReportHtml();
		    	
		    	try {
		    		if (getGuiController() != null)  {
		    			getGuiController().closeGUI();
		    		}
		    	} catch (Throwable e)  {
		    		// ignore 
		    	}
			}
		});
	}
	
	private void createReportHtml() 
	{
		String reportDir = System.getProperty("sysnat.report.dir");
		String timestamp = executionInfo.getStartPointOfTimeAsFileStringForFileName();
		String reportFileName = reportDir + "/" + REPORT_FILENAME_TEMPLATE.replace("TIME_PLACEHOLDER", timestamp);
		String report = ReportCreator.doYourJob();
		SysNatFileUtil.writeFile(reportFileName, report);
		launchHtmlReport( reportFileName );
	}
	
	private void launchHtmlReport(final String reportFileName) 
	{
	    Runtime rt = Runtime.getRuntime();
	    try {
	    	File reportFile = new File( reportFileName );
	    	String pathToHtml = reportFile.getCanonicalPath();
	        String pathToFirefoxExe = System.getProperty("relative.path.to.firefox.root.dir");
			rt.exec(new String[]{ pathToFirefoxExe + "//firefox.exe","-url", "file:///" + pathToHtml });
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	public String buildTestIdFromNatSpecFileName() 
	{
		String fileName = getTestCaseFileName();
//		int pos = fileName.indexOf("_");   Warum das???
//		if (pos == -1) pos = 0; 
//		
//		return fileName.substring(pos);
		
		return fileName;
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
		return ((SeleniumGuiController)getGuiController()).getNumberOfBrowserWindows();
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
	
	public void startDownload() 
	{
		int numberOfAvailableWindows1 = WindowHelper.getNumberOfAvailableWindows();
		((SeleniumGuiController)getGuiController()).clickDownloadButton();
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

	private void sendPressTabEvent() {
		try {
			Robot robot = new Robot();
			robot.keyPress(KeyEvent.VK_TAB);
			robot.keyRelease(KeyEvent.VK_TAB);
		} catch (AWTException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void sendPressEnterEvent() {
		try {
			Robot robot = new Robot();
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);
		} catch (AWTException e) {
			throw new RuntimeException(e);
		}
	}

	private void sendPressSpaceEvent() {
		try {
			Robot robot = new Robot();
			robot.keyPress(KeyEvent.VK_SPACE);
			robot.keyRelease(KeyEvent.VK_SPACE);
		} catch (AWTException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void sendPressTabBackwardsEvent() {
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
	
	public String getScreenshotName() 
	{
		final String fileNamePrefix = getTestCaseFileName() + PICTURE_PROOF;
		final int numberOfExistingBildnachweise = SysNatFileUtil.getNumberOfFilesStartingWith(fileNamePrefix);
		return fileNamePrefix + (numberOfExistingBildnachweise + 1);
	}
	
	public boolean hasBildnachweisCategory() 
	{
		for (String category : getTestCategories()) {
			if (CATEGORY_BILDNACHWEIS.equals(category)) {
				return true;
			}
		}
		
		return false;
	}
	
	public List<String> buildTestCategories(String testCategoriesOfThisTestCase) 
	{
		setTestCategories(SysNatStringUtil.getTestCategoriesAsList(testCategoriesOfThisTestCase, getTestID()));
		return getTestCategories();
	}
	
	public List<String> getTestCategories() {
		return testCategories;
	}
	
	public void setTestCategories(List<String> testCategories) {
		this.testCategories = testCategories;
	}
	
	public GuiControl getGuiController() {
		return guiController;
	}
	
	public void setGuiController(GuiControl guiController) {
		this.guiController = guiController;
	}
	public String getTagName(String elementIdentifier) {
		return guiController.getTagName(elementIdentifier);
	}
		
	public List<String> getPackageNames() 
	{
		final List<String> list = new ArrayList<String>();
		String[] splitResult = getTestCasePackage().getName().split("\\.");
		for (String string : splitResult) {
			list.add(string);
		}
		return list;
	}
	
	
	public void buildTestDataFor(String datasetType, final List<?> datasets)  
	{
		int i = 0;
		for (Object dataset : datasets) 
		{
			i++;
			String aObjectName = datasetType + "_" + i;
	        ObjectData objectData = new ObjectData(aObjectName);
	        addAttributesAsProperties(objectData, dataset);
			testDataSets.addObjectData(aObjectName , objectData);
		}
	}
	
	private void addAttributesAsProperties(ObjectData objectData, Object dataset) 
	{
		String s = dataset.toString();
		int pos = s.indexOf('[');
		s = s.substring(pos+1, s.length()-1);
		String[] splitResult = s.split(",");
		for (String element : splitResult) {
			String[] parts = element.split("=");
			objectData.put(SysNatStringUtil.firstCharTuUpper(parts[0].trim()), parts[1].trim());
		}
	}
	
	/**
	 * Returns test data for domain object specified by aObjectName.
	 * Do not cache test, because they may be modified
	 * by one test case and afterwards do not match the settings
	 * for another test case.
	 */
	public ObjectData buildTestDataFor(final String aObjectName)  
	{
		String fileName = aObjectName;
		if (! aObjectName.endsWith(".dat")) {			
			fileName = aObjectName + ".dat"; 
		}
        String testDataDirName = TEST_DATA_DIR + "/" + executionInfo.getAppUnderTest().name();
        File testDataDir = new File(testDataDirName);
		File file = new File(testDataDir, fileName);
        if ( ! file.exists() ) {
    		String errorMessage = "Für <b>" + aObjectName + "</b> sind keine Testdaten erzeugt worden. "  
					  + "Die Datei <b>" + fileName + "</b> wurde im Verzeichnis"
					  + "<b>" + testDataDir.getAbsolutePath() + "</b> nicht gefunden.";
    		throw new TestDataException(errorMessage);
        }
        
        ObjectData objectData = new ObjectData(aObjectName);
        SysNatFileUtil.loadPropertyFile(file, objectData);
        testDataSets.addObjectData(aObjectName, objectData);
        
        return testDataSets.getObjectData(aObjectName);
	}
	
	
	public void setTestDataSets(SysNatTestData data) {
		testDataSets = data;
	}
	
	public SysNatTestData getTestDataSets() {
		return testDataSets;
	}
}
