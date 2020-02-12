package de.iksgmbh.sysnat.docing.helper;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;

import de.iksgmbh.sysnat.docing.SysNatDocumentGenerator;
import de.iksgmbh.sysnat.docing.domain.TestApplicationDocData;
import de.iksgmbh.sysnat.docing.testutils.TestUtil;

public class MarkdownGeneratorClassLevelTest
{
	@Test
	public void generatesMarkdownFileContent()
	{
		// arrange
		System.setProperty(SysNatConstants.DOC_FORMAT_SETTING_KEY, "PDF");
		SystemPropertyLoader.doYourJob(SystemPropertyLoader.EXECUTUION_PROPERTIES);  
		final String testApplicationName = "TestApplication1"; // contains three nlxx files
		final File testFolder = new File(TestUtil.TEST_DOC_DATA_PATH, testApplicationName);
		final TestApplicationDocData docData = new AppDocFileParser().doYourJob(new File(testFolder, SysNatDocumentGenerator.APPLICATION_DOC_FILENAME));
		
		// act
		String result = MarkdownGenerator.generateFileContent(docData);
		
		// assert
		List<String> expected = SysNatFileUtil.readTextFile("../sysnat.docing/src/test/resources/testDocData/TestApplication1/"
				+ "expectedMarkdownContent.txt");
		List<String> actual = SysNatStringUtil.toListOfLines(result);
		assertEquals("Markdown File Content", TestUtil.removeDateLine(expected), TestUtil.removeDateLine(actual));		
	}

}
