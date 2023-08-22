package com.iksgmbh.sysnat.helper.generator;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.iksgmbh.sysnat.common.utils.SysNatConstants.ApplicationType;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.GuiType;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;

/**
 * Parses a piece of production code from the clipboard and generates from the result a snippet of Id mappings
 * that is supposed to be copied in a PageObject java file.
 * This helper is used to avoid manually search of technical id and displayed names of GUI components. 
 * 
 * @author Reik Oberrath
 */
public class IdMappingSnippetDevGen
{
	private static final String NO_TECHNICAL_ID_FOUND_FOR = "No Technical Id found for field ";
	private static Map<String, String> FIELD_NAME_PREFIX_MAP = getPrefixMap();

	protected static ApplicationType defaultCodeType = ApplicationType.Web;
	protected static boolean isFirstGuiType = true;
	
	public static void main(String[] args) throws Exception {
		doYourJob();
	}
	
	public static String doYourJob() throws Exception
	{
		return doYourJob(defaultCodeType);
	}
	
	public static String doYourJob(ApplicationType codeType) throws Exception
	{
		if (codeType == ApplicationType.Composite) return "Invalid code type: " + codeType.name();
		
		Map<GuiType, Map<String,String>> mappings = null;
		String inputCode = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor).toString();
		
		if (codeType == ApplicationType.Swing) {
			mappings = parseSwingJavaCode(inputCode);
		} else if (codeType == ApplicationType.Web) {
			inputCode = SysNatStringUtil.replaceBetweenQuotes(inputCode, " ", "_");
			mappings = parseWebHtmlCode(inputCode);
		}
		else {
			throw new RuntimeException(codeType.name() + " not yet supported.");
		}
		
