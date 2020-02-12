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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
* Release Helper: Replaces all version strings in pom files.
*/
public class VersionReplacer
{
    private static final String PATH_TO_SOURCES = "..";
    private static final String VERSION_TO_SET = "1.1.0";
    private static final String VERSION_LINE_START_IDENTIFIER = "<version>";
    private static final String VERSION_LINE_END_IDENTIFIER = "</version>";
    private static final String SYSNAT_SECTION_START_IDENTIFIER = "<groupId>com.iksgmbh.snt</groupId>";
    private static final String DEPENDENCY_SECTION_END_IDENTIFIER = "</dependency>";
    private static final String PARENT_SECTION_END_IDENTIFIER = "</parent>";
    private static final String PACKAGING_SECTION_END_IDENTIFIER = "</packaging>";
    private static final String DESCRIPTION_SECTION_END_IDENTIFIER = "</description>";

    public static void main(String[] args)
    {
        try {
        	System.out.println(new File(PATH_TO_SOURCES).getAbsolutePath());
        	List<File> sysNatPomFiles = FileFinder.searchFilesRecursively(new File(PATH_TO_SOURCES), new FilenameFilter() 
			{
				@Override
				public boolean accept(File dir, String name) {
					if (new File(dir, name).isDirectory())
						return true;
					
					if (name.equals("pom.xml")) {
						return true;
					}
					return false;
				}
			});
            
        	VersionReplacer.doYourJob(sysNatPomFiles);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void doYourJob(List<File> sysNatPomFiles) throws IOException
    {
        final VersionReplacer versionReplacer = new VersionReplacer();
        File pomFile;

        System.out.println("");
        System.out.println("VersionReplacer:");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("Replacing current version strings in pom files of all " + sysNatPomFiles.size() 
                           + " SysNat modules with value '" + VERSION_TO_SET + "'.");
        System.out.println("");

        for (int i = 0; i < sysNatPomFiles.size(); i++) {
            pomFile = sysNatPomFiles.get(i);
            System.out.println("Replacing version in " + pomFile.getCanonicalPath() + "...");
            versionReplacer.replaceVersionInPom(pomFile, VERSION_TO_SET);
        }

        System.out.println("");
        System.out.println("Done.");
      System.out.println("-----------------------------------------------------------------------------------------------");
      System.out.println("");
   }

    private List<String> readFileContent(final File file) throws IOException
    {
      final List<String> fileContent = new ArrayList<String>();
        final FileInputStream fis = new FileInputStream(file);
        final BufferedReader br = new BufferedReader(new InputStreamReader(fis));

        String line = null;
        while ((line = br.readLine()) != null) {
            fileContent.add(line);
        }

        br.close();
      return fileContent;
   }

 
    private void replaceVersionInPom(final File pomFile,
                                     final String versionToSet)
    {
        final List<String> fileContent;

        try {
            fileContent = readFileContent(pomFile);
        } catch (Exception e) {
            throw new RuntimeException("Not found: " + pomFile.getAbsolutePath(), e);
        }

        final List<Integer> versionLineIndex = findIndicesOfVersionLines(fileContent, pomFile.getAbsolutePath());
        versionLineIndex.forEach(lineIndex -> replaceVersionInLine(fileContent, lineIndex, versionToSet));

      try {
    	 //fileContent.forEach(System.out::println); 
         createNewFileWithContent(pomFile, fileContent);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

    private void replaceVersionInLine(List<String> fileContent, Integer lineIndex, String versionToSet)
	{
        final String leadingSpace = getLeadingSpace(fileContent.get(lineIndex));
        final String newVersionLine = buildNewVersionLine(versionToSet, leadingSpace);
        fileContent.set(lineIndex, newVersionLine);
	}

	private String getLeadingSpace(final String s)
    {
		int pos = s.indexOf("<");
        return s.substring(0, pos);
    }
    private List<Integer> findIndicesOfVersionLines(final List<String> fileContent,
                                                    final String absolutePath)
    {
    	List<Integer> toReturn = new ArrayList<>();
        int index = -1;
        boolean inSysNatVersionSection = false;  

        for (String line : fileContent)
        {
            index++;

            if (line.trim().startsWith(SYSNAT_SECTION_START_IDENTIFIER)) {
            	inSysNatVersionSection = true;
            }
            if (line.trim().endsWith(DEPENDENCY_SECTION_END_IDENTIFIER)
            	|| line.trim().endsWith(PARENT_SECTION_END_IDENTIFIER)
            	|| line.trim().endsWith(PACKAGING_SECTION_END_IDENTIFIER)
            	|| line.trim().endsWith(DESCRIPTION_SECTION_END_IDENTIFIER)) 
            {
            	inSysNatVersionSection = false;
            }
            
            if (line.trim().startsWith(VERSION_LINE_START_IDENTIFIER) 
            	&& line.trim().endsWith(VERSION_LINE_END_IDENTIFIER)
            	&& inSysNatVersionSection) 
            {
            	toReturn.add(index);
            }
        }

        if (toReturn.size() == 0) {
        	throw new RuntimeException("No version line not found in " + absolutePath);
        }
        
        return toReturn;
    }

    private String buildNewVersionLine(final String versionToSet,
                                       final String leadingSpace)
    {
        return leadingSpace + VERSION_LINE_START_IDENTIFIER + versionToSet + VERSION_LINE_END_IDENTIFIER;
    }

    private void createNewFileWithContent(final File file,
                                          final List<String> fileContent) throws IOException
    {
        final BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        for (String line : fileContent) {
            writer.write(line);
            writer.write(System.getProperty("line.separator"));
        }
        writer.close();
    }

}
