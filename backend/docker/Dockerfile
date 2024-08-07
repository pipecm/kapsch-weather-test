FROM eclipse-temurin:latest AS build
ADD . /app
WORKDIR /app
RUN ./gradlew clean build war

FROM tomcat:latest
COPY --from=build /app/build/libs/forecast-app-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/forecast-app-0.0.1-SNAPSHOT.war
RUN mv /usr/local/tomcat/webapps/forecast-app-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/forecast-app#v1.war
CMD chmod +x /usr/local/tomcat/bin/catalina.sh