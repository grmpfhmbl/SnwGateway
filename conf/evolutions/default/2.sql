# --- !Ups

CREATE INDEX IDX_MEAS_TIME_DESC
  ON SENSORMEASUREMENTS (meastime DESC);

# --- !Downs

DROP INDEX PUBLIC.IDX_MEAS_TIME_DESC;