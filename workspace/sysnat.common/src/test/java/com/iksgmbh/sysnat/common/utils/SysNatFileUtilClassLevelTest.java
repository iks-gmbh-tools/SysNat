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
package com.iksgmbh.sysnat.common.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.common.helper.FileFinder;

public class SysNatFileUtilClassLevelTest 
{

	@Test
	public void findsRecentDocumentInDownloadDir() throws IOException 
	{
		// arrange
		final File downloadDir = SysNatFileUtil.getDownloadDir();
		final File downloadedFile = new File(downloadDir, "DownloadSimulation.pdf");
		downloadedFile.delete();
		assertFalse(downloadedFile.exists());
		downloadedFile.createNewFile();

		// act
		File result = SysNatFileUtil.findRecentDownloadFile(100);

		// assert
		assertTrue(result.exists());

		// cleanup
		downloadedFile.delete();
	}

	@Test
	public void throwsExceptionForNonRecentDocumentInDownloadDir() throws Exception 
	{
		// arrange
		SysNatFileUtil.MAX_SECONDS_TO_WAIT_FOR_DOWNLOAD_TO_START = 1;
		final File downloadDir = SysNatFileUtil.getDownloadDir();
		final File downloadedFile = new File(downloadDir, "DownloadSimulation.pdf");
		downloadedFile.delete();
		assertFalse(downloadedFile.exists());
		downloadedFile.createNewFile();
		Thread.sleep(200);
		
		try {			
			// act
			SysNatFileUtil.findRecentDownloadFile(100);
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("Error message", "Problem: Download stopped to continue.", e.getMessage());
		}
		
		// cleanup
		downloadedFile.delete();
	}

	
	@Test
	public void copiesFolderWithAllSubfoldersAndFiles() 
	{
		// arrange
		final File sourceDir = new File("../sysnat.common/src/test/resources/testTestResult");
		final File targetDir = new File("../sysnat.common/target");
		final File expectedTargetDir = new File(targetDir, "testTestResult");
		SysNatFileUtil.deleteFolder(expectedTargetDir);
		assertFalse(expectedTargetDir.exists());

		// act
		SysNatFileUtil.copyFolder(sourceDir, targetDir);

		// assert
		assertTrue(expectedTargetDir.exists());
		List<File> filesInExpTarget = FileFinder.findFiles(expectedTargetDir, null, null, null, null, null);
		assertEquals("Number of files", 4, filesInExpTarget.size());
	}

	
	@Test
	public void loadsPropertyFile() 
	{
		// arrange
		final File propertiesFile = new File("../sysnat.common/src/test/resources/test.properties");
		final Properties properties = new Properties();
		
		// act
		SysNatFileUtil.loadPropertyFile(propertiesFile, properties);

		// assert
		assertEquals("Number of properties", 4, properties.size());
		assertEquals("Property value", "v4", properties.get("p4"));
	}

	@Test
	public void throwsExceptionForUnreadablePropertyLine() 
	{
		// arrange
		final File propertiesFile = new File("../sysnat.common/src/test/resources/test.err.properties");
		final Properties properties = new Properties();
		
		try {			
			// act
			SysNatFileUtil.loadPropertyFile(propertiesFile, properties);
			fail("Expected exception not thrown!");
		} catch (SysNatTestDataException e) {
			// assert
			assertTrue("Unexpected error message", e.getMessage().endsWith("no valid property line."));
		}
	}
	
}