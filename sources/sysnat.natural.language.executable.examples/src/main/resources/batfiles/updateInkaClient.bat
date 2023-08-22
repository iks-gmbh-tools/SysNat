echo off

rem find directories
set batStartDir=%cd%
set DIR_IDENTIFIER=%batStartDir:~-24%
if %DIR_IDENTIFIER%==\Notepad++\App\Notepad++ (
   cd ../../..
) else (
   cd ../../../../../..
)
set SYSNAT_ROOT_PATH=%cd%
cd java
set JAVA_HOME=%cd%
set path=%JAVA_HOME%/bin;%path%
cd ../sources/sysnat.natural.language.executable.examples

rem output infos about used versions
echo.
echo # Paths found:
echo batStartDir=%batStartDir%
echo SYSNAT_ROOT_PATH=%SYSNAT_ROOT_PATH%
echo JAVA_HOME=%JAVA_HOME%
echo.
echo # Java versions on local maschine:
where java
echo.


java -cp ./target/classes;../sysnat.common/target/classes  com.iksgmbh.sysnat.helper.InkaClientUpdater %sourcePath% %targetPath%
echo.
pause