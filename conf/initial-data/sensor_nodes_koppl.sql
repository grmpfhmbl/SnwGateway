-- KOPPL
-- When changing the "gateway" node (ID == 1) also change sensorweb.uplink.sos.node_equivalent="0013A20040C5407F"
-- in application.conf
-- INSERT INTO SENSORNODES VALUES( 1, '0013A20040B5B303', 'PlainfGw'     , 'Gateway Plainfelder Bach'                   ,         0,         0,          0);
-- INSERT INTO SENSORNODES VALUES( 1, '0013A20040B5B303', 'Meshlium'     , 'Meshlium Meindlbauer'                       ,         0,         0,          0);
INSERT INTO SENSORNODES VALUES( 1, '0013A20040C5407F', 'RooftopGw'    , 'Gateway for Techno-Z Rooftop'               , 47.823516, 13.039458, 417.000000);
-- INSERT INTO SENSORNODES VALUES( 1, '0013A200407ACC4C', 'NocksteinGw'    , 'Gateway at Nockstein'                     , 47.815598, 13.120290, 954.000000);
-- INSERT INTO SENSORNODES VALUES( 1, '0013A20040E69990', 'LabStationGW'    , 'Gateway at LabStation'               , 47.823516, 13.039458, 417.000000);

INSERT INTO SENSORNODES VALUES( 2, '0012100000000001', 'SommerSPA'    , 'Snow Pack Analyzer (Fa. Sommer)'            , 47.817471, 13.154540, 724.000000);
INSERT INTO SENSORNODES VALUES( 3, '0013A2004061645D', 'Agri01'       , 'Agriculture Board on Waspmote 1.1'          , 47.817471, 13.154540, 724.000000);
INSERT INTO SENSORNODES VALUES( 4, '0013A20040C5407C', 'Hydro01'      , 'Customboard (Hydroboard) 01 @Wind'          , 47.829231, 13.191767, 714.000000);
INSERT INTO SENSORNODES VALUES( 5, '0013A20040C541B5', 'Hydro02'      , 'Customboard (Hydroboard) 02 @Knollbauer'    , 47.827278, 13.159145, 714.000000);
INSERT INTO SENSORNODES VALUES( 6, '0013A20040BA23B7', 'Hydro03'      , 'Techno-Z rooftop (Hydroboard)'              , 47.823516, 13.039458, 417.000000);

-- INSERT INTO SENSORNODES VALUES( 7, '0013A20040BA23BC', 'Hydro04'      , 'Customboard (Hydroboard) 04 (am SPA)'       , 47.817471, 13.154540, 724.000000);
-- 0013A20040BA23BC was broken, so replaced with 0013A2004061649B (formerly Router02)
INSERT INTO SENSORNODES VALUES( 7, '0013A2004061649B', 'Hydro04'      , 'Customboard (Hydroboard) 04 (am SPA)'       , 47.817471, 13.154540, 724.000000);

-- INSERT INTO SENSORNODES VALUES( 8, '0013A20040616498', 'Hydro05'      , 'Customboard (Hydroboard) 05 @Nockstein'     , 47.815598, 13.120290, 954.000000);
-- "0013A20040616498" was broken, so replaced with 0013A20040C54083 (formerly Gas01)
INSERT INTO SENSORNODES VALUES( 8, '0013A20040C54083', 'Hydro05'      , 'Customboard (Hydroboard) 05 @Nockstein'     , 47.815598, 13.120290, 954.000000);
INSERT INTO SENSORNODES VALUES( 9, '0013A20040C5407E', 'Hydro06'      , 'Customboard (Hydroboard) 06 @Pferdehof'     , 47.819267, 13.183000, 714.000000);
INSERT INTO SENSORNODES VALUES(10, '0013A20040BA23C8', 'Hydro07'      , 'Customboard (Hydroboard) 07 @Strommast'     , 47.810501, 13.160983, 714.000000);
INSERT INTO SENSORNODES VALUES(11, '0013A20040BA23B2', 'Hydro08'      , 'Customboard (Hydroboard) 08 with Waterlevel', 47.818260, 13.150818, 714.000000);
INSERT INTO SENSORNODES VALUES(12, '0013A20040BA23C7', 'Hydro09'      , 'Customboard (Hydroboard) 09 @Plainf Bach'   , 47.823708, 13.152070, 714.000000);

-- INSERT INTO SENSORNODES VALUES(13, '0013A20000000000', 'Gas01'        , 'Z_GAS board 01'                             , 47.818260, 13.150818, 714.000000);
-- 47.817524, 13.154254
-- INSERT INTO SENSORNODES VALUES(16, '0013A20040B3159A', 'Router01'     , 'ZigBEE-Router Meindl'                       , 47.817183, 13.153886, 724.000000);
-- INSERT INTO SENSORNODES VALUES(17, '0013A2004061649B', 'Router02'     , 'ZigBEE-Router Nockstein'                    , 47.815598, 13.120290, 954.000000);

INSERT INTO SENSORNODES VALUES(18, '744242005335950026', 'TaromMeindl'  , 'Steca Tarom @Meindlbauer'                   , 47.817471, 13.154540, 724.000000);
INSERT INTO SENSORNODES VALUES(19, '000011112222333344', 'TaromPlainf'  , 'Steca Tarom @Plainfelder Bach'              , 47.817471, 13.154540, 724.000000);
INSERT INTO SENSORNODES VALUES(21, '000011112222333355', 'TaromNockst'  , 'Steca Tarom @Nockstein'                     , 47.817471, 13.154540, 724.000000);

--LAB & test
-- INSERT INTO SENSORNODES VALUES( 1, '0013A20040BA23BE', 'LabGateway'   , 'Gateway Lab Station'                        , 47.823377, 13.039382, 417.000000);
INSERT INTO SENSORNODES VALUES(14, '0013A20040616484', 'HydroLab01'   , 'Customboard (Hydroboard) at Lab-Station 01' , 47.823377, 13.039382, 417.000000);
INSERT INTO SENSORNODES VALUES(22, '000011112222333355', 'TaromMockup'  , 'Steca Tarom mockup for testing'           , 47.823377, 13.039382, 417.000000);
INSERT INTO SENSORNODES VALUES(15, '0012100000000002', 'WIZTest01'    , 'Water Insitu Analyzer - Mock and Testing'   , 47.823377, 13.039382, 417.000000);
