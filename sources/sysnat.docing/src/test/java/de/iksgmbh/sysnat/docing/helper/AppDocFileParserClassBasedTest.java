package de.iksgmbh.sysnat.docing.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import de.iksgmbh.sysnat.docing.SysNatDocumentGenerator;
import de.iksgmbh.sysnat.docing.domain.TestApplicationDocData;
import de.iksgmbh.sysnat.docing.testutils.TestUtil;

public class AppDocFileParserClassBasedTest
{
	private AppDocFileParser cut = new AppDocFileParser();

	@Test
	public void handlesMissingApplicationFile()
	{
		// arrange
		final String testAppName = "TestApplication0";
		final File testFolder = new File(TestUtil.TEST_DOC_DATA_PATH, testAppName);
		final File appDocFile = new File(testFolder, SysNatDocumentGenerator.APPLICATION_DOC_FILENAME);	
		appDocFile.delete(); // cleanup
		assertFalse("File exists unexpectedly!", appDocFile.exists());
		
		// act
		TestApplicationDocData result = cut.doYourJob(appDocFile);
		boolean defaultFileCreated = appDocFile.exists();
		appDocFile.delete(); // cleanup
		
		// arrange
		assertTrue("Expected file has not been created!", defaultFileCreated);
		assertEquals("Test Application Name", testAppName, result.getTestApplicationName());
		assertEquals("Number of chapters", 1, result.getChapters().size());
	}

	@Test
	public void parsesApplicationFileWithoutChapters()
	{
		// arrange
		final String testAppName = "TestApplication1";
		final File testFolder = new File(TestUtil.TEST_DOC_DATA_PATH, testAppName);
		final File appDocFile = new File(testFolder, SysNatDocumentGenerator.APPLICATION_DOC_FILENAME);	
		
		// act
		TestApplicationDocData result = cut.doYourJob(appDocFile);

		// arrange
		assertEquals("Test Application Name", testAppName, result.getTestApplicationName());
		assertEquals("Number of chapters", 0, result.getChapters().size());
	}

	@Test
	public void parsesApplicationFileWithTwoChapters()
	{
		// arrange
		final String testAppName = "TestApplication2";
		final File testFolder = new File(TestUtil.TEST_DOC_DATA_PATH, testAppName);
		final File appDocFile = new File(testFolder, SysNatDocumentGenerator.APPLICATION_DOC_FILENAME);	
		
		// act
		TestApplicationDocData result = cut.doYourJob(appDocFile);

		// arrange
		assertEquals("Test Application Name", testAppName, result.getTestApplicationName());
		assertEquals("Number of chapters", 2, result.getChapters().size());
		assertEquals("Number of behaviours", 0, result.getOrderedBehaviours().size());
	}

	@Test
	public void parsesApplicationFileWithBehaviourList()
	{
		// arrange
		final String testAppName = "TestApplication3";
		final File testFolder = new File(TestUtil.TEST_DOC_DATA_PATH, testAppName);
		final File appDocFile = new File(testFolder, SysNatDocumentGenerator.APPLICATION_DOC_FILENAME);	
		
		// act
		TestApplicationDocData result = cut.doYourJob(appDocFile);

		// arrange
		assertEquals("Test Application Name", testAppName, result.getTestApplicationName());
		assertEquals("Number of chapters", 3, result.getChapters().size());
		assertEquals("Number of behaviours", 2, result.getOrderedBehaviours().size());
	}
	
}
