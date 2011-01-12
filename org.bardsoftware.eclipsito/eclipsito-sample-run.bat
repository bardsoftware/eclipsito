@echo off

SET REGXP_HOME=.

IF a%JAVA_HOME%a==aa GOTO ERROR_NO_JAVA

SET LOCAL_CLASSPATH=%REGXP_HOME%\classes\

SET CONFIGURATION_FILE=%REGXP_HOME%\\eclipsito-sample-config.xml
SET BOOT_CLASS=org.bardsoftware.eclipsito.Boot

%JAVA_HOME%\bin\java.exe -classpath %CLASSPATH%;%LOCAL_CLASSPATH% -Dregxp.home=%REGXP_HOME% %BOOT_CLASS% %CONFIGURATION_FILE%
exit

:ERROR_NO_JAVA
echo "Please set up JAVA_HOME variable"
exit

