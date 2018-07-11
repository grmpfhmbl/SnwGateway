# --- !Ups

alter table SENSORMEASUREMENTS
  add constraint uq_meastime_node_type unique (meastime, sensornodes_idsensornode, sensortypes_idsensortype);

# --- !Downs

alter table SENSORMEASUREMENTS
  drop constraint uq_meastime_node_type;
