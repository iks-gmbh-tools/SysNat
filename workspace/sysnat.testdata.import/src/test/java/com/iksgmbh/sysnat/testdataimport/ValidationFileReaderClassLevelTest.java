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

import java.io.File;
import java.util.Properties;

import org.junit.Test;

public class ValidationFileReaderClassLevelTest
{
    @Test
    public void loadsValidationData()
    {
        // arrange
        File file = new File("../sysnat.testdata.import/src/test/resources/test.validation");

        // act
        final Properties result = ValidationFileReader.doYourJob(file);

        // arrange
        assertEquals("Number of properties", 6, result.size());
        assertEquals("Value", "", result.getProperty("Softwarequalität"));
        assertEquals("Value", "8::2", result.getProperty("Die Akteure der Umsetzung"));
        assertEquals("Value", "8", result.getProperty("Der Autor"));
        assertEquals("Value", "", result.getProperty("Dr. <Autor>"));
        assertEquals("Value", "Literatur & Links", result.getProperty("<CCC>"));
        assertEquals("Value", "Literatur & Links::2", result.getProperty("<Titel>"));

    }
}
