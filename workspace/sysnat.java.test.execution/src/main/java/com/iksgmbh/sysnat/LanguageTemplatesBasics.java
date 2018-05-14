package com.iksgmbh.sysnat;

import static com.iksgmbh.sysnat.utils.SysNatConstants.COMMENT_IDENTIFIER;
import static com.iksgmbh.sysnat.utils.SysNatConstants.NO_FILTER;
import static com.iksgmbh.sysnat.utils.SysNatLocaleConstants.ACT_KEYWORD;
import static com.iksgmbh.sysnat.utils.SysNatLocaleConstants.ARRANGE_KEYWORD;
import static com.iksgmbh.sysnat.utils.SysNatLocaleConstants.ASSERT_KEYWORD;
import static com.iksgmbh.sysnat.utils.SysNatLocaleConstants.CLEANUP_KEYWORD;
import static com.iksgmbh.sysnat.utils.SysNatLocaleConstants.ERROR_KEYWORD;
import static com.iksgmbh.sysnat.utils.SysNatLocaleConstants.FROM_FILENAME;

import java.awt.Toolkit;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.domain.DateValue;
import com.iksgmbh.sysnat.domain.SysNatTestData;
import com.iksgmbh.sysnat.domain.SysNatTestData.ObjectData;
import com.iksgmbh.sysnat.exception.SkipTestCaseException;
import com.iksgmbh.sysnat.exception.SkipTestCaseException.SkipReason;
import com.iksgmbh.sysnat.exception.SysNatException;
import com.iksgmbh.sysnat.exception.TestDataException;
import com.iksgmbh.sysnat.exception.UnexpectedResultException;
import com.iksgmbh.sysnat.helper.VirtualTestCase;
import com.iksgmbh.sysnat.utils.SysNatLocaleConstants;
import com.iksgmbh.sysnat.utils.SysNatStringUtil;

