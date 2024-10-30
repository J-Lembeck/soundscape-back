FROM openjdk:21-jdk-slim
RUN  apt-get update && apt-get install -y jq
COPY /app/target/soundscape-0.0.1-SNAPSHOT.jar /opt/soundscape-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/opt/soundscape-0.0.1-SNAPSHOT.jar"]
