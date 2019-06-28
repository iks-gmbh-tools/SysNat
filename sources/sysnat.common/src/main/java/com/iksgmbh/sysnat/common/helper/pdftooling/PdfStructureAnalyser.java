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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.iksgmbh.sysnat.common.domain.FileList;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;

/**
 * Analyses a Kosyfa Contract PDF file and returns a PdfStructure instance 
 * containing an ordered list of documents.
 * 
 * Not yet in use!
 * 
 * @author Reik Oberrath
 */
public class PdfStructureAnalyser
{	
	public static final boolean invertDocumentOrder = false;
	
	public static final List<PdfContractDocument> documentMetaData = getDocumentMetaData();
	
	
	private PdfContractDocument currentDocument = null;
	
	public void doYourJob()
    {
		System.out.println("");
		System.out.println("PdfAnalyser in action: ");

		File pdfDir = SysNatFileUtil.getDownloadDir();
		FileList files = SysNatFileUtil.findFilesIn("pdf", pdfDir);
		System.out.println("Anzahl zu analysierender PDFs: " + files.size());
		
		for (File pdf : files.getFiles()) 
		{
			System.out.println("");
			String fileAsString = pdfDir.getAbsolutePath() + "/" + pdf.getName();
			PdfDocumentStructure result = analyze(fileAsString);	
			System.out.println(result.toString());
		}
    }

	private static List<PdfContractDocument> getDocumentMetaData() 
	{
		List<PdfContractDocument> metadata = new ArrayList<>(); //Factory.createAllFromDataPool();
		if ( ! invertDocumentOrder) {
			return metadata;
		}
		
		final List<PdfContractDocument> toReturn = new ArrayList<PdfContractDocument>();
		
		for (int i = metadata.size()-1; i>=0; i--) {
			toReturn.add(metadata.get(i));
		}
		
		return toReturn;
	}

	public static PdfDocumentStructure doYourJob(String pdfFileAsString) {
		return new PdfStructureAnalyser().analyze( pdfFileAsString );
    }

	public PdfDocumentStructure analyze(String fileAsString)
    {
		int pos = fileAsString.lastIndexOf('/');
    	PdfDocumentStructure toReturn = new PdfDocumentStructure(fileAsString.substring(pos+1));
    	
        try 
        {
            PdfReader pdfReader = new PdfReader(fileAsString);
            toReturn.setValidationNumberOfPages(pdfReader.getNumberOfPages());
            int totalNumberOfPages = pdfReader.getNumberOfPages();
            int numberOfPagesPerDocument = 0;
            
            for (int i = 1; i <= totalNumberOfPages; i++)  
            {
				String textFromPage;
				try {
					// reading some of the pdfs work only that way - reason unclear 
					textFromPage = PdfTextExtractor.getTextFromPage(pdfReader, i);
				} catch (Exception e) {
					// reading some of the pdfs work only that way - reason unclear
					textFromPage = PdfTextExtractor.getTextFromPage(pdfReader, i, new SimpleTextExtractionStrategy());  // do not reuse instance of SimpleTextExtractionStrategy() !!!
				}
				
				String documentTitle = determineDocumentTypeOfPage(textFromPage);
				
				if (documentTitle == PdfDocumentStructure.SEQUEL_TYPE) 
				{
					numberOfPagesPerDocument++;
					if (currentDocument != null && numberOfPagesPerDocument > currentDocument.getSeitenanzahl()) 
					{
						// das passiert, wenn der Vertrag des Bürgen direkt hinter dem des Kunden gedruckt wird 
						// dies geschieht für PrintTarget CUSTOMER_SHORT mit 2. KN
						documentTitle = currentDocument.getTitel();
						numberOfPagesPerDocument = 1;
					}
					
				} else {
					numberOfPagesPerDocument = 1;
				}
				
                toReturn.addPage(documentTitle);
            }

        } catch (IOException e) {
        	throw new RuntimeException(e);
        }
        
        return toReturn;
    }


	public String buildCvsLine(String s1, String s2) {
    	return s1 + ";" + s2;
    }

