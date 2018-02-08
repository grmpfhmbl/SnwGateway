-- SIZE NZ Rangitaiki
-- TODO SensorID fürs GW auf 1, siehe TODO im DbActor
--INSERT INTO SENSORNODES VALUES ( 16,  '0013A20040BA2318',  'Raspi_NZ',   'Raspberry Pi NZ GNS Rangitaiki' ,      -38.8194, 176.4875, 750.00 );
--INSERT INTO SENSORNODES VALUES ( 12,  '0013A20040BA2208',  'NZ.Cust02',  'Cust.02+NZ_Wasp.01+XB.40BA2208 combo', -38.8194, 176.4875, 750.00 );

-- KOPPL
-- When changing the "gateway" node (ID == 1) also change sensorweb.uplink.sos.node_equivalent="0013A20040C5407F"
-- in application.conf
-- INSERT INTO SENSORNODES VALUES( 1, '0013A20040B5B303', 'PlainfGw'     , 'Gateway Plainfelder Bach'                   ,         0,         0,          0);
-- INSERT INTO SENSORNODES VALUES( 1, '0013A20040B5B303', 'Meshlium'     , 'Meshlium Meindlbauer'                       ,         0,         0,          0);
INSERT INTO SENSORNODES VALUES( 1, '0013A20040C5407F', 'RooftopGw'    , 'Gateway for Techno-Z Rooftop'               , 47.823516, 13.039458, 417.000000);
-- INSERT INTO SENSORNODES VALUES( 1, '0013A200407ACC4C', 'NocksteinGw'    , 'Gateway at Nockstein'                     , 47.815598, 13.120290, 954.000000);

INSERT INTO SENSORNODES VALUES( 2, '0012100000000001', 'SommerSPA'    , 'Snow Pack Analyzer (Fa. Sommer)'            , 47.817471, 13.154540, 724.000000);
INSERT INTO SENSORNODES VALUES( 3, '0013A2004061645D', 'Agri01'       , 'Agriculture Board on Waspmote 1.1'          , 47.817471, 13.154540, 724.000000);
INSERT INTO SENSORNODES VALUES( 4, '0013A20040C5407C', 'Hydro01'      , 'Customboard (Hydroboard) 01 @Wind'          , 47.829231, 13.191767, 714.000000);
INSERT INTO SENSORNODES VALUES( 5, '0013A20040C541B5', 'Hydro02'      , 'Customboard (Hydroboard) 02 @Knollbauer'    , 47.827278, 13.159145, 714.000000);
INSERT INTO SENSORNODES VALUES( 6, '0013A20040BA23B7', 'Hydro03'      , 'Techno-Z rooftop (Hydroboard)'              , 47.823516, 13.039458, 417.000000);
-- INSERT INTO SENSORNODES VALUES( 7, '0013A20040BA23BC', 'Hydro04'      , 'Customboard (Hydroboard) 04 (am SPA)'       , 47.817471, 13.154540, 724.000000);
-- Originally Hydro05 had "0013A20040616498". That was broken so replaced it with Gas01
INSERT INTO SENSORNODES VALUES( 8, '0013A20040C54083', 'Hydro05'      , 'Customboard (Hydroboard) 05 @Nockstein'     , 47.815598, 13.120290, 954.000000);
INSERT INTO SENSORNODES VALUES( 9, '0013A20040C5407E', 'Hydro06'      , 'Customboard (Hydroboard) 06 @Pferdehof'     , 47.819267, 13.183000, 714.000000);
INSERT INTO SENSORNODES VALUES(10, '0013A20040BA23C8', 'Hydro07'      , 'Customboard (Hydroboard) 07 @Strommast'     , 47.810501, 13.160983, 714.000000);
-- INSERT INTO SENSORNODES VALUES(11, '0013A20040BA23B2', 'Hydro08'      , 'Customboard (Hydroboard) 08 with Waterlevel', 47.818260, 13.150818, 714.000000);
INSERT INTO SENSORNODES VALUES(12, '0013A20040BA23C7', 'Hydro09'      , 'Customboard (Hydroboard) 09 @Plainf Bach'   , 47.823708, 13.152070, 714.000000);
-- INSERT INTO SENSORNODES VALUES(13, '0013A20000000000', 'Gas01'        , 'Z_GAS board 01'                             , 47.818260, 13.150818, 714.000000);
-- 47.817524, 13.154254

