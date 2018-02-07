# --- !Ups

CREATE TABLE SENSORTYPES (
  idsensortype BIGINT PRIMARY KEY,
  sensid       BIGINT UNIQUE,
  sensorname   VARCHAR,
  placement    DOUBLE,
  phenomenon   VARCHAR,
  unit         VARCHAR,
  description  VARCHAR
);

CREATE TABLE SENSORNODES (
  idsensornode    BIGINT PRIMARY KEY AUTO_INCREMENT,
  extendedaddress VARCHAR UNIQUE,
  name            VARCHAR,
  description     VARCHAR,
  latitude        DOUBLE,
  longitude       DOUBLE,
  altitude        DOUBLE
);

CREATE TABLE SENSORMEASUREMENTS (
  idsensormeasurement      BIGINT PRIMARY KEY AUTO_INCREMENT,
  meastime                 DATETIME,
  latitude                 DOUBLE,
  longitude                DOUBLE,
  altitude                 DOUBLE,
  rawvalue                 DOUBLE,
  calcvalue                DOUBLE,
  sostransmitted           BOOLEAN,
  soserrorcode             BIGINT,
  sensornodes_idsensornode BIGINT,
  sensortypes_idsensortype BIGINT
);

CREATE INDEX IDX_MEAS_TIME
  ON SENSORMEASUREMENTS (meastime);
CREATE INDEX IDX_MEAS_SOS_TRANS
  ON SENSORMEASUREMENTS (sostransmitted);
CREATE INDEX IDX_MEAS_SOS_ERROR
  ON SENSORMEASUREMENTS (soserrorcode);

CREATE TABLE SENSORTEXTOBSERVATIONS (
  idsensortextobservation  BIGINT PRIMARY KEY AUTO_INCREMENT,
  meastime                 DATETIME,
  latitude                 DOUBLE,
  longitude                DOUBLE,
  altitude                 DOUBLE,
  category                 VARCHAR,
  textvalue                VARCHAR,
  sostransmitted           BOOLEAN,
  soserrorcode             BIGINT,
  sensornodes_idsensornode BIGINT,
  sensortypes_idsensortype BIGINT
);

# --- !Downs
DROP TABLE IF EXISTS SENSORTEXTOBSERVATIONS;
DROP TABLE IF EXISTS SENSORMEASUREMENTS;
DROP TABLE IF EXISTS SENSORNODES;
DROP TABLE IF EXISTS SENSORTYPES;