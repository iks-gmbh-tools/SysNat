package com.iksgmbh.sysnat.language_templates.common;

import static org.junit.Assert.assertEquals;

import java.io.File;

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

    private LanguageTemplatesPDF cut = new LanguageTemplatesPDF(executableExample);

    @Test
    public void validatesPdfContent()
    {
        // arrange
        File testPdf = new File("../sysnat.test.runtime.environment/src/test/resources/validation/IKS-Software-zum-Anfassen-Gibt-es-so-etwas.pdf");
        String validationFileName = "test.validation";
        executableExample.storeTestObject("CCC", "Clean Coding Cosmos");
        executableExample.storeTestObject("Titel", "Literatur & Links");
        executableExample.storeTestObject("Autor", "Reik Oberrath");


        // act
        cut.assertFileContainsValidationTexts(testPdf, validationFileName);

        // assert
        assertEquals("Number of report messages", 6, executableExample.getReportMessages().size());

        assertEquals("Report messages", "Enthält das Dokument IKS-Software-zum-Anfassen-Gibt-es-so-etwas.pdf auf der Seite 'Literatur & Links' " +
                                                          "den Text \"Clean Coding Cosmos\"? - Ja. &#x1F60A;",
                      executableExample.getReportMessages().get(0));
        assertEquals("Report messages", "Enthält das Dokument IKS-Software-zum-Anfassen-Gibt-es-so-etwas.pdf auf der Seite 'Literatur & Links' " +
                                                          "in der Zeile 2 den Text \"Literatur & Links\"? - Ja. &#x1F60A;",
                      executableExample.getReportMessages().get(1));
        assertEquals("Report messages", "Enthält das Dokument IKS-Software-zum-Anfassen-Gibt-es-so-etwas.pdf auf der Seite '8' " +
                                                          "den Text \"Der Autor\"? - Ja. &#x1F60A;",
                      executableExample.getReportMessages().get(2));
        assertEquals("Report messages", "Enthält das Dokument IKS-Software-zum-Anfassen-Gibt-es-so-etwas.pdf auf der Seite '8' " +
                                                          "in der Zeile 2 den Text \"Die Akteure der Umsetzung\"? - Ja. &#x1F60A;", executableExample.getReportMessages().get(3));
        assertEquals("Report messages", "Enthält das Dokument IKS-Software-zum-Anfassen-Gibt-es-so-etwas.pdf den Text \"Dr. Reik Oberrath\"?" +
                                                          " - Ja. &#x1F60A;",
                      executableExample.getReportMessages().get(4));
        assertEquals("Report messages", "Enthält das Dokument IKS-Software-zum-Anfassen-Gibt-es-so-etwas.pdf den Text \"Softwarequalität\"? - " +
                                                          "Ja. &#x1F60A;",
                      executableExample.getReportMessages().get(5));
    }
}
