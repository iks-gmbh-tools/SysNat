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
package com.iksgmbh.sysnat.common.helper;

import java.io.File;

import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

public class HtmlLauncher 
{

	public static void doYourJob(String reportFileAsString) 
	{
	    Runtime rt = Runtime.getRuntime();
	    try {
	    	File reportFile = new File( reportFileAsString );
	    	String pathToHtml = reportFile.getCanonicalPath();
	    	String firefoxExeWithPath = SysNatFileUtil.getFirefoxExecutable().getAbsolutePath();
			rt.exec(new String[]{ firefoxExeWithPath, "-url", "file:///" + pathToHtml });
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
