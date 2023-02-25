FROM openjdk:11.0.8-jre

ARG jar_path
ENV JAR=$jar_path

# datadog
RUN mkdir -p /app /opt/datadog
COPY ./cicd/datadog/dd-agent.jar /opt/datadog/

# app
COPY ${JAR} /app/music-service.jar

EXPOSE 9000
CMD java ${JAVA_OPTS} -classpath /app/music-service.jar co.adhoclabs.microservice_template.Main
