@echo off

rem  This software complies with Apache License 2.0,
rem  detail: http://www.apache.org/licenses/LICENSE-2.0
rem
rem
rem  JAVA_HOME       Must point at your Java Development Kit installation.
rem                  Required to run the with the "debug" argument.
rem
rem  JAVA_OPTS       (Optional) Java runtime options used when any command
rem                  is executed.


setlocal

set "CURRENT_DIR=%cd%"
cd ..
set "DHORSE_HOME=%cd%"
cd "%CURRENT_DIR%"

if "%JAVA_HOME%" == "" (
    echo JAVA_HOME could not be found
	pause
	goto end
)

set JAVA_HOME=%JAVA_HOME%

set JAVA_CMD=%JAVA_HOME%\bin\java

set JAVA_OPTS=-server -Xms256m -Xmx256m -Xmn96m -XX:MetaspaceSize=64m -XX:MaxMetaspaceSize=128m

set JAR_PATH=%DHORSE_HOME%/lib/dhorse-rest-${project.version}.jar

set CONFIG_LOCATION=%DHORSE_HOME%/conf/dhorse.yml

%JAVA_CMD% %JAVA_OPTS% -Dfile.encoding=utf-8 -jar %JAR_PATH% --spring.config.location=%CONFIG_LOCATION% &

pause

:end