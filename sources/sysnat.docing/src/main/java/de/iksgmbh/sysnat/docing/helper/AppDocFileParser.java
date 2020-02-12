package de.iksgmbh.sysnat.docing.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;

import de.iksgmbh.sysnat.docing.domain.TestApplicationDocData;

/**
 * Reads data from *.sysdoc files. 
 * 
 * It produces a initial version of a data object called TestApplicationDocData 
 * that does NOT YET contain the behaviours as chapters. It only knows the order of them
 * in the final document to generate.
 */
public class AppDocFileParser
{
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/Docing", Locale.getDefault());
	private static final ResourceBundle BUNDLE_EN = ResourceBundle.getBundle("bundles/Docing", Locale.ENGLISH);
	private static final String BEHAVIOUR_ORDER = "Behaviour Order";

	private boolean doesLineBelongToChapter = false;
	private boolean isBehaviourOrderLine = false;
	private String chapterName;
	private List<String> chapterContent;

	/**
	 * @param sourceFolder where sysdoc- and nlxx-files are located for the test application to document  
	 * @return DocData read from sysdoc files.
	 */
	public TestApplicationDocData doYourJob(final File applicationDocFile)
	{		
		final TestApplicationDocData toReturn = new TestApplicationDocData(applicationDocFile);
		
		if ( ! applicationDocFile.exists() ) 
		{
			System.err.println("File '" + applicationDocFile.getAbsolutePath() + "' does not exist and was created with Default content.");
			createDefaultApplicationSysDocFile(toReturn, applicationDocFile);
		}
		
		parseApplicationDocFile(toReturn, applicationDocFile);
		
		return toReturn;
	}

	private void createDefaultApplicationSysDocFile(TestApplicationDocData docData, File applicationDocFile) 
	{
		applicationDocFile.getParentFile().mkdirs();
		String sysDocFolder = applicationDocFile.getParentFile().getAbsolutePath().replaceAll("\\\\", "/");
		try {
			sysDocFolder = applicationDocFile.getParentFile().getCanonicalPath().replaceAll("\\\\", "/");
		} catch (Exception e) {	/* do nothing */	};
		
		String defaultContent = "Name: <foldername>" 
		                        + System.getProperty("line.separator")
		                        + System.getProperty("line.separator")
		                        + BUNDLE.getString("CHAPTER_IDENTIFIER") + " " + BUNDLE.getString("DefaultIntroChapterName")
		                        + System.getProperty("line.separator")
		                        + BUNDLE.getString("DefaultIntroContent").replace("XY", sysDocFolder);
		SysNatFileUtil.writeFile(applicationDocFile, defaultContent);
	}

	private void parseApplicationDocFile(final TestApplicationDocData docData, final File applicationDocFile)
	{
		final List<String> lines = SysNatFileUtil.readTextFile(applicationDocFile);
		lines.forEach(line -> parseApplicationDocLine(line.trim(), docData));
		if (chapterName != null) {
			docData.addChapter(chapterName, chapterContent); // store last chapter data
		}
		
		if ( ! docData.getChapterNames().contains(BUNDLE.getString("DEFAULT_CHAPTER_NAME"))
		  && ! docData.getChapterNames().contains(BUNDLE_EN.getString("DEFAULT_CHAPTER_NAME")))
		{
			List<String> list = new ArrayList<>();
			String line = BUNDLE.getString("DEFAULT_CHAPTER_CONTENT").replace("XY", applicationDocFile.getName());
			list.add(line.replace("YZ", docData.getTestApplicationName()));
			docData.addChapter(BUNDLE.getString("DEFAULT_CHAPTER_NAME"), list);
		}
	}

	private void parseApplicationDocLine(String line, TestApplicationDocData docData)
	{
		if (line.startsWith("Name")) {
			parseTestApplicationName(line, docData);
			return;
		}
		
		if (line.equalsIgnoreCase(BEHAVIOUR_ORDER)) 
		{
			isBehaviourOrderLine = true;
			doesLineBelongToChapter = false;
		}
		
		if (line.startsWith(BUNDLE.getString("CHAPTER_IDENTIFIER")) 
			|| line.startsWith(BUNDLE_EN.getString("CHAPTER_IDENTIFIER"))) 
		{
			if (doesLineBelongToChapter) {
				docData.addChapter(chapterName, chapterContent); // store parsed chapter
			}
			
			startNewChapterParsing(line);
			return;
		}
		
		if (doesLineBelongToChapter) {
			chapterContent.add(line);
		}
		
		if (isBehaviourOrderLine) 
		{
			if ( ! line.equals(BEHAVIOUR_ORDER) ) {				
				docData.addOrderedBehaviour(line.trim());
			}
		}
		
	}

	private void startNewChapterParsing(final String line)
	{
		chapterName = SysNatStringUtil.parseValueFromPropertyLine(line);
		chapterContent = new ArrayList<String>();
		doesLineBelongToChapter = true;
	}

	private void parseTestApplicationName(String line, TestApplicationDocData docData)
	{
		String name = SysNatStringUtil.parseValueFromPropertyLine(line);
		if (name.equals(SysNatLocaleConstants.PLACEHOLDER_FOLDERNAME) || 
			name.equals(SysNatLocaleConstants.PLACEHOLDER_FOLDERNAME_EN)) 
		{
			name = docData.getDocSourceFolder().getName();
		}
		docData.setTestApplicationName(name);
	}

}
