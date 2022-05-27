package com.iksgmbh.sysnat.helper.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.domain.TestApplication;

/**
 * Given a composite test application C composed of element applications E1-En.
 * This generator joins all the language template methods of the element applications E1 .. En into composite test application C
 * so that the LanguageTemplatesBasics class of C contains all language template methods of E1 .. En.
 * 
 * LanguageTemplatesBasics classes of E1 .. En remain unchanged.
 * Previous language template methods in the LanguageTemplatesBasics class of C will be overwritten! 
 * 
 * @author Reik Oberrath
 */
public class CompositeTestApplicationUpdater
{
	private static final String COMPOSITE_APPLICATION = "UnitTestCompositeApp";
	
	private static final String LanguageTemplatesContainerPattern = "LanguageTemplatesBasics_";
	private static final String SECTION_IDENTIFIER = "L A N G U A G E   T E M P L A T E    M E T H O D S";
	
	protected static String languageTemplatesContainerParentDir = "../sysnat.test.runtime.environment/src/main/java/com/iksgmbh/sysnat/language_templates/";
	
	protected LinkedHashMap<String,LinkedHashMap<String,List<String>>> inputCodeMap = new LinkedHashMap<>();
	protected LinkedHashMap<String,List<String>> joinedCodeMap = new LinkedHashMap<>();
	protected HashMap<String,List<String>> methodOrigin = new HashMap<>();
	
	private int duplicateMethodCount = 0;
	private int joinedLanguageTemplateCount = 0;
	private List<String> firstPartOfTargetFileContent;
	private List<String> secondPartOfTargetFileContent;
	private String compositeAppName;
	private List<String> elementApplications;

	public static void main(String[] args)
	{
		System.out.println("Starting generation of " + getTargetFile(COMPOSITE_APPLICATION).getAbsolutePath());
		System.out.println("");
		TestApplication testApplication = new TestApplication(COMPOSITE_APPLICATION.replaceAll("-", "_"));
		boolean ok = new CompositeTestApplicationUpdater().doYourJob(COMPOSITE_APPLICATION, testApplication.getElementAppications());
		System.out.println("");
		if (ok) {
			System.out.println(getTargetFile(COMPOSITE_APPLICATION).getName() + " successfully updated.");
		} else {
			System.out.println("No change necessary in " + getTargetFile(COMPOSITE_APPLICATION).getName() + ".");
		}
		System.out.println("Done.");
	}
	
	public CompositeTestApplicationUpdater() {
		this.compositeAppName = COMPOSITE_APPLICATION;
		elementApplications = new ArrayList<>();
		elementApplications.add("CompApp1");
		elementApplications.add("CompApp2");
	}
	
	public boolean doYourJob(String aCompositeApplication, List<String> composites)
	{
		this.compositeAppName = aCompositeApplication;
		this.elementApplications = composites;
		
		readInputCode();
		boolean ok = joinCode();
		if (ok) {
			List<String> outputCode = createOutputCode();
			ok = isTargetUpdateNecessary(outputCode);
			if (ok) {
				writeOutputCode(outputCode);
			}
		}
		return ok;
	}

	protected void readInputCode()
	{
		for (String appName : elementApplications) 
		{
			String filename = LanguageTemplatesContainerPattern + appName + ".java";
			File file = new File(languageTemplatesContainerParentDir + appName.toLowerCase(), filename); 
			
			if (! file.exists()) {
				throw new RuntimeException("File not found: " + file.getAbsolutePath());	
			}
			
			List<String> content = SysNatFileUtil.readTextFile(file.getAbsolutePath());
			LinkedHashMap<String,List<String>> methodMap = new LinkedHashMap<>();
			List<String> methodLines = new ArrayList<>();
			String methodDeclarationLine = "";
			boolean sectionToParseReached = false;
			
			for (String line : content) 
			{
				if (line.contains(SECTION_IDENTIFIER)) 
				{
					sectionToParseReached = true;
					continue;
				}
				
				if (sectionToParseReached) 
				{
					if (line.trim().startsWith("@LanguageTemplate")) {
						methodLines.add(line);
					} 
					else if (line.trim().startsWith("public") || (! methodDeclarationLine.isEmpty() && line.trim().contains("{"))) 
					{
						methodDeclarationLine += line.trim();
						if (methodDeclarationLine.contains("{")) 
						{
							int pos = methodDeclarationLine.indexOf("{");
							methodDeclarationLine = methodDeclarationLine.substring(0,pos);
							methodMap.put(methodDeclarationLine, methodLines);
							methodLines = new ArrayList<>();
							methodDeclarationLine = "";
						}
					}
				}
			}
			
			inputCodeMap.put(appName, methodMap);
		}
	}


