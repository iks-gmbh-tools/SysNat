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
package com.iksgmbh.sysnat.test.helper.parameterizedtestapp;

import static com.iksgmbh.sysnat.common.utils.SysNatConstants.COMMENT_IDENTIFIER;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.ERROR_KEYWORD;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.FROM_FILENAME;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;
import com.iksgmbh.sysnat.common.exception.SkipTestCaseException;
import com.iksgmbh.sysnat.common.exception.SkipTestCaseException.SkipReason;
import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.exception.UnexpectedResultException;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.domain.SysNatTestData;
import com.iksgmbh.sysnat.domain.SysNatTestData.SysNatDataset;
import com.iksgmbh.sysnat.helper.VirtualTestCase;

@LanguageTemplateContainer
public class TestLanguageTemplatesContainer  
{	
	public static ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/LanguageTemplatesCommon", Locale.getDefault());

	protected ExecutionRuntimeInfo executionInfo;
	protected ExecutableExample executableExample;
	private Properties nlsProperties;
	
	public TestLanguageTemplatesContainer(ExecutableExample aExecutableExample) 
	{
		this.executableExample = aExecutableExample;
		this.executionInfo = ExecutionRuntimeInfo.getInstance();
	}

	
	//##########################################################################################
	//                          P R I V A T E   M E T H O D S
	//##########################################################################################


	private Class<?> getClassFor(String simpleScriptName) throws ClassNotFoundException 
	{
		String fullyQualifiedScriptName = getScripts().getProperty(simpleScriptName);
		
		if (fullyQualifiedScriptName == null) {			
			fullyQualifiedScriptName = getScripts().getProperty(simpleScriptName + "Script");
			if (fullyQualifiedScriptName == null) {			
				throw new ClassNotFoundException(BUNDLE.getString("UnkownScriptComment").replace("x", simpleScriptName));
			}
		}
		return Class.forName(fullyQualifiedScriptName); 
		
	}


	private Properties getScripts() 
	{
		if (nlsProperties == null) {
			nlsProperties = new Properties();
			final String propertyFileName = System.getProperty("sysnat.nls.lookup.file");
			SysNatFileUtil.loadPropertyFile(new File(propertyFileName), nlsProperties);
		}
		return nlsProperties;
	}

	private List<String> getObjectNameList(String objectNames) 
	{
		String[] splitResult = objectNames.replaceAll("den_", "").replaceAll("the_", "")
				                          .replaceAll("_und_", ",").replaceAll("_and_", ",")
				                          .replaceAll(" ", "").split(",");
		return new ArrayList<>(Arrays.asList(splitResult));
	}
	
