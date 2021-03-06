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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.common.helper.ErrorPageLauncher;
import com.iksgmbh.sysnat.common.helper.FileFinder;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.testdataimport.domain.DocumentValidationRule;


/**
 * Helper to load test data from dat-file or from Excel file.
 * 
 * @author Reik Oberrath
 */
public class TestDataImporter 
{
	public static final String DATA_SET_EXCEL_ID = "EXCEL_ID";
	public static final String TESTDATA_LOCATION_SEPARATOR = "::";
	public static final String EXCEL = ".xlsx";
	public static final String DOC_VAL = ".nldocval";
	
	private static final ResourceBundle ERR_MSG_BUNDLE = ResourceBundle.getBundle("bundles/ErrorMessages", Locale.getDefault());

	protected static final char[] CHARS_TO_CUT_FROM_DATA_FILE_NAME = 
		{'0','1','2','3','4','5','6','7','8','9','0','-','_', '.' };
	
	String testdataDir;
	String testdataId;

	private int validationRuleCounter;
	
	private List<File> lastFilesLoaded;

	/**
	 * @param aTestdataDir path to testdata dir
	 *                   e.g. ../sysnat.natural.language.executable.examples/testdata/HelloWorldSpringBoot
	 */
	public TestDataImporter(final String aTestdataDir) {
		this.testdataDir = aTestdataDir;
	}

	/**
	 * Loads data from file(s). Data of each dataset found are
	 * stored as key-value pairs of a Properties class instance.
	 * 
	 * @param aTestdataId either filename without extension
	 *                   or name of subdirectory
	 * @return test data as a list of key-value pairs
	 */
	public Hashtable<String, Properties> loadTestdata(final String aTestdataId)  
	{
		this.testdataId = aTestdataId;
		final Hashtable<String, Properties> testdata = new Hashtable<>();
		
		final List<File> filesToLoad = findFilesToLoad();
		
		filesToLoad.forEach(file -> addToTestdata(file, testdata));
		
		lastFilesLoaded = filesToLoad;
		
		return testdata;
	}

	List<File> findFilesToLoad() 
	{
		String dir = SysNatFileUtil.findAbsoluteFilePath(testdataDir);
		File targetDir = new File(dir);

		if (! targetDir.exists()) {
			throw new SysNatTestDataException("Das Testdatenverzeichnis '" + testdataDir + "' existiert nicht.");
		}

		final String filenameToSearch;
		if (testdataId.contains( TESTDATA_LOCATION_SEPARATOR )) {
			filenameToSearch = extractFilename(testdataId);
		} else {
			filenameToSearch = testdataId;
		}

		final List<File> toReturn = FileFinder.searchFilesRecursively(targetDir, new FilenameFilter()
		{
			@Override 
			public boolean accept(File dir, String filename) 
			{
				if (! filename.endsWith(".dat") && ! filename.endsWith(".xlsx") && ! filename.endsWith(DOC_VAL))
					return false;

				if (filename.equals(filenameToSearch))
					return true;

				filename = SysNatStringUtil.cutExtension(filename);

				if ( ! filenameToSearch.contains("Testdaten") ) {
					filename = SysNatStringUtil.cutTrailingChars(filename, CHARS_TO_CUT_FROM_DATA_FILE_NAME);
				}

				if (filename.equals(filenameToSearch))
					return true;

				int pos = filenameToSearch.indexOf('_');

				if (pos > 0) {
					String filenameCandidate = filenameToSearch.substring(0, pos);
					return filenameCandidate.equals(filename);
				}

				pos = filenameToSearch.lastIndexOf('/');
				if (pos == -1) 
					return false;
				
				String filenameCandidate = filenameToSearch.substring(pos+1);
				boolean ok = filenameCandidate.equals(filename);
				if (ok) return filenameCandidate.equals(filename);

				pos = filenameCandidate.lastIndexOf(".");
				if (pos == -1) return false;
				String filenameCandidateWithoutExtension = filenameCandidate.substring(0, pos);
				return filenameCandidateWithoutExtension.equals(filename);
			}
		});
		
		if (toReturn.size() == 0) {
			String errorMessage = "For '" + filenameToSearch + "' there is no test data file found in " + testdataDir + ".";
			String helpMessage = "Create test data file or remove its reference.";
			ErrorPageLauncher.doYourJob(errorMessage, helpMessage, ERR_MSG_BUNDLE.getString("GenerationError"));
			throw new SysNatTestDataException(errorMessage);
		}
		
		return toReturn;
	}

	private String extractFilename(String testdataId) {
		return testdataId.split(TESTDATA_LOCATION_SEPARATOR)[0];
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addToTestdata(final File file, 
			                   final Hashtable<String, Properties> testdata) 
	{
		final Hashtable<String, Properties> dataSetsFromFile = loadDatasetsFromFile(file);
		final List<String> datasetNames = new ArrayList(dataSetsFromFile.keySet());
		//datasetNames.forEach(System.err::println);


		if (file.getName().endsWith(".dat") || file.getName().endsWith(".xlsx")) 
		{
			// Add only those datasets that match the testdataId
			datasetNames.stream()
					.filter(name -> name.equals(testdataId) || name.startsWith(testdataId) || testdataId.endsWith(name + ".dat"))
					.forEach(name -> testdata.put(name, dataSetsFromFile.get(name)));
		} else {
			datasetNames.forEach(name -> testdata.put(name, dataSetsFromFile.get(name)));
		}
	}

	public Hashtable<String, Properties> loadDatasetsFromFile(final File file) 
	{
		if (file.getName().endsWith(".dat")) {
			return loadFromDatFile(file);
		}

		if (file.getName().endsWith(DOC_VAL)) {
			return loadFromValidationFile(file);
		}

		if (file.getName().endsWith(".xlsx")) {
			return loadDatasetsFromExcelFile(file, testdataId);
		}

		throw new SysNatTestDataException("File type not supported for test data import: " + file.getName());
	}

	private Hashtable<String, Properties> loadFromValidationFile(File file)
	{
		final Hashtable<String, Properties> toReturn = new Hashtable<>();
		final List<DocumentValidationRule> validationRules = ValidationFileReader.doYourJob(file);
		final Properties loadedValidationData = new Properties();
		final List<String> ruleOrder = new ArrayList<String>();
		validationRuleCounter = 0;
		validationRules.forEach(rule -> addRule(rule, loadedValidationData, ruleOrder));
		loadedValidationData.put("RuleOrder", SysNatStringUtil.listToString(ruleOrder, "|"));
		toReturn.put("ValidationRules", loadedValidationData);

		return toReturn;
	}

	private void addRule(DocumentValidationRule rule, 
			             Properties loadedValidationData, 
			             List<String> ruleOrder)
	{
		String ruleName = buildRuleName();
		loadedValidationData.put(ruleName, rule.toString());
		ruleOrder.add(ruleName);
	}

	private String buildRuleName() {
		return "ValidationRule" + ++validationRuleCounter;
	}

	private Hashtable<String, Properties> loadFromDatFile(File file)
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

	private Hashtable<String, Properties> loadDatasetsFromExcelFile(final File excelFile, String testdataId) 
	{
		Hashtable<String, Properties> toReturn = new Hashtable<>();
		LinkedHashMap<String, Properties> excelData = ExcelDataProvider.doYourJob(new File(excelFile.getAbsolutePath()));
		excelData.entrySet().forEach(entry -> toReturn.put(entry.getKey(), entry.getValue()));
		return toReturn;
	}
	
	public List<File> getLastFilesLoaded()
	{
		return lastFilesLoaded;
	}
	
}