echo off
echo.
set count=0

cd ../../../..
for /D %%a IN ("reports/*") DO set /a count+=1
echo Deleting %count% report(s) in "./SysNat/sources/sysnat.natural.language.executable.examples/reports"...
FOR /D %%p IN ("reports/*") DO rmdir "%%p" /s /q

echo Done.

echo.
pause