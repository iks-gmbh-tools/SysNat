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

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.GenerationRuntimeInfo;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.LanguageInstructionPattern;

public class LanguageInstructionCollectorClassLevelTest 
{
	@Before
	public void setup() 
	{
		GenerationRuntimeInfo.reset();
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.executableExample.source.dir", "../sysnat.testcase.generation/src/test/resources/testTestCases");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.executable.examples.source.dir", "../sysnat.testcase.generation/src/test/resources/testTestCases");
		ExecutionRuntimeInfo.setSysNatSystemProperty(SysNatConstants.TESTING_CONFIG_PROPERTY, "../sysnat.test.runtime.environment/src/test/resources/testSettingConfigs/settingsHomePageIKS.config");
		GenerationRuntimeInfo.getInstance();
	}

	@Test
	public void extractsLineWithBddKeyword()
	{
		// arrange
		final LanguageInstructionCollector cut = new LanguageInstructionCollector("SimpleTestApplication");
		final List<String> list = new ArrayList<String>();

		// act
		cut.extractBddInstruction("Feature X", list);
		cut.extractBddInstruction("OneTimeBackground Do Somthing in the beginning Once", list);
		cut.extractBddInstruction("Background Do Somthing in the beginning", list);
		cut.extractBddInstruction("Cleanup Clean this up", list);
		cut.extractBddInstruction("OneTimeCleanup Clean this up once", list);
		cut.extractBddInstruction("Scenario Y", list);
		cut.extractBddInstruction("Given A", list);
		cut.extractBddInstruction("But B", list);
		cut.extractBddInstruction("When C", list);
		cut.extractBddInstruction("* D", list);
		cut.extractBddInstruction("But E", list);
		cut.extractBddInstruction("Then F", list);
		cut.extractBddInstruction("And G", list);

		// assert
		assertEquals("number of test cases", 26, list.size() );
		assertEquals("instruction", "Set BDD-Keyword \"Given\".", list.get(12) );
		assertEquals("instruction", "A", list.get(13) );
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
		final String path = SysNatFileUtil.findAbsoluteFilePath("../sysnat.testcase.generation/src/test/resources/simpleScriptCall.nlxx");
		final File inputFile = new File(path);

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

		// the nlxx file here does not use BDD-Keywords because it is Behaviour based not Feature based!
		final List<LanguageInstructionPattern> instructionLines =
				(List<LanguageInstructionPattern>) getHashMapValue(result, "Behaviour.nlxx");
		assertEquals("number of instruction lines", "Behaviour: TestBehaviour", instructionLines.get(0).getInstructionLine() );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void doesNotCutTakeInstructionsStartingWithBDDKeywordAsBDDKeywordLine()
	{
		// arrange
		final LanguageInstructionCollector cut = new LanguageInstructionCollector("BehaviourMetaInfoApplication");  

		// act
		final HashMap<Filename, List<LanguageInstructionPattern>> result = cut.findAllNaturalLanguageInstruction();

		// assert
		assertEquals("number of test cases", 1, result.size() );

		// the nlxx file here does not use BDD-Keywords because it is Behaviour based not Feature based!
		final List<LanguageInstructionPattern> instructionLines =
				(List<LanguageInstructionPattern>) getHashMapValue(result, "Behaviour.nlxx");
		assertEquals("Number of instruction lines", 
				     "Given an instruction that starts with a BDD keyword although this is not supposed to represent a BDD-Keyword then do not cut it.", 
				     instructionLines.get(2).getInstructionLine() );
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