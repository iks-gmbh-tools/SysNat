package com.iksgmbh.sysnat.helper;

import com.iksgmbh.sysnat.TestCase;

public class PopupHandler 
{
	private static TestCase testCase;

	public static void closeByOk() {
		testCase.sleep(100);
		testCase.clickElement("closeButtons");		
		testCase.sleep(100);
	}

	public static void setTestCase(TestCase aTestCase) {
		testCase = aTestCase;
	}
}
