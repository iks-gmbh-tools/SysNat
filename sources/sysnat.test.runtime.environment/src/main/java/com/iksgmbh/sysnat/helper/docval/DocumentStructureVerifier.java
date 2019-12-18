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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.helper.docval.domain.DocumentStructure;
import com.iksgmbh.sysnat.testdataimport.ExcelDataProvider;
import com.iksgmbh.sysnat.testdataimport.TestDataImporter;

/**
 * Analyses and verifies a document file by comparing it with predefined document structures.
 * Those structures must be defined as a combination of a dat- and and xlsx-file.
 * 
 * @author Reik Oberrath
 */
public class DocumentStructureVerifier extends DocumentStructureAnalyser
{
	public static final String OK_VERIFIED = "OK";
	/**
	 * Contains structure information, i.e. 
	 * which parts may be expected to find in the document to analyse.
	 */
	private LinkedHashMap<String, DocumentStructure> knownDocumentStructures = new LinkedHashMap<>();


	/**
	 * Analyses the document to identify its structure and verifies it, by
	 * @param File of document to verify
	 * @param structureInfoFile
	 * @param id or name of known structure
	 * @return "OK" if no document is verified successfully
	 */
	public static String doYourJob(File pdfFile, File structureDatFile, String idOrNameOfStructure) {
		return doYourJob(pdfFile.getAbsolutePath(), structureDatFile.getAbsolutePath(), idOrNameOfStructure);
	}

	/**
	 * Analyses the document to identify its structure and verifies it, by
	 * @param documentNameWithPath
	 * @param path to structureInfoFile
	 * @param id or name of known structure
	 * @return "OK" if no document is verified successfully
	 */
	public static String doYourJob(final String documentNameWithPath, 
			                       final String structureInfoFile, 
			                       final String expectedStructureNameOrId)
    {
		DocumentStructureVerifier structureVerifier = new DocumentStructureVerifier(documentNameWithPath);
		structureVerifier.initStructureInfo(structureInfoFile);
		DocumentStructure actualStructure = structureVerifier.analyseDocument();
		DocumentStructure expectedStructure = structureVerifier.findExpectedStructure(expectedStructureNameOrId);
		return structureVerifier.verify(expectedStructure, actualStructure);
    }
	
	void initStructureInfo(String structureInfoFile) 
	{
		super.initStructureInfo(structureInfoFile);

		String excelFile = (String) excelproperties.get("ExcelFile");
		String partsSheet = (String) excelproperties.get("StructuresExcelSheet");
		String partsRootCell = (String) excelproperties.get("StructuresRootCell");
		
		String parentDir = new File(structureInfoFile).getParent();
		LinkedHashMap<String, Properties> knownStructureData = ExcelDataProvider.doYourJob(new File(parentDir + "/" + excelFile), partsSheet, partsRootCell);
		knownStructureData.keySet().forEach(key -> addKnownStructure(knownStructureData.get(key)));
	}


	private void addKnownStructure(Properties structuresProperties)
	{
		Set<Object> keySet = structuresProperties.keySet();
		List<String> list = new ArrayList<>();
		keySet.forEach(key -> list.add((String) key));
		final List<String> orderedKeys = orderKeys(list);
		final String structureName = structuresProperties.getProperty("Name");
		final DocumentStructure knownDocumentStructure = new DocumentStructure(documentStructure.getDocumentFileName());		
		final String structureId = structuresProperties.getProperty(TestDataImporter.DATA_SET_EXCEL_ID);
		
		knownDocumentStructure.setName(structureName);
		knownDocumentStructure.setId(structureId);
		orderedKeys.stream()
		           .filter(key -> ((String)structuresProperties.get(key)).equalsIgnoreCase("X"))
		           .forEach(key -> knownDocumentStructure.addNewPart(key, getPartName(key), getPageNumber(key)));
		
		knownDocumentStructures.put(structureId, knownDocumentStructure);
	}

	private String getPartName(String partId)
	{
		return getDocumentPart(partId).getPartName();
	}

	private int getPageNumber(String partId) {
		return getDocumentPart(partId).getNumberOfPages();
	}

	private String verify(final DocumentStructure expectedStructure, 
			              final DocumentStructure actualStructure)
	{
		if (expectedStructure.toString().equals(actualStructure.toString())) {			
			return OK_VERIFIED;
		} else {
			String separatorLine = "############################################";
			StringBuffer sb = new StringBuffer(); 
			sb.append(separatorLine).append(System.getProperty("line.separator"));
			sb.append("EXPECTED STRUCTURE:").append(System.getProperty("line.separator"));
			sb.append("---------------------")
			  .append(System.getProperty("line.separator"));
			sb.append(expectedStructure.toString());
			sb.append(System.getProperty("line.separator"))
			  .append(separatorLine)
			  .append(System.getProperty("line.separator"));
			sb.append("ACTUAL STRUCTURE:").append(System.getProperty("line.separator"));
			sb.append("---------------------")
			  .append(System.getProperty("line.separator"));
			sb.append(actualStructure.toString());
			sb.append(System.getProperty("line.separator"))
			  .append(separatorLine)
			  .append(System.getProperty("line.separator"));
			
			String diffReport = sb.toString(); 
			System.err.println(diffReport);
			return diffReport;
		}
	}

	private DocumentStructure findExpectedStructure(String expectedStructureNameOrId)
	{
		DocumentStructure toReturn = knownDocumentStructures.get(expectedStructureNameOrId);
		
		if (toReturn != null) {
			return toReturn;
		}
		
		Optional<String> match = knownDocumentStructures.keySet().stream().filter(key -> knownDocumentStructures.get(key).getName().equals(expectedStructureNameOrId)).findFirst();
		
		if (match.isPresent()) {
			return knownDocumentStructures.get(match.get());
		}
		
		throw new SysNatTestDataException("No document structure found with name or id <b>" + expectedStructureNameOrId + "</b> "
				                           + "in <b>" + structureInfoFile + "</b>.");
	}

	DocumentStructureVerifier(String documentNameWithPath) {
		super(documentNameWithPath);
	}

	public List<DocumentStructure> getKnownDocumentStructures()
	{
		List<DocumentStructure> toReturn = new ArrayList<>();
		knownDocumentStructures.entrySet().forEach(entry -> toReturn.add((DocumentStructure) entry.getValue()));
		return toReturn;
	}
	
}

