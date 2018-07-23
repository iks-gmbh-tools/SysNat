package com.iksgmbh.sysnat.helper;

import static org.junit.Assert.*;

import org.junit.Test;

public class MethodAnnotationSuggestionBuilderClassLevelTest 
{
	@Test
	public void buildsMethodAnnotationSuggestion() 
	{
		final String expectedValue = "@LanguageTemplate(value = \"Use ^^ and '' to build <>.\")" + 
	                                 System.getProperty("line.separator") + 
				                     "public Object replaceByAnExpressiveMethodName(String param1, Object param2)" + 
	                                 System.getProperty("line.separator") + 
				                     "    // Implement here code that corresponds to the Language Template's promise." + 
		                             System.getProperty("line.separator") + 
		                             "}";
		assertEquals(expectedValue, 
				MethodAnnotationSuggestionBuilder.buildAnnotationSuggestion(
						"Use \"param1\" and 'param2' to build <returnValue>."));
	}

	@Test
	public void removesReturnValueBetweenCaretSymbols() {
		assertEquals("abc ^^ abc", 
				MethodAnnotationSuggestionBuilder.deleteAllCharsBetween(
						"abc ^xxx^ abc", "^", "^"));
	}

	@Test
	public void removesTextBetweenTwoApostrophies() {
		assertEquals("abc '' abc", 
				MethodAnnotationSuggestionBuilder.deleteAllCharsBetween(
						"abc 'xxx' abc", "'", "'"));
	}

	@Test
	public void removesTextBetweenReturnTypeIdentifiers() {
		assertEquals("abc <> abc", 
				MethodAnnotationSuggestionBuilder.deleteAllCharsBetween(
						"abc <xxx> abc", "<", ">"));
	}

	@Test
	public void removesAllDataCharsFromInstructionLine() {
		assertEquals("abc '' abc '' and <> and ^^ and ^^ abc", 
				MethodAnnotationSuggestionBuilder.buildAnnotationValue(
						"abc 'x1' abc 'x2' and <x3> and ^x4^ and ^x5^ abc"));
	}

	@Test
	public void buildsReturnValueString() 
	{
		assertEquals("void", 
			     MethodAnnotationSuggestionBuilder.buildReturnValueString(
			    		 "abc ^x^ abc"));
		assertEquals("Object", 
			     MethodAnnotationSuggestionBuilder.buildReturnValueString(
			    		 "abc <x> abc"));
		
	}

	@Test
	public void buildsParamsString() 
	{
		assertEquals("String x", 
			     MethodAnnotationSuggestionBuilder.buildParamsString(
			    		 "abc ^x^ abc"));
		assertEquals("Object x", 
			     MethodAnnotationSuggestionBuilder.buildParamsString(
			    		 "abc 'x' abc"));
		assertEquals("Object y", 
			     MethodAnnotationSuggestionBuilder.buildParamsString(
			    		 "abc 'y' abc"));
		assertEquals("Object x1, String x2, Object x4, String x5", 
				     MethodAnnotationSuggestionBuilder.buildParamsString(
				    		 "abc 'x1' abc ^x2^ and <x3> and 'x4' and ^x5^ abc"));
	}
	
}
