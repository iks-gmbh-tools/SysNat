package de.iksgmbh.sysnat.docing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import de.iksgmbh.sysnat.docing.domain.TestApplicationDocData;
import de.iksgmbh.sysnat.docing.testimpl.SysNatDocumentGeneratorTestImpl;
import de.iksgmbh.sysnat.docing.testutils.TestUtil;

public class SysNatDocumentGeneratorClassBasedTest
{
	private SysNatDocumentGenerator cut = new SysNatDocumentGeneratorTestImpl(); 

	@Test
	public void parsesDocDataAndUsesOrdersBehaviourChapters()
	{
		// arrange
		String testAppName = "TestApplication3";
		File docSourceFolder = new File(TestUtil.TEST_DOC_DATA_PATH, testAppName);
		
		// act 
		TestApplicationDocData result = cut.parseDocData(docSourceFolder);
		
		// assert
		assertEquals("Test Application Name", testAppName, result.getTestApplicationName());
		assertEquals("Number of chapters", 5, result.getChapters().size());
		assertEquals("Name of first behaviour chapter", 
				     result.getOrderedBehaviours().get(0), 
				     result.getChapterByIndex(3).getKey());
	}
	
	
	@Test
	public void createsHtmlFile()
	{
		// arrange
		final File docSourceFolder = new File(TestUtil.TEST_DOC_DATA_PATH, "TestApplication3");
		final File expectedFile = new File("../sysnat.docing/target/test.html");
		expectedFile.delete();
		assertFalse("File must not yet exist: " + expectedFile.getAbsolutePath(), expectedFile.exists());
		
		// act 
		cut.doYourJob(docSourceFolder);
		
		// assert
		assertTrue("Expected file does not exist", expectedFile.exists());
	}

}