	protected boolean joinCode()
	{
		String app1 = elementApplications.get(0);
		LinkedHashMap<String, List<String>> appMethodMap1 = inputCodeMap.get(app1);
		joinedCodeMap.putAll(appMethodMap1);
		for (String key : appMethodMap1.keySet()) 
		{
			List<String> list = new ArrayList<>();
			list.add(app1);
			methodOrigin.put(key, list);
		}
		
		for (int i = 1; i < elementApplications.size(); i++) 
		{
			String app2 = elementApplications.get(i);
			LinkedHashMap<String, List<String>> appMethodMap2 = inputCodeMap.get(app2);
			appMethodMap2.keySet().forEach(method -> joinMethod(method, appMethodMap2.get(method), app2));
		}
		
		return checkJoinedCode();
	}

	private void joinMethod(String methodDeclarationLine, List<String> newLanguageTemplates, String origin)
	{

		boolean duplicate = false;
		joinedLanguageTemplateCount = 0;
		if (joinedCodeMap.containsKey(methodDeclarationLine)) 
		{
			duplicate = true;
			duplicateMethodCount++;
			System.out.println(duplicateMethodCount + ". Identical Method: " + methodDeclarationLine);
			List<String> languageTemplates = joinedCodeMap.get(methodDeclarationLine);
			newLanguageTemplates.stream().filter(template -> ! languageTemplates.contains(template))
			                             .forEach(template -> joinLanguageTemplates(template, languageTemplates));
			List<String> origins = methodOrigin.get(methodDeclarationLine);
			if (! origins.contains(origin)) origins.add(origin);
		} else {
			 joinedCodeMap.put(methodDeclarationLine, newLanguageTemplates);
			 List<String> list = new ArrayList<>();
			 list.add(origin);
			 methodOrigin.put(methodDeclarationLine, list);
		}
		
		if (joinedLanguageTemplateCount == 0 && duplicate) {
			System.out.println("   Method has identical Language Templates (No joining of Language Templates necessary)");
		}
	}

	private void joinLanguageTemplates(String template, List<String> languageTemplates)
	{
		joinedLanguageTemplateCount++;
		System.out.println("   Joined Language Templates: " + template);
		languageTemplates.add(template);
	}

	private boolean checkJoinedCode()
	{
		List<String> singleLanguageTemplates = new ArrayList<>();
		List<String> doubleLanguageTemplates = new ArrayList<>();

		for (String key : joinedCodeMap.keySet()) 
		{
			List<String> list = joinedCodeMap.get(key);
			for (String template : list) 
			{
				if (singleLanguageTemplates.contains(template)) {
					doubleLanguageTemplates.add(template);
				} else {
					singleLanguageTemplates.add(template);
				}
			}
		}
		
		if (! doubleLanguageTemplates.isEmpty()) {
			System.err.println("Following @LanguageTemplate annotations are not unique within the joined language template methods:");
			doubleLanguageTemplates.forEach(System.err::println);
			System.err.println("Note: Make sure that the Java Method signature of the method to which the annotions belong are identical for all test applications concerned!");
			return false;
		}
		return true;
	}
	
	protected List<String> createOutputCode()
	{
		List<String> toReturn = new ArrayList<>();
		joinedCodeMap.keySet().forEach(key -> toReturn.addAll(createOutputCode(key)));
		
		toReturn.add("");
		toReturn.add("}");

		return toReturn;
	}

