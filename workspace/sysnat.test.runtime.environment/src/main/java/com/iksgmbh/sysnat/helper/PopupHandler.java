package com.iksgmbh.sysnat.helper;

import com.iksgmbh.sysnat.ExecutableExample;

public class PopupHandler 
{
	private static ExecutableExample testCase;

	public static void closeByOk() {
		testCase.sleep(100);
		testCase.clickElement("closeButtons");		
		testCase.sleep(100);
	}

	public static void setTestCase(ExecutableExample aTestCase) {
		testCase = aTestCase;
	}
}
