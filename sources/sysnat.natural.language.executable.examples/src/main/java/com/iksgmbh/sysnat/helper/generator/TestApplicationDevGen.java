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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.ApplicationLoginParameter;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.ApplicationType;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.GuiType;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.SwingStartParameter;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.TargetEnvironment;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.WebStartParameter;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.helper.CommandLibraryCreator;
import com.iksgmbh.sysnat.helper.generator.PageObjectDevGen.PageChangeElement;
import com.iksgmbh.sysnat.helper.generator.PageObjectDevGen.PageChangeElementBuilder;
import com.iksgmbh.sysnat.language_templates.LanguageTemplateBasics.PageChangeEvent.EventType;

/**
 * Development helper to generate new test applications.
 * 
 * Adapt the value of the constants to your needs and execute this class as Java application.
 * 
 * Note: Java and Properties files that exist will not be overwritten!
 * 
 * @author Reik Oberrath
 */
public class TestApplicationDevGen
{
	private static final String TEMPLATE_TEST_FILE_DIR = "../sysnat.quality.assurance/src/test/resources/TestApplicationGenerator";
	static String naturalLanguageDir = "../sysnat.natural.language.executable.examples";
	static String sysnatTestRuntimeDir = "../sysnat.test.runtime.environment/src/main";
	static String propertiesPath = new File("").getAbsolutePath() + "/../../" + ExecutionRuntimeInfo.PROPERTIES_PATH;

	public enum DataType { TestApplicationName, APP_TYPE, initialEnvironmentName, initialEnvironmentType, withLogin,
		                   StartURL, Composites, JavaStartClass, LibDirs, InstallDir, MainFrameTitle, ConfigFiles,
		                   GuiNameOfLoginUserField, TechIdOfLoginUserField, LoginUserId, 
		                   GuiNameOfLoginPasswordField, TechIdOfLoginPasswordField, LoginPassword, TextOfLoginButton, TechIdOfLoginButton}

	private static final HashMap<GuiType, HashMap<String, String>> PREDEFINED_ELEMENTS = new HashMap<>();
	private static HashMap<DataType,String> data;
	
	public static void main(String[] args) 
	{
		HashMap<DataType,String> generationData = new HashMap<>();
		
		// Common Swing properties (always needed)		
		generationData.put(DataType.TestApplicationName, "DuckDuckGoSearch");
		generationData.put(DataType.APP_TYPE, ApplicationType.Web.name());
		generationData.put(DataType.initialEnvironmentType, TargetEnvironment.PRODUCTION.name());
		generationData.put(DataType.initialEnvironmentName, "Prod");
		generationData.put(DataType.withLogin, ("" + Boolean.FALSE).toLowerCase());
		
		// Web properties (needed only for APP_TYPE = Web)
		generationData.put(DataType.StartURL, "https://duckduckgo.com/");       
		// Swing properties (needed only for APP_TYPE = Swing)
		generationData.put(DataType.JavaStartClass, "de.iksgmbh.sysnat.StartClass");
		generationData.put(DataType.LibDirs, ".,lib");
		generationData.put(DataType.InstallDir, "C:\\Program Files\\app");
		generationData.put(DataType.MainFrameTitle,  "Title");
		generationData.put(DataType.ConfigFiles, "");
		// Composite properties (needed only for APP_TYPE = Composite)
		generationData.put(DataType.Composites, "TestApp1, TestApp2");          
		
		// Login settings (needed only for withLogin = true) 
		generationData.put(DataType.GuiNameOfLoginUserField, "User");
		generationData.put(DataType.TechIdOfLoginUserField, "usernameFieldId");
		generationData.put(DataType.LoginUserId, "Rob");
		generationData.put(DataType.GuiNameOfLoginPasswordField, "Password");
		generationData.put(DataType.TechIdOfLoginPasswordField, "pwFieldId");
		generationData.put(DataType.LoginPassword, "****");
		generationData.put(DataType.TextOfLoginButton, "Anmelden");
		generationData.put(DataType.TechIdOfLoginButton, "loginButtonId");
		
		doYourJob(generationData);
	}
	
