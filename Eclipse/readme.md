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
        ../SysNat/java/bin/javaw.exe
                              
4. Extract the **EclipseSysNatConfig.zip** file to use the predefined *SysNat* workbench. It contains metadata for the SysNat Eclipse workspace. Many useful settings are predefined in this metadata such as Java and Maven settings as well as file associations, launch configurations and a customized tool bar which has been optimized for the use of non-technical users.

5. If you cannot access the public maven repository or if you wish to use a special repository such as Nexus or Artefactory, then add a corresponding mirror section in to "./SysNat/maven/conf/settings.xml".

6. Start Eclipse by using the batch file **SysNatEclipse.bat** from the SysNat root directory. 

7. Import the file "./SysNat/sources/sysnat.parent/pom.xml" as a maven project. This will import all nine SysNat modules. Eclipse must be able to compile all code  and build all projects successfully.

8. After importing the maven projects, assure the following settings are made and the follwoing configurations are available:
- In the preferences the Maven installation is set to "../maven" and in the Maven user settings the Global Settings are set to '..\maven\conf\settings.xml' (let the default value for 'User settings' unchanged). Make sure that Local Repository is '../maven/localRepository' (This value is read from the settings.xml). 
- workspace encoding is  "UTF-8"
- a Maven-Clean-Install-All-skipTest launch configuration exists
- a ClassLevelTest JUnit launch configuration exists
- a ModuleLevelTest JUnit launch configuration exists
- a SystemLevelTest JUnit launch configuration exists
- a SysNatTestingExecutor Java Application launch configuration exists
- an external tool launch configuration Delete-Test-Reports exist
- an external tool launch configuration Delete PDF-files-in-Download-directory exists
- a Window working set "SysNat User View" exists in the Package Explorer that only lists ExecutableExample, reports, help, testdata and settings.config (this view is supposed to be used by a SysNat domain user without technical skills).


9. Run the following launch configurations in the following order:
- Maven-Clean-Install-All-skipTest (Maven build must be successfull!)
- Clean and build all projects (Menu Run-Clean...)
- Run-All-ClassLevelTests 
- Run-All-ModuleLevelTests 
- Run-All-SystemLevelTests 
There must be not a single failing test! In case there is one, restart it.

________________________________________
Santander Consumer Technology Services GmbH – Sitz Mönchengladbach – HRB 7090 – AG Mönchengladbach – Geschäftsführer: José Álvarez Giráldez; Adresse: Madrider Straße 1, 41069 Mönchengladbach.
________________________________________
HAFTUNGSAUSSCHLUSS / DISCLAIMER
http://www.santander.de/disclaimer 
