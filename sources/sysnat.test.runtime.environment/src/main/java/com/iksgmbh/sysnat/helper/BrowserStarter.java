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
import java.net.MalformedURLException;
import java.util.HashMap;

import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.GeckoDriverService;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.BrowserType;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

public class BrowserStarter 
{
   private ExecutionRuntimeInfo executionInfo = ExecutionRuntimeInfo.getInstance();
   private WebDriver webDriver;
   
   public static WebDriver doYourJob() 
   {
      try {
         BrowserStarter browserStarter = new BrowserStarter();
         browserStarter.startBrowserAndInitWebDriver();
         return browserStarter.webDriver;
      } catch (Exception e) {
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }
   
   public static BrowserType getCurrentBrowserType() {
	   return ExecutionRuntimeInfo.getInstance().getTestBrowserType();
   }
   
	public void closeCurrentUI()
	{
		if (webDriver == null) return;
		
		try {
			webDriver.close();
		} catch (Exception e) {
			// ignore exception
		}

		try {
			webDriver.quit();
		} catch (Exception e) {
			// ignore exception
		}
	}
	
    private void startBrowserAndInitWebDriver() throws MalformedURLException
    {
      closeCurrentUI();
      
      if (SysNatConstants.BrowserType.FIREFOX.equals(executionInfo.getTestBrowserType()))
      {
         initFirefoxWebDriver();
      }
      else if (SysNatConstants.BrowserType.CHROME == executionInfo.getTestBrowserType())
      {
         initChromeWebDriver();
      } 
      else if (SysNatConstants.BrowserType.EDGE == executionInfo.getTestBrowserType())
      {
         initEdgeWebDriver();
      }
      else 
      {
         throw new SysNatException("Unknown browser type: '" + executionInfo.getTestBrowserType().name() + "'.");
      }
      
      webDriver.manage().window().maximize();
    }

	private void initChromeWebDriver() throws MalformedURLException
	{
		System.out.println("Initializing Chrome web driver...");
		HashMap<String, Object> preferences = new HashMap<>();

		final ChromeOptions options = new ChromeOptions();
		options.addArguments("--start-maximized");
		options.addArguments("--test-type");
		options.addArguments("--disable-extensions"); // to disable browser extension popup
		options.setExperimentalOption("prefs", preferences);

		preferences.put("safebrowsing.enabled", true);
		preferences.put("browser.set_download_behavior",
		                "{ behavior: 'allow' , downloadPath: '" + SysNatFileUtil.getDownloadDir().getAbsolutePath() + "'}");
		preferences.put("download.prompt_for_download", false);
		preferences.put("download.directory_upgrade", true);
		preferences.put("download.default_directory", SysNatFileUtil.getDownloadDir().getAbsolutePath());
		preferences.put("plugins.always_open_pdf_externally", true);
		preferences.put("plugins.plugins_disabled", new String[] { "Chrome PDF Viewer" });
		
		options.addArguments("--remote-allow-origins=*","ignore-certificate-errors");

		if (executionInfo.isOS_Windows()) {
			System.setProperty("webdriver.chrome.driver", getExecutable("sysnat.webdriver.executable.chrome"));
			webDriver = new ChromeDriver(options);
		} else {
			throw new RuntimeException("Non-Windows systems not yet supported.");
		}
	}

   private void initFirefoxWebDriver() throws MalformedURLException 
   {
      System.out.println("Initializing Firefox web driver using geckodriver...");

      final FirefoxOptions firefoxOptions = new FirefoxOptions();
      firefoxOptions.setBinary(new FirefoxBinary(SysNatFileUtil.getFirefoxExecutable()));
      firefoxOptions.setProfile(createFirefoxProfile());

      if (executionInfo.isOS_Windows())  
      {
         System.setProperty("webdriver.gecko.driver", getExecutable("sysnat.webdriver.executable.firefox.gecko"));
         // System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE,"true"); Selenium 3 Setting
         System.setProperty(GeckoDriverService.GECKO_DRIVER_LOG_PROPERTY,"/dev/null");
         webDriver = new FirefoxDriver(firefoxOptions);
      } else {
         throw new RuntimeException("Non-Windows systems not yet supported.");
      }
   }

   private void initEdgeWebDriver() throws MalformedURLException 
   {
      System.out.println("Initializing Edge web driver...");

      final EdgeOptions options = new EdgeOptions();
      options.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.ACCEPT);
      options.addArguments("--remote-allow-origins=*");

      if (executionInfo.isOS_Windows())  
      {
         System.setProperty("webdriver.edge.driver", getExecutable("sysnat.webdriver.executable.edge"));
         webDriver = new EdgeDriver(options);
      } else {
         throw new RuntimeException("Non-Windows systems not yet supported.");
      }
   }


   private FirefoxProfile createFirefoxProfile() 
   {
      FirefoxProfile profile = new FirefoxProfile();
      profile.setAcceptUntrustedCertificates(true);

      profile.setPreference("browser.download.folderList", 2);
      profile.setPreference("browser.download.useDownloadDir", true);
      profile.setPreference("browser.download.dir", SysNatFileUtil.getDownloadDir().getAbsolutePath());
      profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/pdf");

      profile.setPreference("browser.download.manager.useWindow", false);
      profile.setPreference("browser.download.manager.closeWhenDone", true);
      profile.setPreference("browser.download.manager.showWhenStarting", false);
      profile.setPreference("pdfjs.disabled", true);


      /*
      profile.setPreference("browser.download.folderList", 2);
      profile.setPreference("services.sync.prefs.sync.browser.download.manager.showWhenStarting", false);
      profile.setPreference("plugin.disable_full_page_plugin_for_types", "application/rtf");
      */
      return profile;
   }
   
   private String getExecutable(final String executableType)
   {
      String path = System.getProperty("relative.path.to.webdrivers").replace(SysNatConstants.ROOT_PATH_PLACEHOLDER, System.getProperty("root.path"));
      String toReturn = path + "/" + System.getProperty( executableType );
      File file = new File(toReturn);
      if ( ! file.exists() ) {
         throw new RuntimeException("Could not find: " + file.getAbsolutePath());
      }
      
      return toReturn;
   }
}

