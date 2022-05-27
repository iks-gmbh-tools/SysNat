package com.iksgmbh.sysnat.helper.generator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.helper.generator.TestApplicationDevGen.DataType;
import com.iksgmbh.sysnat.helper.generator.utils.SysNatDevGenTestUtil;
import com.iksgmbh.sysnat.helper.generator.utils.SysNatDevGenTestUtil.TestAppFiles;

public class TestApplicationRemoverClassLevelTest
{
	private static File testDir = SysNatDevGenTestUtil.testDir;
	
	@BeforeClass
	public static void setup() {
		TestApplicationDevGenClassLevelTest.setup();
	}
	
	@Before
	public void init() {
		testDir.delete();
	}

	@Test
	public void removeTestWebApp() throws IOException
	{
		// arrange
		String testApplicationName = "FakeWebApp";
		SysNatFileUtil.writeFile(SysNatDevGenTestUtil.testingConfig, "TestApplication = " + testApplicationName);
		HashMap<TestAppFiles, File> testAppFiles = TestApplicationDevGenClassLevelTest.getTestAppFiles(testApplicationName);
		HashMap<DataType, String> generationData = SysNatDevGenTestUtil.createGenerationData_WEB(testApplicationName, false);
		TestApplicationDevGen.doYourJob(generationData);
		SysNatDevGenTestUtil.checkExisting(testAppFiles, true);
		
		// act
		TestApplicationRemover.doYourJob(testApplicationName);
		
		// assert
		SysNatDevGenTestUtil.checkExisting(testAppFiles, false);
	}
	
}
