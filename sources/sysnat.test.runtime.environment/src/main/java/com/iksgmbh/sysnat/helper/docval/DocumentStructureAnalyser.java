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
package com.iksgmbh.sysnat.helper.docval;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.helper.docval.domain.DocumentContent;
import com.iksgmbh.sysnat.helper.docval.domain.DocumentPart;
import com.iksgmbh.sysnat.helper.docval.domain.DocumentStructure;
import com.iksgmbh.sysnat.testdataimport.ExcelDataProvider;
import com.iksgmbh.sysnat.testdataimport.TestDataImporter;

/**
 * Analyses a document file and returns its structure as a sequence of document parts.
 * Those parts must be defined as a combination of a dat- and and xlsx-file.
 * 
 * @author Reik Oberrath
 */
public class DocumentStructureAnalyser
{
	private static final String ANY_PAGE_IDENTIFIER = "*";
	private static final TestDataImporter testDataImporter = new TestDataImporter(ExecutionRuntimeInfo.getInstance().getTestdataDir());
	public static final String NOT_CONDITION_INDICATOR = "n!";
	
	/**
	 * Contains structure information, i.e. 
	 * which parts may be expected to find in the document to analyse.
	 */
	private LinkedHashMap<String, DocumentPart> knownDocumentParts = new LinkedHashMap<>();

	private DocumentPart currentPart;
	private DocumentPart anyPagePart;

	/**
	 * Contains the content of a document page for page
	 */
	protected DocumentContent documentContent;
	
	/**
	 * Contains the content of a document part for part
	 */
	protected DocumentStructure documentStructure;
	
	protected Properties excelproperties;
	protected String structureInfoFile;

	
	/**
	 * Analyses the document to identify parts it is made of
	 * @param file that contains document to analyse
	 * @param path to structureInfoFile
	 * @return Parts of a document that have been identified
	 */
	public static DocumentStructure doYourJob(File documentFile, File structureInfoFile) {
		return DocumentStructureAnalyser.doYourJob(documentFile.getAbsolutePath(), structureInfoFile.getAbsolutePath());	
    }
	
	/**
	 * Analyses the document to identify parts it is made of
	 * @param file that contains document to analyse
	 * @param path to structureInfoFile
	 * @return Parts of a document that have been identified
	 */
	public static DocumentStructure doYourJob(File documentFile, String structureInfoFile) {
		return DocumentStructureAnalyser.doYourJob(documentFile.getAbsolutePath(), structureInfoFile);	
    }
	
	/**
	 * Analyses the document to identify parts it is made of
	 * @param documentNameWithPath
	 * @param path to structureInfoFile
	 * @return Parts of a document that have been identified
	 */
	public static DocumentStructure doYourJob(String documentNameWithPath, String structureInfoFile)
    {
		DocumentStructureAnalyser structureAnalyser = new DocumentStructureAnalyser(documentNameWithPath);
		structureAnalyser.initStructureInfo(structureInfoFile);
		return structureAnalyser.analyseDocument();	
    }

	DocumentStructureAnalyser(String documentNameWithPath) {
		documentContent = new DocumentContent(documentNameWithPath);
		documentStructure = new DocumentStructure(extractDocName(documentNameWithPath)); 
	}
	
	protected String extractDocName(String documentNameWithPath)
	{
		int pos1 = documentNameWithPath.lastIndexOf("/");
		int pos2 = documentNameWithPath.lastIndexOf(".");
		return documentNameWithPath.substring(pos1+1, pos2);
	}