-- INSERT INTO SENSORNODES VALUES(16, '0013A20040B3159A', 'Router01'     , 'ZigBEE-Router Meindl'                       , 47.817183, 13.153886, 724.000000);
-- INSERT INTO SENSORNODES VALUES(17, '0013A2004061649B', 'Router02'     , 'ZigBEE-Router Nockstein'                    , 47.815598, 13.120290, 954.000000);

--LAB & test
-- INSERT INTO SENSORNODES VALUES( 1, '0013A20040BA23BE', 'LabGateway'   , 'Gateway Lab Station'                        ,         0,         0,          0);
INSERT INTO SENSORNODES VALUES(14, '0013A20040616484', 'HydroLab01'   , 'Customboard (Hydroboard) at Lab-Station 01' , 47.823377, 13.039382, 417.000000);
-- INSERT INTO SENSORNODES VALUES(15, '0012100000000002', 'WIZTest01'    , 'Water Insitu Analyzer - Mock and Testing'   ,         0,         0,          0);

--GK See
-- 0013A20040E6998A,AgriGK,Agriculture Board Gossenkoelle See,47.2282918,11.0145114,2413.0
-- 0013A20040E605B1,WaterGK,SmartWater Gossenkoelle See,47.2282918,11.0145114,2413.0
-- INSERT INTO SENSORNODES VALUES( 1, '0013A20040BA23BE', 'GKSeeGw'      , 'Gateway Goessenkoelle See'                  , 47.228291, 11.014511,     2413.0);
-- INSERT INTO SENSORNODES VALUES( 2, '0013A20040E6998A', 'GKAgri01'     , 'Agriculture Board Goessenkoelle See'        , 47.228291, 11.014511,     2413.0);
-- INSERT INTO SENSORNODES VALUES( 3, '0013A20040E605B1', 'GKWater01'    , 'SmartWater Board Goessenkoelle See'         , 47.228291, 11.014511,     2413.0);
-- INSERT INTO SENSORNODES VALUES( 4, '0013A2004102ECD9', 'GKYsi01'      , 'YSI I Goessenkoelle See'                    , 47.228291, 11.014511,     2413.0);
-- INSERT INTO SENSORNODES VALUES( 5, '0013A2004102ECE3', 'GKYsi02'      , 'YSI II Goessenkoelle See'                   , 47.228291, 11.014511,     2413.0);

-- SONY DAC
-- INSERT INTO SENSORNODES VALUES( 1, '0013A20041678B80', 'SonyGW'      , 'Gateway SONY-DAC'                          ,47.8487914,13.2175185,      575.0);
-- INSERT INTO SENSORNODES VALUES(18, '0013A20041678B83', 'SonyAgri01'  , 'SMART Agriculture @SONY-DAC'               ,47.8501376,13.2189362,      576.0);
-- INSERT INTO SENSORNODES VALUES(19, '0013A20041678B85', 'SonyHydro01' , 'Hydroboard @SONY-DAC'                      ,47.8501376,13.2189362,      576.0);

------------------------------------------------------------------------------
------------------------------------------------------------------------------
------------------------------------------------------------------------------
-- SENSOR TYPES

