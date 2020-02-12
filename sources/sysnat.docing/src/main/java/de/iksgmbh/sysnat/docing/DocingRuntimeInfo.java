package de.iksgmbh.sysnat.docing;

import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.DocumentationDepth;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.DocumentationFormat;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.ResultLaunchOption;

public class DocingRuntimeInfo //extends ExecutionRuntimeInfo
{
	private static DocingRuntimeInfo instance;

	public static DocingRuntimeInfo getInstance() 
	{
		if (instance == null)  {
			instance = new DocingRuntimeInfo();
		}
		return instance;
	}
	
	private DocingRuntimeInfo() {
		super();
	}

	public DocumentationFormat getDocFormat() {
		return DocumentationFormat.valueOf(System.getProperty(SysNatConstants.DOC_FORMAT_SETTING_KEY).toUpperCase());
	}

	public DocumentationDepth getDocDepth()	{
		return DocumentationDepth.valueOf(System.getProperty(SysNatConstants.DOC_DEPTH_SETTING_KEY));
	}
	
	public String getDocumentTargetDir() {
		return SysNatFileUtil.findAbsoluteFilePath(System.getProperty("sysnat.docing.targetDir.dir"));
	}
	
	public ResultLaunchOption getResultLaunchOption() {
		return ResultLaunchOption.valueOf(getSetting(SysNatConstants.RESULT_LAUNCH_OPTION_SETTING_KEY));
	}
	
	private String getSetting(String settingsKey) 
	{
		String toReturn = System.getProperty(settingsKey);

		if (toReturn == null) {
			throw new SysNatException(settingsKey + " is not specified!");
		}

		return toReturn.trim();
	}

}
