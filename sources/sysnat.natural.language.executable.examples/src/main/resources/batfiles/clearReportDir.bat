echo off
echo.
set count=0
for /D %%a IN ("reports\*") DO set /a count+=1
echo Deleting %count% report(s) in sysnat.natural.language.executable.examples/reports...
FOR /D %%p IN ("reports\*") DO rmdir "%%p" /s /q
echo.
echo Done.