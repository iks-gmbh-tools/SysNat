package de.iksgmbh.sysnat.docing.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.common.utils.SysNatConstants;

import de.iksgmbh.sysnat.docing.SysNatDocumentGenerator;
import de.iksgmbh.sysnat.docing.domain.TestApplicationDocData;
import de.iksgmbh.sysnat.docing.domain.XXDocData;
import de.iksgmbh.sysnat.docing.domain.XXGroupDocData;
import de.iksgmbh.sysnat.docing.testutils.TestUtil;

public class NLXXParserClassLevelTest
{
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/Docing", Locale.getDefault());

	private NLXXParser cut = new NLXXParser();
	
	@Before
	public void init() {
		System.setProperty(SysNatConstants.DOC_DEPTH_SETTING_KEY, "Minimum");
	}

	@Test
	public void parsesWholeNLXXFiles()
	{
		// arrange
		final String testApplicationName = "TestApplication1"; // contains three nlxx files
		final File testFolder = new File(TestUtil.TEST_DOC_DATA_PATH, testApplicationName);
		final TestApplicationDocData docData = new AppDocFileParser().doYourJob(new File(testFolder, SysNatDocumentGenerator.APPLICATION_DOC_FILENAME));
		
		// act
		cut.doYourJob(docData);
		
		// assert
		assertEquals("Test Application Name", testApplicationName, docData.getTestApplicationName());
		assertEquals("Number of chapters", 4, docData.getChapters().size());
		List<String> chapterNames = new ArrayList<>(docData.getChapters().keySet());
		assertEquals("Name of chapter 1", "Behaviour1", chapterNames.get(1));
		assertEquals("Name of chapter 2", "Behaviour2", chapterNames.get(2));
		assertEquals("Name of chapter 3", "Behaviour3", chapterNames.get(3));
	}
	
