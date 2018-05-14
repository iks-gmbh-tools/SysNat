package com.iksgmbh.sysnat.domain;

import java.util.HashMap;
import java.util.Properties;

import com.iksgmbh.sysnat.PropertiesUtil;
import com.iksgmbh.sysnat.helper.GenerationRuntimeInfo;
import com.iksgmbh.sysnat.utils.ExceptionHandlingUtil;
import com.iksgmbh.sysnat.utils.SysNatConstants.AppUnderTest;

public class TestApplication 
{
	private enum StartParameter { URL, LOGINID, PASSWORD };
	private AppUnderTest appUnderTest;
	private boolean isWebApplication;
	private HashMap<StartParameter,String> startParameter = new HashMap<>();

	
	public TestApplication(AppUnderTest aAppUnderTest) 
	{
		this.appUnderTest = aAppUnderTest;
		String propertiesFileName = GenerationRuntimeInfo.PROPERTIES_PATH + "/" + aAppUnderTest.name() + ".properties";
		Properties applicationProperties = PropertiesUtil.loadProperties(propertiesFileName);
		String result = (String) applicationProperties.get("isWebApplication");
		isWebApplication = "true".equalsIgnoreCase(result);
		addStartParameter(applicationProperties);
	}

	private void addStartParameter(final Properties applicationProperties) 
	{
		startParameter.put(StartParameter.URL, getParameter(applicationProperties, StartParameter.URL));
		startParameter.put(StartParameter.LOGINID, getParameter(applicationProperties, StartParameter.LOGINID));
		startParameter.put(StartParameter.PASSWORD, getParameter(applicationProperties, StartParameter.PASSWORD));
	}

	private String getParameter(final Properties applicationProperties, 
			                    final StartParameter parameter) 
	{
		String propertyKey = (getName() + "." +
				             GenerationRuntimeInfo.getInstance().getTargetEnv().name() + 
				             ".login." +
				             parameter.name()).toLowerCase();
		String propertyValue = applicationProperties.getProperty(propertyKey);
		if (propertyValue == null) {
			ExceptionHandlingUtil.throwException("Missing application property '" + propertyKey + "'.");
		}
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
		return appUnderTest.name();
	}

	public HashMap<StartParameter, String> getStartParameter() {
		return startParameter;
	}
}
