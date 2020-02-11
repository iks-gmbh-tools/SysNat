package de.iksgmbh.sysnat.docing.domain;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import com.iksgmbh.sysnat.common.utils.SysNatConstants.DocumentationDepth;

import de.iksgmbh.sysnat.docing.DocingRuntimeInfo;

/**
 * Stores all information about the documentation of a whole test application.
 *  
 * @author Reik Oberrath
 */
public class TestApplicationDocData
{
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

	public List<String> getChapterNames() {
		return new ArrayList<>(chapterMap.keySet());
	}
	
	public void addChapter(String chapterName, List<String> chapterContent) {
		chapterMap.put(chapterName, toContentString(chapterName, chapterContent));
	}

	public void addChapter(XXGroupDocData nlxxData) {
		chapterMap.put(nlxxData.getXXGroupId(), toContentString(nlxxData));
	}
	
	public String getChapterContent(String chapterName) {
		return chapterMap.get(chapterName);
	}
	
	public String getChapterName(int chapterNumber) 
	{
		if (chapterNumber < 1) {
			throw new IllegalArgumentException("Chapter Numbers must be larger than 0.");
		}
		if (chapterMap.size() == 0) {
			return null;
		}
		
		List<String> keys = new ArrayList<>(chapterMap.keySet());
		return keys.get(chapterNumber-1);
	}
	
	// ###############################################################################
	//                     P r i v a t e   M e t h o d s
	// ###############################################################################

	private String toContentString(XXGroupDocData nlxxData)
	{
		final StringBuffer sb = new StringBuffer();
		sb.append(System.getProperty("line.separator"));
		sb.append(System.getProperty("line.separator"));		
		sb.append("# " + nlxxData.getXXGroupId());		
		sb.append(System.getProperty("line.separator"));
		sb.append(System.getProperty("line.separator"));

		sb.append(System.getProperty("line.separator"));

		sb.append(System.getProperty("line.separator"));
		sb.append(System.getProperty("line.separator"));
		
		nlxxData.getSysDocingLines().forEach(line -> sb.append(line).append(System.getProperty("line.separator")));
		sb.append(System.getProperty("line.separator"));
		sb.append(System.getProperty("line.separator"));

		final List<String> xxids = nlxxData.getAllXXIDs();
		xxids.forEach(xxid -> appendXXToStringBuffer(sb, nlxxData.getXXGroupId(), nlxxData.getXXDocData(xxid)));

		return sb.toString();
	}
/*
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
*/
	private void appendXXToStringBuffer(StringBuffer sb, String xxGroupId, XXDocData xxDocData)
	{
		DocumentationDepth docDepth = DocingRuntimeInfo.getInstance().getDocDepth();
		
		if (docDepth == DocumentationDepth.Minimum) {
			// append nothing
		} else {
			if ( ! xxDocData.getXXId().equals(xxGroupId) ) 
			{
				// for non-standalone XXs (i.e. nlxx filese with behaviour declaration)
				sb.append(System.getProperty("line.separator"))
				.append("## " + xxDocData.getXXId()).append(System.getProperty("line.separator"))
				.append(System.getProperty("line.separator"))
				.append(System.getProperty("line.separator"));
			}
			
			if (docDepth != DocumentationDepth.Medium) {
				sb.append(System.getProperty("line.separator"));
				xxDocData.getInstructionLines().forEach(line -> sb.append(line));  
			}
		}
	}

	private String toContentString(String chapterName, List<String> chapterContent)
	{
		final StringBuffer toReturn = new StringBuffer();
		
		toReturn.append(System.getProperty("line.separator"))
		        .append("# " + chapterName)
		        .append(System.getProperty("line.separator"))
		        .append(System.getProperty("line.separator"));
		chapterContent.forEach(line -> addToContent(line, toReturn));
		return toReturn.toString();
	}
	
	private void addToContent(String line, StringBuffer toReturn)
	{
		if (! line.isEmpty()) {
			toReturn.append(line);
		}
		toReturn.append(System.getProperty("line.separator"));
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
