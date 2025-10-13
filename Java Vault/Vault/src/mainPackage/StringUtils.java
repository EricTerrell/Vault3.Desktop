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

/**
 * Various String utilities
 * 
 * @author Eric Bergman-Terrell
 *
 */

public class StringUtils {
	/**
	 * Determine if two strings are equal. Either or both strings can be null.
	 * @param string1 possibly null String
	 * @param string2 possibly null String
	 * @return true if the strings are equal, false otherwise.
	 */
	public static boolean equals(String string1, String string2) {
		boolean equals = string1 == null && string2 == null;
		
		if (!equals) {
			equals = string1 != null && string1.equals(string2);
		}
		
		return equals;
	}
	
	public static boolean isURL(String text) {
		final String trimmedText = text.trim().toLowerCase();
		
		return trimmedText.startsWith("http://") || trimmedText.startsWith("https://");
	}

	public static String removeAllWhitespace(String str) {
		return str.replaceAll("\\s+", StringLiterals.EmptyString);
	}
}
