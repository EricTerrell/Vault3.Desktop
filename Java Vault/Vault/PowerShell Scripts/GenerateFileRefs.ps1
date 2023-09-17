<#
  Vault 3
  (C) Copyright 2023, Eric Bergman-Terrell

  This file is part of Vault 3.

  Vault 3 is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Vault 3 is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with Vault 3.  If not, see <http://www.gnu.org/licenses/>.
#>

# Generate file references for ant build scripts

cd 'C:\Eclipse 4.29'

$platforms = ("linux-gtk-x86_64", "linux-x86_64"), ("macosx-cocoa-aarch64", "cocoa-macosx-aarch64"), ("macosx-cocoa-x86_64", "cocoa-macosx-x86_64"), ("win32-x86_64", "win32-win32-x86_64");

foreach ($platform in $platforms)
{
    Write-Output ""
    Write-Output $platform[0]
    Write-Output ""

    $files = "org.eclipse.core.commands", "org.eclipse.core.runtime", "org.eclipse.equinox.common", "org.eclipse.jface.text", "org.eclipse.jface", "org.eclipse.osgi", "org.eclipse.text";

    Get-ChildItem -Verbose:$false -File -Path . -Recurse -Include "swt.jar" | Select FullName | findstr $platform[1]

    foreach ($file in $files)
    {
        $file_pattern = $file + "_*.jar";

        Get-ChildItem -Verbose:$false -File -Path . -Recurse -Include $file_pattern | Select FullName | findstr $platform[0] | findstr "eclipse-platform-"
    }
}