	void initStructureInfo(String aStructureInfoFile) 
	{
		structureInfoFile = aStructureInfoFile;
		Hashtable<String, Properties> loadTestdata = testDataImporter.loadDatasetsFromFile(new File(aStructureInfoFile));
		excelproperties = loadTestdata.get(loadTestdata.keySet().iterator().next());
		String excelFile = (String) excelproperties.get("ExcelFile");
		if (excelFile == null) {
			throw new SysNatTestDataException("No Excel File defined in " + aStructureInfoFile);
		}
		
		String partsSheet = (String) excelproperties.get("PartsExcelSheet");
		String partsRootCell = (String) excelproperties.get("PartsRootCell");
		
		String parentDir = new File(structureInfoFile).getParent();
		LinkedHashMap<String, Properties> knownPartsData = ExcelDataProvider.doYourJob(new File(parentDir + "/" + excelFile), partsSheet, partsRootCell);
		
		List<String> orderedKeys = orderKeys(new ArrayList<>(knownPartsData.keySet()));
		orderedKeys.forEach(key -> addKnownPart(knownPartsData.get(key)));
	}
	

	private void addKnownPart(Properties partsProperties)
	{
		Integer numberOfPages = Integer.valueOf((String) partsProperties.get("NumberOfPages"));
		String partId = (String) partsProperties.get(TestDataImporter.DATA_SET_EXCEL_ID);
		DocumentPart part = new DocumentPart(partId, numberOfPages);
		knownDocumentParts.put(part.getId(), part);
		
		String name = (String) partsProperties.get("Name");
		part.setPartName(name);
		
		String firstPageIdentifiers = (String) partsProperties.get("FirstPageIdentifiers");
		List<String> firstPageIdentifierList = SysNatStringUtil.toList(firstPageIdentifiers, ",");
		firstPageIdentifierList.forEach(identifier -> part.addFirstPageIdentifier(identifier.trim()));
		
		String sequelPageIdentifiers = (String) partsProperties.get("SequelPageIdentifiers");
		List<String> sequelPageIdentifierList = SysNatStringUtil.toList(sequelPageIdentifiers, ",");
		sequelPageIdentifierList.forEach(identifier -> part.addSequelPageIdentifier(identifier.trim()));
	}

	public DocumentStructure analyseDocument()
    {
        for (int pageNo = 1; pageNo <= documentContent.getNumberOfPages(); pageNo++)  
        {
        	String partId = determinePartOfPage(pageNo);
        	if (partId == DocumentStructure.UNKOWN_PART) {
        		documentStructure.addPageToPart(partId, "");
        	} else {
        		documentStructure.addPageToPart(partId, currentPart.getPartName());
        	}
        }
        
        return documentStructure;
    }


	public String buildCvsLine(String s1, String s2) {
    	return s1 + ";" + s2;
    }

	private String determinePartOfPage(int pageNumber) 
	{		
		final String textFromPage = documentContent.getPageContent(pageNumber).getOriginalPageContent();
		final String textFromPageCompressed = compress(textFromPage);
		
		DocumentPart partCandidate = getPartThatMatchFirstPageIndentifier(pageNumber, textFromPageCompressed);
		if (partCandidate == null) 
		{
			if (currentPart == null) 
			{
				// first part of the document is not known
				currentPart = null;
				return DocumentStructure.UNKOWN_PART;
			}
			
			partCandidate = getPartThatMatchSequelPageIndentifier(pageNumber, textFromPageCompressed);
			
			if (partCandidate == null) 
			{
				// part of sequel page not identifable
				currentPart = null;
				return DocumentStructure.UNKOWN_PART;
			} 
			else 
			{
				// part of sequel page identified
				currentPart = partCandidate;
				return partCandidate.getId();
			}
		} 
		else // part is known by first page identifiers 
		{
			if (currentPart == null) 
			{
				// first part recognized
				currentPart = partCandidate;
				return partCandidate.getId();
			} 
			else if (currentPart.getId().equals(partCandidate.getId())) 
			{
				// assure match of sequel page identifiers
				partCandidate = getPartThatMatchSequelPageIndentifier(pageNumber, textFromPageCompressed + "");
				if (partCandidate == null) 
				{
					// unknown sequel page
					currentPart = null;
					return DocumentStructure.UNKOWN_PART;
				} else {
					// sequel page identified
					return currentPart.getId();
				}
			}
			
			// start of next part identified
			currentPart = partCandidate;
			return partCandidate.getId();
		}
	}

