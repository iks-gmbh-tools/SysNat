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

import static com.iksgmbh.sysnat.common.utils.SysNatConstants.NO_FILTER;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TimeZone;

import org.joda.time.DateTime;

import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.BrowserType;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.TargetEnv;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.domain.TestApplication;
import com.iksgmbh.sysnat.guicontrol.GuiControl;
import com.iksgmbh.sysnat.helper.VirtualTestCase;

/**
 * Stores global information on tests and their execution.
 * 
 * Avoid test case specific stuff here!
 * 
 * @author Reik Oberrath
 */
public class ExecutionRuntimeInfo 
{
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/Constants", Locale.getDefault());

	public static final String PROPERTIES_PATH = "../sysnat.test.runtime.environment/src/main/resources/execution_properties";
	private static final Hashtable<String, String> sysNatProperties = new Hashtable<>();
	
	// common technical settings
	public static final String PROPERTIES_FILENAME = "execution.properties"; 
	
	// user settings
	public static final String CONFIG_FILE_NAME = "../sysnat.natural.language.executable.examples/settings.config";

	// the following two constants define the behaviour for searching GUI-elements
	private static final int DEFAULT_MILLIS_TO_WAIT_FOR_AVAILABILITY_CHECK = 100;
	private static final int DEFAULT_SECS_TO_WAIT_FOR_GUI_ELEMENT_TIMEOUT = 1;
	private static final int DEFAULT_SECS_TO_WAIT_FOR_PRINT_TIMEOUT = 60;

	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
	private static final SimpleDateFormat TODAY_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	private static ExecutionRuntimeInfo instance;

	private String screenShotDir;
	private String osName;
	protected TargetEnv targetEnvironment;

	private HashMap<String, List<String>> reportMessagesOK = new HashMap<>();
	private HashMap<String, List<String>> reportMessagesWRONG = new HashMap<>(); // assertion failed (fachliches Problem)
	private HashMap<String, List<String>> reportMessagesFAILED = new HashMap<>(); // excecption thrown (technisches Problem)
	private HashMap<String, Integer> testCategoryCollection = new HashMap<>(); // categories defined by the natspec files
	private HashMap<String, TestStatistics> testStatistics = new HashMap<>();     

	private String testCategories;
	private List<String> testCategoriesToExecute;
	private List<String> inactiveXXIDs = new ArrayList<>();
	private File reportFolder;
	
	private int totalNumberOfTestCases = 0; // known for the given application/product under test
	private int numberOfAllExecutedTestCases = 0;
	private boolean alreadyLoggedIn = false;
	private DateTime startPointOfTime = new DateTime();
	private BrowserType browserTypeToUse;
	private GuiControl guiController;
	private boolean loginOk = true;
	private Locale locale;
	private boolean applicationStarted;
	private boolean archiveResults;
	private boolean settingsOk;


	public static ExecutionRuntimeInfo getInstance() 
	{
		if (instance == null) {
			System.out.println("SysNatTesting v" + SysNatConstants.SYS_NAT_VERSION 
					           + " test environment initialisation...");
			instance = new ExecutionRuntimeInfo();
		}
		return instance;
	}

	protected ExecutionRuntimeInfo() 
	{
		// load test properties
		addToSystemProperties(getExecutionPropertiesAsString());
		addToSystemProperties(getSettingsConfigAsString());
		addToSystemProperties(getPropertiesPath() + "/" + getTestApplicationName() + ".properties");

		try {
			browserTypeToUse = BrowserType.valueOf(getSystemProperty(BUNDLE.getString("BROWSER_SETTING_KEY")).toUpperCase());
		} catch (Exception e) {
			System.err.println("Unknown Browser in config.settings: " + getSystemProperty("Browser"));
		}

		try {
			targetEnvironment = TargetEnv.valueOf(getSystemProperty(BUNDLE.getString("ENVIRONMENT_SETTING_KEY")).toUpperCase());
		} catch (Exception e) {
			System.err.println("Unknown Environment in config.settings: " + getSystemProperty("Environment"));
		}

		testCategories = getSystemProperty(BUNDLE.getString("FILTER_CATEGORIES_TO_EXECUTE"));
		testCategoriesToExecute = SysNatStringUtil.getTestCategoriesAsList(testCategories, "");
		assertPositiveCategoriesInFrontOfNegativeOnes(testCategoriesToExecute);
		if (testCategories.length() == 0) {
			testCategories = NO_FILTER;
		}

		osName = getOsName();
		screenShotDir = System.getProperty("screenshot.dir", "screenshots");

		System.out.println("Starting " + getTestApplicationName() + " on " + targetEnvironment + " at "
				+ getStartPointOfTimeAsFileStringForFileName() + "...");
	}
	
