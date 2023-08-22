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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.exception.SysNatConfigurationException;
import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.helper.ErrorPageLauncher;
import com.iksgmbh.sysnat.common.utils.PropertiesUtil;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.ApplicationLoginParameter;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.ApplicationType;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.TargetEnvironment;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;

/**
 * Holds attributes of the application under test.
 * 
 * @author Reik Oberrath
 */
public class TestApplication
{
	private static final ResourceBundle ERR_MSG_BUNDLE = ResourceBundle.getBundle("bundles/ErrorMessages", Locale.getDefault());

	private String name;
	private String targetEnvironmentAsString;
	private String propertiesFileName;
	private ApplicationType applicationType;
	private boolean withLogin;
	private Map<String, String> applicationProperties;

	public TestApplication(final String anApplicationName, 
			               final String aPropertiesFileName,
			               final String atargetEnvironmentAsString)
	{
		this.name = anApplicationName;
		this.propertiesFileName = aPropertiesFileName;
		this.targetEnvironmentAsString = atargetEnvironmentAsString;
		init();
	}

	public TestApplication(final String anApplicationName)
	{
		this.name = anApplicationName;
		this.propertiesFileName = getDefaultPropertiesFileName(anApplicationName);
		this.targetEnvironmentAsString = getDefaultEnvironmentAsString(anApplicationName);
		init();
	}

	public TestApplication(final File aPropertiesFile)
	{
		this.name = removeExtention(aPropertiesFile.getName());
		this.propertiesFileName = aPropertiesFile.getAbsolutePath();
		this.targetEnvironmentAsString = getDefaultEnvironmentAsString(name);
		init();
	}
	
	private Map<String, String> readProperties() 
	{
		Map<String, String> toReturn = new HashMap<>();

		Properties properties = PropertiesUtil.loadProperties(propertiesFileName);
		properties.entrySet().forEach(entry -> toReturn.put(entry.getKey().toString().toLowerCase(), 
				                                            entry.getValue().toString()));
		return toReturn;
		
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
		this.applicationProperties = readProperties();

		String appTypeAsString = applicationProperties.get("ApplicationType");
		if (appTypeAsString == null ) appTypeAsString = applicationProperties.get("ApplicationType".toLowerCase());
		if (appTypeAsString == null ) {
			throw new SysNatConfigurationException("ApplicationType not defined in " + this.propertiesFileName);
		}
		appTypeAsString = SysNatStringUtil.firstCharToUpperCase(appTypeAsString);
		applicationType = ApplicationType.valueOf(appTypeAsString);
		String withLoginAsString = (String) applicationProperties.get("withLogin".toLowerCase());
		withLogin = "true".equalsIgnoreCase(withLoginAsString);
	}
	
	public List<String> getElementAppications() 
	{
		if (! isCompositeApplication()) {
			return new ArrayList<>();
		}
		
		String value = getProperty(SysNatConstants.APP_COMPOSITES.toLowerCase());
		if (value == null) {
			return new ArrayList<>();
		}
		
		return SysNatStringUtil.toList(value, ",");
	}

	public List<String> getConfiguredEnvironments() 
	{
		if (applicationType == ApplicationType.Composite) {
			return getCompositeConfiguredEnvironments();
		}
		List<String> toReturn = new ArrayList<>();
		List<String> parameterKeys = getApplicationStartParameters();
		
		Arrays.asList(SysNatConstants.ApplicationLoginParameter.values()).forEach(key -> parameterKeys.add(key.name()));

		StringBuffer sb = new StringBuffer();
		parameterKeys.forEach(entry -> sb.append(entry.toString())
				                                 .append(System.getProperty("line.separator")));
		
		for (String paramKey : parameterKeys) 
		{
			List<String> params = applicationProperties.keySet().stream()
                    .map(key -> key.toString().trim())
                    .filter(key -> key.endsWith(paramKey) || key.toLowerCase().endsWith(paramKey.toLowerCase()))
                    .collect(Collectors.toList());
			
			for (String param : params) 
			{
				SysNatConstants.TargetEnvironment[] environments = SysNatConstants.TargetEnvironment.values();
				for (SysNatConstants.TargetEnvironment env : environments) 
				{
					String envName = env.name();
					if (param.toLowerCase().startsWith(envName.toLowerCase()) && ! toReturn.contains(envName)) {
						toReturn.add(envName);
					}
				}
			}
		}
	
		return toReturn;
	}
	
	public String getEnvironmentDisplayName(String env) 
	{
		if (isCompositeApplication()) 
		{
			try {
				String envs = applicationProperties.get("EnvironmentNames");
				String[] splitResult = envs.split(",");
				TargetEnvironment[] values = SysNatConstants.TargetEnvironment.values();
				for (int i = 0; i < values.length; i++) {
					if (values[i].name().equals(env)) {
						return splitResult[i];
					}
				}
			} catch (Exception e) {
				// ignore
			}
			throw new SysNatException("Error: cannot determine DisplayName for Environment '" + env + "'.");
		}
		
		 Optional<String> candidate = applicationProperties.keySet().stream()
         .map(key -> key.toString().trim())
         .filter(key -> key.toLowerCase().endsWith("displayname") && key.toLowerCase().startsWith(env.toLowerCase()))
         .findFirst();
		 if (candidate.isPresent()) {
			 return applicationProperties.get(candidate.get());
		 }
		 return null;
	}
	
	public void checkNumberOfComposites() 
	{
		List<String> apps = getElementAppications();
		
		if (apps.size() < 2) 
		{
			String errorMessage = "The test application <b>" + name + "</b> has too few composits.";
			String helpMessage = "Configure at least two of those in its application properties file with property <b>Composites</b>!";
			ErrorPageLauncher.doYourJob(errorMessage, helpMessage, ERR_MSG_BUNDLE.getString("InitialisationError"));
			throw new RuntimeException(errorMessage);
		}		
	}

