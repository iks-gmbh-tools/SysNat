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
