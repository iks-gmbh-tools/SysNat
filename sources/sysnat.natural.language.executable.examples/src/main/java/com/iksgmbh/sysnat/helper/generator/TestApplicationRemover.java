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
package com.iksgmbh.sysnat.helper.generator;

import java.io.File;
import java.util.List;

import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;


/**
 * Deletes all files and artefacts belonging to a defined test applications.
 * 
 * @author Reik Oberrath
 */
public class TestApplicationRemover
{
	private static final String TestApplicationName = "DuckDuckGoSearch";
	
	private static String testApplicationName = null;
	
	public static void main(String[] args) {
		doYourJob(TestApplicationName);
	}
	
	public static String doYourJob(String testAppName)
	{
		testApplicationName = testAppName;
		System.setProperty("root.path", new File("").getAbsolutePath());

		System.out.println("------------------------------------------------------------------------------------------------------");
		System.out.println("Removing test application '" + testApplicationName + "':");
		System.out.println("");
		
		System.out.println("Removing property file...");
		removePropertyFile();
		System.out.println("Done.");
		
		System.out.println("Removing LanguageTemplatesBasics...");
		removeLanguageTemplatesBasicsJavaFile();
		System.out.println("Done.");
		
		System.out.println("Adapting testing.config...");
		adaptTestingConfig();
		System.out.println("Done.");
		
		System.out.println("Removing folder for natural language files...");
		removeExecutableExampleFolder();
		System.out.println("Done.");

		System.out.println("Removing folder for test data files...");
		removeTestDataFolder();
		System.out.println("Done.");

		System.out.println("Removing folder for help files...");
		removeHelpFiles();
		System.out.println("Done.");
		
		System.out.println("");
		System.out.println("Done with all.");
		System.out.println("------------------------------------------------------------------------------------------------------");
		
		return "Deletion successfull.";
	}

	private static void removeHelpFiles() 
	{
		File file = TestApplicationDevGen.buildHelpFile(testApplicationName);
		tryToDelete(file);
	}
	
	
	private static void tryToDelete(File folderOrFile)
	{
		if (! folderOrFile.exists()) {
			System.out.println("ATTENTION: Not found to delete: " + folderOrFile.getAbsolutePath());
			return;
		}
		
		if (folderOrFile.isFile()) {
			boolean ok = SysNatFileUtil.deleteFile(folderOrFile);
			if (!ok) System.out.println("ATTENTION: Could not remove file '" + folderOrFile.getAbsolutePath() + "'.");
		} else {
			boolean ok = SysNatFileUtil.deleteFolder(folderOrFile);
			if (!ok) System.out.println("ATTENTION: Could not remove folder '" + folderOrFile.getAbsolutePath() + "'.");
		}
	}

	private static void removeTestDataFolder() 
	{
		File folder = TestApplicationDevGen.buildTestDataFolder(testApplicationName);
		tryToDelete(folder);
	}
	
	private static void removeExecutableExampleFolder()
	{
		File folder = TestApplicationDevGen.buildNLFolder(testApplicationName);
		tryToDelete(folder);
	}

	private static void adaptTestingConfig()
	{
		File settingsFile = TestApplicationDevGen.buildTestingConfigFile();
		List<String> content = SysNatFileUtil.readTextFile(settingsFile);
		boolean savingNecessary = false;
		boolean ignoreLine = false;
		StringBuffer newContent = new StringBuffer();
		String otherTestApp = "?";
		
		for (String line : content)
		{
			if (line.contains(testApplicationName)) 
			{
				savingNecessary = true;
				if (line.startsWith("#")) {
					if (line.startsWith("# Known Test Applications:")) {
						line = line.replace(", " + testApplicationName, "")
								   .replace(testApplicationName, "");	
						int pos = line.lastIndexOf(",");
						if (pos == -1) pos = line.lastIndexOf(":");
						otherTestApp = line.substring(pos + 1);
					} else {
						if (! line.contains("TargetEnv")) ignoreLine = true;
					}
				} else {
					line = line.replace(testApplicationName, otherTestApp.trim());
				}
			}
			if (ignoreLine) {
				ignoreLine = false;
			} else {
				newContent.append(line).append(System.getProperty("line.separator"));
			}
		}
		
		if (savingNecessary) {
			SysNatFileUtil.writeFile(settingsFile, newContent.toString());			
		} else {
			System.out.println("ATTENTION: No occurence of " + testApplicationName + " found in " + SysNatConstants.TESTING_CONFIG_PROPERTY + ".");
		}
	}

	private static void removeLanguageTemplatesBasicsJavaFile()
	{
		File folder = TestApplicationDevGen.buildJavaFilesFolder(testApplicationName);
		tryToDelete(folder);
	}


	private static void removePropertyFile()
	{
		File propertiesFile = TestApplicationDevGen.buildTestAppPropertiesFile(testApplicationName);
		tryToDelete(propertiesFile);
	}
}