	public static String doYourJob(HashMap<DataType, String> generationData)
	{
		data = generationData;
		
		if (isWithLogin()) 
		{			
			HashMap<String, String> prededinedButtons = new HashMap<>();
			prededinedButtons.put(data.get(DataType.TextOfLoginButton), data.get(DataType.TechIdOfLoginButton));
			PREDEFINED_ELEMENTS.put(GuiType.Button, prededinedButtons);
			
			HashMap<String, String> prededinedTextfields = new HashMap<>();
			prededinedTextfields.put(data.get(DataType.GuiNameOfLoginUserField), data.get(DataType.TechIdOfLoginUserField));
			prededinedTextfields.put(data.get(DataType.GuiNameOfLoginPasswordField), data.get(DataType.TechIdOfLoginPasswordField));
			PREDEFINED_ELEMENTS.put(GuiType.TextField, prededinedTextfields);
		}
		System.setProperty("root.path", new File("").getAbsolutePath());
		
		System.out.println("------------------------------------------------------------------------------------------------------");
		System.out.println("Generation of new test application '" + data.get(DataType.TestApplicationName) + "':");
		System.out.println("");
		
		System.out.println("Generating property file...");
		createPropertyFile();
		System.out.println("Done.");
		
		System.out.println("Generating LanguageTemplatesBasics...");
		createLanguageTemplatesBasicsJavaFile();
		System.out.println("Done.");

		System.out.println("Generating default PageObjects...");
		createDefaultPageObjectJavaFiles();
		System.out.println("Done.");

		System.out.println("Adapting testing.config...");
		adaptTestingConfig();
		System.out.println("Done.");
		
		System.out.println("Creating folder for natural language files...");
		createExecutableExampleFolder();
		System.out.println("Done.");

		System.out.println("Creating folder for test data files...");
		createTestDataFolder();
		System.out.println("Done.");

		System.out.println("Creating help file...");
		createHelpFile();
		System.out.println("Done.");
		
		System.out.println("");
		System.out.println("Done with all!");
		System.out.println("------------------------------------------------------------------------------------------------------");
		
		return "TestApplication generated";
	}



	private static void createHelpFile()
	{
		String testApplicationName = data.get(DataType.TestApplicationName);
		CommandLibraryCreator.updateFor(testApplicationName, buildLanguageTemplatesBasicsJavaFile(testApplicationName));
	}

	private static void createTestDataFolder() 
	{
		File folder = buildTestDataFolder(data.get(DataType.TestApplicationName));
		if (folder.exists()) {
			System.out.println("ATTENTION: Folder '" + folder.getName() + "' exists and was not overwritten!");
			return;
		}
		boolean ok = folder.mkdir();
		if (! ok) {
			System.err.println("Could not create: " + folder.getAbsolutePath());
		}
		
		File example = new File (TEMPLATE_TEST_FILE_DIR, "ExampleTestData.dat");
		String content = SysNatFileUtil.readTextFileToString(example);
		content = content.replace("<replaceDate>", new Date().toString()).replace("<replaceAppName>", data.get(DataType.TestApplicationName));
		example = new File (folder, "ExampleTestData.dat");
		SysNatFileUtil.writeFile(example, content);
	}

	
	private static void createExecutableExampleFolder()
	{
		File folder = buildNLFolder(data.get(DataType.TestApplicationName));
		if (folder.exists()) {
			System.err.println("ATTENTION: Folder '" + folder.getName() + "' exists and was not overwritten!");
			return;
		}
		boolean ok = folder.mkdirs();
		if (! ok) {
			System.err.println("Could not create: " + folder.getAbsolutePath());
		}

		SysNatFileUtil.copyTextFileToTargetDir(TEMPLATE_TEST_FILE_DIR, 
                "Example.nlxx", folder.getAbsolutePath());
		folder = new File(folder, SysNatConstants.SCRIPT_DIR);
		ok = folder.mkdir();
		if (! ok) {
			System.err.println("Could not create: " + folder.getAbsolutePath());
		}
		
		String scriptName = "Who are you.nls";
		String content = SysNatFileUtil.readTextFileToString(new File(TEMPLATE_TEST_FILE_DIR, scriptName));
		content = content.replace("<AppName>", data.get(DataType.TestApplicationName).toLowerCase());
		SysNatFileUtil.writeFile(new File(folder.getAbsolutePath(), scriptName), content);
	}

