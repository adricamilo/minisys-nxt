#!/bin/bash

function verify_success {
    if [ $1 != 0 ]; then
	echo "-- Installallation failed for $2 --"
	exit -1;
    else
	echo "-- Successfully installed $2 --"
    fi
}

sudo apt-get update

echo "-- Install JDK --"
sudo apt-get install -y openjdk-11-jdk
verify_success $? java

echo "-- Install Maven --"
sudo apt-get install -y maven
verify_success $? maven

echo "-- Install NodeJS --"
curl -sL https://deb.nodesource.com/setup_16.x | sudo bash - && \
sudo apt-get install -y nodejs
verify_success $? nodejs

echo "-- Done --"
