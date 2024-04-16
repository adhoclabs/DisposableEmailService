FROM amazoncorretto:11-al2023

ARG jar_path
ENV JAR=$jar_path

RUN yum update && yum install -y jemalloc

# datadog
RUN mkdir -p /app /opt/datadog
COPY ./cicd/datadog/dd-agent.jar /opt/datadog/

# app
COPY ${JAR} /app/email-service.jar

EXPOSE 9000
CMD java ${JAVA_OPTS} -classpath /app/email-service.jar co.adhoclabs.email.Main
