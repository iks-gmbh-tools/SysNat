package com.iksgmbh.sysnat;

public class SysNatTesting 
{
	public static void main(String[] args) 
	{
		// Phase A: translate natural language into JUnit test code
		SysNatTestCaseGenerator.doYourJob();
		
		// Phase B: compile generated java code
		MavenCaller.compileJUnitTestCode();
		
		// Phase C: start java test runtime to perform tests
		MavenCaller.runJUnitTests();
		
	}
}
