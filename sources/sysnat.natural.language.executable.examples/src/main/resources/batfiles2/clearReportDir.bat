echo off
echo.
set count=0

cd ../../../../reports

for /D %%a IN ("*") DO set /a count+=1
echo Deleting %count% report(s) in "./SysNat/sources/sysnat.natural.language.executable.examples/reports"...
FOR /D %%p IN ("*") DO rmdir "%%p" /s /q

echo.
echo Done.

