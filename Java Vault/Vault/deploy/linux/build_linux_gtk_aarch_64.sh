#!/bin/bash

#  Vault 3
#  (C) Copyright 2026, Eric Bergman-Terrell
#
#  This file is part of Vault 3.
#
#  Vault 3 is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  Vault 3 is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with Vault 3.  If not, see <http://www.gnu.org/licenses/>.

pushd .
cd ../..

# Do not run tests - tests will attempt to load platform-specific code which will fail in Windows
mvn clean package -DskipTests=true -PLinux-aarch_64

cd "deploy/linux"

ant -buildfile "./ant_build_scripts/ant_build_linux_gtk_aarch_64.xml"

cd "/home/eric/Documents/software development/Vault3-build"
chmod +x "Vault3LinuxGTK_aarch_64/custom-runtime/bin/java"

tar -czvf "Vault3LinuxGTK_aarch_64.tar.gz" "Vault3LinuxGTK_aarch_64"

popd