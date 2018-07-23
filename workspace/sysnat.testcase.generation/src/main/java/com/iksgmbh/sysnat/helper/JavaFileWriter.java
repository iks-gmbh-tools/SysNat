package com.iksgmbh.sysnat.helper;

import java.io.File;
import java.util.HashMap;

import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

/**
 * Writes generated JUnit test case files to target directory
 * and deletes old 
 * @author rob
 *
 */
public class JavaFileWriter 
{
	public static void writeToTargetDir(final HashMap<File, String> testCodeToExecute) 
	{
		final String targetDir = System.getProperty("sysnat.generation.target.dir");
		File dir = new File(targetDir);
		SysNatFileUtil.deleteFolder(dir);  
		testCodeToExecute.forEach((file, content) -> SysNatFileUtil.writeFile(file, content));
	}

}
