package com.iksgmbh.sysnat;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;

import javax.tools.ToolProvider;

import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.JavaCommand;
import com.iksgmbh.sysnat.domain.LanguageInstructionPattern;
import com.iksgmbh.sysnat.domain.LanguageTemplatePattern;
import com.iksgmbh.sysnat.domain.TestApplication;
import com.iksgmbh.sysnat.helper.GenerationRuntimeInfo;
import com.iksgmbh.sysnat.helper.LanguageInstructionCollector;
import com.iksgmbh.sysnat.helper.LanguageTemplateCollector;
import com.iksgmbh.sysnat.helper.PatternMergeJavaCommandGenerator;
import com.iksgmbh.sysnat.utils.ExceptionHandlingUtil;
import com.iksgmbh.sysnat.utils.SysNatConstants.AppUnderTest;

/**
 * Generates java test code from nltc-files and executes it.
 * 
 * @author Reik Oberrath
 */
public class SysNatTestCaseGenerator 
{
	private static final String JUNIT_FILE_TEMPLATE_SUFFIX = "TestCaseTemplate";

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
		// init
		final TestApplication applicationUnderTest = findApplicationUnderTest();
		final Class<?> testCaseJavaTemplate = findJavaJUnitTemplateFor(applicationUnderTest.getName());
		
		// step 1: read natural language patterns from LanguageTemplateContainers
		final HashMap<Filename, List<LanguageTemplatePattern>> languageTemplateCollection = 
				LanguageTemplateCollector.doYourJob(testCaseJavaTemplate);
		
		// step 2: read natural language patterns from LanguageTemplateContainers
		final HashMap<Filename, List<LanguageInstructionPattern>> languageInstructionCollection = 
				LanguageInstructionCollector.doYourJob(applicationUnderTest.getName());
		
		// step 3: find matches between language patterns and template patterns
		//         and merge matches into java code
		final HashMap<Filename, List<JavaCommand>> javaCommandCollection = 
				PatternMergeJavaCommandGenerator.doYourJob(languageTemplateCollection, languageInstructionCollection);		
	}

	protected Class<?> findJavaJUnitTemplateFor(final String applicationName) 
	{
		final String javaFileName = "src/main/resources/javaJUnitTemplates/com/iksgmbh/sysnat/testcasejavatemplate/" + applicationName + JUNIT_FILE_TEMPLATE_SUFFIX + ".java";
		final String className = "com.iksgmbh.sysnat.testcasejavatemplate." + applicationName + JUNIT_FILE_TEMPLATE_SUFFIX;
		final String classDir = "src/main/resources/javaJUnitTemplates";

		try {
			ToolProvider.getSystemJavaCompiler().run(null, null, null, new File(javaFileName).getPath());
			URL[] urls = { new File(classDir).toURI().toURL() };
			URLClassLoader urlClassLoader = URLClassLoader.newInstance(urls);
			return Class.forName(className, true, urlClassLoader);
		} catch (ClassNotFoundException e) {
			ExceptionHandlingUtil.throwException("Class file not found: " + className);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected TestApplication findApplicationUnderTest() {
		final AppUnderTest appUnderTest = GenerationRuntimeInfo.getInstance().getAppUnderTest();
		return new TestApplication(appUnderTest);
	}
}
