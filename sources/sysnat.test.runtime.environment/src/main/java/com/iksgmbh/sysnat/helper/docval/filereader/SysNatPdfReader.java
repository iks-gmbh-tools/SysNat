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
package com.iksgmbh.sysnat.helper.docval.filereader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.helper.docval.domain.PageContent;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;

public class SysNatPdfReader
{
	public static List<PageContent> doYourJob(final String pdfFileName) 
	{
		final List<PageContent> toReturn = new ArrayList<>();
		
        try 
        {
            PdfReader pdfReader = new PdfReader(pdfFileName);
            int totalNumberOfPages = pdfReader.getNumberOfPages();
            
            for (int pageNo = 1; pageNo <= totalNumberOfPages; pageNo++)  
            {
				String pageContentAsString;
				try {
					// reading some PDFs work only that way - reason unclear 
					pageContentAsString = PdfTextExtractor.getTextFromPage(pdfReader, pageNo);
				} catch (Exception e) {
					// reading some PDFs work only that way - reason unclear
					pageContentAsString = PdfTextExtractor.getTextFromPage(pdfReader, pageNo, new SimpleTextExtractionStrategy());  // do not reuse instance of SimpleTextExtractionStrategy() !!!
				}
				toReturn.add(new PageContent(pageNo, pageContentAsString));
            }
        } catch (IOException e) {
        	throw new SysNatException("Error parsing PDF <b>" + pdfFileName + "</b>.");
        }
		
        return toReturn;
	}
}
