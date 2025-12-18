FROM maven:3.9-eclipse-temurin-21

WORKDIR /app

COPY pom.xml .

RUN mvn -q -DskipTests dependency:go-offline

CMD ["mvn", "-Dspring-boot.run.profiles=dev", "spring-boot:run"]
