FROM eclipse-temurin:latest AS build
ADD . /app
WORKDIR /app
RUN ./gradlew clean build

FROM eclipse-temurin:latest AS service
COPY --from=build /app/build/libs/forecast-app-0.0.1-SNAPSHOT.jar /app/forecast-app-0.0.1-SNAPSHOT.jar
RUN chmod +x /app/forecast-app-0.0.1-SNAPSHOT.jar
RUN useradd forecast-app
USER forecast-app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/forecast-app-0.0.1-SNAPSHOT.jar"]