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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import commonCode.Base64Coder;
import commonCode.IPlatform.PlatformEnum;

public class FontList implements Serializable {
	private static final long serialVersionUID = 25542892948026941L;
	
	private List<IFont> fonts;
	
	/**
	 * Fonts available to on the running machine.
	 */
	private static Map<String, IFont> availableFonts = null;
	
	public static void setAvailableFonts(Map<String, IFont> availableFonts) {
		FontList.availableFonts = availableFonts;
	}
	
	/**
	 * OS of the running machine.
	 */
	private static PlatformEnum platform = null;
	
	public static void setPlatform(PlatformEnum platform) {
		FontList.platform = platform;
	}
	
	/**
	 * Returns the OS platform of the running machine.
	 * @return OS of running machine
	 */
	public static PlatformEnum getPlatform() {
		return platform; 
	}
	
	/**
	 * Return the number of fonts in the list
	 * @return size of font list
	 */
	public int size() {
		return fonts != null ? fonts.size() : 0;
	}
	
	/**
	 * Add a font to the end of the list.
	 * @param newFont font
	 */
	public void add(IFont newFont) {
		// Remove the font from the list if it's already there.
		for (int i = fonts.size() - 1; i >= 0; i--) {
			IFont currentFont = fonts.get(i);
			
			if (currentFont.getPlatform() == newFont.getPlatform() && currentFont.getName().equals(newFont.getName())) {
				fonts.remove(i);
			}
		}
		
		// Add the new font to the end of the list.
		fonts.add(newFont);
	}
	
	/**
	 * Retrieve the most-recently used font that is available on the current platform.
	 * @return font or null if not found
	 */
	public IFont getFont() {
		IFont result = null;
		
		if (fonts != null) {
			for (int i = fonts.size() - 1; i >= 0; i--) {
				IFont iFont = fonts.get(i);
				
				if (iFont.getPlatform() == platform) {
					String name = iFont.getName();
					
					boolean fontIsAvailable = availableFonts.get(name) != null;
					
					if (fontIsAvailable) {
						result = iFont;
						break;
					}
				}
			}
		}
		
		return result;
	}

	/**
	 * Serialize the FontList to a base64 encoded string
	 * @param fontList list of fonts
	 * @return base 64 encoded string
	 */
	public static String serialize(FontList fontList) {
		String serializedText = "";
		
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
	
			objectOutputStream.writeObject(fontList);
			objectOutputStream.flush();
			
			byteArrayOutputStream.flush();
			
			byte[] serializedBytes = byteArrayOutputStream.toByteArray();
			
			char[] results = Base64Coder.encode(serializedBytes);
			
			byteArrayOutputStream.close();
			objectOutputStream.close();
			
			serializedText = new String(results);
		}
		catch (Throwable ex) {
			ex.printStackTrace();
		}
		
		return serializedText;
	}

	/**
	 * Deserialize the base64 encoded string to a FontList
	 * @param serializedText base64 encoded string
	 * @return FontList
	 */
	public static FontList deserialize(String serializedText) {
		FontList fontList = null;

		if (serializedText != null && !serializedText.isEmpty()) {
			try
			{
				byte[] serializedBytes = Base64Coder.decode(serializedText);
				
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedBytes);
				ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
				
				fontList = (FontList) objectInputStream.readObject();
			}
			catch (Throwable ex) {
				ex.printStackTrace();
			}
		}
		
		return fontList;
	}
	
	public FontList() {
		fonts = new ArrayList<>(1);
	}
}