------------------------------------------------------------------------------
-- Hydro Board
INSERT INTO SENSORTYPES VALUES( 1,  32, 'DS1820'                           ,  0.00, 'Temperature'          , '°C'    , 'Temperature sensor port1');
INSERT INTO SENSORTYPES VALUES( 2,  33, 'DS1820'                           ,  0.00, 'Temperature'          , '°C'    , 'Temperature sensor port2');
INSERT INTO SENSORTYPES VALUES( 3,  34, 'DS1820'                           ,  0.00, 'Temperature'          , '°C'    , 'Temperature sensor port3');
INSERT INTO SENSORTYPES VALUES( 4,  35, 'DS1820'                           ,  0.00, 'Temperature'          , '°C'    , 'Temperature sensor port4');
INSERT INTO SENSORTYPES VALUES( 5,  36, 'DS1820'                           ,  0.00, 'Temperature'          , '°C'    , 'Temperature sensor port5');
INSERT INTO SENSORTYPES VALUES( 6,  28, 'Q-PSB(bivalent)'                  ,-10.00, 'Water Level'          , 'mm'    , 'Waterlevel sensor 1');
INSERT INTO SENSORTYPES VALUES( 7,  29, 'Q-PSB(bivalent)'                  ,-10.00, 'Water Level'          , 'mm'    , 'Waterlevel sensor 2');
INSERT INTO SENSORTYPES VALUES( 8,  40, '10HS'                             , -0.25, 'Soil Moisture'        , '%'     , 'Soil moisture sensor 1');
INSERT INTO SENSORTYPES VALUES( 9,  41, '10HS'                             , -0.50, 'Soil Moisture'        , '%'     , 'Soil moisture sensor 2');
INSERT INTO SENSORTYPES VALUES(10,  42, '10HS'                             , -0.75, 'Soil Moisture'        , '%'     , 'Soil moisture sensor 3');
INSERT INTO SENSORTYPES VALUES(11,  44, 'Battery Voltage'                  ,  0.00, 'Battery Voltage'      , 'ADC'   , 'Battery Voltage Main Station');
INSERT INTO SENSORTYPES VALUES(19,   9, 'Davis RainCollector 2'            ,  0.00, 'Rainfall'             , 'mm'    , 'Rainfall in mm since last measurement');
INSERT INTO SENSORTYPES VALUES(20,  10, 'Davis Vantage Pro2 Anemometer'    ,  0.00, 'Wind speed'           , 'km/h'  , 'Wind speed of Davis Anemometer');
INSERT INTO SENSORTYPES VALUES(21,  11, 'Davis Vantage Pro2 Wind direction',  0.00, 'Wind direction'       , 'deg'   , 'Wind direction of Davis wind vane');
INSERT INTO SENSORTYPES VALUES(23, 257, 'Battery Level'                    ,  0.00, 'Battery Percentage'   , '%'     , 'Battery Level Waspmote');

