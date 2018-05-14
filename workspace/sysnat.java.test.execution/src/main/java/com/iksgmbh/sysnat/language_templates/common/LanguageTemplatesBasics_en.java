package com.iksgmbh.sysnat.language_templates.common;

import java.util.Locale;
import java.util.ResourceBundle;

import com.iksgmbh.sysnat.LanguageTemplatesBasics;
import com.iksgmbh.sysnat.TestCase;
import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.domain.DateValue;
import com.iksgmbh.sysnat.domain.SysNatTestData;
import com.iksgmbh.sysnat.domain.SysNatTestData.ObjectData;

/**
 * English variant of the basic language templates.
 * LanguageTemplate-anntated template methods all deligate to the parent implementation.
 * Non-syntax-method are not supposed be located here.
 */
public class LanguageTemplatesBasics_en extends LanguageTemplatesBasics
{	

	public LanguageTemplatesBasics_en(TestCase aTestCase) 
	{
		super(aTestCase, ResourceBundle.getBundle("LanguageTemplatesBasics", Locale.ENGLISH));
	}

	@LanguageTemplate(value = "Test-ID: #1")
	public void startNewTestCase(String testID) {
		super.startNewTestCase(testID);
	}

	@LanguageTemplate(value = "Category-Filter: #1")
	public void checkFilterCategory(String testCategoriesOfThisTestCase) {
		super.checkFilterCategory(testCategoriesOfThisTestCase);
	}

	@LanguageTemplate(value = "Aktive: #1")
	public void setActiv(String value) {
		setActiveState(value);
	}

	@LanguageTemplate(value = "Comment: #1")
	public void createComment(String comment) {
		super.createComment(comment);
	}

	@LanguageTemplate(value = "Keyword-Comment: Arrange test requirements")
	public void createKeywordArrangeComment() {
		super.createKeywordArrangeComment();
	}
	
	@LanguageTemplate(value = "Keyword-Comment: Perform action under test")
	public void createKeywordActComment() {
		super.createKeywordActComment();
	}

	@LanguageTemplate(value = "Keyword-Comment: Assert expected results")
	public void createAssertKeywordComment() {
		super.createAssertKeywordComment();
	}

	@LanguageTemplate(value = "Keyword-Comment: Reset to start situation")
	public void createCleanupKeywordComment() {
		super.createCleanupKeywordComment();
	}

	@LanguageTemplate(value = "Wait #1 second(s).")
	public void waitSeconds(String seconds)	{
		super.waitSeconds(seconds);
	}

	@LanguageTemplate(value = "Today's date is stored as #1 .")
	public DateValue returnTodayDateAsString(final String dateName) {
		return super.returnTodayDateAsString(dateName);
	}

	@LanguageTemplate(value = "Beep #1 times.")
	public void beep(int timesToBeep) {
		super.beep(timesToBeep);
	}

	@LanguageTemplate(value = "There are #1 .")
	public ObjectData getDatasetObject(String objectName) {
		return super.getDatasetObject(objectName);
	}	

	@LanguageTemplate(value = "Execute with following data")
	public SysNatTestData createSysNatTestData() {
		return super.createSysNatTestData();
	}

	@LanguageTemplate(value = "Execute with #1")
	public SysNatTestData createSysNatTestData(String objectNames) {
		return super.createSysNatTestData(objectNames);
	}

	@LanguageTemplate(value = "Execute for all #1")
	public SysNatTestData loadDataSets(String datasetType) {
		return super.loadDataSets(datasetType);
	}
	
	@LanguageTemplate(value = "#1 = #2")
	public void setSingleTestDataValue(String objectAndfieldName, String value)	{
		super.setSingleTestDataValue(objectAndfieldName, value);
	}
	
	@LanguageTemplate(value = "the script #1 .")
	public void executeScriptWithData(String scriptName) {
		super.executeScriptWithData(scriptName);
	}
	
	@LanguageTemplate(value = "the script #1 as separate testcases.")
	public void theScriptAsSeparateTestcase(String scriptName) {
		super.executeTheScriptAsSeparateTestcase(scriptName);
	}

	@LanguageTemplate(value = "Execute script #1 .")
	public void executeScript(String scriptName) {
		super.executeScript(scriptName, testCase);
	}

	@LanguageTemplate(value = "Ein Screenshot wird als #1 gespeichert.")
	public void saveScreenshotWithName(String fileName) {
		super.saveScreenshotWithName(fileName);
	}

	@LanguageTemplate(value = "Ein Screenshot wird gespeichert, falls der Testfall als 'mitBildnachweis' kategorisiert wurde.")
	public void savePictureProofIfNecessary() {
		super.savePictureProofIfNecessary();
	}

}
