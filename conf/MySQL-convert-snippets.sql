### RUN THIS TO ALTER THE MeSHLIUM DtaBASE TO USABLE FORMAT FOR GATEWAY2
ALTER TABLE `SensorMeasurements`
MODIFY `MeasTime` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE TABLE `SensorTextObservations` (
  `idSensorTextObservation` int(11) NOT NULL auto_increment PRIMARY KEY,
  `MeasTime` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `Latitude` float(10,6) NOT NULL,
  `Longitude` float(10,6) NOT NULL,
  `Altitude` float(10,6) NOT NULL,
  `Category` varchar(255) NOT NULL default 'default',
  `TextValue` varchar(255) NOT NULL default 'default',
  `SosTransmitted` tinyint(1) NOT NULL default '0',
  `SosErrorCode` int(11) NOT NULL,
  `SensorNodes_idSensorNode` int(11) NOT NULL,
  `SensorTypes_idSensorType` int(11) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=2711197 DEFAULT CHARSET=utf8;

INSERT INTO SENSORNODES VALUES( 16, '0013A20040BA23BE', 'Meshlium'     , 'Meshlium Meindlbauer'                       ,         0,         0,          0);
INSERT INTO SENSORTYPES VALUES(50,  50, 'System Message'                   ,  0.00, 'System Status'        , 'text'  , 'Generic System Status Message Logger');

#### END TRANSFORMATION OF MESHLIUM DB

## FIND ALL MEASUREMENTS MARKED AS NOT TRANSFERRED
SELECT count(*) FROM sensormeasurements
WHERE 1=1
   AND sostransmitted=FALSE
   AND soserrorcode=-1
ORDER BY meastime ASC
LIMIT 100;
## PUT THEM AWAY FOR THE MOMENT
# UPDATE SensorMeasurements
# SET SosTransmitted = 0, SosErrorCode = -99
# WHERE 1=1
#       AND sostransmitted=FALSE
#       AND soserrorcode=-1
# ORDER BY meastime ASC
# LIMIT 10;


### FIND ALL Hydro8 MEASUREMENTS IN TIMEFRAME
SELECT count(*),min(MeasTime), max(MeasTime), max(SosErrorCode), min(SosErrorCode) FROM SensorMeasurements
WHERE 1=1
      AND sostransmitted=FALSE
      and MeasTime >= '2016-01-01 00:01'
       and MeasTime <= '2016-02-28 23:59'
       and SensorMeasurements.SensorNodes_idSensorNode = 8
#  AND SensorMeasurements.idSensorMeasurement = 1584431
ORDER BY meastime ASC
limit 30;


# update SensorMeasurements
# set SosTransmitted = 0, SosErrorCode = -1
# where 1=1
#       and MeasTime >= '2016-01-01 00:01'
#       and MeasTime <= '2016-02-28 23:59'
#       and SensorMeasurements.SensorNodes_idSensorNode = 8
#      AND SensorMeasurements.idSensorMeasurement = 1531730
#limit 1
#;

select * from SensorMeasurements
where 1=1
#       and MeasTime >= '2016-01-28 00:01'
#       and MeasTime <= '2016-01-28 23:59'
#       and SensorMeasurements.SensorNodes_idSensorNode = 8
#  AND SensorMeasurements.idSensorMeasurement = 1584431
limit 15;

# 2016-01-28 12:01:36

#WHERE
#  REFERENCED_TABLE_SCHEMA = 'SensDB'
#   AND
#   REFERENCED_TABLE_NAME = '<table>';