	private static void adaptTestingConfig()
	{
		File settingsFile = buildTestingConfigFile();
		List<String> content = SysNatFileUtil.readTextFile(settingsFile);
		StringBuffer newContent = new StringBuffer();
		
		for (String line : content)
		{
			if (line.trim().startsWith("# Known Test Applications:") && ! line.contains(data.get(DataType.TestApplicationName)) ) {
				line += ", " + data.get(DataType.TestApplicationName);
			}
			
			if (line.trim().startsWith(SysNatConstants.TEST_APPLICATION_SETTING_KEY)) {
				line = SysNatConstants.TEST_APPLICATION_SETTING_KEY + " = " + data.get(DataType.TestApplicationName);
			}

			if (line.trim().startsWith(SysNatConstants.TEST_ENVIRONMENT_SETTING_KEY)) {
				line = SysNatConstants.TEST_ENVIRONMENT_SETTING_KEY + " = " + data.get(DataType.initialEnvironmentType).toUpperCase();
			}
			
			if (line.trim().startsWith(SysNatConstants.TEST_EXECUTION_FILTER_SETTING_KEY)) {
				String newCommentLine = "# " + data.get(DataType.TestApplicationName) + ": -";
				newContent.append(newCommentLine).append(System.getProperty("line.separator"));
				line = SysNatConstants.TEST_EXECUTION_FILTER_SETTING_KEY + " = -";
			}

			newContent.append(line).append(System.getProperty("line.separator"));
		}
		
		SysNatFileUtil.writeFile(settingsFile, newContent.toString());
		
	}

	private static void createLanguageTemplatesBasicsJavaFile()
	{
		File javaFile = buildLanguageTemplatesBasicsJavaFile(data.get(DataType.TestApplicationName));
		if ( javaFile.exists()) {			
			System.out.println("ATTENTION: File '" + javaFile.getName() + "' exists and was not overwritten!");
			return;
		}
		
		List<String> content = null;
		ApplicationType appType = ApplicationType.valueOf(data.get(DataType.APP_TYPE));
		switch (appType)
		{
			case Web:       content = createLanguageTemplatesBasicsContent_forWebApplication();	      break;
			case Swing:     content = createLanguageTemplatesBasicsContent_forSwingApplication();     break;
			case Composite: content = createLanguageTemplatesBasicsContent_forCompositeApplication(); break;
			default: 
				System.err.println("Unsupported application type: " + appType);
				System.exit(1);
			break;
		}
		
		SysNatFileUtil.writeFile(javaFile, content);
		
		if (appType == ApplicationType.Composite) {
			List<String> compositeAppNames = SysNatStringUtil.toList(data.get(DataType.Composites), ",").stream().
				     map(e -> SysNatStringUtil.firstCharToUpperCase(e.trim())).collect(Collectors.toList());
	
			new CompositeTestApplicationUpdater().doYourJob(data.get(DataType.TestApplicationName), compositeAppNames);
		}
	}
	

	private static List<String> createLanguageTemplatesBasicsContent_forSwingApplication() {
		return createLanguageTemplatesBasicsContent_forWebApplication();
	}
	