	private List<String> getCompositeConfiguredEnvironments()
	{
		List<String> toReturn = null;
		List<String> apps = getElementAppications();
		checkNumberOfComposites();
		
		boolean firstApp = true;
		String testApp = null;
		
		try {
			for (String app : apps) 
			{
				testApp = app;
				if (firstApp) {
					firstApp = false;
					toReturn = new TestApplication(app).getConfiguredEnvironments();
				} else {
					List<String> envs = new TestApplication(app).getConfiguredEnvironments();
					toReturn = toReturn.stream().filter(env -> envs.contains(env)).collect(Collectors.toList());
				}
			}	
		} catch (RuntimeException e) {
			String errorMessage = "The test application <b>" + testApp + "</b> is unknown";
			String helpMessage = "Configure composits of <b>" + name + "</b> in its application properties file correctly!";
			ErrorPageLauncher.doYourJob(errorMessage, helpMessage, ERR_MSG_BUNDLE.getString("InitialisationError"));
			throw(e);
		}

		
		return toReturn;
	}

	public String getEnvProperty(String key)
	{
		if (isCompositeApplication()) {
			return applicationProperties.get(key);
		}
		String propertyKey = (targetEnvironmentAsString + "." + key).toLowerCase();
		return applicationProperties.get(propertyKey);
	}

	public String getProperty(String key)
	{
		String toReturn = applicationProperties.get(key);
		if (toReturn == null) {
			toReturn = applicationProperties.get(key.toLowerCase());
		}
		return toReturn;
	}

	public boolean isWebApplication() {
		return applicationType == ApplicationType.Web;
	}

	public boolean isSwingApplication() {
		return applicationType == ApplicationType.Swing;
	}
	
	public boolean isCompositeApplication() {
		return applicationType == ApplicationType.Composite;
	}
	
	
	@Override
	public String toString() {
		return getName();
	}

	public String getName() {
		return name;
	}

	public Map<SysNatConstants.ApplicationLoginParameter, String> getLoginParameter() 
	{
		Map<ApplicationLoginParameter, String> toReturn = new HashMap<>();
		List<ApplicationLoginParameter> paramKeys = Arrays.asList(SysNatConstants.ApplicationLoginParameter.values());
		List<String> keysAsString = paramKeys.stream().map(key -> key.name()).collect(Collectors.toList());
		HashMap<String,String> parameterValues = getParameterValues(keysAsString);
		keysAsString.forEach(key -> toReturn.put(SysNatConstants.ApplicationLoginParameter.valueOf(key), 
				                                 parameterValues.get(key)));
		return toReturn;
	}


	private List<String> getApplicationStartParameters()
	{
		String startParameters = applicationType.getStartParameter();
		return SysNatStringUtil.toList(startParameters, ",");	
	}
	
	public HashMap<String, String> getStartParameterValues() {
		return getParameterValues(getApplicationStartParameters());
	}
	
	private HashMap<String, String> getParameterValues(List<String> keys)
	{
		HashMap<String, String> toReturn = new HashMap<String, String>();
		
		for (String paramKey : keys) 
		{
			paramKey = paramKey.trim();
			String env = ExecutionRuntimeInfo.getInstance().getTestEnvironmentName();
			String lookupKey = env + "." + paramKey;
			String value = applicationProperties.get(lookupKey.toLowerCase());
			if (value == null) {
				// There is no value that is specific to the currently defined environment! 
				// So search for a common application setting:
				lookupKey = paramKey;
				value = applicationProperties.get(lookupKey.toLowerCase());
				if (value == null) {
					throw new RuntimeException("In file '" + name + ".properties' "
							+ "the property '" + env + "." + paramKey + "' is not defined. "
							+ "Either define it or change environment for execution!");
				}
			}

			toReturn.put(paramKey, value);
		}
		
		return toReturn;
	}
	
	public ApplicationType getType() {
		return applicationType;
	}
	
	public boolean withLogin() {
		return withLogin;
	}

	public Map<String, String> getSystemProperties()
	{
		String systemPropertyIdentifier = "SystemProperty";
		Map<String, String> toReturn = new HashMap<String, String>();
		String testEnvironmentName = ExecutionRuntimeInfo.getInstance().getTestEnvironmentName();
		List<String> systemPropertyKeyList = applicationProperties.keySet().stream()
				                                                  .filter(key -> key.contains(systemPropertyIdentifier))
				                                                  .collect(Collectors.toList());

		for (String key : systemPropertyKeyList) {
			if (key.startsWith(testEnvironmentName)) {
				String systemPropertyKey = key.substring(testEnvironmentName.length() + systemPropertyIdentifier.length() + 2);
				toReturn.put(systemPropertyKey, applicationProperties.get(key));
			}
		}
		
		for (String key : systemPropertyKeyList) 
		{
			TargetEnvironment[] targetEnvironments = SysNatConstants.TargetEnvironment.values();
			boolean ok = true;
			for (TargetEnvironment targetEnvironment : targetEnvironments) {
				if (key.startsWith(targetEnvironment.name())) {
					ok = false;
					break;
				}
			}
			
			if (! ok) continue;
			
			String systemPropertyKey = key.substring(systemPropertyIdentifier.length() + 1);
			if (! toReturn.containsKey(systemPropertyKey)) {
				toReturn.put(systemPropertyKey, applicationProperties.get(key));
			}
		}
			
		return toReturn;
	}
	
}