#!/bin/bash
#Commands to run after after installation
echo "Entered after install hook"
cd /home/ubuntu/webapp
sudo chown -R ubuntu:ubuntu /home/ubuntu/*
sudo chmod +x cloudwebapp-0.0.1-SNAPSHOT.jar

source /etc/profile.d/envvariable.sh
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/home/ubuntu/webapp/amazon-cloudwatch-agent-schema.json -s
#Killing the application
kill -9 $(ps -ef|grep cloudwebapp-0.0.1 | grep -v grep | awk '{print $2}')
#Removing log files
sudo rm -rf logs/*.log
#Running cloudwebapp jar
nohup java -jar cloudwebapp-0.0.1-SNAPSHOT.jar > /home/ubuntu/log.txt 2> /home/ubuntu/log.txt < /home/ubuntu/log.txt &