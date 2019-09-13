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

import java.io.File;

import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import org.junit.Test;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.testdataimport.TestDataImporter;

public class LanguageTemplatesPdfClassLevelTest
{
    private String testDataDir = "../sysnat.test.runtime.environment/src/test/resources/validation";

    private ExecutableExample executableExample = new ExecutableExample() {
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

        protected TestDataImporter getTestDataImporter() {
            testDataImporter = new TestDataImporter(testDataDir);
            return testDataImporter;
        }
    };

    private LanguageTemplatesPDFValidation cut = new LanguageTemplatesPDFValidation(executableExample);

    @Test
    public void validatesPdfContent()
    {
        // arrange
        String path = SysNatFileUtil.findAbsoluteFilePath("../sysnat.test.runtime.environment/src/test/resources/validation/IKS-Software-zum-Anfassen-Gibt-es-so-etwas.pdf");
        File testPdf = new File(path);

        String validationFileName = "test.validation";
        executableExample.storeTestObject("CCC", "Clean Coding Cosmos");
        executableExample.storeTestObject("Titel", "Literatur & Links");
        executableExample.storeTestObject("Autor", "Reik Oberrath");


        // act
        cut.assertPDFContentMatchesValidation(testPdf, validationFileName);

        // assert
        assertEquals("Number of report messages", 7, executableExample.getReportMessages().size());

        
        assertEquals("Report messages", 
  		             "The data file <b>test.validation</b> has been imported with <b>1</b> datasets.",
                     executableExample.getReportMessages().get(0));
        assertEquals("Report messages", 
        		      "Does PDF <b>IKS-Software-zum-Anfassen-Gibt-es-so-etwas.pdf</b> "
        		      + "contain <b>Softwarequalität</b>? - Ja. &#x1F60A;",
                      executableExample.getReportMessages().get(1));
        assertEquals("Report messages", 
        		     "Does PDF <b>IKS-Software-zum-Anfassen-Gibt-es-so-etwas.pdf</b> on page <b>8</b> "
        		     + "contain <b>Der Autor</b>? - Ja. &#x1F60A;",
                      executableExample.getReportMessages().get(2));
        assertEquals("Report messages", 
        		     "Does PDF <b>IKS-Software-zum-Anfassen-Gibt-es-so-etwas.pdf</b> on page <b>8</b> in line <b>2</b> "
        		     + "contain <b>Die Akteure der Umsetzung</b>? - Ja. &#x1F60A;",
                      executableExample.getReportMessages().get(3));
        assertEquals("Does PDF <b>IKS-Software-zum-Anfassen-Gibt-es-so-etwas.pdf</b> "
        		     + "contain <b>Dr. Reik Oberrath</b>? - Ja. &#x1F60A;", 
        		     executableExample.getReportMessages().get(4));
        assertEquals("Does PDF <b>IKS-Software-zum-Anfassen-Gibt-es-so-etwas.pdf</b> on page <b>Literatur & Links</b> "
        			 + "contain <b>Clean Coding Cosmos</b>? - Ja. &#x1F60A;",
                      executableExample.getReportMessages().get(5));
        assertEquals("Report messages", 
        		     "Does PDF <b>IKS-Software-zum-Anfassen-Gibt-es-so-etwas.pdf</b> on page <b>Literatur & Links</b> "
        		     + "in line <b>2</b> "
        		     + "contain <b>Literatur & Links</b>? - Ja. &#x1F60A;",
                      executableExample.getReportMessages().get(6));
    }
}
