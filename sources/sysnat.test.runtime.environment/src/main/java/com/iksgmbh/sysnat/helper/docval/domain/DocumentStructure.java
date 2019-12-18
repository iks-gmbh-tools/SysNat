/*
 * Copyright 2018 IKS Gesellschaft fuer Informations- und Kommunikationssysteme mbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iksgmbh.sysnat.helper.docval.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;

/**
 * Stores information about a large document that is built as sequence of different document parts. 
 * 
 * @author Reik Oberrath
 */
public class DocumentStructure
{
	public static final String UNKOWN_PART_MARKER = "!!!";
	public static final String UNKOWN_PART = UNKOWN_PART_MARKER + " Unknown Part XX " + UNKOWN_PART_MARKER; 
	public static final String SEQUEL = "Add page to last part added.";  
	
	private String id = null;
	private String name = null;
	private String documentFileName = null;
	private String lastPartId = null;
	private LinkedHashMap<String, String> partNames = new LinkedHashMap<>();
	private Map<String, Integer> pageNumberMap = new HashMap<>();  // number of pages per part
	private int numberOfUnknownParts = 0;
	
	/**
	 * @param aDocumentName : file name of the document to which this DocumentStructure instance refers
	 */
	public DocumentStructure(String aDocumentFileName) {
		this.documentFileName = aDocumentFileName.trim();
	}

	/**
	 * @return a short description of that group of documents to which this DocumentStructure refers
	 */
	public String getDocumentFileName() {
		return documentFileName;
	}

	/**
	 * @return a short description for the significance of this DocumentStructure instance
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return unique identifier of this DocumentStructure instance
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param unique identifier of this DocumentStructure instance
	 */
	public void setId(String aStructureId) {
		this.id = aStructureId.trim();
	}

	/**
	 * @param a short description for the significance of this DocumentStructure instance
	 */
	public void setName(String aName) {
		this.name = aName.trim();
	}

	public int getNumberOfPages(String partName) {
		return pageNumberMap.get(partName);
	}
	
	public void addPageToPart(String partId, String partName) 
	{
		if (partName.equals(SEQUEL)) {
			addPageToLastPart();
		} 
		else 
		{
			if (partId == UNKOWN_PART && lastPartId != null && lastPartId.startsWith(UNKOWN_PART_MARKER)) {
				addPageToPart(partId, SEQUEL);
			} else if (partId.equals(lastPartId)) {
				addPageToLastPart();
			} else {
				addNewPart(partId, partName, 1);
			}
		}
	}

	private void addPageToLastPart()
	{
		int num = pageNumberMap.get(lastPartId);
		pageNumberMap.put(lastPartId, num+1);
	}

	public void addNewPart(String partId, String partName, int numberOfPages) 
	{
		if (partNames.keySet().contains(partId) && partId != UNKOWN_PART) {
			throw new SysNatTestDataException("Dublicate part with id <b>" + partId + "</b> and name <b>" + partName + "</b>.");
		}
		
		
		if (partId.equals(UNKOWN_PART)) 
		{
			numberOfUnknownParts++;
			partId = partId.replace("XX", "" + numberOfUnknownParts);
			partNames.put(partId, partName);
			pageNumberMap.put(partId, numberOfPages);
		} else {			
			partNames.put(partId, partName);
			pageNumberMap.put(partId, numberOfPages);
		}
		
		lastPartId = partId;
	}
	
	public String getIdOfLastPart()  
	{
		if (getNumberOfParts() == 0) return null;
		List<String> ids = new ArrayList<>(partNames.keySet());
		return ids.get(ids.size()-1);
	}

	public int getNumberOfParts() {
		return partNames.size();
	}

	public int getTotalPageNumber() 
	{
		return pageNumberMap.keySet()
				            .stream()
				            .map(key -> pageNumberMap.get(key))
				            .mapToInt( Integer::intValue )
				            .sum();
	}

	public List<String> getOrderedPartIds() {
		List<String> list = new ArrayList<>(partNames.keySet());
		return Collections.unmodifiableList(list);
	}

	@Override
	public String toString() 
	{
		String toReturn = "Document Structure for document '" + documentFileName + "':" + System.getProperty("line.separator");
		int pageCount = 0;
		
		List<String> list = getOrderedPartIds();
		for (String partId : list) 
		{
			pageCount++;
			if (pageNumberMap.get(partId) == 1) {
				toReturn += "Page " + pageCount + ": " + partId + System.getProperty("line.separator"); 
			} else {
				int oldPageNumber = pageCount;
				pageCount += pageNumberMap.get(partId) - 1; 
				toReturn += "Page " + oldPageNumber + "-" + pageCount + ": " + partId;
				String partName = partNames.get(partId);
				if (partName != null && partName.length() > 0) {
					toReturn += " (" + partName + ")";					
				}
				toReturn += System.getProperty("line.separator");
			}
		}
		
		 toReturn += "Summary: " + partNames.size() + " parts with a total of " + getTotalPageNumber() + " pages.";
		return toReturn.trim();
	}

	public boolean contains(String partIdOrName) {
		return getOrderNumber(partIdOrName) > -1;
	}

	public String getPartName(String partId) {
		return partNames.get(partId);
	}

	public int getOrderNumber(String partIdOrName) 
	{
		int toReturn = 0;
		
		List<String> partIds = new ArrayList<>(partNames.keySet());
		for (String partId : partIds) 
		{
			toReturn++;
			if (partId.equals(partIdOrName) || partNames.get(partId).equals(partIdOrName)) {
				return toReturn;
			}
		}
		
		return -1;
	}
	
	public int size() {
		return partNames.size();
	}
}
