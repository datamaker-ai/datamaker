# Build demo 

Use Maven Profile 'demo'

`mvn clean install -P demo,relase`

# Docker Image
```
mvn clean install -P docker
docker build -t datamaker:${project.version} service/
docker run -p 8080:8080 --name datamaker datamaker:${project.version}
```

# Local development
- Set Environment variable
```
BITBUCKET_BUILD_NUMBER=1234
LOGS_PATH=/home/datamaker/logs
```

## Build on Windows
1. Download winutils https://github.com/steveloughran/winutils
2. Set environment variable: HADOOP_HOME
3. Install Ruby Development Kit
4. Add Ruby make path

# Official Builds
- Demo
    - mvn clean install -Prelease -Drevision=1.0.0-DEMO
- Release (ZIP)
    - mvn clean install -Prelease
- Azure oauth + zip
    - mvn clean install -Pazure
- Cognito
    - mvn clean install -Pamazon
- Google
    - mvn clean install -Pgcp
- Tomcat (WAR)
    - mvn clean install -Ptomcat

# Release (bump version)
- mvn -DskipTests --batch-mode release:prepare
- mvn -DskipTests --batch-mode release:perform 

# Custom launcher

```
java \
-cp fat_app.jar \
-Dloader.path=<path_to_your_additional_jars> \
org.springframework.boot.loader.PropertiesLauncher
```

# Override Spring configuration

```
java -jar myproject.jar --spring.config.location=classpath:/default.properties,classpath:/override.properties
```