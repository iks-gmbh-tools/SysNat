package com.iksgmbh.sysnat.helper.generator;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.common.utils.SysNatConstants.GuiType;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.helper.generator.PageObjectDevGen.PageChangeElement;
import com.iksgmbh.sysnat.helper.generator.PageObjectDevGen.PageChangeElementBuilder;
import com.iksgmbh.sysnat.helper.generator.utils.SysNatDevGenTestUtil;
import com.iksgmbh.sysnat.language_templates.LanguageTemplateBasics.PageChangeEvent.EventType;

public class PageObjectDevGenClassLevelTest
{
	@Before
	public void setup() {
		PageObjectDevGen.languageTemplatesContainerParentDir = "../sysnat.natural.language.executable.examples\\src\\test\\resources\\PageObjectGeneration/";
	}
	
	@Test
	public void generatesPageObject_WithoutPageChangeEvents()
	{
		// arrange
		File newPageObjectJavaFile = new File(PageObjectDevGen. languageTemplatesContainerParentDir, "unittestapp/pageobject/NewPageNamePageObject.java");
		newPageObjectJavaFile.delete();
		assertFalse(newPageObjectJavaFile.exists());
		File languageTemplateJavaFile = new File(PageObjectDevGen.languageTemplatesContainerParentDir, "unittestapp/LanguageTemplatesBasics_UnitTestApp.java");
		String newPageName = "newPageName";
		
		// act
		PageObjectDevGen.doYourJob("UnitTestApp", newPageName, null, null);

		// store test data
		List<String> actualPageObjectLines = SysNatFileUtil.readTextFile(newPageObjectJavaFile);
		List<String> actuallanguageTemplateLines = SysNatFileUtil.readTextFile(languageTemplateJavaFile);
		
		// cleanup
		newPageObjectJavaFile.delete();
		cleanLinesFrom(languageTemplateJavaFile, newPageName);
		
		// assert
		SysNatDevGenTestUtil.assertFileContent(actualPageObjectLines, "../PageObjectGeneration/expected/NewPageNamePageObject.java");
		SysNatDevGenTestUtil.assertFileContent(actuallanguageTemplateLines, "../PageObjectGeneration/expected/LanguageTemplatesBasics_UnitTestApp.java");
	}

	@Test
	public void generatesPageObject_With_Wait_Condition_Millis()
	{
		// arrange
		File newPageObjectJavaFile = new File(PageObjectDevGen. languageTemplatesContainerParentDir, "unittestapp/pageobject/NewPageNamePageObject.java");
		newPageObjectJavaFile.delete();
		assertFalse(newPageObjectJavaFile.exists());
		File languageTemplateJavaFile = new File(PageObjectDevGen.languageTemplatesContainerParentDir, "unittestapp/LanguageTemplatesBasics_UnitTestApp.java");
		String newPageName = "newPageName";
		PageChangeElement pageChangeElement1 = new PageChangeElementBuilder().from(newPageName).via(EventType.ButtonClick).on("aButton").to("Main").waiting("1000").build();
		PageChangeElement pageChangeElement2 = new PageChangeElementBuilder().from("Main").via(EventType.ButtonClick).on("anotherButton").to(newPageName).build();
		PageChangeElement[] pageChangeElements = {pageChangeElement1,pageChangeElement2};
		
		// act
		PageObjectDevGen.doYourJob("UnitTestApp", newPageName, pageChangeElements, null);

		// store test data
		List<String> actualPageObjectLines = SysNatFileUtil.readTextFile(newPageObjectJavaFile);
		
		// cleanup
		newPageObjectJavaFile.delete();
		cleanLinesFrom(languageTemplateJavaFile, newPageName);
		
		// assert
		SysNatDevGenTestUtil.assertFileContent(actualPageObjectLines, "../PageObjectGeneration/expected/NewPageNamePageObject.java");
	}