	private DocumentPart getPartThatMatchSequelPageIndentifier(final int pageNumber, 
			                                                   final String textFromPageCompressed)
	{
		@SuppressWarnings({ "rawtypes", "unchecked" })
		List<String> partNameList = new ArrayList(knownDocumentParts.keySet());
		List<DocumentPart> matchingParts = 
				partNameList.stream()
				            .map(partName -> knownDocumentParts.get(partName))
		                    .filter(part -> doesPageMatchPartBySequelPageIdentifier(textFromPageCompressed, part))
		                    .collect(Collectors.toList());

		if (matchingParts.size() > 2) {
			StringBuffer sb = new StringBuffer();
			matchingParts.forEach(part -> sb.append(part.getId()).append(", "));
			String partsString = sb.toString();
			partsString = partsString.substring(0, partsString.length() - 2);
			throw new SysNatTestDataException("Ambigious definition of SequelPageIdentifiers " + "for document part "
			        + partsString + " on page " + pageNumber + " in document " + documentContent.getPdfFileName());
		}

		if (matchingParts.size() == 1) {
			return matchingParts.get(0);
		}
		
		if (anyPagePart != null) {
			DocumentPart toReturn = anyPagePart;
			anyPagePart = null;
			return toReturn;
		}


		return null;
	}

	private DocumentPart getPartThatMatchFirstPageIndentifier(final int pageNumber, 
			                                                  final String textFromPageCompressed)
	{
		@SuppressWarnings({ "rawtypes", "unchecked" })
		List<String> partNameList = new ArrayList(knownDocumentParts.keySet());
		List<DocumentPart> matchingParts = 
				partNameList.stream()
				            .map(partName -> knownDocumentParts.get(partName))
				            .filter(part -> doesPageMatchPartByFirstPageIdentifier(textFromPageCompressed, part))
		                    .collect(Collectors.toList());
		
		if (matchingParts.size() > 2) 
		{
			StringBuffer sb = new StringBuffer();
			matchingParts.forEach(part -> sb.append(part.getId()).append(", "));
			String partsString = sb.toString();
			partsString = partsString.substring(0, partsString.length()-2);
			throw new SysNatTestDataException("Ambigious definition of FirstPageIdentifiers "
					+ "for document part " + partsString + " on page " + pageNumber + " in document "
					+ documentContent.getPdfFileName());
		}
		
		if (matchingParts.size() == 1) {
			return matchingParts.get(0);
		}
		
		if (anyPagePart != null) {
			DocumentPart toReturn = anyPagePart;
			anyPagePart = null;
			return toReturn;
		}
		
		return null;
	}

	/**
	 * Searches for FirstPageIdentifier in a content of pages.
	 * All Identifiers of a certain type must be found in the page's content 
	 * in order to make a match between a page and a part (conjunction).
	 * 
	 * @param pageNumber
	 * @param part
	 * @return true for a match
	 */
	private boolean doesPageMatchPartByFirstPageIdentifier(final String textCompressed, 
			                                               final DocumentPart part)
	{
		final List<String> firstPageIdentifiers = part.getFirstPageIdentifiers();
		if (firstPageIdentifiers.get(0).equals(ANY_PAGE_IDENTIFIER)) 
		{
			anyPagePart = part;
			return false;
		}
		
		final List<String> mustContainValues = firstPageIdentifiers
					.stream()
					.filter(identifier -> ! identifier.startsWith(NOT_CONDITION_INDICATOR))
					.collect(Collectors.toList());
		
		final List<String> mustNotContainValues = firstPageIdentifiers
					.stream()
					.filter(identifier -> identifier.startsWith(NOT_CONDITION_INDICATOR))
					.map(identifier -> identifier.substring(NOT_CONDITION_INDICATOR.length()))
					.collect(Collectors.toList());
		
		final Optional<String> missingMustContainValues = 
				mustContainValues.stream()
								 .filter(identifier -> ! textCompressed.contains(compress(identifier)))
								 .findFirst();
		
		final Optional<String> presentMustNotContainValues = 
				mustNotContainValues.stream()
								    .filter(identifier -> textCompressed.contains(compress(identifier)))
								    .findFirst();
		
		boolean nok = missingMustContainValues.isPresent() || presentMustNotContainValues.isPresent();
		return ! nok;
	}

