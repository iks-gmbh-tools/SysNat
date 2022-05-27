echo off
echo.
echo ###############################################
echo ###########  SYSNAT LIGHT SETUP   #############
echo ###############################################
echo.
echo Richte SysNatLight ein...

set TARGET_DIR_PARENT=C:
set TARGET_DIR=%TARGET_DIR_PARENT%\SysNatLight

if exist %TARGET_DIR% (
  echo Verzeichnis %TARGET_DIR% existiert schon. 
  echo Setup wird abgebrochen.
  goto :END
)

set SOURCE_FOLDER=L:\oberratr\SysNat
set ZIP_EXE="C:\Program Files\7-Zip\7z.exe"

%ZIP_EXE% x %SOURCE_FOLDER%\SysNatLight.zip -o%TARGET_DIR_PARENT% -y

C:
cd %TARGET_DIR%

doUpdateHere.bat
echo Fertig.

:END
echo.
echo ###############################################
echo.
pause