	@Test
	public void generatesPageObject_With_Wait_Condition_WaitOnElement()
	{
		// arrange
		File newPageObjectJavaFile = new File(PageObjectDevGen. languageTemplatesContainerParentDir, "unittestapp/pageobject/NewPageNamePageObject.java");
		newPageObjectJavaFile.delete();
		assertFalse(newPageObjectJavaFile.exists());
		File languageTemplateJavaFile = new File(PageObjectDevGen.languageTemplatesContainerParentDir, "unittestapp/LanguageTemplatesBasics_UnitTestApp.java");
		String newPageName = "newPageName";
		PageChangeElement pageChangeElement1 = new PageChangeElementBuilder().from(newPageName).via(EventType.ButtonClick).on("aButton").to("Main").build();
		PageChangeElement pageChangeElement2 = new PageChangeElementBuilder().from("Main").via(EventType.ButtonClick).on("anotherButton").to(newPageName).waiting("aLabel").build();
		PageChangeElement[] pageChangeElements = {pageChangeElement1,pageChangeElement2};
		
		// act
		PageObjectDevGen.doYourJob("UnitTestApp", newPageName, pageChangeElements, null);

		// store test data
		List<String> actuallanguageTemplateLines = SysNatFileUtil.readTextFile(languageTemplateJavaFile);
		
		// cleanup
		newPageObjectJavaFile.delete();
		cleanLinesFrom(languageTemplateJavaFile, newPageName);
		
		// assert
		SysNatDevGenTestUtil.assertFileContent(actuallanguageTemplateLines, "../PageObjectGeneration/expected/LanguageTemplatesBasics_UnitTestApp3.java");
	}

	@Test
	public void generatesPageObject_With_predefinedGuiElements()
	{
		// arrange
		File newPageObjectJavaFile = new File(PageObjectDevGen. languageTemplatesContainerParentDir, "unittestapp/pageobject/NewPageNamePageObject.java");
		newPageObjectJavaFile.delete();
		assertFalse(newPageObjectJavaFile.exists());
		File languageTemplateJavaFile = new File(PageObjectDevGen.languageTemplatesContainerParentDir, "unittestapp/LanguageTemplatesBasics_UnitTestApp.java");
		String newPageName = "newPageName";
		PageChangeElement pageChangeElement1 = new PageChangeElementBuilder().via(EventType.MenuItemClick).on("aMenu").to("Main").build();
		PageChangeElement[] pageChangeElements = {pageChangeElement1};
		
		HashMap<GuiType, HashMap<String, String>> predefinedGuiElements = new HashMap<>();
		HashMap<String, String> prededinedButtons = new HashMap<>();
		prededinedButtons.put("TextOfLoginButton", "TechIdOfLoginButton");
		predefinedGuiElements.put(GuiType.Button, prededinedButtons);
		
		HashMap<String, String> prededinedTextfields = new HashMap<>();
		prededinedTextfields.put("GuiNameOfLoginUserField", "TechIdOfLoginUserField");
		prededinedTextfields.put("GuiNameOfLoginPasswordField", "TechIdOfLoginPasswordField");
		predefinedGuiElements.put(GuiType.TextField, prededinedTextfields);
		
		
		// act
		PageObjectDevGen.doYourJob("UnitTestApp", newPageName, pageChangeElements, predefinedGuiElements);

		// store test data
		List<String> actualPageObjectLines = SysNatFileUtil.readTextFile(newPageObjectJavaFile);
		List<String> actuallanguageTemplateLines = SysNatFileUtil.readTextFile(languageTemplateJavaFile);
		
		// cleanup
		newPageObjectJavaFile.delete();
		cleanLinesFrom(languageTemplateJavaFile, newPageName);
		
		// assert
		SysNatDevGenTestUtil.assertFileContent(actualPageObjectLines, "../PageObjectGeneration/expected/NewPageNamePageObject4.java");
		SysNatDevGenTestUtil.assertFileContent(actuallanguageTemplateLines, "../PageObjectGeneration/expected/LanguageTemplatesBasics_UnitTestApp4.java");
	}
	
	
	// ##################################################################################################################################


	private void cleanLinesFrom(File languageTemplateJavaFile, String newPageName)
	{
		List<String> content = SysNatFileUtil.readTextFile(languageTemplateJavaFile);
		List<String> newContent = new ArrayList<>();
		content.stream().filter(line -> ! line.contains(newPageName))
		                .filter(line -> ! line.contains("\"aMenu\""))
		                .forEach(line -> newContent.add(line));
		languageTemplateJavaFile.delete();
		SysNatFileUtil.writeFile(languageTemplateJavaFile, newContent);		
	}

}
