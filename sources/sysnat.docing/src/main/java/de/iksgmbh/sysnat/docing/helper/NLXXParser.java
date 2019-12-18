package de.iksgmbh.sysnat.docing.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.iksgmbh.sysnat.common.helper.FileFinder;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.TestPhase;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;

import de.iksgmbh.sysnat.docing.domain.TestApplicationDocData;
import de.iksgmbh.sysnat.docing.domain.XXDocData;
import de.iksgmbh.sysnat.docing.domain.XXGroupDocData;

/**
 * Reads data from *.nlxx files.
 */
public class NLXXParser
{
	public static final String SYS_DOC_IDENTIFIER = "SysDoc:";
	public static final String REQ_DOC_IDENTIFIER = "ReqDoc:";
	
	private boolean doesLineBelongToIgnoreSection = false;
	private boolean doesLineBelongToXX = false;
	private boolean doesLineBelongToXXSectionArrange = false;
	private boolean doesLineBelongToXXSectionAct = false;
	private boolean doesLineBelongToXXSectionAssert = false;
	private boolean doesLineBelongToSysDoc = false;
	private boolean doesLineBelongToReqDoc = false;
	
	private XXDocData xxDocData = null;

	/**
	 * Enriches docData with data read from nlxx files.
	 * @param docData
	 * @return enrichted docData
	 */
	public void doYourJob(final TestApplicationDocData docData)
	{
		final LinkedHashMap<String, XXGroupDocData> behaviourChapterData = new LinkedHashMap<>();
		final List<File> nlxxFiles = FileFinder.searchFilesRecursively(docData.getDocSourceFolder(), "nlxx");
		nlxxFiles.forEach(file -> toBehaviourChapterData(behaviourChapterData, file));
		addBehaviourChaptersInDefinedOrder(docData.getOrderedBehaviours(), docData, behaviourChapterData);
	}

	private void addBehaviourChaptersInDefinedOrder(List<String> orderedBehaviours, 
			                                        TestApplicationDocData docData, 
			                                        HashMap<String, XXGroupDocData> behaviourChapterData)
	{
		List<String> foundOrderedBehaviours = new ArrayList<>();
		orderedBehaviours.forEach(behaviourName -> addAsChapter(behaviourName, docData, behaviourChapterData, foundOrderedBehaviours));
		// add chapters not defined in order
		behaviourChapterData.entrySet().stream()
		                    .filter(entry -> ! foundOrderedBehaviours.contains(entry.getKey()))
				            .forEach(entry -> docData.addChapter(entry.getValue()));
	}

	private void addAsChapter(String behaviourName,
	                          TestApplicationDocData docData,
	                          HashMap<String, XXGroupDocData> behaviourChapterData,
	                          List<String> foundOrderedBehaviours)
	{
		docData.addChapter(behaviourChapterData.get(behaviourName));
		foundOrderedBehaviours.add(behaviourName);
	}

	void toBehaviourChapterData(final HashMap<String, XXGroupDocData> behaviourChapterData, 
			                    final File nlxxFile)
	{
		final XXGroupDocData nlxxData = new XXGroupDocData(); 
		final List<String> lines = SysNatFileUtil.readTextFile(nlxxFile);
		lines.forEach(line -> parseNLXXLine(line.trim(), nlxxData, nlxxFile.getName()));
		if (xxDocData != null) nlxxData.addXX(xxDocData); // store last XX
		behaviourChapterData.put(nlxxData.getXXGroupId(), nlxxData);
	}

	private void parseNLXXLine(final String line, 
			                   final XXGroupDocData nlxxData,
			                   final String filename)
	{
		if (line.startsWith(SysNatLocaleConstants.FEATURE_KEYWORD)
				|| line.startsWith(SysNatLocaleConstants.FEATURE_KEYWORD_EN)
				|| line.startsWith("Behavior")
				|| line.startsWith(SysNatLocaleConstants.BEHAVIOUR_KEYWORD_EN)
				|| line.startsWith(SysNatLocaleConstants.BEHAVIOUR_KEYWORD))
		{
			parseBehaviourName(line, nlxxData, filename);
			return;
		}
		
		if (line.startsWith(SYS_DOC_IDENTIFIER)) 
		{
			startSysDocParsing(nlxxData);
			return;
		}

		if (line.startsWith(REQ_DOC_IDENTIFIER)) 
		{
			startReqDocParsing(nlxxData);
			return;
		}
		
		if (line.startsWith(SysNatLocaleConstants.SCENARIO_KEYWORD)
				|| line.startsWith(SysNatLocaleConstants.SCENARIO_KEYWORD_EN)
				|| line.startsWith("XX"))
		{
			startXXParsing(line, nlxxData);
			return;
		}
		
		addLine(line, nlxxData);
	}

	private void addLine(final String line, final XXGroupDocData nlxxData)
	{
		if (doesLineBelongToXX) 
		{
			addXXLine(line, nlxxData);
			return;
		}

		if (doesLineBelongToSysDoc) {
			nlxxData.addSysDocLine(line);
			return;
		}
		
		if (doesLineBelongToReqDoc) {
			nlxxData.addReqDocLine(line);
			return;
		}

		// ignore those lines that not belong to one of the sections above
	}

