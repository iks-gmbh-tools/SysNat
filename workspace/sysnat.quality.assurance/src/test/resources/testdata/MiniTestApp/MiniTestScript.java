package com.iksgmbh.sysnat.test.integration.testcase;

import com.iksgmbh.sysnat.ExecutableExample;
import com.iksgmbh.sysnat.testcasejavatemplate.ScriptTemplateParent;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

public class MiniTestScript extends ScriptTemplateParent
{	
	public MiniTestScript(ExecutableExample callingTestCase) {
		super(callingTestCase);
	}

	@Override
	public void executeScript() 
	{
		String scriptName = this.getClass().getSimpleName();
		SysNatFileUtil.writeFile("../sysnat.quality.assurance/target/" + scriptName + "Result.txt", 
				                 "This result file is created by '" + scriptName + "'.");
		addReportMessage("Script " + scriptName + " has been executed!");
	}
}