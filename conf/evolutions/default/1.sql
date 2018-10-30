# --- !Ups

CREATE TABLE SENSORTYPES (
  -- idsensortype BIGINT PRIMARY KEY AUTO_INCREMENT,
  idsensortype BIGSERIAL PRIMARY KEY,
  sensid       BIGINT UNIQUE,
  sensorname   VARCHAR,
  placement    FLOAT,
  phenomenon   VARCHAR,
  unit         VARCHAR,
  description  VARCHAR
);

CREATE TABLE SENSORNODES (
--   idsensornode    BIGINT PRIMARY KEY AUTO_INCREMENT,
  idsensornode    BIGSERIAL PRIMARY KEY,
  extendedaddress VARCHAR UNIQUE,
  name            VARCHAR,
  description     VARCHAR,
  latitude        FLOAT,
  longitude       FLOAT,
  altitude        FLOAT
);

CREATE TABLE SENSORMEASUREMENTS (
--   idsensormeasurement      BIGINT PRIMARY KEY AUTO_INCREMENT,
  idsensormeasurement      BIGSERIAL PRIMARY KEY,
  meastime                 TIMESTAMP,
  latitude                 FLOAT,
  longitude                FLOAT,
  altitude                 FLOAT,
  rawvalue                 FLOAT,
  calcvalue                FLOAT,
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
--   idsensortextobservation  BIGINT PRIMARY KEY AUTO_INCREMENT,
  idsensortextobservation  BIGSERIAL PRIMARY KEY,
  meastime                 TIMESTAMP,
  latitude                 FLOAT,
  longitude                FLOAT,
  altitude                 FLOAT,
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