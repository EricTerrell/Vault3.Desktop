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

public class AndroidFont implements IFont, Serializable {
	private static final long serialVersionUID = -1007908993868285121L;

	private String name;
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public PlatformEnum getPlatform() {
		return PlatformEnum.Android;
	}

	private int style;
	
	public int getStyle() {
		return style;
	}
	
	public void setStyle(int style) {
		this.style = style;
	}
	
	private float sizeInPoints;
	
	public float getSizeInPoints() {
		return sizeInPoints;
	}

	public void setSizeInPoints(float sizeInPoints) {
		this.sizeInPoints = sizeInPoints;
	}

	public AndroidFont(String name, float sizeInPoints, int effects) {
		this.name = name;
		this.sizeInPoints = sizeInPoints;
		this.style = effects;
	}
}
