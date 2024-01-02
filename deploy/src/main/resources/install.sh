!#/bin/bash

#mkdir /opt/datamaker
#cp * /opt/datamaker

groupadd -r datamaker
useradd -m -r -g datamaker datamaker

cp /opt/datamaker/datamaker.service /etc/systemd/system/

chown -R datamaker:datamaker /opt/datamaker

systemctl enable datamaker.service

service datamaker status

service datamaker start