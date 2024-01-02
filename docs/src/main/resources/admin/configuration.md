---
layout: page
title: Configuration
parent: Admin
---

# Configuration

## Properties file

The application settings are stored in /opt/datamaker/application.properties file.
You can edit them and restart the application for taking effect.

## Java System properties

You can override application settings from the JAVA_OPTS variable in service.conf (or in Tomcat setenv.sh).

JAVA_OPTS='-Xmx512M -Dlogging.config=/opt/datamaker/logback-spring.xml'

Example: -logging.file.max-size=50MB

JAVA_OPTS='-Xmx512M -Dlogging.config=/opt/datamaker/logback-spring.xml -logging.file.max-size=50MB'

## Binding from Environment Variables

You can also use Windows or Linux environment variable to override application settings.
You need to use the snake case formatting.

Ex: spring.datasource.url becomes SPRING_DATASOURCE_URL

EXPORT SPRING_DATASOURCE_URL=jdbc:mysql://127.0.0.1:3306/datamaker

### Naming convention

Most operating systems impose strict rules around the names that can be used for environment variables. For example, Linux shell variables can contain only letters (a to z or A to Z), numbers (0 to 9) or the underscore character (_). By convention, Unix shell variables will also have their names in UPPERCASE.

Spring Boot’s relaxed binding rules are, as much as possible, designed to be compatible with these naming restrictions.

To convert a property name in the canonical-form to an environment variable name you can follow these rules:

    Replace dots (.) with underscores (_).

    Remove any dashes (-).

    Convert to uppercase.

For example, the configuration property spring.main.log-startup-info would be an environment variable named SPRING_MAIN_LOGSTARTUPINFO.

Environment variables can also be used when binding to object lists. To bind to a List, the element number should be surrounded with underscores in the variable name.

For example, the configuration property my.service[0].other would use an environment variable named MY_SERVICE_0_OTHER.

