package com.iksgmbh.sysnat.helper;

import java.io.File;
import java.io.IOException;

import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

public class InkaClientUpdater
{
	public static final String DefaultSource = "P:\\0311_AIA_Anwendung\\Inka\\Client_Versionen";
	public static final String DefaultTarget = "C:\\InkaClient - Umgebung REL";
	
	public static void main(String[] args)
	{
		System.out.println("InkaClientUpdater bei der Arbeit:");
		try {
			String sourcePath = DefaultSource;
			File sourceFile = new File(sourcePath);
			if (! sourceFile.exists()) {
				System.err.println("Quellverzeichnis " + sourceFile.getCanonicalPath() + " nicht gefunden.");
				return;
			}
			
			String targetPath = DefaultTarget;
			File targetFile = new File(targetPath);
			
			doYourJob(sourceFile, targetFile);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void doYourJob(File sourceFile, File targetFile) throws IOException
	{
		if (targetFile.exists()) 
		{
			boolean ok = SysNatFileUtil.deleteFolder(targetFile);
			if (! ok) {				
				System.err.println("Verzeichnis kann nicht geloescht werden: " + targetFile.getCanonicalPath());
				return;
			} else {
				System.out.println("Zielverzeichnis mit altem InkaClient geloescht.");
			}
		}
		
		String maxVersion = CurrentVersionFinder.doYourJob(sourceFile);
		sourceFile = CurrentVersionFinder.findVersionDir(sourceFile);
		
		if (sourceFile != null && sourceFile.getAbsolutePath().endsWith(" Umgebung REL")) {
			System.out.println("Kopiere neueste InkaClient-Version (" + maxVersion  + ") nach '" + targetFile.getCanonicalPath() + "'...");
			File[] children = sourceFile.listFiles();
			for (int i = 0; i < children.length; i++) 
			{
				if (children[i].isDirectory()) {
					SysNatFileUtil.copyFolder(children[i], targetFile);
				} else {
					SysNatFileUtil.copyFileToTargetDir(children[i], targetFile);
				}
			}
			SysNatFileUtil.writeFile(new File(targetFile, maxVersion + ".txt"), "InkaClientVersion: " + maxVersion);
		} else {
			System.err.println("Kein gueltiges Quellverzeichnis gefunden!");
		}
		
		setSortSAPResultFile();
		
		System.out.println("Fertig.");
	}

	private static void setSortSAPResultFile() throws IOException
	{
		File file = new File(DefaultTarget, "InkaClient.properties");
		String content = SysNatFileUtil.readTextFileToString(file);
		content = content.replace("sap.export.sortEreignisse=false", "sap.export.sortEreignisse=true");
		SysNatFileUtil.writeFile(file, content);
		System.out.println("Die InkaClient-Property 'sap.export.sortEreignisse' wurde auf 'true' gesetzt.");
	}


}
