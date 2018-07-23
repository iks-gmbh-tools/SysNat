package com.iksgmbh.sysnat.common.utils;

import java.io.File;
import java.util.Properties;

public class PropertiesUtil 
{
	public static Properties loadProperties(String propertiesFileName) 
	{
		final File f = new File(propertiesFileName);
        
		if ( ! f.exists() ) {
            String message = "The following necessary file is missing: " + f.getAbsolutePath();
			System.err.println(message);
            throw new RuntimeException(message);
        }
        
		Properties properties = new Properties();
        SysNatFileUtil.loadPropertyFile(f, properties);
		
        return properties;
	}

}
