@echo off

rem Expected Arguments Are:
rem %1 ... webname (folder name to save)

setlocal EnableDelayedExpansion

pushd %~dp0..
set _PROJECT_DIR=%CD%
set DOWNLOAD_FOLDER=%USERPROFILE%\Downloads
set CAPTURE_FOLDER=%_PROJECT_DIR%\results\capture\%1

cd %DOWNLOAD_FOLDER%

for %%A in (*.png) do (
	set STR=%%A
	set STR2=!STR:~26,30!
	ren "%%A" "!STR2!.png"
)

move %DOWNLOAD_FOLDER%\*.png %CAPTURE_FOLDER%
