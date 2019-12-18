package de.iksgmbh.sysnat.docing.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores all information about the documentation of a single XX.
 *  
 * @author Reik Oberrath
 */
public class XXDocData
{
	private String xxid;
	private List<String> arrangeInstructions = new ArrayList<>();
	private List<String> actInstructions = new ArrayList<>();
	private List<String> assertInstructions = new ArrayList<>();

	public void setXXId(String xxid) {
		this.xxid = xxid;
	}

	public String getXXId() {
		return xxid;
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

	public List<String> getArrangeInstructions()
	{
		return arrangeInstructions;
	}

	public List<String> getActInstructions()
	{
		return actInstructions;
	}

	public List<String> getAssertInstructions()
	{
		return assertInstructions;
	}
	
	public List<String> getInstructionLines()
	{
		final List<String> list = new ArrayList<String>();
		
		list.addAll(arrangeInstructions);
		list.add(System.getProperty("line.separator"));
		list.addAll(actInstructions);
		list.add(System.getProperty("line.separator"));
		list.addAll(assertInstructions);
		
		return list;
	}
	
}
