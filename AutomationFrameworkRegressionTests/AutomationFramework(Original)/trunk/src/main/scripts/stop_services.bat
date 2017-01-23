:: stop_services.bat
:: stop the services associated with the clm install
::
@echo off

:: setup properties
set START_DIR=%CD%
set TMP_DIR=C:\EdifecsSetups\tmp
set LOG_FILE=%TMP_DIR%\log.txt
set TM_DIR=%ECRootPath%\TM\ServiceManager

set TIME_LOG=time /t
set SLEEP_10=PING -n 11 127.0.0.1
set SLEEP_30=PING -n 31 127.0.0.1
set SLEEP_60=PING -n 61 127.0.0.1
set CMD_EXE=CMD /C
set SERV_STOP=NET STOP
set SERV_START=NET START
set XESERVER_STOP=%CMD_EXE% %XESRoot%\bin\XEServer_stop.bat
set XESERVER_START=%CMD_EXE% %XESRoot%\bin\XEServer.bat

echo Stopping Services at: >> %LOG_FILE%
%TIME_LOG% >> %LOG_FILE%
echo Stopping Services
:: stop services
::  - stop app server
REM echo Attempt to stop tomcat
REM %SERV_STOP% Tomcat6
REM IF ERRORLEVEL 1 (
REM     ECHO Unable to stop Tomcat6 Service.
REM )
%SLEEP_10%>nul

::  - stop XES Profiles Outbound, RIM, InterNal, Inbound, common
IF EXIST %XESRoot%\profiles\Outbound (
    %SERV_STOP% Edifecs_XEServer_Outbound
    IF ERRORLEVEL 1 (
        ECHO Unable to stop Edifecs_XEServer_Outbound Service. Trying through bat file.
        %XESERVER_STOP% Outbound
        IF ERRORLEVEL 1 echo Outbound XES Profile could not be stopped
    ) ELSE (
        %SLEEP_10%>nul
        %XESERVER_STOP% Outbound>nul
    )
)
IF EXIST %XESRoot%\profiles\RIM (
    %SERV_STOP% Edifecs_XEServer_RIM
    IF ERRORLEVEL 1 (
        ECHO Unable to stop Edifecs_XEServer_RIM Service. Trying through bat file.
        %XESERVER_STOP% RIM
        IF ERRORLEVEL 1 echo RIM XES Profile could not be stopped
    ) ELSE (
        %SLEEP_10%>nul
        %XESERVER_STOP% RIM>nul
    )
)
IF EXIST %XESRoot%\profiles\InterNal (
    %SERV_STOP% Edifecs_XEServer_InterNal
    IF ERRORLEVEL 1 (
        ECHO Unable to stop Edifecs_XEServer_InterNal Service. Trying through bat file.
        %XESERVER_STOP% InterNal
        IF ERRORLEVEL 1 echo InterNal XES Profile could not be stopped
    ) ELSE (
        %SLEEP_10%>nul
        %XESERVER_STOP% InterNal>nul
    )
)
IF EXIST %XESRoot%\profiles\Inbound (
    %SERV_STOP% Edifecs_XEServer_Inbound
    IF ERRORLEVEL 1 (
        ECHO Unable to stop Edifecs_XEServer_Inbound Service. Trying through bat file.
        %XESERVER_STOP% Inbound
        IF ERRORLEVEL 1 echo Inbound XES Profile could not be stopped
    ) ELSE (
        %SLEEP_10%>nul
        %XESERVER_STOP% Inbound>nul
    )
)
IF EXIST %XESRoot%\profiles\Common (
    %SERV_STOP% Edifecs_XEServer_Common
    IF ERRORLEVEL 1 (
        ECHO Unable to stop Edifecs_XEServer_Common Service. Trying through bat file.
        %XESERVER_STOP% Common
        IF ERRORLEVEL 1 echo Common XES Profile could not be stopped
    ) ELSE (
        %SLEEP_10%>nul
        %XESERVER_STOP% Common>nul
    )
)

%SLEEP_30%>nul

::  - stop TMETLResubmission service
IF EXIST %ECRootPath%\TM\ETLResubmission\classes\stop_task_manager.bat (
    %SERV_STOP% EdifecsTMETLResubmission
    IF ERRORLEVEL 1 (
        ECHO Unable to stop EdifecsTMETLResubmission Service. Trying bat file.
        pushd %ECRootPath%\TM\ETLResubmission\classes
        %CMD_EXE% %ECRootPath%\TM\ETLResubmission\classes\stop_task_manager.bat
        IF ERRORLEVEL 1 echo EdifecsTMETLResubmission could not be stopped
        popd
    )
    %SLEEP_10%>nul
)

::  - stop EIPServices
IF EXIST %TM_DIR%\EIPServices\exec\stop_windows_service.bat (
    %SERV_STOP% EdifecsEIPService
    IF ERRORLEVEL 1 (
        ECHO Unable to stop EdifecsEIPService Service. Trying bat file.
        pushd %TM_DIR%\EIPServices\exec
        %CMD_EXE% %TM_DIR%\EIPServices\exec\stop_windows_service.bat
        IF ERRORLEVEL 1 echo EIPServices could not be stopped
        popd
    )
    %SLEEP_10%>nul
)

::  - stop TM
IF EXIST %TM_DIR%\classes\stop_task_manager.bat (
    %SERV_STOP% EdifecsTMServiceManager
    IF ERRORLEVEL 1 (
        ECHO Unable to stop EdifecsTMServiceManager Service. Trying bat file.
        pushd %TM_DIR%\classes
        %CMD_EXE% %TM_DIR%\classes\stop_task_manager.bat
        IF ERRORLEVEL 1 echo TM Task Manager could not be stopped
        popd
    )
    %SLEEP_60%>nul
)

echo Finished Stop Services Process
exit 0
