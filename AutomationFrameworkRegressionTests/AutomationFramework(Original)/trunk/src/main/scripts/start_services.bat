:: start_services.bat
:: start the services associated with CLM artifacts
::
@echo off

:: setup properties
set START_DIR=%CD%
set TMP_DIR=C:\EdifecsSetups\tmp
set LOG_FILE=%TMP_DIR%\log.txt
set TM_DIR=%ECRootPath%\TM\ServiceManager

set TIME_LOG=time /t
set SLEEP_10=PING -n 11 127.0.0.1
set SLEEP_60=PING -n 61 127.0.0.1
set SLEEP_120=PING -n 121 127.0.0.1
set SLEEP_180=PING -n 181 127.0.0.1
set CMD_EXE=CMD /C
set SERV_STOP=NET STOP
set SERV_START=NET START
set XESERVER_STOP=%CMD_EXE% %XESRoot%\bin\XEServer_stop.bat
set XESERVER_START=%CMD_EXE% %XESRoot%\bin\XEServer.bat

echo Starting Services at: >> %LOG_FILE%
%TIME_LOG% >> %LOG_FILE%
:: Start services
echo Starting Services
%SLEEP_10%>nul
::  - start XES Profiles Common, Inbound, InterNal, RIM, Outbound
IF EXIST %XESRoot%\profiles\Common (
    %SERV_START% Edifecs_XEServer_Common
    IF ERRORLEVEL 1 (
        ECHO Unable to start Edifecs_XEServer_Common Service!
        REM ECHO Unable to start Edifecs_XEServer_Common Service! Trying bat file
        REM %XESERVER_START% Common
        REM IF ERRORLEVEL 1 echo Common XES Profile could not be started
    ) ELSE (
        %SLEEP_180%>nul
    )
)
::  - start TM
IF EXIST %TM_DIR%\classes\run_task_manager.bat (
    %SERV_START% EdifecsTMServiceManager
    IF ERRORLEVEL 1 (
        echo Unable to start EdifecsTMServiceManager Service!
    ) ELSE (
        %SLEEP_120%>nul
    )
    :: DO NOT uncomment below currently will not run in background
    REM IF ERRORLEVEL 1 echo Unable to start EdifecsTMServiceManager Service! Trying bat file
    REM pushd %TM_DIR%\classes
    REM %CMD_EXE% %TM_DIR%\classes\run_task_manager.bat start
    REM IF ERRORLEVEL 1 echo Unable to start TM Task Manager!
    REM popd
)
IF EXIST %XESRoot%\profiles\Inbound (
    %SERV_START% Edifecs_XEServer_Inbound
    IF ERRORLEVEL 1 (
        ECHO Unable to start Edifecs_XEServer_Inbound Service!
        REM ECHO Unable to start Edifecs_XEServer_Inbound Service! Trying bat file
        REM %XESERVER_START% Inbound
        REM IF ERRORLEVEL 1 echo Inbound XES Profile could not be started
    ) ELSE (
        %SLEEP_120%>nul
    )
)
IF EXIST %XESRoot%\profiles\InterNal (
    %SERV_START% Edifecs_XEServer_InterNal
    IF ERRORLEVEL 1 (
        ECHO Unable to start Edifecs_XEServer_InterNal Service!
        REM ECHO Unable to start Edifecs_XEServer_InterNal Service! Trying bat file
        REM %XESERVER_START% InterNal
        REM IF ERRORLEVEL 1 echo InterNal XES Profile could not be started
    ) ELSE (
        %SLEEP_120%>nul
    )
)
IF EXIST %XESRoot%\profiles\RIM (
    %SERV_START% Edifecs_XEServer_RIM
    IF ERRORLEVEL 1 (
        ECHO Unable to start Edifecs_XEServer_RIMl Service!
        REM ECHO Unable to start Edifecs_XEServer_RIM Service! Trying bat file
        REM %XESERVER_START% RIM
        REM IF ERRORLEVEL 1 echo RIM XES Profile could not be started
    ) ELSE (
        %SLEEP_120%>nul
    )
)
IF EXIST %XESRoot%\profiles\Outbound (
    %SERV_START% Edifecs_XEServer_Outbound
    IF ERRORLEVEL 1 (
        ECHO Unable to start Edifecs_XEServer_Outbound Service!
        REM ECHO Unable to start Edifecs_XEServer_Outbound Service! Trying bat file
        REM %XESERVER_START% Outbound
        REM IF ERRORLEVEL 1 echo Outbound XES Profile could not be started
    ) ELSE (
        %SLEEP_120%>nul
    )
)
::  - start TMETLResubmission service
IF EXIST %ECRootPath%\TM\ETLResubmission\classes\run_task_manager.bat (
    %SERV_START% EdifecsTMETLResubmission
    IF ERRORLEVEL 1 (
        ECHO Unable to start EdifecsTMETLResubmission Service!
    ) ELSE (
        %SLEEP_120%>nul
    )
    :: DO NOT uncomment below currently will not run in background
    REM IF ERRORLEVEL 1 echo Unable to start EdifecsTMETLResubmission Service! Trying bat file
    REM pushd %ECRootPath%\TM\ETLResubmission\classes
    REM %CMD_EXE% %ECRootPath%\TM\ETLResubmission\classes\run_task_manager.bat start
    REM IF ERRORLEVEL 1 echo Unable to start TM ETLResubmission Task Manager!
    REM popd
)
::  - start EIPServices
IF EXIST %TM_DIR%\EIPServices\exec\run_services.bat (
    %SERV_START% EdifecsEIPService
    IF ERRORLEVEL 1 (
        echo Unable to start EdifecsEIPService Service!
    ) ELSE (
        %SLEEP_60%>nul
    )
    :: DO NOT uncomment below currently will not run in background
    REM IF ERRORLEVEL 1 echo Unable to start EdifecsTMServiceManager Service! Trying bat file
    REM pushd %TM_DIR%\classes
    REM %CMD_EXE% %TM_DIR%\classes\run_task_manager.bat start
    REM IF ERRORLEVEL 1 echo Unable to start TM Task Manager!
    REM popd
)

::  - start app server
echo Starting Tomcat
REM %SERV_STOP% Tomcat6
REM IF ERRORLEVEL 1 (
REM     ECHO Unable to stop Tomcat6 Service.
REM )
REM %SLEEP_10%>nul
%SERV_START% Tomcat6
IF ERRORLEVEL 1 (
    ECHO Unable to start Tomcat6 Service.
) ELSE (
    %SLEEP_60%>nul
)

:: finish
echo Finished Start Services Process

rem @echo off
exit 0
