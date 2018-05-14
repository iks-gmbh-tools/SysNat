package com.iksgmbh.sysnat.utils;

import java.io.File;
import java.util.Properties;

import com.iksgmbh.sysnat.ExecutionInfo;
import com.iksgmbh.sysnat.TestCase;

public class SysNatUtil 
{
	public static boolean doesTestBelongToApplicationUnderTest(TestCase testcase) 
	{
		String aut = ExecutionInfo.getInstance().getAppUnderTestAsLowerCaseName().replaceAll("_", "");
		String fullClassName = testcase.getClass().getName().toLowerCase();
		int pos = fullClassName.indexOf('.');
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
        File f = new File(ExecutionInfo.PROPERTIES_FILE_NAME);
        if ( ! f.exists() ) {
            f = new File("..", ExecutionInfo.PROPERTIES_FILE_NAME);
            if ( ! f.exists() ) {
            	throw new RuntimeException("The following necessary file is missing: " + ExecutionInfo.PROPERTIES_FILE_NAME);
            }
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
		target.setTestDataSets(source.getTestDataSets());
	}
}
