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
		List<File> filesInExpTarget = FileFinder.findFiles(expectedTargetDir, null, null, null, null);
		assertEquals("Number of files", 4, filesInExpTarget.size());
	}

}
