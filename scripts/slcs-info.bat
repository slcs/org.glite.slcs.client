@echo off
rem ---------------------------------------------------------------------------
rem Execute the SLCS info
rem
rem Copyright (c) 2007. Members of the EGEE Collaboration. 
rem http:\\www.eu-egee.org
rem
rem $Id: slcs-info.bat,v 1.1 2007/11/04 16:30:20 vtschopp Exp $
rem ---------------------------------------------------------------------------
if "%OS%" == "Windows_NT" setlocal
if "%OS%" == "WINNT" setlocal

rem set SLCS_HOME directory
set CURRENT_DIR=%~dp0
if not "%SLCS_HOME%" == "" goto gotHome
set SLCS_HOME=%CURRENT_DIR%
if exist "%SLCS_HOME%\bin\slcs-info.bat" goto okHome
cd /d %CURRENT_DIR%\..
set SLCS_HOME=%cd%
cd %CURRENT_DIR%
:gotHome
if exist "%SLCS_HOME%\bin\slcs-info.bat" goto okHome
echo The SLCS_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:okHome
rem XXX: echo SLCS_HOME=%SLCS_HOME%

rem set the Java classpath: glite-slcs-* jars
set SLCS_CP=%SLCS_HOME%\etc\glite-slcs-ui
set SLCS_CP=%SLCS_CP%;%SLCS_HOME%\share\java\glite-slcs-ui.jar;%SLCS_HOME%\share\java\glite-slcs-common.jar;

rem set the Java classpath: external jars
set SLCS_CP=%SLCS_CP%;%SLCS_HOME%\externals\share\java\bcprov-jdk15-134.jar
set SLCS_CP=%SLCS_CP%;%SLCS_HOME%\externals\share\java\commons-cli-1.0.jar
set SLCS_CP=%SLCS_CP%;%SLCS_HOME%\externals\share\java\commons-codec-1.3.jar
set SLCS_CP=%SLCS_CP%;%SLCS_HOME%\externals\share\java\commons-collections-3.2.jar
set SLCS_CP=%SLCS_CP%;%SLCS_HOME%\externals\share\java\commons-configuration-1.2.jar
set SLCS_CP=%SLCS_CP%;%SLCS_HOME%\externals\share\java\commons-httpclient-3.0.1.jar
set SLCS_CP=%SLCS_CP%;%SLCS_HOME%\externals\share\java\commons-lang-2.1.jar
set SLCS_CP=%SLCS_CP%;%SLCS_HOME%\externals\share\java\commons-logging.jar
set SLCS_CP=%SLCS_CP%;%SLCS_HOME%\externals\share\java\jericho-html-2.3.jar
set SLCS_CP=%SLCS_CP%;%SLCS_HOME%\externals\share\java\log4j-1.2.8.jar
rem XXX: echo SLCS_CP=%SLCS_CP%

rem get command line arguments
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

rem set Java command
if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVA_CMD%" == "" set _JAVA_CMD=%JAVA_HOME%\bin\java.exe
goto exec
:noJavaHome
if "%_JAVA_CMD%" == "" set _JAVA_CMD=java.exe

rem execute command
:exec
set SLCS_MAIN=org.glite.slcs.SLCSInfo
"%_JAVA_CMD%" -classpath "%SLCS_CP%" %SLCS_MAIN% %CMD_LINE_ARGS%
rem if not errorlevel 1 goto end
rem pause

:end
if "%OS%" == "Windows_NT" endlocal
if "%OS%" == "WINNT" endlocal

pause