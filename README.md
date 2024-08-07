# kapsch-weather-test
This is a project intended for being a code challenge required to apply to a position at Kapsch TrafficCom. It was built with the following tech stack:
* Java SE 21
* Spring Boot 3.3.2
* Gradle 8.8
* MySQL 9.0
* Docker 27.1.1
* H2 Database 2.3 (for integration tests)

## Backend's WAR file generation

### Prerequisites

* Java 21 or above
* Git
* Gradle (optional)

### Steps

1. Clone the following GitHub repository:
```
git clone https://github.com/pipecm/kapsch-weather-test.git
```
2. Move to the project's directory:
```
cd kapsch-weather-test/backend
```
3. Run Gradle build tasks using either the Gradle wrapper or your own Gradle installation:
```
./gradlew clean build war
```
```
gradle clean build war
```
4. If the above command ran successfully, you can find the generated WAR at:
```
kapsch-weather-test/backend/build/libs/forecast-app-0.0.1-SNAPSHOT.war
```

## Local Deployment

### Overview
For deploying locally, it is possible performing it using Docker Compose. It creates and runs the following containers:
* A Tomcat container with the backend WAR file deployed inside it.
* A MySQL container that hosts the database of the application.

### Prerequisites

* Java 21 or above
* Docker CLI (with Docker Compose)
* Git
* Postman (or similar)

### Steps

1. Clone the following GitHub repository:
```
git clone https://github.com/pipecm/kapsch-weather-test.git
```
2. Move to the project's directory:
```
cd kapsch-weather-test
```
3. Deploy locally the project using Docker Compose:
```
sudo docker compose up --build
```
If the above command is executed successfully, the base URL of the local deployment is:
```
http://localhost:8080/forecast-app/v1/'
```
4. After finishing the deployment, run this cURL with Postman or other REST manager (No authentication required):
```
curl --location --request GET 'http://localhost:8080/forecast-app/v1/actuator/health'
```
If the deployment is performed successfully, you should get a response similar to this:
```json
{
    "status": "UP",
    "components": {
        "db": {
            "status": "UP",
            "details": {
                "database": "MySQL",
                "validationQuery": "isValid()"
            }
        },
        "diskSpace": {
            "status": "UP",
            "details": {
                "total": 982818799616,
                "free": 835827900416,
                "threshold": 10485760,
                "path": "/usr/local/tomcat/.",
                "exists": true
            }
        },
        "ping": {
            "status": "UP"
        }
    }
}
```
## Database info
You can access to the database using a tool like DBeaver or similar.
```
Engine: MySQL
Host: localhost
Port: 3306
Database: forecast_app
User: forecast_app
Password: kapsch
```

## CSV file generation
After deployment, you can generate CSV files based in the full history of requests performed to this API. To generate a new CSV file, run the following cURL:

```
curl --location --request POST 'http://localhost:8080/forecast-app/v1/forecast' \
--header 'Content-Type: application/json' \
--data '{
    "latitude": {latitude},
    "longitude": {longitude}
}' > {filename}.csv
```

| Parameter | Description                     | Mandatory | Default value |
|-----------|-----------------------------------------------|-------|-----|
| latitude  | Latitude of the location queried (in degrees) | true  | N/A |
| longitude | Latitude of the location queried (in degrees) | true  | N/A |
| filename  | CSV filename                                  | false | N/A |
