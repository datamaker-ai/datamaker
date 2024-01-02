#!/bin/bash

service datamaker stop

TOKEN=`curl -X PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 21600"`

INSTANCE_ID=`curl -H "X-aws-ec2-metadata-token: ${TOKEN}" http://169.254.169.254/latest/meta-data/instance-id`

echo ${INSTANCE_ID}

PASSWORD=`/usr/bin/htpasswd -bnBC 10 "" ${INSTANCE_ID} | tr -d ':\n' | sed 's/$2y/$2a/'`

echo ${PASSWORD}

java -cp h2-1.4.200.jar org.h2.tools.Shell -url jdbc:h2:file:/home/datamaker/testdb -user sa -password '' -driver org.h2.Driver -sql "update user set password='${PASSWORD}' where id=1"

service datamaker start