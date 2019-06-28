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

import jdk.nashorn.internal.objects.NativeJava;
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
	private static final ResourceBundle BUNDLE_EN = ResourceBundle.getBundle("bundles/Constants", Locale.ENGLISH);

	private static final Hashtable<String, String> sysNatProperties = new Hashtable<>();

	public static final String UNNAMED_XX_GROUP = "UNNAMED_XX_GROUP";
	public static final String PROPERTIES_PATH = "sources/sysnat.test.runtime.environment/src/main/resources/execution_properties";
	public static final String PROPERTIES_FILENAME = "execution.properties";
	public static final String CONFIG_FILE_NAME = "sources/sysnat.natural.language.executable.examples/settings.config";

	// the following two constants define the behaviour for searching GUI-elements
	private static final int DEFAULT_MILLIS_TO_WAIT_FOR_AVAILABILITY_CHECK = 100;
	private static final int DEFAULT_SECS_TO_WAIT_FOR_GUI_ELEMENT_TIMEOUT = 1;
	private static final int DEFAULT_SECS_TO_WAIT_FOR_PRINT_TIMEOUT = 60;

	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
	private static final SimpleDateFormat TODAY_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");


	private static ExecutionRuntimeInfo instance;

	private String osName;
	protected TargetEnv targetEnvironment;

	private HashMap<String, List<String>> reportMessagesOK = new HashMap<>();
	private HashMap<String, List<String>> reportMessagesWRONG = new HashMap<>(); // assertion failed (fachliches Problem)
	private HashMap<String, List<String>> reportMessagesFAILED = new HashMap<>(); // excecption thrown (technisches Problem)
	private HashMap<String, Integer> executionFilterMap = new HashMap<>(); // execution filter defined in the nlxx files
	private HashMap<String, TestStatistics> testStatistics = new HashMap<>();
	private HashMap<String, String> xxidBehaviourMap = new HashMap<>();   // stores for each XXID the named Behaviour if defined
	private List<String> knownFeatures = new ArrayList<>();   // stores behaviour with Feature-Keyword
	private List<String> executedNLFiles = new ArrayList<>();   // list of executed natural language files
	
	private String executionFilters;
	private List<String> executionFilterList;
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
	private boolean testEnvironmentInitialized = false;
	private boolean shutDownHookAdded;
	private HashMap<String, Integer> numberOfExistingXXPerGroupMap = new HashMap<>();
	private HashMap<String, Integer> numberOfExecutedXXPerGroupMap = new HashMap<>();

	public static ExecutionRuntimeInfo getInstance() {
		if (instance == null) {
			System.out.println("SysNat v" + SysNatConstants.SYS_NAT_VERSION
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
			targetEnvironment = TargetEnv.valueOf(getSystemProperty(BUNDLE_EN.getString("ENVIRONMENT_SETTING_KEY")).toUpperCase());
		}

		try {
			executionFilters = getSystemProperty(BUNDLE.getString("EXECUTION_FILTER"));
		} catch (Exception e) {
			executionFilters = getSystemProperty(BUNDLE_EN.getString("EXECUTION_FILTER"));
		}
		
		executionFilterList = SysNatStringUtil.getExecutionFilterAsList(executionFilters, "");
		assertPositiveFiltersInFrontOfNegativeOnes(executionFilterList);
		if (executionFilters.length() == 0) {
			executionFilters = NO_FILTER;
		}

		osName = getOsName();

		System.out.println("Starting " + getTestApplicationName() + " on " + targetEnvironment + " at "
				+ getStartPointOfTimeAsFileStringForFileName() + "...");
	}

	void assertPositiveFiltersInFrontOfNegativeOnes(List<String> filterList) {
		int indexOfLastPositiveFilter = 0;
		int indexOfFirstNegativeFilter = 0;
		int indexCounter = 0;

		for (String filter : filterList) {
			if (filter.startsWith(SysNatLocaleConstants.NON_KEYWORD)) {
				if (indexOfFirstNegativeFilter < indexCounter) {
					indexOfFirstNegativeFilter = indexCounter;
				}
			} else {
				if (indexOfLastPositiveFilter > indexCounter) {
					indexOfLastPositiveFilter = indexCounter;
				}

			}
			indexCounter++;
		}

		if (indexOfLastPositiveFilter > indexOfFirstNegativeFilter) {
			throw new RuntimeException("Negative execution filters must not be defined before positive ones.");
		}
	}

	private String getSystemProperty(String key) 
	{
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

	private String getExecutionProperty(String propertyEnding) {
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

	public void countTestCase(String behaviourId) 
	{
		if (behaviourId != null) {
			Integer oldFrequence = numberOfExecutedXXPerGroupMap.get(behaviourId);
			if (oldFrequence != null) {
				int newFrequence = oldFrequence + 1;
				numberOfExecutedXXPerGroupMap.put(behaviourId, Integer.valueOf(newFrequence));
			}
		}
		totalNumberOfTestCases++;
	}

	public void addInactiveTestCase(String inactiveXXId, String behaviourID) {
		inactiveXXIDs.add(inactiveXXId);
		addToBehaviourXXMapping(inactiveXXId, behaviourID);
	}

	public void addTestMessagesOK(String xxid, List<String> messagesOK, String behaviourID) 
	{
		reportMessagesOK.put(xxid, messagesOK);			
		addToBehaviourXXMapping(xxid, behaviourID);
	}

	public void addTestMessagesWRONG(String xxid, List<String> messagesWRONG, String behaviourID) 
	{
		reportMessagesWRONG.put(xxid, messagesWRONG);
		addToBehaviourXXMapping(xxid, behaviourID);
	}

	public void addTestMessagesFAILED(String xxid, List<String> messagesFAILED, String behaviourID) 
	{
		reportMessagesFAILED.put(xxid, messagesFAILED);
		addToBehaviourXXMapping(xxid, behaviourID);
	}

	private void addToBehaviourXXMapping(String xxid, String behaviourID) 
	{
		if (behaviourID == null || behaviourID.isEmpty()) {
			behaviourID = UNNAMED_XX_GROUP;
		}
		xxidBehaviourMap.put(xxid, behaviourID);
	}

	public List<String> getExecutionFilterList() {
		return executionFilterList;
	}
	
	public void setExecutionFilterString(String filter) {
		if (filter != null) {
			System.setProperty(BUNDLE.getString("EXECUTION_FILTER"), filter);
		}
	}

	public void setExecutionFilterList(List<String> filterList) {
		this.executionFilterList = filterList;
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
		if ( ! f.exists() ) 
		{
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
		if (System.getProperty(key.toString()) == null) {
			// add only if not yet present
			System.setProperty(key, value);
			sysNatProperties.put(key, value);
		}
	}

	public List<String> getInactiveXXIDs() {
		return inactiveXXIDs;
	}

	public boolean isXXIdAlreadyUsed(String xxid) {
		return reportMessagesOK.containsKey(xxid)
				|| reportMessagesFAILED.containsKey(xxid)
				|| reportMessagesWRONG.containsKey(xxid)
				|| inactiveXXIDs.contains(xxid);
	}

	
	public HashMap<String, String> getXXidBehaviourMap() {
		return xxidBehaviourMap;
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

	public String getFiltersToExecute() {
		return executionFilters;
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
		sysNatProperties.forEach((key, value) -> System.clearProperty(key));
		sysNatProperties.clear();
	}

	public String getLoginDataForDefaultLogin(final String loginDataItem) {
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
		String toReturn = System.getProperty(BUNDLE.getString("TESTAPP_SETTING_KEY"));

		if (toReturn == null) {
			toReturn = System.getProperty(BUNDLE_EN.getString("TESTAPP_SETTING_KEY"));
		}

		if (toReturn == null) {
			throw new SysNatException("Application under test not specified!");
		}

		return toReturn;
	}

	public String getExecutionSpeed() {
		try {
			return getSystemProperty(BUNDLE.getString("EXECUTION_SPEED_SETTING_KEY"));
		} catch (Exception e) {
			return getSystemProperty(BUNDLE_EN.getString("EXECUTION_SPEED_SETTING_KEY"));
		}
	}

	public int getMaxMilliesForWaitState() {
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

	public void addFilterToCollection(String filter) {
		Integer frequency = executionFilterMap.get(filter);

		if (frequency != null) {
			frequency = frequency + 1;
			executionFilterMap.put(filter, frequency);
		} else {
			executionFilterMap.put(filter, 1);
		}
	}

	public List<String> getExecutionFilterFromMap() {
		return new ArrayList<>(executionFilterMap.keySet());
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
		String defaultValue = SysNatFileUtil.findAbsoluteFilePath(CONFIG_FILE_NAME);
		String value = System.getProperty("settings.config", defaultValue);
		System.out.println("settings.config used: " + value);
		return value;
	}

	private String getExecutionPropertiesAsString() {
		String toReturn = System.getProperty("execution.properties", getPropertiesPath() + "/" + PROPERTIES_FILENAME);
		return SysNatFileUtil.findAbsoluteFilePath(toReturn);
	}

	public String getPropertiesPath()
	{
		String defaultValue = SysNatFileUtil.findAbsoluteFilePath(PROPERTIES_PATH);
		return System.getProperty("sysnat.properties.path", defaultValue);
	}

	public String getRootPath() {
		return System.getProperty("root.path");
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
		String settingsConfig = getSettingsConfigAsString();
		File f = new File(settingsConfig);
		if (!f.exists()) {
			throw new RuntimeException("Folgende notwendige Datei wurde nicht gefunden: " + settingsConfig);
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
		if (TargetEnv.valueOf(targetEnv) != null) {
			targetEnvironment = TargetEnv.valueOf(targetEnv);
			System.setProperty(BUNDLE.getString("ENVIRONMENT_SETTING_KEY"), targetEnv);
		} else {
			throw new RuntimeException("Unknown target environment");
		}
	}

	public String getReportName() {
		String toReturn;
		
		try {
			toReturn = System.getProperty(BUNDLE.getString("REPORT_NAME_SETTING_KEY")).trim();
		} catch (Exception e) {
			toReturn = System.getProperty(BUNDLE_EN.getString("REPORT_NAME_SETTING_KEY")).trim();
		}		

		if (toReturn == null || toReturn.trim().length() == 0) {
			toReturn = buildDefaultReportName();
			System.setProperty(BUNDLE.getString("REPORT_NAME_SETTING_KEY"), toReturn);
		}

		if (toReturn.endsWith("-")) {
			toReturn = toReturn.substring(0, toReturn.length() - 1);
		}

		return toReturn;
	}

	public String buildDefaultReportName() {
		String filters = getFiltersToExecute();
		if (filters.equals(NO_FILTER)) {
			filters = BUNDLE.getString("All");
		}
		return getTestApplicationName() + "-"
				+ getTargetEnv().name() + "-"
				+ filters;
	}

	public String getArchiveDir() {
		try {			
			return getSystemProperty(BUNDLE.getString("ARCHIVE_DIR_SETTING_KEY"));
		} catch (Exception e) {
			return getSystemProperty(BUNDLE_EN.getString("ARCHIVE_DIR_SETTING_KEY"));
		}
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
		return System.getProperty("Start_With_SettingsConfigDialog").equalsIgnoreCase("on")
				|| System.getProperty("Starte_Mit_SettingsConfigDialog").equalsIgnoreCase("an");
	}


	public boolean areSettingsComplete() {
		return settingsOk;
	}

	public void setSettingsOk() {
		settingsOk = true;
	}

	public String getReportFolderAsString() {
		String toReturn = System.getProperty("sysnat.report.dir");
		return SysNatFileUtil.findAbsoluteFilePath(toReturn) + "/"
				+ getReportName() + " "
				+ getStartPointOfTimeAsFileStringForFileName();
	}

	public File getReportFolder() {
		if (reportFolder == null) {
			reportFolder = SysNatFileUtil.createFolder(getReportFolderAsString());
		}
		return reportFolder;
	}

	public void addTestStatistics(String xxid, TestStatistics statistics) {
		testStatistics.put(xxid, statistics);
	}

	public static class TestStatistics {
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

	public String getIntermediateResultLogText() {
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
	
	public void addToKnownFeatures(String feature) {
		knownFeatures.add(feature);
	}
	
	public List<String> getKnownFeatures() {
		return knownFeatures;
	}

	public boolean isTestEnvironmentInitialized() {
		return testEnvironmentInitialized;
	}

	public void setTestEnvironmentInitialized() {
		this.testEnvironmentInitialized = true;
	}

	public void setShutDownHookAdded() {
		this.shutDownHookAdded = true;
	}

	public boolean isShutDownHookAdded() {
		return shutDownHookAdded;
	}

	/**
	 * Registers a group of XX (i.e. a Behavior or Feature) and counts the
	 * members of this group as indicator of how many XXs of this group
	 * are already executed.
	 * 
	 * @param behaviorId as id of XX group
	 * @param numberOfXXInGroup of XX belonging to group <behaviorId>
	 */
	public void register(String behaviorId, int numberOfXXInGroup) 
	{
		if (numberOfExistingXXPerGroupMap.get(behaviorId) == null) {
			numberOfExistingXXPerGroupMap.put(behaviorId, Integer.valueOf(numberOfXXInGroup));
			numberOfExecutedXXPerGroupMap.put(behaviorId, Integer.valueOf(1));
		} else {
			Integer xxCounter = numberOfExecutedXXPerGroupMap.get(behaviorId);
			numberOfExecutedXXPerGroupMap.put(behaviorId, Integer.valueOf( ++xxCounter ));
		}
	}

	public boolean isLastXXOfGroup(String behaviorId) 
	{
		Integer numberOfExistingXXInGroup = numberOfExistingXXPerGroupMap.get(behaviorId);
		Integer numberOfExecutedXXInGroup = numberOfExecutedXXPerGroupMap.get(behaviorId);
		return numberOfExecutedXXInGroup == numberOfExistingXXInGroup;
	}

	public boolean isFirstXXOfGroup(String behaviorId) {
		return numberOfExecutedXXPerGroupMap.get(behaviorId) == 1;
	}

	public void registerExecutedNLFile(String filename) 
	{
		if (! executedNLFiles.contains(filename)) {
			executedNLFiles.add(filename);
		}
	}
	
	public List<String> getExecutedNLFiles() {
		return executedNLFiles;
	}

	public String getTestCaseDir() {
		return System.getProperty("sysnat.executable.examples.source.dir");
	}

}
