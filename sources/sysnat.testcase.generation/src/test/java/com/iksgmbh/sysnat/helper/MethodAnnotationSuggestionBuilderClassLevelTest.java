/*
 * Copyright 2018 IKS Gesellschaft fuer Informations- und Kommunikationssysteme mbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iksgmbh.sysnat.helper;

import static org.junit.Assert.assertEquals;

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
	                                 "{" +
	                                 System.getProperty("line.separator") + 
				                     "    // Implement here code that corresponds to the Language Template's promise." +
	                                 System.getProperty("line.separator") + 
	                                 "    // And don't forget to create a report message entry!" + 
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
				     MethodAnnotationSuggestionBuilder.buildAnnotationValue("abc 'x1' abc 'x2' and <x3> and \"^x4\" and \"x5\" abc"));
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