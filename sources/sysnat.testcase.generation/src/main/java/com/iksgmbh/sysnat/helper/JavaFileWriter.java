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