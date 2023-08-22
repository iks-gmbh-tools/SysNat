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
package com.iksgmbh.sysnat.language_templates.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.testdataimport.TestDataImporter;
import com.iksgmbh.sysnat.utils.TestUtils;

public class LanguageTemplatesDocValClassLevelTest
{
    private String testDataDir = "../sysnat.test.runtime.environment/src/test/resources/validation";
    
    private ExecutableExample executableExample = new ExecutableExample() 
    {
    	@Override
    	public void executeTestCase() {
    		
    	}
    	
    	@Override
    	public String getTestCaseFileName() {
    		return "aTest";
    	}
    	
    	@Override
    	public Package getTestCasePackage() {
    		return null;
    	}
    	
    	@Override
    	public boolean doesTestBelongToApplicationUnderTest() {
    		return true;
    	}
    	
    	@Override
    	public TestDataImporter getTestDataImporter() 
    	{
    		if (testDataImporter == null) {
    			testDataImporter = new TestDataImporter(testDataDir);
    		}
    		return testDataImporter;
    	}
    };
    
    private LanguageTemplatesDocVal cut = new LanguageTemplatesDocVal(executableExample);

    @Test
    public void throwsExceptionForNonExistingShouldBeFile()
    {
        // arrange
        String path = SysNatFileUtil.findAbsoluteFilePath("../sysnat.test.runtime.environment/src/test/resources/validation/CompareTest.pdf");
        File testPdf = new File(path);
        String validationFileName = "PDFCompareWithNonExistingShouldBeFile.nldocval";

        try {
        	// act
        	cut.validateDocumentContentByWholeContentComparison(testPdf, validationFileName);
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("Error message", "Should-Be-File 'NonExistingShouldBeFile.pdf' defined in validation file does not exist.", e.getMessage());
		}
    }
    
    @Test
    public void throwsExceptionForNonExistingShouldBeRule()
    {
        // arrange
        String path = SysNatFileUtil.findAbsoluteFilePath("../sysnat.test.runtime.environment/src/test/resources/validation/CompareTest.pdf");
        File testPdf = new File(path);
        String validationFileName = "PDFCompareMissingShouldBeRule.nldocval";

        try {
        	// act
        	cut.validateDocumentContentByWholeContentComparison(testPdf, validationFileName);
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("Error message", "Validation file used to validate a document by comparison does not define a should-be-file.", e.getMessage());
		}
    }
    
    @Test
    public void validatesPdfByContentComparison()
    {
        // arrange
        String path = SysNatFileUtil.findAbsoluteFilePath("../sysnat.test.runtime.environment/src/test/resources/validation/CompareTest.pdf");
        File testPdf = new File(path);
        String validationFileName = "PDFCompare.nldocval";

        try {
        	// act
        	cut.validateDocumentContentByWholeContentComparison(testPdf, validationFileName);
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertListEquals(loadExpectedLines("CompareDifferenceReport.txt"), executableExample.getReportMessages());
		}
    }
    

	@Test
    public void validatesPdfByContentSearch()
    {
        // arrange
        String path = SysNatFileUtil.findAbsoluteFilePath("../sysnat.test.runtime.environment/src/test/resources/validation/IKS-Software-zum-Anfassen-Gibt-es-so-etwas.pdf");
        File testPdf = new File(path);

        String validationFileName = "PDFSearch.nldocval";
        executableExample.storeTestObject("CCC", "Clean Coding Cosmos");
        executableExample.storeTestObject("Titel", "Literatur & Links");
        executableExample.storeTestObject("Autor", "Reik Oberrath");


        // act
        cut.validateDocumentContentByWholeContentComparison(testPdf, validationFileName);

        // assert
        assertEquals("Number of report messages", 9, executableExample.getReportMessages().size());
        
        assertEquals("Report messages", 
  		             "The data file <b>PDFSearch.nldocval</b> has been imported with <b>1</b> dataset(s).",
                     executableExample.getReportMessages().get(0));
        assertEquals("Report messages", 
        		      "Does document <b>IKS-Software-zum-Anfassen-Gibt-es-so-etwas.pdf</b> "
        		      + "contain the sequence <b>Softwarequalität</b>? - Ja. &#x1F60A;",
                      executableExample.getReportMessages().get(1));
        assertEquals("Report messages", 
        		     "Does document <b>IKS-Software-zum-Anfassen-Gibt-es-so-etwas.pdf</b> on page <b>8</b> "
        		     + "contain the sequence <b>Der Autor</b>? - Ja. &#x1F60A;",
                      executableExample.getReportMessages().get(2));
        assertEquals("Report messages", 
        		     "Does document <b>IKS-Software-zum-Anfassen-Gibt-es-so-etwas.pdf</b> on page <b>8</b> in line <b>2</b> "
        		     + "contain the sequence <b>Die Akteure der Umsetzung</b>? - Ja. &#x1F60A;",
                      executableExample.getReportMessages().get(3));
        assertEquals("Does document <b>IKS-Software-zum-Anfassen-Gibt-es-so-etwas.pdf</b> on page <b>Abb. 3</b> "
        		      + "contain the sequence <b>Ein ganzheitliches Model</b>? - Ja. &#x1F60A;",
        		     executableExample.getReportMessages().get(4));
        assertEquals("Does document <b>IKS-Software-zum-Anfassen-Gibt-es-so-etwas.pdf</b> on page <b>Tabelle 1</b> in line <b>2</b> "
        		      + "contain the sequence <b>Nutzer des Qualitätsmodell</b>? - Ja. &#x1F60A;",
                      executableExample.getReportMessages().get(5));
        assertEquals("Report messages", 
        		     "Does document <b>IKS-Software-zum-Anfassen-Gibt-es-so-etwas.pdf</b> "
        		     + "contain the sequence <b>Dr. Reik Oberrath</b>?"
        		     + " - Ja. &#x1F60A;",
                      executableExample.getReportMessages().get(6));
        assertEquals("Report messages", 
   		             "Does document <b>IKS-Software-zum-Anfassen-Gibt-es-so-etwas.pdf</b> on page <b>Literatur & Links</b> "
   		             + "contain the sequence <b>Clean Coding Cosmos</b>? - Ja. &#x1F60A;",
                      executableExample.getReportMessages().get(7));
        assertEquals("Report messages", 
   		             "Does document <b>IKS-Software-zum-Anfassen-Gibt-es-so-etwas.pdf</b> on page <b>Literatur & Links</b> "
   		             + "in line <b>2</b> "
   		             + "contain the sequence <b>Literatur & Links</b>? - Ja. &#x1F60A;",
                     executableExample.getReportMessages().get(8));
    }

