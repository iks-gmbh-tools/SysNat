package com.iksgmbh.sysnat.helper.generator;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.iksgmbh.sysnat.common.helper.FileFinder;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.helper.generator.TestApplicationDevGen.DataType;
import com.iksgmbh.sysnat.helper.generator.utils.SysNatDevGenTestUtil;
import com.iksgmbh.sysnat.helper.generator.utils.SysNatDevGenTestUtil.TestAppFiles;

public class TestApplicationDevGenClassLevelTest
{
	private static File testDir = SysNatDevGenTestUtil.testDir;

	@BeforeClass
	public static void setup() 
	{
		SysNatFileUtil.deleteFolder(testDir);
		testDir.mkdirs();
		new File(testDir, "help").mkdir();
		System.setProperty("sysnat.help.command.list.file", testDir.getAbsolutePath() + "/help/ExistingNLInstructions_<testapp>.html");
		TestApplicationDevGen.naturalLanguageDir = testDir.getAbsolutePath();
		TestApplicationDevGen.sysnatTestRuntimeDir = testDir.getAbsolutePath();
		TestApplicationDevGen.propertiesPath = testDir.getAbsolutePath();
		CompositeTestApplicationUpdater.languageTemplatesContainerParentDir = testDir.getAbsolutePath() + "/java/com/iksgmbh/sysnat/language_templates/";
		PageObjectDevGen.languageTemplatesContainerParentDir = testDir.getAbsolutePath() + "/java/com/iksgmbh/sysnat/language_templates/";
		File nlxx = new File(testDir, "/resources/TestApplicationDevGen/Example.nlxx");
		nlxx.getParentFile().mkdirs();
		SysNatFileUtil.writeFile(nlxx, "XX:  <filename>");
		SysNatFileUtil.writeFile(new File(nlxx.getParentFile(), "Who are you.nls"), "# Example Script to demonstrate the use of Natural Language Scripts");
		SysNatFileUtil.writeFile(new File(nlxx.getParentFile(), "ExampleTestData.dat"), "");
	}

	@Before
	public void init() {
		testDir.delete();
	}

	@Test
	public void generatesTestWebAppWithoutLogin()
	{
		// arrange
		String testApplicationName = "FakeWebApp";
		SysNatFileUtil.writeFile(SysNatDevGenTestUtil.testingConfig, "TestApplication = " + testApplicationName);
		HashMap<TestAppFiles, File> testAppFiles = getTestAppFiles(testApplicationName);
		SysNatDevGenTestUtil.checkNotExisting(testAppFiles);
		HashMap<DataType, String> generationData = SysNatDevGenTestUtil.createGenerationData_WEB(testApplicationName, false);
		
		// act
		TestApplicationDevGen.doYourJob(generationData);
		
		// store test data
		List<String> actualJavaLines = SysNatFileUtil.readTextFile(testAppFiles.get(TestAppFiles.LanguageTemplatesBasicsJavaFile));
		List<String> actualPropertiesLines = SysNatFileUtil.readTextFile(testAppFiles.get(TestAppFiles.PropertiesFile));
		List<String> actualHelpLines = SysNatFileUtil.readTextFile(testAppFiles.get(TestAppFiles.HelpFile));
		List<File> javaFiles = FileFinder.searchFilesRecursively(testAppFiles.get(TestAppFiles.LanguageTemplatesBasicsJavaFile).getParentFile(), ".java");
		List<File> nlFiles = FileFinder.searchFilesRecursively(testAppFiles.get(TestAppFiles.NLFolder), ".nlxx", ".nls");
		List<File> testDataFiles = FileFinder.searchFilesRecursively(testAppFiles.get(TestAppFiles.TestDataFolder), ".dat");
		
		// assert
		SysNatDevGenTestUtil.assertFileContent(actualJavaLines, "TestWebAppWithoutLogin/LanguageTemplateBasicFile.txt");
		SysNatDevGenTestUtil.assertFileContent(actualPropertiesLines, "TestWebAppWithoutLogin/PropertiesFile.txt");
		SysNatDevGenTestUtil.assertFileContent(actualHelpLines, "TestWebAppWithoutLogin/HelpFile.txt");
		assertEquals("Number of Java Files", 2, javaFiles.size());
		assertEquals("Number of Natural Language Files", 2, nlFiles.size());
		assertEquals("Number of Test Data Files", 1, testDataFiles.size());
	}

