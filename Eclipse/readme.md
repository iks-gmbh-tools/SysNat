Setting up SysNat Eclipse
=========================

Preconditions:
- a Java SDK 8 is available
- a Eclipse installation (v4.12) is available (at best the Java Developers package)

Preparation:

0. The SysNat repository from GitHub has been cloned or downloaded on your local system.
1. Put the Java SDK into the subfolder "./SysNat/java" (expected path to java.exe is "./SysNat/java/bin/java.exe")
2. Put the Eclipse installation into the directory of this readme file (expected path to eclipse.exe is "./SysNat/Eclipse/eclipse.exe"). 
3. Adapt eclipse.ini to find Java by adding the following lines (before the vmargs setting !!!)
        -vm
        <path on your local system>/SysNat/java/bin/javaw.exe
4. Extract the **EclipseSysNatConfig.zip** file to use the predefined *SysNat* workbench. It contains metadata for the SysNat Eclipse workspace. Many useful settings are predefined in this metadata such as Java and Maven settings as well as file associations, launch configurations and a customized tool bar which has been optimized for the use of non-technical users.
5. Start Eclipse by using the batch file **SysNatEclipse.bat** from the SysNat root directory. 
6. Assure the following settings:
- In the preferences the Maven installation is set to "../maven" and that in the Maven user settings the Global Settings are set to '..\maven\conf\settings.xml' (let the default value for 'User settings' unchanged). Make sure that Local Repository is '../maven/localRepository' (This value is read from the settings.xml). 
If you wish to use a specific artefact repository (such as an Artifactory or Nexus installation) you have to define a corresponding mirror section in the file '..\maven\conf\settings.xml'. If the relative paths defined do not work replace those by absolute values valued on your local system.
- The workspace encoding is set to "UTF-8".

- The collection of launch configuration contains 
-- a Maven-Clean-Install-All-skipTest launch configuration 
-- a Run-All-ClassLevelTests launch configuration 
-- a Run-All-ModuleLevelTests launch configuration 
-- a Run-All-SystemLevelTests launch configuration 

- The collection of external tool launch configuration contains 
-- a launch configuration to clean the report directory from existing reports
-- a launch configuration to clean the download directory from existing PDF files

7. Import the file "./SysNat/sources/sysnat.parent/pom.xml" as maven project. This will import all nine SysNat modules.

8. Call the following launch configurations in the following order:
- Maven-Clean-Install-All-skipTest
- Run-All-ClassLevelTests 
- Run-All-ModuleLevelTests 
- Run-All-SystemLevelTests 

There must be no failing test!

