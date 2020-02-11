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
import java.util.Properties;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.dialog.SysNatDialog;
import com.iksgmbh.sysnat.testresult.archiving.SysNatTestResultArchiver;

import de.iksgmbh.sysnat.docing.SysNatDocumentGenerator;

public class SysNatExecutor 
{
	public static final String MAVEN_OK = "OK";
	public static final File JAVA_HOME = new File("../../java");
	private static final String PATH_TO_MAVEN = "../../maven";

	public static void main(String[] args) 
	{
		// optional PRE-Phase: read settings from dialog
		SysNatDialog.doYourJob();
		
		String sysNatMode = System.getProperty(SysNatConstants.SYSNAT_MODE);
		if (SysNatConstants.SysNatMode.Testing.name().equals(sysNatMode)) 
		{
			
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
		else
		{
			String applicationUnderTest = ExecutionRuntimeInfo.getInstance().getDocApplicationName();
			SysNatDocumentGenerator.doYourJob(applicationUnderTest);
		}
	}

	private static void archiveIfNeccessary() 
	{
		final ExecutionRuntimeInfo executionInfo = ExecutionRuntimeInfo.getInstance();
		if (executionInfo.isTestReportToArchive()) {			
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
		
		String propertyKey = SysNatConstants.TEST_APPLICATION_SETTING_KEY;
		String property = System.getProperty(propertyKey);
		properties.setProperty(propertyKey, property);

		propertyKey = SysNatConstants.TEST_ENVIRONMENT_SETTING_KEY;
		property = System.getProperty(propertyKey);
		properties.setProperty(propertyKey, property);

		propertyKey = SysNatConstants.TEST_EXECUTION_FILTER_SETTING_KEY;
		property = System.getProperty(propertyKey);
		properties.setProperty(propertyKey, property);		
		
		propertyKey = SysNatConstants.TEST_BROWSER_SETTING_KEY;
		property = System.getProperty(propertyKey);
		properties.setProperty(propertyKey, System.getProperty(propertyKey));
		
		propertyKey = SysNatConstants.TEST_EXECUTION_SPEED_SETTING_KEY;
		property = System.getProperty(propertyKey);
		properties.setProperty(propertyKey, property);		

		propertyKey = SysNatConstants.TEST_REPORT_NAME_SETTING_KEY;
		property = System.getProperty(propertyKey);
		properties.setProperty(propertyKey, property);		
		
		propertyKey = SysNatConstants.TEST_ARCHIVE_DIR_SETTING_KEY;
		property = System.getProperty(propertyKey, "");
		properties.setProperty(propertyKey, property);		
		
		propertyKey = "sysnat.dummy.test.run";
		properties.setProperty(propertyKey, System.getProperty(propertyKey));
		
		propertyKey = SysNatConstants.RESULT_LAUNCH_OPTION_SETTING_KEY; // former: "sysnat.autolaunch.report";
		properties.setProperty(propertyKey, System.getProperty(propertyKey));
		
		propertyKey = SysNatConstants.TESTING_CONFIG_PROPERTY;
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