/**
 * Contains the implementation of the most basic language templates available in the natspec files
 * available in each test case. Note that the NatSpec-LanguageTempleate annotated method are contained
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
public abstract class LanguageTemplatesBasics  
{	
	private static final String FACTORY_PACKAGE = "com.iksgmbh.helloworldspringboot.greeting.factory";
	
	protected ResourceBundle bundle;
	protected ExecutionInfo executionInfo;
	protected TestCase testCase;
	
	public LanguageTemplatesBasics(TestCase test, ResourceBundle aBundle) 
	{
		this.testCase = test;
		this.bundle =  aBundle;
		this.executionInfo = ExecutionInfo.getInstance();
	}

	
	//##########################################################################################
	//                          P R I V A T E   M E T H O D S
	//##########################################################################################

	boolean isThisTestToExecute(final List<String> testCategoriesOfTestCase,
			                    final List<String> testCategoriesToExecute) 
	{
		if (testCategoriesToExecute.isEmpty()) {
			return true;
		}
		
		if (testCategoriesToExecute.contains(testCase.getTestID().replaceAll("_", " "))) {
			return true;
		}

		final List<String> listOfNegativeMatchingCategories = new ArrayList<>();
		final List<String> listOfPositiveMatchingCategories = new ArrayList<>();
		final List<String> listOfPositiveCategoriesToExecute = new ArrayList<>();

		for (String categoryToExecute : testCategoriesToExecute) {
			if (categoryToExecute.startsWith(SysNatLocaleConstants.NON_KEYWORD + "")) 
			{
				final String excludedCategory = SysNatStringUtil.cutPrefix(categoryToExecute, SysNatLocaleConstants.NON_KEYWORD);
				if (testCategoriesOfTestCase.contains(excludedCategory)) {
					listOfNegativeMatchingCategories.add(excludedCategory);
				}
			} else {
				listOfPositiveCategoriesToExecute.add(categoryToExecute);
				if (testCategoriesOfTestCase.contains(categoryToExecute)) {
					listOfPositiveMatchingCategories.add(categoryToExecute);
				}
			}
		}

		
		if (listOfPositiveCategoriesToExecute.isEmpty()) 
		{
			// at least one negative category is set - now check whether it is a match:
			return listOfNegativeMatchingCategories.isEmpty();
		} 
		else 
		{
			if (listOfPositiveMatchingCategories.isEmpty()) {
				return false;  // at least one positive category is set but there is no match 
			}
			
			// at least one positive matching category is set - now check negative categories:
			return listOfNegativeMatchingCategories.isEmpty();
		}
	}

	private Class<?> getClassFor(String scriptName) throws ClassNotFoundException 
	{
		final List<String> scriptPackages = testCase.getApplicationSpecificLangaugeTemplates().getScriptDirectories();
		
		for (String packagePath : scriptPackages) {
			try {
				return Class.forName(packagePath + "." + scriptName); 
			} catch (Exception e) {
				// ignore
			}
		}

		
		throw new ClassNotFoundException(bundle.getString("UnkownScriptComment").replace("x", scriptName));
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
			executeTestMethod = classForName.getMethod("executeTestCase");
		} catch (ClassNotFoundException e) {
			aTestCase.failWithMessage(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			aTestCase.failWithMessage(bundle.getString("NotExecutableScriptComment").replace("x", scriptName)
					                 + System.getProperty("line.separator") + e.getMessage());
		}
		
		if (executeTestMethod != null  && newInstance != null) {
			try {
				aTestCase.addReportMessage(COMMENT_IDENTIFIER + "---------- " + bundle.getString("ScriptStart") 
				                          + ": <b>" + scriptName + "</b> ----------");
				executeTestMethod.invoke(newInstance);
				aTestCase.addReportMessage(COMMENT_IDENTIFIER + "---------- " + bundle.getString("ScriptEnd") 
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
				aTestCase.failWithMessage(bundle.getString("FailedScriptExecutionComment").replace("x", scriptName));
			}
		}
	}


	//##########################################################################################
	//                       T E X T  S Y N T A X    M E T H O D S
	//##########################################################################################

	
	public void startNewTestCase(String testID) 
	{
		if (! testCase.doesTestBelongToApplicationUnderTest()) {
			throw new SkipTestCaseException(SkipReason.APPLICATION_TO_TEST);
		}
		
		testID = SysNatStringUtil.replaceEmptyStringSymbol(testID);
		
		if (testID.equals(FROM_FILENAME) || testID.equals("<filename>"))  {
			testID = testCase.buildTestIdFromNatSpecFileName();
		}
		
		if (executionInfo.isTestIdAlreadyUsed(testID))  {
			testCase.failWithMessage(bundle.getString("Ambiguous") + " Test-Id: " + testID);
		}

		testCase.setTestID( testID.trim() );
		System.out.println((executionInfo.getTotalNumberOfTestCases() + 1) + ". TestID: " + testID);
		executionInfo.countTestCase();
	}

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

	public void setActiveState(String value)  
	{
		if (value.equalsIgnoreCase("nein"))  {
			executionInfo.uncountAsExecutedTestCases();  // undo previously added 
			executionInfo.addInactiveTestCase( testCase.getTestID() );
			throw new SkipTestCaseException(SkipReason.ACTIVATION_STATE);
		}
	}

	public void createComment(String comment) {
		comment = SysNatStringUtil.replaceEmptyStringSymbol(comment);
		testCase.addReportMessage(COMMENT_IDENTIFIER + comment);
	}
	
	public void createKeywordArrangeComment() {
		testCase.addReportMessage(COMMENT_IDENTIFIER + ARRANGE_KEYWORD);
	}
	
	public void createKeywordActComment() {
		testCase.addReportMessage(COMMENT_IDENTIFIER + ACT_KEYWORD);
	}

	public void createAssertKeywordComment() {
		testCase.addReportMessage(COMMENT_IDENTIFIER + ASSERT_KEYWORD);
	}

	public void createCleanupKeywordComment() {
		testCase.addReportMessage(COMMENT_IDENTIFIER + CLEANUP_KEYWORD);
	}

	@LanguageTemplate(value = "Warte #1 Sekunden.")
	public void waitSeconds(String seconds) 
	{
		seconds = seconds.replace(',', '.');
		
		try {
			double sec = Double.valueOf(seconds);
			Thread.sleep((long)sec * 1000);
			testCase.addReportMessage(bundle.getString("WaitComment").replace("x", seconds));
		} catch (Exception e) {
			testCase.addReportMessage(ERROR_KEYWORD + ": Die Testausführung hat nicht warten können.");
		}
	}


	public DateValue returnTodayDateAsString(final String dateName) {
			return returnTodayDateAsString(dateName);
	}

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
	public ObjectData getDatasetObject(String objectName) 
	{
		if ( ! testCase.getTestDataSets().isKnown(objectName) ) {
			testCase.buildTestDataFor(objectName);
		}
		
		return testCase.getTestDataSets().getObjectData(objectName);
	}	

	/**
	 * Removes all existing data sets and makes an empty data collection available.
	 * Test data MUST be added by "setSingleTestDataValue" method!
	 */
	public SysNatTestData createSysNatTestData() 
	{
		testCase.getTestDataSets().clear();
		return testCase.getTestDataSets();
	}
	
	/**
	 * Removes all existing data sets and makes an data collection available
	 * that contains those data referenced by 'objectNames'.
	 * The references test data may be modified by data set with the "setSingleTestDataValue" method!
	 * 
	 * @param objectNames one or more (comma separated) references to data sets.
	 * @return data collection
	 */
	public SysNatTestData createSysNatTestData(String objectNames) 
	{
		testCase.getTestDataSets().clear();
		final List<String> objectNameList = getObjectNameList(objectNames);
		for (String objectName : objectNameList) {
			testCase.buildTestDataFor(objectName);
		}
		
		return testCase.getTestDataSets();
	}

	/**
	 * Removes all existing data sets and makes a data collection available
	 * that contains those data referenced by 'datasetType'.
	 * Those datasets represent data of the same type (of the same data class) as key-value-pairs.
	 * They are used for repeated execution of a script with different test data (like Parameterized Tests in JUnit)
	 * They can be provided on the ExcelToJava way or as plain text dat files in the test data directory.
	 * 
	 * @param objectNames one or more (comma separated) references to data sets.
	 * @return data collection
	 */
	public SysNatTestData loadDataSets(String datasetType) 
	{
		testCase.getTestDataSets().clear();

		final Class<?> factoryClass = getFactoryFor(datasetType);
		if (factoryClass != null) 
		{
			// load Excel2Java data
			List<?> datasets = null;
			try {
				Method method = factoryClass.getMethod("createAllFromDataPool");
				datasets = (List<?>)method.invoke(null);
			} catch (Exception e) {
				e.printStackTrace();
				throw new TestDataException("Error accessing " + factoryClass.getName() + "!");
			}
			testCase.buildTestDataFor(datasetType, datasets);
		}
		else 
		{
			// load plain text data from TestData directory
			File testDataDir = new File(TestCase.TEST_DATA_DIR, ExecutionInfo.getInstance().getAppUnderTest().name());
			String[] datFiles = testDataDir.list(new FilenameFilter() {
				@Override public boolean accept(File dir, String name) {
					return name.contains(datasetType)
						   && name.endsWith(".dat");
				}
			});
			
			for (String name : datFiles) {
				getDatasetObject(name);
			}
			
		}
		
		return testCase.getTestDataSets();
	}	


	private Class<?> getFactoryFor(String datasetType) 
	{
		String className = FACTORY_PACKAGE + "." + datasetType + "Factory";
		Class<?> toReturn;
		 try {
			 toReturn = Class.forName(className);
		} catch (ClassNotFoundException e) {
			return null;
		} 
		return toReturn;
	}


	/**
	 * Used to override or extent data sets provided in testDataSets.
	 * @param objectAndfieldName
	 * @param value
	 */
	public void setSingleTestDataValue(String objectAndfieldName, String value) 
	{
		String[] splitResult = objectAndfieldName.split("\\.");
		
		if (splitResult.length == 1) {
			testCase.getTestDataSets().add(splitResult[0], value);
		} else {			
			String objectName = splitResult[0];
			String fieldName = splitResult[1];
			if ( ! testCase.getTestDataSets().isKnown(objectName) ) {
				testCase.failWithMessage(bundle.getString("NoTestDataComment"));
			}
			testCase.getTestDataSets().add(objectName, fieldName, value);
		}
	}


	/**
	 * Executes the given script using the data previously defined
	 * Terminates the test case with error if no data can be found.
	 * @param scriptName
	 */
	public void executeScriptWithData(String scriptName)
	{
		if (testCase.getTestDataSets().size() == 0)  {
			testCase.failWithMessage(bundle.getString("NoTestDataForScriptComment").replace("x", scriptName));
		}

		executeScript(scriptName, testCase);
	}
	
	public void executeScript(String scriptName) {
		executeScript(scriptName, testCase);
	}	
	
	/**
	 * Executes the given script repeatedly using the data previously defined.
	 * Each repetition is reported as individual test case.
	 * @param scriptName
	 */	
	public void executeTheScriptAsSeparateTestcase(String scriptName) 
	{
		try {
			getClassFor(scriptName);
		} catch (ClassNotFoundException e) {
			testCase.failWithMessage(ERROR_KEYWORD + ": " + e. getMessage()); 
		}
		
		List<ObjectData> objectDataSets = testCase.getTestDataSets().getAllObjectData();
		for (ObjectData objectData : objectDataSets) 
		{
			testCase.getTestDataSets().clear();
			testCase.getTestDataSets().addObjectData(objectData.getName(), objectData);
			VirtualTestCase virtualTestCase = new VirtualTestCase(objectData.getName());
			
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
	
	public void saveScreenshotWithName(String fileName) 
	{
		fileName = fileName.replace("<TestId>", testCase.getTestID());
		fileName = SysNatStringUtil.replaceEmptyStringSymbol(fileName);
		testCase.takeScreenshot(fileName);
		testCase.addReportMessage(bundle.getString("ScreenshotSavedComment").replace("x", fileName));
	}

	public void savePictureProofIfNecessary() 
	{
		if (testCase.hasBildnachweisCategory()) 
		{			
			String fileName = testCase.getScreenshotName();
			testCase.takeScreenshot(fileName);
			testCase.addReportMessage(bundle.getString("ScreenshotSavedComment").replace("x", fileName));
		}
	}

}
