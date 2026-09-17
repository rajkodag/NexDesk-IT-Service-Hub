@echo off
setlocal
if not exist "lib\ojdbc11.jar" (echo ERROR: Put ojdbc11.jar in backend\lib & pause & exit /b 1)
if not exist out mkdir out
for /R src %%F in (*.java) do echo %%F>>sources.txt
javac -cp "lib\ojdbc11.jar" -d out @sources.txt
if errorlevel 1 (del sources.txt & echo COMPILATION FAILED & pause & exit /b 1)
del sources.txt
java -cp "out;lib\ojdbc11.jar" com.nexdesk.NexDeskApplication
