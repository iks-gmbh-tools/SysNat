package com.iksgmbh.sysnat.language_templates.common;

import static com.iksgmbh.sysnat.common.utils.SysNatConstants.COMMENT_IDENTIFIER;
import static com.iksgmbh.sysnat.common.utils.SysNatConstants.NO_FILTER;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.ACT_KEYWORD;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.ARRANGE_KEYWORD;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.ASSERT_KEYWORD;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.CLEANUP_KEYWORD;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.ERROR_KEYWORD;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.FROM_FILENAME;

import java.awt.Toolkit;
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
import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;
import com.iksgmbh.sysnat.common.exception.SkipTestCaseException;
import com.iksgmbh.sysnat.common.exception.SkipTestCaseException.SkipReason;
import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.exception.UnexpectedResultException;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.domain.SysNatTestData.SysNatDataset;
import com.iksgmbh.sysnat.helper.VirtualTestCase;

/**
 * Contains the implementation of the most basic language templates available in the natspec files
 * available in each test case. Note that the NatSpec-TextSyntax annotated method are contained
 * in language specific subclasses (see package com.iksgmbh.sysnat.language_templates.common).
 * Therefore this class is not meant to be instantiated (therefore set abstract).
 * 
 * The most basic language templates are those that do not access GUI elements and represent no 
 * application-under-test specific business logic. Each application has its own specific 
 * LanguageTemplates files.
 * 
 * Each individual template represents typically a pattern for one specific instruction in natural language.
 * However, some templates represent an instruction part and must be combined with other templates 
 * to build a full sentence in natural language. The following four types of instructions exist:
 * 
 * 1. Imperative sentences ("Do something.") - Used to manipulate the GUI under test.
 * 2. Questions ("Is something that way?") - Used for asserting domain specific expectation.
 * 3. Passive sentences ("The result is stored.") - Used to hold information during test runtime.
 * 4. Stage directions ("Key: value") - Used to define meta information.
 * 
 */
@LanguageTemplateContainer
public class LanguageTemplatesCommon  
{	
	public static ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/LanguageTemplatesCommon", Locale.getDefault());

	protected ExecutionRuntimeInfo executionInfo;
	protected ExecutableExample testCase;
	private Properties nlsProperties;
	
	public LanguageTemplatesCommon(ExecutableExample test) 
	{
		this.testCase = test;
		this.executionInfo = ExecutionRuntimeInfo.getInstance();
	}

