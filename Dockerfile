FROM openjdk:21-jdk-slim
RUN  apt-get update && apt-get install -y jq
RUN mvn package -DskipTests
WORKDIR /app
COPY --from=builder /app/target/soundscape-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
