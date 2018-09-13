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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.GenerationRuntimeInfo;
import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.LanguageInstructionPattern;

public class LanguageInstructionCollectorClassLevelTest 
{
	@Before
	public void setup() {
		GenerationRuntimeInfo.reset();
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.executableExample.source.dir", "../sysnat.testcase.generation/src/test/resources/testTestCases");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.executable.examples.source.dir", "../sysnat.testcase.generation/src/test/resources/testTestCases");
		GenerationRuntimeInfo.getInstance();
	}

	@Test
	public void extractsLineWithBddKeyword()
	{
		// arrange
		final LanguageInstructionCollector cut = new LanguageInstructionCollector("SimpleTestApplication");
		final List<String> list = new ArrayList<String>();

		// act
		cut.extractInstruction("Given a passenger named \"Bob\".", list);

		// assert
		assertEquals("number of test cases", 2, list.size() );
		assertEquals("instruction", "Set BDD-Keyword \"Given\".", list.get(0) );
		assertEquals("instruction", "a passenger named \"Bob\".", list.get(1) );
	}

	@Test
	public void findsNaturalLanguageTestCaseFiles() 
	{
		// arrange
		final LanguageInstructionCollector cut = new LanguageInstructionCollector("SimpleTestApplication");
		
		// act
		List<File> result = cut.findInstructionFiles(".nlxx");
		result.forEach(System.out::println);
		// assert
		assertEquals("number of test cases", 2, result.size() );
		assertEquals("name of testcase", "naturalLanguageTestExamples.nlxx", result.get(0).getName() );
		assertEquals("name of testcase", "test.nlxx", result.get(1).getName() );
	}

	@Test
	public void parsesScriptCallFromInstructionLineWithoutQuotationMark()
	{
		// arrange
		final List<String> knownScripts = new ArrayList<>();
		knownScripts.add("Create an order.nls");
		GenerationRuntimeInfo.getInstance().setListOfKnownScriptNames(knownScripts);
		final File inputFile = new File("../sysnat.testcase.generation/src/test/resources/simpleScriptCall.nlxx");

		// act
		List<String> result = new LanguageInstructionCollector("aTestApplication").getInstructionLines(inputFile);

		// assert
		assertEquals("number of instructions", 2, result.size() );
		assertEquals("instruction", "\"Create an order\".", result.get(0) );
		assertEquals("instruction", result.get(0), result.get(1) );
	}


	@SuppressWarnings("unchecked")
	@Test
	public void findsAllNaturalLanguageInstructions()
	{
		// arrange
		final LanguageInstructionCollector cut = new LanguageInstructionCollector("SubFolderTestApplication");
				
		// act
		final HashMap<Filename, List<LanguageInstructionPattern>> result = cut.findAllNaturalLanguageInstruction();
		
		// assert
		assertEquals("number of test cases", 3, result.size() );
		
		final List<LanguageInstructionPattern> instructionLines1 = 
				(List<LanguageInstructionPattern>) getHashMapValue(result, "Test1.nlxx");
		assertEquals("number of instruction lines", 1, instructionLines1.size() );

		final List<LanguageInstructionPattern> instructionLines2 = 
				(List<LanguageInstructionPattern>) getHashMapValue(result, "Test2.nlxx");
		assertEquals("number of instruction lines", 3, instructionLines2.size() );

		final List<LanguageInstructionPattern> instructionLines3 = 
				(List<LanguageInstructionPattern>) getHashMapValue(result, "Test3.nlxx");
		assertEquals("number of instruction lines", 4, instructionLines3.size() );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void ignoresMetaInfoHeaderLines()
	{
		// arrange
		final LanguageInstructionCollector cut = new LanguageInstructionCollector("BehaviourMetaInfoApplication");

		// act
		final HashMap<Filename, List<LanguageInstructionPattern>> result = cut.findAllNaturalLanguageInstruction();

		// assert
		assertEquals("number of test cases", 1, result.size() );

		final List<LanguageInstructionPattern> instructionLines =
				(List<LanguageInstructionPattern>) getHashMapValue(result, "Behaviour.nlxx");
		assertEquals("number of instruction lines", 7, instructionLines.size() );
	}


	private Object getHashMapValue(final HashMap<Filename, List<LanguageInstructionPattern>> result,
			                       final String keyIdentifier) 
	{
		final Set<Filename> keySet = result.keySet();
		for (Filename key : keySet) {
			if (key.value.endsWith(keyIdentifier)) {
				return result.get(key);
			}
		}
		return null;
	}

}