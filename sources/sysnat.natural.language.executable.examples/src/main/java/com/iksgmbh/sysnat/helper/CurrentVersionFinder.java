package com.iksgmbh.sysnat.helper;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CurrentVersionFinder
{
	private static final String PREFIX = "\\InkaClient-";
	private static final String SUFFIX = " - Umgebung REL";

	public static String doYourJob(File sourceFile) 
	{
		List<File> subdirs = findSubDirs(sourceFile, PREFIX, SUFFIX);
		List<String> versions = subdirs.stream().map(f -> f.getAbsolutePath())
                                                .map(s -> s.replace(sourceFile.getAbsolutePath(), ""))
				                                .map(s -> s.replace(PREFIX, ""))
				                                .map(s -> s.replace(SUFFIX, ""))
				                                .collect(Collectors.toList());
		return findMaxVersion(versions);
	}

	private static List<File> findSubDirs(File sourceFile, String prefix, String suffix)
	{
		List<File> subdirs = Arrays.asList(sourceFile.listFiles()).stream().filter(f -> f.isDirectory())
																		   .filter(f -> f.getAbsolutePath().contains(prefix))
				                                                           .filter(f -> f.getAbsolutePath().endsWith(suffix))
				                                                           .collect(Collectors.toList());
		return subdirs;
	}

	public static File findVersionDir(File sourceFile)
	{
		String maxVersion = doYourJob(sourceFile);
		if (maxVersion == null) {
			return null;
		}
		
		List<File> subdirs = findSubDirs(sourceFile, PREFIX, SUFFIX);
		return subdirs.stream().filter(f -> f.getAbsolutePath().contains(maxVersion)).findFirst().orElse(null);
	}
	
	private static String findMaxVersion(List<String> versions)
	{
		int position = 1;
		while (versions.size() > 1) {
			versions = findMaxMainVersions(versions, position);
			position++;
		}
		
		if (versions.isEmpty()) {
			return null;
		}
		return versions.get(0);
	}

	private static List<String> findMaxMainVersions(List<String> versions, int position)
	{

		int maxVersionNumber = extractVersionNumber(versions.get(0), position);
		for (String v : versions) 
		{
			int versionNumber = extractVersionNumber(v, position);
			if (versionNumber > maxVersionNumber) {
				maxVersionNumber = versionNumber;
			}
		}
		
		final int vNo =  maxVersionNumber;
		return versions.stream().filter(v -> extractVersionNumber(v, position) == vNo).collect(Collectors.toList());
	}

	private static int extractVersionNumber(String version, int position)
	{
		if (! version.contains(".")) {
			throw new RuntimeException("Error: cannot parse version");	
		}
		
		String toReturn = version;
		int positionCounter = 1;
		while (positionCounter < position) {
			int pos = toReturn.indexOf(".");
			if (pos == -1) {
				return 0;  // subversion not available at this position -> Take Default
			}
			toReturn = toReturn.substring(pos+1); 
			positionCounter++;
		} 
		
		if (toReturn.contains(".")) {
			int pos = toReturn.indexOf(".");
			toReturn = toReturn.substring(0,pos); 
		}
		
		return Integer.valueOf(toReturn);
	}
	
}
