FROM maven:3.8.4-jdk-11-slim as builder
WORKDIR /src
COPY ./pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
COPY ./src ./src
RUN mvn package -DskipTests


FROM alpine:3.15.3
RUN apk --no-cache add openjdk11-jre
COPY --from=builder /src/target/money-transfer-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
