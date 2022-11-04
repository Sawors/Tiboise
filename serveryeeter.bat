
set source=%1
rem ends with \IdeaProjects
set root=C:\Users\ytsaw\IdeaProjects

rem usually you shouldn't have to edit these
set target=%source%
set javah=%JAVA_HOME%
set server_executable=paper
set ram=4


cd "%root%\%source%"
rem clean + package the project
call mvn clean
call mvn package
rem move the plugin the the server
del /q "%root%\%source%\target\%source%-*-shaded.jar"
del /q "%root%\_SERVERS\%target%\plugins\%source%-*.jar"
xcopy "%root%\%source%\target\%source%-*.jar" "%root%\_SERVERS\%target%\plugins" /y /s
rem start the server
cd "%root%\_SERVERS\%target%\"
"%javah%\bin\java" -Xmx%ram%G -XX:+UseG1GC -XX:G1HeapRegionSize=4M -XX:+UnlockExperimentalVMOptions -XX:+ParallelRefProcEnabled -XX:+AlwaysPreTouch -jar %server_executable%.jar nogui
