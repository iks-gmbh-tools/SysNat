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
package com.iksgmbh.sysnat.domain;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.helper.ErrorPageLauncher;
import com.iksgmbh.sysnat.common.utils.PropertiesUtil;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.TargetEnvironment;

/**
 * Holds attributes of the application under test.
 * 
 * @author Reik Oberrath
 */
public class TestApplication
{
	private static final ResourceBundle ERR_MSG_BUNDLE = ResourceBundle.getBundle("bundles/ErrorMessages",
	        Locale.getDefault());

	private String name;
	private String targetEnvironmentAsString;
	private String propertiesFileName;
	private boolean isWebApplication;
	private boolean withLogin;
	private Properties applicationProperties;
	private HashMap<SysNatConstants.WebLoginParameter, String> loginParameter = new HashMap<>();

	public TestApplication(final String anApplicationName, 
			               final String aPropertiesFileName,
			               final String atargetEnvironmentAsString)
	{
		this.name = anApplicationName;
		this.propertiesFileName = aPropertiesFileName;
		this.targetEnvironmentAsString = atargetEnvironmentAsString;
		this.applicationProperties = PropertiesUtil.loadProperties(propertiesFileName);

		init();
	}

	public TestApplication(final String anApplicationName)
	{
		this.name = anApplicationName;
		this.propertiesFileName = getDefaultPropertiesFileName(anApplicationName);
		this.targetEnvironmentAsString = getDefaultEnvironmentAsString(anApplicationName);
		this.applicationProperties = PropertiesUtil.loadProperties(propertiesFileName);

		init();
	}

	public TestApplication(final File aPropertiesFile)
	{
		this.name = removeExtention(aPropertiesFile.getName());
		this.propertiesFileName = aPropertiesFile.getAbsolutePath();
		this.targetEnvironmentAsString = getDefaultEnvironmentAsString(name);
		this.applicationProperties = PropertiesUtil.loadProperties(propertiesFileName);

		//init();  // do not init here !!!
	}
	
	private String removeExtention(String filename)
	{
		int pos = filename.lastIndexOf('.');
		if (pos == -1) return filename;
		
		return filename.substring(0, pos);
	}

	private String getDefaultEnvironmentAsString(String anApplicationName)
	{
		TargetEnvironment targetEnv = ExecutionRuntimeInfo.getInstance().getTestEnvironment();

		if (targetEnv == null) {
			SysNatException exception = new SysNatException(
			        "No (valid) target environment defined in settings.config!");
			exception.printStackTrace();
			return "<Unknown target environment>";
		}

		return targetEnv.name();
	}

	private String getDefaultPropertiesFileName(String anApplicationName)
	{
		String propertiesPath = ExecutionRuntimeInfo.getInstance().getPropertiesPath();

		if (propertiesPath == null) {
			SysNatException exception = new SysNatException("No (valid) properties.execution file defined!");
			exception.printStackTrace();
		}

		return propertiesPath + "/" + anApplicationName + ".properties";
	}

	private void init()
	{
		String result = (String) applicationProperties.get("isWebApplication");
		isWebApplication = "true".equalsIgnoreCase(result);
		result = (String) applicationProperties.get("withLogin");
		withLogin = "true".equalsIgnoreCase(result);

		if (withLogin) {
			addStartParameter(applicationProperties);
		}
	}

	public List<String> getConfiguredEnvironments() 
	{
		List<String> toReturn = new ArrayList<>();
		Object value = applicationProperties.get("StartParameter");
		if (value == null) return toReturn;
		String startParameter = value.toString();
		List<String> params = applicationProperties.keySet().stream()
		                      .map(key -> key.toString())
		                      .filter(key -> key.endsWith(startParameter))
		                      .collect(Collectors.toList());

		
		for (String param : params) 
		{
			String prefix = name.toLowerCase() + '.';
			if (param.startsWith(prefix)) {
				param = param.substring(prefix.length());
			}
			SysNatConstants.TargetEnvironment[] environments = SysNatConstants.TargetEnvironment.values();
			for (SysNatConstants.TargetEnvironment env : environments) 
			{
				String envName = env.name().toLowerCase();
				if (param.startsWith(envName) && ! toReturn.contains(envName)) {
					toReturn.add(env.name());
				}
			}
		}
		
		return toReturn;
	}

	public String getProperty(String key)
	{
		String propertyKey = (targetEnvironmentAsString + "." + key).toLowerCase();
		return applicationProperties.getProperty(propertyKey);
	}

	private void addStartParameter(final Properties applicationProperties)
	{
		if (isWebApplication) {
			loginParameter.put(SysNatConstants.WebLoginParameter.URL,
			        getLoginParameter(applicationProperties, SysNatConstants.WebLoginParameter.URL));
		}
		if (withLogin) {
			loginParameter.put(SysNatConstants.WebLoginParameter.LOGINID,
			        getLoginParameter(applicationProperties, SysNatConstants.WebLoginParameter.LOGINID));
			loginParameter.put(SysNatConstants.WebLoginParameter.PASSWORD,
			        getLoginParameter(applicationProperties, SysNatConstants.WebLoginParameter.PASSWORD));
		}
	}

	private String getLoginParameter(final Properties applicationProperties,
	        final SysNatConstants.WebLoginParameter parameter)
	{
		String propertyKey = (targetEnvironmentAsString + ".login." + parameter.name()).toLowerCase();
		String propertyValue = applicationProperties.getProperty(propertyKey);

		if (propertyValue == null) {
			propertyKey = name.toLowerCase() + "." + propertyKey;
			propertyValue = applicationProperties.getProperty(propertyKey);
		}

		if (propertyValue == null) {
			String errorMessage = ERR_MSG_BUNDLE.getString("TestApplicationEnvironmentMismatch_Error");
			errorMessage = errorMessage.replace("XXX", propertyKey).replace("YYY", propertiesFileName).replace("ZZZ",
			        targetEnvironmentAsString);
			String helpMessage = ERR_MSG_BUNDLE.getString("TestApplicationEnvironmentMismatch_Help");
			helpMessage = helpMessage.replace("XXX", propertyKey).replace("YYY", name);

			ErrorPageLauncher.doYourJob(errorMessage, helpMessage, ERR_MSG_BUNDLE.getString("InitialisationError"));
		}

		if (propertyValue == null)
			return null;

		return propertyValue.trim();
	}

	public boolean isWebApplication() {
		return isWebApplication;
	}

	@Override
	public String toString() {
		return getName();
	}

	public String getName() {
		return name;
	}

	public HashMap<SysNatConstants.WebLoginParameter, String> getLoginParameter() {
		return loginParameter;
	}

	public String getStartParameterValue()
	{
		String startParameter = applicationProperties.get("StartParameter").toString();
		String toReturn = getProperty(startParameter);

		if (toReturn == null) {
			String errorMessage = "Required property <b>" + startParameter + "</b> not found in <b>"
			        + propertiesFileName + "</b>.";
			ErrorPageLauncher.doYourJob(errorMessage, "Assure that the required property is present and valid.",
			        ERR_MSG_BUNDLE.getString("InitialisationError"));
			System.exit(1);
		}

		return toReturn;
	}
}