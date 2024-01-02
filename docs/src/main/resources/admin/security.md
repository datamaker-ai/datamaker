---
layout: page
title: Security
parent: Admin
nav_order:
---

# Security
## Generate secure password (hashed)

To overwrite a password you can use this command:

`htpasswd -bnBC 10 "" password | tr -d ':\n' | sed 's/$2y/$2a/'`

```properties
admin.password=$2a$10$loR4oKdMPHdpQwVyem5TMu3vR3ktQdGHmQgvKeP3VKiWkim5OvbUa
```

## Encryption key

The default encryption key is changeme.
You can override it using environment variable, java options or application.properties.

```properties
encryption.secret.key=changeme
encryption.salt=changeme
```

# SSL

To generate a self-signed certificate (or use your own):
`$ keytool -genkeypair -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore cert.p12 -validity 365`

Uncomment these settings in application.properties:

```properties
# SSL
server.port=8443
server.ssl.key-store=/home/datamaker/cert.p12
server.ssl.key-store-password=123456

# JKS or PKCS12
server.ssl.keyStoreType=PKCS12

# Spring Security
security.require-ssl=true
```
