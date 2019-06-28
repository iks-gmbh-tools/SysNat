del ..\..\..\..\reports\*.html /s/q
FOR /D %%p IN ("..\..\..\..\reports\*") DO rmdir "%%p" /s /q
