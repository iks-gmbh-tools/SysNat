package com.iksgmbh.sysnat.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

public class SysNatCreateUpdatePreparer
{
	public static final String EXECUTION_POM = "../sysnat.test.execution/pom.xml";
	public static final String RUNTIME_POM = "../sysnat.test.runtime.environment/pom.xml";
	public static final String PROPERTIES = "../sysnat.test.runtime.environment/src/main/resources/execution_properties/Client.properties";
	public static String PATH = "../../../../";
	public static final String VERSION = "1.1"; //CurrentVersionFinder.doYourJob(new File(ClientUpdater.DefaultSource));
	
	public static void main(String[] args) throws IOException
	{
		System.out.println("SysNatCreateUpdatePreparer bei der Arbeit:");
		if (new File(".").getCanonicalPath().endsWith("snat.natural.language.executable.examples")) {
			PATH = "";
		}
		
		clearExecutionPOM();
		clearProperties();
		deleteReports();
		updateRuntimePom();
		
		System.out.println("Fertig.");
	}

	private static void updateRuntimePom()
	{
		List<String> lines = SysNatFileUtil.readTextFile(PATH + RUNTIME_POM);
		List<String> newLines = new ArrayList<>();

		for (int i = 0; i < lines.size(); i++) 
		{
			if (lines.get(i).contains("<version>")) {
				newLines.add("    <version>" + VERSION + "</version>");
			} else {
				newLines.add(lines.get(i));
			}
		}		
		
		File targetFile = new File(PATH + RUNTIME_POM);
		targetFile.delete();
		SysNatFileUtil.writeFile(targetFile, newLines);
	}

	private static void deleteReports()
	{
		String dir = PATH + "../sysnat.natural.language.executable.examples/reports";
		SysNatFileUtil.deleteFolder(dir);
		SysNatFileUtil.createFolder(dir);
	}

	private static void clearProperties()
	{
		List<String> lines = SysNatFileUtil.readTextFile(PATH + PROPERTIES);
		List<String> newLines = new ArrayList<>();
		
		for (int i = 0; i < lines.size(); i++) {
			if (lines.get(i).contains("PRODUCTION.Password")) {
				newLines.add("PRODUCTION.Password=<Never enter your password here!>");
			} else if (lines.get(i).contains("PRODUCTION.LoginId")) {
				newLines.add("PRODUCTION.LoginId=do100a\\<your loginId>");
			} else {
				newLines.add(lines.get(i));
			}
		}	
		
		File targetFile = new File(PATH + PROPERTIES);
		targetFile.delete();
		SysNatFileUtil.writeFile(targetFile, newLines);
	}
	
	private static void clearExecutionPOM()
	{
		List<String> lines = SysNatFileUtil.readTextFile(PATH + EXECUTION_POM);
		List<String> newLines = new ArrayList<>();
		boolean ignore = false;

		for (int i = 0; i < lines.size(); i++) 
		{
			if (lines.get(i).contains("<!--  End of autogenerated dependencies that are test application specific -->")) {
				newLines.add("");
				ignore = false;
			}
			
			if (! ignore) {
				newLines.add(lines.get(i));
			}
			
			if (lines.get(i).contains("<!--  Start of autogenerated dependencies that are test application specific -->")) {
				ignore = true;
				newLines.add("");
			} 
			
		}		
	
		
		File targetFile = new File(PATH + EXECUTION_POM);
		targetFile.delete();
		SysNatFileUtil.writeFile(targetFile, newLines);
	}
}