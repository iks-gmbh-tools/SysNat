package com.iksgmbh.sysnat.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.GenerationRuntimeInfo;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

public class JavaFileWriterClassLevelTest 
{
	@Before
	public void setup() {
		GenerationRuntimeInfo.reset();
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.generation.target.dir", "target/testTargetDir");
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
