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
package com.iksgmbh.sysnat.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.TargetEnvironment;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.TestPhase;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

public class SysNatTestRuntimeUtil 
{
	public static boolean doesTestBelongToApplicationUnderTest(ExecutableExample executableExample) 
	{
		String aut = ExecutionRuntimeInfo.getInstance().getTestApplicationName();
		String fullClassName = executableExample.getClass().getName().toLowerCase();
		int pos = fullClassName.indexOf('.');
		if (pos == -1) {
			// class belongs to default package: avoid this !
			return true;
		}
		String mainPackage = fullClassName.substring(0, pos);
		return aut.toLowerCase().contains(mainPackage);
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

	public static void copyContextData(ExecutableExample source, ExecutableExample target)
	{
		target.setXXID(source.getXXID());
		target.setGuiController(source.getGuiController());
		target.setReportMessages(source.getReportMessages());
		target.setExecutionFilter(source.getExecutionFilterList());
		target.setTestData(source.getTestData());
		target.setTestObjects(source.getTestObjects());
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
			TargetEnvironment.valueOf(value);
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

	


	public static boolean isTestPhaseKeyword(String word) 
	{
		TestPhase[] values = TestPhase.values();
		for (TestPhase testPhase : values) {
			if (testPhase.name().equals(word.toUpperCase())) {
				return true;
			}
		}
		return false;
	}
	
}