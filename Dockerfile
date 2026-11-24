FROM maven:3.9-eclipse-temurin-17

LABEL maintainer="Equipo Cards4Play - EAM" \
      description="API REST Cards4Play (Java 17 + Spark Java)"

ENV TASA_DEFECTO=4000 \
    CARDS4PLAY_ADMIN_PASSWORD=admin123 \
    PORT=4567

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -q package dependency:copy-dependencies -DskipTests

EXPOSE 4567

VOLUME ["/app/data"]

CMD ["java", "-cp", "target/classes:target/dependency/*", "api.Main"]
