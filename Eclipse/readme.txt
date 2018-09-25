If you plan to use SysNat with Eclipse, put a Photon Eclipse IDE for Java Developers package into this directory.
Use SysNatEclipse.bat to start Eclipse with the SysNat workspace.
SysNatEclipse.bat expects to find the eclipse.exe with the relative path "./SysNat/Exclipse/eclipse.exe".

The workspace directory in the SysNat GitHub repository contains Eclipse-project and settings file as well as a complete Eclipse workspace (.metadata). Either you can use this predefined workspace or you build your own. In the later case consider the following steps:


Before starting eclipse, assure to keep the following preconditions:

a) Configure the Java version to be used in the 'eclipse.ini' file by adding the following lines before the -vmargs line:
-vm
..\java\bin\javaw.exe

b) Check '..\maven\conf\settings.xml'. You may add an additional repositories (e.g. Nexus or Artifactory used in the dev environment of your company).

1. Having started Eclipse the first time, assure that in the Preferences the Maven installation is to "../maven" and that in the Maven user settings the Global Settings are set to '..\maven\conf\settings.xml (let the default value for 'User settings' unchanged). Make sure that Local Repository is '../../maven/localRepository' (This value is read from the settings.xml).

2. Set workspace encoding to "UTF-8".
 
3. Import all maven project in "./SysNat/workspace".

4. Call Maven Clean and install on the sys.parent project and make sure that all projects are build correctly and all class level tests are green.

5. Run class level tests with JUnit test "./SysNat/workspace/sysnat.quality.assurance/src/test/java/com/iksgmbh/sysnat/test/ClassLevelTestExecutor.java" and make sure all tests are green.

6. Run module level tests with JUnit test "./SysNat/workspace/sysnat.quality.assurance/src/test/java/com/iksgmbh/sysnat/test/ModuleLevelTestExecutor.java" and make sure all tests are green.

7. Run system level tests with "./SysNat/workspace/sysnat.quality.assurance/src/test/java/com/iksgmbh/sysnat/test/SystemLevelTestExecutor.java" and make sure all tests are green.