	protected void executeScript(String scriptName, ExecutableExample aExecutableExample)
	{
		Method executeTestMethod = null;
		Object newInstance = null;
		try {
			Class<?> classForName = getClassFor(scriptName);
			Constructor<?> constructor = classForName.getConstructor(ExecutableExample.class);
			newInstance = constructor.newInstance(aExecutableExample);
			executeTestMethod = classForName.getMethod("executeScript");
		} catch (ClassNotFoundException e) {
			aExecutableExample.failWithMessage(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			aExecutableExample.failWithMessage(BUNDLE.getString("NotExecutableScriptComment").replace("x", scriptName)
					                 + System.getProperty("line.separator") + e.getMessage());
		}
		
		if (executeTestMethod != null  && newInstance != null) {
			try {
				aExecutableExample.addReportMessage(COMMENT_IDENTIFIER + "---------- " + BUNDLE.getString("ScriptStart") 
				                          + ": <b>" + scriptName + "</b> ----------");
				executeTestMethod.invoke(newInstance);
				aExecutableExample.addReportMessage(COMMENT_IDENTIFIER + "---------- " + BUNDLE.getString("ScriptEnd") 
				                          + ": <b>" + scriptName + "</b> ----------");
			}
			catch (InvocationTargetException ite) 
			{
				Throwable targetException = ite.getTargetException();
				if (targetException instanceof SysNatException) {
					throw (SysNatException)targetException;
				}
				throw new RuntimeException(ite.getTargetException());
			}
			catch (Exception e) 
			{
				aExecutableExample.failWithMessage(BUNDLE.getString("FailedScriptExecutionComment").replace("x", scriptName));
			}
		}
	}


	//##########################################################################################
	//                   L A N G U A G E   T E M P L A T E    M E T H O D S
	//##########################################################################################

	
	@LanguageTemplate(value = "XXID: ^^")
	public void startNewXX(String xxid) 
	{
		xxid = xxid.trim();
		if (! executableExample.doesTestBelongToApplicationUnderTest()) {
			throw new SkipTestCaseException(SkipReason.APPLICATION_TO_TEST);
		}
		
		if (xxid.equals(FROM_FILENAME) || xxid.equals("<filename>"))  {
			xxid = executableExample.getTestCaseFileName();
		}
		
		if (executionInfo.isXXIdAlreadyUsed(xxid))  {
			executableExample.failWithMessage(BUNDLE.getString("Ambiguous") + " XXID: " + xxid);
		}

		executableExample.setXXID( xxid.trim() );
		System.out.println((executionInfo.getTotalNumberOfXXs() + 1) + ". XXID: " + xxid);
		executionInfo.countAsExecuted(xxid, executableExample.getBehaviorID());
		executionInfo.countExistingXX();
		
		if ( ! executionInfo.isApplicationStarted() ) {
			executableExample.failWithMessage("Die Anwendung <b>" + executionInfo.getTestApplicationName() 
			                          + "</b> steht derzeit nicht zur Verfügung!");
		}
	}


	@LanguageTemplate(value = "Kommentar: ^^")
	@LanguageTemplate(value = "Comment: ^^")
	public void createComment(String comment) {
		executableExample.addReportMessage(COMMENT_IDENTIFIER + comment);
	}

	@LanguageTemplate(value = "Test-Phase: ^^")
	public void startNewTestPhase(String phase)
	{
		if (SysNatConstants.TestPhase.valueOf(phase.trim().toUpperCase()) != null) {
			throw new SysNatException("Unknown test phase <b>" + phase + "</b>.");
		}
		executableExample.addReportMessage(COMMENT_IDENTIFIER + phase);
	}
	
	/**
	 * Makes a single data set available.
	 * If not yet done, it is initialized now.
	 * Terminates with error, if objectName is unknown as test data.
	 * Test data is read from the TestData directory
	 * @param objectName
	 * @return
	 */
	@LanguageTemplate(value = "There are ^^.") 
	@LanguageTemplate(value = "Es existieren ^^.")
	public void setDatasetObject(String datatype) 
	{
		if ( ! executableExample.getTestData().isKnown(datatype) ) {
			executableExample.importTestData(datatype);
		}
	}	
	
	@LanguageTemplate(value = "Testdaten: ^^")
	@LanguageTemplate(value = "TestData: ^^")
	public void importTestData(String nameOfDataset) {
		executableExample.importTestData(nameOfDataset);
	}


	/**
	 * Removes all existing data sets and makes an empty data collection available.
	 * Test data MUST be added by "setSingleTestDataValue" method!
	 */
	@LanguageTemplate(value = "Execute with following data") 
	@LanguageTemplate(value = "Führe mit folgenden Daten")  
	public void createSysNatTestData() 
	{
		executableExample.getTestData().clear();
	}
	
	/**
	 * Removes all existing data sets and makes an data collection available
	 * that contains those data referenced by 'objectNames'.
	 * The references test data may be modified by data set with the "setSingleTestDataValue" method!
	 * 
	 * @param objectNames one or more (comma separated) references to data sets.
	 */
	@LanguageTemplate(value = "Execute with ^^") 
	@LanguageTemplate(value = "Führe mit folgenden Daten ^^")  
	public void loadTestDatasets(String datatypes) 
	{
		executableExample.getTestData().clear();
		final List<String> datatypesList = getObjectNameList(datatypes);
		for (String datatype : datatypesList) {
			executableExample.importTestData(datatype);
		}
	}

	/**
	 * Removes all existing data sets and makes a data collection available
	 * that contains those data referenced by 'datasetType'.
	 * Those datasets represent data of the same type (of the same data class) as key-value-pairs.
	 * They are used for repeated execution of a script with different test data (like Parameterized Tests in JUnit)
	 * They can be provided on the ExcelToJava way or as plain text dat files in the test data directory.
	 * 
	 * @param objectNames one or more (comma separated) references to data sets.
	 */
	@LanguageTemplate(value = "Execute for all ^^") 
	@LanguageTemplate(value = "Führe mit ^^")
	public void storeDataSets(String datatype) 
	{
		executableExample.getTestData().clear();
		executableExample.importTestData(datatype);
	}	


	/**
	 * Used to override or extent data sets provided in testDataSets.
	 * @param objectAndfieldName
	 * @param value
	 */
	@LanguageTemplate(value = "^^ = ^^") 
	public void setSingleTestDataValue(String objectAndfieldName, String value) 
	{
		String[] splitResult = objectAndfieldName.split("\\.");
		
		if (splitResult.length == 1) {
			executableExample.getTestData().addValue(SysNatTestData.SINGLE_TEST_DATA_VALUES, 
					                                 splitResult[0], value);
		} else {			
			String objectName = splitResult[0];
			String fieldName = splitResult[1];
			if ( ! executableExample.getTestData().isKnown(objectName) ) {
				executableExample.failWithMessage(BUNDLE.getString("NoTestDataComment"));
			}
			executableExample.getTestData().addValue(objectName, fieldName, value);
		}
	}


	/**
	 * Executes the given script using the data previously defined
	 * Terminates the test case with error if no data can be found.
	 * @param scriptName
	 */
	@LanguageTemplate(value = "the script ^^.") 
	@LanguageTemplate(value = "das Skript ^^ aus.")
	public void executeScriptWithData(String scriptName)
	{
		if (executableExample.getTestData().size() == 0)  {
			executableExample.failWithMessage(BUNDLE.getString("NoTestDataForScriptComment").replace("x", scriptName));
		}

		executeScript(scriptName, executableExample);
	}
	
	@LanguageTemplate(value = "Execute script ^^.")  
	@LanguageTemplate(value = "Führe das Skript ^^ aus.")
	@LanguageTemplate(value = "^^.")  
	public void executeScript(String scriptName) {
		executeScript(scriptName, executableExample);
	}	
	
	/**
	 * Executes the given script repeatedly using the data previously defined.
	 * Each repetition is reported as individual test case.
	 * @param scriptName
	 */	
	@LanguageTemplate(value = "the script ^^ as separate testcases.") 
	@LanguageTemplate(value = "das Skript ^^ als separaten Testfall aus.")
	public void executeTheScriptAsSeparateTestcase(String scriptName)      // TODO Was macht das?
	{
		try {
			getClassFor(scriptName);
		} catch (ClassNotFoundException e) {
			executableExample.failWithMessage(ERROR_KEYWORD + ": " + e. getMessage()); 
		}
		
		List<SysNatDataset> objectDataSets = executableExample.getTestData().getAllDatasets();
		for (SysNatDataset objectData : objectDataSets) 
		{
			VirtualTestCase virtualTestCase = new VirtualTestCase(objectData.getName());
			virtualTestCase.getTestData().addDataset(objectData.getName(), objectData);
			
			try {
				executeScript(scriptName, virtualTestCase);
			} catch (UnexpectedResultException e) {
				// do nothing
			} catch (Exception e) {
				virtualTestCase.addReportMessage(ERROR_KEYWORD + ": " + e. getMessage());
			}
			executionInfo.addToResultAsSeparateXX(virtualTestCase);
		}
		
		executableExample.addReportMessage("A total of " + objectDataSets.size()	+ " datasets has been executed as separate test cases.");
	}
	
	/**
	 * Marker method to manage parameterized tests.
	 */
	@LanguageTemplate(value = "Test-Parameter: ^^")
	public void applyTestParameter(String s) {
	    // needs no implementation because this method call will be replaced !
	}
	
	@LanguageTemplate(value = "Click button ^^.")
	public void clickButton(String s) {
	}
	
	@LanguageTemplate(value = "Is the displayed text ^^ equal to ^^?")
	public void isTextDisplayed(String s1, String s2) {
	}
	


}