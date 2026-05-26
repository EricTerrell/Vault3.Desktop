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

.\ant_build_linux_gtk_x86_64.ps1
.\ant_build_linux_gtk_aarch_64.ps1
.\ant_build_win_aarch_64.ps1
.\ant_build_win_x86_64.ps1

Write-Output ""
Write-Output "***************************************"
Write-Output "Now run Inno Setup (Build/Compile)"
Write-Output "Then run build_windows_setup_zip.ps1"
Write-Output "***************************************"
