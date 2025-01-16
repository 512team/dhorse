#!/bin/bash

os=`uname`
JAVA_HOME=$JAVA_HOME

APP_NAME1=dhorse
APP_NAME2=DHorse
JAR_NAME=$APP_NAME1-rest-${project.version}.jar
DHORSE_HOME=`dirname "$bin"`
JAR_PATH=$DHORSE_HOME/lib/$JAR_NAME
CONF_PATH=$DHORSE_HOME/conf/$APP_NAME1.yml

#Jvm参数
JAVA_OPTS="-server -Xms256m -Xmx256m -Xmn96m -XX:MetaspaceSize=64m -XX:MaxMetaspaceSize=128m"

dhorse_banner(){
	echo "  _____  _    _                     "
	echo " |  __ \| |  | |                    "
	echo " | |  | | |__| | ___  _ __ ___  ___ "
	echo " | |  | |  __  |/ _ \| '__/ __|/ _ \\"
	echo " | |__| | |  | | (_) | |  \__ \  __/"
	echo " |_____/|_|  |_|\___/|_|  |___/\___|"
	echo "  :: DHorse ::             (v${project.version})"
	echo "                                   "
}

#如果没有指定data目录，则在安装目录下创建
mkdir_data_path(){
	data_path_config=`grep 'data.path' $DHORSE_HOME/conf/dhorse.yml`
	if [[ ! $data_path_config =~ ^# ]]; then
		key_value=(${data_path_config//:/ })
		data_path=`echo "${key_value[1]}"`
	fi
	if test -z "$data_path"; then
		data_path=$DHORSE_HOME/data
	fi
	if [ ! -d "$data_path" ];then
	  mkdir $data_path
	fi
}

is_exist() {
	if [ ! -f "$data_path/pid" ]; then
		return 0
	fi
	pidf=$(cat $data_path/pid)
	if test -z "$pidf"; then
		return 0
	fi
	pid_number=$(ps -ef | grep $JAR_NAME | grep $pidf | grep -v grep | awk '{print $2}')
	if test -z "$pid_number"; then
		return 0
	fi
	return 1
}

#启动方法
start() {
	dhorse_banner
	if [[ -z "$JAVA_HOME" ]]; then
		echo "JAVA_HOME could not be found"
		exit 0
	fi
	
	mkdir_data_path
	is_exist
	if [ $? == "1" ]; then
		echo "The $APP_NAME2 service is already running, pid is $pid_number"
		exit 0
	fi
	
	echo "Starting $APP_NAME2 service, please wait a moment..."
	
	nohup $JAVA_HOME/bin/java $JAVA_OPTS -jar $JAR_PATH --spring.config.location=$CONF_PATH >/dev/null 2>&1 &
	for i in {0..60}; do
		if [[ $os == "Darwin" ]]; then
			process=`lsof -a -p $! | grep $JAR_NAME | grep java`
		else
			process=`netstat -tlpn | grep $!`
		fi
		if test -z "$process"; then
			sleep 1
		else
			echo $! > $data_path/pid
			echo "Start $APP_NAME2 service successfully, pid is $!"
			exit 0
		fi
	done
	
	echo "The $APP_NAME2 service startup failure"
}

#停止方法
stop() {
	dhorse_banner
	mkdir_data_path
	is_exist
	if [ $? == "0" ]; then
		echo "The $APP_NAME2 service is not running"
		return
	fi
	echo "Stoping $APP_NAME2 service, pid is $pid_number"
	kill $pid_number
	rm -rf $data_path/pid
	sleep 2
	is_exist
	if [ $? == "0" ]; then
		echo "Stop $APP_NAME2 service successfully"
		return
	fi
	
	kill -9 $pid_number
	sleep 2
	echo "Stop $APP_NAME2 service successfully"
}

#重启
restart(){
  stop
  start
}

#输出运行状态
status(){
  is_exist
  if [ $? -eq "1" ]; then
    echo "The $APP_NAME2 service is running, pid is $pid_number"
  else
    echo "The $APP_NAME2 service is not running"
  fi
}

#根据输入参数，选择执行对应方法，不输入则执行使用说明
case "$1" in
"start")
	start
	;;
"stop")
	stop
	;;
"status")
	status
	;;
"restart")
	restart
	;;
*)
	echo "usage:  [start | stop]"
	;;
esac
exit 0
