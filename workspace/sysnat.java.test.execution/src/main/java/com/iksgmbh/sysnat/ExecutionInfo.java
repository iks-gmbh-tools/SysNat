package com.iksgmbh.sysnat;

import static com.iksgmbh.sysnat.utils.SysNatConstants.NO_FILTER;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import org.joda.time.DateTime;

import com.iksgmbh.sysnat.exception.SysNatException;
import com.iksgmbh.sysnat.guicontrol.GuiControl;
import com.iksgmbh.sysnat.helper.VirtualTestCase;
import com.iksgmbh.sysnat.utils.SysNatConstants;
import com.iksgmbh.sysnat.utils.SysNatConstants.AppUnderTest;
import com.iksgmbh.sysnat.utils.SysNatConstants.BrowserType;
import com.iksgmbh.sysnat.utils.SysNatConstants.TargetEnv;
import com.iksgmbh.sysnat.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.utils.SysNatLocaleConstants;
import com.iksgmbh.sysnat.utils.SysNatStringUtil;

/**
 * Stores global information on tests and their execution.
 * 
 * Avoid test case specific stuff here!
 * 
 * @author Reik Oberrath
 */
public class ExecutionInfo 
{
	public static final String PROPERTIES_FILE_NAME = "resources/execution.properties";  // technical settings

	// the following two constants define the behaviour for searching GUI-elements
	private static final int DEFAULT_MILLIS_TO_WAIT_FOR_AVAILABILITY_CHECK = 100;
	private static final int DEFAULT_SECS_TO_WAIT_FOR_GUI_ELEMENT_TIMEOUT = 1;
	private static final int DEFAULT_SECS_TO_WAIT_FOR_PRINT_TIMEOUT = 60;

	private static final SimpleDateFormat TODAY_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static final String CONFIG_FILE_NAME = "settings.config";                     // user settings
    private static ExecutionInfo instance;
    
    private String screenShotDir;
    private String osName;
    private String targetLoginURL;
    private TargetEnv targetEnvironment;
    private AppUnderTest appUnderTest;

	private HashMap<String, List<String>> reportMessagesOK = new HashMap<>();
	private HashMap<String, List<String>> reportMessagesWRONG = new HashMap<>();   // assertion failed (fachliches Problem)
	private HashMap<String, List<String>> reportMessagesFAILED = new HashMap<>();  // excecption thrown (technisches Problem)
	private HashMap<String, Integer> testCategoryCollection = new HashMap<>();     // categories defined by the natspec files
	
	private String testCategories;
	private List<String> testCategoriesToExecute;
	private List<String> inactiveTestIDs = new ArrayList<>();
	
	private int totalNumberOfTestCases = 0;  // known for the given application/product under test
	private int numberOfAllExecutedTestCases = 0;	
	private boolean alreadyLoggedIn = false;	
	private DateTime startPointOfTime = new DateTime();
	private BrowserType browserTypeToUse;
	private GuiControl guiController;
	private boolean loginOk = true;
	private String testApplName;
	private Locale locale;

	public static ExecutionInfo getInstance() 
	{
		if (instance == null)  {
			System.out.println("SysNatTesting v" + SysNatConstants.SYS_NAT_VERSION + " test environment initialisation...");
			instance = new ExecutionInfo();
		}
		return instance;
	}
	
	private ExecutionInfo() 
	{
		// load test properties
		addToSystemProperties(PROPERTIES_FILE_NAME);
		addToSystemProperties(CONFIG_FILE_NAME);

		try {			
			browserTypeToUse = BrowserType.valueOf(getSystemProperty("Browser").toUpperCase());
		} catch (Exception e) {
			System.err.println("Unknown Browser in config.settings: " + getSystemProperty("Browser"));
		}

		
		try {			
			targetEnvironment = TargetEnv.valueOf( getSystemProperty("Environment").toUpperCase() );
		} catch (Exception e) {
			System.err.println("Unknown Environment in config.settings: " + getSystemProperty("Environment"));
		}
				
		try {			
			appUnderTest = AppUnderTest.valueOf( getSystemProperty("TestApplication"));			
		} catch (Exception e) {
			System.err.println("Unknown TestApplication in config.settings: " + getSystemProperty("TestApplication"));
		}
		
		testCategories = getSystemProperty("Filter_auszufuehrende_Testkategorien");
		testCategoriesToExecute = SysNatStringUtil.getTestCategoriesAsList(testCategories, "");
		assertPositiveCategoriesInFrontOfNegativeOnes(testCategoriesToExecute);
		if (testCategories.length() == 0) {
			testCategories = NO_FILTER; 
		}
		
		osName = getOsName();
		targetLoginURL = getLoginDataForDefaultLogin("url");
		screenShotDir = System.getProperty("screenshot.dir", "screenshots");
		
		System.out.println("Starting " + appUnderTest.name() + " on " + targetEnvironment 
				           + " at " + getStartPointOfTimeAsFileStringForFileName() + "...");
	}

