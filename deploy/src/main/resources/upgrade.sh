#!/bin/bash
set -e
echo "Stopping service..."
sudo service datamaker stop
echo "Upgrading..."
unzip ~/datamaker-*.zip
mv ~/datamaker/service.jar /opt/datamaker/
echo "Cleaning resources..."
mv ~/datamaker-*.zip ~/downloads/demo/
rm -rf ~/datamaker
rm -rf /tmp/datamaker
echo "Starting service..."
sudo service datamaker start
echo "Upgrade completed"
