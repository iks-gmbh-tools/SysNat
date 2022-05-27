package com.iksgmbh.sysnat.helper.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import com.iksgmbh.sysnat.common.utils.SysNatConstants.GuiType;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.language_templates.LanguageTemplateBasics.PageChangeEvent;
import com.iksgmbh.sysnat.language_templates.LanguageTemplateBasics.PageChangeEvent.EventType;

/**
 * Development helper to generate new page object.
 * 
 * Adapt the value of the constants to your needs and execute this class as Java application.
 * 
 * Note: Java and Properties files that exist will not be overwritten!
 * 
 * @author Reik Oberrath
 */
public class PageObjectDevGen
{
	protected static String languageTemplatesContainerParentDir = "../sysnat.test.runtime.environment/src/main/java/com/iksgmbh/sysnat/language_templates/";
	
	private static final String TestApp = "Ikaros";
	private static final String PageObject = "AktenUebersicht";
	private static final PageChangeElement[] PAGE_CHANGE_ELEMENTS = { 
		new PageChangeElementBuilder().from("main").via(EventType.MenuItemClick).on("Akten").to("this").waiting("1000").build(),
		new PageChangeElementBuilder().from("this").via(EventType.ButtonClick).on("Neu").to("main").waiting("guielement").build()
	};
	
	public static void main(String[] args) {
		System.out.println("PageObjectGenerator Result: " + doYourJob(TestApp, PageObject, PAGE_CHANGE_ELEMENTS, null));
	}

	public static String doYourJob(String testappName, String pageobjectName, PageChangeElement[] pageChangeElements, HashMap<GuiType, HashMap<String, String>> idMappings)
	{
		PageObjectDevGen instance = new PageObjectDevGen();
		boolean ok = instance.createPageObjectFile(testappName, pageobjectName, idMappings);
		if (ok) ok = instance.introducePageObjectIntoLanguageTemplateContainer(testappName, pageobjectName, pageChangeElements);
		return "" + ok;
	}

	private boolean createPageObjectFile(String testappName, String pageobjectName, HashMap<GuiType, HashMap<String, String>> idMappings)
	{
		File folder = new File(languageTemplatesContainerParentDir + testappName.toLowerCase());
		if (! folder.exists()) {
			System.err.println("Folder does not exist: " + folder.getAbsolutePath());
			return false;
		}
		folder = new File(folder.getAbsolutePath() + "/pageobject");
		if (! folder.exists()) {
			System.err.println("Folder does not exist: " + folder.getAbsolutePath());
			return false;
		}
		File pageObjectFile = new File(folder.getAbsolutePath(), standardizePageObjectName(pageobjectName) + "PageObject.java");
		SysNatFileUtil.writeFile(pageObjectFile, createPageObjectFileContent(testappName, pageobjectName, idMappings));
		
		return pageObjectFile.exists();
	}

	private String standardizePageObjectName(String pageobjectName) {
		return checkSuffix(SysNatStringUtil.firstCharToUpperCase(pageobjectName));
	}

