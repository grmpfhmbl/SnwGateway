select
  sostransmitted,
  soserrorcode,
  count(sostransmitted)
from sensormeasurements
group by sostransmitted, soserrorcode;
-- ###################################
select
  meastime,
  sensortypes_idsensortype,
  sensornodes_idsensornode,
  count(*),
  max(idsensormeasurement),
  min(idsensormeasurement)
from sensormeasurements
group by meastime,
  sensortypes_idsensortype,
  sensornodes_idsensornode
having count(*) > 1
order by meastime desc;

-- ###################################
select count(*)
from sensormeasurements;
select count(*)
from sensormeasurements
where sostransmitted = false;
-- ###################################
update sensormeasurements
set sostransmitted = false,
  soserrorcode     = -1
where idsensormeasurement >= 13000
      and idsensormeasurement < 14000;
-- ###################################
insert into sensormeasurements (
  idsensormeasurement
  , meastime
  , latitude
  , longitude
  , altitude
  , rawvalue
  , calcvalue
  , sostransmitted
  , soserrorcode
  , sensornodes_idsensornode
  , sensortypes_idsensortype)
  select
    idsensormeasurement,
    meastime,
    latitude,
    longitude,
    altitude,
    rawvalue,
    calcvalue,
    sostransmitted,
    soserrorcode,
    sensornodes_idsensornode,
    sensortypes_idsensortype
  from csvread('/Users/steffen/sensormeasurements.csv')

-- ###################################
select
  n.name,
  sostransmitted,
  count(*)
from sensormeasurements m
  inner join sensornodes n on n.idsensornode = m.sensornodes_idsensornode
group by sensornodes_idsensornode, sostransmitted
order by n.name;
-- ###################################
select
  n.name,
  sostransmitted,
  meastime
from sensormeasurements m
  inner join sensornodes n on n.idsensornode = m.sensornodes_idsensornode
where
  IDSENSORMEASUREMENT = 67154;
-- ###################################
CALL CSVWRITE('./measurements.plainfeldgw.csv',
              'SELECT ' ||
              'IDSENSORMEASUREMENT, ' ||
              'MEASTIME, ' ||
              'LATITUDE, ' ||
              'LONGITUDE, ' ||
              'ALTITUDE, ' ||
              'RAWVALUE, ' ||
              'CALCVALUE, ' ||
              'SOSTRANSMITTED, ' ||
              'SOSERRORCODE, ' ||
              'SENSORNODES_IDSENSORNODE, ' ||
              'SENSORTYPES_IDSENSORTYPE ' ||
              'FROM SENSORMEASUREMENTS');
-- ###################################
INSERT INTO SENSORMEASUREMENTS (
  MEASTIME,
  LATITUDE,
  LONGITUDE,
  ALTITUDE,
  RAWVALUE,
  CALCVALUE,
  SOSTRANSMITTED,
  SOSERRORCODE,
  SENSORNODES_IDSENSORNODE,
  SENSORTYPES_IDSENSORTYPE)
  SELECT
    MEASTIME,
    LATITUDE,
    LONGITUDE,
    ALTITUDE,
    RAWVALUE,
    CALCVALUE,
    0,
    --  SOSTRANSMITTED,
    -1,
    --  SOSERRORCODE,
    SENSORNODES_IDSENSORNODE,
    SENSORTYPES_IDSENSORTYPE
  FROM CSVREAD('./measurements.plainfeldgw.csv');
-- ###################################
select
       meastime,
       sensortypes_idsensortype,
       sensornodes_idsensornode,
       count(*),
       max(idsensormeasurement),
       min(idsensormeasurement)
from sensormeasurements
where idsensormeasurement < 1000000
group by meastime,
         sensortypes_idsensortype,
         sensornodes_idsensornode
having count(*) > 1
order by meastime desc;

-- ###################################
-- delete duplicated meastime/sensornode/sensor entries in database
delete from sensormeasurements
where idsensormeasurement in (
  select id2delete
  from (
    select
      meastime,
      sensortypes_idsensortype,
      sensornodes_idsensornode,
      count(*),
      max(idsensormeasurement) AS id2delete,
      min(idsensormeasurement)
    from sensormeasurements
    where idsensormeasurement < 2000000
    group by meastime,
      sensortypes_idsensortype,
      sensornodes_idsensornode
    having count(*) > 1
    order by meastime desc
  )
);