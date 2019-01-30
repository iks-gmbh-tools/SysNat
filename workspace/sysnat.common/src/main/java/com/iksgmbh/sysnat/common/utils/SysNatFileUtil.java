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
package com.iksgmbh.sysnat.common.utils;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.iksgmbh.sysnat.common.domain.FileList;
import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.helper.FileFinder;

public class SysNatFileUtil 
{
	private static File downloadDir = null;
	
	public static void copyTextFileToTargetDir(final String sourceDir, 
			                                   final String sourceFilename,
                                               final String targetDir) 
	{
		final String content = SysNatFileUtil.readTextFileToString(sourceDir + "/" + sourceFilename);
		SysNatFileUtil.writeFile(new File(targetDir, sourceFilename), content);		
	}

	
	public static void writeFile(final String fileName, 
			                     final String fileContent) 
	{
		try {			
			writeFile(new FileOutputStream(fileName), fileContent, fileName);
		} catch (Exception e) {
			String message = "Could not write file " + new File(fileName).getAbsolutePath();
			System.err.println(message);
			e.printStackTrace();
			throw new SysNatException(message);
		}
	}


	public static void writeFile(final File file, 
			                     final String fileContent) 
	{
		if ( ! file.getParentFile().exists() ) 
		{
			file.getParentFile().mkdirs();
			if ( ! file.getParentFile().exists() ) {
				throw new SysNatException("Could not create directory " + file.getParentFile().getAbsolutePath());
			}
		}
		try {			
			writeFile(new FileOutputStream(file), fileContent, file.getAbsolutePath());
		} catch (Exception e) {
			throw new SysNatException("Could not write file " + file.getAbsolutePath());
		}
	}

