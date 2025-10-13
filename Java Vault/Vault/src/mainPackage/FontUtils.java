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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;

public class FontUtils {
	public static String fontListToString(FontData[] fontList) {
		final StringBuilder fontString = new StringBuilder();
		
		for (int i = 0; i < fontList.length; i++) {
			fontString.append(fontList[i].toString());
			
			if (i < fontList.length - 1) {
				fontString.append('\0');
			}
		}
		
		return fontString.toString();
	}
	
	public static FontData[] stringToFontList(String fontString) {
		FontData[] fontData = null;
		
		if (fontString != null && !fontString.isEmpty()) {
			final String[] fontStringArray = fontString.split("\0");
			
			if (fontStringArray != null && fontStringArray.length > 0) {
				List<FontData> fontList = new ArrayList<>();
	
				for (String currentFontString : fontStringArray) {
					FontData currentFontData = new FontData(currentFontString);
					fontList.add(currentFontData);
				}
				
				fontData = fontList.toArray(new FontData[0]);
			}
		}
		
		return fontData;
	}
	
	public static Font stringToFont(Device device, String fontString) {
		final FontData[] fontList = stringToFontList(fontString);

		return new Font(device, fontList);
	}
	
	public static Font getDefaultFont(GC gc) {
		final String defaultFontString = Globals.getPreferenceStore().getString(PreferenceKeys.DefaultTextFont);

		return FontUtils.stringToFont(gc.getDevice(), defaultFontString);
	}

	public static String stringToDescription(String fontString) {
		String fontDescription = StringLiterals.EmptyString;
		
		if (fontString != null && !fontString.isEmpty()) {
			final FontData[] fontData = FontUtils.stringToFontList(fontString);
		
			String style = StringLiterals.EmptyString;
			
			if ((fontData[0].getStyle() & SWT.BOLD) != 0) {
				style = "bold";
			}
			
			if ((fontData[0].getStyle() & SWT.ITALIC) != 0) {
				if (style.isEmpty()) {
					style = "italic";
				}
				else {
					style = style + ", italic";
				}
			}
			
			fontDescription = MessageFormat.format("{0} {1} point {2}", fontData[0].getName(), fontData[0].getHeight(), style);
		}
		
		return fontDescription;
	}
}