	void assertPositiveCategoriesInFrontOfNegativeOnes(List<String> testCategories) 
	{
		int indexOfLastPositiveCategory = 0;
		int indexOfFirstNegativeCategory = 0;
		int indexCounter = 0;

		for (String category : testCategories) {
			if (category.startsWith(SysNatLocaleConstants.NON_KEYWORD)) {
				if (indexOfFirstNegativeCategory < indexCounter) {
					indexOfFirstNegativeCategory = indexCounter;
				}
			} else {
				if (indexOfLastPositiveCategory > indexCounter) {
					indexOfLastPositiveCategory = indexCounter;
				}

			}
			indexCounter++;
		}

		if (indexOfLastPositiveCategory > indexOfFirstNegativeCategory) {
			throw new RuntimeException("Negative categories must not be defined before positive ones.");
		}
	}

	private String getSystemProperty(String key) {
		String toReturn = System.getProperty(key);
		if (toReturn == null) {
			handleException("Unknown system property '" + key + "'!");
		}
		return toReturn.trim();
	}

	private void handleException(String errorMessage) {
		System.err.println(errorMessage);
		throw new SysNatException(errorMessage);
	}

	private String getExecutionProperty(String propertyEnding) 
	{
		if (targetEnvironment == null) {
			throw new RuntimeException(
					"Die property 'Zielumgebung' wurde in der Datei settings.config nicht gesetzt oder ist nicht gültig.");
		}

		return getTestApplicationNameAsPropertyKey() + "." 
				+ targetEnvironment.name().toLowerCase() + "." + propertyEnding;
	}
	
	public String getTestApplicationNameAsPropertyKey() {
		return getTestApplicationName().toLowerCase().replaceAll("_", "");
	}

	public String getStartPointOfTimeAsFileStringForFileName() {
		return getStartPointOfTimeAsString().replaceFirst(":", "h ").replaceFirst(":", "min ") + "sec";
	}

	public String getStartPointOfTimeAsString() {
		return DATE_FORMAT.format(startPointOfTime.toDate());
	}

	public Date getStartPointOfTime() {
		return startPointOfTime.toDate();
	}

	public void countExcecutedTestCase() {
		numberOfAllExecutedTestCases++;
	}

	public void countTestCase() {
		totalNumberOfTestCases++;
	}

	public void addInactiveTestCase(String inactiveXXId) {
		inactiveXXIDs.add(inactiveXXId);
	}

	public void addTestMessagesOK(String xxid, List<String> messagesOK) {
		reportMessagesOK.put(xxid, messagesOK);
	}

	public void addTestMessagesWRONG(String xxid, List<String> messagesWRONG) {
		reportMessagesWRONG.put(xxid, messagesWRONG);
	}

	public void addTestMessagesFAILED(String xxid, List<String> messagesFAILED) {
		reportMessagesFAILED.put(xxid, messagesFAILED);
	}

	public List<String> getTestCategoriesToExecute() {
		return testCategoriesToExecute;
	}

	public void setTestCategoriesToExecute(List<String> testCategoriesToExecute) {
		this.testCategoriesToExecute = testCategoriesToExecute;
	}

	public boolean isAlreadyLoggedIn() {
		return alreadyLoggedIn;
	}

	public void setAlreadyLoggedIn(boolean alreadyLoggedIn) {
		this.alreadyLoggedIn = alreadyLoggedIn;
	}

	public int getTotalNumberOfTestCases() {
		return totalNumberOfTestCases;
	}

	public int getNumberOfAllExecutedTestCases() {
		return numberOfAllExecutedTestCases;
	}

	public void uncountAsExecutedTestCases() {
		numberOfAllExecutedTestCases--;
	}

