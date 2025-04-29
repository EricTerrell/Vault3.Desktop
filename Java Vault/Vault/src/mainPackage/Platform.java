/*
  Vault 3
  (C) Copyright 2025, Eric Bergman-Terrell
  
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
*/

package mainPackage;

import commonCode.IPlatform;

public class Platform implements IPlatform {
	private static PlatformEnum platform = PlatformEnum.Unknown;
	
	static {
		final String osName = System.getProperty("os.name").toLowerCase();
		
		if (osName.contains("windows")) {
			platform = PlatformEnum.Windows;
		}
		else if (osName.contains("linux")) {
			platform = PlatformEnum.Linux;
		}
		else if (osName.contains("mac os x")) {
			platform = PlatformEnum.MacOSX;
		}
		else {
			Globals.getLogger().severe(String.format("Cannot determine platform. os.name: %s", osName));
		}

		Globals.getLogger().info(String.format("FontList.getPlatform: %s", platform));
	}
	
	@Override
	public PlatformEnum getPlatform() {
		return platform;
	}
}
