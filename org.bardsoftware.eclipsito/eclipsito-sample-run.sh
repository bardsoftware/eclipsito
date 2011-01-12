#!/bin/sh

DIRNAME=`dirname $0`/.
REGXP_HOME=`realpath $DIRNAME`

if [ -z $JAVA_HOME ]; then
    echo "Please set up JAVA_HOME variable";
    exit 0;
fi;

LOCAL_CLASSPATH=\
$REGXP_HOME/classes/

CONFIGURATION_FILE=$REGXP_HOME/eclipsito-sample-config.xml
BOOT_CLASS=org.bardsoftware.eclipsito.Boot
echo $REGXP_HOME

$JAVA_HOME/bin/java -classpath $CLASSPATH:$LOCAL_CLASSPATH -Dregxp.home=$REGXP_HOME $BOOT_CLASS $CONFIGURATION_FILE