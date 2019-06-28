package com.iksgmbh.sysnat.test.integration.testcase;

import org.junit.Test;

import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

public class FakeTestCase 
{
	@Test
	public void writesAResultFile() throws Exception 
	{
		String testCaseName = this.getClass().getSimpleName();
		SysNatFileUtil.writeFile("../sysnat.quality.assurance/target/" + testCaseName + ".txt", 
				                 "This is the result file of testcase '" + testCaseName + "'.");
	}
}