package com.iksgmbh.sysnat.testdataimport.domain;

import java.util.List;
import java.util.Optional;

import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;

public interface DocumentValidationRule
{
	String toString();

	public static boolean isCompareRule(String toStringRepresentation) 
	{
		if (toStringRepresentation.startsWith("DocumentContentCompareValidationRule" )) {
			return true;
		}
		return false;
	}
	
	public static DocumentValidationRule getInstance(String toStringRepresentation) 
	{
		if (isCompareRule(toStringRepresentation)) {
			return getCompareInstance(toStringRepresentation);
		}
		return getSearchInstance(toStringRepresentation);
	}
	
	public static DocumentContentCompareValidationRule getCompareInstance(String toStringRepresentation) 
	{
		String s = SysNatStringUtil.extractTextBetween(toStringRepresentation, "[", "]");
		List<String> keyValuesPairs = SysNatStringUtil.toList(s, ",");
		String type = getValue("type", keyValuesPairs);
		String value = getValue("value", keyValuesPairs);

		return new DocumentContentCompareValidationRule(type, value);
	}
	
	public static DocumentContentSearchValidationRule getSearchInstance(String toStringRepresentation) 
	{
		String s = SysNatStringUtil.extractTextBetween(toStringRepresentation, "[", "]");
		List<String> keyValuesPairs = SysNatStringUtil.toList(s, ",");
		String expectedContent = getValue("expectedContent", keyValuesPairs);
		int pageNumber = getIntValue("pageNumber", keyValuesPairs);
		int lineNumber = getIntValue("lineNumber", keyValuesPairs);
		
		if ( pageNumber != -1 ) 
		{
			if ( lineNumber != -1 ) {
				return new DocumentContentSearchValidationRule(expectedContent, pageNumber, lineNumber);
			}
			
			return new DocumentContentSearchValidationRule(expectedContent, pageNumber);
		}
		
		String pageIdentifier = getValue("pageIdentifier", keyValuesPairs);
		if (pageIdentifier != null) {
			if ( lineNumber != -1 ) {
				return new DocumentContentSearchValidationRule(expectedContent, pageIdentifier, lineNumber);
			}
			return new DocumentContentSearchValidationRule(expectedContent, pageIdentifier);
		}
		
		return new DocumentContentSearchValidationRule(expectedContent);
	}
	
	public static int getIntValue(String key, List<String> keyValuesPairs) 
	{
		String value = getValue(key, keyValuesPairs); 
        try {
        	return Integer.valueOf(value);
        } catch (NumberFormatException e) {
			throw new IllegalArgumentException("toStringRepresentation of DocumentValidationRule "
					+ "does not contain a number value for key " + key + "!");
        }
	}

	public static String getValue(String key, List<String> keyValuesPairs) 
	{
		Optional<String> value = keyValuesPairs.stream().filter(pair -> pair.trim().startsWith(key)).findFirst();
		if ( ! value.isPresent() ) {
			throw new IllegalArgumentException("toStringRepresentation of DocumentValidationRule "
					+ "does not contain a value for key " + key + "!");
		}
		String[] splitResult = value.get().split("=");
		if ( splitResult.length != 2 ) {
			throw new IllegalArgumentException("From toStringRepresentation of DocumentValidationRule "
					+ "a value for key " + key + " is not parsable!");
		}		
		
		String toReturn = splitResult[1].trim();
		if ( "null".equals(toReturn)) {
			return null;
		}
		
		return toReturn;
	}
	
}