	@Test
	public void generatesTestSwingAppWithLogin()
	{
		// arrange
		String testApplicationName = "FakeSwingApp";
		SysNatFileUtil.writeFile(SysNatDevGenTestUtil.testingConfig, "TestApplication = " + testApplicationName);
		HashMap<TestAppFiles, File> testAppFiles = getTestAppFiles(testApplicationName);
		SysNatDevGenTestUtil.checkNotExisting(testAppFiles);
		HashMap<DataType, String> generationData = SysNatDevGenTestUtil.createGenerationData_Swing(testApplicationName, true);
		
		// act
		TestApplicationDevGen.doYourJob(generationData);
		
		// store test data
		List<String> actualPropertiesLines = SysNatFileUtil.readTextFile(testAppFiles.get(TestAppFiles.PropertiesFile));
		File languageTemplatesBasicsJavaFile = testAppFiles.get(TestAppFiles.LanguageTemplatesBasicsJavaFile);
		List<String> actualJavaLines = SysNatFileUtil.readTextFile(languageTemplatesBasicsJavaFile);
		File pageObjectFolder = new File(languageTemplatesBasicsJavaFile.getParentFile(), "pageobject");
		List<String> actualMainPageObjectLines = SysNatFileUtil.readTextFile(new File(pageObjectFolder, "MainPageObject.java"));
		List<String> actualLoginPageObjectLines = SysNatFileUtil.readTextFile(new File(pageObjectFolder, "LoginPageObject.java"));

		// assert
		SysNatDevGenTestUtil.assertFileContent(actualJavaLines, "TestSwingAppWithLogin/LanguageTemplateBasicFile.txt");
		SysNatDevGenTestUtil.assertFileContent(actualPropertiesLines, "TestSwingAppWithLogin/PropertiesFile.txt");
		SysNatDevGenTestUtil.assertFileContent(actualMainPageObjectLines, "TestSwingAppWithLogin/MainPageObjectFile.txt");
		SysNatDevGenTestUtil.assertFileContent(actualLoginPageObjectLines, "TestSwingAppWithLogin/LoginPageObjectFile.txt");
	}
	
	@Test
	public void generatesTestCompositeApp()
	{
		// arrange
		String testApplicationName = "FakeCompositeApp";
		SysNatFileUtil.writeFile(SysNatDevGenTestUtil.testingConfig, "TestApplication = " + testApplicationName);
		HashMap<TestAppFiles, File> testAppFiles = getTestAppFiles(testApplicationName);
		SysNatDevGenTestUtil.checkNotExisting(testAppFiles);
		HashMap<DataType, String> generationData1 = SysNatDevGenTestUtil.createGenerationData_Swing("TestApp1", false);
		TestApplicationDevGen.doYourJob(generationData1);
		HashMap<DataType, String> generationData2 = SysNatDevGenTestUtil.createGenerationData_WEB("TestApp2", true);
		TestApplicationDevGen.doYourJob(generationData2);
		HashMap<DataType, String> generationData3 = SysNatDevGenTestUtil.createGenerationData_Composite(testApplicationName);
		
		// act
		TestApplicationDevGen.doYourJob(generationData3);
		
		// store test data
		List<String> actualPropertiesLines = SysNatFileUtil.readTextFile(testAppFiles.get(TestAppFiles.PropertiesFile));
		File languageTemplatesBasicsJavaFile = testAppFiles.get(TestAppFiles.LanguageTemplatesBasicsJavaFile);
		List<String> actualJavaLines = SysNatFileUtil.readTextFile(languageTemplatesBasicsJavaFile);
		List<File> javaFiles = FileFinder.searchFilesRecursively(testAppFiles.get(TestAppFiles.LanguageTemplatesBasicsJavaFile).getParentFile(), ".java");

		// assert
		SysNatDevGenTestUtil.assertFileContent(actualJavaLines, "TestCompositeApp/LanguageTemplateBasicFile.txt");
		SysNatDevGenTestUtil.assertFileContent(actualPropertiesLines, "TestCompositeApp/PropertiesFile.txt");
		assertEquals("Number of Java Files", 1, javaFiles.size());  // no page objects !
	}
	
	// ##################################################################################################################################

	public static HashMap<TestAppFiles, File> getTestAppFiles(String testApplicationName) 
	{
		HashMap<TestAppFiles, File> toReturn = new HashMap<>();

		toReturn.put(TestAppFiles.LanguageTemplatesBasicsJavaFile, 
				     new File(testDir, "java/com/iksgmbh/sysnat/language_templates/" + testApplicationName.toLowerCase() 
				                     + "/LanguageTemplatesBasics_" + testApplicationName + ".java"));
		toReturn.put(TestAppFiles.NLFolder, new File(testDir, "ExecutableExamples/" + testApplicationName));
		toReturn.put(TestAppFiles.TestDataFolder, new File(testDir, "testdata/" + testApplicationName));
		toReturn.put(TestAppFiles.HelpFile, new File(testDir, "help/ExistingNLInstructions_" + testApplicationName + ".html"));
		toReturn.put(TestAppFiles.PropertiesFile, new File(testDir, testApplicationName + ".properties"));
		
		return toReturn;
	}
	
}
