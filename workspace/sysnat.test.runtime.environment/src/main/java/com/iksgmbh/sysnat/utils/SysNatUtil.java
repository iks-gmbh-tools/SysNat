package com.iksgmbh.sysnat.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.TestCase;
import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.TargetEnv;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

public class SysNatUtil 
{
	public static boolean doesTestBelongToApplicationUnderTest(TestCase testcase) 
	{
		String aut = ExecutionRuntimeInfo.getInstance().getTestApplicationNameAsPropertyKey();
		String fullClassName = testcase.getClass().getName().toLowerCase();
		int pos = fullClassName.indexOf('.');
		if (pos == -1) {
			// class belongs to default package: avoid this !
			return true;
		}
		String mainPackage = fullClassName.substring(0, pos);
		return aut.contains(mainPackage);
	}

	public static String getScreenshotErrorFileName(String testCaseName) {
		return testCaseName + "Error.png";
	}
	
	public static String getScreenshotFailureFileName(String testCaseName) {
		return testCaseName + "AssertionFailure.png";
	}
	
	public static Properties getExecutionProperties()  
	{
        File f = new File(System.getProperty("execution.properties"));
        if ( ! f.exists() ) {
//            f = new File("..", ExecutionRuntimeInfo.PROPERTIES_FILE_NAME);
//            if ( ! f.exists() ) {
//            }
            throw new RuntimeException("The following necessary file is missing: " + ExecutionRuntimeInfo.PROPERTIES_FILENAME);
        }
        
        final Properties properties = new Properties();
        SysNatFileUtil.loadPropertyFile(f, properties);        
        return properties;
	}

	public static String getScreenshotDir() {
		return getExecutionProperties().getProperty("screenshot.dir", "screenshots");
	}

	public static void copyContextData(TestCase source, TestCase target)
	{
		target.setTestID(source.getTestID());
		target.setGuiController(source.getGuiController());
		target.setReportMessages(source.getReportMessages());
		target.setTestCategories(source.getTestCategories());
		target.setTestData(source.getTestData());
	}
	public static String getPathToFirefoxBinary() {
		return getPathToFirefoxBinary("sysnat.path.to.firefox.dir");
	}

	public static String getPathToFirefoxBinary(final String systemPropertyKey) 
	{
    	final String pathToFirefoxExe = System.getProperty(systemPropertyKey);
		if (pathToFirefoxExe == null) {
			// file execution.properties misses values or has not been loaded !
			throw new SysNatException("Location of firefox binary not defined.");
		}
		if (pathToFirefoxExe.startsWith(".") 
			|| pathToFirefoxExe.startsWith("/")
			|| pathToFirefoxExe.startsWith("\\")) 
		{
			return System.getProperty("user.dir") + '/' + pathToFirefoxExe; 
		}
		
		return pathToFirefoxExe;  // path is alreafy absolute
	}

	public static String getSysNatRootDir() 
	{
		final File f = new File("../..", "Test");
		
		String canonicalPath;
		try {
			canonicalPath = f.getParentFile().getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return canonicalPath;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<String> buildAllowedDifferencesList(Properties properties)
	{
		final List<String> keys = new ArrayList(properties.keySet());
		final List<String> toReturn = new ArrayList();
		
		keys.forEach(key -> toReturn.add(properties.get(key).toString()));
		
		return toReturn;
	}
	
	public static List<String> removeLinesToBeIgnoreByPrefix(final List<String> lines, 
			                                                 final List<String> prefixesToIgnore) 
	{
		final List<String> toReturn = new ArrayList<>();
		
		for (String line : lines) 
		{
			line = line.trim();
			boolean takeLine = true;
			for (String ignoreLinePrefix : prefixesToIgnore) 
			{
				if ( line.startsWith(ignoreLinePrefix) ) {
					takeLine = false;
					continue;
				}
			}
			
			if (takeLine) {				
				toReturn.add(line);
			}
			
		}

		return toReturn;
	}

	public static boolean isTargetEnv(String value) 
	{
		try {
			TargetEnv.valueOf(value);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static boolean isTestApp(List<String> knownTestApplications, String value) 
	{
		for (String testApp : knownTestApplications) {
			if (value.equals(testApp)) {
				return true;
			}
		}
		return false;
	}
	
}
