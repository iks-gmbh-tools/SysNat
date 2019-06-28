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
package com.iksgmbh.sysnat;

import com.iksgmbh.sysnat.common.utils.SysNatConstants.TargetEnv;

import java.util.List;

/**
 * Stores information on generation of java test case files.
 * 
 * @author Reik Oberrath
 */
public class GenerationRuntimeInfo extends ExecutionRuntimeInfo
{
	private static GenerationRuntimeInfo instance;
	private List<String> listOfScriptNames;

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

	public void setListOfKnownScriptNames(List<String> aList) {
		listOfScriptNames = aList;
	}

	public List<String> getListOfKnownScriptNames() {
		return listOfScriptNames;
	}

}