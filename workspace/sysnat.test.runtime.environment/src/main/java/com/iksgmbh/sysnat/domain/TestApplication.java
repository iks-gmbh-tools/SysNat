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

import java.util.HashMap;
import java.util.Properties;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.utils.PropertiesUtil;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;

/**
 * Holds attributes of the application under test.
 * @author Reik Oberrath
 */
public class TestApplication 
{
   private String name;
   private String targetEnvironmentAsString;
   private boolean isWebApplication;
   private boolean withLogin;
   private Properties applicationProperties;
   private HashMap<SysNatConstants.WebLoginParameter,String> loginParameter = new HashMap<>();

   public TestApplication(final String anApplicationName,
                          final String propertiesFileName,
                          final String atargetEnvironmentAsString)
   {
      this.name = anApplicationName;
      this.targetEnvironmentAsString = atargetEnvironmentAsString;

      applicationProperties = PropertiesUtil.loadProperties(propertiesFileName);
      String result = (String) applicationProperties.get("isWebApplication");
      isWebApplication = "true".equalsIgnoreCase(result);
      result = (String) applicationProperties.get("withLogin");
      withLogin = "true".equalsIgnoreCase(result);

      if (withLogin) {
         addStartParameter(applicationProperties);
      }
   }

   public String getProperty(String key)
   {
      String propertyKey = ( name + "." +
                           targetEnvironmentAsString + "." +
                               key
                           ).toLowerCase();

      return applicationProperties.getProperty(propertyKey);
   }

   public TestApplication(final String aApplicationUnderTest)
   {
      this(aApplicationUnderTest,
           ExecutionRuntimeInfo.getInstance().getPropertiesPath() + "/" + aApplicationUnderTest + ".properties",
           ExecutionRuntimeInfo.getInstance().getTargetEnv().name());
   }

   private void addStartParameter(final Properties applicationProperties)
   {
      if (isWebApplication) {
         loginParameter.put(SysNatConstants.WebLoginParameter.URL, getLoginParameter(applicationProperties, SysNatConstants.WebLoginParameter.URL));
      }
      if (withLogin) {
         loginParameter.put(SysNatConstants.WebLoginParameter.LOGINID, getLoginParameter(applicationProperties, SysNatConstants.WebLoginParameter.LOGINID));
         loginParameter.put(SysNatConstants.WebLoginParameter.PASSWORD, getLoginParameter(applicationProperties, SysNatConstants.WebLoginParameter.PASSWORD));
      }
   }

   private String getLoginParameter(final Properties applicationProperties,
                            final SysNatConstants.WebLoginParameter parameter)
   {
      String propertyKey = ( name + "." +
                             targetEnvironmentAsString +
                             ".login." +
                             parameter.name()
                           ).toLowerCase();

      String propertyValue = applicationProperties.getProperty(propertyKey);
      if (propertyValue == null) {
         //ExceptionHandlingUtil.throwException("Missing application property '" + propertyKey + "'.");
         throw new SysNatException("Missing application property '" + propertyKey + "'.");
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
      return name;
   }

   public HashMap<SysNatConstants.WebLoginParameter, String> getLoginParameter() {
      return loginParameter;
   }

    public String getStartParameterValue()
    {
        String startParameter = applicationProperties.get("StartParameter").toString();
        return getProperty(startParameter);
    }
}