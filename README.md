Gateway2 Akka / Scala / Play
=================================

This is the Sensor Gateway.

<!-- [![Build Status][build-status-badge]][build-status-url] -->
[![Issues][issues-badge]][issues-url]

[![License][license-badge]][license-url]

<!-- 
[build-status-badge]: https://img.shields.io/travis/ZGIS/smart-portal-backend.svg?style=flat-square
[build-status-url]: https://travis-ci.org/ZGIS/smart-portal-backend
-->
[issues-badge]: https://img.shields.io/github/issues/grmpfhmbl/SnwGateway.svg?style=flat-square
[issues-url]: https://github.com/grmpfhmbl/SnwGateway/issues
[license-badge]: https://img.shields.io/badge/License-Apache%202-blue.svg?style=flat-square
[license-url]: LICENSE

## Build

To create the release package just run (_activator.bat_ under Windows)

    ./activator dist

## Installation

This package is meant to be installed on Raspberry PI using Raspian. Should work on any other Linux distribution as
well. For Windows and MacOS no guarantee :-)

Copy archive to Raspberry and unzip

```
    $ unzip gateway2-1.1.zip
    $ cd gateway2-1.1
    $ chmod +x service_installer/h2server/*.sh
    $ chmod +x service_installer/snw_gateway/*.sh
```

Install H2 database service (*NOTE:* at that point in time the installer **HAS** to be run from within its directory)

```
    $ cd service_installer/h2server/
    $ sudo ./install_h2_service.sh
    $ sudo systemctl start h2server.service   # start database
    $ systemctl status h2server.service       # check if started correctly
    $ cd ../..
```

You should be able to connect to the database service with your browser _http://<hostname_or_ip>:8082/_.

Install SnwGateway service (*NOTE:* at that point in time the installer **HAS** to be run from within its directory)

```
    $ cd service_installer/snw_gateway/
    $ sudo ./install_snw_gateway_service.sh
    $ sudo systemctl start snw_gateway.service  # start service
    $ systemctl status snw_gateway.service      # check if service started (may take a while)
    $ tail -f /opt/snw_gateway/logs/application.log  # follow logfile to see if anything went wrong
```

You should be able to connect to the gateway service with your browser _http://<hostname_or_ip>:9000/_. By default every
function (MQTT, Upload, XBEE) and the database is an _in memory_ database - so it will be deleted on reboot. See
[Configuration section](README.md#Configuration) on how to enable functions and persist database.

After first gateway start the database should be empty (for example look under _inventory/list nodes_) so we need
to fill it with initial data. Find database url and username / password in _/opt/snw_gateway/conf/application.conf_.

```
    $ cd /opt/snw_gateway/conf/
    $ runscript_h2.sh ... (TODO) 
    
    update sensormeasurements set soserrorcode = -1, sostransmitted = false;
```


- Run _inital-data.sql_ script by connecting to H2 database, when installing the gateway for the first time.

### Configuration

**TODO**
Change _node_equivalent = "0013A20040C5407F"_ to correct value!

### Updating

Transfer the zip file containing the new version of the gateway to your Raspberry PI.

```
    $ sudo systemctl status snw_gateway.service  ## stop gateway service before updating
    $ sudo /opt/snw_gateway/bin/_gateway.sh <gateway zipfile> 
```

The updater will update the libraries etc. but not the configuration and binary
files. Check _/opt/snw_gateway/bin_new_ and _/opt/snw_gateway/conf_new_ for
the binary and configuration files provided with the new version and update the
old ones if necessary.

**TODO** explain database update (H2) and how to check service installer unit.

## Licenses

This software is distributed under the Apache Software License 2.0.

- [Xbee-Api](https://github.com/andrewrapp/xbee-api) is distributed under GNU General Public License v3.0
- [Play Framework](https://www.playframework.com/) is distributed under the Apache Software License 2.0.

- All additional documentation is distributed under ![CC-4.0-BY-SA](https://licensebuttons.net/l/by-sa/4.0/88x31.png)

Copyright (c) 2011-2017 Interfaculty Department of Geoinformatics, University of
Salzburg (Z_GIS) & Institute of Geological and Nuclear Sciences Limited (GNS Science)
in the SMART Aquifer Characterisation (SAC) programme funded by the New Zealand
Ministry of Business, Innovation and Employment (MBIE)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Acknowledgments

The authors wish to acknowledge the six year funding (07/2011-06/2017) of the
Ministry of Business, Innovation, and Employment (MBIE), New Zealand,
contract number C05X1102, for the [SMART Aquifer characterisation (SAC) programme](http://www.gns.cri.nz/Home/Our-Science/Environment-and-Materials/Groundwater/Research-Programmes/SMART-Aquifer-Characterisation).
