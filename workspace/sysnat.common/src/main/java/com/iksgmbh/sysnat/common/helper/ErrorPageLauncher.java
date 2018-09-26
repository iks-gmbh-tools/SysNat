package com.iksgmbh.sysnat.common.helper;

import java.util.Locale;

import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

public class ErrorPageLauncher 
{
	private static final String ERROR_PAGE_TEMPLATE = 
			"../sysnat.testcase.generation/src/main/resources/htmlTemplates/GenerationErrorPage.htm.Template." 
            + Locale.getDefault().getLanguage() + ".txt";

	public static void doYourJob(String errorMessage, String helpMessage) 
	{
		if (System.getProperty("sysnat.dummy.test.run").equalsIgnoreCase("true")) {
			return;
		}
		
		final String templateText = SysNatFileUtil.readTextFileToString(ERROR_PAGE_TEMPLATE);
		final String filename = System.getProperty("sysnat.report.dir")
				                + "/GenerationError.html";
		
		final String errorReport = templateText.replace("ERROR_MESSAGE_PLACEHOLDER", errorMessage);
		SysNatFileUtil.writeFile(filename, errorReport.replace("HELP_MESSAGE_PLACEHOLDER", helpMessage));
		
		
		HtmlLauncher.doYourJob(filename);
	}

}
