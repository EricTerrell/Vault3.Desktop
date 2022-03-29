/*
  Vault 3
  (C) Copyright 2022, Eric Bergman-Terrell
  
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

package fonts;

import java.io.Serializable;

import commonCode.IPlatform.PlatformEnum;

public class SWTFont implements IFont, Serializable {
	private static final long serialVersionUID = -7288831517252400548L;

	private String name;
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	private PlatformEnum platform;
	
	@Override
	public PlatformEnum getPlatform() {
		return platform;
	}
	
	public void setPlatform(PlatformEnum platform) {
		this.platform = platform;
	}
	
	private String data;
	
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public SWTFont(String name, PlatformEnum platform, String data) {
		this.name = name;
		this.platform = platform;
		this.data = data;
	}
}
