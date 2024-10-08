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
* An Angular container with the frontend layer of the application.
* A Tomcat container with the backend WAR file deployed inside it.
* A MySQL container that hosts the database of the application.

### Prerequisites

* Docker CLI (with Docker Compose)
* Git
* An internet browser
* Postman or similar

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
After deployment, you can generate CSV files based in the full history of requests performed to this API. 

### Generation via GUI 
To generate a new CSV file using the GUI, go to the following URL in your browser:
```
http://localhost:4200
```
Enter the latitude and longitude in the form and then, press the button `Generate CSV Forecast File`
After that, the CSV file is downloaded to your local environment.

### Generation via cURL
To generate a new CSV file via CLI or Postman, run the following cURL:

```
curl --location --request POST 'http://localhost:8080/forecast-app/v1/forecast' \
--header 'Content-Type: application/json' \
--data '{
    "latitude": {latitude},
    "longitude": {longitude}
}'
```

| Parameter | Description                     | Mandatory | Default value |
|-----------|-----------------------------------------------|-------|-----|
| latitude  | Latitude of the location queried (in degrees) | true  | N/A |
| longitude | Latitude of the location queried (in degrees) | true  | N/A |

### Response codes
| Code | Description           | 
|------|-----------------------|
| 200  | Success response (OK) | 
| 400  | Bad Request           | 
| 500  | Internal Server Error | 

### Sample success response
```csv
Local timestamp,UTC timestamp,Latitude,Longitude,Temperature (°C),Wind speed (km/h),Wind direction (°)
2024-08-07T19:05:18.958591,2024-08-07T19:05:18.958619,-33.5,-70.625,10.9,6.3,204
2024-08-07T19:10:51.172171,2024-08-07T19:10:51.172208,-33.5,-70.625,10.9,6.3,204
2024-08-07T19:04:39.293515,2024-08-07T19:04:39.293538,-33.5,-70.625,10.9,6.3,204
2024-08-07T18:06:39.020760,2024-08-07T18:06:39.020793,-33.5,-70.625,10.8,4.6,231
```

### Sample error response
```json
{
    "code": 400,
    "status": "BAD_REQUEST",
    "message": "Bad Request: Latitude and longitude are required"
}
```
© Created by [Felipe Cardenas](https://www.linkedin.com/in/felipecardenasm) in August 2024. All rights reserved.