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
import com.iksgmbh.sysnat.utils.StageInstructionUtil;

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
	
	private boolean doesLineBelongToPrivateBehaviourHeader = false;
	private boolean doesLineBelongToXX = false;
	private boolean doesLineBelongToXXButIgnoreIt = false;
	private boolean doesLineBelongToXXTestDataInfo = false;
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
		addBehaviourChaptersInDefinedOrder(docData, behaviourChapterData);
	}

	private void addBehaviourChaptersInDefinedOrder(TestApplicationDocData docData, 
			                                        HashMap<String, XXGroupDocData> behaviourChapterData)
	{
		List<String> orderInfoFromSysDocFile = docData.getOrderedBehaviours();
		if (orderInfoFromSysDocFile.isEmpty()) {
			orderInfoFromSysDocFile.addAll(behaviourChapterData.keySet());  // take order read from file system
		}
		if (orderInfoFromSysDocFile.isEmpty()) {
			return;
		}
		List<String> orderedBehaviours = new ArrayList<>();
		orderInfoFromSysDocFile.forEach(behaviourName -> addAsChapter(behaviourName, docData, behaviourChapterData, orderedBehaviours));
		// add chapters not defined in order
		behaviourChapterData.entrySet().stream()
		                    .filter(entry -> ! orderedBehaviours.contains(entry.getKey()))
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
		if (nlxxFile.getName().equals("Relogin.nlxx")) {
			System.out.println("");
		}
		final XXGroupDocData nlxxData = new XXGroupDocData(); 
		final List<String> lines = SysNatFileUtil.readTextFile(nlxxFile);
		lines.forEach(line -> parseNLXXLine(line.trim(), nlxxData, nlxxFile.getName()));
		
		if (nlxxData.getXXGroupId() == null) {
			nlxxData.setXXGroupId(getChapterNameFromFileName(nlxxFile));
		}
		behaviourChapterData.put(nlxxData.getXXGroupId(), nlxxData);
	}

	private String getChapterNameFromFileName(File nlxxFile)
	{
		String name = nlxxFile.getName();
		int pos = name.lastIndexOf(".");
		return name.substring(0,pos);
	}

	private void parseNLXXLine(String line, 
			                   final XXGroupDocData nlxxData,
			                   final String filename)
	{
		if (isComment(line)) return;
		line = removeInlineComment(line);
		
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
			startXXParsing(line, nlxxData, filename);
			return;
		}
		
		if ( ! doesLineBelongToPrivateBehaviourHeader ) {
			addLine(line, nlxxData);
		}
	}

	private String removeInlineComment(String line)
	{
		int pos = line.indexOf("#");
		if (pos == -1) return line;
		return line.substring(0, pos).trim();
	}

	private void addLine(final String line, final XXGroupDocData nlxxData)
	{
		if (doesLineBelongToSysDoc) {
			nlxxData.addSysDocLine(line);
		} else if (doesLineBelongToReqDoc) {
			nlxxData.addReqDocLine(line);
		} else if (doesLineBelongToXX) {
			addXXLine(line, nlxxData);
		}

		// ignore those lines that not belong to one of the sections above
	}

	private void addXXLine(String line, XXGroupDocData nlxxData)
	{
		if (line.trim().isEmpty()) {
			return; // ignore this line
		}

		if (line.startsWith(SysNatConstants.TEST_DATA) || line.startsWith(SysNatConstants.TEST_PARAMETER)) 
		{
			doesLineBelongToXXTestDataInfo = true;
			doesLineBelongToXXSectionArrange = true;
			doesLineBelongToXXSectionAct = false;
			doesLineBelongToXXSectionAssert = false;
			xxDocData.addTestDataInfoLine(line);
			return;
		}
		
		if (StageInstructionUtil.isStageInstruction(line) && 
			! line.startsWith(SysNatConstants.TEST_PHASE)) {
			return;
		}
		
		if (isTestPhaseLine(line)) {
			return;
		}
		
		if (doesLineBelongToXXButIgnoreIt) {
			return;
		}
		
		if (line.toUpperCase().startsWith(TestPhase.GIVEN.name())
			|| line.toUpperCase().startsWith(TestPhase.ANGENOMMEN.name())) 
		{
			doesLineBelongToXXTestDataInfo = false;
			doesLineBelongToXXSectionArrange = true;
			doesLineBelongToXXSectionAct = false;
			doesLineBelongToXXSectionAssert = false;
			xxDocData.addArrangeInstruction(line);
			return;
		}
		
		if (line.toUpperCase().startsWith(TestPhase.WHEN.name())
			|| line.toUpperCase().startsWith(TestPhase.WENN.name())) 
		{
			doesLineBelongToXXTestDataInfo = false;
			doesLineBelongToXXSectionArrange = false;
			doesLineBelongToXXSectionAct = true;
			doesLineBelongToXXSectionAssert = false;
			xxDocData.addActInstruction(line);
			return;
		}
		
		if (line.toUpperCase().startsWith(TestPhase.THEN.name())
			|| line.toUpperCase().startsWith(TestPhase.DANN.name()))
		{
			doesLineBelongToXXTestDataInfo = false;
			doesLineBelongToXXSectionArrange = false;
			doesLineBelongToXXSectionAct = false;
			doesLineBelongToXXSectionAssert = true;
			xxDocData.addAssertInstruction(line);
			return;
		}
		
		if (line.toUpperCase().startsWith(SysNatConstants.ASTERIX + " ")) {
			line = line.substring(2);
		}		
		
		if (doesLineBelongToXXTestDataInfo) {
			xxDocData.addTestDataInfoLine(line);
			return;
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
		
		// no section defined so arbitrarily assign this line to ACT
		xxDocData.addActInstruction(line);
	}

	private boolean isComment(String line) {
		return line.startsWith("#");
	}

	private boolean isTestPhaseLine(String line)
	{
		if (line.equalsIgnoreCase(SysNatConstants.TEST_PHASE + ": " + TestPhase.ARRANGE.name())) 
		{
			doesLineBelongToXXTestDataInfo = false;
			doesLineBelongToXXSectionArrange = true;
			doesLineBelongToXXSectionAct = false;
			doesLineBelongToXXSectionAssert = false;
			doesLineBelongToXXButIgnoreIt = false;
			xxDocData.createNewArrangeSection();
			//xxDocData.resetActInstructions();
			return true;
		} 
		
		if (line.equalsIgnoreCase(SysNatConstants.TEST_PHASE + ": " + TestPhase.ACT.name())) 
		{
			doesLineBelongToXXTestDataInfo = false;
			doesLineBelongToXXSectionArrange = false;
			doesLineBelongToXXSectionAct = true;
			doesLineBelongToXXSectionAssert = false;
			doesLineBelongToXXButIgnoreIt = false;
			xxDocData.createNewActSection();
			return true;
		} 
		
		if (line.equalsIgnoreCase(SysNatConstants.TEST_PHASE + ": " + TestPhase.ASSERT.name())) 
		{
			doesLineBelongToXXTestDataInfo = false;
			doesLineBelongToXXSectionArrange = false;
			doesLineBelongToXXSectionAct = false;
			doesLineBelongToXXSectionAssert = true;
			doesLineBelongToXXButIgnoreIt = false;
			xxDocData.createNewAssertSection();
			return true;
		}
		
		if (line.startsWith(SysNatConstants.TEST_PHASE + ": ")) 
		{
			// ignore all other phases
			doesLineBelongToXXTestDataInfo = false;
			doesLineBelongToXXSectionArrange = false;
			doesLineBelongToXXSectionAct = false;
			doesLineBelongToXXSectionAssert = false;
			doesLineBelongToXXButIgnoreIt = true;
			return true;
		}
		
		return false;
	}

	private void startXXParsing(final String line, 
			                    final XXGroupDocData nlxxData, 
			                    final String filename)
	{
		doesLineBelongToPrivateBehaviourHeader = false;
		doesLineBelongToSysDoc = false;
		doesLineBelongToReqDoc = false;
		
		xxDocData = new XXDocData();
		parseXXID(line, xxDocData, filename);
		nlxxData.addXX(xxDocData); 
		doesLineBelongToXX = true;
	}

	private void startReqDocParsing(XXGroupDocData nlxxData)
	{
		doesLineBelongToPrivateBehaviourHeader = false;
		doesLineBelongToReqDoc = true;
		doesLineBelongToSysDoc = false;
	}

	private void startSysDocParsing(XXGroupDocData nlxxData)
	{
		doesLineBelongToPrivateBehaviourHeader = false;
		doesLineBelongToSysDoc = true;
		doesLineBelongToReqDoc = false;
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
		doesLineBelongToPrivateBehaviourHeader = true;
	}
	
	private void parseXXID(final String line, final XXDocData xxData, String filename)
	{
		String xxid = SysNatStringUtil.parseValueFromPropertyLine(line);
		
		if (xxid.equals(SysNatLocaleConstants.PLACEHOLDER_FILENAME) 
			|| xxid.equals(SysNatLocaleConstants.PLACEHOLDER_FILENAME_EN))  
		{
			xxData.setXXId(SysNatStringUtil.cutExtension(filename));
		} else {
			xxData.setXXId(xxid);
		}
	}
	
	/**
	 * for test purpose only
	 */
	XXDocData getXXData()
	{
		return xxDocData;
	}

}
