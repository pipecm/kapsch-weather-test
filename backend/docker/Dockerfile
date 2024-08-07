FROM tomcat:latest
ADD ../build/libs/forecast-app-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/
RUN mv /usr/local/tomcat/webapps/forecast-app-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/forecast-app#v1.war
CMD chmod +x /usr/local/tomcat/bin/catalina.sh