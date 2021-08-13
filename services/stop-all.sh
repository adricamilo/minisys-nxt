#!/bin/bash

echo Killing pids $(cat pid.txt | xargs) 
cat pid.txt | xargs kill -9
rm -f pid.txt
