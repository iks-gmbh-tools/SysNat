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
package com.iksgmbh.sysnat.test.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

public class SysNatTestUtils 
{
	public static void assertFile_NOT_Exists(File f) {
		assertFalse("File exist unexpectedly", f.exists());
	};
	
	public static void assertFile_NOT_Exists(String f) {
		assertFile_NOT_Exists(new File(f));
	};

	
	public static void assertFileExists(File f) {
		assertTrue("Expected file does not exist", f.exists());
	};
	
	public static void assertFileExists(String f) {
		assertFileExists(new File(f));
	};
	
	public static File findNewReport(final File reportDir, 
			                         final List<String> oldReports) 
	{
		final List<String> newReports = Arrays.asList(reportDir.list());
		if (oldReports.size() == 0) {
			if ( newReports.size() == 1) {
				return new File(reportDir, newReports.get(0));
			} else if ( newReports.size() == 0) {
				fail("No report generated!");
			} else {
				throw new RuntimeException("Unambiguous report!");
			}
		}
		for (String report : newReports) {
			final Optional<String> filename = oldReports.stream().filter(s -> !s.equals(report)).findFirst();
			if (filename.isPresent()) {				
				return new File(reportDir, filename.get());
			}
		}
		fail("No report generated!");
		return null;
	}


	public static List<String> readCurrentlyKnownPids() 
	{
		try {
			final String[] args = {"cmd", "/C", "tasklist"};
			Process process = Runtime.getRuntime().exec(args);
			InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
			BufferedReader reader = new BufferedReader(inputStreamReader);
			String line = null;
			List<String> lines = new ArrayList<>();
			while ( (line = reader.readLine()) != null ) {
				lines.add(line);
			}
			return lines;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void assertReportFolderNotExistsBeginningWith(final String reportNamePrefix) 
	{
		String path = SysNatFileUtil.findAbsoluteFilePath(System.getProperty("sysnat.report.dir"));
		List<File> filelist = Arrays.asList( new File( path ).listFiles() );
		filelist = filelist.stream().filter(f->f.getName().startsWith(reportNamePrefix) && f.isDirectory()).collect(Collectors.toList());
		filelist.forEach(f -> SysNatFileUtil.deleteFolder(f));
		Optional<File> oldReportFolder = Arrays.asList( new File( path ).listFiles() ).stream().filter(f->f.getName().startsWith("MiniTestCaseTestReport") && f.isDirectory()).findFirst();
		assertFalse("A old report folder still exists!", oldReportFolder.isPresent());
	}

	public static void assertReportContains(String report, String expected) 
	{
		if ( ! report.contains(expected)) {			
			System.err.println(report);
		}
		assertTrue("Expected text '" + expected + "' not found in report.", report.contains(expected));		
	}	
	
}