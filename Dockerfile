FROM eclipse-temurin:11-jdk

WORKDIR /app

COPY gradlew gradlew.bat settings.gradle build.gradle gradle.properties ./
COPY gradle ./gradle
COPY buildSrc ./buildSrc
COPY modules ./modules
COPY src ./src

RUN chmod +x ./gradlew

EXPOSE 8080

CMD ["./gradlew", "--no-daemon", "run", "-Daxelor.config=/app/src/main/resources/axelor-config.properties", "-Ddb.default.url=jdbc:postgresql://db:5432/elap_db", "-Ddb.default.user=postgres", "-Ddb.default.password=postgres"]
