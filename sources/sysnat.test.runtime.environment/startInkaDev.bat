@echo off

set inkaSourcePath=C:\dev\sourcen\Inka
set javaPath=C:\dev\Java\jdk-8u221
set javaMainClass=com.cnm.client.aia.system.Main


rem allgemeine Flags
set JVM_OPTS=-client
set JVM_OPTS=%JVM_OPTS% -Xms96M
set JVM_OPTS=%JVM_OPTS% -Xmx384M
rem spezielle Flags
set JVM_OPTS=%JVM_OPTS% -XX:+HeapDumpOnOutOfMemoryError
set JVM_OPTS=%JVM_OPTS% -XX:PermSize=64M
set JVM_OPTS=%JVM_OPTS% -XX:MaxPermSize=128M
rem GC-Logs aktivieren
set JVM_OPTS=%JVM_OPTS% -Xloggc:gc.log
set JVM_OPTS=%JVM_OPTS% -XX:NumberOfGCLogFiles=5
set JVM_OPTS=%JVM_OPTS% -XX:+UseGCLogFileRotation
set JVM_OPTS=%JVM_OPTS% -XX:GCLogFileSize=2m
set JVM_OPTS=%JVM_OPTS% -XX:+PrintGC
set JVM_OPTS=%JVM_OPTS% -XX:+PrintGCDateStamps
set JVM_OPTS=%JVM_OPTS% -XX:+PrintGCDetails
set JVM_OPTS=%JVM_OPTS% -XX:+PrintHeapAtGC
set JVM_OPTS=%JVM_OPTS% -XX:+PrintGCCause
set JVM_OPTS=%JVM_OPTS% -XX:+PrintTenuringDistribution
set JVM_OPTS=%JVM_OPTS% -XX:+PrintReferenceGC
set JVM_OPTS=%JVM_OPTS% -XX:+PrintAdaptiveSizePolicy

rem set JVM_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=22222 %JVM_OPTS%
rem start "Starte Inka-Client..." jre\bin\javaw %JVM_OPTS% -jar InkaClient.jar

cd %inkaSourcePath%
%javaPath%\jre\bin\javaw %JVM_OPTS% -cp ./target/classes %javaMainClass% 