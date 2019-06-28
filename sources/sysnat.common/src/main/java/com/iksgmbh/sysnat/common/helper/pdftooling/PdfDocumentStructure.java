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
package com.iksgmbh.sysnat.common.helper.pdftooling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores information about a Contract-PDF consisting of 
 * a number of smaller documents, i.e. which documents
 * are contained in the contract and in which order.
 * 
 * Not yet in use!
 * 
 * @author Reik Oberrath
 */
public class PdfDocumentStructure 
{
	public static final String UNKOWN_DOCUMENT_MARKER = "  !!! UNSPEZIFIZIERTES DOKUMENT !!!";
	public static final String SEQUEL_TYPE = "<Fortsetzung>";
	
	private String pdfName = null;
	private List<String> documents = new ArrayList<>();
	private List<Integer> numberOfPages = new ArrayList<>();
	private int validationNumberOfPages;
	
	public PdfDocumentStructure(String aName) {
		this.pdfName = aName.trim();
	}

	public String getPdfName() {
		return pdfName;
	}
	
	public void addPage(String documentName) 
	{
		if (documents.size() == 0) {
			addDocument(documentName, 1);
		} else if (documentName.equals(SEQUEL_TYPE)) 
		{
			int indexOfLastDocument = getDocumentNumber()-1;
			int num = numberOfPages.get(indexOfLastDocument);
			numberOfPages.set(indexOfLastDocument, num+1);
		} else {
			addDocument(documentName, 1);
		}
	}

	public void addDocument(String documentName, int pageNumber) 
	{
		documents.add(documentName);
		numberOfPages.add(pageNumber);
	}
	
	public String getLastDocument()  
	{
		if (getDocumentNumber() == 0) return null;
		return documents.get(getDocumentNumber()-1);
	}

	public int getDocumentNumber() {
		return documents.size();
	}

	public int getPageNumber() 
	{
		return numberOfPages.stream().mapToInt( Integer::intValue ).sum();
	}

	public List<String> getOrderedDocuments() {
		return Collections.unmodifiableList(documents);
	}

	@Override
	public String toString() 
	{
		String toReturn = "PdfStructure von " + pdfName + " :" + System.getProperty("line.separator");
		int i = 0;
		int totalPageNumber = 0;
		
		for (String document : documents) 
		{
			totalPageNumber++;
			if (numberOfPages.get(i) == 1) {
				toReturn += "Page " + totalPageNumber + ": " + document + System.getProperty("line.separator"); 
			} else {
				int oldPageNumber = totalPageNumber;
				totalPageNumber += numberOfPages.get(i) - 1; 
				toReturn += "Page " + oldPageNumber + "-" + totalPageNumber + ": " + document + System.getProperty("line.separator");
			}
			i++;
		}
		
		 toReturn += "Zusammenfassung: " + getDocumentNumber() + " Dokumente und "
                       + getNumberOfPages() + " Seiten.";
		return toReturn.trim();
	}

	private int getNumberOfPages() 
	{
		int toReturn = 0;
		for (Integer i : numberOfPages) {
			toReturn += i;
		}
		return toReturn;
	}

	public int getValidationNumberOfPages() {
		return validationNumberOfPages;
	}

	public void setValidationNumberOfPages(int validationNumberOfPages) {
		this.validationNumberOfPages = validationNumberOfPages;
	}

	public boolean contains(String titel) {
		return getOrderNumber(titel) > -1;
	}

	public int getOrderNumber(String titel) 
	{
		int toReturn = 0;
		
		for (String docTitle : documents) 
		{
			toReturn++;
			if (docTitle.equals(titel)) {
				return toReturn;
			}
		}
		
		return -1;
	}
}
