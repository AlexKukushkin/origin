:: install.bat
:: perform installation process for jar being extracted
::
@echo off

:: setup properties
set LOG_FILE=C:\EdifecsSetups\tmp\log.txt

echo uninstall was started at:  >> %LOG_FILE%
time /t >> %LOG_FILE%
rem @echo on

echo this is the uninstall program
IF ERRORLEVEL 1 (
    echo Exiting due to Error!
    GOTO ErrorExit
)

rem @echo off
echo Uninstall finished: >> %LOG_FILE%
time /t >> %LOG_FILE%
echo =========-=========-=========-=========-=========- >> %LOG_FILE%
exit 0

:ErrorExit
echo Install encountered errors: >> %LOG_FILE%
time /t >> %LOG_FILE%
echo =========!=========!=========!=========!=========! >> %LOG_FILE%
exit 1