	private String determineDocumentTypeOfPage(String textFromPage) 
	{
		final String textCompressed = compress(textFromPage);
		
		if (isExplicitSequel(textCompressed)) {
			if (currentDocument == null) {
				return PdfDocumentStructure.UNKOWN_DOCUMENT_MARKER;
			}
			return PdfDocumentStructure.SEQUEL_TYPE;
		}

		PdfContractDocument document = getDocument(textCompressed);
		if (document != null) 
		{
			if (currentDocument != null && document.getTitel().equals(currentDocument.getTitel())) {
				return PdfDocumentStructure.SEQUEL_TYPE;
			}
			currentDocument = document;
			return currentDocument.getTitel();
		}
		
		if ( isImplicitSequel(textCompressed) ) {
			return PdfDocumentStructure.SEQUEL_TYPE;
		}
		
		currentDocument = null;
		return PdfDocumentStructure.UNKOWN_DOCUMENT_MARKER;
	}

	private PdfContractDocument getDocument(final String textCompressed) 
	{
		// Try to find document type by FirstPageIdentifiers.
		// More than one identifier are needed because a single one (the headline) is frequently not unique.
		// For sequel pages no match will occur here!
		for (PdfContractDocument documentMetaData : documentMetaData) 
		{
//			if (documentMetaData.getTechnischeId().contains("PDF_CONTRACT_AUTOFLEX")
//					&& textCompressed.contains("ex-Darlehensvertrag")) {
//				System.out.println();
//			}
			String ersteSeiteIdentifier = documentMetaData.getErsteSeiteIdentifier();
			if (ersteSeiteIdentifier == null || ersteSeiteIdentifier.trim().length() == 0) {
				continue;
			}
			String[] splitResult = ersteSeiteIdentifier.split("#");
			
			if (splitResult.length == 1) {
				String compressedUeberschrift = compress(documentMetaData.getErsteSeiteIdentifier());
				if (textCompressed.contains(compressedUeberschrift)) {
					return documentMetaData;
				}
			} else {
				boolean ok = true;
				for (String identifier: splitResult) 
				{
					String compressedIdentifier = compress(identifier);
					if (compressedIdentifier.startsWith("nicht|") && compressedIdentifier.endsWith("|"))
					{
						compressedIdentifier = compressedIdentifier.substring(6, compressedIdentifier.length()-1);
						if ( textCompressed.contains(compressedIdentifier) ) {
							ok = false;
						}
					} else {
						if ( ! textCompressed.contains(compressedIdentifier) ) {
							ok = false;
						}
					}
				}
				if (ok) {
					return documentMetaData;
				}
			}
		}

		return null;
	}

	private boolean isImplicitSequel(final String textCompressed) 
	{
		// Try to find document type by SequelPageIdentifier
		// Clue: DokumentID from footer or arbitrary, but unique text  
		for (PdfContractDocument documentMetaData : documentMetaData) 
		{
			String folgeSeiteIdentifier = documentMetaData.getFolgeSeitenIdentifier();
			if (folgeSeiteIdentifier != null && folgeSeiteIdentifier.trim().length() > 0) 
			{				
				String[] splitResult = folgeSeiteIdentifier.split("#");
				for (String alternativText: splitResult) {
					String compressedAlternativText = compress(alternativText);
					if (textCompressed.contains(compressedAlternativText)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	private boolean isExplicitSequel(String textCompressed) 
	{
		if (textCompressed.contains(compress("Fortsetzung von Seite"))) {
			return true;
		}
		
		if ( ! textCompressed.contains(compress("Seite 1 von")) ) 
		{
			int maximumPageNumber = 20; 
			for (int i=2; i<=maximumPageNumber; i++) {
				String fortsetzungsIdentifier = "Seite" + i + "von";
				if (textCompressed.contains(fortsetzungsIdentifier)) {
					return true;
				}
			}
		}
		
		return false;
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
		
		String toReturn = sb.toString();
		
		// Sonderlocke für Problemen in bestimmten Dokumenten
		toReturn = toReturn.replace("Bedingungenfrdenec-/Maestro-Service", "Bedingungenfürdenec-/Maestro-Service");

		return toReturn;
	}
}

