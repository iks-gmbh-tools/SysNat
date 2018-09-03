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
package com.iksgmbh.sysnat.testdataimport;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.common.helper.FileFinder;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;

/**
 * Helper to load test data from dat-file or from Excel file.
 * 
 * @author Reik Oberrath
 */
public class TestDataImporter 
{
	public static final String FACTORY_PACKAGE = "com.iksgmbh.helloworldspringboot.greeting.factory";

	protected static final char[] CHARS_TO_CUT_FROM_DATA_FILE_NAME = 
		{'0','1','2','3','4','5','6','7','8','9','0','-','_', '.' };
	
	String testdataDir;
	String testdataId;
	
	/**
	 * @param testdataId path to testdata dir 
	 *                   e.g. ../sysnat.natural.language.executable.examples/testdata/HelloWorldSpringBoot
	 */
	public TestDataImporter(final String aTestdataDir) {
		this.testdataDir = aTestdataDir;
	}

	/**
	 * Loads data from file(s). Data of each dataset found are
	 * stored as key-value pairs of a Properties class instance.
	 * 
	 * @param testdataId either filename without extension
	 *                   or name of subdirectory
	 * @return test data as a list of key-value pairs
	 */
	public Hashtable<String, Properties> loadTestdata(final String aTestdataId)  
	{
		this.testdataId = aTestdataId;
		final Hashtable<String, Properties> testdata = new Hashtable<>();
		
		final List<File> filesToLoad = findFilesToLoad();
		
		filesToLoad.forEach(file -> addToTestdata(file, testdata));
		
		return testdata;
	}

	List<File> findFilesToLoad() 
	{
		final List<File> toReturn = FileFinder.searchFilesRecursively(new File(testdataDir), new FilenameFilter() 
		{
			@Override 
			public boolean accept(File dir, String filename) 
			{
				if (! filename.endsWith(".dat") && ! filename.endsWith(".xlsx")) 
					return false;
				
				filename = SysNatStringUtil.cutExtension(filename);
				filename = SysNatStringUtil.cutTrailingChars(filename, CHARS_TO_CUT_FROM_DATA_FILE_NAME);
				
				if (filename.equals(testdataId))
					return true;
				
				int pos = testdataId.indexOf('_');
				if (pos == -1) 
					return false;
				
				String filenameCandidate = testdataId.substring(0, pos);
				
				return filenameCandidate.equals(filename);
			}
		});
		
		if (toReturn.size() == 0) {
			throw new SysNatTestDataException("Zu '" + testdataId + "' wurden in " + testdataDir + " keine Testdaten-Dateien gefunden.");
		}
		
		return toReturn;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addToTestdata(final File file, 
			                   final Hashtable<String, Properties> testdata) 
	{
		final Hashtable<String, Properties> dataSetsFromFile = loadDatasetsFromFile(file);
		final List<String> datasetNames = new ArrayList(dataSetsFromFile.keySet());
		//datasetNames.forEach(System.err::println);
		
		// Add only those datasets that match the testdataId read from the nlxx file!
		datasetNames.stream().filter(name -> name.equals(testdataId) || name.startsWith(testdataId))
		                     .forEach(name -> testdata.put(name, dataSetsFromFile.get(name)));
	}

	private Hashtable<String, Properties> loadDatasetsFromFile(final File file) 
	{
		if (file.getName().endsWith(".dat")) 
		{
			final Hashtable<String, Properties> toReturn = new Hashtable<>();
			final List<Properties> loadedDataSets = DatFileReader.doYourJob(file);
			
			if (loadedDataSets.size() == 1) {
				String dataSetName = SysNatStringUtil.cutExtension(file.getName());
				toReturn.put(dataSetName, loadedDataSets.get(0));
			}
			else
			{				
				int counter = 0;
				for (Properties dataset : loadedDataSets) {
					counter++;
					String dataSetName = SysNatStringUtil.cutExtension(file.getName()) + "_" + counter;
					toReturn.put(dataSetName, dataset);
				}
			}
			
			return toReturn;
		}
		
		return loadDatasetsFromExcelFile(file);
	}
	

	private Hashtable<String, Properties> loadDatasetsFromExcelFile(final File excelFile) 
	{
		return ExcelDataProvider.doYourJob(excelFile);
	}

	
}