	private List<String> createPageObjectFileContent(String testappName, String pageobjectName, HashMap<GuiType, HashMap<String, String>> idMappings)
	{
		List<String> content = new ArrayList<>();
		
		content.add("/*");
		content.add(" * Copyright 2018 IKS Gesellschaft fuer Informations- und Kommunikationssysteme mbH");
		content.add(" * ");
		content.add(" * Licensed under the Apache License, Version 2.0 (the \"License\");");
		content.add(" * you may not use this file except in compliance with the License.");
		content.add(" * You may obtain a copy of the License at");
		content.add(" * ");
		content.add(" *     http://www.apache.org/licenses/LICENSE-2.0");
		content.add(" * ");
		content.add(" * Unless required by applicable law or agreed to in writing, software");
		content.add(" * distributed under the License is distributed on an \"AS IS\" BASIS,");
		content.add(" * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.");
		content.add(" * See the License for the specific language governing permissions and");
		content.add(" * limitations under the License.");
		content.add(" */");
		content.add("package com.iksgmbh.sysnat.language_templates." + testappName.toLowerCase() + ".pageobject;");
		content.add("");
		content.add("import java.util.*;");
		content.add("");
		content.add("import com.iksgmbh.sysnat.ExecutableExample;");
		content.add("import com.iksgmbh.sysnat.common.utils.SysNatConstants.GuiType;");
		content.add("import com.iksgmbh.sysnat.guicontrol.impl.*;");
		content.add("import com.iksgmbh.sysnat.language_templates.*;");
		content.add("");
		content.add("/**");
		content.add(" * Implements actions that can be applied to this page.");
		content.add(" * Some standard actions are available from the parent class.");
		content.add(" * To use them, the idMappings must be defined.");
		content.add(" */");
		content.add("");
		content.add("public class " + standardizePageObjectName(pageobjectName) + "PageObject extends PageObject");
		content.add("{");
		content.add("	public " + standardizePageObjectName(pageobjectName) + "PageObject(ExecutableExample executableExample, LanguageTemplateBasics aLanguageTemplateBasics)");
		content.add("	{");
		content.add("		super(aLanguageTemplateBasics);");
		content.add("		this.executableExample = executableExample;");
		content.add("		this.idMappingCollection = new HashMap<GuiType, HashMap<String, List<String>>>();");
		content.add("");
		content.add("		HashMap<String, List<String>> idMappings = new HashMap<String, List<String>>();");
		
		if (idMappings != null && ! idMappings.isEmpty()) 
		{
			HashMap<String, String> textfields = idMappings.get(GuiType.TextField);
			textfields.keySet().forEach(key -> content.add(toMappingLine(key, textfields.get(key))));
		}

		
		content.add("		idMappingCollection.put(GuiType.TextField, idMappings);		");
		content.add("");
		content.add("		idMappings = new HashMap<String, List<String>>();");
		
		if (idMappings != null && ! idMappings.isEmpty()) 
		{
			HashMap<String, String> buttons = idMappings.get(GuiType.Button);
			buttons.keySet().forEach(key -> content.add(toMappingLine(key, buttons.get(key))));
		}
		
		content.add("		idMappingCollection.put(GuiType.Button, idMappings);");
		content.add("	}");
		content.add("");
		content.add("	@Override");
		content.add("	public String getPageName() {");
		content.add("		return getClass().getSimpleName().replaceAll(\"Object\", \"\");");
		content.add("	}");
		content.add("");
		content.add("	@Override");
		content.add("	public boolean isCurrentlyDisplayed() {");
		
		if (idMappings != null && idMappings.get(GuiType.Button) != null && idMappings.get(GuiType.Button).get("Login") != null) {
			content.add("		return executableExample.getActiveGuiController().isElementAvailable(\"" 
		                + idMappings.get(GuiType.Button).get("Login") + "\", 500, true);");
		} else {
			content.add("	    return true;");
			content.add("	    // String uniqueComponentTechnicalId = \"?\";  TODO specify unique component of this page");
			content.add("		// return executableExample.getActiveGuiController().isElementAvailable(uniqueComponentTechnicalId, 500, true);");
		}
		
		content.add("	}");
		content.add("}");
				
		return content;
	}
	

	private String toMappingLine(String guiName, String techId) {
		return "		idMappings.put(\"" + guiName + "\", createList(\"" + techId + "\"));";
	}

	private boolean introducePageObjectIntoLanguageTemplateContainer(String testappName, String pageobjectName, PageChangeElement[] pageChangeElements)
	{
		File folder = new File(languageTemplatesContainerParentDir + testappName.toLowerCase());
		File languageTemplateContainerFile = new File(folder.getAbsolutePath(), "LanguageTemplatesBasics_" + testappName + ".java");
		if (! languageTemplateContainerFile.exists()) {
			System.err.println("LanguageTemplateContainerFile does not exist: " + languageTemplateContainerFile.getAbsolutePath());
			return false;
		}
		
		if (pageobjectName.equals("Main")) return true;
		
		
		List<String> content = SysNatFileUtil.readTextFile(languageTemplateContainerFile);
		boolean ok = languageTemplateContainerFile.delete();
		
		if (ok) {
			SysNatFileUtil.writeFile(languageTemplateContainerFile, introducePageObjectIntoLanguageTemplateContainer(content, testappName, pageobjectName, pageChangeElements));
		} else {
			System.err.println("File could not be overwritten: " + languageTemplateContainerFile.getAbsolutePath());
		}
		
		return ok;
	}

