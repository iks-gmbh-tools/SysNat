package com.iksgmbh.sysnat.utils;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;

import org.junit.Test;

public class NaturalLanguageAnalyseUtilClassLevelTest
{
	private LinkedHashMap<String, String> valueParameters = new LinkedHashMap<>(); 

	@Test
	public void analyseEmptyString()
	{
		assertEquals("Message", "Problem: Enter complete Natural Language Expression", NaturalLanguageAnalyseUtil.analyseNL("", "", valueParameters));
	}
	
	@Test
	public void analyseOK()
	{
		assertTrue("One".equals(NaturalLanguageAnalyseUtil.analyseNL("One", "", valueParameters)));
		assertTrue(valueParameters.size() == 0);
		
		assertEquals("Message", "One \" Two \" Three", NaturalLanguageAnalyseUtil.analyseNL("One \" Two \" Three", "FirstParameterName", valueParameters));
		assertTrue(valueParameters.size() == 1);
		assertEquals("Key", "FirstParameterName", valueParameters.keySet().toArray()[0].toString());
		assertEquals("Value", NaturalLanguageAnalyseUtil.DATA_VALUE_OPERATOR + " Two ", valueParameters.values().toArray()[0].toString());
		
		assertEquals("Message", "One \" Two \" Three \" Four \" Five", NaturalLanguageAnalyseUtil.analyseNL("One \" Two \" Three \" Four \" Five", "PN2", valueParameters));
		assertTrue(valueParameters.size() == 2);
		assertEquals("Key", "PN2", valueParameters.keySet().toArray()[1].toString());
		assertEquals("Value", NaturalLanguageAnalyseUtil.DATA_VALUE_OPERATOR + " Four ", valueParameters.values().toArray()[1].toString());
	}

	@Test
	public void analyseUnkownTestParameter()
	{
		assertEquals("Message", "Unkown value parameter:  Two ", NaturalLanguageAnalyseUtil.analyseNL("One \" Two \" Three", "", valueParameters));
		assertEquals("Message", "Unkown value parameter:  Two ", NaturalLanguageAnalyseUtil.analyseNL("One \" Two \" Three \" Four \" Five", "", valueParameters));
		
		valueParameters.put("PN1", "aValue");
		assertEquals("Message", "Unkown value parameter:  Four ", NaturalLanguageAnalyseUtil.analyseNL("One \" Two \" Three \" Four \" Five", "", valueParameters));
	}
	

	@Test
	public void determinesSingleControlSymbols()
	{
		assertEquals("Message", "Problem: There is a single Double Qoute!", NaturalLanguageAnalyseUtil.analyseNL("One \" Two", "", valueParameters));
		assertEquals("Message", "Problem: There is a single Double Qoute!", NaturalLanguageAnalyseUtil.analyseNL("One \" Two \" Three \"", "", valueParameters));
		assertEquals("Message", "Problem: There is a single apostrophy!", NaturalLanguageAnalyseUtil.analyseNL("One ' Two ", "", valueParameters));
		assertEquals("Message", "Problem: There is a single apostrophy!", NaturalLanguageAnalyseUtil.analyseNL("One ' Two ' Three '", "", valueParameters));
		assertEquals("Message", "Problem: There is a single caret (^)!", NaturalLanguageAnalyseUtil.analyseNL("One ^^ Two ^ Three ^", "", valueParameters));
		assertEquals("Message", "Problem: There is a single Angle Bracket (<)!", NaturalLanguageAnalyseUtil.analyseNL("One < Two", "", valueParameters));
		assertEquals("Message", "Problem: There is a single Angle Bracket (>)!", NaturalLanguageAnalyseUtil.analyseNL("One > Two", "", valueParameters));
		assertEquals("Message", "Problem: There is more than one Angle Bracket (< or > or both)!", NaturalLanguageAnalyseUtil.analyseNL("One <> Two <", "", valueParameters));
		assertEquals("Message", "Problem: There is more than one Angle Bracket (< or > or both)!", NaturalLanguageAnalyseUtil.analyseNL("One <> Two >", "", valueParameters));
		assertEquals("Message", "Problem: Wrong order of Angle Bracket (> <)!", NaturalLanguageAnalyseUtil.analyseNL("One >Two< ", "", valueParameters));
		
	}

}
