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

package jUnitTests;

import mainPackage.StringLiterals;
import org.junit.Assert;
import mainPackage.CodePointString;
import org.junit.Test;

/**
 * @author Eric Bergman-Terrell
 *
 * Tests for the CodePointString class. 
 * 
 * See http://java.sun.com/mailers/techtips/corejava/2006/tt0822.html
 */
public class CodePointStringTests {
	@Test
	public void charsVersusCharacters() {
		String testString = "abcd\u5B66\uD800\uDF30"; // 7 chars, 6 characters.

		int charCount = testString.length();
		int characterCount = testString.codePointCount(0, charCount);
		
		Assert.assertEquals(7, charCount);
		Assert.assertEquals(6, characterCount);
		
		CodePointString codePointString = new CodePointString(testString);
		
		Assert.assertEquals(characterCount, codePointString.length());
	}
	
	@Test
	public void emptyString() {
		CodePointString codePointString = new CodePointString();
		Assert.assertEquals(0, codePointString.length());
		
		codePointString = new CodePointString(StringLiterals.EmptyString);
		Assert.assertEquals(0, codePointString.length());
		
		codePointString = new CodePointString(new CodePointString());
		Assert.assertEquals(0, codePointString.length());
		
		String nullString = null;
		codePointString = new CodePointString(nullString);
		Assert.assertEquals(0, codePointString.length());
	}

	@Test
	public void clear() {
		CodePointString codePointString = new CodePointString();
		Assert.assertEquals(0, codePointString.length());
		
		codePointString.clear();
		Assert.assertEquals(0, codePointString.length());

		codePointString = new CodePointString("Hello");
		Assert.assertEquals(5, codePointString.length());
		
		codePointString.clear();
		Assert.assertEquals(0, codePointString.length());
	}
	
	@Test
	public void append() {
		CodePointString original = new CodePointString("Hello");
		
		CodePointString appended = original.append('!');
		
		Assert.assertEquals("Hello!", appended.toString());
		Assert.assertEquals("Hello", original.toString());
	}
	
	@Test
	public void testToString() {
		CodePointString empty = new CodePointString();
		Assert.assertEquals(StringLiterals.EmptyString, empty.toString());
		
		CodePointString hello = new CodePointString("Hello");
		Assert.assertEquals("Hello", hello.toString());
	}
	
	@Test
	public void length() {
		Assert.assertEquals(0, new CodePointString().length());
		Assert.assertEquals(5, new CodePointString("Hello").length());
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void setAtBadIndex1() {
		CodePointString codePointString = new CodePointString("Hello");
		codePointString.setAt(-1, '!');
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void setAtBadIndex2() {
		CodePointString codePointString = new CodePointString("Hello");
		codePointString.setAt(6, '!');
	}
	
	@Test public void setAt() {
		CodePointString codePointString = new CodePointString("Hello");

		codePointString.setAt(0, 'y');
		codePointString.setAt(4, '!');
		
		Assert.assertEquals("yell!", codePointString.toString());
	}
	
	@Test
	public void codePointAt() {
		CodePointString codePointString = new CodePointString("Hello");
		Assert.assertEquals('H', codePointString.codePointAt(0));
		Assert.assertEquals('o', codePointString.codePointAt(4));
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void codePointAtBadIndex1() {
		CodePointString codePointString = new CodePointString("Hello");
		codePointString.codePointAt(-1);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void codePointAtBadIndex2() {
		CodePointString codePointString = new CodePointString("Hello");
		codePointString.codePointAt(6);
	}
	

}
