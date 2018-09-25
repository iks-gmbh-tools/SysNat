package com.iksgmbh.sysnat.common.helper;

import java.io.File;

import com.iksgmbh.sysnat.common.exception.SysNatException;

public class HtmlLauncher 
{

	public static void doYourJob(String reportFileAsString) 
	{
	    Runtime rt = Runtime.getRuntime();
	    try {
	    	File reportFile = new File( reportFileAsString );
	    	String pathToHtml = reportFile.getCanonicalPath();
	        String pathToFirefoxExe = getPathToFirefoxBinary();
			rt.exec(new String[]{ pathToFirefoxExe + "//firefox.exe","-url", "file:///" + pathToHtml });
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
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
		
		return pathToFirefoxExe;  // path is already absolute
	}
	
}
