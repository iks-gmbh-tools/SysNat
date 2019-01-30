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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility, to find certain files in specific subdirectories within a given main folder.
 * 
 * @author  Reik Oberrath
 */
public class FileFinder 
{
	private static final FilenameFilter DIRECTORY_FILTER = new FilenameFilter() {
		@Override public boolean accept(File dir, String name) {
			return new File(dir, name).isDirectory();
		}
	};

	public static List<File> findFiles(final File mainFolder, 
			                           final List<String> subdirsToSearch,
                                       final List<String> subdirsToIgnore,
			                           final String fileExtensionToSearch,
			                           final String fileExtensionToIgnore,
			                           final String partOfFileName) 
	{
		if (mainFolder == null || mainFolder.isFile() || ! mainFolder.exists())
		{
			throw new RuntimeException("Mainfolder is not valid!");
		}
		return searchFilesRecursively(mainFolder, new CustomizedFileFilter(subdirsToSearch, 
				                                                           subdirsToIgnore, 
				                                                           fileExtensionToSearch, 
				                                                           fileExtensionToIgnore,
				                                                           partOfFileName));
	}	
	

	public static List<File> searchFilesRecursively(final String folder, 
 		                                            final String fileExtension)
	{
		return searchFilesRecursively(new File(folder), new FilenameFilter() {
			@Override public boolean accept(File dir, String name) {
				return name.endsWith(fileExtension);
			}
		});
	}
	
	public static List<File> searchFilesRecursively(final File folder, 
			                                        final String fileExtension) 
	{
		return searchFilesRecursively(folder, new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(fileExtension);
			}
		});
	}
	
	public static List<File> searchFilesRecursively(final File folder, 
			                             		    final FilenameFilter fileFilter)
	{
		final List<File> toReturn = new ArrayList<File>();
		
		if (! folder.exists()) {
			System.err.println("Folder does not exist: " + folder.getAbsolutePath());
			return toReturn;
		}
		
		final List<File> foundChildren = new ArrayList<File>();
		foundChildren.addAll(Arrays.asList(folder.listFiles(fileFilter)));
		
		if (fileFilter instanceof CustomizedFileFilter) {
			// do nothing
		} else {
			foundChildren.addAll(Arrays.asList( folder.listFiles(DIRECTORY_FILTER) ));
		}
		
		for (File child : foundChildren) 
		{
			if ( child.isDirectory() )
			{
				toReturn.addAll( searchFilesRecursively(child, fileFilter) );					
			}
			else
			{
				toReturn.add(child);
			}
		}
		return toReturn;
	}
	
	
	public static class CustomizedFileFilter implements FilenameFilter
	{
		private List<String> subdirsToSearch;
		private List<String> subdirsToIgnore;
		private String fileExtensionToSearch;
		private String fileExtensionToIgnore;
		private String partOfFileName;
		
		public CustomizedFileFilter(final List<String> subdirsToSearch,
									final List<String> subdirsToIgnore, 
									final String aFileExtensionToSearch, 
									final String aFileExtensionToIgnore, 
									final String aPartOfFileName) 
		{
			this.subdirsToSearch = unifyPaths(subdirsToSearch);
			this.subdirsToIgnore = unifyPaths(subdirsToIgnore);
			this.partOfFileName = aPartOfFileName;
			
			this.fileExtensionToSearch = aFileExtensionToSearch;
			if (fileExtensionToSearch != null && ! fileExtensionToSearch.startsWith("."))
			{
				this.fileExtensionToSearch = "." + fileExtensionToSearch;
			}

			this.fileExtensionToIgnore = aFileExtensionToIgnore;
			if (fileExtensionToIgnore != null && ! fileExtensionToIgnore.startsWith("."))
			{
				this.fileExtensionToIgnore = "." + fileExtensionToIgnore;
			}
		}
		
		@Override
		public boolean accept(File dir, String name) 
		{

			final File file = new File(dir, name);
			
			if (file.isDirectory())  {
				return true;
			}

			if (subdirsToSearch != null) {
				boolean isSubdirToSearch = false;
				for (String subdir : subdirsToSearch) 
				{
					if (getUnifiedPath(dir).contains(subdir)) 
						isSubdirToSearch = true;
				}
				
				if ( ! isSubdirToSearch ) {
					 return false;
				}
			}
			
			if (subdirsToIgnore != null) {
				boolean isSubdirToSearch = true;
				for (String subdir : subdirsToIgnore) 
				{
					if (getUnifiedPath(dir).contains(subdir)) 
						isSubdirToSearch = false;
				}
				
				if ( ! isSubdirToSearch ) {
					 return false;
				}
			}
	
			if (fileExtensionToSearch != null && ! name.endsWith(fileExtensionToSearch))  {				
				return false;
			}
			
			if (fileExtensionToIgnore != null && name.endsWith(fileExtensionToIgnore))  {				
				return false;
			}
			
			if (partOfFileName != null && ! name.contains(partOfFileName))  {				
				return false;
			}

			return true;
		}
		
		private String getUnifiedPath(File dir) {
			String path = dir.getAbsolutePath();
			return getUnifiedPath(path);
		}
		
		/**
		 * Transforms possibly occurring Windows specific path separators into uniquely valid path separators.   
		 */
		private String getUnifiedPath(final String path) {
			return path.replace("\\", "/");   //apache.common.StringUtils.replace(path, "\\", "/");
		}
        
		private List<String> unifyPaths(final List<String> pathList)
		{
			if (pathList == null) return null;
			
			final List<String> toReturn = new ArrayList<String>();
			
			for (String path : pathList)
			{
				toReturn.add( getUnifiedPath(path) );
			}
			
			return toReturn;
		}

		
	}
			
}