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

import java.io.File;

import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

public class ErrorPageLauncher 
{
	private static final String ERROR_PAGE_TEMPLATE = 
			"sources/sysnat.common/src/main/resources/ErrorPage.html.Template.txt";
	
	public static void doYourJob(final String errorMessage, 
			                     final String helpMessage, 
			                     final String title) 
	{
		final String templateText = SysNatFileUtil.readTextFileToString(SysNatFileUtil.findAbsoluteFilePath(ERROR_PAGE_TEMPLATE));
		String filename = SysNatFileUtil.findAbsoluteFilePath(getErrorReportFileAsString());
		File targetFolder = new File(filename).getParentFile();
		SysNatFileUtil.deleteFolder(targetFolder);
		targetFolder.mkdirs();
		
		final String errorReport = templateText.replace("TITLE_PLACEHOLDER", title)
		                                       .replace("ERROR_MESSAGE_PLACEHOLDER", errorMessage);
		try {
			SysNatFileUtil.writeFile(filename, errorReport.replace("HELP_MESSAGE_PLACEHOLDER", helpMessage));
			
			if (! "true".equalsIgnoreCase(System.getProperty(SysNatConstants.SYSNAT_DUMMY_TEST_RUN))) {
				HtmlLauncher.doYourJob(filename);
			}
		} catch (Exception e) {
			System.err.println("Problem: Cannot start ErrorPageLauncher.");
			throw new SysNatTestDataException("Could not start ErrorPageLaunch with message: " + errorMessage);
		}
	}
	
	private static String getErrorReportFileAsString() 
	{
		return SysNatFileUtil.findAbsoluteFilePath(System.getProperty("sysnat.report.dir")) + "/"
				+ System.getProperty(SysNatConstants.TEST_REPORT_NAME_SETTING_KEY)
				+ "/GenerationError.html";
	}
	

}
