If you plan to use SysNat with IntelliJ, put a Idea IntelliJ Community version into this directory.
Use SysNatIntelliJ.bat to start IntelliJ with the SysNat workspace.
SysNatIntelliJ.bat expects to find the idea.exe with the relative path "./SysNat/IntelliJ/idea.exe".

To make SysNat running correctly follow the following steps:
 
1. Having started IntelliJ the first time, create a new project by importing "./SysNat/workspace/sysnat.parent/pom.xml".

2. In the setting menu configure the Maven installation in "./SysNat/Maven" to be used and the file "./SysNat/Maven/config/settings.xml" as user settings.

3. Call Maven Clean and install on the sys.parent project and make sure that all projects are build correctly and all class level tests are green.

4. Run module level tests with JUnit test "./SysNat/workspace/sysnat.quality.assurance/src/test/java/com/iksgmbh/sysnat/test/ModuleLevelTestExecutor.java" and make sure all tests are green.

5. Run system level tests with "./SysNat/workspace/sysnat.quality.assurance/src/test/java/com/iksgmbh/sysnat/test/SystemLevelTestExecutor.java" and make sure all tests are green.

