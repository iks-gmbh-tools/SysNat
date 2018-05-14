package com.iksgmbh.sysnat.helper;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.iksgmbh.sysnat.utils.SysNatUtil;

/**
 * Utility, to delete all png files in the screenshot dir.
 * 
 * @author  Reik Oberrath
 */
public class ScreenshotDeleter extends FileFinder
{
	public static void main(String[] args) throws IOException 
	{
		System.out.println("");
		final File folder = new File( SysNatUtil.getScreenshotDir() );
		
		int counter = 0;
		List<File> toDelete = findFiles(folder, null, null, ".png", null);
		if (toDelete.isEmpty()) {
			System.out.println("Es wurden keine png-Dateien in " + folder.getCanonicalPath() + " gefunden." );
		} else {			
			for (File pdfFile : toDelete) {
				pdfFile.delete();
				counter++;
			}
			System.out.println("Es wurden " + counter + " Screenshots aus " + folder.getCanonicalPath() + " entfernt." );
		}
		}
}