package com.iksgmbh.sysnat.helper.generator.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.ApplicationType;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.helper.generator.TestApplicationDevGen.DataType;

/**
 * Utility to test DevGenerators.
 * 
 * @author Reik Oberrath
 */
public class SysNatDevGenTestUtil
{
	public enum TestAppFiles { LanguageTemplatesBasicsJavaFile, NLFolder, TestDataFolder, HelpFile, PropertiesFile }

	public static File testDir = new File("../sysnat.natural.language.executable.examples/target/unittest");
	public static File testingConfig = new File(testDir, "testing.config");
	
	
	public static void assertFileContent(List<String> actualList, String expectedFilename)
	{
		File file = new File("../sysnat.natural.language.executable.examples/src/test/resources/GenerationResult/" + expectedFilename + "/");
		if (! file.exists()) try { file.createNewFile(); } catch (IOException e) { /* ignore */ }
		List<String> expectedList = SysNatFileUtil.readTextFile(file);
		String actual = SysNatStringUtil.listToString(actualList, System.getProperty("line.separator"));
		String expected = SysNatStringUtil.listToString(expectedList, System.getProperty("line.separator"));
		assertEquals("file content", expected, actual);
	}
	
	public static void checkExisting(HashMap<TestAppFiles, File> testAppFiles, boolean expectation) {
		Stream.of(TestAppFiles.values()).forEach(e -> assertEquals("Does " + testAppFiles.get(e) + " exists?", expectation, testAppFiles.get(e).exists()));
	}
	
	public static HashMap<DataType, String> createGenerationData_WEB(String testApplicationName, boolean withLogin)
	{
		HashMap<DataType,String> generationData = new HashMap<>();
		
		generationData.put(DataType.TestApplicationName, testApplicationName);
		generationData.put(DataType.APP_TYPE, ApplicationType.Web.name());
		generationData.put(DataType.initialEnvironmentType, SysNatConstants.TargetEnvironment.LOCAL.name());
		generationData.put(DataType.initialEnvironmentName, "test");
		generationData.put(DataType.withLogin, "" + withLogin);		
		generationData.put(DataType.StartURL, "http//somewhere.com");
		
		if (withLogin) 
		{ 			
			generationData.put(DataType.GuiNameOfLoginUserField, "User");  
			generationData.put(DataType.TechIdOfLoginUserField, "UserId");
			generationData.put(DataType.LoginUserId, "Tester");
			generationData.put(DataType.GuiNameOfLoginPasswordField, "Password");
			generationData.put(DataType.TechIdOfLoginPasswordField, "PwId");
			generationData.put(DataType.LoginPassword, "12345");
			generationData.put(DataType.TextOfLoginButton, "Login");
			generationData.put(DataType.TechIdOfLoginButton, "ButtonId");
		}
		
		return generationData;
	}

	public static HashMap<DataType, String> createGenerationData_Swing(String testApplicationName, boolean withLogin)
	{
		HashMap<DataType,String> generationData = new HashMap<>();
		
		generationData.put(DataType.TestApplicationName, testApplicationName);
		generationData.put(DataType.APP_TYPE, ApplicationType.Swing.name());
		generationData.put(DataType.initialEnvironmentType, SysNatConstants.TargetEnvironment.LOCAL.name());
		generationData.put(DataType.initialEnvironmentName, "test");
		generationData.put(DataType.withLogin, "" + withLogin);		
		generationData.put(DataType.LibDirs, "testlib");  
		generationData.put(DataType.InstallDir, "testdir"); 
		generationData.put(DataType.MainFrameTitle, "TestTitle"); 
		generationData.put(DataType.ConfigFiles, "testconfig"); 

		if (withLogin) 
		{ 			
			generationData.put(DataType.GuiNameOfLoginUserField, "User");  
			generationData.put(DataType.TechIdOfLoginUserField, "UserId");
			generationData.put(DataType.LoginUserId, "Tester");
			generationData.put(DataType.GuiNameOfLoginPasswordField, "Password");
			generationData.put(DataType.TechIdOfLoginPasswordField, "PwId");
			generationData.put(DataType.LoginPassword, "12345");
			generationData.put(DataType.TextOfLoginButton, "Login");
			generationData.put(DataType.TechIdOfLoginButton, "ButtonId");
		}
		
		return generationData;
	}

	public static HashMap<DataType, String> createGenerationData_Composite(String testApplicationName)
	{
		HashMap<DataType,String> generationData = new HashMap<>();
		
		generationData.put(DataType.TestApplicationName, testApplicationName);
		generationData.put(DataType.APP_TYPE, ApplicationType.Composite.name());
		generationData.put(DataType.initialEnvironmentType, SysNatConstants.TargetEnvironment.LOCAL.name());
		generationData.put(DataType.initialEnvironmentName, "test");
		generationData.put(DataType.Composites, "TestApp1, TestApp2");          
		
		return generationData;
	}
	
	public static void checkNotExisting(HashMap<TestAppFiles, File> testAppFiles) {
		List<TestAppFiles> result = Stream.of(TestAppFiles.values()).filter(e -> testAppFiles.get(e) != null)
		                                   .filter(e -> testAppFiles.get(e).exists())
		                                   .collect(Collectors.toList());
		if (! result.isEmpty()) {
			System.err.println("Followings files are not expected to exist:");
			result.stream().map(e -> testAppFiles.get(e).getAbsolutePath()).forEach(System.err::println);
		}
		assertTrue(result.isEmpty());
	}
	
}