	private List<String> createOutputCode(String methodDeclarationLine)
	{
		List<String> toReturn = new ArrayList<>();
		toReturn.add("");
		
		joinedCodeMap.get(methodDeclarationLine).forEach(line -> toReturn.add(line));
		
		toReturn.add("\t" + methodDeclarationLine);
		toReturn.add("\t{");
		
		List<String> origins = methodOrigin.get(methodDeclarationLine);
		boolean firstOrigin = true;
		
		for (String app : origins) 
		{
			String line = "\t\t";
			
			if (firstOrigin) {
				firstOrigin = false;
			} else {
				line += "else ";
			}
			line += "if (applicationInFocus == APP." + app + ") " 
					+ (hasReturnValue(methodDeclarationLine) ? "return " : "")
					+ getContainerVariable(app) + "." 
					+ getMethodName(methodDeclarationLine) + "(" 
					+ getArguments(methodDeclarationLine) + ");";
			toReturn.add(line);
		}
		toReturn.add("\t\telse throw new RuntimeException(\"Unsupported application: \" + applicationInFocus);");
		
		toReturn.add("\t}");
		
		return toReturn;
	}
	
	
	private String getArguments(String methodDeclarationLine)
	{
		int pos1 = methodDeclarationLine.trim().indexOf("(");
		int pos2 = methodDeclarationLine.trim().indexOf(")");
		String s = methodDeclarationLine.substring(pos1+1, pos2).replace("final ", "");
		String[] splitResult = s.split(",");
		if (splitResult.length == 1) 
		{
			if (s.trim().isEmpty()) {
				return "";
			}
			return cutType(splitResult[0].trim());
		}
			
		String toReturn = cutType(splitResult[0].trim());
		for (int i = 1; i < splitResult.length; i++) {
			toReturn += ", " + cutType(splitResult[i].trim());
		}

		return toReturn.trim();
	}

	private String cutType(String argument)
	{
		int pos = argument.indexOf(" ");
		return argument.substring(pos).trim();
	}

	private boolean hasReturnValue(String methodDeclarationLine) {
		return ! methodDeclarationLine.contains(" void ");
	}

	private String getMethodName(String methodDeclarationLine)
	{
		int pos = methodDeclarationLine.trim().lastIndexOf("(");
		String s = methodDeclarationLine.substring(0, pos);
		pos = s.trim().lastIndexOf(" ");
		return s.substring(pos+1);
	}

	private String getContainerVariable(String appName) {
		return "languageTemplatesBasics_" + appName;
	}

	
	protected boolean isTargetUpdateNecessary(List<String> outputCode)
	{
		readTargetFile(compositeAppName);
		
		if (outputCode.size() != secondPartOfTargetFileContent.size()) {
			return true;
		}
		
		for (int i = 0; i < outputCode.size(); i++) 
		{
			if (! outputCode.get(i).equals(secondPartOfTargetFileContent.get(i))) {
				return true;
			}
		}
		
		return false;
	}
	
	
	private void readTargetFile(String compositeApplication)
	{
		File targetFile = getTargetFile(compositeApplication); 
		
		if (! targetFile.exists()) {
			throw new RuntimeException("File not found: " + targetFile.getAbsolutePath());	
		}
		
		List<String> content = SysNatFileUtil.readTextFile(targetFile.getAbsolutePath());
		firstPartOfTargetFileContent = new ArrayList<>();
		secondPartOfTargetFileContent = new ArrayList<>();
		boolean identifierFound = false;
		boolean isFirstPart = true;

		for (String line : content) 
		{
			if (isFirstPart) 
			{
				firstPartOfTargetFileContent.add(line);
				if (! identifierFound) {
					identifierFound = line.contains(SECTION_IDENTIFIER);
				} else {
					if (line.trim().isEmpty()) {
						isFirstPart = false;
					}
				}
			} else {
				secondPartOfTargetFileContent.add(line);
			}
		}
	}

	private static File getTargetFile(String compositeApplication)
	{
		String appId = compositeApplication.replaceAll("-", "_");
		String filename = LanguageTemplatesContainerPattern + appId + ".java";
		File file = new File(languageTemplatesContainerParentDir + appId.toLowerCase(), filename);
		return file;
	}

	protected void writeOutputCode(List<String> outputCode)
	{
		List<String> newContent = new ArrayList<>();
		
		newContent.addAll(firstPartOfTargetFileContent);
		newContent.addAll(outputCode);
		
		SysNatFileUtil.writeFile(getTargetFile(compositeAppName), newContent);
	}
}
