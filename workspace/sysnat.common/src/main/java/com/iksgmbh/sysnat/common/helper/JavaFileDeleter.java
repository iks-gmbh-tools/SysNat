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