------------------------------------------------------------------------------
-- Agriculture board (SONY DADC
INSERT INTO SENSORTYPES VALUES(12,   1, '808H5V5'                          ,  0.00, 'Humidity'             , '%'     , 'Humidity Sensor');
INSERT INTO SENSORTYPES VALUES(13,   3, 'MPX4115A'                         ,  0.00, 'Atmospheric Pressure' , 'cbar'  , 'Atmospheric pressure Sensor');
INSERT INTO SENSORTYPES VALUES(14,   4, 'SHT75'                            ,  0.00, 'Humidity'             , '%'     , 'I2C humidity Sensor');
INSERT INTO SENSORTYPES VALUES(15,   5, 'SHT75'                            ,  0.00, 'Temperature'          , '°C'    , 'I2C Temperatur Sensor');
INSERT INTO SENSORTYPES VALUES(22,  12, 'Solar Radiation'                  ,  0.00, 'Solar Radiation'      , 'µmol/m^2/s' , 'Solar radiation sensor');
INSERT INTO SENSORTYPES VALUES(16,   6, 'Watermark'                        ,  0.00, 'Soil Water Tension'   , 'cbar'  , 'Soil moisture sensor port1');
INSERT INTO SENSORTYPES VALUES(17,   7, 'Watermark'                        ,  0.00, 'Soil Water Tension'   , 'cbar'  , 'Soil moisture sensor port2');
INSERT INTO SENSORTYPES VALUES(18,   8, 'Watermark'                        ,  0.00, 'Soil Water Tension'   , 'cbar'  , 'Soil moisture sensor port3');
INSERT INTO SENSORTYPES VALUES(58, 163, 'Windvane WS3000'                  ,  0.00, 'Wind direction'       , '°'     , 'WS3000 Wind vane');
INSERT INTO SENSORTYPES VALUES(59, 164, 'Anemometer WS3000'                ,  0.00, 'Wind speed'           , 'km/h'  , 'WS3000 Anemometer');
INSERT INTO SENSORTYPES VALUES(60, 165, 'Pluviometer WS3000'               ,  0.00, 'Rainfall'             , 'mm'    , 'WS3000 Pluviometer');
INSERT INTO SENSORTYPES VALUES(61, 166, 'Temperature BME280'               ,  0.00, 'Temperature'          , '°C'    , 'BME280 Temperature');
INSERT INTO SENSORTYPES VALUES(62, 167, 'Humidity BME280'                  ,  0.00, 'Humidity'             , '%'     , 'BME280 Humidity');
INSERT INTO SENSORTYPES VALUES(63, 168, 'Pressure BME280'                  ,  0.00, 'Atmospheric Pressure' , 'Pa'    , 'BME280 Pressure');
INSERT INTO SENSORTYPES VALUES(64, 169, 'Temperature PT1000'               ,  0.00, 'Temperature'          , '°C'    , 'PT1000 Temperature');

------------------------------------------------------------------------------
-- SPA
INSERT INTO SENSORTYPES VALUES(24, 304, 'SPA ice content 2'                ,  0.00, 'Ice content'          , '%'     , 'Sommer Sensor 2 Ice content');
INSERT INTO SENSORTYPES VALUES(25, 305, 'SPA water content 2'              ,  0.00, 'Water content'        , '%'     , 'Sommer Sensor 2 Water content');
INSERT INTO SENSORTYPES VALUES(26, 306, 'SPA snow density 2'               ,  0.00, 'Snow density'         , 'kg/m^3', 'Sommer Sensor 2 Snow density');
INSERT INTO SENSORTYPES VALUES(27, 307, 'SPA SWE 2'                        ,  0.00, 'Snow water equivalent', 'mm'    , 'Sommer Sensor 2 SWE');
INSERT INTO SENSORTYPES VALUES(28, 316, 'SPA snow depth'                   ,  0.00, 'Snow depth'           , 'mm'    , 'Sommer snow depth sensor');
INSERT INTO SENSORTYPES VALUES(29, 318, 'SPA chip temperature'             ,  0.00, 'Chip temperature'     , '°C'    , 'Sommer snow depth sensor chip temperature');
INSERT INTO SENSORTYPES VALUES(30, 300, 'SPA ice content 1'                ,  0.00, 'Ice content'          , '%'     , 'Sommer Sensor 1 Ice content');
INSERT INTO SENSORTYPES VALUES(31, 301, 'SPA water content 1'              ,  0.00, 'Water content'        , '%'     , 'Sommer Sensor 1 Water content');
INSERT INTO SENSORTYPES VALUES(32, 302, 'SPA snow density 1'               ,  0.00, 'Snow density'         , 'kg/m^3', 'Sommer Sensor 1 Snow density');
INSERT INTO SENSORTYPES VALUES(33, 303, 'SPA SWE 1'                        ,  0.00, 'Snow water equivalent', 'mm'    , 'Sommer Sensor 1 SWE');

------------------------------------------------------------------------------
-- Z_GAS board
-- INSERT INTO SENSORTYPES VALUES (35,13,'<name>',0.0,'CO2','ppm','<description>');
INSERT INTO SENSORTYPES VALUES(36,  14, 'CO  - TGS2442'                    ,  0.00, 'CO'                   , 'ppm'   , 'Gas Sensor CO - TGS2442');
INSERT INTO SENSORTYPES VALUES(37,  15, 'CH4 - TGS2611'                    ,  0.00, 'CH4'                  , 'ppm'   , 'Gas Sensor CH4 - TGS2611');
INSERT INTO SENSORTYPES VALUES(38,  16, 'NH3 - TGS2444'                    ,  0.00, 'NH3'                  , 'ppm'   , 'Gas Sensor NH3 - TGS2444');
INSERT INTO SENSORTYPES VALUES(39,  17, 'NO2 - mics2744'                   ,  0.00, 'NO2'                  , 'ppm'   , 'Gas Sensor NO2 - mics2744');
INSERT INTO SENSORTYPES VALUES(40,  18, 'DHT22 humidity'                   ,  0.00, 'Humidity'             , '%'     , 'Humidity Sensor DHT22');
INSERT INTO SENSORTYPES VALUES(41,  19, 'DHT22 temperature'                ,  0.00, 'Temperature'          , '°C'    , 'Temperature Sensor DHT22');

------------------------------------------------------------------------------
-- WIZ
INSERT INTO SENSORTYPES VALUES(51,  51, 'WIZ-TP'                           ,  0.00, 'Total Phosphorus'     , 'ppb'   , 'WIZ Method 1 - Total Phosphorus');
INSERT INTO SENSORTYPES VALUES(52,  52, 'WIZ-OP'                           ,  0.00, 'Bioavailable Phosphorus', 'ppb' , 'WIZ Method 2 - Bioavailable Phosphorus');
INSERT INTO SENSORTYPES VALUES(53,  53, 'WIZ-PO4'                          ,  0.00, 'Orthophosphate'       , 'ppb'   , 'WIZ Method 3 - Orthophosphate');

------------------------------------------------------------------------------
-- Generic Sensors
INSERT INTO SENSORTYPES VALUES(34,  45, 'RSSI'                             ,  0.00, 'RSSI'                 , 'dBm'   , 'Received Signal Strength Indicator');
INSERT INTO SENSORTYPES VALUES(50,  50, 'System Message'                   ,  0.00, 'System Status'        , 'text'  , 'Generic System Status Message Logger');
INSERT INTO SENSORTYPES VALUES(54,  88, 'Waspmote RTC Temp'                ,  0.00, 'RTC Temp'             , '°C'    , 'Waspmote RTC Temperature');


------------------------------------------------------------------------------
-- SmartWater
INSERT INTO SENSORTYPES VALUES(55, 160, 'PT1000'                           ,  0.00, 'Water Temperature'    , '°C'    , 'Water Temperature');
INSERT INTO SENSORTYPES VALUES(56, 161, 'DissolvedOxygen'                  ,  0.00, 'Dissolved Oxygen'     , '%'     , 'Dissolved Oxygen');
INSERT INTO SENSORTYPES VALUES(57, 162, 'Conductivity'                     ,  0.00, 'Conductivity'         , 'mS/cm' , 'Conductivity');

-- STECA Tarom
--INSERT INTO SENSORTYPES VALUES ( 300, 330, 'RS232 Daten-Info1', 0.0, 'Versionsnummer', 'code', '1, kompatibel mit MPPT und Tarom 4545');
--INSERT INTO SENSORTYPES VALUES ( 301, 331, 'RS232 Daten-Info2', 0.0, 'Datum', 'date', 'YYYY/MM/TT');
--INSERT INTO SENSORTYPES VALUES ( 302, 332, 'RS232 Daten-Info3', 0.0, 'Zeit', 'time', 'hh:mm 24 h-Format');
--INSERT INTO SENSORTYPES VALUES ( 303, 333, 'RS232 Daten-Info4', 0.0, 'Batteriespannung', 'V', 'U bat');
--INSERT INTO SENSORTYPES VALUES ( 304, 334, 'RS232 Daten-Info5', 0.0, 'Modulspannung 1', 'V', 'String 1, Wert und Format entsprechend Einstellung im Display (Wert vom RS485-Master)');
--
--INSERT INTO SENSORTYPES VALUES ( 305, 335, 'RS232 Daten-Info6', 0.0, 'Modulspannung 2', 'V', 'String 2, Wert und Format entsprechend Einstellung im Display (Wert vom RS485-Master) nur Tarom 4545');
--INSERT INTO SENSORTYPES VALUES ( 306, 336, 'RS232 Daten-Info7', 0.0, 'Ladezustand SOC', '%', 'Wert und Format entsprechend Einstellung im Display (Wert vom RS485-Master)');
--INSERT INTO SENSORTYPES VALUES ( 307, 337, 'RS232 Daten-Info8', 0.0, 'State of health', '%', '(SOH) Wert und Format entsprechend Einstellung im Display (Wert vom RS485-Master) nur Tarom 4545');
--
--INSERT INTO SENSORTYPES VALUES ( 308, 338, 'RS232 Daten-Info9', 0.0, 'Gesamt-Batteriestrom', 'A', 'Batterie-Ladestrom Master + Batterie-Ladestrom Slave (0…x) + Batterie-Ladestrom ext. Stromsensoren ( 0…x)');
--INSERT INTO SENSORTYPES VALUES ( 309, 339, 'RS232 Daten-Info10', 0.0, 'max Modul-Eingangsstrom 1', 'A', 'String 1, IPV max (PWM eingeschaltet)');
--INSERT INTO SENSORTYPES VALUES ( 310, 340, 'RS232 Daten-Info11', 0.0, 'max Modul-Eingangsstrom 2', 'A', 'String 2 IPV max (PWM eingeschaltet), nur Tarom 4545');
--INSERT INTO SENSORTYPES VALUES ( 311, 341, 'RS232 Daten-Info12', 0.0, 'akt Modul-Eingangsstrom', 'A', 'momentan IPV in = Ibat + ILast');
--
--INSERT INTO SENSORTYPES VALUES ( 312, 342, 'RS232 Daten-Info13', 0.0, 'Gesamt-Ladestrom', 'A', '(Shunts aller Generatoren)Ladestrom Master + Ladestrom Slave (0…x) + Ladestrom ext. Stromsensoren ( 0…x)');
--INSERT INTO SENSORTYPES VALUES ( 313, 343, 'RS232 Daten-Info14', 0.0, 'Laststrom Geraet', 'A', 'Entladestrom wird negativ angezeigt (− wirddargestellt)');
--INSERT INTO SENSORTYPES VALUES ( 314, 344, 'RS232 Daten-Info15', 0.0, 'Gesamt-Laststrom Entladestrom', 'A', 'Summe Batterie-Entladestrom ext. Stromsensoren ( 0…x)');
--INSERT INTO SENSORTYPES VALUES ( 315, 345, 'RS232 Daten-Info16', 0.0, 'Temperatur Batteriesensors', '°C', '(intern/ extern)(Wert vom RS485-Master) Bei Master-/Slave-System wird nur der Wert des an den Master angeschlossenen Sensors verwendet');
--INSERT INTO SENSORTYPES VALUES ( 316, 346, 'RS232 Daten-Info17', 0.0, 'Fehlerzustand', 'code', '0 = kein Fehler, 1 = Info, 2 = Warnung, 3 = Fehler');
--
--INSERT INTO SENSORTYPES VALUES ( 317, 347, 'RS232 Daten-Info18', 0.0, 'Lademodus', 'code', 'Information über den aktuell aktiven Lademodus (float, boost, equal, IUIA, NiMH, Li-Ion) (Wert vom RS485-Master) Buchstabe entsprechend Statusanzeige auf Display');
--INSERT INTO SENSORTYPES VALUES ( 318, 348, 'RS232 Daten-Info19', 0.0, 'Last', 'code', 'Lastschalter: 0 = Aus, 1 = Ein');
--INSERT INTO SENSORTYPES VALUES ( 319, 349, 'RS232 Daten-Info20', 0.0, 'AUX 1', 'code', 'Relais 1: 0 = Aus, 1 = Ein');
--INSERT INTO SENSORTYPES VALUES ( 320, 350, 'RS232 Daten-Info21', 0.0, 'AUX 2', 'code', 'Relais 2: 0 = Aus, 1 = Ein');
--
--INSERT INTO SENSORTYPES VALUES ( 321, 351, 'RS232 Daten-Info22', 0.0, 'Max Ah in Batterie 24h', 'Ah', 'Wert ganzzahlig ausgeben');
--INSERT INTO SENSORTYPES VALUES ( 322, 352, 'RS232 Daten-Info23', 0.0, 'Max Ah in Batterie all', 'Ah', 'seit Erstinbetriebnahme Wert ganzzahlig ausgeben');
--INSERT INTO SENSORTYPES VALUES ( 323, 353, 'RS232 Daten-Info24', 0.0, 'Max Ah in Last 24h', 'Ah', 'Wert ganzzahlig ausgeben');
--INSERT INTO SENSORTYPES VALUES ( 324, 354, 'RS232 Daten-Info25', 0.0, 'Max Ah in Last all', 'Ah', 'seit Erstinbetriebnahme Wert ganzzahlig ausgeben');
--INSERT INTO SENSORTYPES VALUES ( 325, 355, 'RS232 Daten-Info26', 0.0, 'Derating', 'code', '0 = Derating off, 1 = Derating on');
--INSERT INTO SENSORTYPES VALUES ( 326, 356, 'RS232 Daten-Info27', 0.0, 'Cyclic redundancy code CRC', 'code', 'Name: CRC-16-CCITT/openUART Width: 16 Direction: right shift Polynom: 0x8408 CCITT reversed, 2 Byte Länge, Highbyte, Lowbyte gebildet. Mit Semikolon und ohne CR + LF wird der CRC berechnet.');
