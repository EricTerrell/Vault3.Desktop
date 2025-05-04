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

import org.junit.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import commonCode.IPlatform.PlatformEnum;

import fonts.AndroidFont;
import fonts.FontList;
import fonts.IFont;
import fonts.SWTFont;

public class FontTests {
	@BeforeClass
	public static void beforeClass() {
		FontListInitializer.initialize();
	}
	
	@Test
	public void addItemsAndSerializeAndDeserialize() {
		SWTFont font1 = new SWTFont("Ariel", PlatformEnum.Windows, "_font1_1|Lucida Console|20.25|0|WINDOWS|1|-27|0|0|0|400|0|0|0|0|3|2|1|49|Lucida Console");
		AndroidFont font2 = new AndroidFont("sans", 2.4f, 0);
		
		FontList fontList = new FontList();
		
		fontList.add(font1);
		fontList.add(font2);
		
		Assert.assertEquals(2, fontList.size());

		String serializedText = FontList.serialize(fontList);
		
		System.out.printf("Length: %d Text: %s%n", serializedText.length(), serializedText);
		
		FontList roundTripFontList = FontList.deserialize(serializedText);
		Assert.assertNotNull(roundTripFontList);

		IFont retrievedFont = roundTripFontList.getFont();
		Assert.assertEquals(font1.getName(), retrievedFont.getName());
		Assert.assertEquals(font1.getPlatform(), retrievedFont.getPlatform());
		Assert.assertEquals(font1.getData(), ((SWTFont) retrievedFont).getData());
		
		Assert.assertEquals(2, roundTripFontList.size());
	}
	
	@Test
	public void testGetFont() {
		SWTFont font1 = new SWTFont("Ariel", PlatformEnum.Windows, "ARIELFONTDATA");
		SWTFont font2 = new SWTFont("Lucida Console", PlatformEnum.Windows, "_font1_1|Lucida Console|20.25|0|WINDOWS|1|-27|0|0|0|400|0|0|0|0|3|2|1|49|Lucida Console");
		AndroidFont font3 = new AndroidFont("sans", 2.4f, 0);
		
		FontList fontList = new FontList();
		fontList.add(font1);
		fontList.add(font2);
		fontList.add(font3);
		
		Assert.assertEquals(3, fontList.size());
		
		IFont iFont = fontList.getFont();
		Assert.assertEquals("Ariel", iFont.getName());
		
		fontList.add(new SWTFont("Courier New", PlatformEnum.Windows, "COURIERNEWFONTDATA"));
		
		iFont = fontList.getFont();
		Assert.assertEquals("Courier New", iFont.getName());
	}
	
	@Test
	public void testGetNullFont() {
		SWTFont font1 = new SWTFont("DOESNOTEXIST", PlatformEnum.Windows, "DOESNOTEXISTFONTDATA");
		SWTFont font2 = new SWTFont("Lucida Console", PlatformEnum.Windows, "_font1_1|Lucida Console|20.25|0|WINDOWS|1|-27|0|0|0|400|0|0|0|0|3|2|1|49|Lucida Console");
		AndroidFont font3 = new AndroidFont("sans", 2.4f, 0);
		
		FontList fontList = new FontList();
		fontList.add(font1);
		fontList.add(font2);
		fontList.add(font3);
		
		Assert.assertNull(fontList.getFont());
	}
	
	@Test
	public void testAddFont() {
		SWTFont font1 = new SWTFont("Ariel", PlatformEnum.Windows, "ARIELFONTDATA");
		
		FontList fontList = new FontList();
		fontList.add(font1);
		Assert.assertEquals(1, fontList.size());

		SWTFont retrievedFont = (SWTFont) fontList.getFont();
		Assert.assertEquals("Ariel", retrievedFont.getName());
		Assert.assertEquals(PlatformEnum.Windows, retrievedFont.getPlatform());
		Assert.assertEquals("ARIELFONTDATA", retrievedFont.getData());
		
		SWTFont font2 = new SWTFont("Ariel", PlatformEnum.Windows, "ARIELFONTDATA2");
		fontList.add(font2);
		Assert.assertEquals(1, fontList.size());

		// Make sure the new "Ariel" font replaced the old one.
		retrievedFont = (SWTFont) fontList.getFont();
		Assert.assertEquals("Ariel", retrievedFont.getName());
		Assert.assertEquals(PlatformEnum.Windows, retrievedFont.getPlatform());
		Assert.assertEquals("ARIELFONTDATA2", retrievedFont.getData());

		// Make sure the new "Courier New" is added to the list and doesn't replace any other items.
		SWTFont font3 = new SWTFont("Courier New", PlatformEnum.Windows, "COURIERNEWFONTDATA");
		fontList.add(font3);
		Assert.assertEquals(2, fontList.size());
	}
}
