package com.iksgmbh.sysnat.common.helper;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

public class TextInFileReplacerClassLevelTest 
{
	@Test
	public void returnsMessageForNoFilesFound() 
	{
		// arrange
		final String toSearch = "aNonExistingText";
		final String replacement = "aReplacement";
		final String targetDir = "./nonExistingFolder";

		// act
		String result = TextInFileReplacer.doYourJob(targetDir, toSearch, replacement);

		// assert
		assertEquals("Result message", "Es wurde in './nonExistingFolder' keine Dateien gefunden.", result);
	}

	@Test
	public void doesNoReplacementForNoMatch() 
	{
		// arrange
		final String toSearch = "aNonExistingText";
		final String replacement = "aReplacement";
		final String targetDir = "../sysnat.common/src/test/resources/searchReplaceFiles";

		// act
		String result = TextInFileReplacer.doYourJob(targetDir, toSearch, replacement);

		// assert
		assertEquals("Result message", "Es wurden in keine der 3 gefundenen Dateien der Text 'aNonExistingText' "
				+ "gefunden und daher keine Ersetzungen durchgeführt.", result);
	}

	@Test
	public void replacesAllMatchesOfAStringInAllFilesOfTargetDirectoryRecursiverly() 
	{
		// arrange
		final String toSearch = "directory/subdirectory";
		final String replacement = "folder/subfolder/subsubfolder";
		final String targetDir = "../sysnat.common/src/test/resources/searchReplaceFiles";
		assureSearchStringIsContainedInTargetFiles(targetDir, toSearch, replacement);

		// act
		String result = TextInFileReplacer.doYourJob(targetDir, toSearch, replacement);

		// cleanup
		TextInFileReplacer.doYourJob(targetDir, replacement, toSearch);

		// assert
		assertEquals("Result message", "Es wurden in 3 von insgesamt 3 gefundenen Dateien insgesamt "
				+ "12 Ersetzungen des Textes 'directory/subdirectory' durchgeführt.", result);
	}

	private void assureSearchStringIsContainedInTargetFiles(final String targetDir, 
			                                                final String toSearch,
			                                                final String replacement) 
	{
		File dir = new File(targetDir);
		File[] files = dir.listFiles();
		File textFile = files[0];
		if (textFile.isDirectory()) {
			textFile = files[1];
			if (textFile.isDirectory()) {
				throw new RuntimeException("Expected text file not found.");
			}
		}

		String fileContent = SysNatFileUtil.readTextFileToString(textFile);
		if (fileContent.contains(replacement)) {
			TextInFileReplacer.doYourJob(targetDir, replacement, toSearch);
		}
	}
}