		return generateOutput(mappings, codeType);
	}

	private static Map<GuiType, Map<String, String>> parseWebHtmlCode(String inputCode)
	{		
		Map<GuiType, Map<String,String>> toReturn = new LinkedHashMap<>();
	
		checkHtmlTextfields(inputCode, toReturn);
		checkHtmlComboBoxes(inputCode, toReturn);
		checkHtmlRadioButtons(inputCode, toReturn);
		checkHtmlButtons(inputCode, toReturn);
	
		return toReturn;
	}

	private static void checkHtmlButtons(String inputCode, Map<GuiType, Map<String, String>> toReturn)
	{
		GuiType guiType = GuiType.Button;
		String identifier1 = "<input ";
		String identifier2 = "type=\"button\"";
		if (inputCode.contains(identifier1) && inputCode.contains(identifier2)) {
			parseHtmlCodeFor(inputCode, toReturn, guiType, identifier1, identifier2);
		}
		
		identifier2="submit";
		if (inputCode.contains(identifier1) && inputCode.contains(identifier2)) {
			parseHtmlCodeFor(inputCode, toReturn, guiType, identifier1, identifier2);
		}
	}

	private static void checkHtmlRadioButtons(String inputCode, Map<GuiType, Map<String, String>> toReturn)
	{
		GuiType guiType = GuiType.RadioButtonSelection;
		String identifier1 = "<input ";
		String identifier2 = "type=\"radio\"";
		if (inputCode.contains(identifier1) && inputCode.contains(identifier2)) {
			parseHtmlCodeFor(inputCode, toReturn, guiType, identifier1, identifier2);
		}
	}

	private static void checkHtmlComboBoxes(String inputCode, Map<GuiType, Map<String, String>> toReturn)
	{
		GuiType guiType = GuiType.ComboBox;
		String identifier = "<select ";
		if (inputCode.contains(identifier)) {
			parseHtmlCodeFor(inputCode, toReturn, guiType, identifier);
		}
	}

	private static void checkHtmlTextfields(final String inputCode, final Map<GuiType, Map<String, String>> toReturn)
	{
		GuiType guiType = GuiType.TextField;
		String identifier = "<input type=\"text\"";
		if (inputCode.contains(identifier)) {
			parseHtmlCodeFor(inputCode, toReturn, guiType, identifier);
		}
	}

	private static void parseHtmlCodeFor(final String inputCode,
                                         final Map<GuiType, Map<String, String>> toReturn,
                                         final GuiType guiType,
                                         final String identifier)
	{
		parseHtmlCodeFor(inputCode, toReturn, guiType, identifier, null);
	}
	
	
	private static void parseHtmlCodeFor(final String inputCode,
	                                     final Map<GuiType, Map<String, String>> toReturn,
	                                     final GuiType guiType,
	                                     final String identifier1,
	                                     final String identifier2)
	{
		LinkedHashMap<String, String> mappings = new LinkedHashMap<>();
		toReturn.put(guiType, mappings);
		String s = inputCode;
		String nameTag = null;
		
		while (s.contains(identifier1)) 
		{
			int pos = s.indexOf(identifier1);
			s = s.substring(pos + identifier1.length());
			pos = s.indexOf(">");
			String ss = s.substring(0, pos);
			List<String> splitResult = SysNatStringUtil.split(ss, " ").stream().filter(e -> ! e.isEmpty()).collect(Collectors.toList());
			Optional<String> nameCandidate = splitResult.stream().map(e -> e.trim()).filter(e -> e.startsWith("name=")).findFirst();
			Optional<String> typeCandidate = splitResult.stream().map(e -> e.trim()).filter(e -> e.startsWith("type=")).findFirst();
			Optional<String> idCandidate = splitResult.stream().map(e -> e.trim()).filter(e -> e.startsWith("id=")).findFirst();
			
			boolean typeMatch = identifier2 == null || ( typeCandidate.isPresent() && getValueFromTag(typeCandidate.get()).equals( identifier2)) 
					|| (typeCandidate.isPresent() && typeCandidate.get().equals( identifier2));
			if (nameCandidate.isPresent() && typeMatch) 
			{				
				nameTag = nameCandidate.get().replace("\"", "").trim();
				String key = getValueFromTag(nameTag);
				Optional<String> valueCandidate = splitResult.stream().map(e -> e.trim()).filter(e -> e.startsWith("value=")).findFirst();
				if (valueCandidate.isPresent()) {
					key = key + "_" + getValueFromTag(valueCandidate.get()).replace("\"", "");
				}
				
				String value = "?";
				if (idCandidate.isPresent()) {
					value = idCandidate.get().trim();
				}
				if (mappings.containsKey(key)) {
					mappings.put(generateDoubledKey(mappings.keySet(), key), value);
				} else {
					mappings.put(key, value);
				}
			}
		}
		
		mappings.keySet().forEach(e -> searchHtmlGuiDisplayName_WithHeuristic_findLabelText(inputCode, toReturn, guiType, e.trim()));
		mappings.keySet().forEach(e -> searchHtmlTechnicalId(inputCode, toReturn, guiType, e.trim(), identifier1, identifier2));		
	}	
	
	private static String generateDoubledKey(Set<String> keySet, String key)
	{
		int count = 0;
		String toReturn = key;
		
		while (keySet.contains(toReturn)) {
			count++;
			 toReturn = key + "_" + count;
		}
		
		return toReturn;
	}

	private static void searchHtmlTechnicalId(String inputCode,
	                                          Map<GuiType, Map<String, String>> toReturn,
	                                          GuiType guiType,
	                                          String key, 
	                                          String identifier1,
	                                          String identifier2)
	{
		String s = inputCode;
		while (s.contains(identifier1)) 
		{
			int pos = s.indexOf(identifier1);
			s = s.substring(pos + identifier1.length());
			pos = s.indexOf(">");
			String ss = s.substring(0, pos);
			if (identifier2 != null && ! ss.contains(identifier2)) continue;
			s = s.substring(ss.length());
			List<String> splitResult = SysNatStringUtil.split(ss, " ").stream().filter(e -> ! e.isEmpty()).collect(Collectors.toList());
			Optional<String> candidate = splitResult.stream().map(e -> e.trim()).filter(e -> e.startsWith("name=")).findFirst();
			if (candidate.isPresent()) 
			{				
				String nameTag = candidate.get().replace("\"", "").trim();
				Optional<String> valueCandidate = splitResult.stream().map(e -> e.trim()).filter(e -> e.startsWith("value=")).findFirst();
				if (valueCandidate.isPresent()) {
					nameTag = nameTag + "_" + getValueFromTag(valueCandidate.get()).replace("\"", "");
				}
				
				if (splitResult != null) {			
					candidate = splitResult.stream().map(e -> e.trim()).filter(e -> e.startsWith("id=")).findFirst();
					if (candidate.isPresent()) {		
						String fieldName = getValueFromTag(nameTag);
						String displayName = toReturn.get(guiType).get(fieldName);
						if (displayName == null || displayName.startsWith("?")) {					
							displayName = "?";
						} else {
							displayName = displayName.trim();
							pos = displayName.indexOf(":");
							displayName = displayName.substring(pos+1).trim();
						}
						fieldName = fieldName.trim();
						String newValue = displayName + "=" + getValueFromTag(candidate.get()).replace("\"", "").trim();
						toReturn.get(guiType).put(fieldName, newValue);
					}
				}	
			}
		}
	}

	private static void searchHtmlGuiDisplayName_WithHeuristic_findLabelText(String inputCode,
	                                                                         Map<GuiType, Map<String, String>> toReturn,
	                                                                         GuiType guiType,
	                                                                         String fieldName)
	{
		String identifier;
		int pos;
		String displayText;
		
		if (guiType == GuiType.Button) 
		{
			identifier="type=\"button\"";
			pos = inputCode.indexOf(identifier);
			String s = inputCode.substring(pos + identifier.length());
			identifier="value=\"";
			pos = s.indexOf(identifier);
			s = s.substring(pos + identifier.length());
			pos = s.indexOf("\"");
			displayText = s.substring(0, pos).replace("_", " ").trim();
		} else {
			identifier = " for=\"" + fieldName + "\"";
			pos = inputCode.indexOf(identifier);
			if (pos == -1) {
				String value = toReturn.get(guiType).get(fieldName);
				if (value.startsWith("id=")) {
					String fieldId = getValueFromTag(value).replace("\"", "");
					identifier = " for=\"" + fieldId + "\"";
					pos = inputCode.indexOf(identifier);
				} else {
					toReturn.get(guiType).put(fieldName, "?=" + NO_TECHNICAL_ID_FOUND_FOR + "with code name: " + fieldName);
					return;
				}
			}
			
			String s = inputCode.substring(pos + identifier.length());
			pos = s.indexOf("<");
			s = s.substring(0, pos).replace(System.getProperty("line.separator"), "").trim();
			pos = s.indexOf(">")+1;
			displayText = s.substring(pos);
		}
		
		toReturn.get(guiType).put(fieldName, displayText + "=" + NO_TECHNICAL_ID_FOUND_FOR + "with display name: " + displayText);		
	}

	private static String getValueFromTag(String tag) {
		String[] splitResult = tag.split("=");
		return splitResult[1].replace("\"", "");
	}

	private static String generateOutput(Map<GuiType, Map<String, String>> mappings, ApplicationType codeType)
	{
		String toReturn = null;
		if (mappings == null) 
		{
			toReturn = codeType.name() + " not yet supported";
			System.err.println(toReturn);
			return toReturn;
		}
		
		StringBuffer sb = new StringBuffer();
		mappings.keySet().forEach(key -> guiTypeToMappingString(sb, key, mappings.get(key)));
		toReturn = sb.toString().replace(";", ";" + System.getProperty("line.separator"));
		int pos = toReturn.lastIndexOf(";");
		toReturn = toReturn.substring(0, pos+1);
		
		System.out.println(toReturn);
		return toReturn;
	}

	private static void guiTypeToMappingString(StringBuffer sb, GuiType guiType, Map<String, String> mappings)
	{
		if (isFirstGuiType) {
			isFirstGuiType = false;
			sb.append("        HashMap<String, List<String>> idMappings = new HashMap<String, List<String>>();");
		} else {
			sb.append("        idMappings = new HashMap<String, List<String>>();");
		}
		
		mappings.keySet().forEach(key -> mappingToJavaLine(mappings.get(key), sb));
		sb.append("        idMappingCollection.put(GuiType." + guiType.name() + ", idMappings);");
		sb.append(System.getProperty("line.separator"));
	}

	private static void mappingToJavaLine(String value, StringBuffer sb)
	{
		String line = "        idMappings.put(\"" + extractDisplayName(value) + "\", createList(\"" + extractTexchnicalId(value) + "\"));";
		if (! sb.toString().contains(line)) {
			sb.append(line);
		}
	}

	private static String extractDisplayName(String keyValuePair) {
		return SysNatStringUtil.split(keyValuePair, "=").get(0);
	}
	
	private static String extractTexchnicalId(String keyValuePair) {
		return SysNatStringUtil.split(keyValuePair, "=").get(1);
	}
	

	private static Map<GuiType, Map<String, String>> parseSwingJavaCode(String inputCode)
	{
		Map<GuiType, Map<String,String>> toReturn = new LinkedHashMap<>();
		
		checkSwingTextfields(inputCode, toReturn);
		checkSwingComboBoxes(inputCode, toReturn);

		return toReturn;
	}

	private static void checkSwingComboBoxes(final String inputCode, 
			                                 final Map<GuiType, Map<String, String>> toReturn)
	{
		GuiType guiType = GuiType.ComboBox;
		String identifier = " JComboBox<String>";
		if (inputCode.contains(identifier)) {
			parseSwingCodeFor(inputCode, toReturn, guiType, identifier);
		}
	}
	
	private static void checkSwingTextfields(final String inputCode, 
			                                 final Map<GuiType, Map<String, String>> toReturn)
	{
		GuiType guiType = GuiType.TextField;
		String identifier = " JTextField ";
		if (inputCode.contains(identifier)) {
			parseSwingCodeFor(inputCode, toReturn, guiType, identifier);
		}
	}

	private static void parseSwingCodeFor(final String inputCode,
	                                      final Map<GuiType, Map<String, String>> toReturn,
	                                      final GuiType guiType,
	                                      final String identifier)
	{
		LinkedHashMap<String, String> mappings = new LinkedHashMap<>();
		toReturn.put(guiType, mappings);
		String s = inputCode;
		while (s.contains(identifier)) 
		{
			int pos = s.indexOf(identifier);
			s = s.substring(pos + identifier.length());
			pos = s.indexOf(";");
			String ss = s.substring(0, pos);
			List<String> splitResult = SysNatStringUtil.split(ss, ",");
			splitResult.forEach(e -> mappings.put(e.trim(), "?"));
		}
		
		mappings.keySet().forEach(e -> searchSwingGuiDisplayName_WithHeuristic_findLabelText(inputCode, toReturn, guiType, e));
		mappings.keySet().forEach(e -> searchSwingTechnicalId(inputCode, toReturn, guiType, e));		
	}	

	private static void searchSwingTechnicalId(final String inputCode,
										  	   final Map<GuiType, Map<String, String>> toReturn,
										  	   final GuiType guiType,
										  	   final String fieldNameInCode)
	{
		String identifier = fieldNameInCode + ".setName";
		String technicalId = NO_TECHNICAL_ID_FOUND_FOR.trim() + ": " + fieldNameInCode;
		if (inputCode.contains(identifier)) 
		{
			int pos = inputCode.indexOf(identifier);
			String s = inputCode.substring(pos + identifier.length());
			pos = s.indexOf(";");
			s = s.substring(0, pos);
			technicalId = SysNatStringUtil.extractTextBetween(s, "\"", "\"");
		} 
		String displayName = toReturn.get(guiType).get(fieldNameInCode);
		toReturn.get(guiType).put(fieldNameInCode, displayName + "=" + technicalId);
	}

	private static void searchSwingGuiDisplayName_WithHeuristic_findLabelText(final String inputCode,
			                                                             final Map<GuiType, Map<String, String>> toReturn,
			                                                             final GuiType guiType,
			                                                             final String fieldNameInCode)
	{
		String prefix = FIELD_NAME_PREFIX_MAP.get(guiType.name());
		if (fieldNameInCode.startsWith(prefix)) 
		{
			String assumedLabelNameInCode = FIELD_NAME_PREFIX_MAP.get("Label") + fieldNameInCode.substring(prefix.length());
			String identifier = assumedLabelNameInCode + " = ";
			if (inputCode.contains(identifier)) 
			{
				int pos = inputCode.indexOf(identifier);
				String s = inputCode.substring(pos + identifier.length());
				pos = s.indexOf(";");
				s = s.substring(0, pos);
				
				String displayName = "?";
				if (s.contains("\"")) {
					displayName = SysNatStringUtil.extractTextBetween(s, "\"", "\"");
				} 
				
				identifier = assumedLabelNameInCode + ".setText";
				pos = inputCode.indexOf(identifier);
				s = inputCode.substring(pos + identifier.length());
				pos = s.indexOf(";");
				s = s.substring(0, pos);
				if (s.contains("\"")) {
					displayName = SysNatStringUtil.extractTextBetween(s, "\"", "\"");
				} 
				
				toReturn.get(guiType).put(fieldNameInCode, displayName);
			}
			
		}
	}


	private static Map<String, String> getPrefixMap()
	{
		Map<String, String> toReturn = new HashMap<>();
		toReturn.put(GuiType.TextField.name(), "txt");
		toReturn.put(GuiType.ComboBox.name(), "cbx");
		toReturn.put(GuiType.Button.name(), "btn");
		toReturn.put("Label", "lbl");
		return toReturn;
	}
}
