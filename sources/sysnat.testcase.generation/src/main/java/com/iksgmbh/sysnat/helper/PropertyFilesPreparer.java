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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.domain.TestApplication;

/**
 * Deletes non-required files from root directory of the sysnat.test.execution project
 * and copies into it all those property files that are defined for the test application.
 * 
 * @author Reik Oberrath
 */

public class PropertyFilesPreparer 
{

	/**
	 * Reads natural language instruction files and transforms the instructions given in a domain language
	 * into java commands of JUnit java files.
	 * @param fingAppsToStart 
	 * @return true if JUnit test classes has been generated
	 */
	public static boolean doYourJob(List<String> fingAppsToStart) {
		return new PropertyFilesPreparer().prepositionFiles(fingAppsToStart);
	}

	protected boolean prepositionFiles(List<String> fingAppsToStart) 
	{
		for (String app : fingAppsToStart) 
		{
			// step 0: init
			final TestApplication testApplication = new TestApplication(app);
			File rootDir = SysNatFileUtil.getTestExecutionRootDir();

			// step 1: determine required files
			List<String> filesToCopy = getRequiredFiles(testApplication.getStartParameterValues());

			// step 2: copy to root
			filesToCopy.forEach(file -> preposition(file, rootDir, testApplication.getStartParameterValues()));

			// step 3: check system properties
			Map<String, String> systemProperties = testApplication.getSystemProperties();
			File propFile = new File(rootDir, SysNatConstants.SYSTEM_PROPERTIES_FILENAME);
			List<String> content = toText(systemProperties);
			if (propFile.exists()) 
			{
				List<String> alreadyExistingContent = SysNatFileUtil.readTextFile(propFile);
				alreadyExistingContent.addAll(content);
				content = alreadyExistingContent;
			}
			SysNatFileUtil.writeFile(propFile, content);
		}
		
		return true;
	}

	private List<String> toText(Map<String, String> systemProperties)
	{
		List<String> lines = new ArrayList<>();
		systemProperties.entrySet().forEach(entry -> lines.add(entry.getKey() + "=" + entry.getValue()));
		return lines;
	}

	private void preposition(String filename, File targetDir, HashMap<String, String> parameters)
	{
		String appInstallDir = parameters.get(SysNatConstants.SwingStartParameter.InstallDir.name());

		appInstallDir = appInstallDir.replaceAll("\"", "");
		File sourceFile;
		sourceFile = new File(appInstallDir, filename);
		SysNatFileUtil.copyBinaryFile(sourceFile, targetDir);
	}

	private List<String> getRequiredFiles(HashMap<String, String> parameters)
	{
		final List<String> toReturn = new ArrayList<>();
		String files = parameters.get(SysNatConstants.SwingStartParameter.ConfigFiles.name());
		
		if (files != null) {
			List<String> list = SysNatStringUtil.toList(files, ",");
			if (list.size() == 0 || list.get(0).trim().length() == 0) return toReturn;
			toReturn.addAll(list);
		}
			
		return toReturn;
	}

}	