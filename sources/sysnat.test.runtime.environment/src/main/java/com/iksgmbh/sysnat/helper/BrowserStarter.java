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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
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
   
	public void closeCurrentUI()
	{
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
      
      if (SysNatConstants.BrowserType.FIREFOX.equals(executionInfo.getBrowserTypeToUse()))
      {
         initFireFoxWebDriver();
      }
      else if (SysNatConstants.BrowserType.CHROME == executionInfo.getBrowserTypeToUse())
      {
         initChromeWebDriver();
      } 
      else if (SysNatConstants.BrowserType.IE == executionInfo.getBrowserTypeToUse())
      {
         initInternetExplorerWebDriver();
      }
      else if (SysNatConstants.BrowserType.FIREFOX_45_9 == executionInfo.getBrowserTypeToUse())
      {
         initFireFoxWeb_45_9_Driver();
      }
      else 
      {
         throw new SysNatException("Unknown browser type: '" + executionInfo.getBrowserTypeToUse().name() + "'.");
      }
      
      webDriver.manage().window().maximize();
    }

   private void initInternetExplorerWebDriver() throws MalformedURLException 
   {
      System.out.println("Initializing Internet Explorer web driver...");

      final InternetExplorerOptions options = new InternetExplorerOptions();
      options.withInitialBrowserUrl("www.iks-gmbh.com");
      options.ignoreZoomSettings();
      options.introduceFlakinessByIgnoringSecurityDomains();
      //options.withAttachTimeout(5, TimeUnit.SECONDS );
      //options.requireWindowFocus();
      //options.enableNativeEvents();
      //options.setPageLoadStrategy(PageLoadStrategy.EAGER);
      //options.destructivelyEnsureCleanSession();
      

       if (executionInfo.isOS_Windows())  {
          //System.setProperty("java.net.preferIPv4Stack", "true");
         System.setProperty("webdriver.ie.driver", getExecutable("sysnat.webdriver.executable.ie"));
         webDriver = new InternetExplorerDriver(options);
         //webDriver.switchTo().defaultContent();
      } else {
         throw new RuntimeException("Non-Windows systems not yet implemented.");
      }
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

		if (executionInfo.isOS_Windows()) {
			System.setProperty("webdriver.chrome.driver", getExecutable("sysnat.webdriver.executable.chrome"));
			// webDriver = new ChromeDriver(cap); does also not work :-)
			webDriver = new ChromeDriver(options);
		} else {
			throw new RuntimeException("Non-Windows systems not yet implemented.");
		}
	}

   private void initFireFoxWebDriver() throws MalformedURLException 
   {
      System.out.println("Initializing Firefox web driver using geckodriver...");

      final FirefoxOptions firefoxOptions = new FirefoxOptions();
      firefoxOptions.setBinary(new FirefoxBinary(SysNatFileUtil.getFirefoxExecutable()));
      firefoxOptions.setProfile(createFirefoxProfile());

      if (executionInfo.isOS_Windows())  
      {
         System.setProperty("webdriver.gecko.driver", getExecutable("sysnat.webdriver.executable.firefox.gecko"));
         System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE,"true");
         System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE,"/dev/null");
         webDriver = new FirefoxDriver(firefoxOptions);
         
      } else {
         throw new RuntimeException("Non-Windows systems not yet supported.");
      }
   }

   private void initFireFoxWeb_45_9_Driver() throws MalformedURLException 
   {
      System.out.println("Initializing Firefox v45.9 web driver...");

      final FirefoxOptions firefoxOptions = new FirefoxOptions();
      firefoxOptions.setBinary(getFireFoxBinary("sysnat.path.to.firefox_45_9.dir"));
      firefoxOptions.setProfile(createFirefoxProfile());

      if (executionInfo.isOS_Windows())  
      {
         System.setProperty("webdriver.gecko.driver", getExecutable("sysnat.webdriver.executable.firefox_45_9.gecko"));
         System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "false");
         webDriver = new FirefoxDriver(firefoxOptions);
         
      } else {
         throw new RuntimeException("Non-Windows systems not yet implemented.");
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
   
    private FirefoxBinary getFireFoxBinary(final String systemPropertyKey) 
    {
      final File firefoxExe = SysNatFileUtil.getFirefoxExecutable();
      if (! firefoxExe.exists()) {
         throw new RuntimeException("Firefox Executable not found: " + firefoxExe.getAbsolutePath());
      }
      try {
         return new FirefoxBinary(firefoxExe);
      } catch (Throwable t) {
         t.printStackTrace();
         throw t;
      }
   }
   
   private String getExecutable(final String executableType)
   {
      String path = System.getProperty("relative.path.to.webdrivers").replace(SysNatConstants.ROOT_PATH_PLACEHOLDER, System.getProperty("root.path"));
      String toReturn = path + "/" + System.getProperty( executableType );
      if ( ! new File(toReturn).exists() ) {
         throw new RuntimeException("Could not find: " + executableType);
      }
      
      return toReturn;
   }
}