	private void addToSystemProperties(final String propertiesFilename) 
	{
		System.out.println("Properties filename in use: " + propertiesFilename);

		final File f = new File(propertiesFilename);
		if ( ! f.exists() ) {
			RuntimeException e = new RuntimeException("The following necessary file is missing: " + f.getAbsolutePath());
			e.printStackTrace();
			throw e;
		}

		final Properties properties = new Properties();
		SysNatFileUtil.loadPropertyFile(f, properties);

		for (Object key : properties.keySet()) {
			setSysNatSystemProperty((String) key, properties.getProperty((String) key));
		}

	}

	public static void setSysNatSystemProperty(String key, String value) 
	{
		if (System.getProperty(key.toString()) == null) 
		{
			// add only if not yet present
			System.setProperty(key, value);
			sysNatProperties.put(key, value);
		}
	}

	public String getInactiveTestListAsString() {
		if (inactiveXXIDs.size() == 0) {
			return "-";
		}

		final StringBuffer sb = new StringBuffer();
		for (String xxid : inactiveXXIDs) {
			sb.append(xxid).append(", ");
		}

		String toReturn = sb.toString();
		return toReturn.substring(0, toReturn.length() - 2);
	}

	public boolean isXXIdAlreadyUsed(String xxid) {
		return reportMessagesOK.containsKey(xxid) 
			   || reportMessagesFAILED.containsKey(xxid)
			   || reportMessagesWRONG.containsKey(xxid)
			   || inactiveXXIDs.contains(xxid);
	}

	public HashMap<String, List<String>> getReportMessagesOK() {
		return reportMessagesOK;
	}

	public HashMap<String, List<String>> getReportMessagesWRONG() {
		return reportMessagesWRONG;
	}

	public HashMap<String, List<String>> getReportMessagesFAILED() {
		return reportMessagesFAILED;
	}

	public int getNumberOfInactiveTests() {
		return inactiveXXIDs.size();
	}

	public String getTestCategories() {
		return testCategories;
	}

	public String getExecutionDurationAsString() {
		DateTime endPointOfTime = new DateTime();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(endPointOfTime.toDate().getTime() - startPointOfTime.toDate().getTime());
	}

	public String getOsName() {
		String osname = osName;
		if (osname == null) {
			osname = System.getProperty("os.name");
			// System.out.println("Detected Operating System: " + osname);
		}

		return osname;
	}

	public String getScreenShotDir() {
		return screenShotDir;
	}

	public BrowserType getBrowserTypeToUse() {
		return browserTypeToUse;
	}

	public static void reset() {
		instance = null;
		clearSysNatProperties();
		System.clearProperty("execution.properties");
		System.clearProperty(BUNDLE.getString("TESTAPP_SETTING_KEY"));
		System.clearProperty("sysnat.properties.path");
		System.clearProperty("settings.config");
	}

	protected static void clearSysNatProperties() {
		sysNatProperties.forEach( (key,value) -> System.clearProperty(key) );
		sysNatProperties.clear();
	}

	public String getLoginDataForDefaultLogin(final String loginDataItem) 
	{
		final String propertyKey = getExecutionProperty("login." + loginDataItem).toLowerCase();
		final String toReturn = System.getProperty(propertyKey);

		if (toReturn == null) {
			String errorMessage = "Expected property '" + propertyKey + "' not found " + "in '" 
		           + getTestApplicationName() + ".properties' !";
			System.err.println(errorMessage);
			throw new RuntimeException(errorMessage);
		}

		return toReturn.trim();
	}

	public GuiControl getGuiController() {
		return guiController;
	}

	public void setGuiController(GuiControl aGuiController) {
		guiController = aGuiController;
	}

	public String getTodayDateAsString() {
		return TODAY_FORMAT.format(new Date());
	}

	public TargetEnv getTargetEnv() {
		return targetEnvironment;
	}

	/**
	 * For test purpose only!
	 */
	public void setTestApplicationName(String name) {
		System.setProperty(BUNDLE.getString("TESTAPP_SETTING_KEY"), name);
	}

	public String getTestApplicationName() 
	{
		final String toReturn = System.getProperty(BUNDLE.getString("TESTAPP_SETTING_KEY"));

		if (toReturn == null) {
			throw new SysNatException("Appliacation under test not specified!");
		}
		
		return toReturn;
	}