	void assertPositiveCategoriesInFrontOfNegativeOnes(List<String> testCategories) 
	{
		int indexOfLastPositiveCategory = 0;
		int indexOfFirstNegativeCategory = 0;
		int indexCounter = 0;
		
		for (String category : testCategories) 
		{
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

		if (indexOfLastPositiveCategory > indexOfFirstNegativeCategory)  {
			throw new RuntimeException("Negative categories must not be defined before positive ones.");
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

	private String getExecutionProperty(String propertyEnding) 
	{
		if (appUnderTest == null) {
			throw new RuntimeException("Die property 'TestApplikation' wurde in der Datei settings.config nicht gesetzt oder ist nicht gültig.");
		}

		if (targetEnvironment == null) {
			throw new RuntimeException("Die property 'Zielumgebung' wurde in der Datei settings.config nicht gesetzt oder ist nicht gültig.");
		}
		
		String prefix = getAppUnderTestAsLowerCaseName();
		return prefix + "." + targetEnvironment.name().toLowerCase()  + "." + propertyEnding;
	}

	public String getStartPointOfTimeAsFileStringForFileName()  {
		return getStartPointOfTimeAsString().replaceFirst(":", "h ").replaceFirst(":", "min ") + "sec";
	}

	public String getStartPointOfTimeAsString()  {
		return DATE_FORMAT.format(startPointOfTime.toDate());
	}
	
	public Date getStartPointOfTime()  {
		return startPointOfTime.toDate();
	}

	public void countExcecutedTestCase() {
		numberOfAllExecutedTestCases++;
	}
	
	public void countTestCase() {
		totalNumberOfTestCases++;
	}
	
	public void addInactiveTestCase(String inactiveTestId) {
		inactiveTestIDs.add(inactiveTestId);
	}
	
	public void addTestMessagesOK(String testIdOK, List<String> messagesOK) {
		reportMessagesOK.put(testIdOK, messagesOK);
	}

	public void addTestMessagesWRONG(String testIdWRONG, List<String> messagesWRONG) {
		reportMessagesWRONG.put(testIdWRONG, messagesWRONG);
	}

	public void addTestMessagesFAILED(String testIdFAILED, List<String> messagesFAILED) {
		reportMessagesFAILED.put(testIdFAILED, messagesFAILED);
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

	private void addToSystemProperties(String configFileName) 
	{
        File f = new File(configFileName);
        if ( ! f.exists() ) {
            throw new RuntimeException("The following necessary file is missing" + configFileName);
        }
        Properties properties = new Properties();
        SysNatFileUtil.loadPropertyFile(f, properties);
        
        for (Object key : properties.keySet()) {
			System.setProperty((String) key, properties.getProperty((String) key));
		}
		
	}

	public String getInactiveTestListAsString() 
	{
		if (inactiveTestIDs.size() == 0)
		{
			return "-";
		}
		
		final StringBuffer sb = new StringBuffer();
		for (String testId : inactiveTestIDs) 
		{
			sb.append(testId).append(", ");
		}
		
		String toReturn = sb.toString();
		return toReturn.substring(0, toReturn.length()-2);
	}

	public boolean isTestIdAlreadyUsed(String testID) 
	{
		return reportMessagesOK.containsKey(testID)  
		    || reportMessagesFAILED.containsKey(testID)
	        || reportMessagesWRONG.containsKey(testID);	
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
		return inactiveTestIDs.size();
	}

	public String getTestCategories() {
		return testCategories;
	}

	public String getTargetLoginUrl() {
		return targetLoginURL;
	}
	
	public String getExecutionDurationAsString() 
	{
		DateTime endPointOfTime = new DateTime();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(endPointOfTime.toDate().getTime() - startPointOfTime.toDate().getTime());
	}

	public String getOsName() 
	{
		String osname = osName;
		if (osname == null)  {
			osname = System.getProperty("os.name");
			//System.out.println("Detected Operating System: " + osname);
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
    }
	
	public String getLoginDataForDefaultLogin(final String loginDataItem) 
	{
		final String propertyKey = getExecutionProperty("login." + loginDataItem);
		final String toReturn = System.getProperty(propertyKey);
		
		if (toReturn == null) {			
			String errorMessage = "Expected property '" + propertyKey + "' not found "
	                               + "in execution.properies";
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

	public String getTodayDateAsString()  {
		return TODAY_FORMAT.format(new Date()); 
	}
	
	public TargetEnv getTargetEnv()  {
		return targetEnvironment; 
	}

	
	public AppUnderTest getAppUnderTest()  {
		return appUnderTest; 
	}
	
	public String getExecutionSpeed() {
		return getSystemProperty("ExecutionSpeed");
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
		
		System.out.println("Unbekannte Ausführungsgeschwindigkeit: " + executionSpeed + " Default 'schnell' wird genommen.");
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

    public void addCategoryToCollection(String category) 
    {
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

	public String getAppUnderTestAsLowerCaseName() {
		if (testApplName != null) {
			return testApplName;
		}
		return  appUnderTest.name().replaceAll("_", "").toLowerCase();
	}

	public void setTestApplName(String testApplName) {
		this.testApplName = testApplName;
	}

	public Locale getLocale() {
		return locale;
	}

	public boolean addToResultAsSeparateTestCase(VirtualTestCase virtualTestCase) 
	{
		List<String> reportMessages = virtualTestCase.getReportMessages();
		for (String message : reportMessages) 
		{
			if (message.contains(SysNatLocaleConstants.ERROR_KEYWORD)) 
			{
				reportMessagesFAILED.put(virtualTestCase.getTestID(), virtualTestCase.getReportMessages());
				return false;
			}
			if (message.contains(SysNatConstants.QUESTION_IDENTIFIER) && 
					message.contains(SysNatLocaleConstants.NO_KEYWORD)) 
			{
				reportMessagesWRONG.put(virtualTestCase.getTestID(), virtualTestCase.getReportMessages());
				return false;
			}
		}
		
		reportMessagesOK.put(virtualTestCase.getTestID(), virtualTestCase.getReportMessages());		
		return true;
	}

}
