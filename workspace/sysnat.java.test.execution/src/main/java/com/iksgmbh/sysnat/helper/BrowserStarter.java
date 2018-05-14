package com.iksgmbh.sysnat.helper;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.iksgmbh.sysnat.ExecutionInfo;
import com.iksgmbh.sysnat.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.utils.SysNatConstants.BrowserType;

public class BrowserStarter 
{
	private ExecutionInfo executionInfo = ExecutionInfo.getInstance();
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
	
	
    private void startBrowserAndInitWebDriver() throws MalformedURLException
    {
		if (webDriver != null) {
			webDriver.quit();
		}
		
		DesiredCapabilities dc = null;
		
		if (BrowserType.FIREFOX.equals(executionInfo.getBrowserTypeToUse()))
		{
			dc = initFireFoxWebDriver();
		}
		else if (BrowserType.CHROME == executionInfo.getBrowserTypeToUse())
		{
			dc = initChromeWebDriver();
			dc.setJavascriptEnabled(true);
			
		} else {
			dc = initInternetExplorerWebDriver();
			dc.setJavascriptEnabled(true);
		}
		
    }

	private DesiredCapabilities initInternetExplorerWebDriver() throws MalformedURLException 
	{
		DesiredCapabilities dc;
		// setup internet explorer driver
		dc = DesiredCapabilities.internetExplorer();
		// If IE fail to work, please remove this and remove enable protected mode for all the 4 zones from Internet options
		dc.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
		//dc.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		dc.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
		dc.setCapability(InternetExplorerDriver.INITIAL_BROWSER_URL, "");
		if (executionInfo.isOS_Windows())  {
			System.setProperty("webdriver.ie.driver", System.getProperty("webdriver.dir.windows") + "//IEDriverServer.exe");
			dc.setCapability(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, true);
			webDriver = new InternetExplorerDriver(dc);
		} else {
			//System.setProperty("webdriver.ie.driver", TEST_PROPS.getProperty("webdriver.dir.windows") + "IEDriverServer");
			dc.setPlatform(Platform.ANY);
			webDriver = new RemoteWebDriver(getRemoteAddress(), dc);
		}
		return dc;
	}

	private DesiredCapabilities initChromeWebDriver() throws MalformedURLException 
	{
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--start-maximized");
		Map<String,Object> preferences = new HashMap<>();
		preferences.put("pdfjs.disabled", true);
		preferences.put("profile.default_content_settings.popups", 0);
		preferences.put("download.default_directory", SysNatFileUtil.getDownloadDir());		
		options.setExperimentalOption("prefs", preferences);		
	    options.addArguments("--test-type");
	    options.addArguments("--disable-extensions"); //to disable browser extension popup
		DesiredCapabilities dc = DesiredCapabilities.chrome();
		dc.setCapability(ChromeOptions.CAPABILITY, options);
		
		
		
		if (executionInfo.isOS_Windows())  {
			System.setProperty("webdriver.chrome.driver", System.getProperty("webdriver.dir.windows")+ "/chromedriver.exe");
			webDriver = new ChromeDriver(dc);
		} else {
			String driver = System.getProperty("webdriver.dir.linux" + "chromedriver");
			System.setProperty("webdriver.chrome.driver", driver);
			dc.setPlatform(Platform.ANY);
			webDriver = new RemoteWebDriver(getRemoteAddress(), dc);
		}
		return dc;
	}

	private DesiredCapabilities initFireFoxWebDriver() throws MalformedURLException 
	{
		DesiredCapabilities dc = null;
		if (executionInfo.isOS_Windows())  
		{
			System.out.println("Initializing Firefox Browser using geckodriver...");
			System.setProperty("webdriver.gecko.driver", SysNatFileUtil.getSysNatDir() + "/webdriver/geckodriver.exe");	
			System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE,"true");
			System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE,"/dev/null");
			FirefoxOptions firefoxOptions = new FirefoxOptions();
			firefoxOptions.setBinary(getFireFoxBinary());
			firefoxOptions.setProfile(createFirefoxProfile());
			webDriver = new FirefoxDriver(firefoxOptions);
			
		} else {
			dc = DesiredCapabilities.firefox();
			webDriver = new RemoteWebDriver(getRemoteAddress(), dc);
		}
		return dc;
	}


	private FirefoxProfile createFirefoxProfile() 
	{
		FirefoxProfile profile = new FirefoxProfile();
		profile.setAcceptUntrustedCertificates(true);
		profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/pdf");
		profile.setPreference("browser.download.folderList", 1);
		profile.setPreference("browser.download.dir", SysNatFileUtil.getDownloadDir().getAbsolutePath()); 
		profile.setPreference("browser.download.useDownloadDir", true);
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
	
    private FirefoxBinary getFireFoxBinary() 
    {
    	String firefoxDir = System.getProperty("user.dir") + '/' + System.getProperty("relative.path.to.firefox.root.dir"); 
		final File firefoxExe = new File( firefoxDir + "\\firefox.exe");
		if (! firefoxExe.exists()) {
			throw new RuntimeException("Firefox Executable not found: " + firefoxExe.getAbsolutePath());
		}
		return new FirefoxBinary(firefoxExe);
	}


	private URL getRemoteAddress() throws MalformedURLException
    {
        return new URL(System.getProperty("selenium.hub"));
    }
}