	public boolean isThisTestToExecute(final List<String> testCategoriesOfTestCase,
			                           final List<String> testCategoriesToExecute) 
	{
		if (testCategoriesToExecute.isEmpty()) {
			return true;
		}
		
		if (testCategoriesToExecute.contains(testCase.getXXID())) {
			return true;
		}

		final List<String> listOfNegativeMatchingCategories = new ArrayList<>();
		final List<String> listOfPositiveMatchingCategories = new ArrayList<>();
		final List<String> listOfPositiveCategoriesToExecute = new ArrayList<>();

		for (String categoryToExecute : testCategoriesToExecute)
		{
			if (categoryToExecute.startsWith(SysNatLocaleConstants.NON_KEYWORD + ""))
			{
				final String excludedCategory = SysNatStringUtil.cutPrefix(categoryToExecute,
						SysNatLocaleConstants.NON_KEYWORD);

				if (testCategoriesOfTestCase.contains(excludedCategory)) {
					listOfNegativeMatchingCategories.add(excludedCategory);
				}
			}
			else
			{
				listOfPositiveCategoriesToExecute.add(categoryToExecute);
				if (testCategoriesOfTestCase.contains(categoryToExecute)) {
					listOfPositiveMatchingCategories.add(categoryToExecute);
				}
			}
		}

		if (listOfPositiveCategoriesToExecute.isEmpty()) {
			// at least one negative category is set - now check whether it is a
			// match:
			return listOfNegativeMatchingCategories.isEmpty();
		}
		else
		{
			if (listOfPositiveMatchingCategories.isEmpty()) {
				return false; // at least one positive category is set but there
								// is no match
			}

			// at least one positive matching category is set - now check
			// negative categories:

			return listOfNegativeMatchingCategories.isEmpty();

		}
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
	
	protected void executeScript(String scriptName, ExecutableExample aTestCase)
	{
		Method executeTestMethod = null;
		Object newInstance = null;
		try {
			Class<?> classForName = getClassFor(scriptName);
			Constructor<?> constructor = classForName.getConstructor(ExecutableExample.class);
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

	
	@LanguageTemplate(value = "XXID: ^^")
	public void startNewTestCase(String xxid) 
	{
		xxid = xxid.trim();
		if (! testCase.doesTestBelongToApplicationUnderTest()) {
			throw new SkipTestCaseException(SkipReason.APPLICATION_TO_TEST);
		}
		
		if (xxid.equals(FROM_FILENAME) || xxid.equals("<filename>"))  {
			xxid = testCase.getTestCaseFileName();
		}
		
		if (executionInfo.isXXIdAlreadyUsed(xxid))  {
			testCase.failWithMessage(BUNDLE.getString("Ambiguous") + " XXId: " + xxid);
		}

		testCase.setXXID( xxid.trim() );
		System.out.println((executionInfo.getTotalNumberOfTestCases() + 1) + ". XXId: " + xxid);
		executionInfo.countTestCase();
		
		if ( ! executionInfo.isApplicationStarted() ) {
			testCase.failWithMessage("Die Anwendung <b>" + executionInfo.getTestApplicationName() 
			                          + "</b> steht derzeit nicht zur Verfügung!");
		}
	}

	@LanguageTemplate(value = "Kategorie-Filter: ^^")
	@LanguageTemplate(value = "Category-Filter: ^^")
	public void checkFilterCategory(String testCategoriesOfThisTestCase) 
	{
		boolean executeThisTest = true;
		final List<String> testCategoriesOfTestCase = testCase.buildTestCategories(testCategoriesOfThisTestCase);
		testCategoriesOfTestCase.addAll(testCase.getPackageNames());
		final List<String> testCategoriesToExecute = executionInfo.getTestCategoriesToExecute();
		if (testCategoriesToExecute.size() > 1 || ! testCategoriesToExecute.get(0).equalsIgnoreCase(NO_FILTER)) {
			executeThisTest = isThisTestToExecute(testCategoriesOfTestCase, testCategoriesToExecute);
		}
		
		for (String category : testCategoriesOfTestCase) {
			executionInfo.addCategoryToCollection(category);
		}	
		
		if (executeThisTest) {
			executionInfo.countExcecutedTestCase(); 
		} else {
			throw new SkipTestCaseException(SkipReason.CATEGORY_FILTER);
		}
	}

	@LanguageTemplate(value = "Aktiv: ^^")
	@LanguageTemplate(value = "Active: ^^")
	public void setActiveState(String value)  
	{
		if (value.trim().equalsIgnoreCase("nein") 
			|| value.trim().equalsIgnoreCase("no"))  
		{
			executionInfo.uncountAsExecutedTestCases();  // undo previously added 
			executionInfo.addInactiveTestCase( testCase.getXXID() );
			throw new SkipTestCaseException(SkipReason.ACTIVATION_STATE);
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

	@LanguageTemplate(value = "Wait ^^ second(s).") 
	@LanguageTemplate(value = "Warte ^^ Sekunde(n).")
	public void waitSeconds(String seconds) 
	{
		seconds = seconds.replace(',', '.');
		
		try {
			double sec = Double.valueOf(seconds);
			Thread.sleep((long)sec * 1000);
			testCase.addReportMessage(BUNDLE.getString("WaitComment").replace("x", seconds));
		} catch (Exception e) {
			testCase.addReportMessage(ERROR_KEYWORD + ": Die Testausführung hat nicht warten können.");
		}
	}


	@LanguageTemplate(value = "Date of today is stored as <>.")  
	@LanguageTemplate(value = "Das heutige Datum wird als <> festgehalten.")
	public String returnTodayDateAsString() {
		return executionInfo.getTodayDateAsString();
	}

	@LanguageTemplate(value = "Beep ^^ times.") 
	@LanguageTemplate(value = "Piepse ^^ mal.")
	public void beep(int timesToBeep) 
	{
		for (int i = 0; i < timesToBeep; i++) {
			Toolkit.getDefaultToolkit().beep();
			testCase.sleep(200);
		}
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
	public void importTestData(String testDataId) {
		testCase.importTestData(testDataId);
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
	
	@LanguageTemplate(value = "Save screenshot now as ^^.") 
	@LanguageTemplate(value = "Ein Screenshot wird als ^^ gespeichert.")
	public void saveScreenshotWithName(String filename) 
	{
		filename = filename.replace("<XXId>", testCase.getXXID());
		testCase.takeScreenshot(filename);
		testCase.addReportMessage(BUNDLE.getString("ScreenshotSavedComment").replace("x", filename));
	}

	@LanguageTemplate(value = "Save screenshot now if test case is categorized as mitBildnachweis.")
	@LanguageTemplate(value = "Ein Screenshot wird gespeichert, falls der Testfall als pictureProof kategorisiert wurde.")
	public void savePictureProofIfNecessary() 
	{
		if (testCase.hasBildnachweisCategory()) 
		{			
			String fileName = testCase.getScreenshotName();
			testCase.takeScreenshot(fileName);
			testCase.addReportMessage(BUNDLE.getString("ScreenshotSavedComment").replace("x", fileName));
		}
	}

	/**
	 * Marker method to manage parameterized tests.
	 */
	@LanguageTemplate(value = "Test-Parameter: ^^")
	public void applyTestParameter(String param) {
	    // needs no implementation because this method call will be replaced !
	}
	
}
