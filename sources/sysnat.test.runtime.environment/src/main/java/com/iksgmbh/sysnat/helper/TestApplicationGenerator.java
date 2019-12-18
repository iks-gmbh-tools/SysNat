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
package com.iksgmbh.sysnat.helper;

import java.io.File;
import java.util.List;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.WebLoginParameter;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;


/**
 * Startup helper for new test applications.
 * 
 * Adapt the value of the constants to your needs and execute this class as Java application.
 * 
 * Note: Java and Properties files that exist will not be overwritten!
 * 
 * @author Reik Oberrath
 */
public class TestApplicationGenerator
{
	private static final String SOURCE_DIR = "sources";
	
	// Test Application context data 
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
		adaptTestingConfig();
		
		System.out.println("Creating folder for nlxx files...");
		createExecutableExampleFolder();
		
		System.out.println("");
		System.out.println("Done with generation.");
	}

	private static void createExecutableExampleFolder()
	{
		String path = System.getProperty("sysnat.executable.examples.source.dir");
		File folder = new File(path, TestApplicationName);
		boolean ok = folder.mkdir();
		if (! ok) {
			System.err.println("Could not create: " + folder.getAbsolutePath());
		}
	}

	private static void adaptTestingConfig()
	{
		String path = ExecutionRuntimeInfo.getInstance().getRootPath()
		        + "/" + SOURCE_DIR + "/sysnat.natural.language.executable.examples";
		File settingsFile = new File(path, SysNatConstants.TESTING_CONFIG_PROPERTY);
		List<String> content = SysNatFileUtil.readTextFile(settingsFile);
		
		StringBuffer newContent = new StringBuffer();
		
		for (String line : content)
		{
			if (line.trim().startsWith(SysNatConstants.TEST_APPLICATION_SETTING_KEY)) {
				line = SysNatConstants.TEST_APPLICATION_SETTING_KEY + " = " + TestApplicationName;
			}

			if (line.trim().startsWith(SysNatConstants.TEST_ENVIRONMENT_SETTING_KEY)) {
				line = SysNatConstants.TEST_ENVIRONMENT_SETTING_KEY + " = " + environment.toUpperCase();
			}
			
			if (line.trim().startsWith(SysNatConstants.TEST_EXECUTION_FILTER_SETTING_KEY)) {
				String newCommentLine = "# " + TestApplicationName + ": -";
				newContent.append(newCommentLine).append(System.getProperty("line.separator"));
				line = SysNatConstants.TEST_EXECUTION_FILTER_SETTING_KEY + " = -";
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
		        + "/sources/sysnat.test.runtime.environment/src/main/java/com/iksgmbh/sysnat/language_templates/homepageiks/";
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
				+ "/" + SOURCE_DIR + "/sysnat.test.runtime.environment/src/main/java/com/iksgmbh/sysnat/language_templates/"
		        + TestApplicationName.toLowerCase();
		File folder = new File(path);
		folder.mkdir();
		File javaFile = new File(folder, "LanguageTemplatesBasics_" + TestApplicationName + ".java");
		
		if ( ! javaFile.exists()) {			
			SysNatFileUtil.writeFile(javaFile, newContent);
		} else {
			System.out.println("Existing file " + javaFile.getName() + " was not overwritten");
		}
	}

	private static void createLanguageTemplatesBasics_basedOn_HelloWordSpringBoot()
	{
		String path = ExecutionRuntimeInfo.getInstance().getRootPath()
				+ "/" + SOURCE_DIR + "/sysnat.test.runtime.environment/src/main/java/com/iksgmbh/sysnat/language_templates/helloworldspringboot/";
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
				+ "/" + SOURCE_DIR + "/sysnat.test.runtime.environment/src/main/java/com/iksgmbh/sysnat/language_templates/"
			   + TestApplicationName.toLowerCase();
		File folder = new File(path);
		folder.mkdir();
		File javaFile = new File(folder, "LanguageTemplatesBasics_" + TestApplicationName);
		
		if ( ! javaFile.exists()) {			
			SysNatFileUtil.writeFile(javaFile, newContent);
		} else {
			System.out.println("Existing file " + javaFile.getName() + " was not overwritten");
		}
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

		if ( ! propertiesFile.exists()) {			
			SysNatFileUtil.writeFile(propertiesFile, properties);
		} else {
			System.out.println("Existing file " + propertiesFile.getName() + " was not overwritten");
		}
	}
}
