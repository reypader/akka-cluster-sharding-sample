#!/bin/bash

sbt '; set javaOptions += "-Dnode=NODE_1" ;runMain com.rmpader.gitprojects.MainApp1' >> logs/log.txt &
NODE_1=$!
sbt '; set javaOptions += "-Dnode=NODE_2" ;runMain com.rmpader.gitprojects.MainApp2' >> logs/log.txt &
NODE_2=$!
sbt '; set javaOptions += "-Dnode=NODE_3" ;runMain com.rmpader.gitprojects.MainApp3' >> logs/log.txt &
NODE_3=$!
sleep 5
read -n1 -rsp "See logs in 'logs/log.txt'.  Press any key to terminate..." key
kill -9 $NODE_1
kill -9 $NODE_2
kill -9 $NODE_3