	private static List<String> createLanguageTemplatesBasicsContent_forWebApplication()
	{
		List<String> content = createLanguageTemplatesBasicsHeader();
		
		content.add("{");
		content.add("	private MainPageObject mainPageObject;");
		content.add("");
		content.add("	public LanguageTemplatesBasics_" + data.get(DataType.TestApplicationName) + "(ExecutableExample anXX) ");
		content.add("	{");
		content.add("		this.executableExample = anXX;");
		content.add("		this.executionInfo = ExecutionRuntimeInfo.getInstance();");
		content.add("		this.mainPageObject = createAndRegister(MainPageObject.class, executableExample);");
		
		if (isWithLogin()) {
			content.add("		this.setCurrentPage(loginPageObject);  // if not static use: setCurrentPage(findCurrentPage());");
		} else {
			content.add("		this.setCurrentPage(mainPageObject);  // if not static use: setCurrentPage(findCurrentPage());");
		}
		
		content.add("");
		content.add("		// define page change events");
		content.add("	}");
		content.add("");
		content.add("");
		content.add("	//##########################################################################################");
		content.add("	//                       I N T E R F A C E    M E T H O D S");
		content.add("	//##########################################################################################");
		content.add("");
		content.add("	@Override");
		content.add("	public void doLogin(Map<ApplicationLoginParameter,String> startParameter)");
		
		if (isWithLogin()) 
		{			
			content.add("	{");
			content.add("		loginPageObject.enterTextInTextField(\"" + data.get(DataType.GuiNameOfLoginUserField) + "\", startParameter.get(ApplicationLoginParameter.LoginId));");
			content.add("		loginPageObject.enterTextInTextField(\"" + data.get(DataType.GuiNameOfLoginPasswordField) + "\", startParameter.get(ApplicationLoginParameter.Password));");
			content.add("		loginPageObject.clickButton(\"" + data.get(DataType.TextOfLoginButton) + "\");");
			content.add("		setCurrentPage(mainPageObject);");
			content.add("	}");
		} else {
			content.add("	{ /* test application needs no login */ }");
		}
		
		content.add("");
		content.add("	@Override");
		content.add("    public void doLogout() {");
		content.add("        // executableExample.clickMenuHeader(\"Logout\");");
		content.add("        // findCurrentPage();");
		content.add("    }");
		content.add("");
		content.add("	@Override");
		content.add("	public boolean isLoginPageVisible() {");
		content.add("		return false;");
		content.add("	}");
		content.add("");
		content.add("	@Override");
		content.add("	public boolean isStartPageVisible() {");
		content.add("		return mainPageObject.isCurrentlyDisplayed();");
		content.add("	}");
		content.add("");
		content.add("	@Override");
		content.add("	public void gotoStartPage() {");
		content.add("		// Implement if needed");
		content.add("	}");
		content.add("	");
		content.add("	");		
		content.add("	//##########################################################################################");
		content.add("	//                   L A N G U A G E   T E M P L A T E    M E T H O D S");
		content.add("	//##########################################################################################");
		content.add("");
		
		if (isWithLogin()) 
		{			
			content.add("	@LanguageTemplate(value = \"Login with ^^, ^^.\")");
			content.add("	public void loginWith(String username, String password)  ");
			content.add("	{");
			content.add("		final HashMap<ApplicationLoginParameter, String> startParameter = new HashMap<>();");
			content.add("		startParameter.put(ApplicationLoginParameter.LoginId, username);");
			content.add("		startParameter.put(ApplicationLoginParameter.Password, password);");
			content.add("		doLogin(startParameter);");
			content.add("		executableExample.addReportMessage(\"Login performed for <b>\" + username + \"</b>.\");");
			content.add("	}");
			content.add("");
		}
		
		content.add("");
		content.add("	// ############################  Click instructions  ###################################");
		content.add("");
		
		content.add("	@LanguageTemplate(value = \"Click menu item ^^.\")");
		content.add("	public void clickMainMenuItem(final String mainMenuItem) {");
		content.add("		super.clickMainMenuItem(mainMenuItem);");
		content.add("	}");
		content.add("");
		content.add("	@LanguageTemplate(value = \"Click button ^^.\")");
		content.add("	public void clickButton(String buttonName) {");
		content.add("		List<PageObject> possiblePages = new ArrayList<>();");
		content.add("		// add possiblePages if known");
		content.add("		PageObject newPage = super.clickButtonToChangePage(buttonName, possiblePages , 0);");
		content.add("		setCurrentPage(newPage);");
		content.add("	}");
		content.add("");
		content.add("	@LanguageTemplate(value = \"In Dialog ^^ click button ^^.\")");
		content.add("	public void clickDialogButton(String dialogName, String buttonName) {");
		content.add("		super.clickDialogButton(dialogName, buttonName);");
		content.add("	}");
		content.add("");
		content.add("	@LanguageTemplate(value = \"Click link ^^.\")");
		content.add("	protected void clickLink(String linkText) {");
		content.add("		super.clickLink(linkText);");
		content.add("	}");		
		content.add("");
		content.add("	// #####################  Text and Combobox instructions  ###########################");
		content.add("");
		content.add("	@LanguageTemplate(value = \"Enter ^^ in textfield ^^.\")");
		content.add("	public void enterTextInField(String valueCandidate, String fieldName) {");
		content.add("		super.enterTextInField(valueCandidate, fieldName);");
		content.add("	}");
		content.add("");
		content.add("	@LanguageTemplate(value = \"Enter ^^ in textarea ^^.\")");
		content.add("	public void enterTextInTextarea(String valueCandidate, String fieldName) {");
		content.add("		super.enterTextInTextarea(valueCandidate, fieldName);");
		content.add("	}");
		content.add("");
		content.add("	@LanguageTemplate(value = \"Select ^^ in box ^^.\")");
		content.add("	public void chooseInCombobox(String valueCandidate, String fieldName) {");
		content.add("		super.chooseInCombobox(valueCandidate, fieldName);");
		content.add("	}");		
		content.add("");
		content.add("	// ##########################  checkbox and radiobutton instructions  ###################################");
		content.add("");
		content.add("	@LanguageTemplate(value = \"Click radiobutton ^^.\")");
		content.add("	public void clickRadioButton(String radioButtonName) {");
		content.add("		super.clickRadioButton(radioButtonName);");
		content.add("	}");		
		content.add("");
		content.add("	@LanguageTemplate(value = \"Tick checkbox ^^.\")");
		content.add("	public void ensureCheckboxIsTicked(String checkBoxDisplayName) {");
		content.add("		super.ensureCheckboxIsTicked(checkBoxDisplayName);");
		content.add("	}");		
		content.add("");
		content.add("	@LanguageTemplate(value = \"Tick checkbox ^^ if ^^=^^.\")");
		content.add("	public void tickCheckboxForCondition(String checkBoxDisplayName, String actual, String expected) {");
		content.add("		super.tickCheckboxForCondition(checkBoxDisplayName, actual, expected);");
		content.add("	}");		
		content.add("");
		content.add("	@LanguageTemplate(value = \"Untick checkbox ^^.\")");
		content.add("	public void ensureCheckboxIsUNticked(String checkBoxDisplayName) {");
		content.add("		super.ensureCheckboxIsUNticked(checkBoxDisplayName);");
		content.add("	}");		
		content.add("");
		content.add("	// ############################  M I S C  ###################################");
		content.add("");
		content.add("	@LanguageTemplate(value = \"Is the displayed text ^^ equal to ^^?\")");
		content.add("	public void isTextDislayed(final String guiElementToRead, final String valueCandidate) {");
		content.add("		super.isTextDislayed(guiElementToRead, valueCandidate);");
		content.add("	}");
		content.add("");
		content.add("}");		
		return content;
	}

