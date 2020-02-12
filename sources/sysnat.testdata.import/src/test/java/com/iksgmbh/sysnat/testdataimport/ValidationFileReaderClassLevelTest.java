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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.testdataimport.domain.DocumentContentSearchValidationRule;
import com.iksgmbh.sysnat.testdataimport.domain.DocumentValidationRule;

public class ValidationFileReaderClassLevelTest
{
    @Test
    public void loadsDocumentsComparisonValidationData()
    {
        // arrange
		String path = SysNatFileUtil.findAbsoluteFilePath("../sysnat.testdata.import/src/test/resources/"
				       + "DocumentContentComparison.nldocval");
        File file = new File(path);

        // act
        final List<DocumentValidationRule> result = ValidationFileReader.doYourJob(file);

        // arrange
        assertEquals("Number of properties", 16, result.size());
    }

	
	
    @Test
    public void loadsSearchValidationData()
    {
        // arrange
		String path = SysNatFileUtil.findAbsoluteFilePath("../sysnat.testdata.import/src/test/resources/DocumentContentSearch.nldocval");
        File file = new File(path);

        // act
        final List<DocumentValidationRule> result = ValidationFileReader.doYourJob(file);

        // arrange
        assertEquals("Number of properties", 10, result.size());
        
        DocumentContentSearchValidationRule rule = (DocumentContentSearchValidationRule) result.get(0);
        assertEquals("ExpectedContent", "Software", rule.getExpectedContent());
        assertEquals("PageNumber", -1, rule.getPageNumber());
        assertEquals("LineNumber", -1, rule.getLineNumber());
        assertEquals("PageIdentifier", null, rule.getPageIdentifier());

        rule = (DocumentContentSearchValidationRule) result.get(1);
        assertEquals("ExpectedContent", "Author", rule.getExpectedContent());
        assertEquals("PageNumber", 8, rule.getPageNumber());
        assertEquals("LineNumber", -1, rule.getLineNumber());
        assertEquals("PageIdentifier", null, rule.getPageIdentifier());

        rule = (DocumentContentSearchValidationRule) result.get(2);
        assertEquals("ExpectedContent", "Figure 1", rule.getExpectedContent());
        assertEquals("PageNumber", 9, rule.getPageNumber());
        assertEquals("LineNumber", 2, rule.getLineNumber());
        assertEquals("PageIdentifier", null, rule.getPageIdentifier());

        rule = (DocumentContentSearchValidationRule) result.get(3);
        assertEquals("ExpectedContent", "Preface", rule.getExpectedContent());
        assertEquals("PageNumber", -1, rule.getPageNumber());
        assertEquals("LineNumber", -1, rule.getLineNumber());
        assertEquals("PageIdentifier", "Content", rule.getPageIdentifier());

        rule = (DocumentContentSearchValidationRule) result.get(4);
        assertEquals("ExpectedContent", "Table", rule.getExpectedContent());
        assertEquals("PageNumber", -1, rule.getPageNumber());
        assertEquals("LineNumber", 3, rule.getLineNumber());
        assertEquals("PageIdentifier", "Summary", rule.getPageIdentifier());
    }
 
    @Test
    public void extractsNumericValuesFromRuleDefinitions()
    {
    	 // arrange
		String path = SysNatFileUtil.findAbsoluteFilePath("../sysnat.testdata.import/src/test/resources/dummy.nldocval");
        String pattern = "Test " + ValidationFileReader.REGEX_ANY + " Test " 
    	                         + ValidationFileReader.REGEX_ANY + " Test " 
        		                 + ValidationFileReader.REGEX_ANY + ".";
		String rule = "Test 1 Test  22  Test 333.";
		
		// act
        final List<String> result = new ValidationFileReader(new File(path)).extractValues(rule, pattern);

        // arrange
        assertEquals("Number of values", 3, result.size());
        assertEquals("Value", "1", result.get(0));
        assertEquals("Value", "22", result.get(1));
        assertEquals("Value", "333", result.get(2));
    }

    @Test
    public void extractsAlphanumericValueFromRuleDefinitions()
    {
    	 // arrange
		String path = SysNatFileUtil.findAbsoluteFilePath("../sysnat.testdata.import/src/test/resources/dummy.nldocval");
        String pattern = "Test " + ValidationFileReader.REGEX_ANY + " Test " 
    	                         + ValidationFileReader.REGEX_ANY + " Test " 
        		                 + ValidationFileReader.REGEX_ANY + ".";
		String rule = "Test \"a a\" Test  \"b  b\"  Test \"c   c\".";
		
		// act
        final List<String> result = new ValidationFileReader(new File(path)).extractValues(rule, pattern);

        // arrange
        assertEquals("Number of values", 3, result.size());
        assertEquals("Value", "a a", result.get(0));
        assertEquals("Value", "b  b", result.get(1));
        assertEquals("Value", "c   c", result.get(2));
    }

    
    @Test
    public void throwsErrorForMissingQuotationMark()
    {
    	// arrange
		String path = SysNatFileUtil.findAbsoluteFilePath("../sysnat.testdata.import/src/test/resources/DocumentContentSearch.nldocval");
    	String errorMessage = ValidationFileReader.BUNDLE.getString("InvalidValidationRuleMessage");
    	ValidationFileReader validationFileReader = new ValidationFileReader(new File(path));
    	List<DocumentValidationRule> rules = new ArrayList<>();
	
    	// check 1
    	String validationRule = "The document contains on page with sequence Content\" the sequence \"Preface\".";
    	if (Locale.getDefault().getLanguage().equals("de")) {
    		validationRule = "Das Dokument enthält auf der Seite mit dem Text Inhalt\" den Text \"Vorwort\".";
    	}
    	checkRule(errorMessage, validationFileReader, rules, validationRule);
		
		
    	// check 2
    	validationRule = "The document contains on page with sequence \"Content the sequence \"Preface\".";
    	if (Locale.getDefault().getLanguage().equals("de")) {
    		validationRule = "Das Dokument enthält auf der Seite mit dem Text \"Inhalt den Text \"Vorwort\".";
    	}    	
		checkRule(errorMessage, validationFileReader, rules, validationRule);
		
    	// check 3
    	validationRule = "The document contains on page with sequence \"Content\" the sequence Preface\".";
    	if (Locale.getDefault().getLanguage().equals("de")) {
    		validationRule = "Das Dokument enthält auf der Seite mit dem Text \"Inhalt\" den Text Vorwort\".";
    	}    	
		checkRule(errorMessage, validationFileReader, rules, validationRule);

    	// check 4
    	validationRule = "The document contains on page with sequence \"Content\" the sequence \"Preface.";
    	if (Locale.getDefault().getLanguage().equals("de")) {
    		validationRule = "Das Dokument enthält auf der Seite mit dem Text \"Inhalt\" den Text \"Vorwort.";
    	}    	
		checkRule(errorMessage, validationFileReader, rules, validationRule);
    }

	private void checkRule(final String errorMessage,
			               final ValidationFileReader validationFileReader,
			               final List<DocumentValidationRule> rules,
			               final String validationRule)
	{
		try {
			// act
			validationFileReader.parseLineToSearchValidationRule(validationRule, rules);
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("Error message", errorMessage.replaceAll("XY", validationRule), e.getMessage());
		}
	}
}
