package de.iksgmbh.sysnat.docing.domain;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.ResourceBundle;

/**
 * Stores all information about the documentation of a whole test application.
 *  
 * @author Reik Oberrath
 */
public class TestApplicationDocData
{
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/Docing", Locale.getDefault());

	private File appDocSourceFile;
	private String testAppName;
	private LinkedHashMap<String, String> chapterMap = new LinkedHashMap<>();
	private List<String> orderedBehaviours = new ArrayList<>();


	public TestApplicationDocData(File aSourceFile) {
		appDocSourceFile = aSourceFile;
	}

	public File getDocSourceFolder() {
		return appDocSourceFile.getParentFile();
	}

	public String getTestApplicationName() {
		return testAppName;
	}
	
	public void setTestApplicationName(String aName) {
		testAppName = aName;
	}

	public void addChapter(String chapterName, List<String> chapterContent) {
		chapterMap.put(chapterName, toContentString(chapterContent));
	}

	public void addChapter(XXGroupDocData nlxxData) {
		chapterMap.put(nlxxData.getXXGroupId(), toContentString(nlxxData));
	}
	
	public String getChapterContent(String chapterName) {
		return chapterMap.get(chapterName);
	}
	
	
	// ###############################################################################
	//                     P r i v a t e   M e t h o d s
	// ###############################################################################

	private String toContentString(XXGroupDocData nlxxData)
	{
		// TODO DocTiefe einbauen
		final StringBuffer sb = new StringBuffer(nlxxData.getXXGroupId());
		sb.append(System.getProperty("line.separator"));
		sb.append(System.getProperty("line.separator"));
		
		nlxxData.getSysDocingLines().forEach(line -> sb.append(line).append(System.getProperty("line.separator")));
		sb.append(System.getProperty("line.separator"));
		sb.append(System.getProperty("line.separator"));

		final List<String> xxids = nlxxData.getAllXXIDs();
		xxids.forEach(xxid -> appendToStringBuffer(sb, nlxxData.getXXDocData(xxid)));

		final List<String> sysDocLines = getSysDocingLines(nlxxData);
		sysDocLines.forEach(line -> sb.append(line).append(System.getProperty("line.separator")));

		return sb.toString();
	}

	private List<String> getSysDocingLines(XXGroupDocData nlxxData)
	{
		List<String> sysDocingLines = nlxxData.getSysDocingLines();
		
		if (sysDocingLines == null) {
			sysDocingLines = new ArrayList<>();
		}
		
		if (sysDocingLines.isEmpty()) {
			sysDocingLines.add(BUNDLE.getString("MSG_MISSING_SYSDOC_SECTION"));			
		}
		
		return sysDocingLines;
	}

	private void appendToStringBuffer(StringBuffer sb, XXDocData xxDocData)
	{
		sb.append(xxDocData.getXXId()).append(System.getProperty("line.separator"));
		sb.append(System.getProperty("line.separator"));
		sb.append(System.getProperty("line.separator"));
		
		final List<String> instructionLines = xxDocData.getInstructionLines();
		instructionLines.forEach(line -> sb.append(line).append(System.getProperty("line.separator")));
	}

	private String toContentString(List<String> chapterContent)
	{
		final StringBuffer toReturn = new StringBuffer();
		chapterContent.forEach(line -> addToContent(line, toReturn));
		return toReturn.toString().trim();
	}
	
	private void addToContent(String line, StringBuffer toReturn)
	{
		if (line.isEmpty()) {
			toReturn.append(System.getProperty("line.separator"));
		} else {
			toReturn.append(line);
		}
	}

	public LinkedHashMap<String, String> getChapters() {
		return chapterMap;
	}
	
	@SuppressWarnings("unchecked")
	public Entry<String, String> getChapterByIndex(int i) {
		return (Entry<String, String>) chapterMap.entrySet().toArray()[i];
	}

	public List<String> getOrderedBehaviours() {
		return orderedBehaviours;
	}

	public void addOrderedBehaviour(String aBehaviourName) {
		orderedBehaviours.add(aBehaviourName);
	}
}
