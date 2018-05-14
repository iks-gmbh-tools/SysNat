package com.iksgmbh.sysnat.helper;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.LanguageInstructionPattern;

public class LanguageInstructionCollectorTest 
{
	@Before
	public void setup() {
		System.setProperty("sysnat.testcase.source.dir", "./src/test/resources/testTestCases");
	}
	
	@Test
	public void findsNaturalLanguageTestCaseFiles() 
	{
		// arrange
		final LanguageInstructionCollector cut = new LanguageInstructionCollector("SimpleTestAppication");
		
		// act
		List<File> result = cut.findTestCaseFiles();
		result.forEach(System.out::println);
		// assert
		assertEquals("number of test cases", 2, result.size() );
		assertEquals("name of testcase", "naturalLanguageTestExamples.nltc", result.get(0).getName() );
		assertEquals("name of testcase", "test.nltc", result.get(1).getName() );
	}	
	
	

	@SuppressWarnings("unchecked")
	@Test
	public void findsAllNaturalLanguageInstruction() 
	{
		// arrange
		final LanguageInstructionCollector cut = new LanguageInstructionCollector("SubFolderTestApplication");
				
		// act
		HashMap<Filename, List<LanguageInstructionPattern>> result = cut.findAllNaturalLanguageInstruction();
		
		// assert
		assertEquals("number of test cases", 3, result.size() );
		
		final List<LanguageInstructionPattern> instructionLines1 = 
				(List<LanguageInstructionPattern>) getHashMapValue(result, "Test1.nltc");
		assertEquals("number of instruction lines", 1, instructionLines1.size() );

		final List<LanguageInstructionPattern> instructionLines2 = 
				(List<LanguageInstructionPattern>) getHashMapValue(result, "Test2.nltc");
		assertEquals("number of instruction lines", 3, instructionLines2.size() );

		final List<LanguageInstructionPattern> instructionLines3 = 
				(List<LanguageInstructionPattern>) getHashMapValue(result, "Test3.nltc");
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
