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
package com.iksgmbh.sysnat.common.helper;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Utility, to delete all Java files in the natspec directory
 * 
 * @author  Reik Oberrath
 */
public class JavaFileDeleter extends FileFinder
{
	public static void main(String[] args) throws IOException 
	{
		System.out.println("");
		
		File folder = new File("../natspec");
		if ( ! folder.exists()) {
			folder = new File("natspec");
		}
		
		int counter = 0;
		final List<File> toDelete = findFiles(folder, null, null, ".java", null);
		
		for (File javaFile : toDelete) {
			if ( ! javaFile.getName().equals("_NatSpecTemplate.java") )  {
				javaFile.delete();
				counter++;
			}
		}
		
		if (counter == 0) {
			System.out.println("Es wurden keine Java-Files aus " + folder.getCanonicalPath() + " entfernt." );		
		} else {
			System.out.println("Es wurden " + counter + " Java-Files aus " + folder.getCanonicalPath() + " entfernt." );		
		}
	}
}