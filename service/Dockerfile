#FROM adoptopenjdk/openjdk12:latest
#RUN mkdir /opt/app
#COPY target/service-1.0-SNAPSHOT-exec.jar /opt/app/service.jar
#ENV SPRING_PROFILES_ACTIVE=dev
#CMD ["java", "-jar", "/opt/app/service.jar"]
#EXPOSE 8080
# mvn clean package spring-boot:repackage

# docker run --publish=8080:8080 -it service

# docker build -t service .

FROM adoptopenjdk:11-jre-hotspot as builder
WORKDIR application
ARG JAR_FILE=target/*-exec.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM adoptopenjdk:11-jre-hotspot
WORKDIR application
RUN addgroup --system --gid 1001 appuser
RUN adduser --system --uid 1001 --group appuser
#RUN chown -R appuser:appuser /opt
RUN mkdir service && chown -R appuser:appuser service
RUN mkdir service/logs && chown -R appuser:appuser service/logs
RUN mkdir service/conf && chown -R appuser:appuser service/conf
RUN mkdir service/index && chown -R appuser:appuser service/index
RUN mkdir service/replay && chown -R appuser:appuser service/replay
USER appuser:appuser
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
ENV SPRING_PROFILES_ACTIVE=''
ENV LOGS_PATH=service/logs
ENV HOME_PATH=service
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
EXPOSE 8080
