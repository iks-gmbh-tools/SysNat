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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import com.iksgmbh.sysnat.testresult.archiving.SysNatTestResultArchiver;

public class SysNatTestingExecutor 
{
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/Constants", Locale.getDefault());
	private static final ResourceBundle BUNDLE_EN = ResourceBundle.getBundle("bundles/Constants", Locale.ENGLISH);

	public static final String MAVEN_OK = "OK";
	public static final File JAVA_HOME = new File("../../java");
	private static final String PATH_TO_MAVEN = "../../maven";

	public static void main(String[] args) 
	{
		// optional PRE-Phase: read settings from dialog
		SysNatStartDialog.doYourJob();
		
		// mandatory Phase A: translate natural language into JUnit test code
		boolean generationSuccessful = SysNatJUnitTestClassGenerator.doYourJob();
		
		if (generationSuccessful) 
		{
			// mandatory Phase B: compile and start tests 
			startMavenCleanCompileTest();

			// optional POST-Phase: archive test results
			archiveIfNeccessary();
		}
		
	}

	private static void archiveIfNeccessary() 
	{
		final ExecutionRuntimeInfo executionInfo = ExecutionRuntimeInfo.getInstance();
		if (executionInfo.areResultsToArchive()) {			
			SysNatTestResultArchiver.doYourJob(executionInfo.getReportFolder());
		}
	}

	/**
	 * @return result of execution 
	 */
	public static String startMavenCleanCompileTest(Properties jvmSystemProperties)
	{
		final InvocationRequest request = new DefaultInvocationRequest();
		final String pomFile = SysNatFileUtil.findAbsoluteFilePath("../sysnat.test.execution/pom.xml");
		request.setPomFile(new File(pomFile));
		request.setProperties(jvmSystemProperties);
		request.setJavaHome(JAVA_HOME);
		request.setMavenOpts("-Dfile.encoding=UTF-8");
		
		final List<String> goals = new ArrayList<>();
		goals.add("clean"); 
		goals.add("test");
		request.setGoals(goals);
		
		final Invoker mavenInvoker = new DefaultInvoker();
		mavenInvoker.setMavenHome(new File(PATH_TO_MAVEN));
		
		InvocationResult result = null;
		try {
			result = mavenInvoker.execute(request);
		} catch (MavenInvocationException e) {
			e.printStackTrace();
			return "Error";
		}
		
		if (result.getExitCode() != 0) 
		{
			String toReturn = "Failure";
			if (result.getExecutionException() != null) {				
				toReturn += ": " + result.getExecutionException().getMessage();
			} else {
				toReturn += "!";
			}
			return toReturn;
		}
		
		return MAVEN_OK;
	}
	
	/**
	 * @return result of execution 
	 */
	public static String startMavenCleanCompileTest() 
	{
		Properties properties = collectSysNatSystemProperties();
		return startMavenCleanCompileTest( properties );
	}

	public static Properties collectSysNatSystemProperties() 
	{
		final Properties properties = new Properties();
		
		String propertyKey = BUNDLE.getString("TESTAPP_SETTING_KEY");
		String property = System.getProperty(propertyKey);
		if (property == null) property = System.getProperty(BUNDLE_EN.getString("TESTAPP_SETTING_KEY"));
		properties.setProperty(propertyKey, property);

		propertyKey = BUNDLE.getString("BROWSER_SETTING_KEY");
		properties.setProperty(propertyKey, System.getProperty(propertyKey));
		
		
		propertyKey = BUNDLE.getString("ENVIRONMENT_SETTING_KEY");
		property = System.getProperty(propertyKey);
		if (property == null) property = System.getProperty(BUNDLE_EN.getString("ENVIRONMENT_SETTING_KEY"));
		properties.setProperty(propertyKey, property);
		
		propertyKey = BUNDLE.getString("EXECUTION_FILTER");
		property = System.getProperty(propertyKey);
		if (property == null) property = System.getProperty(BUNDLE_EN.getString("EXECUTION_FILTER"));
		properties.setProperty(propertyKey, property);		
		
		propertyKey = BUNDLE.getString("EXECUTION_SPEED_SETTING_KEY");
		property = System.getProperty(propertyKey);
		if (property == null) property = System.getProperty(BUNDLE_EN.getString("EXECUTION_SPEED_SETTING_KEY"));
		properties.setProperty(propertyKey, property);		

		propertyKey = BUNDLE.getString("REPORT_NAME_SETTING_KEY");
		property = System.getProperty(propertyKey);
		if (property == null) property = System.getProperty(BUNDLE_EN.getString("REPORT_NAME_SETTING_KEY"));
		properties.setProperty(propertyKey, property);		
		
		propertyKey = BUNDLE.getString("ARCHIVE_DIR_SETTING_KEY");
		property = System.getProperty(propertyKey);
		if (property == null) property = System.getProperty(BUNDLE_EN.getString("ARCHIVE_DIR_SETTING_KEY"));
		properties.setProperty(propertyKey, property);		
		
		propertyKey = "sysnat.dummy.test.run";
		properties.setProperty(propertyKey, System.getProperty(propertyKey));
		propertyKey = "sysnat.autolaunch.report";
		properties.setProperty(propertyKey, System.getProperty(propertyKey));
		
		propertyKey = "settings.config";
		if (System.getProperty(propertyKey) != null) {			
			properties.setProperty(propertyKey, System.getProperty(propertyKey));
		}
		
		propertyKey = "sysnat.properties.path";
		if (System.getProperty(propertyKey) != null) {			
			properties.setProperty(propertyKey, System.getProperty(propertyKey));
		}
		
		return properties;
	}
}