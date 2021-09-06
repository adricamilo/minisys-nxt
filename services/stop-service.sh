#!/bin/bash

echo "Killing"
if [ "$1" == "all" ]; then
    echo $(jps | grep -e auth.war -e admin.war -e product.war -e order.war -e inventory.war)
    jps | grep -e auth.war -e admin.war -e product.war -e order.war -e inventory.war -e gateway.war | awk '{print $1}' | xargs kill
elif [ "$1" == "admin" ]; then
    echo $(jps | grep -e admin.war)
    jps | grep -e admin.war | awk '{print $1}' | xargs kill
elif [ "$1" == "auth" ]; then
    echo $(jps | grep -e auth.war)
    jps | grep -e auth.war | awk '{print $1}' | xargs kill
elif [ "$1" == "product" ]; then
    echo $(jps | grep -e product.war)
    jps | grep -e product.war | awk '{print $1}' | xargs kill
elif [ "$1" == "order" ]; then
    echo $(jps | grep -e order.war)
    jps | grep -e order.war | awk '{print $1}' | xargs kill
elif [ "$1" == "inventory" ]; then
    echo $(jps | grep -e inventory.war)
    jps | grep -e inventory.war | awk '{print $1}' | xargs kill
elif [ "$1" == "gateway" ]; then
    echo $(jps | grep -e gateway.war)
    jps | grep -e gateway.war | awk '{print $1}' | xargs kill
else
    echo "Incorrect service name"
fi
