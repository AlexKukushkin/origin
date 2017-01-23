:: install.bat
:: perform installation process for jar being extracted
::
@echo off

:: setup properties
set START_DIR=%CD%
set TMP_DIR=C:\EdifecsSetups\tmp
set LOG_FILE=%TMP_DIR%\log.txt
set TM_DIR=%ECRootPath%\TM\ServiceManager
set TM_TOOLS_DIR=%TM_DIR%\tools
set CLM_DIR=%ECRootPath%\EUO\CLM
set OPS_REPOSITORY_INSTALL=%ECRootPath%\EUO\Common\components\ops-repository\install
set OPS_REPOSITORY_CONFIG=%ECRootPath%\EUO\Common\components\ops-repository\config
set EIPSERVICES_CONF=%ECRootPath%\TM\TrackingInfoProcessor\EIPServices\conf
set UNPACK_DIR=C:\EdifecsSetups\CLM.artifacts
set ZIP_FILE=%UNPACK_DIR%\clm-artifacts.zip

set TIME_LOG=time /t
set SLEEP_10=PING -n 11 127.0.0.1
set SLEEP_60=PING -n 61 127.0.0.1
set SLEEP_120=PING -n 121 127.0.0.1
set SLEEP_180=PING -n 181 127.0.0.1
set JAVA_EXE="%JAVA_HOME%\bin\java.exe"
set COPY_EXE=COPY /Y
set CMD_EXE=CMD /C
set SERV_STOP=NET STOP
set SERV_START=NET START
set XESERVER_STOP=%CMD_EXE% %XESRoot%\bin\XEServer_stop.bat
set XESERVER_START=%CMD_EXE% %XESRoot%\bin\XEServer.bat
set OPS_OPTIONS=-i

IF NOT EXIST %TMP_DIR% (
    mkdir %TMP_DIR%
)

echo Install was started at:  >> %LOG_FILE%
%TIME_LOG% >> %LOG_FILE%
rem @echo on

:: start
echo Starting CLM Auto-Install Process

:: copy resources to tmp location
IF EXIST ..\resources (
    %COPY_EXE% ..\resources\E-Mail.properties %TMP_DIR%\E-Mail.properties
    %COPY_EXE% ..\resources\config.properties %TMP_DIR%\config.properties
    rem %COPY_EXE% ..\resources\ucf-datastorage-config.properties %TMP_DIR%\ucf-datastorage-config.properties
	%COPY_EXE% ..\resources\TMBatch.properties %TMP_DIR%\TMBatch.properties
	 %COPY_EXE% ..\resources\TM.JMS.BATCH.JNDI.properties.tm %TMP_DIR%\TM.JMS.BATCH.JNDI.properties.tm
	 %COPY_EXE% ..\resources\eip-camel-context.xml %TMP_DIR%\eip-camel-context.xml
	 %COPY_EXE% ..\resources\edifecs-registry.xml %TMP_DIR%\edifecs-registry.xml
)

echo Install backing up files... >> %LOG_FILE%
echo Backing Up
:: backup eip-camel-context.xml file
%COPY_EXE% %EIPSERVICES_CONF%\eip-camel-context.xml %TMP_DIR%\eip-camel-context_xml.old
%COPY_EXE% %EIPSERVICES_CONF%\eip-camel-context.xml %TMP_DIR%\eip-camel-context_xml.bak
IF EXIST ..\resources\eip-camel-context.xml (
    %COPY_EXE% ..\resources\eip-camel-context.xml %TMP_DIR%\eip-camel-context_xml.bak
)

:: backup ops-repository config file
IF EXIST %OPS_REPOSITORY_INSTALL%\config.xml (
    set OPS_OPTIONS=-u
    echo     Found previous Ops-Repository install
    %COPY_EXE% %OPS_REPOSITORY_INSTALL%\config.xml %TMP_DIR%\ops_repository_config_xml.old
    %COPY_EXE% %OPS_REPOSITORY_INSTALL%\config.xml %TMP_DIR%\ops_repository_config_xml.bak
)
IF EXIST ..\resources\ops_repository_config.xml (
    %COPY_EXE% ..\resources\ops_repository_config.xml %TMP_DIR%\ops_repository_config_xml.bak
)
REM IF EXIST %OPS_REPOSITORY_CONFIG%\com.edifecs.opsrepository.xml (
REM     %COPY_EXE% %OPS_REPOSITORY_CONFIG%\com.edifecs.opsrepository.xml %TMP_DIR%\com.edifecs.opsrepository.xml
REM     set OPS_OPTIONS=-u
REM )

echo Install stopping services at: >> %LOG_FILE%
%TIME_LOG% >> %LOG_FILE%
%CMD_EXE% %START_DIR%\stop_services.bat

:InstallJar
echo Install running clm installer at: >> %LOG_FILE%
%TIME_LOG% >> %LOG_FILE%
:: go to installer directory

cd /D %UNPACK_DIR%
cd clm-7.0.11*

:: override config.properties file (to enable tomcat install)
IF EXIST %TMP_DIR%\config.properties (
    echo Setting up installer config...
    %COPY_EXE% %TMP_DIR%\config.properties config.properties
)
:: run installer jar
echo Running CLM Install Jar
IF EXIST clm-7.0.11*.jar (
    %JAVA_EXE% -jar clm-7.0.11*.jar
    IF ERRORLEVEL 1 (
        echo UNABLE to Run CLM Install Jar! Exiting...
        GOTO ErrorExit
    )
) ELSE (
    echo Install Jar not found!
	dir
    GOTO ErrorExit
)


