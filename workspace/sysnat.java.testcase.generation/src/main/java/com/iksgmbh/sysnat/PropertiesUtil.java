package com.iksgmbh.sysnat;

import java.io.File;
import java.util.Properties;

import com.iksgmbh.sysnat.utils.SysNatFileUtil;

public class PropertiesUtil 
{

	public static Properties loadProperties(String configFileName) 
	{
		final File f = new File(configFileName);
        
		if ( ! f.exists() ) {
            throw new RuntimeException("The following necessary file is missing" + configFileName);
        }
        
		Properties properties = new Properties();
        SysNatFileUtil.loadPropertyFile(f, properties);
		
        return properties;
	}

}
