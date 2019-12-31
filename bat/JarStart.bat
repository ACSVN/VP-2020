@echo off

echo:
echo ********** Please Input Information To Execute Web-Crawling App **********
echo:

pushd %~dp0..
set _PROJECT_DIR=%CD%

java -jar %_PROJECT_DIR%\jar\vp-2.1.0.jar