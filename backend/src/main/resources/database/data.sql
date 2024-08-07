CREATE TABLE IF NOT EXISTS `forecast_request` (
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `request_id` binary(16) NOT NULL,
  PRIMARY KEY (`request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `forecast_response_data_record` (
  `temperature` double DEFAULT NULL,
  `wind_direction` int DEFAULT NULL,
  `wind_speed` double DEFAULT NULL,
  `local_request_dt` datetime(6) DEFAULT NULL,
  `utc_request_dt` datetime(6) DEFAULT NULL,
  `forecast_request_id` binary(16) NOT NULL,
  `record_id` binary(16) NOT NULL,
  PRIMARY KEY (`record_id`),
  KEY `FK7j4vr1q45anm2mgwk3nlqbl8q` (`forecast_request_id`),
  CONSTRAINT `FK7j4vr1q45anm2mgwk3nlqbl8q` FOREIGN KEY (`forecast_request_id`) REFERENCES `forecast_request` (`request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE `forecast_request` ADD INDEX `forecast_request_lat_lon_idx` (`latitude`, `longitude`);
ALTER TABLE `forecast_response_data_record` ADD INDEX `forecast_response_request_id` (`forecast_request_id`);