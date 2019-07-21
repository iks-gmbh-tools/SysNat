Setting up SysNat Eclipse
=========================

Preconditions:
- a Java SDK 8 is available
- a Eclipse installation (v4.12) is available (at best the Java Developers package)

Preparation:

0. The SysNat repository from GitHub has been downloaded on your local system.

1. Put the Java SDK into the subfolder "./SysNat/java" (expected path to java.exe is "./SysNat/java/bin/java.exe")

2. Put the Eclipse installation into the bin subdirectory (expected path of the start batch script to find the eclipse.exe is "./SysNat/Eclipse/bin/eclipse.exe"). Your Eclipse subdirectory should look like this:

![](https://raw.github.com/iks-github/SysNatTesting/master/documentation/Figures/screenshots/EclipseSubdir.jpg)


3. Adapt eclipse.ini so that Eclipse will find Java by a relative path. This is best done by adding the following _vm_ setting after the _openFile_ and before the _vmargs_ setting:

        ...
        openFile
        --launcher.appendVmargs
        -vm
        ..\..\java\bin\javaw.exe
        -vmargs
        ...
                              
4. Extract the **EclipseSysNatConfig.zip** file to use the predefined *SysNat* workbench. It contains metadata for the SysNat Eclipse workspace. Many useful settings are predefined in this metadata such as Java and Maven settings as well as file associations, launch configurations and a customized tool bar which has been optimized for the use of non-technical users.

5. If you cannot access the public *Maven* repository or if you wish to use a special repository such as Nexus or Artefactory, then add a corresponding mirror section into file "./SysNat/maven/conf/settings.xml".

6. Start Eclipse by using the batch file **SysNatEclipse.bat** from the SysNat root directory. Open menu "Windows - Preferences - Java - Installed JREs" and if the "./SysNat/java" installation is not selected or available, then add and select it.

7. Assure that in the preferences the Maven installation is set to "../../maven" and in the Maven user settings the Global Settings are set to '../../maven\conf\settings.xml' (let the default value for 'User settings' unchanged). Make sure that Local Repository is '../../maven/localRepository' (This value is read from the settings.xml). 

8. Import the file "./SysNat/sources/sysnat.parent/pom.xml" as a maven project. This will import all nine SysNat modules. Eclipse must be able to compile all code and build all projects successfully with no problems in the problem view.

9. After importing the maven projects, assure the following settings are made and the follwoing configurations are available:
- workspace encoding is  "UTF-8"
- a Maven-Clean-Install-All-skipTest launch configuration exists
- a ClassLevelTest JUnit launch configuration exists (ClassLevelTestExecutor)
- a ModuleLevelTest JUnit launch configuration exists (ModuleLevelTestExecutor)
- a SystemLevelTest JUnit launch configuration exists (SystemLevelTestExecutor)
- a SysNatTestingExecutor Java Application launch configuration exists
- an external tool launch configuration Delete-Test-Reports exist
- an external tool launch configuration Delete PDF-files-in-Download-directory exists
- a Window working set "SysNat User View" exists in the Package Explorer that only lists ExecutableExample, reports, help, testdata and settings.config (this view is supposed to be used by a SysNat domain user without technical skills).

10. Open for module *sysnat.quality.assurance* the properties and in the submenu *Java Build Path - Projects* add all SysNat modules. This is necessary for the *ClassLevelTestExecutor* to find all class level tests.

11. Run the following launch configurations in the following order:
- Maven-Clean-Install-All-skipTest 
- Clean and build all projects (Menu *Run-Clean...*)
- ClassLevelTestExecutor 
- ModuleLevelTestExecutor
- SystemLevelTestExecutor 

There must be not a single failing test! In case there is one, restart it.

11. Within the *Package Explorer* the Windows working **SysNat User View** is defined. Activate it if you plan to provide Eclipse to your collegues who use _SysNat_ from their business point of view without technical skills and experience as developers. If this view is empty, select "Edit active working set" and add from module *sysnat.natural.language.executable.examples* the directories *ExecutableExample*, *reports*, *help*, *TestData* as well as the file *settings.config*.

12. You may delete within the SysNat root directory all folders and files not needed for your context (e.g. folder documentation and IntelliJ and the files .gitignore or SysNat_IntelliJ.bat).