echo Install running ops-repository installer at: >> %LOG_FILE%
%TIME_LOG% >> %LOG_FILE%
:: configure ops-repository install
IF EXIST %OPS_REPOSITORY_INSTALL%\config.xml (
    cd %OPS_REPOSITORY_INSTALL%
    IF EXIST %OPS_REPOSITORY_INSTALL%\samples\mssql\config.xml (
        echo Copying ops-repository mssql sample config
        %COPY_EXE% %OPS_REPOSITORY_INSTALL%\samples\mssql\config.xml %OPS_REPOSITORY_INSTALL%\config.xml
    )
    IF EXIST %TMP_DIR%\ops_repository_config_xml.bak (
        echo Restoring backed up ops-repository config.
        %COPY_EXE% %TMP_DIR%\ops_repository_config_xml.bak %OPS_REPOSITORY_INSTALL%\config.xml
    )
) ELSE (
    echo ops_repository installer not found!
    GOTO ErrorExit
)
REM IF EXIST %TMP_DIR%\com.edifecs.opsrepository.xml (
REM     %COPY_EXE% %TMP_DIR%\com.edifecs.opsrepository.xml %OPS_REPOSITORY_CONFIG%\com.edifecs.opsrepository.xml
REM     DEL %TMP_DIR%\com.edifecs.opsrepository.xml>nul
REM )

:: for now skip updates on the ops-repository.  it seems to be hanging for some reason
IF "%OPS_OPTIONS%" == "-u" (
    ECHO Skipping Ops-Repository Update
    GOTO CopyOpsConfig
)

:: run ops-repository install
:InstallOps
echo Running Ops-Repository Install
.\install.bat %OPS_OPTIONS%
IF ERRORLEVEL 1 (
    echo Ops-Repository Install exited with errors
    GOTO ErrorExit
)

:CopyOpsConfig
IF EXIST %TOMCAT_HOME%\webapps\Correction\WEB-INF\classes (
    echo Copying opsrepository config file...
    %COPY_EXE% %OPS_REPOSITORY_CONFIG%\com.edifecs.opsrepository.xml %TOMCAT_HOME%\webapps\Correction\WEB-INF\classes\com.edifecs.opsrepository.xml
)

:CopyDatastorageConfig
rem IF EXIST %TOMCAT_HOME%\webapps\tm\WEB-INF\classes (
rem     echo Copying ucf-datastorage-config.properties to TM webapp...
rem     %COPY_EXE% %TMP_DIR%\ucf-datastorage-config.properties %TOMCAT_HOME%\webapps\tm\WEB-INF\classes\ucf-datastorage-config.properties
rem )
rem IF EXIST %TOMCAT_HOME%\webapps\Correction\WEB-INF\classes (
rem     echo Copying ucf-datastorage-config.properties to Correction webapp...
rem     %COPY_EXE% %TMP_DIR%\ucf-datastorage-config.properties %TOMCAT_HOME%\webapps\Correction\WEB-INF\classes\ucf-datastorage-config.properties
rem )

:LoadPartner
echo Install loading sample partner file at: >> %LOG_FILE%
%TIME_LOG% >> %LOG_FILE%
echo Importing Sample Partner File
:: (optional) import sample partner file
IF EXIST %CLM_DIR%\RIM\community\EUO_partners.xml (
    cd %TM_TOOLS_DIR%\partners-import-export
    %CMD_EXE% import_partners.bat %CLM_DIR%\RIM\community\EUO_partners.xml -mergeAddPartners -debug
    IF ERRORLEVEL 1 echo Unable to import sample partners
)

:: Restore backed up files
%COPY_EXE% %TMP_DIR%\E-Mail.properties %XESRoot%\platform\environment\"CLM E-Mail.properties"
:: restore eip-camel-context.xml file??
%COPY_EXE% %TMP_DIR%\eip-camel-context.xml %EIPSERVICES_CONF%\eip-camel-context.xml
::restore TMBatch.properties
%COPY_EXE% %TMP_DIR%\TMBatch.properties %XESRoot%\platform\environment\TMBatch.properties
::restore TM.JMS.BATCH.JNDI.properties.tm
%COPY_EXE% %TMP_DIR%\TM.JMS.BATCH.JNDI.properties.tm %XESRoot%\platform\environment\TM.JMS.BATCH.JNDI.properties.tm
::restore edifecs-registry.xml
%COPY_EXE% %TMP_DIR%\edifecs-registry.xml %TM_DIR%\conf\edifecs-registry.xml


echo Install restarting services at: >> %LOG_FILE%
%TIME_LOG% >> %LOG_FILE%
%CMD_EXE% %START_DIR%\start_services.bat

:: cleanup some things
DEL %TMP_DIR%\E-Mail.properties
DEL %TMP_DIR%\config.properties
DEL %TMP_DIR%\TMBatch.properties
DEL %TMP_DIR%\TM.JMS.BATCH.JNDI.properties.tm

:: finish
echo Finished CLM Auto-Install Process

rem @echo off
:NormalExit
echo Install finished: >> %LOG_FILE%
%TIME_LOG% >> %LOG_FILE%
echo =========+=========+=========+=========+=========+ >> %LOG_FILE%
exit 0

:ErrorExit
echo Install encountered errors: >> %LOG_FILE%
%TIME_LOG% >> %LOG_FILE%
echo =========!=========!=========!=========!=========! >> %LOG_FILE%
exit 1
