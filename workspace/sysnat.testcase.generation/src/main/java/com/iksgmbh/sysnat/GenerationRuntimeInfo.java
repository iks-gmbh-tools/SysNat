package com.iksgmbh.sysnat;

import com.iksgmbh.sysnat.common.utils.SysNatConstants.TargetEnv;

/**
 * Stores information on generation of java test case files.
 * 
 * @author Reik Oberrath
 */
public class GenerationRuntimeInfo extends ExecutionRuntimeInfo
{
	private static GenerationRuntimeInfo instance;
	
	public static GenerationRuntimeInfo getInstance() 
	{
		if (instance == null)  {
			instance = new GenerationRuntimeInfo();
		}
		return instance;
	}
	
	private GenerationRuntimeInfo() {
		super();
	}

	public static void setSysNatSystemProperty(String key, String value) {
		ExecutionRuntimeInfo.setSysNatSystemProperty(key, value);
	}
    
    public static void reset() {
    	instance = null;
    	ExecutionRuntimeInfo.reset();
    }

	public void setTargetEnv(final String aValue) {
		try {
			TargetEnv targetEnv = TargetEnv.valueOf(aValue);
			targetEnvironment = targetEnv;
		} catch (Exception e) {
			System.err.println("Unknown target environment: " + aValue);
		}
	}	
	
}
