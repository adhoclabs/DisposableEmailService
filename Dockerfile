FROM openjdk:11.0.8-jre

ARG jar_path
ENV JAR=$jar_path

RUN mkdir -p /app /opt/newrelic
COPY ./cicd/newrelic/newrelic.jar /opt/newrelic/
COPY ./cicd/newrelic/newrelic.yml /opt/newrelic/
COPY ${JAR} /app/music-service.jar

EXPOSE 9000
CMD java ${JAVA_OPTS} -classpath /app/music-service.jar co.adhoclabs.microservice_template.Main
