package com.iksgmbh.sysnat.helper;

import static org.junit.Assert.assertEquals;

import java.io.File;
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
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.testcase.source.dir", "../sysnat.testcase.generation/src/test/resources/testTestCases");
		GenerationRuntimeInfo.setSysNatSystemProperty("sysnat.executable.examples.source.dir", "../sysnat.testcase.generation/src/test/resources/testTestCases");
		GenerationRuntimeInfo.getInstance();
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
	
	

	@SuppressWarnings("unchecked")
	@Test
	public void findsAllNaturalLanguageInstruction() 
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
