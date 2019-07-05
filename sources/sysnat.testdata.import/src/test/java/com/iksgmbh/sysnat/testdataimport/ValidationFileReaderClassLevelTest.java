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
package com.iksgmbh.sysnat.testdataimport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Locale;

import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import org.junit.Test;

import com.iksgmbh.sysnat.common.exception.SysNatException;

public class ValidationFileReaderClassLevelTest
{
    @Test
    public void loadsValidationData()
    {
        // arrange
		String path = SysNatFileUtil.findAbsoluteFilePath("../sysnat.testdata.import/src/test/resources/test.validation");
        File file = new File(path);

        // act
        final LinkedHashMap<String, String> result = ValidationFileReader.doYourJob(file);

        // arrange
        assertEquals("Number of properties", 6, result.size());
        assertEquals("Value", "", result.get("Softwarequalität"));
        assertEquals("Value", "8::2", result.get("Die Akteure der Umsetzung"));
        assertEquals("Value", "8", result.get("Der Autor"));
        assertEquals("Value", "", result.get("Dr. <Autor>"));
        assertEquals("Value", "Literatur & Links", result.get("<CCC>"));
        assertEquals("Value", "Literatur & Links::2", result.get("<Titel>"));

    }
 
    @Test
    public void throwsErrorForMissingQuotationMark()
    {
    	// arrange
    	String errorMessage = ValidationFileReader.BUNDLE.getString("InvalidValidationRuleMessage");
    	ValidationFileReader validationFileReader = new ValidationFileReader(null);
    	LinkedHashMap<String, String> keyValuePairs = new LinkedHashMap<>();
	
    	// check 1
    	String validationRule = "The PDF contains on page with sequence Content\" the sequence \"Preface\".";
    	if (Locale.getDefault().getLanguage().equals("de")) {
    		validationRule = "Das PDF enthält auf der Seite mit dem Text Inhalt\" den Text \"Vorwort\".";
    	}
    	checkRule(errorMessage, validationFileReader, keyValuePairs, validationRule);
		
		
    	// check 2
    	validationRule = "The PDF contains on page with sequence \"Content the sequence \"Preface\".";
    	if (Locale.getDefault().getLanguage().equals("de")) {
    		validationRule = "Das PDF enthält auf der Seite mit dem Text \"Inhalt den Text \"Vorwort\".";
    	}    	
		checkRule(errorMessage, validationFileReader, keyValuePairs, validationRule);
		
    	// check 3
    	validationRule = "The PDF contains on page with sequence \"Content\" the sequence Preface\".";
    	if (Locale.getDefault().getLanguage().equals("de")) {
    		validationRule = "Das PDF enthält auf der Seite mit dem Text \"Inhalt\" den Text Vorwort\".";
    	}    	
		checkRule(errorMessage, validationFileReader, keyValuePairs, validationRule);

    	// check 4
    	validationRule = "The PDF contains on page with sequence \"Content\" the sequence \"Preface.";
    	if (Locale.getDefault().getLanguage().equals("de")) {
    		validationRule = "Das PDF enthält auf der Seite mit dem Text \"Inhalt\" den Text \"Vorwort.";
    	}    	
		checkRule(errorMessage, validationFileReader, keyValuePairs, validationRule);
		
    }

	private void checkRule(String errorMessage,
	        ValidationFileReader validationFileReader,
	        LinkedHashMap<String, String> keyValuePairs,
	        String validationRule)
	{
		try {
			// act
			validationFileReader.addToProperties(validationRule, keyValuePairs);
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("Error message", errorMessage.replaceAll("XY", validationRule), e.getMessage());
		}
	}
}
