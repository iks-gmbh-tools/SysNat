package com.iksgmbh.sysnat.helper;

import static com.iksgmbh.sysnat.utils.SysNatConstants.NO_FILTER;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import org.joda.time.DateTime;

import com.iksgmbh.sysnat.PropertiesUtil;
import com.iksgmbh.sysnat.utils.ExceptionHandlingUtil;
import com.iksgmbh.sysnat.utils.SysNatConstants;
import com.iksgmbh.sysnat.utils.SysNatConstants.AppUnderTest;
import com.iksgmbh.sysnat.utils.SysNatConstants.BrowserType;
import com.iksgmbh.sysnat.utils.SysNatConstants.TargetEnv;
import com.iksgmbh.sysnat.utils.SysNatLocaleConstants;
import com.iksgmbh.sysnat.utils.SysNatStringUtil;

/**
 * Stores global information on tests and their execution.
 * 
 * Avoid test case specific stuff here!
 * 
 * @author Reik Oberrath
 */
public class GenerationRuntimeInfo 
{
	public static final String PROPERTIES_PATH = "src/main/resources/execution_properties";  // common technical settings
	public static final String PROPERTIES_FILENAME = "execution.properties";  // common technical settings
	private static final String CONFIG_FILE_NAME = "../sysnat.szenario.natural.description/settings.config";  // user settings

	private static final SimpleDateFormat TODAY_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static GenerationRuntimeInfo instance;
    
    private String screenShotDir;
    private String osName;
    private TargetEnv targetEnvironment;
    private AppUnderTest appUnderTest;
	
	private String testCategories;
	private List<String> testCategoriesToExecute;
	
	private DateTime startPointOfTime = new DateTime();
	private BrowserType browserTypeToUse;
	private String testApplName;
	private Locale locale;

	public static GenerationRuntimeInfo getInstance() 
	{
		if (instance == null)  {
			System.out.println("SysNatTesting v" + SysNatConstants.SYS_NAT_VERSION + " test environment initialisation...");
			instance = new GenerationRuntimeInfo();
		}
		return instance;
	}
	
	private GenerationRuntimeInfo() 
	{
		// load test properties
		addToSystemProperties(PROPERTIES_PATH + "/" + PROPERTIES_FILENAME);
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
		if ( ! testCategories.isEmpty()) {			
			assertPositiveCategoriesInFrontOfNegativeOnes(testCategoriesToExecute);
		}
		if (testCategories.length() == 0) {
			testCategories = NO_FILTER; 
		}
		
		osName = getOsName();
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
			ExceptionHandlingUtil.throwException("Unknown system property '" + key + "'!");
		}
		return toReturn.trim();
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

	
	public List<String> getTestCategoriesToExecute() {
		return testCategoriesToExecute;
	}

	public void setTestCategoriesToExecute(List<String> testCategoriesToExecute) {
		this.testCategoriesToExecute = testCategoriesToExecute;
	}

	private void addToSystemProperties(String propertiesFileName) 
	{
        Properties properties = PropertiesUtil.loadProperties(propertiesFileName);
        
        for (Object key : properties.keySet()) {
			System.setProperty((String) key, properties.getProperty((String) key));
		}
		
	}

	public String getTestCategories() {
		return testCategories;
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
	
    public boolean isOS_Windows() {
        return getOsName().startsWith("Windows");
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

}
