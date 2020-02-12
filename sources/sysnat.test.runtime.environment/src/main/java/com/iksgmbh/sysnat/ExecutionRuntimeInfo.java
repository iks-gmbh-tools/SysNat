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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TimeZone;

import org.joda.time.DateTime;

import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.BrowserType;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.DialogStartTab;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.DocumentationDepth;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.DocumentationFormat;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.DocumentationType;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.ExecSpeed;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.ResultLaunchOption;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.TargetEnvironment;
import com.iksgmbh.sysnat.common.utils.SysNatDateUtil;
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
	private static final Hashtable<String, String> sysNatProperties = new Hashtable<>();
	private static final ResourceBundle CONSTANTS_BUNDLE = ResourceBundle.getBundle("bundles/Constants", Locale.getDefault());

	public static final String UNNAMED_XX_GROUP = "UNNAMED_XX_GROUP";
	public static final String PROPERTIES_PATH = "sources/sysnat.test.runtime.environment/src/main/resources/execution_properties";
	public static final String PROPERTIES_FILENAME = "execution.properties";
	public static final String TESTING_CONFIG_FILE_NAME = "sources/sysnat.natural.language.executable.examples/testing.config";
	public static final String DOCING_CONFIG_FILE_NAME = "sources/sysnat.natural.language.executable.examples/docing.config";
	public static final String GENERAL_CONFIG_FILE_NAME = "sources/sysnat.natural.language.executable.examples/general.config";

	// the following two constants define the behaviour for searching GUI-elements
	private static final int DEFAULT_MILLIS_TO_WAIT_FOR_AVAILABILITY_CHECK = 10;
	private static final int DEFAULT_SECS_TO_WAIT_FOR_GUI_ELEMENT_TIMEOUT = 1;
	private static final int DEFAULT_SECS_TO_WAIT_FOR_PRINT_TIMEOUT = 60;
	
	private static final String PROPERTIES_FILE_EXTENSION = ".properties";
	private static final SimpleDateFormat TODAY_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");


	private static ExecutionRuntimeInfo instance;

	private HashMap<String, List<String>> reportMessagesOK = new HashMap<>();
	private HashMap<String, List<String>> reportMessagesWRONG = new HashMap<>(); // assertion failed (fachliches Problem)
	private HashMap<String, List<String>> reportMessagesFAILED = new HashMap<>(); // exception thrown (technisches Problem)
	private HashMap<String, Integer> executionFilterMap = new HashMap<>(); // execution filter defined in the nlxx files
	private HashMap<String, TestStatistics> testStatistics = new HashMap<>();
	private LinkedHashMap<String, String> sortedXXidBehaviourMap = new LinkedHashMap<>();   // stores for each XXID its Behaviour/group in the executed order
	private List<String> knownFeatures = new ArrayList<>();   // stores Behaviour with Feature-Keyword
	private List<String> executedNLFiles = new ArrayList<>();   // list of executed natural language files
	private String osName;
	private HashMap<String, List<String>> testAppEnvironmentsMap = new HashMap<>();
	private List<String> executionFilterList;
	private List<String> inactiveXXIDs = new ArrayList<>();
	private File reportFolder;
	private List<String> orderedListOfAllExecutedXXs = new ArrayList<>();	
	private int totalNumberOfXX = 0; // known for the given application/product under test
	private boolean alreadyLoggedIn = false;
	private DateTime startPointOfTime = new DateTime();
	private GuiControl guiController;
	private boolean loginOk = true;
	private Locale locale;
	private boolean applicationStarted;
	private boolean archiveTestReport;
	private boolean archiveDocumentation;
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
			instance.readConfiguredTestAppsAndTheirEnvironments();
		}
		return instance;
	}

	protected ExecutionRuntimeInfo() 
	{
		// load test properties
		addToSystemProperties(getExecutionPropertiesAsString());
		addToSystemProperties(getConfigFileAsString(SysNatConstants.GENERAL_CONFIG_PROPERTY, GENERAL_CONFIG_FILE_NAME));
		addToSystemProperties(getConfigFileAsString(SysNatConstants.DOCING_CONFIG_PROPERTY, DOCING_CONFIG_FILE_NAME));
		addToSystemProperties(getConfigFileAsString(SysNatConstants.TESTING_CONFIG_PROPERTY, TESTING_CONFIG_FILE_NAME));
		addToSystemProperties(getPropertiesPath() + "/" + getTestApplicationName() + ".properties");
		
		String executionFilters = getTestExecutionFilter();
		executionFilterList = SysNatStringUtil.getExecutionFilterAsList(executionFilters, "");
		assertPositiveFiltersInFrontOfNegativeOnes(executionFilterList);
		if (executionFilters.length() == 0) {
			executionFilters = NO_FILTER;
		}

		osName = getOsName();

		System.out.println("Starting " + getTestApplicationName() + 
				           " on " + getTestEnvironmentName() + 
				           " at " + getStartPointOfTimeAsFileStringForFileName() + 
				           "...");
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


	public String getStartPointOfTimeAsFileStringForFileName() {
		return getStartPointOfTimeAsString().replaceFirst(":", "h ").replaceFirst(":", "min ") + "sec";
	}

	public String getStartPointOfTimeAsString() {
		return DATE_FORMAT.format(startPointOfTime.toDate());
	}

	public Date getStartPointOfTime() {
		return startPointOfTime.toDate();
	}
	
	public List<String> getOrderedListOfAllExecutedTestCases() {
		return orderedListOfAllExecutedXXs;
	}

	public void countAsExecuted(String xxid) {
		orderedListOfAllExecutedXXs.add(xxid);
	}
	
	public void countAsExecuted(String xxid, String behaviourId) 
	{
		if (behaviourId != null) {
			Integer oldFrequence = numberOfExecutedXXPerGroupMap.get(behaviourId);
			if (oldFrequence != null) {
				int newFrequence = oldFrequence + 1;
				numberOfExecutedXXPerGroupMap.put(behaviourId, Integer.valueOf(newFrequence));
			}
		}
		countAsExecuted(xxid);
	}

	public void countExistingXX() {
		totalNumberOfXX++;
	}
	
	public int getNumberOfAllExecutedXXs() {
		return orderedListOfAllExecutedXXs.size();
	}

	public void uncountAsExecuted(String xxid) {
		orderedListOfAllExecutedXXs.remove(xxid);
	}

	public int getTotalNumberOfXXs() {
		return totalNumberOfXX;
	}

	public void addInactiveXX(String inactiveXXId, String behaviourID) {
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
		sortedXXidBehaviourMap.put(xxid, behaviourID);
	}

	public List<String> getExecutionFilterList() {
		return executionFilterList;
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

	
	public LinkedHashMap<String, String> getSortedXXidBehaviourMap() {
		return sortedXXidBehaviourMap;
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

	public static void reset() {
		instance = null;
		clearSysNatProperties();
		System.clearProperty(SysNatConstants.TEST_APPLICATION_SETTING_KEY);
		System.clearProperty("execution.properties");
		System.clearProperty("sysnat.properties.path");
		System.clearProperty(SysNatConstants.TESTING_CONFIG_PROPERTY);
	}

	protected static void clearSysNatProperties() {
		sysNatProperties.forEach((key, value) -> System.clearProperty(key));
		sysNatProperties.clear();
	}

	public String getLoginDataForDefaultLogin(final String loginDataItem) 
	{
		final String propertyKey = "login." + getTestApplicationName().toLowerCase() + "." + 
				                   getTestEnvironmentName().toLowerCase() + "." + loginDataItem;
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

	public int getMillisToWaitForAvailabilityCheck() 
	{
		ExecSpeed executionSpeed = getTestExecutionSpeed();

		switch (executionSpeed) 
		{
			case LANGSAM:
			case SLOW: 
				   return Integer.valueOf(System.getProperty("execution.delay.millis.slow", "5000"));

			case NORMAL:
				return Integer.valueOf(System.getProperty("execution.delay.millis.normal", "1250"));

			case QUICK:
			case SCHNELL: 
			default: 
				return DEFAULT_MILLIS_TO_WAIT_FOR_AVAILABILITY_CHECK;
		}
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

	public boolean addToResultAsSeparateXX(VirtualTestCase virtualExecutableExample) 
	{
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

	private String getConfigFileAsString(String propertyKey, String defaultValue)
	{
		String toReturn = System.getProperty(propertyKey);
		if (toReturn == null) {
			String absolutePath = SysNatFileUtil.findAbsoluteFilePath(defaultValue);
			System.setProperty(propertyKey, absolutePath);
			toReturn = absolutePath;
		}

		if ( ! SysNatFileUtil.isAbsolutePath(toReturn) ) {
			toReturn = SysNatFileUtil.findAbsoluteFilePath(toReturn);
			System.setProperty(propertyKey, toReturn);
		}
		System.out.println(propertyKey + " used: " + toReturn);
		return toReturn;
	}

	private String getExecutionPropertiesAsString() {
		return System.getProperty("execution.properties", getPropertiesPath() + "/" + PROPERTIES_FILENAME);
	}

	public String getPropertiesPath()
	{
		String toReturn = System.getProperty("sysnat.properties.path");
		if (toReturn == null) {
			String defaultValue = SysNatFileUtil.findAbsoluteFilePath(PROPERTIES_PATH);
			System.setProperty("sysnat.properties.path", defaultValue);
			toReturn = defaultValue;
		}
		if ( ! SysNatFileUtil.isAbsolutePath(toReturn) ) {
			toReturn = SysNatFileUtil.findAbsoluteFilePath(toReturn);
			System.setProperty("sysnat.properties.path", toReturn);
		}

		return toReturn;
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

	public boolean isTestReportToArchive() {
		return archiveTestReport;
	}

	public void setArchiveTestReport(boolean value) {
		archiveTestReport = value;
	}

	public boolean isDocumentationToArchive() {
		return archiveDocumentation;
	}

	public void setArchiveDocumentation(boolean value) {
		archiveDocumentation = value;
	}

	public boolean useSettingsDialog() {
		return System.getProperty(SysNatConstants.USE_SYSNAT_DIALOG_SETTING_KEY).equalsIgnoreCase("yes");
	}


	public boolean areSettingsComplete() {
		return settingsOk;
	}

	public void setSettingsOk() {
		settingsOk = true;
	}
	
	public void readConfiguredTestAppsAndTheirEnvironments()
	{
		final File testAppConfigDir = new File(System.getProperty("sysnat.properties.path"));
		final List<File> propertiesFiles = SysNatFileUtil.findFilesIn(PROPERTIES_FILE_EXTENSION, testAppConfigDir).getFiles();
		propertiesFiles.stream()
		               .filter(file -> ! file.getName().equals(ExecutionRuntimeInfo.PROPERTIES_FILENAME))
		               .forEach(this::readTestAppProperties);
	}
	
	private void readTestAppProperties(File propertiesFile) 
	{
		TestApplication testApp = new TestApplication(propertiesFile);
		testAppEnvironmentsMap.put(testApp.getName(), testApp.getConfiguredEnvironments());
	}
	
	public HashMap<String,List<String>> getTestAppEnvironmentsMap() {
		return testAppEnvironmentsMap;
	}

	public List<String> getKnownTestApplications()
	{
		List<String> testApps = new ArrayList<>(testAppEnvironmentsMap.keySet());
		Collections.sort(testApps);
		return testApps;
	}

	public List<String> getKnownEnvironments(String testApplicationName) {
		return testAppEnvironmentsMap.get(testApplicationName);
	}
	
	public String getReportFolderAsString() 
	{
		String mainDir = System.getProperty("sysnat.report.dir");
		String testReportName = checkReportName(getTestReportName());
		return SysNatFileUtil.findAbsoluteFilePath(mainDir) + "/"
				+ testReportName  + " "
				+ getStartPointOfTimeAsFileStringForFileName();
	}

	private String checkReportName(String testReportName)
	{
		String testApp = getTestApplicationName();

		if (testReportName.startsWith(testApp)) return testReportName;
		
		Optional<String> match = getKnownTestApplications().stream()
		                            .filter(appName -> testReportName.startsWith(appName))
		                            .findFirst();
		
		if ( ! match.isPresent() ) return testReportName;
		
		return buildDefaultReportName(testApp, getTestEnvironmentName(), getTestExecutionFilter());
	}

	public String buildDefaultReportName(String testApplication,
			                             String targetEnvironment,
			                             String executionFilter) 
	{
		if (executionFilter.equals(NO_FILTER)) {
			executionFilter = CONSTANTS_BUNDLE.getString("All");
		}

		if (executionFilter.length() != 0) {
			executionFilter = "-" + executionFilter;
		}


		
		return testApplication + "-" + targetEnvironment + "-" + executionFilter;
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

	public static class TestStatistics 
	{
		public TestStatistics(DateTime startTime, DateTime endTime) {
			startPointOfTime = DATE_FORMAT.format(startTime.toDate());
			long millis = endTime.toDate().getTime() - startTime.toDate().getTime();
			duration = SysNatDateUtil.formatDuration(millis);
		}


		public String startPointOfTime;
		public String duration;
	}

	public String getTotalTimePast() {
		return SysNatDateUtil.formatDuration(new Date().getTime() - startPointOfTime.getMillis());
	}

	public String getIntermediateResultLogText() {
		int numTotal = getNumberOfAllExecutedXXs();
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

	public String getExecutableExampleDir() {
		return System.getProperty("sysnat.executable.examples.source.dir");
	}
	
	private String getSetting(String settingsKey) 
	{
		String toReturn = System.getProperty(settingsKey);

		if (toReturn == null) {
			throw new SysNatException(settingsKey + " is not specified!");
		}

		return toReturn.trim();
	}
	
	// #################################################################################
	//              S e t t e r   f o r    C o n f i g S e t t i n g s
	// #################################################################################
	
	public void setTestApplicationName(String value) 
	{
		if (value != null) {			
			System.setProperty(SysNatConstants.TEST_APPLICATION_SETTING_KEY, value);
		}
	}

	public void setTestEnvironmentName(String value) 
	{
		value = value.toUpperCase();
		if (TargetEnvironment.valueOf(value) != null) {
			System.setProperty(SysNatConstants.TEST_ENVIRONMENT_SETTING_KEY, value);
		} else {
			throw new RuntimeException("Unknown " + SysNatConstants.TEST_ENVIRONMENT_SETTING_KEY + ": " + value);
		}
	}
	
	public void setTestExecutionFilter(String value) {
		if (value != null) {
			System.setProperty(SysNatConstants.TEST_EXECUTION_FILTER_SETTING_KEY, value);
		}
	}

	public void setTestBrowserName(String value) 
	{
		value = value.toUpperCase();
		if (BrowserType.valueOf(value) != null) {
			System.setProperty(SysNatConstants.TEST_BROWSER_SETTING_KEY, value);
		} else {
			throw new RuntimeException("Unknown " + SysNatConstants.TEST_BROWSER_SETTING_KEY + ": " + value);
		} 
	}

	public void setTestExecutionSpeedName(String value) 
	{
		value = value.toUpperCase();
		if (ExecSpeed.valueOf(value) != null) {
			System.setProperty(SysNatConstants.TEST_EXECUTION_SPEED_SETTING_KEY, value);
		} else {
			throw new RuntimeException("Unknown " + SysNatConstants.TEST_EXECUTION_SPEED_SETTING_KEY + ": " + value);
		} 
	}

	public void setTestReportName(String value) {
		if (value != null) {
			System.setProperty(SysNatConstants.TEST_REPORT_NAME_SETTING_KEY, value);
		}
	}

	public void setTestArchiveDir(String value) {
		if (value != null) {
			System.setProperty(SysNatConstants.TEST_ARCHIVE_DIR_SETTING_KEY, value);
		}
	}
	
	// ############################################################################

	public void setDocApplicationName(String value) 
	{
		if (value != null) {			
			System.setProperty(SysNatConstants.DOC_APPLICATION_SETTING_KEY, value);
		}
	}

	public void setDocTypeName(String value) 
	{
		if (DocumentationType.valueOf(value) != null) {
			System.setProperty(SysNatConstants.DOC_TYPE_SETTING_KEY, value);
		} else {
			throw new RuntimeException("Unknown " + SysNatConstants.DOC_TYPE_SETTING_KEY + ": " + value);
		}
	}

	public void setDocDepthName(String value) 
	{
		if (DocumentationDepth.valueOf(value) != null) {
			System.setProperty(SysNatConstants.DOC_DEPTH_SETTING_KEY, value);
		} else {
			throw new RuntimeException("Unknown " + SysNatConstants.DOC_DEPTH_SETTING_KEY + ": " + value);
		}
	}
	
	public void setDocEnvironmentName(String value) 
	{
		value = value.toUpperCase();
		if (TargetEnvironment.valueOf(value) != null) {
			System.setProperty(SysNatConstants.DOV_ENVIRONMENT_SETTING_KEY, value);
		} else {
			throw new RuntimeException("Unknown " + SysNatConstants.DOV_ENVIRONMENT_SETTING_KEY + ": " + value);
		}
	}

	public void setDocFormatName(String value) 
	{
		value = value.toUpperCase();
		if (DocumentationFormat.valueOf(value) != null) {
			System.setProperty(SysNatConstants.DOC_FORMAT_SETTING_KEY, value);
		} else {
			throw new RuntimeException("Unknown " + SysNatConstants.DOC_FORMAT_SETTING_KEY + ": " + value);
		}
	}

	public void setDocumentationName(String value) {
		if (value != null) {
			System.setProperty(SysNatConstants.DOC_NAME_SETTING_KEY, value);
		}
	}

	public void setDocArchiveDir(String value) {
		if (value != null) {
			System.setProperty(SysNatConstants.DOC_ARCHIVE_DIR_SETTING_KEY, value);
		}
	}


	// ############################################################################

	public void setUseSysNatDialog(String value) {
		if (value != null) {
			System.setProperty(SysNatConstants.USE_SYSNAT_DIALOG_SETTING_KEY, value);
		}
	}

	public void setDialogStartTab(String value) 
	{
		if (DialogStartTab.valueOf(value) != null) {
			System.setProperty(SysNatConstants.DIALOG_START_TAB_SETTING_KEY, value);
		} else {
			throw new RuntimeException("Unknown " + SysNatConstants.DIALOG_START_TAB_SETTING_KEY + ": " + value);
		}
	}

	public void setResultLaunchOptionName(String value) 
	{
		if (ResultLaunchOption.valueOf(value) != null) {
			System.setProperty(SysNatConstants.RESULT_LAUNCH_OPTION_SETTING_KEY, value);
		} else {
			throw new RuntimeException("Unknown " + SysNatConstants.RESULT_LAUNCH_OPTION_SETTING_KEY + ": " + value);
		}
	}
	
	// #################################################################################
	//           G e t t e r   f o r    C o n f i g S e t t i n g s
	// #################################################################################
	
	
	public String getTestApplicationName() {
		return getSetting(SysNatConstants.TEST_APPLICATION_SETTING_KEY);
	}

	public String getTestEnvironmentName() {
		return getSetting(SysNatConstants.TEST_ENVIRONMENT_SETTING_KEY);
	}
	
	public TargetEnvironment getTestEnvironment() {
		return TargetEnvironment.valueOf(getTestEnvironmentName().toUpperCase());
	}

	public String getTestExecutionFilter() {
		return getSetting(SysNatConstants.TEST_EXECUTION_FILTER_SETTING_KEY);
	}
	
	public String getTestBrowserTypeName() {
		return getSetting(SysNatConstants.TEST_BROWSER_SETTING_KEY);
	}
	
	public BrowserType getTestBrowserType() {
		return BrowserType.valueOf(getTestBrowserTypeName().toUpperCase());
	}	
	
	public String getTestExecutionSpeedName() {
		return getSetting(SysNatConstants.TEST_EXECUTION_SPEED_SETTING_KEY);
	}

	public ExecSpeed getTestExecutionSpeed() {
		return ExecSpeed.valueOf(getTestExecutionSpeedName().toUpperCase());
	}	

	public String getTestArchiveDir() {
		return getSetting(SysNatConstants.TEST_ARCHIVE_DIR_SETTING_KEY);
	}
	
	public String getTestReportName() {
		return getSetting(SysNatConstants.TEST_REPORT_NAME_SETTING_KEY);
	}

	// ############################################################################
	
	public String getDocApplicationName() {
		return getSetting(SysNatConstants.DOC_APPLICATION_SETTING_KEY);
	}

	public String getDocTypeName() {
		return getSetting(SysNatConstants.DOC_TYPE_SETTING_KEY);
	}
	
	public DocumentationType getDocType() {
		return DocumentationType.valueOf(getDocTypeName().toUpperCase());
	}

	public String getDocDepthName() {
		return getSetting(SysNatConstants.DOC_DEPTH_SETTING_KEY);
	}
	
	public DocumentationDepth getDocumentationDepth() {
		return DocumentationDepth.valueOf(getDocDepthName().toUpperCase());
	}	
	
	public String getDocEnvironmentName() {
		return getSetting(SysNatConstants.DOV_ENVIRONMENT_SETTING_KEY);
	}

	public TargetEnvironment getDocEnvironment() {
		return TargetEnvironment.valueOf(getDocEnvironmentName().toUpperCase());
	}	
	
	public String getDocFormatName() {
		return getSetting(SysNatConstants.DOC_FORMAT_SETTING_KEY);
	}

	public DocumentationFormat getDocFormat() {
		return DocumentationFormat.valueOf(getDocFormatName().toUpperCase());
	}	

	public String getDocArchiveDir() {
		return getSetting(SysNatConstants.DOC_ARCHIVE_DIR_SETTING_KEY);
	}
	
	public String getDocumentationName() {
		return getSetting(SysNatConstants.DOC_NAME_SETTING_KEY);
	}

	// ############################################################################

	public String getUseSysNatDialog() {
		return getSetting(SysNatConstants.USE_SYSNAT_DIALOG_SETTING_KEY);
	}
	
	public String getDialogStartTabName() {
		return getSetting(SysNatConstants.DIALOG_START_TAB_SETTING_KEY);
	}

	public DialogStartTab getDialogStartTab() {
		return DialogStartTab.valueOf(getDialogStartTabName());
	}

	public String getResultLaunchOptionName() {
		return getSetting(SysNatConstants.RESULT_LAUNCH_OPTION_SETTING_KEY);
	}

	public ResultLaunchOption getResultLaunchOption() {
		return ResultLaunchOption.valueOf(getResultLaunchOptionName());
	}


}
