package com.iksgmbh.sysnat.helper;

import java.io.File;
import java.util.List;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.WebLoginParameter;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;


/**
 * Startup helper for new test applications.
 * 
 * Adapt the value of the constants to your needs and execute this class as Java application.
 * 
 * ATTENSION: Already existing files will be overwritten !
 * 
 * @author Reik Oberrath
 */
public class TestApplicationGenerator
{
	private static final String TestApplicationName = "GoogleSearch"; 
	private static final boolean isWebApplication = true; 
	private static final boolean withLogin = false;   // true is not yet tested!
	private static final String StartParameter = "starturl";          
	private static final String environment="production"; 
	private static final String starturl="https://www.google.com/"; 
	
	// only needed for test applications with login 
	private static final String NameOfLoginField="Username"; 
	private static final String NameOfPasswordField="Password"; 
	private static final String LoginId="UserId"; 
	private static final String Password="12345"; 
	private static final String NameOfLoginButton="Login"; 
	
	public static void main(String[] args)
	{
		ExecutionRuntimeInfo.getInstance();
		System.out.println("");
		System.out.println("Generation of new test application '" + TestApplicationName + "':");
		System.out.println("");
		
		System.out.println("Generating property file...");
		createPropertyFile();
		
		System.out.println("Generating LanguageTemplatesBasics...");
		createLanguageTemplatesBasicsJavaFile();
		
		System.out.println("Adapting settings.config...");
		adaptSettingsConfig();
		
		System.out.println("");
		System.out.println("Done with generation.");
	}

	private static void adaptSettingsConfig()
	{
		String path = ExecutionRuntimeInfo.getInstance().getRootPath()
		        + "/workspace/sysnat.natural.language.executable.examples";
		File settingsFile = new File(path, "settings.config");
		List<String> content = SysNatFileUtil.readTextFile(settingsFile);
		
		StringBuffer newContent = new StringBuffer();
		boolean addTestApp = false;
		boolean firstAllowedValues = true;
		
		for (String line : content)
		{
			if (addTestApp) 
			{
				if ( ! line.contains(TestApplicationName)) {					
					line += ", " + TestApplicationName;
				}
				addTestApp = false;
			}
			
			if (firstAllowedValues && line.trim().startsWith("# Values allowed")) {
				addTestApp = true;
				firstAllowedValues = false;
			}
			
			if (line.trim().startsWith("Application_Under_Test")) {
				line = "Application_Under_Test = " + TestApplicationName;
			}
			
			if (line.trim().startsWith("Zu_testende_Anwendung")) {
				line = "Zu_testende_Anwendung = " + TestApplicationName;
			}

			if (line.trim().startsWith("Zielumgebung")) {
				line = "Zielumgebung = " + environment.toUpperCase();
			}
			
			if (line.trim().startsWith("Environment")) {
				line = "Environment = " + environment.toUpperCase();
			}
			
			if (line.trim().startsWith("Ausführungsfilter")) {
				line = "Ausführungsfilter = -";
			}
			
			if (line.trim().startsWith("ExecutionFilter")) {
				line = "ExecutionFilter = -";
			}

			newContent.append(line).append(System.getProperty("line.separator"));
		}
		
		SysNatFileUtil.writeFile(settingsFile, newContent.toString());
		
	}

	private static void createLanguageTemplatesBasicsJavaFile()
	{
		if (isWebApplication) {
			if (withLogin) {
				createLanguageTemplatesBasics_basedOn_HelloWordSpringBoot();
			} else {
				createLanguageTemplatesBasics_basedOn_HomepageIKS();
			}
		} else {
			System.err.println("Non-WebApplications are not yet supported by the TestApplicationGenerator.");
		}
		
	}

