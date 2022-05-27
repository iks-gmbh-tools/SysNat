echo off

set CURR_DIR1=%cd%
set CURR_DIR2=%CURR_DIR1:~24%

if %CURR_DIR2%==\Notepad++\App\Notepad++ (
   set SYSNAT_ROOT_PATH=../../..
) else (
   set SYSNAT_ROOT_PATH=..
)

cd %SYSNAT_ROOT_PATH%/sources/sysnat.parent
set JAVA_HOME=%SYSNAT_ROOT_PATH%/java
set PATH=%PATH%;%JAVA_HOME%

"../../maven/bin/mvn" clean install -DskipTests
pause