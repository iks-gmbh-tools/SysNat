package com.iksgmbh.sysnat.utils;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import com.iksgmbh.sysnat.ExecutionInfo;
import com.iksgmbh.sysnat.domain.FileList;

public class SysNatFileUtil 
{
	private static File downloadDir = null;
	
	public static void writeFile(String fileName, String fileContent) 
	{
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
				    new FileOutputStream(fileName), "UTF-8"));
				try {
				    writer.write(fileContent);
				} finally {
				    writer.close();
				}
		} catch (Exception e) {
            throw new RuntimeException("Datei konnte nicht gespeichert werden: " + fileName);
		}
		finally {
			if (writer != null)
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public static FileList findFilesIn(String fileExtension, File directory) 
	{
		fileExtension = fileExtension.toLowerCase();
		final FileList toReturn = new FileList();
		final File[] listFiles = directory.listFiles();
		for (File file : listFiles) {
			if (file.getAbsolutePath().toLowerCase().endsWith(fileExtension) && file.isFile())  {
				toReturn.add(file);
			}
		}
		
		return toReturn;
	}
	
	public static FileList findDownloadFiles(String fileExtension) 
	{
		fileExtension = fileExtension.toLowerCase();
		final File downloadDir = getDownloadDir();
		final FileList toReturn = new FileList();
		final File[] listFiles = downloadDir.listFiles();
		
		for (File file : listFiles) {
			if (file.getAbsolutePath().toLowerCase().endsWith(fileExtension) && file.isFile())  {
				toReturn.add(file);
			}
		}
		
		return toReturn;
	}

	public static File getDownloadDir() 
	{
		if (downloadDir == null)
		{
			downloadDir = new File(System.getProperty("user.home") + "/Downloads");
			if ( ! downloadDir.exists())  {
				throw new RuntimeException("Download-Verzeichnis nicht verfügbar.");
			}
		}
		return downloadDir;
	}

	public static File findLatest(FileList currentFileList) 
	{
		if (currentFileList == null  || currentFileList.size() == 0 ) {
			return null;
		}
		
		final List<File> list = currentFileList.getFiles();
		File toReturn = list.get(0);
		long maxValue = getCreationTimeAsMillis(toReturn);
		
		for (File file : list) 
		{
			long millisToCompare = getCreationTimeAsMillis(file);
			if (millisToCompare > maxValue) {
				toReturn = file;
				maxValue = millisToCompare;
			}
		}
		
		long millisAtTestStart = ExecutionInfo.getInstance().getStartPointOfTime().getTime();
		
		if (millisAtTestStart > maxValue) {
			return null;
		}
		
		return toReturn;
	}
	
	public static long getCreationTimeAsMillis(File f)
	{
		try {
			Path path = Paths.get(f.getCanonicalPath());
			BasicFileAttributes attr;
		    attr = Files.readAttributes(path, BasicFileAttributes.class);
		    return attr.creationTime().toMillis();
		} catch (IOException e) {
	        e.printStackTrace();
	        throw new RuntimeException("Das Erzeugungsdatum der Datei " + f.getAbsolutePath() + " konnte nicht bestimmt werden.");
	    }
	}

	public static void open(File file) 
	{
		if ( ! file.exists()) {
			throw new RuntimeException("Das angegebene Datei " + file.getAbsolutePath() + " existiert nicht mehr.");
		}
		if ( ! file.getName().endsWith("pdf")) {
			throw new RuntimeException("Das Öffnen der angegebene Datei " + file.getAbsolutePath() + " wird nicht unterstützt.");
		}
		  try {
			Desktop.getDesktop().open(file);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Das Öffnen der Datei " + file.getAbsolutePath() + " schlug fehl.");
		}
	}

	public static void waitUntilNumberOfPdfFilesIs(int expectedFileNumber)  
	{
		List<File> files;
		boolean waitAgain = true;
		boolean partFileFound = false;
		
		while (waitAgain) 
		{
			files = findDownloadFiles("pdf").getFiles();
			for (File file : files) {
				if (file.getName().endsWith("part"))  {
					partFileFound = true;
				}
			}
		
			if (partFileFound) {
				try {
					Thread.sleep(100);  // wait until part file is gone
				} catch (InterruptedException e) {
					e.printStackTrace();
					waitAgain = false;
				}
			} else {
				waitAgain = files.size() != expectedFileNumber;
			}
		}
	}

	public static void appendTo(File csvfile, String line) 
	{
		line += System.getProperty("line.separator");
		Path path = Paths.get(csvfile.getAbsolutePath());
		try {
		    Files.write(path, line.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
		    System.err.println(e);
		}	
	}

	public static List<String> readTextFile(String pathAndfilename) 
	{
		final List<String> fileContent = new ArrayList<String>();
		
		try (Stream<String> stream = Files.lines(Paths.get(pathAndfilename))) {
			stream.forEach(line -> fileContent.add(line));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return fileContent;
	}
	
	public static void loadPropertyFile(File f, Properties properties) 
	{
		try {
			properties.load(new InputStreamReader(new FileInputStream (f), "UTF-8"));
		} catch (Exception e) {
			throw new RuntimeException("Fehler beim Laden von " + f.getAbsolutePath() + ".", e);
		}
	}

	public static int getNumberOfFilesStartingWith(String fileNamePrefix) 
	{
		int counter = 0;
		final String[] listFiles = new File(ExecutionInfo.getInstance().getScreenShotDir()).list();
		for (String fileName : listFiles) 
		{
			if (fileName.startsWith(fileNamePrefix)) {
				counter++;
			}
		}
		
		return counter;
	}

	public static String getSysNatDir() 
	{
		final File f = new File("../../..", "Test");
		
		String canonicalPath;
		try {
			canonicalPath = f.getParentFile().getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		
		
		return canonicalPath;
	}

	public static List<String> readTextFile(final File file) 
	{
		return readTextFile(file.getAbsolutePath());
	}

	public static String readTextFileToString(final File file) 
	{
		final List<String> lines = readTextFile(file.getAbsolutePath());
		final StringBuffer sb = new StringBuffer();
		lines.forEach(l -> sb.append(l).append(System.getProperty("line.separator")));
		return sb.toString().trim();
	}
	
}
