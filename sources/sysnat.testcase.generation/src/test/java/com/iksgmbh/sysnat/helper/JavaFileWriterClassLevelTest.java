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
package com.iksgmbh.sysnat.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.GenerationRuntimeInfo;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

public class JavaFileWriterClassLevelTest 
{
	@Before
	public void setup() {
		GenerationRuntimeInfo.reset();
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.generation.target.dir", "target/testTargetDir");
		ExecutionRuntimeInfo.setSysNatSystemProperty("settings.config", "../sysnat.test.runtime.environment/src/test/resources/testSettingConfigs/settingsHomePageIKS.config");
		GenerationRuntimeInfo.getInstance();
	}
	
	@Test
	public void writesFiles() 
	{
		// arrange
		final String testApp = "TestApp";
		final HashMap<File, String> toSave = new HashMap<>();
		final File file1 = createFile("TestFile1.java", testApp);
		final String fileContent1 = "Content of File 1";
		toSave.put(file1, fileContent1);
		final File file2 = createFile("TestFile2.java", testApp);
		final String fileContent2 = "Content of File 2";
		toSave.put(file2, fileContent2);

		// act
		JavaFileWriter.writeToTargetDir(toSave);
		
		// arrange
		assertTrue("Missing file " + file1.getAbsolutePath(), file1.exists());
		assertEquals("File Content", fileContent1, SysNatFileUtil.readTextFileToString(file1));
		assertTrue("Missing file " + file2.getAbsolutePath(), file1.exists());
		assertEquals("File Content", fileContent2, SysNatFileUtil.readTextFileToString(file2));
	}

	@Test
	public void deletesTargetDirBeforeWritingNewFiles() throws IOException 
	{
		// arrange 1: run a test
		final String testApp = "TestApp";
		final HashMap<File, String> toSave = new HashMap<>();
		final File file1 = createFile("TestFile1.java", testApp);
		toSave.put(file1, "test content");
		JavaFileWriter.writeToTargetDir(toSave);
		assertTrue("Missing file " + file1.getAbsolutePath(), file1.exists());
		
		// arrange 2: run a second test
		toSave.clear();
		final File file2 = createFile("TestFile2.java", testApp);
		toSave.put(file2, "test content");
		
		// act
		JavaFileWriter.writeToTargetDir(toSave);
		
		// assert
		assertFalse("First run-results were not deleted!" + file1.getAbsolutePath(), file1.exists());
		assertTrue("Missing file " + file2.getAbsolutePath(), file2.exists());
	}
	
	private File createFile(String filename, String parent) 
	{
		final String targetDir = System.getProperty("sysnat.generation.target.dir");
		final File toReturn = new File(targetDir + "/" + parent, filename);
		toReturn.delete();
		assertFalse(toReturn.exists());	
		return toReturn;
	}
	
}