If you plan to use SysNat with Eclipse, put a Photon Eclipse IDE for Java Developers package into this directory. Use SysNatEclipse.bat from the SysNat root directory to start Eclipse with the SysNat workspace. The bat file expects to find the eclipse.exe with the relative path "./SysNat/Eclipse/eclipse.exe". The SysNat directory in Eclipse directory contains the metadata for the SysNat Eclipse workspace. Many useful settings are predefined in this metadata such as the Java and Maven version to be used as well as file associations, encoding type and launch configurations.

Having started SysNat Eclipse the first time, assure the following settings:

1a) In the preferences the Maven installation is set to "../maven" and that in the Maven user settings the Global Settings are set to '..\maven\conf\settings.xml' (let the default value for 'User settings' unchanged). Make sure that Local Repository is '../../maven/localRepository' (This value is read from the settings.xml). 
1b) If you wish to use a specific artefact repository (such as an Artifactory or Nexus installation) you have to define this in the file '..\maven\conf\settings.xml'.

2. The workspace encoding is set to "UTF-8". 3. 

3. All seven maven project in "./SysNat/workspace" are available. 

4. A Maven-Clean-Install-All-skipTest launch configuration is availble and its execution is successful.

5. A Run-All-ClassLevelTests launch configuration is available and its execution results in 100% success.

6. A Run-All-ModuleLevelTests launch configuration is available and its execution results in 100% success

7. A Run-All-SystemLevelTests launch configuration is available and its execution results in 100% success.

8. Two external tool launch configurations are available, one to delete all existing reports from the standard report directory and one to delete all PDFs from the Download directory.




