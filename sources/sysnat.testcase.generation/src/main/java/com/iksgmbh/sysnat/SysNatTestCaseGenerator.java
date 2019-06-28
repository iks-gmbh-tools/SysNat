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
package com.iksgmbh.sysnat;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import com.iksgmbh.sysnat.common.helper.ErrorPageLauncher;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.JavaCommand;
import com.iksgmbh.sysnat.domain.JavaFieldData;
import com.iksgmbh.sysnat.domain.LanguageInstructionPattern;
import com.iksgmbh.sysnat.domain.LanguageTemplatePattern;
import com.iksgmbh.sysnat.domain.TestApplication;
import com.iksgmbh.sysnat.helper.CommandLibraryCreator;
import com.iksgmbh.sysnat.helper.JavaFileBuilder;
import com.iksgmbh.sysnat.helper.JavaFileWriter;
import com.iksgmbh.sysnat.helper.LanguageInstructionCollector;
import com.iksgmbh.sysnat.helper.LanguageTemplateCollector;
import com.iksgmbh.sysnat.helper.LanguageTemplateContainerFinder;
import com.iksgmbh.sysnat.helper.PatternMergeJavaCommandGenerator;
import com.iksgmbh.sysnat.helper.XXGroupBuilder;

/**
 * Generates java test code from nlxx-files and executes it.
 * 
 * @author Reik Oberrath
 */
public class SysNatTestCaseGenerator 
{
	/**
	 * Reads natural language instruction files and
	 * transforms the instructions given in a domain language
	 * into java commands of JUnit java files.
	 */
	public static void doYourJob() {
		new SysNatTestCaseGenerator().generateJUnitTestCaseFiles();
	}

	protected void generateJUnitTestCaseFiles() 
	{
		// step 0: init
		final TestApplication testApplication = GenerationRuntimeInfo.getInstance().getTestApplication();
		
		// step 1: find languageTemplateContainer in java template
		final List<JavaFieldData> languageTemplateContainerJavaFields = 
				LanguageTemplateContainerFinder.findLanguageTemplateContainers(testApplication.getName());
		
		// step 2: read natural language patterns from LanguageTemplateContainers
		final HashMap<Filename, List<LanguageTemplatePattern>> languageTemplateCollection = 
				LanguageTemplateCollector.doYourJob(languageTemplateContainerJavaFields);
		CommandLibraryCreator.doYourJob(languageTemplateCollection);
		
		// step 3: read natural language patterns from instructions of nlxx and nls files
		final HashMap<Filename, List<LanguageInstructionPattern>> languageInstructionCollection = 
				LanguageInstructionCollector.doYourJob(testApplication.getName());
		
		boolean ok = areExecutableExamplesAvailable(languageInstructionCollection.size());
		if (!ok) return;
		
		// step 4: find matches between language patterns and template patterns
		//         and merge matches into java code
		final HashMap<Filename, List<JavaCommand>> javaCommandCollectionRaw = 
				PatternMergeJavaCommandGenerator.doYourJob(languageTemplateCollection, 
						                                   languageInstructionCollection,
						                                   testApplication.getName());
		
		// step 5: build test cases for Parameter-Tests
		final HashMap<Filename, List<JavaCommand>> javaCommandCollection = 
				XXGroupBuilder.doYourJob(javaCommandCollectionRaw);
		
		// step 6: inject java commands in testCaseJavaTemplate and 
		//         create a JUnit test case
		final HashMap<File, String> javaFilesToCompile = 
				JavaFileBuilder.doYourJob(javaCommandCollection, 
						                  testApplication.getName(),
						                  languageTemplateContainerJavaFields);		

		// step 7: write JUnit test case to file in sysnat.test.execution
		JavaFileWriter.writeToTargetDir(javaFilesToCompile);  
		
		System.out.println(SysNatConstants.SYS_OUT_SEPARATOR);
		System.out.println("Done with generating JUnit test classes.");
		System.out.println(SysNatConstants.SYS_OUT_SEPARATOR);
	}

	private boolean areExecutableExamplesAvailable(int size)
	{
		if (size == 0) 
		{
			String testApp = ExecutionRuntimeInfo.getInstance().getTestApplicationName();
			ErrorPageLauncher.doYourJob("For test application <b>" + testApp 
					                   + "</b> there are no Executable Examples available.", 
					                   "Please, write nlxx files in folder "
					                   + "<b>sysnat.natural.language.executable.examples/ExecutableExamples/" 
					                   + testApp + "</b>.");
			return false;
		}
		return true;
	}
}