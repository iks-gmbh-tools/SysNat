package com.iksgmbh.sysnat.dialog.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatLocaleConstants;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;

public class ConfigFileParser
{
	private List<String> possibleValues;
	private StringBuffer tooltip;
	private boolean possibleValueLineDetected = false;
	private HashMap<String, ConfigData> configDataMap;
	private List<String> testApplications;
	
	private ConfigFileParser(List<String> knownTestApplications) 
	{
		testApplications = knownTestApplications;
		possibleValues = new ArrayList<>();
		tooltip = getNewToolTipStringBuffer();
		configDataMap = new HashMap<>();
	}

	public static HashMap<String, ConfigData> doYourJob(final String configFileName) {
		return doYourJob(configFileName, new ArrayList<String>());
	}
	

	public static HashMap<String, ConfigData> doYourJob(String configFileName, List<String> knownTestApplications)
	{
		List<String> lines = SysNatFileUtil.readTextFile(configFileName);
		return new ConfigFileParser(knownTestApplications).parseLines(lines);
	}

	
	private HashMap<String, ConfigData> parseLines(List<String> lines)
	{
		for (String line : lines) 
		{
			line = line.trim();
			
			if (line.isEmpty()) continue;
			
			if (line.startsWith(SysNatConstants.COMMENT_CONFIG_IDENTIFIER)) // handle comment lines
			{
				line = line.substring(1).trim();
				
				if (line.startsWith(SysNatLocaleConstants.POSSIBLE_VALUE_IDENTIFIER) || 
					line.contains(SysNatLocaleConstants.POSSIBLE_VALUE_IDENTIFIER)) 
				{
					possibleValueLineDetected = true;
					continue;
				}
				
				if (possibleValueLineDetected) {
					List<String> values = SysNatStringUtil.toListOfLines(line, ",");
					values = checkForTestApplicationSpecificValues(values);
					values.forEach(value -> possibleValues.add(value.trim()));
				}

				if (line.startsWith(SysNatConstants.TOOLTIP_IDENTIFIER)) {
					String toolTipLine = line.substring(SysNatConstants.TOOLTIP_IDENTIFIER.length()).trim();
					tooltip.append(toolTipLine).append("<br>");
					continue;
				}
			}
			else // handle settings lines
			{
				ConfigData parseResult = parseTestingField(line);
				if (parseResult == null) {
					parseResult = parseDocingField(line);
				}
				if (parseResult == null) {
					parseResult = parseGeneralSettingField(line);
				}
				if (parseResult != null) {
					configDataMap.put(parseResult.fieldName, parseResult);
				}
			}
		}
		
		return configDataMap;
	}
	
	
	private List<String> checkForTestApplicationSpecificValues(List<String> values)
	{
		Optional<String> match = testApplications.stream().filter(testAppName -> values.get(0).startsWith(testAppName + ":")).findFirst();
		
		if (! match.isPresent() ) {
			return values;
		}
		
		String firstValue = values.get(0);
		values.remove(0);
		
		int pos = firstValue.indexOf(":");
		String prefix = firstValue.substring(0, pos+1);
		firstValue = firstValue.substring(pos+1).trim();
				
		List<String> toReturn = new ArrayList<>();
		toReturn.add(prefix+firstValue);
		values.forEach(value -> toReturn.add(prefix+value));
		
		return toReturn;
	}

	private ConfigData parseGeneralSettingField(String line)
	{
		// TODO Auto-generated method stub
		return null;
	}

	private ConfigData parseDocingField(String line)
	{
		// TODO Auto-generated method stub
		return null;
	}

	private ConfigData parseTestingField(String line)
	{
		ConfigData parseResult = null;
		
		if (isTestAppSetting(line)) {
			parseResult = new ConfigData(SysNatConstants.TEST_APPLICATION_SETTING_KEY,
                                         getTooltip(),
					                     possibleValues);
		} else if (isEnvironmentSetting(line)) {
			parseResult = new ConfigData(SysNatConstants.TEST_ENVIRONMENT_SETTING_KEY,
					getTooltip(),
					possibleValues);
		} else if (isExecutionFilter(line)) 
		{
			List<String> possibleFilters = new ArrayList<>();
			possibleFilters.addAll(possibleValues);
			parseResult = new ConfigData(SysNatConstants.TEST_EXECUTION_FILTER_SETTING_KEY,
					getTooltip(),
					possibleFilters);
		} else if (isBrowserSetting(line)) {
			parseResult = new ConfigData(SysNatConstants.TEST_BROWSER_SETTING_KEY,
					getTooltip(),
					possibleValues);
		} else if (isExecutionSpeed(line)) {
			parseResult = new ConfigData(SysNatConstants.TEST_EXECUTION_SPEED_SETTING_KEY,
                                         getTooltip(),
                                         possibleValues);
		}

		if (parseResult != null) {			
			tooltip = getNewToolTipStringBuffer();
			possibleValues.clear();
			possibleValueLineDetected = false;
		}
		
		return parseResult;
	}


	private StringBuffer getNewToolTipStringBuffer() {
		return new StringBuffer("<html>");
	}

	private String getTooltip() {
		return tooltip.append("</html>").toString();
	}
	
	private boolean isTestAppSetting(String line)
	{
		return line.startsWith(SysNatConstants.TEST_APPLICATION_SETTING_KEY);
	}

	private boolean isEnvironmentSetting(String line)
	{
		return line.startsWith(SysNatConstants.TEST_ENVIRONMENT_SETTING_KEY);
	}

	private static boolean isExecutionFilter(String line)
	{
		return line.startsWith(SysNatConstants.TEST_EXECUTION_FILTER_SETTING_KEY);
	}
	
	private static boolean isBrowserSetting(String line)
	{
		return line.startsWith(SysNatConstants.TEST_BROWSER_SETTING_KEY);
	}

	private static boolean isExecutionSpeed(String line)
	{
		return line.startsWith(SysNatConstants.TEST_EXECUTION_SPEED_SETTING_KEY);
	}
	
	public class ConfigData
	{
		public String fieldName;
		public String tooltip;
		public List<String> possibleValues = new ArrayList<>();
		
		public ConfigData(String aFieldName, 
				          String aToolTip, 
				          List<String> somePossibleValues)
		{
			this.fieldName = aFieldName;
			this.tooltip = aToolTip;
			somePossibleValues.forEach(value -> possibleValues.add(value.trim()));
		}
		
		public String[] getPossibleValuesArray() {
			return possibleValues.toArray(new String[possibleValues.size()]);
		}
	}
		

}
