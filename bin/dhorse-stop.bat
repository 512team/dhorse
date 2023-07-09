@echo off

rem  This software complies with Apache License 2.0,
rem  detail: http://www.apache.org/licenses/LICENSE-2.0
rem
rem ---------------------------------------------------------------------------
rem Stop script for the DHorse Server
rem ---------------------------------------------------------------------------


setlocal

for /f "tokens=1" %%a in ('jps ^| findstr dhorse') do (
	taskkill /f /pid %%a
)

pause