	private void addXXLine(String line, XXGroupDocData nlxxData)
	{
		if (line.trim().isEmpty()) {
			return; // ignore this line
		}
		
		if (line.equalsIgnoreCase(SysNatConstants.TEST_PHASE + ": " + TestPhase.ARRANGE.name())) 
		{
			doesLineBelongToIgnoreSection = false;
			doesLineBelongToXXSectionArrange = true;
			doesLineBelongToXXSectionAct = false;
			doesLineBelongToXXSectionAssert = false;
			return;
		}
		
		if (line.toUpperCase().startsWith(TestPhase.GIVEN.name())
			|| line.toUpperCase().startsWith(TestPhase.ANGENOMMEN.name())) 
		{
			doesLineBelongToIgnoreSection = false;
			doesLineBelongToXXSectionArrange = true;
			doesLineBelongToXXSectionAct = false;
			doesLineBelongToXXSectionAssert = false;
			xxDocData.addArrangeInstruction(line);
			return;
		}
		
		if (line.equalsIgnoreCase(SysNatConstants.TEST_PHASE + ": " + TestPhase.ACT.name())) 
		{
			doesLineBelongToIgnoreSection = false;
			doesLineBelongToXXSectionArrange = false;
			doesLineBelongToXXSectionAct = true;
			doesLineBelongToXXSectionAssert = false;
			return;
		}
		
		if (line.toUpperCase().startsWith(TestPhase.WHEN.name())
			|| line.toUpperCase().startsWith(TestPhase.WENN.name())) 
		{
			doesLineBelongToIgnoreSection = false;
			doesLineBelongToXXSectionArrange = false;
			doesLineBelongToXXSectionAct = true;
			doesLineBelongToXXSectionAssert = false;
			xxDocData.addActInstruction(line);
			return;
		}
				
		if (line.equalsIgnoreCase(SysNatConstants.TEST_PHASE + ": " + TestPhase.ASSERT.name())) 
		{
			doesLineBelongToIgnoreSection = false;
			doesLineBelongToXXSectionArrange = false;
			doesLineBelongToXXSectionAct = false;
			doesLineBelongToXXSectionAssert = true;
			return;
		}
		
		if (line.toUpperCase().startsWith(TestPhase.THEN.name())
			|| line.toUpperCase().startsWith(TestPhase.DANN.name()))
		{
			doesLineBelongToIgnoreSection = false;
			doesLineBelongToXXSectionArrange = false;
			doesLineBelongToXXSectionAct = false;
			doesLineBelongToXXSectionAssert = true;
			xxDocData.addAssertInstruction(line);
			return;
		}
		
		if (doesLineBelongToIgnoreSection || line.startsWith(SysNatConstants.TEST_PHASE + ": ")) {
			doesLineBelongToIgnoreSection = true;
			doesLineBelongToXXSectionArrange = false;
			doesLineBelongToXXSectionAct = false;
			doesLineBelongToXXSectionAssert = false;
			return; // ignore other Test-Phases
		}
		
		if (doesLineBelongToXXSectionArrange) {
			xxDocData.addArrangeInstruction(line);
			return;
		}
		
		if (doesLineBelongToXXSectionAct) {
			xxDocData.addActInstruction(line);
			return;
		}
		
		if (doesLineBelongToXXSectionAssert) {
			xxDocData.addAssertInstruction(line);
			return;
		}
		
		if (line.toUpperCase().startsWith(SysNatConstants.ASTERIX)) {
			line = line.substring(1).trim();
		}		
		
		// no section defined so arbitrarily assign this line to ACT
		xxDocData.addActInstruction(line);
	}

	private void startXXParsing(final String line, XXGroupDocData nlxxData)
	{
		doesLineBelongToSysDoc = false;
		doesLineBelongToReqDoc = false;
		
		if (doesLineBelongToXX) {
			nlxxData.addXX(xxDocData); // store last XX
		}
		
		xxDocData = new XXDocData();
		parseXXID(line, xxDocData);
		doesLineBelongToXX = true;
	}

	private void startReqDocParsing(XXGroupDocData nlxxData)
	{
		doesLineBelongToReqDoc = true;
		doesLineBelongToSysDoc = false;
		if (doesLineBelongToXX) {
			nlxxData.addXX(xxDocData); // store last XX
			doesLineBelongToXX = false;
		}
	}

	private void startSysDocParsing(XXGroupDocData nlxxData)
	{
		doesLineBelongToSysDoc = true;
		doesLineBelongToReqDoc = false;
		if (doesLineBelongToXX) {
			nlxxData.addXX(xxDocData); // store last XX
			doesLineBelongToXX = false;
		}
	}

	private void parseBehaviourName(final String line, 
			                        final XXGroupDocData nlxxData, 
			                        final String filename)
	{
		String xxGroupID = SysNatStringUtil.parseValueFromPropertyLine(line);
		
		if (xxGroupID.equals(SysNatLocaleConstants.PLACEHOLDER_FILENAME) || 
			xxGroupID.equals(SysNatLocaleConstants.PLACEHOLDER_FILENAME_EN))
		{
			int pos = filename.indexOf(".");
			xxGroupID = filename.substring(0, pos);
		}
		
		nlxxData.setXXGroupId(xxGroupID);
	}
	
	private void parseXXID(final String line, final XXDocData xxData)
	{
		String xxid = SysNatStringUtil.parseValueFromPropertyLine(line);
		xxData.setXXId(xxid);
	}
	
	/**
	 * for test purpose only
	 */
	XXDocData getXXData()
	{
		return xxDocData;
	}

}
