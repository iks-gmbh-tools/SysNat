echo off
echo.
set count=0

set USER_DOWNLOAD_DIR=%userprofile%\Downloads

for %%a IN ("%USER_DOWNLOAD_DIR%\*.pdf") DO set /a count+=1
echo Deleting %count% PDFs in %USER_DOWNLOAD_DIR%...
FOR %%p IN ("%USER_DOWNLOAD_DIR%\*.pdf") DO del "%%p"
echo.
echo Done.