	public static void writeFile(final FileOutputStream fileOutputStream, 
			                     final String fileContent,
			                     final String fileName) 
	{
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));
				try {
				    writer.write(fileContent);
				} finally {
				    writer.close();
				}
		} catch (Exception e) {
            throw new RuntimeException("Datei konnte nicht gespeichert werden: " + fileName);
		}
		finally 
		{
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
				throw new SysNatException("Download-Verzeichnis nicht verfügbar.");
			}
		}
		return downloadDir;
	}

	public static File findLatest(final FileList currentFileList, 
			                      final long millisAtTestStart) 
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
			throw new SysNatException("Das Erzeugungsdatum der Datei " + f.getAbsolutePath() + " konnte nicht bestimmt werden.");
	    }
	}

	public static void open(File file) 
	{
		if ( ! file.exists()) {
			throw new SysNatException("Das angegebene Datei " + file.getAbsolutePath() + " existiert nicht mehr.");
		}
		if ( ! file.getName().endsWith("pdf")) {
			throw new SysNatException("Das Öffnen der angegebene Datei " + file.getAbsolutePath() + " wird nicht unterstützt.");
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
		if ( ! new File(pathAndfilename).exists()) {
			throw new SysNatException("Cannot read non existing file '" + pathAndfilename + "'.");
		}
		
		final List<String> fileContent = new ArrayList<String>();
		
		try (Stream<String> stream = Files.lines(Paths.get(pathAndfilename))) {
			stream.forEach(line -> fileContent.add(line));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return fileContent;
	}

	public static void loadPropertyFile(String filename, Properties properties) {
		loadPropertyFile(new File(filename), properties);
	}

	public static void loadPropertyFile(File f, Properties properties) 
	{
		try {
			properties.load(new InputStreamReader(new FileInputStream (f), "UTF-8"));
		} catch (FileNotFoundException e) {
			String message = "Die benötigte Date " + f.getAbsolutePath() + " ist nicht vorhanden.";
			System.err.println(message);
			throw new SysNatException(message);
		} catch (Exception e) {
			String message = "Fehler beim Laden von " + f.getAbsolutePath() + ".";
			System.err.println(message);
			throw new SysNatException(message);
		}
	}

	
	public static int getNumberOfFilesStartingWith(String fileNamePrefix, String searchDir) 
	{
		int counter = 0;
		final String[] listFiles = new File(searchDir).list();
		for (String fileName : listFiles) 
		{
			if (fileName.startsWith(fileNamePrefix)) {
				counter++;
			}
		}
		
		return counter;
	}

	public static List<String> readTextFile(final File file) 
	{
		return readTextFile(file.getAbsolutePath());
	}

	public static String readTextFileToString(final String pathAndFilename) 
	{
		return readTextFileToString(new File(pathAndFilename));
	}
	
	public static String readTextFileToString(final File file) 
	{
		final List<String> lines = readTextFile(file.getAbsolutePath());
		final StringBuffer sb = new StringBuffer();
		lines.forEach(l -> sb.append(l).append(System.getProperty("line.separator")));
		return sb.toString().trim();
	}


	public static List<File> findFilesEndingWith(final String fileEnding, 
			                                     final File dirToSearch) 
	{
		return FileFinder.searchFilesRecursively(dirToSearch, new FilenameFilter() {
			@Override public boolean accept(File dir, String name) {
				return name.endsWith(fileEnding);
			}
		});
	}

	/**
	 * @param dir to remove with all files and subdirs
	 * @return true if successful
	 */
	public static boolean deleteFolder(String dir) {
		return deleteFolder(new File(dir));
	}

	/**
	 * @param dir to remove with all files and subdirs
	 * @return true if successful
	 */
	public static boolean deleteFolder(File dir) 
	{
		if (dir.exists() && dir.isDirectory()) 
		{
			final List<File> children = Arrays.asList( dir.listFiles() );
			children.stream().filter(file -> file.isFile()).forEach(file -> file.delete());
			children.stream().filter(file -> file.isDirectory()).forEach(SysNatFileUtil::deleteFolder);
			return dir.delete();
		} else {
			if (! dir.exists()) {
				System.out.println("Nothing to delete. Directory '" + dir.getAbsoluteFile() + "' does not exist.");
			} else {
				System.err.println("Directory to delete '" + dir.getAbsoluteFile() + "' is no directory!");	
			}
		}
		return false;
	}


	public static void copyFolder(File sourceDir, File targetDir) 
	{
		final String sourceFolderName = sourceDir.getName();
		final String targetFolderName = targetDir.getName();
		final int lengthOfRootPath = buildCanonicalPath(sourceDir).length();
		final List<File> result = FileFinder.findFiles(sourceDir, null, null, null, null, null);

		if ( ! sourceFolderName.equals(targetFolderName)) {
			targetDir = new File (targetDir, sourceFolderName);
		}
		
		for (File file : result) 
		{
			String relativePath = buildCanonicalPath(file.getParentFile()).substring(lengthOfRootPath);
			File targetFile = new File(targetDir, relativePath + "/" + file.getName());
			targetFile.getParentFile().mkdirs();
			copyBinaryFile(file, targetFile);
		}
	}
	
	private static String buildCanonicalPath(File f) 
	{
		try {			
			return f.getCanonicalPath();
		} catch (Exception e) {
			throw new RuntimeException("Error building canoical path for " + f.getAbsolutePath());
		}
	}

    public static void copyFileToTargetDir(final String sourceFileAsString, 
                                           final String targetDirAsString) 
    {
    	final File targetDir = new File(targetDirAsString);
    	if ( ! targetDir.exists()) {
    		throw new RuntimeException("Target dir does not exist: " + targetDir.getAbsolutePath());
    	}
    	if ( ! targetDir.isDirectory()) {
    		throw new RuntimeException("Target dir is no directory: " + targetDir.getAbsolutePath());
    	}
    	final File sourceFile = new File(sourceFileAsString);
    	if ( ! sourceFile.exists()) {
    		throw new RuntimeException("Source file does not exist: " + sourceFile.getAbsolutePath());
    	}
    	
    	final File targetFile = new File(targetDir, sourceFile.getName());
    	copyBinaryFile(sourceFile, targetFile);
    }
	
	
    public static void copyBinaryFile(final String fromFileName, 
    		                          final String toFileName) 
    {
           final File fromFile = new File(fromFileName);
           final File toFile = new File(toFileName);
           copyBinaryFile(fromFile, toFile);
    }

    public static void copyBinaryFile(final File fromFile, 
                                      final String toFileName) 
	{
		final File toFile = new File(toFileName);
		copyBinaryFile(fromFile, toFile);
	}
    
	public static void copyBinaryFile(final String fromFileName, 
			                          final File toFile) 
	{
		final File fromFile = new File(fromFileName);
		copyBinaryFile(fromFile, toFile);
	}    
    
    /**
    * Uses streams to perform copy
    * @param fromFile
    * @param toFile
    * @throws IOException
    */
    public static void copyBinaryFile(final File fromFile, File toFile) 
    {
           if (!fromFile.exists())
                   throw new RuntimeException("FileCopy: " + "no such source file: "
                                  + fromFile.getAbsolutePath());
           if (!fromFile.isFile())
                   throw new RuntimeException("FileCopy: " + "can't copy directory: "
                                  + fromFile.getAbsolutePath());
           if (!fromFile.canRead())
                   throw new RuntimeException("FileCopy: " + "source file is unreadable: "
                                  + fromFile.getAbsolutePath());

           if (toFile.isDirectory())
                   toFile = new File(toFile, fromFile.getName());

           if (toFile.exists()) {
                   if (!toFile.canWrite())
                           throw new RuntimeException("FileCopy: "
                                          + "destination file is unwriteable: " + toFile.getAbsolutePath());
           } else {
                   String parent = toFile.getParent();
                   if (parent == null)
                           parent = System.getProperty("user.dir");
                   File dir = new File(parent);
                   if (!dir.exists())
                           throw new RuntimeException("FileCopy: "
                                          + "destination directory doesn't exist: " + parent);
                   if (dir.isFile())
                           throw new RuntimeException("FileCopy: "
                                          + "destination is not a directory: " + parent);
                   if (!dir.canWrite())
                           throw new RuntimeException("FileCopy: "
                                          + "destination directory is unwriteable: " + parent);
           }

           FileInputStream from = null;
           FileOutputStream to = null;
           try {
                   from = new FileInputStream(fromFile);
                   to = new FileOutputStream(toFile);
                   byte[] buffer = new byte[4096];
                   int bytesRead;

                   while ((bytesRead = from.read(buffer)) != -1)
                           to.write(buffer, 0, bytesRead); // write
           } catch (IOException e) {
                   throw new RuntimeException(e);
           } finally {
                   if (from != null)
                           try {
                                  from.close();
                           } catch (IOException e) {
                                  ;
                           }
                   if (to != null)
                           try {
                                  to.close();
                           } catch (IOException e) {
                                  ;
                           }
           }
    }

	public static File createFolder(final String toCreate) 
	{
		final File folder = new File( toCreate );
		boolean ok = folder.mkdirs();
		if ( ! ok ) {
			throw new RuntimeException("Error creating report directory '" + toCreate + "'.");
		}
		return folder;
		
	}

	public static void createZipFile(File directoryToZip, File targetZipFile)
	{
		List<File> filesToZip = FileFinder.findFiles(directoryToZip, null, null, null, ".zip", null);
		String rootZipDir = directoryToZip.getName();
		
        byte[] buffer = new byte[1024];
        try (FileOutputStream fos = new FileOutputStream(targetZipFile);
        	 ZipOutputStream zos = new ZipOutputStream(fos)) 
        {
            for (File file: filesToZip) 
            {
                String filename = file.getAbsolutePath();
                int pos = filename.indexOf(rootZipDir);
                String zipEntryPath = filename.substring(pos+1+rootZipDir.length());
				ZipEntry ze = new ZipEntry(zipEntryPath);
                zos.putNextEntry(ze);
                try (FileInputStream in = new FileInputStream(file);)
                {
                    int len;
                    while ((len = in .read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
        	throw new SysNatException(ex.getMessage());
        } catch (Exception ex) {
            throw new SysNatException("Error zipping directory: " + directoryToZip.getAbsolutePath());
        }
    }	
	
}