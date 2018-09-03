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
package com.iksgmbh.sysnat.testresult.archiving;

import java.io.File;

import com.iksgmbh.sysnat.common.exception.SysNatConfigurationException;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

/**
 * Default implementation to archive test results.
 * Per default the test reports are copied to a directory
 * that serves as backup store. It is defined in the 
 * settings.config.
 * 
 * In case the results have to integrated into a test report tool,
 * the default implementation is supposed to be replaced by 
 * an interface to the test report tool in use.
 * 
 * @author Reik Oberrath
 *
 */
public class SysNatTestResultArchiver 
{

	public static void doYourJob(final File sourceDir) 
	{
		File targetDir = new File(System.getProperty("sysnat.report.archive"));
		
		if ( ! sourceDir.exists() ) {
			throw new SysNatConfigurationException("No report creation directory has been defined!");
		}
		
		if ( ! targetDir.exists() ) {
			throw new SysNatConfigurationException("No report archive directory has been defined!");
		}
		
		SysNatFileUtil.copyFolder(sourceDir, targetDir);
	}
}