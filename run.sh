#!/bin/bash

sudo ifconfig lo0 alias 127.0.0.2 up
sudo ifconfig lo0 alias 127.0.0.3 up
sudo ifconfig lo0 alias 127.0.0.4 up

sbt -Dconfig.resource=local-1.conf -J-Dnode=NODE_1 'runMain com.rmpader.gitprojects.Main' >> logs/log.txt &
NODE_1=$!
sbt -Dconfig.resource=local-2.conf -J-Dnode=NODE_2 'runMain com.rmpader.gitprojects.Main' >> logs/log.txt &
NODE_2=$!
sbt -Dconfig.resource=local-3.conf -J-Dnode=NODE_3 'runMain com.rmpader.gitprojects.Main' >> logs/log.txt &
NODE_3=$!
sleep 5
read -n1 -rsp "See logs in 'logs/log.txt'.  Press any key to terminate..." key
kill -9 $NODE_1
kill -9 $NODE_2
kill -9 $NODE_3