	@Test
    public void validatesPdfStuctureByTextFileComparison()
    {
        // arrange
        String path = SysNatFileUtil.findAbsoluteFilePath("../sysnat.test.runtime.environment/src/test/resources/pdfStructureTestData/EBook.pdf");
        File testPdf = new File(path);
        String validationFileName = "ExpectedEBookStructure.txt";
        String structureInfoFile = "TestStructureInfo.dat";
        ExecutionRuntimeInfo.getInstance().setTestApplicationName("");
        System.setProperty("sysnat.testdata.import.directory", "../sysnat.test.runtime.environment/src/test/resources");
        
        // act
        cut.validateDocumentStructureByTextFileComparison(testPdf, structureInfoFile, validationFileName);

        // assert
        assertEquals("Number of report messages", 1, executableExample.getReportMessages().size());
        
        assertEquals("Report messages", 
  		             "Does structure of document <b>EBook.pdf</b> in assumption of <b>TestStructureInfo.dat</b> match "
  		             + "content of file <b>ExpectedEBookStructure.txt</b>? - Ja. &#x1F60A;",
                     executableExample.getReportMessages().get(0));
    }

	@Test
    public void validatesPdfStuctureByStructureDefinition()
    {
        // arrange
        String path = SysNatFileUtil.findAbsoluteFilePath("../sysnat.test.runtime.environment/src/test/resources/pdfStructureTestData/EBook.pdf");
        File testPdf = new File(path);
        String structureInfoFile = "TestStructureInfo.dat";
        ExecutionRuntimeInfo.getInstance().setTestApplicationName("");
        System.setProperty("sysnat.testdata.import.directory", "../sysnat.test.runtime.environment/src/test/resources");
        
        // act
        cut.validateDocumentStructureByPredefinedStructure(testPdf, structureInfoFile, "Sample");

        // assert
        assertEquals("Number of report messages", 1, executableExample.getReportMessages().size());
        
        assertEquals("Report messages", 
  		             "Does structure of document <b>EBook.pdf</b> in assumption of "
  		             + "<b>TestStructureInfo.dat</b> match structure <b>Sample</b>? - Ja. &#x1F60A;",
                     executableExample.getReportMessages().get(0));
    }
	
	// #############################################################################################
	//                         P r i v a t e   M e t h o d s
	// #############################################################################################
	
    private void assertListEquals(List<String> expectedMessages, List<String> reportMessages)
	{
        assertEquals("number of message", expectedMessages.size(), reportMessages.size());
        
    	int size = expectedMessages.size();
    	for (int i = 0; i<size; i++) 
    	{
    		String expected = expectedMessages.get(i);
    		if (i != 0 && i+1 < size) expected = "//" + expected;
    		
			expected = TestUtils.cutMainPath(expected.replace("sysnat.test.runtime.environment\\..\\", ""));
			String actual = TestUtils.cutMainPath(reportMessages.get(i).replace("sysnat.quality.assurance\\..\\", ""));

            assertEquals( i + "th report message", expected, actual);
    	}
	}

	private List<String> loadExpectedLines(String filename)
	{
		String expectedReportMessages = SysNatFileUtil.readTextFileToString(
				"../sysnat.test.runtime.environment/src/test/resources/expectedReports/" + filename);

		return SysNatStringUtil.toList(expectedReportMessages, System.getProperty("line.separator"));
	}
	
}
