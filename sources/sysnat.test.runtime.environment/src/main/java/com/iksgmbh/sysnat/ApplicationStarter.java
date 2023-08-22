package com.iksgmbh.sysnat;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import com.iksgmbh.sysnat.common.helper.ErrorPageLauncher;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.ApplicationLoginParameter;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.domain.TestApplication;
import com.iksgmbh.sysnat.guicontrol.GuiControl;
import com.iksgmbh.sysnat.guicontrol.impl.SeleniumGuiController;
import com.iksgmbh.sysnat.guicontrol.impl.SwingGuiController;
import com.iksgmbh.sysnat.helper.PopupHandler;

/**
 * Starts an application by initializing its GuiControl and 
 * logs in the user defined in the application properties.

 * @author Reik Oberrath
 */
public class ApplicationStarter
{
	private static final ResourceBundle ERR_MSG_BUNDLE = ResourceBundle.getBundle("bundles/ErrorMessages", Locale.getDefault());

	public static GuiControl doYourJob(ExecutableExample executableExample, TestApplication testApplication)
	{
		HashMap<String, String> startParams = testApplication.getStartParameterValues();
		
		if (testApplication.isWebApplication()) 
		{
			return startWebApplication(executableExample, testApplication, startParams);
		} 
		else if (testApplication.isSwingApplication()) 
		{
			return startSwingApplication(executableExample, testApplication, startParams);
		}
		else if (testApplication.getType() == SysNatConstants.ApplicationType.Composite) 
		{
			List<String> applicationNames = testApplication.getElementAppications();
			if ("true".equals(testApplication.getEnvProperty("StartCompositsLazy"))) {
				TestApplication testAppToStart = new TestApplication(applicationNames.get(0));
				return doYourJob(executableExample, testAppToStart);  // Start only first app - others are started lazily!
			}
			GuiControl firstAppGuiControl = null;
			
			for (String app : applicationNames) 
			{
				TestApplication testAppToStart = new TestApplication(app);
				GuiControl guiControl = doYourJob(executableExample, testAppToStart);
				if (guiControl == null) return null;
				if (firstAppGuiControl == null) firstAppGuiControl = guiControl;
			}
			
			return firstAppGuiControl;			
		}
		
		throw new RuntimeException("Application Type '" + testApplication.getType().name() + "' not supported!");	
	}


	private static GuiControl startSwingApplication(ExecutableExample executableExample,
	                                                TestApplication testApplication,
	                                                HashMap<String, String> startParams)
	{
		loadSystemPropertiesOfTestapplication(); 
		SwingGuiController swingGuiController = new SwingGuiController();
		ExecutionRuntimeInfo.getInstance().addGuiController(testApplication.getName(), swingGuiController);
		executableExample.setActiveGuiControllerFor(testApplication.getName());
		
		boolean applicationStarted = swingGuiController.init(startParams);
		
		if ( ! applicationStarted ) 
		{
			String errorMessage = ERR_MSG_BUNDLE.getString("AppNotAvailable")
					                            .replace("XY", startParams.get(SysNatConstants.SwingStartParameter.MainFrameTitle.name()));
			String hintMessage = ERR_MSG_BUNDLE.getString("AppNotAvailableHint");
			ErrorPageLauncher.doYourJob(errorMessage, hintMessage,
					                    ERR_MSG_BUNDLE.getString("InitialisationError"));
			return null;
		}
		
		if (testApplication.withLogin()) {
			applicationStarted = executeLogin(executableExample, testApplication);
		}
		if (! applicationStarted) return null;
		
		
		if (System.getProperty("maximizeApplicationWindow", "true").equalsIgnoreCase("true")) {
			swingGuiController.maximizeWindow();
			swingGuiController.windowToFront();
			swingGuiController.clickOnScreenCoordinate(1840, 25);
		}
		
		return swingGuiController;
	}


	private static GuiControl startWebApplication(ExecutableExample executableExample,
	                                              TestApplication testApplication,
	                                              HashMap<String, String> startParams)
	{
		SeleniumGuiController seleniumGuiController = new SeleniumGuiController();
		ExecutionRuntimeInfo.getInstance().addGuiController(testApplication.getName(), seleniumGuiController);
		executableExample.setActiveGuiControllerFor(testApplication.getName());
		
		boolean applicationStarted = seleniumGuiController.init(startParams);
		
		if ( ! applicationStarted ) 
		{
			String errorMessage = ERR_MSG_BUNDLE.getString("AppNotAvailable").replace("XY", startParams.get("starturl"));
			String hintMessage = ERR_MSG_BUNDLE.getString("AppNotAvailableHint");
			ErrorPageLauncher.doYourJob(errorMessage, hintMessage,
					                    ERR_MSG_BUNDLE.getString("InitialisationError"));
			return null;
		}
		
		PopupHandler.setTestCase(executableExample);
		if (testApplication.withLogin()) {
			applicationStarted = executeLogin(executableExample, testApplication);
		}

		if (! applicationStarted) return null;
		seleniumGuiController.maximizeWindow();
		seleniumGuiController.windowToFront();
		return seleniumGuiController;
	}

    
    private static void loadSystemPropertiesOfTestapplication()
	{
    	File rootDir = SysNatFileUtil.getTestExecutionRootDir();
    	File propertyFile = new File(rootDir, SysNatConstants.SYSTEM_PROPERTIES_FILENAME);
    	Properties propertiesToLoadToJVM = new Properties();
    	SysNatFileUtil.loadPropertyFile(propertyFile, propertiesToLoadToJVM);
    	propertiesToLoadToJVM.entrySet().stream()
    	                     .forEach(entry -> System.setProperty(entry.getKey().toString(), entry.getValue().toString()));
	}


	public static boolean executeLogin(ExecutableExample executableExample, TestApplication testApplication) 
    {
    	if ("false".equalsIgnoreCase( System.getProperty(SysNatConstants.SYSNAT_DUMMY_TEST_RUN))) 
    	{
    		try {				
    			String appName = testApplication.getName();
				Map<ApplicationLoginParameter, String> loginParameter = testApplication.getLoginParameter();
				executableExample.getApplicationSpecificLanguageTemplates(appName).doLogin(loginParameter);
			} catch (Exception e) {
				System.err.println("###################################################");
				e.printStackTrace();
				System.err.println("###################################################");
				return false;
			}
    	} 
    	else 
    	{
    		System.err.println("ATTENTION: This is a dummy test run!");
    	}
    	return true;
    }
	
}
