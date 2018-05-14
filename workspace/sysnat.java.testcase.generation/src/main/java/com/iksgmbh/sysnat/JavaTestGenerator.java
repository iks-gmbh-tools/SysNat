package com.iksgmbh.sysnat;

import java.util.HashMap;

import com.iksgmbh.sysnat.domain.TestApplication;

/**
 * Parses the test cases written in natural language into Java code
 * and saves the result into the module java.test.execution where it
 * is executed during test runtime.
 * 
 * @author Reik Oberrath
 */
public class JavaTestGenerator 
{
	public static void doYourJob(final TestApplication applicationUnderTest ) 
	{
		final HashMap<String, String> javaTestCode = new JavaTestGenerator().createTestCode(applicationUnderTest);
		// TODO store data
	}

	protected HashMap<String, String> createTestCode(TestApplication applicationUnderTest) 
	{
		// TODO Auto-generated method stub
		return null;
	}
}
