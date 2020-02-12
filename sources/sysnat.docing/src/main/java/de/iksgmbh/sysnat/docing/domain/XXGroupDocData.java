package de.iksgmbh.sysnat.docing.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Stores all information about the documentation of a single nlxx file.
 *  
 * @author Reik Oberrath
 */
public class XXGroupDocData
{
	private String xxGroupId;
	private LinkedHashMap<String, XXDocData> xxMap = new LinkedHashMap<>();
	private List<String> sysDoc = new ArrayList<>();
	private List<String> reqDoc = new ArrayList<>();
	
	public int size() {
		return xxMap.size();
	}
	
	public List<String> getAllXXIDs() {
		return new ArrayList<String>(xxMap.keySet());
	}
	
	public XXDocData getXXDocData(String xxid) {
		return xxMap.get(xxid);
	}
	
	public String getXXGroupId() {
		return xxGroupId;
	}

	public void setXXGroupId(String aXXGroupId) {
		xxGroupId = aXXGroupId;		
	}

	public void addXX(XXDocData xxData) {
		xxMap.put(xxData.getXXId(), xxData);
	}

	public void addSysDocLine(final String line) {
		sysDoc.add(line);
	}

	public void addReqDocLine(final String line) {
		reqDoc.add(line);
	}

	public List<String> getSysDocingLines() {
		return sysDoc;
	}

	public List<String> getReqDocingLines() {
		return reqDoc;
	}
}
