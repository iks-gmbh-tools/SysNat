package com.iksgmbh.sysnat.test.module_level;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.iksgmbh.sysnat.common.helper.FileFinder;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.ApplicationType;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.helper.generator.NaturalLanguageMethodDevGen;
import com.iksgmbh.sysnat.helper.generator.PageObjectDevGen;
import com.iksgmbh.sysnat.helper.generator.PageObjectDevGen.PageChangeElement;
import com.iksgmbh.sysnat.helper.generator.PageObjectDevGen.PageChangeElementBuilder;
import com.iksgmbh.sysnat.helper.generator.TestApplicationDevGen;
import com.iksgmbh.sysnat.helper.generator.TestApplicationDevGen.DataType;
import com.iksgmbh.sysnat.helper.generator.TestApplicationRemover;
import com.iksgmbh.sysnat.language_templates.LanguageTemplateBasics.PageChangeEvent.EventType;

/**
 * Tests code generation for Developer purposes.
 * 
 * @author Reik Oberrath
 */
public class SysNatDeveloperGenerator_ModuleLevelTest
{
	public enum TestAppFiles { LanguageTemplatesBasicsJavaFile, NLFolder, TestDataFolder, HelpFile, PropertiesFile }

	@BeforeClass
	public static void setup() {
		System.setProperty("sysnat.help.command.list.file", "../sysnat.natural.language.executable.examples/help/ExistingNLInstructions_<testapp>.html");
	}
	
	@Test
	public void generatesCodeForDeveloper() throws Exception
	{
		// arrange
		String testApplicationName = "ModuleTest_TestApplication";
		String pageObjectName = "AnotherDataMask";
		HashMap<TestAppFiles, File> testAppFiles = getTestAppFiles(testApplicationName);
		HashMap<DataType,String> testAppGenerationData = createTestAppGenerationData(testApplicationName);
		LinkedHashMap<com.iksgmbh.sysnat.helper.generator.NaturalLanguageMethodDevGen.DataType, String> nlGenerationData = buildNlGenerationData(testApplicationName);
		PageChangeElement[] pageChangeElements = {
				new PageChangeElementBuilder().via(EventType.TabSwitch).on("AnotherTab").to(pageObjectName).waiting("1000").build(),
				new PageChangeElementBuilder().via(EventType.MenuItemClick).on("AnotherMenu").to(pageObjectName).build()
		};
		checkNotExisting(testAppFiles);
		
		
		// act
		TestApplicationDevGen.doYourJob(testAppGenerationData);
		PageObjectDevGen.doYourJob(testApplicationName, pageObjectName, pageChangeElements, null);
		NaturalLanguageMethodDevGen.doYourJob(nlGenerationData);

		// store test data
		List<String> actualJavaLines = SysNatFileUtil.readTextFile(testAppFiles.get(TestAppFiles.LanguageTemplatesBasicsJavaFile));
		List<String> actualPropertiesLines = SysNatFileUtil.readTextFile(testAppFiles.get(TestAppFiles.PropertiesFile));
		List<String> actualHelpLines = SysNatFileUtil.readTextFile(testAppFiles.get(TestAppFiles.HelpFile));
		List<File> javaFiles = FileFinder.searchFilesRecursively(testAppFiles.get(TestAppFiles.LanguageTemplatesBasicsJavaFile).getParentFile(), ".java");
		List<File> nlFiles = FileFinder.searchFilesRecursively(testAppFiles.get(TestAppFiles.NLFolder), ".nlxx", ".nls");
		List<File> testDataFiles = FileFinder.searchFilesRecursively(testAppFiles.get(TestAppFiles.TestDataFolder), ".dat");
		File languageTemplatesBasicsJavaFile = testAppFiles.get(TestAppFiles.LanguageTemplatesBasicsJavaFile);
		File pageObjectFolder = new File(languageTemplatesBasicsJavaFile.getParentFile(), "pageobject");
		List<String> actualMainPageObjectLines = SysNatFileUtil.readTextFile(new File(pageObjectFolder, "MainPageObject.java"));
		List<String> actualLoginPageObjectLines = SysNatFileUtil.readTextFile(new File(pageObjectFolder, "LoginPageObject.java"));
		
		// cleanup
		TestApplicationRemover.doYourJob(testApplicationName);
		checkNotExisting(testAppFiles);
		
		// arrange
		assertFileContent(actualPropertiesLines, "PropertiesFile.txt");
		assertFileContent(actualJavaLines, "LanguageTemplateBasicFile.txt");
		assertFileContent(actualMainPageObjectLines, "MainPageObjectFile.txt");
		assertFileContent(actualLoginPageObjectLines, "LoginPageObjectFile.txt");
		assertFileContent(actualHelpLines, "HelpFile.txt");
		assertEquals("Number of Java Files", 4, javaFiles.size());
		assertEquals("Number of Natural Language Files", 2, nlFiles.size());
		assertEquals("Number of Test Data Files", 1, testDataFiles.size());
	}
	
	
	// ####################################################################################################################
	
	
	private HashMap<TestAppFiles, File> getTestAppFiles(String testApplicationName) 
	{
		HashMap<TestAppFiles, File> toReturn = new HashMap<>();

		toReturn.put(TestAppFiles.LanguageTemplatesBasicsJavaFile, TestApplicationDevGen.buildLanguageTemplatesBasicsJavaFile(testApplicationName));
		toReturn.put(TestAppFiles.NLFolder, TestApplicationDevGen.buildNLFolder(testApplicationName));
		toReturn.put(TestAppFiles.TestDataFolder, TestApplicationDevGen.buildTestDataFolder(testApplicationName));
		toReturn.put(TestAppFiles.HelpFile, TestApplicationDevGen.buildHelpFile(testApplicationName));
		toReturn.put(TestAppFiles.PropertiesFile, TestApplicationDevGen.buildTestAppPropertiesFile(testApplicationName));
		
		return toReturn;
	}
	
	
	private LinkedHashMap<com.iksgmbh.sysnat.helper.generator.NaturalLanguageMethodDevGen.DataType, String> buildNlGenerationData(String testApplicationName)
	{
		LinkedHashMap<NaturalLanguageMethodDevGen.DataType, String> data = new LinkedHashMap<>();
		
		data.put(com.iksgmbh.sysnat.helper.generator.NaturalLanguageMethodDevGen.DataType.NLExpression, "Do something with 'this' and \"that\" and return <toReturn>.");
		data.put(com.iksgmbh.sysnat.helper.generator.NaturalLanguageMethodDevGen.DataType.MethodeName, "doSomethingWith");
		data.put(com.iksgmbh.sysnat.helper.generator.NaturalLanguageMethodDevGen.DataType.ReportMessage, "Something done with <arg1> and <arg2> and return ? .");
		data.put(com.iksgmbh.sysnat.helper.generator.NaturalLanguageMethodDevGen.DataType.TestApplication, testApplicationName);
		data.put(com.iksgmbh.sysnat.helper.generator.NaturalLanguageMethodDevGen.DataType.MethodParameter, "aValue=this" + NaturalLanguageMethodDevGen.PARAMETER_SEPARATOR + "anObject:that");
		
		return data;
	}
	