	@Test
	public void parsesSysDocingAsChapterText()
	{
		// arrange
		final String testApplicationName = "TestApplication1"; // contains three nlxx files
		final File testFolder = new File(TestUtil.TEST_DOC_DATA_PATH, testApplicationName);
		final TestApplicationDocData docData = new AppDocFileParser().doYourJob(new File(testFolder, SysNatDocumentGenerator.APPLICATION_DOC_FILENAME));
		
		// act
		cut.doYourJob(docData);
		
		// assert
		List<String> chapterNames = new ArrayList<>(docData.getChapters().keySet());
		assertEquals("Chapter Number", 4, chapterNames.size());
		String chapterContent = docData.getChapterContent(chapterNames.get(0)).trim();
		assertTrue("Unexpected chapter content", chapterContent.startsWith("# " + BUNDLE.getString("DEFAULT_CHAPTER_NAME")));
		chapterContent = docData.getChapterContent(chapterNames.get(1)).trim();
		assertTrue("Unexpected chapter content", chapterContent.startsWith("# Behaviour1"));
		assertTrue("Unexpected chapter content", chapterContent.contains("This is a documentation of this behaviour "));
		assertTrue("Unexpected chapter content", chapterContent.contains("This is a second paragraph of the SysDocing information."));
	}
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void parsesSingleNLXXFileWithBDDKeyWords()
	{
		// arrange
		final File nlxxFile = new File(TestUtil.TEST_DOC_DATA_PATH + "/TestApplication0/xxFolder/FeatureWithBDDKeywords.nlxx");
		final HashMap<String, XXGroupDocData> behaviourChapterData = new HashMap<>();
	
		// act
		cut.toBehaviourChapterData(behaviourChapterData, nlxxFile);
		
		// assert
		assertEquals("Number of chapters", 1, behaviourChapterData.size());
		XXGroupDocData xxGroupDocData = behaviourChapterData.get(new ArrayList(behaviourChapterData.keySet()).get(0));
		assertEquals("XXID", "FeatureWithBDDKeywords", xxGroupDocData.getXXGroupId());
		assertEquals("Number of XXs in file", 1, xxGroupDocData.size());
		
		XXDocData xxDocData = xxGroupDocData.getXXDocData(xxGroupDocData.getAllXXIDs().get(0));
		
		assertEquals("Number of arrange instruction", 2, xxDocData.getArrangeInstructions().size());
		assertEquals("Number of act instruction", 2, xxDocData.getActInstructions().size());
		assertEquals("Number of assert instruction", 2, xxDocData.getAssertInstructions().size());
		
		assertEquals("Instruction", "Given a certain start condition", xxDocData.getArrangeInstructions().get(0));
		assertEquals("Instruction", "And another start condition", xxDocData.getArrangeInstructions().get(1));
		assertEquals("Instruction", "When something is performed", xxDocData.getActInstructions().get(0));
		assertEquals("Instruction", "And something else is performed", xxDocData.getActInstructions().get(1));
		assertEquals("Instruction", "Then a certain end condition is reached", xxDocData.getAssertInstructions().get(0));
		assertEquals("Instruction", "another end condition is reached", xxDocData.getAssertInstructions().get(1));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void parsesSingleNLXXFileWithFeatureButNoBDDKeyWords()
	{
		// arrange
		final File nlxxFile = new File(TestUtil.TEST_DOC_DATA_PATH + "/TestApplication0/xxFolder/FeatureWithoutBDDKeywords.nlxx");
		final HashMap<String, XXGroupDocData> behaviourChapterData = new HashMap<>();
		
		// act
		cut.toBehaviourChapterData(behaviourChapterData, nlxxFile);
		
		// assert
		assertEquals("Number of chapters", 1, behaviourChapterData.size());
		XXGroupDocData xxGroupDocData = behaviourChapterData.get(new ArrayList(behaviourChapterData.keySet()).get(0));
		assertEquals("XXID", "FeatureWithoutBDDKeywords", xxGroupDocData.getXXGroupId());
		assertEquals("Number of XXs in file", 1, xxGroupDocData.size());
		
		XXDocData xxDocData = xxGroupDocData.getXXDocData(xxGroupDocData.getAllXXIDs().get(0));
		
		assertEquals("Number of arrange instruction", 0, xxDocData.getArrangeInstructions().size());
		assertEquals("Number of act instruction", 2, xxDocData.getActInstructions().size());
		assertEquals("Number of assert instruction", 0, xxDocData.getAssertInstructions().size());
		
		assertEquals("Instruction", "Foo", xxDocData.getActInstructions().get(0));
		assertEquals("Instruction", "Bar", xxDocData.getActInstructions().get(1));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void parsesSingleNLXXFileWithBehaviourAndTestPhases()
	{
		// arrange
		final File nlxxFile = new File(TestUtil.TEST_DOC_DATA_PATH + "/TestApplication0/xxFolder/BehaviourWithTestPhases.nlxx");
		final HashMap<String, XXGroupDocData> behaviourChapterData = new HashMap<>();
		
		// act
		cut.toBehaviourChapterData(behaviourChapterData, nlxxFile);
		
		// assert
		assertEquals("Number of chapters", 1, behaviourChapterData.size());
		XXGroupDocData xxGroupDocData = behaviourChapterData.get(new ArrayList(behaviourChapterData.keySet()).get(0));
		assertEquals("XXID", "BehaviourWithTestPhases", xxGroupDocData.getXXGroupId());
		assertEquals("Number of XXs in file", 1, xxGroupDocData.size());
		
		XXDocData xxDocData = xxGroupDocData.getXXDocData(xxGroupDocData.getAllXXIDs().get(0));
		
		assertEquals("Number of arrange instruction", 3, xxDocData.getArrangeInstructions().size());
		assertEquals("Number of act instruction", 3, xxDocData.getActInstructions().size());
		assertEquals("Number of assert instruction", 3, xxDocData.getAssertInstructions().size());
		assertEquals("Number of SysDoc lines", 0, xxGroupDocData.getSysDocingLines().size());
		
		assertEquals("Instruction", "Foo1", xxDocData.getArrangeInstructions().get(1));
		assertEquals("Instruction", "Bar1", xxDocData.getArrangeInstructions().get(2));
		assertEquals("Instruction", "Foo2", xxDocData.getActInstructions().get(1));
		assertEquals("Instruction", "Bar2", xxDocData.getActInstructions().get(2));
		assertEquals("Instruction", "Foo3", xxDocData.getAssertInstructions().get(1));
		assertEquals("Instruction", "Bar3", xxDocData.getAssertInstructions().get(2));
	}

	@Test
	public void parsesSingleNLXXFileWithBehaviourButWithoutTestPhases()
	{
		// arrange
		final File nlxxFile = new File(TestUtil.TEST_DOC_DATA_PATH + "/TestApplication0/xxFolder/BehaviourWithoutTestPhases.nlxx");
		final HashMap<String, XXGroupDocData> behaviourChapterData = new HashMap<>();
		
		// act
		cut.toBehaviourChapterData(behaviourChapterData, nlxxFile);
		
		// assert
		assertEquals("Number of chapters", 1, behaviourChapterData.size());
		XXGroupDocData xxGroupDocData = behaviourChapterData.get(new ArrayList<>(behaviourChapterData.keySet()).get(0));
		assertEquals("XXID", "BehaviourWithoutTestPhases", xxGroupDocData.getXXGroupId());
		assertEquals("Number of XXs in file", 1, xxGroupDocData.size());
		
		XXDocData xxDocData = xxGroupDocData.getXXDocData(xxGroupDocData.getAllXXIDs().get(0));
		
		assertEquals("Number of arrange instruction", 0, xxDocData.getArrangeInstructions().size());
		assertEquals("Number of act instruction", 4, xxDocData.getActInstructions().size());
		assertEquals("Number of assert instruction", 0, xxDocData.getAssertInstructions().size());
		
		assertEquals("Instruction", "Foo", xxDocData.getActInstructions().get(0));
		assertEquals("Instruction", "Bar", xxDocData.getActInstructions().get(1));
		assertEquals("Instruction", "Foo", xxDocData.getActInstructions().get(2));
		assertEquals("Instruction", "Bar", xxDocData.getActInstructions().get(3));
	}
	
}