	private static List<String> createLanguageTemplatesBasicsHeader()
	{
		List<String> content = new ArrayList<>();
		
		content.add("/*");
		content.add(" * Copyright 2018 IKS Gesellschaft fuer Informations- und Kommunikationssysteme mbH");
		content.add(" * ");
		content.add(" * Licensed under the Apache License, Version 2.0 (the \"License\");");
		content.add(" * you may not use this file except in compliance with the License.");
		content.add(" * You may obtain a copy of the License at");
		content.add(" * ");
		content.add(" *     http://www.apache.org/licenses/LICENSE-2.0");
		content.add(" * ");
		content.add(" * Unless required by applicable law or agreed to in writing, software");
		content.add(" * distributed under the License is distributed on an \"AS IS\" BASIS,");
		content.add(" * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.");
		content.add(" * See the License for the specific language governing permissions and");
		content.add(" * limitations under the License.");
		content.add(" */");
		content.add("package com.iksgmbh.sysnat.language_templates." + data.get(DataType.TestApplicationName).toLowerCase() + ";");
		content.add("");
		content.add("import java.util.*;");
		content.add("");
		content.add("import com.iksgmbh.sysnat.*;");
		content.add("import com.iksgmbh.sysnat.annotation.*;");
	    content.add("import com.iksgmbh.sysnat.common.exception.*;");
	    content.add("import com.iksgmbh.sysnat.common.utils.SysNatConstants.GuiType;");
		content.add("import com.iksgmbh.sysnat.guicontrol.GuiControl;");
		content.add("import com.iksgmbh.sysnat.guicontrol.impl.*;");
		content.add("import com.iksgmbh.sysnat.language_templates.LanguageTemplateBasics.PageChangeEvent.EventType;");
		content.add("import com.iksgmbh.sysnat.language_templates.*;");
		content.add("import com.iksgmbh.sysnat.language_templates." + data.get(DataType.TestApplicationName).toLowerCase() + ".*;");
		
		if (data.get(DataType.APP_TYPE).equals(ApplicationType.Composite.name())) {			
			List<String> compositeAppNames = SysNatStringUtil.toList(data.get(DataType.Composites), ",");
			compositeAppNames.forEach(e -> content.add("import com.iksgmbh.sysnat.language_templates." + e.toLowerCase() + ".*;"));
		} else {
			content.add("import com.iksgmbh.sysnat.language_templates." + data.get(DataType.TestApplicationName).toLowerCase() + ".pageobject.*;");			
		}
		
		content.add("import com.iksgmbh.sysnat.common.utils.SysNatConstants.ApplicationLoginParameter;");
		content.add("");
		content.add("/**");
		content.add(" * Implements all Natural Language instructions specific to " + data.get(DataType.TestApplicationName) + ".");
		content.add(" * Some common instructions are available from the parent class.");
		content.add(" */");
		content.add("@LanguageTemplateContainer");
		content.add("public class LanguageTemplatesBasics_" + data.get(DataType.TestApplicationName) + " extends LanguageTemplateBasics");
		return content;
	}

