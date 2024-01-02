
# Linux Daemon
systemctl enable myapp.service

# Docker

- docker images
- docker save datamaker:latest > datamaker.tar
- docker import datamaker.tar

## Docker Registry

1. docker login -u ZDRhYzzzz -p ZDRhYzzzz registry.digitalocean.com
2. docker image ls
3. docker tag dd94b6e31473 registry.digitalocean.com/datamaker/dd94b6e31473
3. docker push registry.digitalocean.com/datamaker/dd94b6e31473

- Private token: ``

- Public token: ``

# Demo Server

apt-get install build-essential

Ok, found a solution. I do not need install linux-generic at EC2 but seems need be a procedure at Google Compute Engine. All procedure to get quota work:

sudo -s
apt-get -y install quota quotatool

nano /etc/fstab

Edit fstab:

LABEL=cloudimg-rootfs   /    ext4   defaults,usrjquota=quota.user,grpjquota=quota.group,jqfmt=vfsv0 0 0

Check for missing packages.

dpkg -s linux-generic
dpkg-query: package 'linux-generic' is not installed and no information is available
Use dpkg --info (= dpkg-deb --info) to examine archive files,
and dpkg --contents (= dpkg-deb --contents) to list their contents.

We can install the full missing linux-generic package:

apt-get -y install linux-generic

Or only the extras packages (I prefer this):

apt-get -y install linux-image-generic
apt-get -y install linux-headers-generic
apt-get -y install linux-image-extra-`uname -r`

We need add the quota modules to start with boot:

echo quota_v1 >> /etc/modules
echo quota_v2 >> /etc/modules

reboot

Check if it's working:

sudo -s
cat /proc/modules | grep -i quota

quota_v1 16384 0 - Live 0xffffffffc037c000
quota_v2 16384 2 - Live 0xffffffffc0377000
quota_tree 20480 1 quota_v2, Live 0xffffffffc0250000

quotaon -pa

group quota on / (/dev/sda1) is on
user quota on / (/dev/sda1) is on


# repquota /quota
# edquota -f /quota chirico

https://linuxize.com/post/create-a-linux-swap-file/