	public String getExecutionSpeed() {
		return getSystemProperty(BUNDLE.getString("EXECUTION_SPEED_SETTING_KEY"));
	}

	public int getMilliesForWaitState() 
	{
		String executionSpeed = getExecutionSpeed();

		if ("verySlow".equals(executionSpeed)) {
			return 5000;
		}

		if ("slow".equals(executionSpeed)) {
			return 1500;
		}

		if ("fast".equals(executionSpeed)) {
			return DEFAULT_MILLIS_TO_WAIT_FOR_AVAILABILITY_CHECK;
		}

		//System.out.println("Unbekannte Ausführungsgeschwindigkeit: " + executionSpeed + " Default 'schnell' wird genommen.");
		return DEFAULT_MILLIS_TO_WAIT_FOR_AVAILABILITY_CHECK;
	}

	public int getDefaultGuiElementTimeout() {
		return DEFAULT_SECS_TO_WAIT_FOR_GUI_ELEMENT_TIMEOUT;
	}

	public int getDefaultPrintTimeout() {
		return DEFAULT_SECS_TO_WAIT_FOR_PRINT_TIMEOUT;
	}

	public void setLoginFailed() {
		loginOk = false;
	}

	public boolean getLoginOK() {
		return loginOk;
	}

	public boolean isOS_Windows() {
		return getOsName().startsWith("Windows");
	}

	public void addCategoryToCollection(String category) {
		Integer frequency = testCategoryCollection.get(category);

		if (frequency != null) {
			frequency = frequency + 1;
			testCategoryCollection.put(category, frequency);
		} else {
			testCategoryCollection.put(category, 1);
		}
	}

	public List<String> getCategoriesFromCollection() {
		return new ArrayList<>(testCategoryCollection.keySet());
	}

	public Locale getLocale() {
		return locale;
	}

	public boolean addToResultAsSeparateTestCase(VirtualTestCase virtualExecutableExample) {
		List<String> reportMessages = virtualExecutableExample.getReportMessages();
		for (String message : reportMessages) {
			if (message.contains(SysNatLocaleConstants.ERROR_KEYWORD)) {
				reportMessagesFAILED.put(virtualExecutableExample.getXXID(), virtualExecutableExample.getReportMessages());
				return false;
			}
			if (message.contains(SysNatConstants.QUESTION_IDENTIFIER)
					&& message.contains(SysNatLocaleConstants.NO_KEYWORD)) {
				reportMessagesWRONG.put(virtualExecutableExample.getXXID(), virtualExecutableExample.getReportMessages());
				return false;
			}
		}

		reportMessagesOK.put(virtualExecutableExample.getXXID(), virtualExecutableExample.getReportMessages());
		return true;
	}

	private String getSettingsConfigAsString() 
	{
		final String value = System.getProperty("settings.config", CONFIG_FILE_NAME);
		System.out.println("settings.config used: " + value);
		return value;
	}

	private String getExecutionPropertiesAsString() {
		return System.getProperty("execution.properties", getPropertiesPath() + "/" + PROPERTIES_FILENAME);
	}

	public String getPropertiesPath() {
		return System.getProperty("sysnat.properties.path", PROPERTIES_PATH);
	}

	public void setApplicationStarted(boolean applicationStarted) {
		this.applicationStarted = applicationStarted;
	}
	
	public boolean isApplicationStarted() {
		return applicationStarted;
	}

	public TestApplication getTestApplication() {
		final String applicationUnderTest = getTestApplicationName();
		return new TestApplication(applicationUnderTest);
	}

	public String getTestdataDir() {
		return System.getProperty("sysnat.testdata.import.directory") + "/" + getTestApplicationName();
	}
	
	public List<String> getContentOfConfigSettingsFile() 
	{
        File f = new File(CONFIG_FILE_NAME);
        if ( ! f.exists() ) {
            throw new RuntimeException("Folgende notwendige Datei wurde nicht gefunden: " + CONFIG_FILE_NAME);
        }
        String[] splitResult = SysNatFileUtil.readTextFileToString(f.getAbsolutePath()).split(System.getProperty("line.separator"));
        return Arrays.asList(splitResult);
	}

