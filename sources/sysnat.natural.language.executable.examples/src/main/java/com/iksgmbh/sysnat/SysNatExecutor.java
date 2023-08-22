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
import java.io.FileFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.DialogStartTab;
import com.iksgmbh.sysnat.dialog.SysNatDialog;
import com.iksgmbh.sysnat.domain.TestApplication;
import com.iksgmbh.sysnat.helper.PomFileDependencyInjector;
import com.iksgmbh.sysnat.helper.PropertyFilesPreparer;
import com.iksgmbh.sysnat.testresult.archiving.SysNatTestResultArchiver;

import de.iksgmbh.sysnat.docing.SysNatDocumentGenerator;

public class SysNatExecutor 
{
	public static final String MAVEN_OK = "OK";
	public static final File JAVA_JDK_DIR = new File("C:/dev/java/openjdk-20.0.2");
	
	private static final String PATH_TO_MAVEN = "../../maven";
	private static final String[] SYSNAT_FILES = {"pom.xml", "AvailableNaturalLanguageScripts.properties"};
	private static final boolean updateSysNatJarsInLocalMavenRepository = false;
	
	public static void main(String[] args) 
	{
		// optional PRE-Phase: read settings from dialog
		SysNatDialog.doYourJob();
		
		String sysNatMode = getSysNatMode();
		if (SysNatConstants.SysNatMode.Testing.name().equals(sysNatMode)) 
		{
			
			// mandatory Phase A: translate natural language into JUnit test code
			boolean generationSuccessful = SysNatJUnitTestClassGenerator.doYourJob();
			
			if (generationSuccessful) {
				cleanGenerationTargetRootDir();
				List<String> fingAppsToStart = findAppsToStart();
				generationSuccessful = PomFileDependencyInjector.doYourJob(fingAppsToStart);
				if (generationSuccessful) generationSuccessful = PropertyFilesPreparer.doYourJob(fingAppsToStart);
			}
			
			if (ExecutionRuntimeInfo.getInstance().getTestApplication().getName().equals("InkaClient")) {
				String inkaVersion = readInkaVersionFromCurrentProperties();  // depends on environment !
				writeVersionInPomFile(inkaVersion);
				Properties properties = new Properties();
				properties.put("skipTests", "true");
				startMaven( properties, "../sysnat.test.runtime.environment/pom.xml", "install");
			}
			
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

	private static void writeVersionInPomFile(String inkaVersion)
	{
		if (inkaVersion == null) return;
		List<String> newContent = new ArrayList<>();
		String pom = "../sysnat.test.runtime.environment/pom.xml";
		List<String> lines = SysNatFileUtil.readTextFile(pom);
		for (String line : lines) {
			if (line.trim().startsWith("<inka.version>")) {
				newContent.add("    <inka.version>" + inkaVersion + "</inka.version>");
			} else {
				newContent.add(line);
			}
		}
		SysNatFileUtil.writeFile(new File(pom), newContent);
	}

	private static String readInkaVersionFromCurrentProperties()
	{
		try {
			List<String> properties = SysNatFileUtil.readTextFile("../sysnat.test.execution/InkaClient.properties");
			for (String p : properties) {
				if (p.trim().startsWith("client.version")) 
				{
					String[] splitResult = p.split("=");
					return splitResult[1].trim();
				}
			}
		} catch (Exception e) {
			// ignore
		}
		return null;
		
	}

	private static void cleanGenerationTargetRootDir()
	{
		File rootDir = SysNatFileUtil.getTestExecutionRootDir();
		File[] todelete = rootDir.listFiles(new FileFilter() {
			@Override public boolean accept(File file)
			{
				for (String filename : SYSNAT_FILES) {
					if (file.isDirectory()) return false;
					if (file.getName().equals(filename)) return false;
					if (file.getName().startsWith(".")) return false;
				}
				return true;
			}
		});
		
		for (File file : todelete) {
			boolean ok = file.delete();
			if (!ok) System.err.println("Warning: Could not delete " + file.getAbsolutePath() + ".");
		}
	}

	private static List<String> findAppsToStart()
	{
		final List<String> toReturn = new ArrayList<>();
		
		TestApplication testApplication = ExecutionRuntimeInfo.getInstance().getTestApplication();
		
		if (testApplication.isCompositeApplication()) 
		{
			testApplication.checkNumberOfComposites();
			List<String> composites = testApplication.getElementAppications();
			toReturn.addAll(composites);
		}
		else 
		{
			toReturn.add(testApplication.getName());
		}

		return toReturn;
	}

	private static String getSysNatMode()
	{
		String sysNatMode = System.getProperty(SysNatConstants.SYSNAT_MODE);
		if (sysNatMode == null) {
			if (ExecutionRuntimeInfo.getInstance().getDialogStartTab() == DialogStartTab.Testing)
			{
				sysNatMode = SysNatConstants.SysNatMode.Testing.name();
			} else {
				sysNatMode = SysNatConstants.SysNatMode.Docing.name();
			}
		}
		return sysNatMode;
	}

	private static void archiveIfNeccessary() 
	{
		final ExecutionRuntimeInfo executionInfo = ExecutionRuntimeInfo.getInstance();
		if (executionInfo.isTestReportToArchive()) {			
			SysNatTestResultArchiver.doYourJob(executionInfo.getReportFolder());
		}
	}

	/**
	 * @param relativePathToPom 
	 * @return result of execution 
	 */
	public static String startMaven(Properties jvmSystemProperties, String relativePathToPom, String goal)
	{
		if (! JAVA_JDK_DIR.exists()) {
			throw new SysNatException("Defined JAVA_JDK_DIR ('" + JAVA_JDK_DIR + "') does not exist. "
					+ "Please correct path to Java JDK in source code (SysNatExecutor.java).");
		}
		final InvocationRequest request = new DefaultInvocationRequest();
		final String pomFile = SysNatFileUtil.findAbsoluteFilePath(relativePathToPom);
		request.setPomFile(new File(pomFile));
		request.setProperties(jvmSystemProperties);
		request.setJavaHome(JAVA_JDK_DIR);
		request.setMavenOpts("-Dfile.encoding=UTF-8");
		
		final List<String> goals = new ArrayList<>();
		goals.add("clean"); 
		goals.add(goal);
		request.setGoals(goals);
		
		final Invoker mavenInvoker = new DefaultInvoker();
		File mavenDir = new File(PATH_TO_MAVEN);
		mavenInvoker.setMavenHome(mavenDir);
		System.out.println(mavenDir.getAbsolutePath());
		
		InvocationResult result = null;
		try {
			request.setInputStream(InputStream.nullInputStream());
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
		if (updateSysNatJarsInLocalMavenRepository) {
			properties.setProperty("skipTests", "true"); 
			startMaven( properties, "../sysnat.parent/pom.xml", "install" );
			properties = collectSysNatSystemProperties();
		}
		return startMaven( properties, "../sysnat.test.execution/pom.xml", "test" );
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
		
		propertyKey = SysNatConstants.SYSNAT_DUMMY_TEST_RUN;
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