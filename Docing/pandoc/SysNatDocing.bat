echo off
set filename=%1
set format=%2

rem note that prince does not generate toc without line numbers and content head line :-)
rem pandoc test.md -s -o test.pdf --pdf-engine=./prince/bin/prince.exe -V pagetitle='bubu' --toc -V toc-title='content'

echo Creating SysNat Document...

if "%format%" == "PDF" (
	
	pandoc SysNatDocing.md -s -o %filename%.pdf --pdf-engine=./prince/bin/prince.exe -V pagetitle='%filename%'
	echo %filename%.pdf created.

) else if "%format%" == "DOCX" (

	pandoc SysNatDocing.md -s -o %filename%.docx -V pagetitle='%filename%'
	echo %filename%.docx created.

) else if "%format%" == "HTML" (

	echo %filename%.html
	pandoc SysNatDocing.md -s -o %filename%.html -V pagetitle='%filename%'
	echo %filename%.html created.
	
) else (
	
	echo Unbekannes Format: %format%
	pause
	
)



