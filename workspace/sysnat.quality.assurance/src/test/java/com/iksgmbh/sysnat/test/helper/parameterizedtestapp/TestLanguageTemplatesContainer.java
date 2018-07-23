package com.iksgmbh.sysnat.test.helper.parameterizedtestapp;

import static com.iksgmbh.sysnat.common.utils.SysNatConstants.COMMENT_IDENTIFIER;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.ACT_KEYWORD;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.ARRANGE_KEYWORD;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.ASSERT_KEYWORD;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.CLEANUP_KEYWORD;
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

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.TestCase;
import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;
import com.iksgmbh.sysnat.common.exception.SkipTestCaseException;
import com.iksgmbh.sysnat.common.exception.SkipTestCaseException.SkipReason;
import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.exception.UnexpectedResultException;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.domain.SysNatTestData.SysNatDataset;
import com.iksgmbh.sysnat.helper.VirtualTestCase;

@LanguageTemplateContainer
public class TestLanguageTemplatesContainer  
{	
	public static ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/LanguageTemplatesCommon", Locale.getDefault());

	protected ExecutionRuntimeInfo executionInfo;
	protected TestCase testCase;
	private Properties nlsProperties;
	
	public TestLanguageTemplatesContainer(TestCase test) 
	{
		this.testCase = test;
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
	
	protected void executeScript(String scriptName, TestCase aTestCase)
	{
		Method executeTestMethod = null;
		Object newInstance = null;
		try {
			Class<?> classForName = getClassFor(scriptName);
			Constructor<?> constructor = classForName.getConstructor(TestCase.class);
			newInstance = constructor.newInstance(aTestCase);
			executeTestMethod = classForName.getMethod("executeScript");
		} catch (ClassNotFoundException e) {
			aTestCase.failWithMessage(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			aTestCase.failWithMessage(BUNDLE.getString("NotExecutableScriptComment").replace("x", scriptName)
					                 + System.getProperty("line.separator") + e.getMessage());
		}
		
		if (executeTestMethod != null  && newInstance != null) {
			try {
				aTestCase.addReportMessage(COMMENT_IDENTIFIER + "---------- " + BUNDLE.getString("ScriptStart") 
				                          + ": <b>" + scriptName + "</b> ----------");
				executeTestMethod.invoke(newInstance);
				aTestCase.addReportMessage(COMMENT_IDENTIFIER + "---------- " + BUNDLE.getString("ScriptEnd") 
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
				aTestCase.failWithMessage(BUNDLE.getString("FailedScriptExecutionComment").replace("x", scriptName));
			}
		}
	}


	//##########################################################################################
	//                   L A N G U A G E   T E M P L A T E    M E T H O D S
	//##########################################################################################

	
	@LanguageTemplate(value = "Test-ID: ^^")
	public void startNewTestCase(String testID) 
	{
		testID = testID.trim();
		if (! testCase.doesTestBelongToApplicationUnderTest()) {
			throw new SkipTestCaseException(SkipReason.APPLICATION_TO_TEST);
		}
		
		if (testID.equals(FROM_FILENAME) || testID.equals("<filename>"))  {
			testID = testCase.getTestCaseFileName();
		}
		
		if (executionInfo.isTestIdAlreadyUsed(testID))  {
			testCase.failWithMessage(BUNDLE.getString("Ambiguous") + " Test-Id: " + testID);
		}

		testCase.setTestID( testID.trim() );
		System.out.println((executionInfo.getTotalNumberOfTestCases() + 1) + ". TestID: " + testID);
		executionInfo.countTestCase();
		
		if ( ! executionInfo.isApplicationStarted() ) {
			testCase.failWithMessage("Die Anwendung <b>" + executionInfo.getTestApplicationName() 
			                          + "</b> steht derzeit nicht zur Verfügung!");
		}
	}


	@LanguageTemplate(value = "Kommentar: ^^")
	@LanguageTemplate(value = "Comment: ^^")
	public void createComment(String comment) {
		testCase.addReportMessage(COMMENT_IDENTIFIER + comment);
	}
	
	@LanguageTemplate(value = "Keyword-Comment: Arrange test requirements") 
	@LanguageTemplate(value = "Schlüsselkommentar: Test-Vorbereitungen")
	public void createKeywordArrangeComment() {
		testCase.addReportMessage(COMMENT_IDENTIFIER + ARRANGE_KEYWORD);
	}
	
	@LanguageTemplate(value = "Keyword-Comment: Perform action under test") 
	@LanguageTemplate(value = "Schlüsselkommentar: Test-Durchführung")
	public void createKeywordActComment() {
		testCase.addReportMessage(COMMENT_IDENTIFIER + ACT_KEYWORD);
	}

	@LanguageTemplate(value = "Keyword-Comment: Assert expected results") 
	@LanguageTemplate(value = "Schlüsselkommentar: Überprüfung der Testergebnisse")
	public void createKeywordAssertComment() {
		testCase.addReportMessage(COMMENT_IDENTIFIER + ASSERT_KEYWORD);
	}

	@LanguageTemplate(value = "Keyword-Comment: Reset to start situation")  
	@LanguageTemplate(value = "Schlüsselkommentar: Wiederherstellen der Ausgangssituation")
	public void createKeywordCleanupComment() {
		testCase.addReportMessage(COMMENT_IDENTIFIER + CLEANUP_KEYWORD);
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
		if ( ! testCase.getTestData().isKnown(datatype) ) {
			testCase.importTestData(datatype);
		}
	}	
	
	@LanguageTemplate(value = "Testdaten: ^^")
	@LanguageTemplate(value = "TestData: ^^")
	public void importTestData(String nameOfDataset) {
		testCase.importTestData(nameOfDataset);
	}


	/**
	 * Removes all existing data sets and makes an empty data collection available.
	 * Test data MUST be added by "setSingleTestDataValue" method!
	 */
	@LanguageTemplate(value = "Execute with following data") 
	@LanguageTemplate(value = "Führe mit folgenden Daten")  
	public void createSysNatTestData() 
	{
		testCase.getTestData().clear();
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
		testCase.getTestData().clear();
		final List<String> datatypesList = getObjectNameList(datatypes);
		for (String datatype : datatypesList) {
			testCase.importTestData(datatype);
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
		testCase.getTestData().clear();
		testCase.importTestData(datatype);
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
			testCase.getTestData().addValue(splitResult[0], value);
		} else {			
			String objectName = splitResult[0];
			String fieldName = splitResult[1];
			if ( ! testCase.getTestData().isKnown(objectName) ) {
				testCase.failWithMessage(BUNDLE.getString("NoTestDataComment"));
			}
			testCase.getTestData().addValue(objectName, fieldName, value);
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
		if (testCase.getTestData().size() == 0)  {
			testCase.failWithMessage(BUNDLE.getString("NoTestDataForScriptComment").replace("x", scriptName));
		}

		executeScript(scriptName, testCase);
	}
	
	@LanguageTemplate(value = "Execute script ^^.")  
	@LanguageTemplate(value = "Führe das Skript ^^ aus.")
	@LanguageTemplate(value = "^^.")  
	public void executeScript(String scriptName) {
		executeScript(scriptName, testCase);
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
			testCase.failWithMessage(ERROR_KEYWORD + ": " + e. getMessage()); 
		}
		
		List<SysNatDataset> objectDataSets = testCase.getTestData().getAllDatasets();
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
			executionInfo.addToResultAsSeparateTestCase(virtualTestCase);
		}
		
		testCase.addReportMessage("A total of " + objectDataSets.size()	+ " datasets has been executed as separate test cases.");
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
