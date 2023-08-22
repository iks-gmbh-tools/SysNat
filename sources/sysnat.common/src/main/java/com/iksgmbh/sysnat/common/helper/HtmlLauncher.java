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

import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;

import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;

public class HtmlLauncher 
{

	public static void doYourJob(String htmlFileAsString)
	{
		final File file = new File(htmlFileAsString);
		doYourJob(file);
	}
	
	public static void doYourJob(File file)
	{
		Runtime rt = Runtime.getRuntime();
		try {
			final String pathToHtmlFile = file.getCanonicalPath();
			final String browserTypeToUse = System.getProperty(SysNatConstants.TEST_BROWSER_SETTING_KEY);
			
			if (browserTypeToUse.equalsIgnoreCase("CHROME")) 
			{
				System.setProperty("webdriver.chrome.driver", getPathToBrowserExe(browserTypeToUse));
				final ChromeOptions options = new ChromeOptions();
				options.addArguments("--start-maximized");
				options.addArguments("--test-type");
				options.addArguments("--disable-extensions"); // to disable browser extension popup
				ChromeDriver chromeDriver = new ChromeDriver(options);
				chromeDriver.manage().window().maximize();
				chromeDriver.get("file:///" + pathToHtmlFile);
			} 
			else if (browserTypeToUse.equalsIgnoreCase("Edge")) 
			{
				System.setProperty("webdriver.edge.driver", getPathToBrowserExe(browserTypeToUse));
				final EdgeOptions options = new EdgeOptions();
			    options.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.ACCEPT);
			    options.addArguments("--remote-allow-origins=*");
				EdgeDriver webDriver = new EdgeDriver(options);
				webDriver.manage().window().maximize();
				webDriver.get("file:///" + pathToHtmlFile);
			}
			else if (browserTypeToUse.equalsIgnoreCase("IE")) 
			{
				System.setProperty("webdriver.ie.driver", getPathToBrowserExe(browserTypeToUse));
				final InternetExplorerOptions options = new InternetExplorerOptions();
				options.ignoreZoomSettings();
				options.introduceFlakinessByIgnoringSecurityDomains();
				InternetExplorerDriver webDriver = new InternetExplorerDriver(options);
				webDriver.manage().window().maximize();
				webDriver.get("file:///" + pathToHtmlFile);
			}
			else
			{
				// Firefox
				final String pathToBrowserExe = getPathToBrowserExe(browserTypeToUse);
				rt.exec(new String[] { pathToBrowserExe, "-url", "file:///" + pathToHtmlFile });
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String extractExecutableName(String pathToBrowserExe)
	{
		int pos1 = pathToBrowserExe.lastIndexOf("/");
		int pos2 = pathToBrowserExe.lastIndexOf("\\");
		
		if (pos2 > pos1) pos1 = pos2;
		
		return pathToBrowserExe.substring(pos1+1);
	}

	public static String getPathToBrowserExe(final String browserTypeToUse)
	{
		final String absolutePathToExecutable = System.getProperty("absolute.path.to.browser.executable");	
		if (isAbsolutePathToExecutableToBeUsed(absolutePathToExecutable, browserTypeToUse)) {
			return absolutePathToExecutable;
		}
		
		String relativePathToWebdriverDir = System.getProperty("relative.path.to.webdrivers");	
		relativePathToWebdriverDir = relativePathToWebdriverDir.replace(SysNatConstants.ROOT_PATH_PLACEHOLDER, System.getProperty("root.path"));
			
		if (browserTypeToUse.equalsIgnoreCase("Chrome")) {
			return relativePathToWebdriverDir + "//" + System.getProperty("sysnat.webdriver.executable.chrome");
		}
		
		if (browserTypeToUse.equalsIgnoreCase("IE")) {
			return relativePathToWebdriverDir + "//" + System.getProperty("sysnat.webdriver.executable.ie");
		}

		if (browserTypeToUse.equalsIgnoreCase("Firefox")) {
			return relativePathToWebdriverDir + "//" + System.getProperty("sysnat.webdriver.executable.firefox");
		}
		
		if (browserTypeToUse.equalsIgnoreCase("Edge")) {
			return relativePathToWebdriverDir + "//" + System.getProperty("sysnat.webdriver.executable.edge");
		}
		
		throw new SysNatException("Browser type '" + browserTypeToUse + "' is not supported.");
	}

	private static boolean isAbsolutePathToExecutableToBeUsed(String absolutePathToExecutable, String browserTypeToUse)
	{
		if (absolutePathToExecutable == null) return false;
		
		File file = new File(absolutePathToExecutable);
		if (! file.exists()) return false;
		
		String executableName = extractExecutableName(absolutePathToExecutable);
		String executableNameWithoutExtension = executableName.substring(0,executableName.length()-4);
		
		return browserTypeToUse.toUpperCase().contains(executableNameWithoutExtension.toUpperCase());
	}

}