	private static List<String> createLanguageTemplatesBasicsContent_forCompositeApplication()
	{	   
		List<String> compositeAppNames = SysNatStringUtil.toList(data.get(DataType.Composites), ",").stream().
				     map(e -> SysNatStringUtil.firstCharToUpperCase(e.trim())).collect(Collectors.toList());
		
		List<String> content = createLanguageTemplatesBasicsHeader();
		
		content.add("{");
		content.add("    private static boolean firstInstance = true;");
		content.add("    enum APP {" + data.get(DataType.Composites) + "};");
		content.add("");
		String testApplicationName = SysNatStringUtil.firstCharToUpperCase(data.get(DataType.TestApplicationName));
		List<String> compositeApps = SysNatStringUtil.toList(data.get(DataType.Composites), ", ");
		compositeApps.forEach(e -> content.add("    private LanguageTemplatesBasics_" + e 
				                               + " languageTemplatesBasics_" + e + ";"));
		content.add("    private APP applicationInFocus;");
		content.add("");
		content.add("   public LanguageTemplatesBasics_" + testApplicationName + "(ExecutableExample anXX) ");
	    content.add("	{");
	    content.add("		this.executableExample = anXX;");
	    content.add("		this.executionInfo = ExecutionRuntimeInfo.getInstance();");
	    
		compositeAppNames.forEach(e -> content.add("        this.languageTemplatesBasics_" + e 
		                		 + " = new LanguageTemplatesBasics_" + e + "(executableExample);"));
		
	    content.add("		");
	    content.add("		if (firstInstance) {");
	    content.add("			firstInstance = false;");
	    content.add("			switchApp(ExecutionRuntimeInfo.getInstance().firstElementApplication());");
	    content.add("		} else {");
	    content.add("			findCurrentAppInFocus();");
	    content.add("		}");
	    content.add("	}");
	    content.add("");
	    content.add("	private void findCurrentAppInFocus()");
	    content.add("	{");
	    content.add("		HashMap<String,GuiControl> guiControllerMap = ExecutionRuntimeInfo.getInstance().getGuiControllerMap();");
	    content.add("		for (String key : guiControllerMap.keySet()) {");
	    content.add("			if (guiControllerMap.get(key) == executableExample.getActiveGuiController()) {");
	    content.add("				switchApp(key);");
	    content.add("				return;");
	    content.add("			}");
	    content.add("		}");
	    content.add("		");
	    content.add("		throw new SysNatException(\"Application in focus not found!\");");
	    content.add("	}");
	    content.add("");
	    content.add("	private APP toApp(String appName) ");
	    content.add("	{");
	    content.add("		APP[] values = APP.values();");
	    content.add("		for (APP app : values) {");
	    content.add("			if (app.name().equals(appName)) {");
	    content.add("				return app;");
	    content.add("			}");
	    content.add("		}");
	    content.add("		throw new RuntimeException(\"Unknown application: \" + appName);");
	    content.add("	}");
	    content.add("");
	    content.add("");
	    content.add("	//##########################################################################################");
	    content.add("	//                       I N T E R F A C E    M E T H O D S");
	    content.add("	//##########################################################################################");
	    content.add("	");
	    content.add("	@Override");
	    content.add("	public void doLogin(final Map<ApplicationLoginParameter,String> loginParameter) {");
	    content.add("		// no implementation needed here ");
	    content.add("	}");
	    content.add("");
	    content.add("	@Override");
	    content.add("    public void doLogout() {");
	    content.add("		// no implementation needed here ");
	    content.add("    }");
	    content.add("    ");
	    content.add("	@Override");
	    content.add("	public boolean isLoginPageVisible() {");
	    content.add("		return false;");
	    content.add("	}");
	    content.add("");
	    content.add("	@Override");
	    content.add("	public boolean isStartPageVisible() {");
	    content.add("		return false;");
	    content.add("	}");
	    content.add("");
	    content.add("	@Override");
	    content.add("	public void gotoStartPage() ");
	    content.add("	{");
	    content.add("		if (applicationInFocus == APP." + compositeAppNames.get(0) + ") {");
	    content.add("			languageTemplatesBasics_" + compositeAppNames.get(0) + ".gotoStartPage();");
	    content.add("		} else {");
	    content.add("			languageTemplatesBasics_" + compositeAppNames.get(1) + ".gotoStartPage();");
	    content.add("		}");
	    content.add("	}");
	    content.add("	");
	    content.add("	@LanguageTemplate(value = \"Wechsle zur Anwendung ^^.\")");
	    content.add("	@LanguageTemplate(value = \"Switch to application ^^.\")");
	    content.add("	public void switchApp(String appName)");
	    content.add("	{");
	    content.add("		applicationInFocus = toApp(appName);");
	    content.add("		boolean switched = executableExample.setActiveGuiControllerFor(appName);");
	    content.add("		if (switched) {");
	    content.add("			executableExample.getActiveGuiController().windowToFront();");
	    content.add("			executableExample.addReportMessage(\"The application <b>\" + appName + \"</b> is now focused.\");");
	    content.add("		}");
	    content.add("	}		");
	    content.add("	");
	    content.add("	//###########################################################################################");
	    content.add("	//!                   L A N G U A G E   T E M P L A T E    M E T H O D S                    !");
	    content.add("	//!   see code below this comment can be generated by class CompositeLanguageTemplateJoiner !");
	    content.add("	//###########################################################################################");
	    content.add("");
		content.add("}");		
		return content;		
	}
	
