
echo off

echo.
echo ########################################################
echo Creating SysNat Update...
echo --------------------------------------------------------
echo.

echo Step 1: Preparing update...
echo.

set sourceFolder="./sources"
cd %sourceFolder%/sysnat.natural.language.executable.examples/src/main/resources/batfiles
call prepareUpdateCreation.bat
cd ../../../../../..


echo --------------------------------------------------------
echo.
echo Step 2: Zipping sources folder...
echo.

set targetFile=SourcesUpdate.zip
set targetFolder=L:\oberratr\SysNat\update
set zipExe="C:\Program Files\7-Zip\7z.exe"

if exist %targetFolder%\%targetFile% (
   del /s %targetFolder%\%targetFile%
) 

%zipExe% a %targetFolder%\%targetFile% %sourceFolder%

echo.
echo --------------------------------------------------------
echo.
echo Step 3: Zipping Maven SysNat folder...
echo.

set sourceFolder=".\maven\localRepository\com\iksgmbh"
set targetFile="MavenJarsUpdate.zip"

if exist %targetFolder%\%targetFile% (
    del /s %targetFolder%\%targetFile%
)
%zipExe% a %targetFolder%\%targetFile% %sourceFolder%

echo.
echo --------------------------------------------------------
echo.
echo Step 4: Zipping jre security...
echo.

set sourceFolder=".\java\jre\lib\security"
set targetFile="JreSecurity.zip"

if exist %targetFolder%\%targetFile% (
    del /s %targetFolder%\%targetFile%
)
%zipExe% a %targetFolder%\%targetFile% %sourceFolder%

echo.
echo --------------------------------------------------------
echo Done.
echo ########################################################
pause