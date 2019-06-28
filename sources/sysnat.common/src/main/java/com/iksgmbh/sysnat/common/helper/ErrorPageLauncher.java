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

import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

public class ErrorPageLauncher 
{
	private static final String ERROR_PAGE_TEMPLATE = 
			"sources/sysnat.testcase.generation/src/main/resources/htmlTemplates/GenerationErrorPage.htm.Template."
            + Locale.getDefault().getLanguage() + ".txt";

	public static void doYourJob(String errorMessage, String helpMessage) 
	{
		if (! "true".equalsIgnoreCase(System.getProperty("sysnat.dummy.test.run"))) 
		{
			final String templateText = SysNatFileUtil.readTextFileToString(SysNatFileUtil.findAbsoluteFilePath(ERROR_PAGE_TEMPLATE));
			String filename = System.getProperty("sysnat.report.dir") + "/GenerationError.html";
			filename = SysNatFileUtil.findAbsoluteFilePath(filename);
			
			final String errorReport = templateText.replace("ERROR_MESSAGE_PLACEHOLDER", errorMessage);
			
			try {
				SysNatFileUtil.writeFile(filename, errorReport.replace("HELP_MESSAGE_PLACEHOLDER", helpMessage));
				HtmlLauncher.doYourJob(filename);
			} catch (Exception e) {
				System.err.println("Problem: Cannot start ErrorPageLauncher.");
				throw new SysNatTestDataException(errorMessage);
			}
		}
	}

}