	private static void createDefaultPageObjectJavaFiles()
	{
		if (data.get(DataType.APP_TYPE).equals(ApplicationType.Composite.name())) return;
		File pageObjectFolder = buildPageObjectFolder();
		if (! pageObjectFolder.exists()) {
			pageObjectFolder.mkdir();
		} else {
			try {
				System.out.println("ATTENTION: Folder '" + pageObjectFolder.getCanonicalPath() + "' exists and was not overwritten!");
			} catch (Exception e) {
				System.out.println("ATTENTION: Folder '" + pageObjectFolder.getName() + "' exists and was not overwritten!");
			}
		}
		PageObjectDevGen.doYourJob(data.get(DataType.TestApplicationName), "Main", new PageChangeElement[0], null);
		
		if (isWithLogin()) 
		{
			PageChangeElement pageChangeElement = new PageChangeElementBuilder().from("Login").via(EventType.ButtonClick).on(data.get(DataType.TextOfLoginButton)).to("Main").build();
			PageChangeElement[] pageChangeElements = {pageChangeElement};
			PageObjectDevGen.doYourJob(data.get(DataType.TestApplicationName), "Login", pageChangeElements, PREDEFINED_ELEMENTS);
		}
	}

	public static File buildPageObjectFolder() {
		return new File(buildJavaFilesFolder(data.get(DataType.TestApplicationName)), "pageobject");
	}

