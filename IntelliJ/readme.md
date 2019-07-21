Setting up SysNat IntelliJ
==========================

Preconditions:
- a Java SDK 8 is available
- a IntelliJ installation is available (a recent community edition is fine)

Preparation:

0. The SysNat repository from GitHub has been downloaded on your local system.

1. Put the Java SDK into the subfolder "./SysNat/java" (expected path to java.exe is "./SysNat/java/bin/java.exe")

2. Put the IntelliJ installation into the directory of this readme file (expected path to idea.exe is "./SysNat/IntelliJ/bin/idea.exe"). Your subdirectory should look like this:

![](https://raw.github.com/iks-github/SysNatTesting/master/documentation/Figures/screenshots/IntelliJSubdir.jpg)

3. Extract the **IdeaSysNatConfig.zip** file to use the predefined *SysNat* workbench. It contains metadata for the SysNat IntelliJ project. Many useful settings are predefined in this metadata such as Java and Maven settings as well as file associations, launch configurations, a customized tool bar and a "SysNat User View" which has been optimized for the use of non-technical users.

4. If you want to access a special repository such as Nexus or Artefactory (instead of using public repositories for fetching third party libraries), then add a corresponding mirror section in file "./SysNat/maven/conf/settings.xml". 

5. Start IntelliJ by using the batch file **SysNatIntelliJ.bat** from the SysNat root directory. Open menu "File - Project Structure - SDKs" and make sure, that the "./SysNat/java" is used. Here, IntelliJ does not allow a relative path - define the correct absolute path for your local system.

6. Assure that in the _Settings_ menu, the *Maven home directory* is set to "./SysNat/maven".  The *Local Repository* should be "./SysNat/maven/localRepository" (This value is read from the settings.xml). If the later is not the case, set *User settings file* to './SysNat/maven/conf/settings.xml'. 

7. Import all nine modules separately using the menu "Module - New - Model from Existing Sources...". Take care to perform the import with the option "Import Maven projects automatically". Unfortunately, IntelliJ does not allow to import a parent module with all submodules. However, the necessary nine imports must be done only once.

*Attention:* Do neither use menu "File - Project from Existing Sources..." nor menu "File - Open". Both would create a totally new project and all predefined project settings would be lost. 

8. Assure the following settings and configurations:
- a Maven-Clean-Install-All-skipTest launch configuration 
- a Run-All-ClassLevelTests launch configuration (ClassLevelTestExecutor)
- a Run-All-ModuleLevelTests launch configuration (ModuleLevelTestExecutor)
- a Run-All-SystemLevelTests launch configuration (SystemLevelTestExecutor)
- a launch configuration to clean the report directory from existing reports (red icon in the tool bar)
- a launch configuration to clean the download directory from existing PDF files (orange icon in the tool bar)
- a "SysNat User View" scope exists in the Project View that only lists ExecutableExample, reports, help, testdata and settings.config (this view is supposed to be used by a SysNat domain user without technical skills).

9. Call the following launch configurations in the following order:
- Maven-Clean-Install-All-skipTest
- ClassLevelTestExecutor 
- ModuleLevelTestExecutor
- SystemLevelTestExecutor 
There must be not a single failing test! In case there is one, restart it.

10. One of the options in the project view is **SysNat User View**. It is defined as a scope for non-developers who use the SysNat workbench. Activate this option if you plan to provide IntelliJ to your collegues who use SysNat from their business point of view without technical skills and experience as developers.

11. You may delete within the SysNat root directory all folders and files not needed for your context (e.g. folder documentation and Eclipse and the files .gitignore or SysNat_Eclipse.bat).