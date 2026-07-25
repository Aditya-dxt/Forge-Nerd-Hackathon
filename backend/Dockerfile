FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY . /app
WORKDIR /app
RUN ./mvnw clean package -DskipTests
EXPOSE 8080
ENTRYPOINT ["java","-jar","target/goalflow-backend-0.0.1-SNAPSHOT.jar"]