	private static void createPropertyFile()
	{
		File propertiesFile = buildTestAppPropertiesFile(data.get(DataType.TestApplicationName));
		String properties = "# Common Application Properties "
				            + System.getProperty("line.separator");
		
		properties += "ApplicationType=" + data.get(DataType.APP_TYPE) + System.getProperty("line.separator")
		           + "withLogin=" + data.get(DataType.withLogin) + System.getProperty("line.separator")
				   + "runGuiInSeparateThread=true" + System.getProperty("line.separator")
		   		   + "maximizeApplicationWindow=true" + System.getProperty("line.separator");
		
		if (isWithLogin()) {
			properties += "LoginId=" + data.get(DataType.LoginUserId) 
					+ System.getProperty("line.separator")
					+ "Password=" + data.get(DataType.LoginPassword) 
					+ System.getProperty("line.separator");
		}
		properties += System.getProperty("line.separator")
		            + "# Environment specific properties"
                    + System.getProperty("line.separator");
		
		ApplicationType appType = ApplicationType.valueOf(data.get(DataType.APP_TYPE));
		switch (appType) 
		{
			case Web:
				properties += data.get(DataType.initialEnvironmentType) + "." + WebStartParameter.starturl.name().toLowerCase() 
						   + "=" + data.get(DataType.StartURL) + System.getProperty("line.separator");
			break;
			
			case Swing:
				properties += SwingStartParameter.JavaStartClass + "=" + data.get(DataType.JavaStartClass) 
						   + System.getProperty("line.separator");	
				properties += SwingStartParameter.LibDirs + "=" + data.get(DataType.LibDirs)  
						   + System.getProperty("line.separator");	
				properties += SwingStartParameter.ConfigFiles + "=" + data.get(DataType.ConfigFiles)  
						   + System.getProperty("line.separator");				
				properties += SwingStartParameter.MainFrameTitle + "=" + data.get(DataType.MainFrameTitle)  
						   + System.getProperty("line.separator");			
				
				properties += System.getProperty("line.separator")
		                   + "# Environment specific properties"
		                   + System.getProperty("line.separator")
                           + System.getProperty("line.separator");

				properties += data.get(DataType.initialEnvironmentType) + "." + SwingStartParameter.InstallDir.name() 
						   + "=" + data.get(DataType.InstallDir) + System.getProperty("line.separator");
				properties += data.get(DataType.initialEnvironmentType) + "." + SysNatConstants.ENV_DISPLAY_NAME 
				   + "=" + data.get(DataType.initialEnvironmentName) + System.getProperty("line.separator");
				break;

			case Composite:
				properties += SysNatConstants.APP_COMPOSITES 
				           + "=" + data.get(DataType.Composites) + System.getProperty("line.separator");
				
			break;
			
			default: 
				System.err.println("Unsupported application type: " + appType);
				System.exit(1);
			break;		
		}

		if (appType != ApplicationType.Composite) 
		{
			if (isWithLogin()) 
			{
				properties += data.get(DataType.initialEnvironmentType) + "." + ApplicationLoginParameter.LoginId.name()
				           + "=" + data.get(DataType.LoginUserId) + System.getProperty("line.separator");
				
				properties += data.get(DataType.initialEnvironmentType) + "." + ApplicationLoginParameter.Password.name() 
				           + "=" + data.get(DataType.LoginPassword) + System.getProperty("line.separator");
			}
			
			properties += System.getProperty("line.separator")
					   +  "# Note: Environment specific properties do overwrite common ones with the same key";
		}
		
		
		if ( ! propertiesFile.exists()) {			
			SysNatFileUtil.writeFile(propertiesFile, properties);
		} else {
			System.out.println("ATTENTION: File '" + propertiesFile.getName() + "' exists and was not overwritten!");
		}
	}

	private static boolean isWithLogin() {
		return data.get(DataType.withLogin) != null && data.get(DataType.withLogin).equalsIgnoreCase("true");
	}

	public static File buildTestAppPropertiesFile(String testApplicationName) {
		return new File(propertiesPath, testApplicationName + ".properties");
	}


	public static File buildNLFolder(String testApplicationName)
	{
		String path = naturalLanguageDir + "/ExecutableExamples";
		return new File(path, testApplicationName);
	}

	public static File buildTestDataFolder(String testApplicationName)
	{
		String path = naturalLanguageDir + "/testdata";
		return new File(path, testApplicationName);
	}	


	public static File buildLanguageTemplatesBasicsJavaFile(String testApplicationName)
	{
		File folder = buildJavaFilesFolder(testApplicationName);		
		return new File(folder, "LanguageTemplatesBasics_" + testApplicationName + ".java");
	}

	public static File buildJavaFilesFolder(String testApplicationName)
	{
		String path = sysnatTestRuntimeDir + "/java/com/iksgmbh/sysnat/language_templates/" + testApplicationName.toLowerCase();
		return new File(path);
	}

	public static File buildHelpFile(String testApplicationName) {
		return new File(CommandLibraryCreator.buildLibraryFilename(testApplicationName));
	}

	public static File buildTestingConfigFile() {
		return new File(naturalLanguageDir, SysNatConstants.TESTING_CONFIG_PROPERTY);
	}

	
}