	private List<String> introducePageObjectIntoLanguageTemplateContainer(List<String> content, String testappName, String pageobjectName, PageChangeElement[] pageChangeElements)
	{
		List<String> toReturn = new ArrayList<>();
		if (pageobjectName.equals("Main")) return toReturn;
		pageobjectName = standardizePageObjectName(pageobjectName); 
		String varName = pageobjectName.substring(0, 1).toLowerCase() + pageobjectName.substring(1); 
		boolean firstImportPageObjectLinePassed = false;
		boolean lastImportPageObjectLinePassed = false;
		boolean firstCreateAndRegisterLinePassed = false;
		boolean lastCreateAndRegisterLinePassed = false;
		boolean firstPageObjectFieldDefinitionLinePassed = false;
		boolean lastPageObjectFieldDefinitionLinePassed = false;
		
		for (String line : content) 
		{
			if (line.trim().equals("import com.iksgmbh.sysnat.language_templates." + testappName.toLowerCase() + ".pageobject.MainPageObject;")) {
				toReturn.add("import com.iksgmbh.sysnat.language_templates." + testappName.toLowerCase() + ".pageobject." + pageobjectName + "PageObject;");
			}
			else if (! line.contains("PageObject = createAndRegister(") 
					&& firstCreateAndRegisterLinePassed && ! lastCreateAndRegisterLinePassed) {
				toReturn.add("		this." + varName + "PageObject = createAndRegister(" + pageobjectName + "PageObject.class, executableExample);");
				lastCreateAndRegisterLinePassed = true;
			}
			else if (! line.trim().startsWith("private ") && ! line.trim().endsWith("PageObject;") 
					&& firstPageObjectFieldDefinitionLinePassed && ! lastPageObjectFieldDefinitionLinePassed) {
				toReturn.add("	private " + pageobjectName + "PageObject " + varName + "PageObject;");
				lastPageObjectFieldDefinitionLinePassed = true;
			}
			else if (! line.trim().startsWith("import ") && ! line.trim().endsWith("PageObject;") 
					&& firstImportPageObjectLinePassed && ! lastImportPageObjectLinePassed) {
				toReturn.add("import com.iksgmbh.sysnat.language_templates." + testappName.toLowerCase() + ".pageobject." + pageobjectName + "PageObject;");
				lastImportPageObjectLinePassed = true;
			}
			
			toReturn.add(line);
			
			if (line.contains("define page change events") && pageChangeElements != null) {
				Stream.of(pageChangeElements).forEach(e -> toReturn.add(toPageChangeEventLine(e)));
			}
			
			if (line.contains("PageObject = createAndRegister(") && ! firstCreateAndRegisterLinePassed) {
				firstCreateAndRegisterLinePassed = true;
			} else if (line.trim().startsWith("private ") && line.trim().endsWith("PageObject;") && ! firstPageObjectFieldDefinitionLinePassed) {
				firstPageObjectFieldDefinitionLinePassed = true;
			} else if (line.trim().startsWith("import ") && line.trim().endsWith("PageObject;") && ! firstImportPageObjectLinePassed) {
				firstImportPageObjectLinePassed = true;
			}
			
		}

		return toReturn;
	}

	private String toPageChangeEventLine(PageChangeElement e)
	{
		String waitOption = "";
		if (e.millisToWait != 0) {
			waitOption = ".waiting(" + e.millisToWait + ")";
		} else if (e.uiElementIdentifierWaitForElement != null && ! e.uiElementIdentifierWaitForElement.isEmpty()) {
			waitOption = ".waiting(\"" + e.uiElementIdentifierWaitForElement + "\")";
		}
		
		String fromPart = "";
		if (e.currentPageName != null) {
			fromPart = ".from(" + SysNatStringUtil.firstCharToLowerCase(e.currentPageName) + "PageObject)";
		}
		
		return "		"
				+ "addPageChangeEvent(new PageChangeEventBuilder()" + fromPart
				+ ".via(EventType." + e.type.name() + ")"
				+ ".on(\"" + e.uiElementIdentifierTrigger + "\")"
				+ ".to(" + SysNatStringUtil.firstCharToLowerCase(e.nextPageName) + "PageObject)"
				+ waitOption + ".build());";
	}

	
	private static String checkSuffix(String pageName)
	{
		if (pageName == null || pageName.isEmpty()) return pageName;
		String toReturn = pageName;
		String toCut = "Object";
		if (toReturn.endsWith(toCut)) {
			toReturn = toReturn.substring(0, toReturn.length() - toCut.length());
		}
		toCut = "Page";
		if (toReturn.endsWith(toCut)) {
			toReturn = toReturn.substring(0, toReturn.length() - toCut.length());
		}
		return toReturn;
	}
	
	public static class PageChangeElement extends PageChangeEvent 
	{
		public String nextPageName;
		public String currentPageName;
		
		private PageChangeElement() {};
	}
	
	public static class PageChangeElementBuilder
	{
		private PageChangeElement result = new PageChangeElement();
		
		public PageChangeElementBuilder on(String uiElement) {
			result.uiElementIdentifierTrigger = uiElement;
			return this;
		}
		
		public PageChangeElementBuilder from(String pageName) {
			result.currentPageName = checkSuffix(pageName);
			return this;
		}	
		
		public PageChangeElementBuilder to(String pageName) {
			result.nextPageName = checkSuffix(pageName);
			return this;
		}	
		
		public PageChangeElementBuilder via(EventType aType) {
			result.type = aType;
			return this;
		}	

		public PageChangeElementBuilder waiting(String value) 
		{
			try {
				int i = Integer.valueOf(value);
				result.millisToWait = i;
			} catch (Exception e) {
				result.uiElementIdentifierWaitForElement = value;
			}
			return this;
		}	
		
		public PageChangeElement build() {
			return result;
		}
	}
	
}