	private HashMap<DataType, String> createTestAppGenerationData(String testApplicationName)
	{
		HashMap<DataType,String> generationData = new HashMap<>();
		
		generationData.put(DataType.TestApplicationName, testApplicationName);
		generationData.put(DataType.APP_TYPE, ApplicationType.Web.name());
		generationData.put(DataType.initialEnvironmentType, SysNatConstants.TargetEnvironment.LOCAL.name());
		generationData.put(DataType.initialEnvironmentName, "test");
		generationData.put(DataType.withLogin, "true");		
		generationData.put(DataType.StartURL, "http//somewhere.com");
		generationData.put(DataType.GuiNameOfLoginUserField, "User");  
		generationData.put(DataType.TechIdOfLoginUserField, "UserId");
		generationData.put(DataType.LoginUserId, "Tester");
		generationData.put(DataType.GuiNameOfLoginPasswordField, "Password");
		generationData.put(DataType.TechIdOfLoginPasswordField, "PwId");
		generationData.put(DataType.LoginPassword, "12345");
		generationData.put(DataType.TextOfLoginButton, "Login");
		generationData.put(DataType.TechIdOfLoginButton, "ButtonId");
		
		return generationData;
	}

	private void checkNotExisting(HashMap<TestAppFiles, File> testAppFiles) {
		List<TestAppFiles> result = Stream.of(TestAppFiles.values()).filter(e -> testAppFiles.get(e) != null)
		                                   .filter(e -> testAppFiles.get(e).exists())
		                                   .collect(Collectors.toList());
		if (! result.isEmpty()) {
			System.err.println("Followings files are not expected to exist:");
			result.stream().map(e -> testAppFiles.get(e).getAbsolutePath()).forEach(System.err::println);
		}
		assertTrue(result.isEmpty());
	}
	
	private void assertFileContent(List<String> actualList, String expectedFilename)
	{
		File file = new File("./src/test/resources/DevGenExpectedTestResult/" + expectedFilename);
		if (! file.exists()) try { file.createNewFile(); } catch (IOException e) { /* ignore */ }
		List<String> expectedList = SysNatFileUtil.readTextFile(file);
		String actual = SysNatStringUtil.listToString(actualList, System.getProperty("line.separator"));
		String expected = SysNatStringUtil.listToString(expectedList, System.getProperty("line.separator"));
		assertEquals("file content", expected, actual);
	}	
}
