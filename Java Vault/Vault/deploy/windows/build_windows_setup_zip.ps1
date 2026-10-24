<#
  Vault 3
  (C) Copyright 2026, Eric Bergman-Terrell
  
  This file is part of EBT Weather.

  EBT Weather is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  EBT Weather is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with EBT Weather.  If not, see <http://www.gnu.org/licenses/>.
#>

Push-Location

Write-Output ""
Write-Output "Creating Windows x64 Setup Zip File"

$ZipPath = "C:\Temp\Vault3\Vault3Win_x86_64.zip"

if (Test-Path $ZipPath)
{
    Remove-Item -Path $ZipPath
}

$BuildPath = "C:\Temp\Vault3\Vault3Win_x86_64"

if (Test-Path $BuildPath)
{
    Remove-Item -Path $BuildPath -Recurse
}

Compress-Archive -Path "..\setup\Output\*" -DestinationPath $ZipPath -Force

Write-Output ""
Write-Output "Finished creating Windows x64 Setup Zip File"

Pop-Location