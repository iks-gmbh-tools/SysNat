package de.iksgmbh.sysnat.docing.testimpl;

import java.io.File;

import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

import de.iksgmbh.sysnat.docing.SysNatDocumentGenerator;

public class SysNatDocumentGeneratorTestImpl extends SysNatDocumentGenerator
{
	@Override
	protected File convert(File markdownFile)
	{
		String targetDir = "../sysnat.docing/target";
		File toReturn = new File(targetDir, "test." + targetFormat.name());
		SysNatFileUtil.writeFile(toReturn, "<test>");
		return toReturn;
	}
	
}
