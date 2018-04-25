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

- Install H2 Service
- Install SnwGateway service
- Run _inital-data.sql_ script by connecting to H2 database, when installing the gateway for the first time.

### Updating

**TODO** run _update.sh <gateway.zip>_

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