	public boolean areResultsToArchive() {
		return archiveResults;
	}

	public void setResultsToArchive(boolean value) {
		archiveResults = value;
	}
	
	public void setTargetEnv(String targetEnv) {
		if (TargetEnv.valueOf(targetEnv) != null)  {
			targetEnvironment = TargetEnv.valueOf(targetEnv);
			System.setProperty(BUNDLE.getString("ENVIRONMENT_SETTING_KEY"), targetEnv);
		} else {
			throw new RuntimeException("Unknown target environment");
		}
	}

	public String getReportName() 
	{
		String toReturn = System.getProperty(BUNDLE.getString("REPORT_NAME_SETTING_KEY")).trim();

		if (toReturn == null || toReturn.trim().length() == 0) {
			toReturn = buildDefaultReportName();
			System.setProperty(BUNDLE.getString("REPORT_NAME_SETTING_KEY"), toReturn);
		}
		
		if (toReturn.endsWith("-")) {
			toReturn = toReturn.substring(0, toReturn.length()-1);
		}
		
		return toReturn;
	}

	public String buildDefaultReportName() 
	{
		String testCategories = getTestCategories();
		if (testCategories.equals(NO_FILTER)) {
			testCategories = BUNDLE.getString("All");
		}
		return getTestApplicationName() + "-" 
	         + getTargetEnv().name() + "-"
	         + testCategories;
	}

	public String getArchiveDir() {
		return getSystemProperty(BUNDLE.getString("ARCHIVE_DIR_SETTING_KEY"));
	}

	public void setReportName(String value) {
		System.setProperty(BUNDLE.getString("REPORT_NAME_SETTING_KEY"), value);
	}

	public void setArchiveDir(String value) {
		System.setProperty(BUNDLE.getString("ARCHIVE_DIR_SETTING_KEY"), value);
	}

	public void setBrowserTypeToUse(String value) {
		browserTypeToUse = BrowserType.valueOf(value.toUpperCase());
		System.setProperty(BUNDLE.getString("BROWSER_SETTING_KEY"), value);
	}

	public void setExecSpeed(String value) {
		System.setProperty(BUNDLE.getString("EXECUTION_SPEED_SETTING_KEY"), value);
	}

	public boolean getUseSettingsDialog() {
		return System.getProperty("SettingsConfigDialog").equalsIgnoreCase("on") 
				|| System.getProperty("SettingsConfigDialog").equalsIgnoreCase("an") ;
	}

	
	public boolean areSettingsComplete() {
		return settingsOk;
	}

	public void setSettingsOk() {
		settingsOk = true;
	}

	public String getReportFolderAsString() {
		return System.getProperty("sysnat.report.dir") + "/" 
	           + getReportName() + " "
	           + getStartPointOfTimeAsFileStringForFileName();
	}

	public File getReportFolder() 
	{
		if (reportFolder == null) {
			reportFolder = SysNatFileUtil.createFolder( getReportFolderAsString() );
		}
		return reportFolder;
	}

	public void addTestStatistics(String xxid, TestStatistics statistics) {
		testStatistics.put(xxid, statistics);
	}

	public static class TestStatistics 
	{
		public TestStatistics(DateTime startTime, DateTime endTime) {
			startPointOfTime = DATE_FORMAT.format(startTime.toDate()); 
			duration = TIME_FORMAT.format(endTime.toDate().getTime() - startTime.toDate().getTime());
		}
		public String startPointOfTime;
		public String duration;
	}

	public String getTotalTimePassed() {
		return TIME_FORMAT.format(new Date().getTime() - startPointOfTime.getMillis());
	}

    public String getIntermediateResultLogText() 
    {
          int numTotal = getNumberOfAllExecutedTestCases();
          int numSuccess = getReportMessagesOK().size();
          
          if (numTotal == 0) {
                 return "No tests excecuted so far";
          }
          
          if (numTotal == 1) {
                 if (numSuccess == 0) {
                       return "One failed test excecuted so far";
                 } else {
                       return "One successful test excecuted so far";
                 }
          }
          
          return numTotal + " tests executed so far - of these " + getReportMessagesOK().size() + " successful";
    }
	
}