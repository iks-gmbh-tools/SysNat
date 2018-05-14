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
 * German variant of the basic language templates. 
 * LanguageTemplate-anntated template methods all deligate to the parent implementation.
 * Non-syntax-method are not supposed be located here.
 */
public class LanguageTemplatesBasics_de extends LanguageTemplatesBasics
{	

	public LanguageTemplatesBasics_de(TestCase aTestCase) 
	{
		super(aTestCase, ResourceBundle.getBundle("LanguageTemplatesBasics", Locale.GERMAN));
	}

	@LanguageTemplate(value = "Test-ID: #1")
	public void startNewTestCase(String testID) {
		super.startNewTestCase(testID);
	}

	@LanguageTemplate(value = "Kategorie-Filter: #1")
	public void checkFilterCategory(String testCategoriesOfThisTestCase) {
		super.checkFilterCategory(testCategoriesOfThisTestCase);
	}

	@LanguageTemplate(value = "Aktiv: #1")
	public void setActiv(String value) {
		setActiveState(value);
	}

	@LanguageTemplate(value = "Kommentar: #1")
	public void createComment(String comment) {
		super.createComment(comment);
	}

	@LanguageTemplate(value = "Schlüsselkommentar: Test-Vorbereitungen")
	public void createKeywordArrangeComment() {
		super.createKeywordArrangeComment();
	}
	
	@LanguageTemplate(value = "Schlüsselkommentar: Test-Durchführung")
	public void createKeywordActComment() {
		super.createKeywordActComment();
	}

	@LanguageTemplate(value = "Schlüsselkommentar: Überprüfung der Testergebnisse")
	public void createAssertKeywordComment() {
		super.createAssertKeywordComment();
	}

	@LanguageTemplate(value = "Schlüsselkommentar: Wiederherstellen der Ausgangssituation")
	public void createCleanupKeywordComment() {
		super.createCleanupKeywordComment();
	}

	@LanguageTemplate(value = "Warte #1 Sekunde(n).")
	public void waitSeconds(String seconds)	{
		super.waitSeconds(seconds);
	}

	@LanguageTemplate(value = "Das heutige Datum wird als #1 festgehalten.")
	public DateValue returnTodayDateAsString(final String dateName) {
		return super.returnTodayDateAsString(dateName);
	}

	@LanguageTemplate(value = "Piepse #1 mal.")
	public void beep(int timesToBeep) {
		super.beep(timesToBeep);
	}

	@LanguageTemplate(value = "Es existieren #1 .")
	public ObjectData getDatasetObject(String objectName) {
		return super.getDatasetObject(objectName);
	}	

	@LanguageTemplate(value = "Führe mit folgenden Daten")
	public SysNatTestData createSysNatTestData() {
		return super.createSysNatTestData();
	}

	@LanguageTemplate(value = "Führe mit #1")
	public SysNatTestData createSysNatTestData(String objectNames) {
		return super.createSysNatTestData(objectNames);
	}

	@LanguageTemplate(value = "Führe für alle #1")
	public SysNatTestData loadDataSets(String datasetType) {
		return loadDataSets(datasetType);
	}
	
	@LanguageTemplate(value = "#1 = #2")
	public void setSingleTestDataValue(String objectAndfieldName, String value)	{
		super.setSingleTestDataValue(objectAndfieldName, value);
	}
	
	@LanguageTemplate(value = "das Skript #1 aus.")
	public void executeScriptWithData(String scriptName) {
		super.executeScriptWithData(scriptName);
	}
	
	@LanguageTemplate(value = "the script #1 as separate testcase.")
	public void theScriptAsSeparateTestcase(String scriptName) {
		super.executeTheScriptAsSeparateTestcase(scriptName);
	}

	@LanguageTemplate(value = "Führe das Skript #1 aus.")
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
