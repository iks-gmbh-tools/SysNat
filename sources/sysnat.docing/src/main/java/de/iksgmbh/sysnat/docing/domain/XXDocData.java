package de.iksgmbh.sysnat.docing.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.iksgmbh.sysnat.common.utils.SysNatConstants;

/**
 * Stores all information about the documentation of a single XX.
 *  
 * @author Reik Oberrath
 */
public class XXDocData
{
	private String xxid;
	private LinkedHashMap<Integer, List<String>> sections = new LinkedHashMap<>();
	private LinkedHashMap<Integer, String> sectionTypes = new LinkedHashMap<>();
	private List<String> arrangeInstructions = new ArrayList<>();
	private List<String> actInstructions = new ArrayList<>();
	private List<String> assertInstructions = new ArrayList<>();
	private List<String> testDataInfoLines = new ArrayList<>();
	private boolean firstTestDataSetting = false;
	private boolean firstTimeDataTableLine = true;

	public void setXXId(String xxid) {
		this.xxid = xxid; 
	}

	public String getXXId() {
		return xxid;
	}
	
	public void createNewArrangeSection() 
	{
		if ( ! arrangeInstructions.isEmpty()) {
			createNewSection("Arrange", arrangeInstructions);
		}
		arrangeInstructions.add("<p class=\"testphase\">**Arrange**</p>");
	}

	public void createNewActSection() 
	{
		if ( ! actInstructions.isEmpty()) {
			createNewSection("Act", actInstructions);
		}
		actInstructions.add("<p class=\"testphase\">**Act**</p>");
	}

	public void createNewAssertSection() 
	{
		if (! assertInstructions.isEmpty()) {
			createNewSection("Assert", assertInstructions);
		}
		assertInstructions.add("<p class=\"testphase\">**Assert**</p>");
	}
	
	public void addArrangeInstruction(final String line) {
		arrangeInstructions.add(line);
	}

	public void addActInstruction(final String line) {
		actInstructions.add(line);
	}

	public void addAssertInstruction(final String line) {
		assertInstructions.add(line);
	}

	public void addTestDataInfoLine(final String line) 
	{
		if (line.startsWith("|")) 
		{
			if (firstTimeDataTableLine) {				
				firstTimeDataTableLine = false;
				testDataInfoLines.add(System.getProperty("line.separator"));
				testDataInfoLines.add(System.getProperty("line.separator"));
				testDataInfoLines.add("|   |   |");
				testDataInfoLines.add(System.getProperty("line.separator"));
				testDataInfoLines.add("|---|---|");
			}
		}
		
		if (line.startsWith(SysNatConstants.TEST_DATA) && firstTestDataSetting) {
			firstTestDataSetting  = false;
		} else {
			testDataInfoLines.add(System.getProperty("line.separator"));
		}
		testDataInfoLines.add(line);
	}

	public List<String> getArrangeInstructions() {
		return arrangeInstructions;
	}

	public List<String> getActInstructions() {
		return actInstructions;
	}

	public List<String> getAssertInstructions() {
		return assertInstructions;
	}

	public List<String> getTestDataInfoLines() {
		return testDataInfoLines;
	}

	public void resetActInstructions() {
		actInstructions.clear();
	}

	public List<String> getInstructionLines()
	{
		final List<String> list = new ArrayList<String>();
		
		testDataInfoLines.forEach(instruction -> list.add(instruction + "  "));
		if (testDataInfoLines.size() > 0) {
			list.add(System.getProperty("line.separator"));
			list.add(System.getProperty("line.separator"));
		}

		if ( ! arrangeInstructions.isEmpty()) {
			createNewSection("Arrange", arrangeInstructions);
		}
		if ( ! actInstructions.isEmpty()) {
			createNewSection("Act", actInstructions);
		}
		if (! assertInstructions.isEmpty()) {
			createNewSection("Assert", assertInstructions);
		}
		
		final List<Integer> keys = new ArrayList<>(sections.keySet());	
		int sectionCounter = 0;
		for (Integer id : keys) 
		{
			sectionCounter++;
			List<String> instructions = sections.get(id);
			
			
			
			
			if (instructions.size() > 1 || ! instructions.get(0).contains("<p class=\"testphase\">")) {
				buildMarkDownLinesForSection(list, keys, sectionCounter, id, instructions);
			}
			
		}
				
		return list;
	}

	private void buildMarkDownLinesForSection(final List<String> list,
	                                          final List<Integer> keys,
	                                          int sectionCounter,
	                                          Integer id,
	                                          List<String> instructions)
	{
		if (instructions.size() > 0) {
			list.add(System.getProperty("line.separator"));
			list.add(System.getProperty("line.separator"));
		}
		instructions.forEach(instruction -> list.add(instruction + "  "));
		if (instructions.size() > 0) {
			list.add(System.getProperty("line.separator"));
			list.add(System.getProperty("line.separator"));
		}
		
		String type = sectionTypes.get(id);
		if (type.equals("Assert")) 
		{
			if (keys.size() > 1 && sectionCounter < keys.size()) {
				list.add("***");
			}
		}
	}
	
	private void createNewSection(String type, List<String> instructions) 
	{
		int id = sections.size();
		List<String> section = new ArrayList<>();
		section.addAll(instructions);
		sections.put(id, section);
		sectionTypes.put(id, type);
		instructions.clear();
	}
}