	/**
	 * Searches for SequelPageIdentifier in a content of pages.
	 * All Identifiers of a certain type must be found in the page's content 
	 * in order to make a match between a page and a part (conjunction).
	 * 
	 * @param compressed text of page content
	 * @param part
	 * @return true for a match
	 */
	private boolean doesPageMatchPartBySequelPageIdentifier(final String textCompressed, 
            											    final DocumentPart part)
	{
		final List<String> sequelPageIdentifiers = part.getSequelPageIdentifiers();
		if (sequelPageIdentifiers.get(0).equals(ANY_PAGE_IDENTIFIER)) 
		{
			anyPagePart = part;
			return false;
		}
		
		final List<String> mustContainValues = sequelPageIdentifiers
					.stream()
					.filter(identifier -> ! identifier.startsWith(NOT_CONDITION_INDICATOR))
					.collect(Collectors.toList());
		
		final List<String> mustNotContainValues = sequelPageIdentifiers
					.stream()
					.filter(identifier -> identifier.startsWith(NOT_CONDITION_INDICATOR))
					.map(identifier -> identifier.substring(NOT_CONDITION_INDICATOR.length()))
					.collect(Collectors.toList());
		
		final Optional<String> missingMustContainValues = 
				mustContainValues.stream()
								 .filter(identifier -> ! textCompressed.contains(compress(identifier)))
								 .findFirst();
		
		final Optional<String> presentMustNotContainValues = 
				mustNotContainValues.stream()
								    .filter(identifier -> textCompressed.contains(compress(identifier)))
								    .findFirst();
		
		boolean nok = missingMustContainValues.isPresent() || presentMustNotContainValues.isPresent();
		return ! nok;
	}	

	private String compress(String text) 
	{
		char[] charArray = text.toCharArray();
		final StringBuffer sb = new StringBuffer();
		
		for (char c : charArray) 
		{
			if (c != ' ' && c > 30) {
				sb.append(c);
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * 
	 * @param partName
	 * @return
	 */
	public List<DocumentPart> getKnownDocumentParts()
	{
		List<DocumentPart> toReturn = new ArrayList<>();
		knownDocumentParts.keySet().forEach(key -> toReturn.add(knownDocumentParts.get(key)));
		return toReturn;
	}
	

	public DocumentPart getDocumentPart(String partNameOrId) 
	{
		DocumentPart toReturn = knownDocumentParts.get(partNameOrId);
		if (toReturn != null) {
			return toReturn;
		}
		
		Optional<DocumentPart> match = knownDocumentParts.keySet()
				                                         .stream()
				                                         .map(key -> knownDocumentParts.get(key))
				                                         .filter(part -> part.getPartName().equals(partNameOrId))
				                                         .findFirst();
		
		if (match.isPresent()) {
			return match.get();
		}
		
		throw new SysNatTestDataException("No document part found with name or id <b>" + partNameOrId + "</b> "
                + "in <b>" + structureInfoFile + "</b>.");
	}

	protected List<String> orderKeys(List<String> keys)
	{
		LinkedHashMap<Integer, String> map = new LinkedHashMap<>(); 
		
		for (Iterator<?> iterator = keys.iterator(); iterator.hasNext();) 
		{
			String key = (String) iterator.next();
			char[] charArray = key.toCharArray();
			String orderNumber = "";
			for (int i = 0; i < charArray.length; i++) 
			{
				if (Character.isDigit(charArray[i]))
				{
					orderNumber += charArray[i];
				}
			}
			if (orderNumber.length() > 0) {
				map.put(Integer.valueOf(orderNumber), key);
			}
		}
		
		List<Integer> orderedOrderNumbers = new ArrayList<>(map.keySet());
		
		Collections.sort(orderedOrderNumbers);
		
		List<String> toReturn = new ArrayList<>();
		orderedOrderNumbers.forEach(orderNumber -> toReturn.add(map.get(orderNumber)));
		return toReturn;
	}


}

