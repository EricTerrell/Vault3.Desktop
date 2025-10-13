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

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * @author Eric Bergman-Terrell
 *
 * Color objects consume system resources and should be disposed. The ColorRegistry
 * class caches every unique Color used, so that system resources are not leaked.
 */
public class ColorRegistry {
	private final Dictionary<String, Color> colors;
	
	/**
	 * @param rgb RGB color value
	 * @return unique key for the specified color value
	 */
	private String getKey(RGB rgb) {
		return String.format("%02X%02X%02X", rgb.red, rgb.green, rgb.blue);
	}
	
	public ColorRegistry() {
		colors = new Hashtable<>();
	}
	
	/**
	 * @param rgb RGB color value
	 * @return a Color object that should not be disposed
	 */
	Color get(RGB rgb) {
		final String key = getKey(rgb);
		
		Color color = colors.get(key);
		
		// If color was previously cached, and was disposed, force a new Color object to be instantiated and cached.
		if (color != null && color.isDisposed()) {
			color = null;
		}
		
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb);
			
			colors.put(key, color);
		}
			
		return color;
	}
	
	/**
	 * @param red red value (0 to 255)
	 * @param green green value (0 to 255)
	 * @param blue blue value (0 to 255)
	 * @return a Color object that should not be disposed
	 */
	Color get(int red, int green, int blue) {
		return get(new RGB(red, green, blue));
	}
}
