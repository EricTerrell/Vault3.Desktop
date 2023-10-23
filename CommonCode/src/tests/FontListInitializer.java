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

package tests;

import java.util.HashMap;

import commonCode.IPlatform;
import commonCode.IPlatform.PlatformEnum;

import fonts.FontList;
import fonts.IFont;
import fonts.SWTFont;

public class FontListInitializer {
	public static void initialize() {
		HashMap<String, IFont> availableFonts = new HashMap<>();
		
		SWTFont swtFont = new SWTFont("Ariel", IPlatform.PlatformEnum.Windows, "ARIELFONTDATA");
		availableFonts.put(swtFont.getName(), swtFont);
			
		swtFont = new SWTFont("Courier New", IPlatform.PlatformEnum.Windows, "COURIERNEWFONTDATA");
		availableFonts.put(swtFont.getName(), swtFont);
			
		FontList.setAvailableFonts(availableFonts);
		FontList.setPlatform(PlatformEnum.Windows);
	}
}