	private static void createLanguageTemplatesBasics_basedOn_HomepageIKS()
	{
		String path = ExecutionRuntimeInfo.getInstance().getRootPath()
		        + "/workspace/sysnat.test.runtime.environment/src/main/java/com/iksgmbh/sysnat/language_templates/homepageiks/";
		List<String> content = SysNatFileUtil
		        .readTextFile(new File(path, "LanguageTemplatesBasics_HomePageIKS.java"));

		StringBuffer sb = new StringBuffer();
		boolean skip = true;
		for (String line : content) 
		{
			if (line.startsWith("package ")) {
				skip = false;
			}
			
			if (line.trim().startsWith("@LanguageTemplate(")) {
				break;
			}
			
			if (line.trim().equals("private String getPageName()")) {
				sb.append(line).append(System.getProperty("line.separator"));
				sb.append("	{").append(System.getProperty("line.separator"));;
				sb.append("		return null;   // TODO").append(System.getProperty("line.separator"));;
				skip = true;
			}
			
			if (skip) {
				if (line.equals("	}")) {
					sb.append(line).append(System.getProperty("line.separator"));
					skip = false;
				}
			} else {				
				sb.append(line).append(System.getProperty("line.separator"));
			}
			
		}
		sb.append(System.getProperty("line.separator"));
		sb.append("    // Add Language Template Methods here").append(System.getProperty("line.separator"));
		sb.append("}").append(System.getProperty("line.separator"));

		String newContent = sb.toString();
		newContent = newContent.replace("HomePageIKS", TestApplicationName);
		newContent = newContent.replace("HomePageIKS", TestApplicationName);
		newContent = newContent.replace("homepageiks", TestApplicationName.toLowerCase());
		newContent = newContent.replace("executableExample.clickLink(\"Home\");", "// TODO");
		newContent = newContent.replace("executableExample.clickLink(\"Home\");", "// TODO");
		newContent = newContent.replace("executableExample.getTextForId(\"//div[@class='content']//h1\").equals(\"Projekte. Beratung. Spezialisten.\");",
				                        "false;   // TODO");
		
		path = ExecutionRuntimeInfo.getInstance().getRootPath()
		        + "/workspace/sysnat.test.runtime.environment/src/main/java/com/iksgmbh/sysnat/language_templates/"
		        + TestApplicationName.toLowerCase();
		File folder = new File(path);
		folder.mkdir();
		File javaFile = new File(folder, "LanguageTemplatesBasics_" + TestApplicationName + ".java");
		SysNatFileUtil.writeFile(javaFile, newContent);
	}

	private static void createLanguageTemplatesBasics_basedOn_HelloWordSpringBoot()
	{
		String path = ExecutionRuntimeInfo.getInstance().getRootPath()
				      + "/workspace/sysnat.test.runtime.environment/src/main/java/com/iksgmbh/sysnat/language_templates/helloworldspringboot/";
		List<String> content = SysNatFileUtil.readTextFile(new File(path, "LanguageTemplatesBasics_HelloWorldSpringBoot.java"));
		
		StringBuffer sb = new StringBuffer();
		
		for (String line : content) {
			if (line.trim().startsWith("@LanguageTemplate")) {
				break;
			}
			sb.append(line).append(System.getProperty("line.separator"));
		}
		sb.append(System.getProperty("line.separator"));
		sb.append("    // Add Language Template Methods here").append(System.getProperty("line.separator"));
		sb.append("}").append(System.getProperty("line.separator"));
		
		String newContent = sb.toString();
		newContent = newContent.replace("HelloWorldSpringBoot", TestApplicationName);
		newContent = newContent.replace("HelloWorldSpringBoot", TestApplicationName);
		newContent = newContent.replace("helloworldspringboot", TestApplicationName.toLowerCase());
		newContent = newContent.replace("\"Username\"", "\"" + NameOfLoginField + "\"");
		newContent = newContent.replace("\"Password\"", "\"" + NameOfPasswordField + "\"");
		newContent = newContent.replace("\"Log in\"", "\"" + NameOfLoginButton + "\"");
		
		path = ExecutionRuntimeInfo.getInstance().getRootPath()
			   + "/workspace/sysnat.test.runtime.environment/src/main/java/com/iksgmbh/sysnat/language_templates/"
			   + TestApplicationName.toLowerCase();
		File folder = new File(path);
		folder.mkdir();
		File javaFile = new File(folder, "LanguageTemplatesBasics_" + TestApplicationName);
		SysNatFileUtil.writeFile(javaFile, newContent);
	}

	private static void createPropertyFile()
	{
		String propertiesPath = ExecutionRuntimeInfo.getInstance().getPropertiesPath();
		File propertiesFile = new File(propertiesPath, TestApplicationName + ".properties");
		
		String properties = "";
		properties += "isWebApplication=" + isWebApplication + System.getProperty("line.separator");
		properties += "withLogin=" + withLogin + System.getProperty("line.separator");
		properties += "StartParameter=" + StartParameter + System.getProperty("line.separator");
		properties += System.getProperty("line.separator");
		properties += TestApplicationName.toLowerCase() + "." + environment 
		              + ".starturl=" + starturl + System.getProperty("line.separator");
		
		if (withLogin) 
		{
			properties += TestApplicationName.toLowerCase() + "." + environment 
		              + ".login." + WebLoginParameter.LOGINID.name().toLowerCase() + "=" + LoginId + System.getProperty("line.separator");
		
			properties += TestApplicationName.toLowerCase() + "." + environment 
		              + ".login." + WebLoginParameter.PASSWORD.name().toLowerCase() + "=" + Password + System.getProperty("line.separator");
		}
		SysNatFileUtil.writeFile(propertiesFile, properties);
	}
}
