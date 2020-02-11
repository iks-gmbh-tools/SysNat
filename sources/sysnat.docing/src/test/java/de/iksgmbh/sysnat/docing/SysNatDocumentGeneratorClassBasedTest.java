package de.iksgmbh.sysnat.docing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

import de.iksgmbh.sysnat.docing.domain.TestApplicationDocData;
import de.iksgmbh.sysnat.docing.helper.SystemPropertyLoader;
import de.iksgmbh.sysnat.docing.testutils.TestUtil;

public class SysNatDocumentGeneratorClassBasedTest
{
	private SysNatDocumentGenerator cut = new SysNatDocumentGenerator(); 

	@Before
	public void setup() {
		System.setProperty(SysNatConstants.RESULT_LAUNCH_OPTION_SETTING_KEY, "None");
	}
	
	@Test
	public void parsesDocDataAndUsesOrdersBehaviourChapters()
	{
		// arrange
		System.setProperty(SysNatConstants.DOC_DEPTH_SETTING_KEY, "Maximum");
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
	public void creates_PDF_SystemDocumentation()
	{
		// arrange
		System.setProperty(SysNatConstants.DOC_DEPTH_SETTING_KEY, "Maximum");
		final String testApp = "TestApplication1";
		final String documentFormat = "PDF";

		// act 
		File resultFile = createsSystemDocumentation(documentFormat, testApp);
		
		// assert
		assertTrue("Expected file does not exist", resultFile.exists());

		// TODO pdf vergleich - falls es Unterschiede zu html geben wird!
	}

	@Test
	public void creates_Word_SystemDocumentation()
	{
		// arrange
		final String testApp = "TestApplication2";
		final String documentFormat = "DOCX";

		// act 
		File resultFile = createsSystemDocumentation(documentFormat, testApp);
		
		// assert
		assertTrue("Expected file does not exist", resultFile.exists());
	}

	@Test
	public void creates_HTML_Maximum_SystemDocumentation()
	{
		// arrange
		System.setProperty(SysNatConstants.DOC_DEPTH_SETTING_KEY, "Maximum");
		final String testApp = "TestApplication3";
		final String documentFormat = "HTML";

		// act 
		File resultFile = createsSystemDocumentation(documentFormat, testApp);
		
		// assert
		List<String> expected = SysNatFileUtil.readTextFile("../sysnat.docing/src/test/resources/testDocData/TestApplication3/"
				+ "expectedResult.max.html.txt");
		List<String> actual = SysNatFileUtil.readTextFile(resultFile);
		assertEquals("File Content", TestUtil.removeDateLine(expected), TestUtil.removeDateLine(actual));
	}

	
	@Test
	public void creates_HTML_Medium_SystemDocumentation()
	{
		// arrange
		System.setProperty(SysNatConstants.DOC_DEPTH_SETTING_KEY, "Medium");
		final String testApp = "TestApplication3";
		final String documentFormat = "HTML";

		// act 
		File resultFile = createsSystemDocumentation(documentFormat, testApp);
		
		// assert
		List<String> expected = SysNatFileUtil.readTextFile("../sysnat.docing/src/test/resources/testDocData/TestApplication3/"
				+ "expectedResult.med.html.txt");
		List<String> actual = SysNatFileUtil.readTextFile(resultFile);
		assertEquals("File Content", TestUtil.removeDateLine(expected), TestUtil.removeDateLine(actual));
	}
	
	@Test
	public void creates_HTML_Minimum_SystemDocumentation()
	{
		// arrange
		System.setProperty(SysNatConstants.DOC_DEPTH_SETTING_KEY, "Minimum");
		final String testApp = "TestApplication3";
		final String documentFormat = "HTML";

		// act 
		File resultFile = createsSystemDocumentation(documentFormat, testApp);
		
		// assert
		List<String> expected = SysNatFileUtil.readTextFile("../sysnat.docing/src/test/resources/testDocData/TestApplication3/"
				+ "expectedResult.min.html.txt");
		List<String> actual = SysNatFileUtil.readTextFile(resultFile);
		assertEquals("File Content", TestUtil.removeDateLine(expected), TestUtil.removeDateLine(actual));
	}

	private File createsSystemDocumentation(final String documentFormat, final String testApp)
	{
		// arrange
		final String documentName = "ExampleResultDocument";
		SystemPropertyLoader.doYourJob(SystemPropertyLoader.EXECUTUION_PROPERTIES);  
		System.setProperty(SysNatConstants.DOC_FORMAT_SETTING_KEY, documentFormat);
		System.setProperty(SysNatConstants.DOC_NAME_SETTING_KEY, documentName);
		final File docSourceFolder = new File(TestUtil.TEST_DOC_DATA_PATH, testApp);
		final File resultFile = new File(System.getProperty("sysnat.docing.targetDir.dir"), 
				                           documentName + "." + documentFormat);
		resultFile.delete();
		assertFalse("File must not yet exist: " + resultFile.getAbsolutePath(), resultFile.exists());
		
		// act 
		cut.generate(docSourceFolder);
		
		// assert
		assertTrue("Expected file does not exist", resultFile.exists());
		return resultFile;
	}


}
