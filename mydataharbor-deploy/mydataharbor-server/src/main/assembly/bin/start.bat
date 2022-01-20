echo off

set APP_NAME=${project.build.finalName}.jar
set LOG_IMPL_FILE=logback.xml
set LOGGING_CONFIG=
if exist ../config/%LOG_IMPL_FILE% (
    set LOGGING_CONFIG=-Dlogging.config=../config/%LOG_IMPL_FILE%
)
set CONFIG= -Dfile.encoding=utf-8 -Dlogging.path=../logs %LOGGING_CONFIG% -Dspring.config.location=../config/

set DEBUG_OPTS=
if ""%1"" == ""debug"" (
   set DEBUG_OPTS= -Xloggc:../logs/gc.log -verbose:gc -XX:+PrintGCDetails -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=../logs
   goto debug
)

set JMX_OPTS=
if ""%1"" == ""jmx"" (
   set JMX_OPTS= -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9888 -Dcom.sun.management.jmxremote.ssl=FALSE -Dcom.sun.management.jmxremote.authenticate=FALSE
   goto jmx
)

echo "Starting the %APP_NAME%"
echo "java -server %DEBUG_OPTS% %JMX_OPTS% %CONFIG% -jar ../%APP_NAME%"
java -server %DEBUG_OPTS% %JMX_OPTS% %CONFIG% -jar ../%APP_NAME%
goto end

:debug
echo "debug"
java  -server %DEBUG_OPTS% %CONFIG% -jar ../%APP_NAME%
goto end

:jmx
java -server %JMX_OPTS% %CONFIG% -jar ../%APP_NAME%
goto end

:end
pause