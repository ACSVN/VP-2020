@echo off

rem Expected Parameters Are:
rem %1 ... webname (folder name to save)

pushd %~dp0..
set _PROJECT_DIR=%CD%

md %_PROJECT_DIR%\results\HTTrack\%1
xcopy /E C:\HTTrackLog %_PROJECT_DIR%\results\HTTrack\%1
rd /S /Q C:\HTTrackLog