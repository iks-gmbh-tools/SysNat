package de.iksgmbh.sysnat.docing.helper;

import java.io.File;
import java.util.Properties;

import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

public class SystemPropertyLoader
{
	public static final String DOCING_CONFIG_FILE_NAME = "sources/sysnat.natural.language.executable.examples/docing.config";
	public static final String PROPERTIES_PATH = "../sysnat.test.runtime.environment/src/main/resources/execution_properties";
	public static final String PROPERTIES_FILENAME = "execution.properties";
	public static final String EXECUTUION_PROPERTIES = PROPERTIES_PATH + "/" +PROPERTIES_FILENAME;
	
	public static void doYourJob(String propertyFile) 
	{
		addToSystemProperties(propertyFile);
	}
	
	private static void addToSystemProperties(final String propertiesFilename)  
	{
		final File f = new File(propertiesFilename);
		if ( ! f.exists() ) 
		{
			RuntimeException e = new RuntimeException("The following necessary file is missing: " + f.getAbsolutePath());
			e.printStackTrace();
			throw e;
		}

		final Properties properties = new Properties();
		SysNatFileUtil.loadPropertyFile(f, properties);

		for (Object key : properties.keySet()) {
			System.setProperty((String) key, properties.getProperty((String) key));
		}
	}
}
