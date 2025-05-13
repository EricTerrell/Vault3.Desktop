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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Eric Bergman-Terrell
 * 
 * Use CodePointString when you need to manipulate arbitrary language text on a character by character basis.
 * 
 * See http://java.sun.com/mailers/techtips/corejava/2006/tt0822.html
 */
public class CodePointString {
	private List<Integer> codePoints;
	
	/**
	 * default Constructor
	 */
	public CodePointString() {
		codePoints = new ArrayList<>();
	}
	
	/**
	 * Create a CodePointString from the given string.
	 * @param text
	 */
	public CodePointString(String text) {
		final int initialLength = text != null ? text.length() : 0;
		
		codePoints = new ArrayList<>(initialLength);
		
		if (text != null && !text.isEmpty()) {
			final int codePointCount = text.codePointCount(0, text.length());
			
			for (int i = 0; i < codePointCount; i++) {
				codePoints.add(text.codePointAt(i));
			}
		}
	}
	
	/**
	 * Create a new CodePointString from an existing CodePointString.
	 * 
	 * @param text CodePointString that will be cloned.
	 */
	public CodePointString(CodePointString text) {
		codePoints = new ArrayList<>(text.codePoints.size());

		codePoints.addAll(text.codePoints.stream().map(Integer::intValue).collect(Collectors.toList()));
	}
	
	/**
	 * Create a new CodePointString from a code point
	 * 
	 * @param codePoint code point
	 */
	public CodePointString(int codePoint) {
		codePoints = new ArrayList<>(1);
		
		codePoints.add(codePoint);
	}
	
	/**
	 * Return a new CodePointString with the code point added to the end.
	 * 
	 * @param codePoint code point to append to the string.
	 * @return CodePointString with the code point appended to the end
	 */
	public CodePointString append(int codePoint) {
		final CodePointString appendedString = new CodePointString(this);
		
		appendedString.codePoints.add(codePoint);
		
		return appendedString;
	}
	
	/**
	 * Return the code point at the specified index.
	 * 
	 * @param index
	 * @return code point
	 */
	public int codePointAt(int index) {
		return codePoints.get(index);
	}
	
	/**
	 * Return the number of characters, not chars, in the string.
	 * 
	 * @return number of characters
	 */
	public int length() {
		return codePoints.size();
	}

	
	/**
	 * Return an array of code points for the string.
	 * 
	 * @return array of code points
	 */
	private int[] toIntArray() {
		final int[] intArray = new int[codePoints.size()];
		
		for (int i = 0; i < intArray.length; i++) {
			intArray[i] = codePoints.get(i);
		}
		
		return intArray;
	}

	/**
	 * Set the code point at the specified index to the specified value.
	 * 
	 * @param index
	 * @param codePoint
	 */
	public void setAt(int index, int codePoint) {
		codePoints.set(index, codePoint);
	}
	
	public void insertAt(int index, char ch) {
		codePoints.add(index, (int) ch);
	}
	
	/**
	 * Remove all instances of the specified character.
	 * 
	 * @param ch character to remove
	 */
	public void remove(char ch) {
		final Integer codePoint = (int) ch;
		
		for (int i = codePoints.size() - 1; i >= 0; i--) {
			if (codePoints.get(i).equals(codePoint)) {
				codePoints.remove(i);
			}
		}
	}
	
	/**
	 * Set the string to an empty string.
	 */
	public void clear() {
		codePoints = new ArrayList<>();
	}
	
	@Override
	public String toString() {
		final int[] intArray = toIntArray();
		
		return new String(intArray, 0, intArray.length);
	}
}
