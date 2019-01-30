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

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.iksgmbh.sysnat.common.helper.FileFinder;

public class SysNatFileUtilTest {

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

}