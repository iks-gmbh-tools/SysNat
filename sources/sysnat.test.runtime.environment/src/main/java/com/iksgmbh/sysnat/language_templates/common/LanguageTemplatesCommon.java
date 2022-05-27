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
package com.iksgmbh.sysnat.language_templates.common;

import static com.iksgmbh.sysnat.common.utils.SysNatConstants.COMMENT_IDENTIFIER;
import static com.iksgmbh.sysnat.common.utils.SysNatConstants.NO_FILTER;
import static com.iksgmbh.sysnat.common.utils.SysNatConstants.QUESTION_IDENTIFIER;
import static com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants.ERROR_KEYWORD;

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
import java.util.stream.Collectors;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;
import com.iksgmbh.sysnat.common.exception.SkipTestCaseException;
import com.iksgmbh.sysnat.common.exception.SkipTestCaseException.SkipReason;
import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.domain.SysNatTestData;
import com.iksgmbh.sysnat.utils.SysNatTestRuntimeUtil;

/**
 * Contains the implementation of the most basic language templates available in the nlxx files
 * and which are used in all test applications.
 * Note that the application specific LanguageTemplate-annotated method are contained
 * in language specific subclasses (see package com.iksgmbh.sysnat.language_templates.common).
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
	protected ExecutableExample executableExample;
	private Properties nlsProperties;
	private List<String> alreadyLoadedDataFiles = new ArrayList<String>();

	public LanguageTemplatesCommon(ExecutableExample aExecutableExample) 
	{
		this.executableExample = aExecutableExample;
		this.executionInfo = ExecutionRuntimeInfo.getInstance();
	}

	public boolean isThisTestToExecute(final List<String> execFilterOfExecutableExample,
			                           final List<String> execFilterToExecute) 
	{
		if (execFilterToExecute.isEmpty()) {
			return true;
		}
		
		if (execFilterToExecute.contains(executableExample.getXXID())) {
			return true;
		}

		final List<String> listOfNegativeMatchingFilters = new ArrayList<>();;
		final List<String> listOfPositiveMatchingFilters = new ArrayList<>();
		final List<String> listOfPositiveFilterToExecute = new ArrayList<>();

		for (String filterToExecute : execFilterToExecute)
		{
			if (filterToExecute.startsWith(SysNatLocaleConstants.NON_KEYWORD + ""))
			{
				String excludedFilter = SysNatStringUtil.cutPrefix(filterToExecute,
						                                           SysNatLocaleConstants.NON_KEYWORD);

				if (execFilterOfExecutableExample.contains(excludedFilter)) {
					listOfNegativeMatchingFilters.add(excludedFilter);
				}
				
				excludedFilter = excludedFilter.replace("-", "_").replace("-", "_").replace(" ", "");
				if (execFilterOfExecutableExample.contains(excludedFilter)) {
					listOfNegativeMatchingFilters.add(excludedFilter);
				}
				
			}
			else
			{
				listOfPositiveFilterToExecute.add(filterToExecute);
				filterToExecute = filterToExecute.replace("-", "_").replace(" ", "");
				if (execFilterOfExecutableExample.contains(filterToExecute)) {
					listOfPositiveMatchingFilters.add(filterToExecute);
				}

				if (execFilterOfExecutableExample.contains(filterToExecute)) {
					listOfPositiveMatchingFilters.add(filterToExecute);
				}
			}
		}

		if (listOfPositiveFilterToExecute.isEmpty()) {
			// at least one negative filter is set - now check whether it is a
			// match:
			return listOfNegativeMatchingFilters.isEmpty();
		}
		else
		{
			if (listOfPositiveMatchingFilters.isEmpty()) {
				return false; // at least one positive filter is set but there
								// is no match
			}

			// at least one positive matching filter is set - now check
			// negative filters:

			return listOfNegativeMatchingFilters.isEmpty();

		}
	}

	
	//##########################################################################################
	//                          P R I V A T E   M E T H O D S
	//##########################################################################################


	private String checkForTestDataValue(String comment)
	{
		try {
			String toParse = comment;
			StringBuffer toReturn = new StringBuffer();
			
			while (! toParse.isEmpty()) 
			{
				if (toParse.charAt(0) == '\'') 
				{
					toParse = toParse.substring(1);
					int pos = toParse.indexOf("'");
					String temp = toParse.substring(0, pos);
					if (temp.contains("getTestDataValue")) 
					{
						toParse = toParse.substring(temp.length());
						pos = toParse.indexOf("'");
						toParse = toParse.substring(pos+1);
						pos = toParse.indexOf("'");
						String value = toParse.substring(0, pos);
						toReturn.append(executableExample.getTestDataValue(value));
						toParse = toParse.substring(pos+1);
						pos = toParse.indexOf("'");
						toParse = toParse.substring(pos+1);
					} else {
						toReturn.append("'");
					}
				} else {
					toReturn.append(toParse.charAt(0));
					toParse = toParse.substring(1);
				}
			}
			
			return toReturn.toString();
			
		} catch (Exception e) {
			return comment;
		}
	}

	private Class<?> getClassFor(String simpleScriptName) throws ClassNotFoundException 
	{
		simpleScriptName = simpleScriptName.replaceAll("-", "_");
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
			String propertyFileName = System.getProperty("sysnat.nls.lookup.file");
			propertyFileName = SysNatFileUtil.findAbsoluteFilePath(propertyFileName);
			SysNatFileUtil.loadPropertyFile(new File(propertyFileName), nlsProperties);
		}
		return nlsProperties;
	}

	private List<String> getObjectNameList(String objectNames) 
	{
		String[] splitResult = objectNames.replaceAll("den ", "").replaceAll("der ", "").replaceAll("the ", "")
				                          .replaceAll(" und ", ",").replaceAll(" and ", ",")
				                          .split(",");

		ArrayList<String> names = new ArrayList<>(Arrays.asList(splitResult));
		List<String> toReturn = new ArrayList<String>();
		names.forEach(name -> toReturn.add(name.trim()));
		
		return toReturn;
	}
	
	protected void executeScript(String scriptName, ExecutableExample aExecutableExample)
	{
		scriptName = scriptName.replaceAll(",", "");
		executionInfo.registerExecutedNLFile(scriptName + ".nls");
		scriptName = SysNatStringUtil.toFileName(scriptName);
		Method executeTestMethod = null;
		Object scriptJavaCode = null;
		try {
			Class<?> classForName = getClassFor(scriptName);
			Constructor<?> constructor = classForName.getConstructor(ExecutableExample.class);
			scriptJavaCode = constructor.newInstance(aExecutableExample);
			executeTestMethod = classForName.getMethod("executeScript");
		} catch (ClassNotFoundException e) {
			aExecutableExample.failWithMessage(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			aExecutableExample.failWithMessage(BUNDLE.getString("NotExecutableScriptComment").replace("x", scriptName)
					                 + System.getProperty("line.separator") + e.getMessage());
		}
		
		if (executeTestMethod != null  && scriptJavaCode != null) 
		{
			executeScript(scriptName, aExecutableExample, executeTestMethod, scriptJavaCode);
			aExecutableExample.setCurrentPage(((ExecutableExample)scriptJavaCode).getCurrentPage());
		}
	}

	private int toInteger(String intAsString, String valueName) {
		try {
			return Integer.valueOf(intAsString);
		} catch (NumberFormatException e) {
			executableExample.failWithMessage("Der Wert <b>" + valueName + "</b> ist keine Ganzzahl (<b>" + intAsString + "</b>).");
			return Integer.MIN_VALUE;
		}
	}
	
	private void executeScript(final String scriptName,
                               final ExecutableExample aExecutableExample,
                               final Method executeTestMethod,
                               final Object newInstance)
	{
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


	//##########################################################################################
	//                   L A N G U A G E   T E M P L A T E    M E T H O D S
	//##########################################################################################

	@LanguageTemplate(value = "Behaviour: ^^")
	public void declareXXGroupForBehaviour(String aBehaviourID) 
	{
		if (aBehaviourID.equals(SysNatLocaleConstants.PLACEHOLDER_FILENAME) 
			|| aBehaviourID.equals(SysNatLocaleConstants.PLACEHOLDER_FILENAME_EN))  
		{
			aBehaviourID = executableExample.getTestCasePackage().getName();
			int pos = aBehaviourID.lastIndexOf('.');
			aBehaviourID = aBehaviourID.substring(pos+1);
			aBehaviourID = SysNatStringUtil.firstCharToUpperCase(aBehaviourID);
		}
		executableExample.setBehaviorID(aBehaviourID);
	}

	@LanguageTemplate(value = "Verhalten: ^^")
	public void declareXXGroupForBehaviour_in_German(String aBehaviourID) 
	{
		if (aBehaviourID.equals("<Dateiname>"))  {
			aBehaviourID = executableExample.getTestCaseFileName();
		}
		executableExample.setBehaviorID(aBehaviourID);
	}	

	@LanguageTemplate(value = "XX: ^^")
	@LanguageTemplate(value = "XXID: ^^")
	@LanguageTemplate(value = "XXId: ^^")
	public void startNewXX(String xxid) 
	{
		xxid = xxid.trim();
		if (! executableExample.doesTestBelongToApplicationUnderTest()) {
			throw new SkipTestCaseException(SkipReason.APPLICATION_TO_TEST);
		}
		
		if (xxid.equals(SysNatLocaleConstants.PLACEHOLDER_FILENAME) || 
			xxid.equals(SysNatLocaleConstants.PLACEHOLDER_FILENAME_EN))  {
			xxid = executableExample.getTestCaseFileName();
		}
		
		if (executionInfo.isXXIdAlreadyUsed(xxid))  {
			String message = BUNDLE.getString("Error") + ": " + BUNDLE.getString("Ambiguous") + " XXId: " + xxid;
			executableExample.addReportMessage(message);
			executionInfo.addTestMessagesFAILED(xxid, executableExample.getReportMessages(), executableExample.getBehaviorID());
			executableExample.terminateTestCase(message);
		}

		executableExample.setXXID( xxid );
		System.out.println((executionInfo.getTotalNumberOfXXs() + 1) + ". XXId: " + xxid);
		executionInfo.countAsExecuted(xxid, executableExample.getBehaviorID());
		executionInfo.countExistingXX();
		
		if ( ! executionInfo.isTestEnvironmentInitialized() ) 
		{
			executableExample.failWithMessage("Die Anwendung <b>" + executionInfo.getTestApplicationName() 
			                          + "</b> steht derzeit nicht zur Verfügung!");
		}
	}

	@LanguageTemplate(value = "Filter: ^^")
	@LanguageTemplate(value = "Ausführungsfilter: ^^")
	@LanguageTemplate(value = "Tags: ^^")
	public void defineAndCheckExecutionFilter(String executionFilterOfThisTestCase) 
	{
		boolean executeThisTest = true;
		final List<String> execFilterOfExecutableExample = executableExample.buildExecutionFilterList(executionFilterOfThisTestCase);
		execFilterOfExecutableExample.addAll(executableExample.getNlxxFilePathAsList());
		execFilterOfExecutableExample.forEach(possibleFilter -> executionInfo.addFilterToCollection(possibleFilter));
		final List<String> execFilterToExecute = executionInfo.getExecutionFilterList()
				                                              .stream().map(e -> SysNatStringUtil.replaceGermanUmlauts(e))
				                                              .collect(Collectors.toList());
		
		if (execFilterToExecute.size() > 1 || 
			(execFilterToExecute.size() == 1 && ! execFilterToExecute.get(0).equalsIgnoreCase(NO_FILTER))) 
		{
			executeThisTest = isThisTestToExecute(execFilterOfExecutableExample, execFilterToExecute);
		}
		
		if (! executeThisTest) {
			executionInfo.uncountAsExecuted(executableExample.getXXID().trim(), executableExample.getBehaviorID()); 
			throw new SkipTestCaseException(SkipReason.EXECUTION_FILTER);
		}
	}
	
	@LanguageTemplate(value = "Synonym: ^^")
	public void declareSynonym(String keyValuePair) 
	{
		String[] splitResult = keyValuePair.split("=");
		if (splitResult.length != 2) {
			executableExample.failWithMessage("Unable to parse Synonym instruction: <b>" + keyValuePair + "</b>.");
		}
		String alternative = splitResult[0].trim();
		String original = splitResult[1].trim();
		executableExample.getTestData().addSynonym(alternative, original);
		executableExample.addReportMessage(COMMENT_IDENTIFIER + "Synonym <b>" + keyValuePair + "</b> has been stored.");
	}
	
	@LanguageTemplate(value = "Aktiv: ^^")
	@LanguageTemplate(value = "Active: ^^")
	public void setActiveState(String value)  
	{
		if (value.trim().equalsIgnoreCase("nein") 
			|| value.trim().equalsIgnoreCase("no"))  
		{
			executionInfo.uncountAsExecuted(executableExample.getXXID(), executableExample.getBehaviorID());  // undo previously added 
			executionInfo.addInactiveXX( executableExample.getXXID(), executableExample.getBehaviorID() );
			throw new SkipTestCaseException(SkipReason.ACTIVATION_STATE);
		}
	}

	@LanguageTemplate(value = "Kommentar: ^^")
	@LanguageTemplate(value = "Comment: ^^")
	public void createComment(String comment) 
	{
		comment = checkForTestDataValue(comment);
		executableExample.addReportMessage(COMMENT_IDENTIFIER + comment);
	}

	@LanguageTemplate(value = "Test-Phase: ^^")
	public void startNewTestPhase(String phase)
	{
		if ( ! SysNatTestRuntimeUtil.isTestPhaseKeyword(phase) ) {
			throw new SysNatException("Unknown test phase <b>" + phase + "</b>.");
		}
		executableExample.addReportMessage(COMMENT_IDENTIFIER + phase);
	}

	@LanguageTemplate(value = "Wait ^^ second(s).") 
	@LanguageTemplate(value = "Wait ^^ seconds.") 
	@LanguageTemplate(value = "Warte ^^ Sekunden.")
	@LanguageTemplate(value = "Warte ^^ Sekunde.")
	@LanguageTemplate(value = "Warte ^^ Sekunde(n).")
	public void waitSeconds(String seconds) 
	{
		seconds = seconds.replace(',', '.');
		
		try {
			double sec = Double.valueOf(seconds);
			Thread.sleep((long)sec * 1000);
			executableExample.addReportMessage(BUNDLE.getString("WaitComment").replace("x", seconds));
		} catch (Exception e) {
			executableExample.addReportMessage(ERROR_KEYWORD + ": Die Testausführung hat nicht warten können.");
		}
	}


	@LanguageTemplate(value = "Date of today is stored as <>.")  
	@LanguageTemplate(value = "Das heutige Datum wird als <> festgehalten.")
	public String storeTestObject_TodayDateAsString() {
		String toReturn = executionInfo.getTodayDateAsString();
		executableExample.addReportMessage("The current date (<b>" + toReturn + "</b>) has been stored as <today>.");
		return toReturn;
	}

	@LanguageTemplate(value = "Beep ^^ times.") 
	@LanguageTemplate(value = "Piepse ^^ mal.")
	public void beep(int timesToBeep) 
	{
		for (int i = 0; i < timesToBeep; i++) {
			Toolkit.getDefaultToolkit().beep();
			executableExample.sleep(200);
		}
	}
	
	/**
	 * Makes a single data set available.
	 * If not yet done, it is initialized now.
	 * Test data is read from the TestData directory
	 * @param dataReference
	 * @return
	 */
	@LanguageTemplate(value = "There are ^^.") 
	@LanguageTemplate(value = "Es existieren ^^.")
	@LanguageTemplate(value = "Testdaten: ^^")
	@LanguageTemplate(value = "TestData: ^^")
	public void setTestData(String dataReference)  
	{
		final List<String> datatypesList = getObjectNameList(dataReference);
		for (String datatype : datatypesList) 
		{
			if (datatype.contains("=")) {
				String[] splitResult = dataReference.split("=");
				setSingleTestDataValue(splitResult[0].trim(), splitResult[1].trim());
			} 
			else if (executableExample.getTestData().isKnown(datatype) ) {
				executableExample.getTestData().setMarker(executableExample.getTestData().getDataSet(datatype));
			} else {
				if (alreadyLoadedDataFiles.contains(datatype)) {
					return;
				}
				executableExample.importTestData(datatype);
				alreadyLoadedDataFiles.add(datatype);
			}
		}		
	}	

	@LanguageTemplate(value = "Execute with ^^") 
	@LanguageTemplate(value = "Führe mit ^^")
	public void loadTestData(String datfileNames) 
	{
		final List<String> datatypesList = getObjectNameList(datfileNames);
		for (String datatype : datatypesList) {
			executableExample.importTestData(datatype);
		}
	}	

	@LanguageTemplate(value = "Execute with") 
	@LanguageTemplate(value = "Führe mit")
	public void initDefaultTestDataset() 
	{
		executableExample.getTestData().clear();
		executableExample.getTestData().addEmptyDataset(SysNatTestData.SINGLE_TEST_DATA_VALUES);
	}	

	/**
	 * Used to override or extent data sets provided in testDataSets.
	 * @param objectAndfieldName
	 * @param value
	 */
	@LanguageTemplate(value = "^^=^^") 
	@LanguageTemplate(value = "^^ = ^^") 
	@LanguageTemplate(value = "^^ =^^") 
	@LanguageTemplate(value = "^^= ^^") 
	public void setSingleTestDataValue(String objectAndfieldName, String value) 
	{
		String[] splitResult = objectAndfieldName.split("\\.");
		
		if (splitResult.length == 1) {
			executableExample.getTestData().addValue(splitResult[0], value);
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

	@LanguageTemplate(value = "Execute script ^^ with input data ^^.")  
	@LanguageTemplate(value = "Führe das Skript ^^ aus mit den Eingabedaten ^^.")
	@LanguageTemplate(value = "^^ mit den Eingabedaten ^^.")  
	@LanguageTemplate(value = "^^ with input data ^^.")  
	public void executeScriptWithDataset(String scriptName, String datasetNameCandidate) 
	{
		if (executableExample.getTestData().findMatchingDataSet(datasetNameCandidate) == null) {
			executableExample.importTestData(datasetNameCandidate);
		};
		executableExample.getTestData().setMarker( executableExample.getTestData().findMatchingDataSet(datasetNameCandidate) );
		executeScript(scriptName);
	}	

	@LanguageTemplate(value = "Execute script ^^ with input data ^^ and")  
	@LanguageTemplate(value = "Führe das Skript ^^ aus mit den Eingabedaten ^^ und")
	@LanguageTemplate(value = "^^ mit den Eingabedaten ^^ und")  
	@LanguageTemplate(value = "^^ with input data ^^ and")  
	public void setScriptNameAndDataset(String scriptName, String datasetNameCandidate) {
		if (executableExample.getTestData().findMatchingDataSet(datasetNameCandidate) == null) {
			executableExample.importTestData(datasetNameCandidate);
		};
		// do not set a dataset-marker here so that additional data values can override those of the dataset defined here.
		executableExample.setScriptToExecute(scriptName);
	}	
	
	
	@LanguageTemplate(value = "Execute script ^^ with")  
	@LanguageTemplate(value = "Execute script ^^ with input data")  
	@LanguageTemplate(value = "^^ with input data")  
	@LanguageTemplate(value = "^^ with")  
	@LanguageTemplate(value = "Führe das Skript ^^ aus mit")
	@LanguageTemplate(value = "Führe das Skript ^^ aus mit den Eingabedaten")
	@LanguageTemplate(value = "^^ mit den Eingabedaten")  
	@LanguageTemplate(value = "^^ mit")  
	public void setScriptName(String scriptName) {
		executableExample.getTestData().initDefaultDataset();
		executableExample.setScriptToExecute(scriptName);
	}	
	
	@LanguageTemplate(value = ".")  
	public void executeScript() 
	{
		final String scriptName = executableExample.getScriptToExecute();
		
		if (scriptName == null) {
			throw new SysNatException(BUNDLE.getString("NoScriptNameDefined"));
		}
		
		executeScript( scriptName );
	}	
	
	@LanguageTemplate(value = "Save screenshot now as ^^.") 
	@LanguageTemplate(value = "Speichere jetzt einen Screenshot unter ^^.")
	public void saveScreenshotWithName(String filename) 
	{
		filename = filename.replace("<XXId>", executableExample.getXXID());
		executableExample.takeScreenshot(filename);
		executableExample.addReportMessage(BUNDLE.getString("ScreenshotSavedComment").replace("x", filename));
	}

	@LanguageTemplate(value = "Take a screenshoot as picture proof now if test case is categorized as mitBildnachweis.")
	@LanguageTemplate(value = "Speichere jetzt einen Screenshot als Bildnachweis, falls der Testfall als pictureProof kategorisiert wurde.")
	public void savePictureProofIfNecessary() 
	{
		if (executableExample.hasBildnachweisCategory()) {			
			takeScreenshotNow( executableExample.getPictureProofName() );
		}
	}
	
	@LanguageTemplate(value = "Save a screenshoot as picture proof.")
	@LanguageTemplate(value = "Speichere einen Screenshot als Bildnachweis.")
	public void savePictureProof() 
	{
		takeScreenshotNow( executableExample.getPictureProofName() );
	}

	@LanguageTemplate(value = "Take a screenshoot now and save with name ^^.")
	@LanguageTemplate(value = "Erzeuge einen Screenshot und speichere ihn unter den Namen ^^.")
	public void takeScreenshotNow(String screenshotName) {
		executableExample.takeScreenshot(screenshotName);
		executableExample.addCommentToReport(BUNDLE.getString("ScreenshotSavedComment").replace("x", screenshotName));
	}
	
	
	/**
	 * Marker method to manage parameterized tests.
	 */
	@LanguageTemplate(value = "Test-Parameter: ^^")
	public void applyTestParameter(String param) {
	    // needs no implementation because this method call will be replaced !
	}

	@LanguageTemplate(value = "Set BDD-Keyword ^^.")
	public void setBddKeyword(String aKeyword) {
		executableExample.setBddKeyword(aKeyword);
	}
	
	/**
	 * Performs a series of clicks in the menu of an application.
	 * @param menuPath names of menu items separated by the '/'-symbol.
	 */
	@LanguageTemplate(value = "Öffne den Menüpfad ^^.")
	public void openMenuPath(String menuPath) 
	{
		String[] splitResult = menuPath.split("/");
		for (String menuItem : splitResult) {
			executableExample.clickMenuHeader(menuItem);
		}
		executableExample.addReportMessage("Der Menüpfad <b>" + menuPath + "</b> wurde geöffnet.");
	}	
	
	@LanguageTemplate(value = "Env: ^^ = ^^.")
	public void setTestDataEnvSpecific(String key, String value) {
		executableExample.getTestData().addValue(key, value);
	}		
	
	@LanguageTemplate(value = "Der Wert ^^ wird als <> festgehalten.")
	public String storeTestObject(String valueCandidate)
	{
		final String value = executableExample.getTestDataValue(valueCandidate);
		return value;  // storing is done automatically by the return value !
	}

	@LanguageTemplate(value = "Drücke die Tastenkombination ^^.")
	@LanguageTemplate(value = "Press keysDrücke die Tastenkombination ^^.")
	public void pressKeys(String keyCombination)
	{
		String keys = keyCombination.replaceAll(" ", "");
		if ("AltShiftTab".equalsIgnoreCase(keys)) {
			executableExample.getActiveGuiController().pressAltShiftTab();
		} else if ("AltTab".equalsIgnoreCase(keys)) {
			executableExample.getActiveGuiController().pressAltTab();
		} else if ("Tab".equalsIgnoreCase(keys)) {
			executableExample.getActiveGuiController().pressTab();
		} else if ("AltF4".equalsIgnoreCase(keys)) {
			executableExample.getActiveGuiController().pressFunctionKey("F4", true, false);
		} else if ("Enter".equalsIgnoreCase(keys)) {
			executableExample.getActiveGuiController().pressEnter();
		} else if (keys.toUpperCase().startsWith("ALT")) {
			char key = keys.charAt(3); 
			executableExample.getActiveGuiController().pressAltWith(key);
		} else if (keys.toUpperCase().startsWith("STRG")) {
			char key = keys.charAt(4); 
			executableExample.getActiveGuiController().pressStrgWith(key);
		} else if (keys.toUpperCase().startsWith("ALTSTRG") || keys.toUpperCase().startsWith("STRGALT")) {
			char key = keys.charAt(7); 
			executableExample.getActiveGuiController().pressStrgAltWith(key);
		} else if (keys.length() == 1) {
			executableExample.getActiveGuiController().pressKey(keys.charAt(0));
		} else {
			executableExample.addCommentToReport("Die Tastenkombination <b>" + keyCombination + "</b> wird noch nicht unterstützt.");
		}
		
		executableExample.addReportMessage("Die Tastenkombination <b>" + keyCombination + "</b> wurde gedrückt.");
	}

	@LanguageTemplate(value = "Klicke auf Bildschirmposition ^^/^^.")
	public void clickOnScreenCoordinate(String xPos, String yPos)
	{
		int x = toInteger(xPos, "X-Koordinate");
		int y = toInteger(yPos, "Y-Koordinate");
		
		executableExample.getActiveGuiController().clickOnScreenCoordinate(x,y);
		executableExample.addReportMessage("Es wurde auf die Bildschirmposition<b>" + x + "/" + y + "</b> geklickt.");
	}
	
		
	@LanguageTemplate(value = "Minimiere das Anwendungsfenster.")
	public void minimizeWindow()
	{
		executableExample.getActiveGuiController().minimizeWindow();
		executableExample.addReportMessage("Das Anwendungsfenster wurde minimiert.");
	}

	@LanguageTemplate(value = "Maximiere das Anwendungsfenster.")
	public void maximizeWindow()
	{
		executableExample.getActiveGuiController().maximizeWindow();
		executableExample.addReportMessage("Das Anwendungsfenster wurde maximiert.");
	}

	@LanguageTemplate(value = "Prüfe, ob das Verzeichnis ^^ zur Verfügung steht.")
	public void checkDir(String dir)
	{
		File file = new File(dir);
		final String question = "Does directory <b>" + dir + "</b> exist" + QUESTION_IDENTIFIER;
		executableExample.answerQuestion(question, 	! file.exists());	
	}
	
	@LanguageTemplate(value = "Aus ^^ und '' wird <>.")
	@LanguageTemplate(value = "Concat ^^ and '' to <>.")
	public String concatStrings1(String s1, String s2){
		return s1 + s2;
	}
	
	@LanguageTemplate(value = "Aus '' und ^^ wird <>.")
	@LanguageTemplate(value = "Concat '' and ^^ to <>.")
	public String concatStrings2(String s1, String s2){
		return s1 + s2;
	}
	
	@LanguageTemplate(value = "Aus '' und '' wird <>.")
	@LanguageTemplate(value = "Concat '' and '' to <>.")
	public String concatStrings3(String s1, String s2){
		return s1 + s2;
	}
	
	@LanguageTemplate(value = "Aus ^^ und '' und ^^ wird <>.")
	@LanguageTemplate(value = "Concat ^^ and '' und ^^ wird <>.")
	public String concatStrings4(String s1, String s2, String s3){
		return s1 + s2 + s3;
	}

	public void gotoStartPage()
	{
		// TODO Auto-generated method stub
		
	}
}