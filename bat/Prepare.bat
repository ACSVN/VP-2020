@echo off

pushd %~dp0..
set _PROJECT_DIR=%CD%

del %_PROJECT_DIR%\results\*.txt
del %_PROJECT_DIR%\results\*.csv
rd /S /Q %_PROJECT_DIR%\results\capture
rd /S /Q %_PROJECT_DIR%\results\HTTrack