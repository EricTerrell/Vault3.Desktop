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

import java.io.UnsupportedEncodingException;
import java.util.Random;
import org.junit.Assert;
import commonCode.Base64Coder;
import org.junit.Test;

public class Base64TestCase {
	@Test
	public void englishTextRoundTrip() throws UnsupportedEncodingException {
		final String originalText = "A man, a plan, a canal, Panama!";
		
		final String encodedText = Base64Coder.i18nEncode(originalText);
		final String decodedText = Base64Coder.i18nDecode(encodedText);
		
		Assert.assertEquals(originalText, decodedText);
	}

	/* Base64Coder.i18nEncode and Base64Coder.i18nDecode use a Unicode encoding so non-English text can round-trip. */
	
	@Test
	public void chineseTextRoundTrip() throws UnsupportedEncodingException {
		final String originalText = "å†¯æ­£è™Žæ˜ŸæœŸå››ï¼ˆ1æœˆ21æ—¥ï¼‰å¯¹BBCä¸­æ–‡éƒ¨è¯´ï¼Œç‰§é‡Žè¡¨ç¤ºï¼Œä»–æ˜¯å…³å¿ƒå†¯æ­£è™Žçš„äººæ�ƒé—®é¢˜è€Œæ�¥ï¼Œå¾�è¯¢å†¯æ­£è™Žæœ‰ä½•éœ€æ±‚ã€‚";
		
		final String encodedText = Base64Coder.i18nEncode(originalText);
		final String decodedText = Base64Coder.i18nDecode(encodedText);
		
		Assert.assertEquals(originalText, decodedText);
	}

	@Test
	public void arabicTextRoundTrip() throws UnsupportedEncodingException {
		final String originalText = "Ù�ÙŠ Ù„ÙˆØ¨Ø§Ù†ØºÙˆ Ù�ÙŠ Ù…Ù†Ø§Ù�Ø³Ø§Øª Ø§Ù„Ù…Ø¬Ù…ÙˆØ¹Ø© Ø§Ù„Ø±Ø§Ø¨Ø¹Ø© Ù„Ù†Ù‡Ø§Ø¦ÙŠØ§Øª ÙƒØ£Ø³ Ø§Ù�Ø±ÙŠÙ‚ÙŠØ§ Ù„Ù„Ø§Ù…Ù… Ù„ÙƒØ±Ø© Ø§Ù„Ù‚Ø¯Ù… Ø§Ù„ØªÙŠ ØªØ³ØªØ¶ÙŠÙ�Ù‡Ø§ Ø§Ù†ØºÙˆÙ„Ø§.";
		
		final String encodedText = Base64Coder.i18nEncode(originalText);
		final String decodedText = Base64Coder.i18nDecode(encodedText);
		
		Assert.assertEquals(originalText, decodedText);
	}

	@Test
	public void emptyTextRoundTrip() throws UnsupportedEncodingException {
		final String originalText = "";
		
		final String encodedText = Base64Coder.i18nEncode(originalText);
		final String decodedText = Base64Coder.i18nDecode(encodedText);
		
		Assert.assertEquals(originalText, decodedText);
	}

	@Test
	public void nullTextRoundTrip() throws UnsupportedEncodingException {
		final String encodedText = Base64Coder.i18nEncode(null);
		final String decodedText = Base64Coder.i18nDecode(encodedText);

		Assert.assertNull(decodedText);
	}

	private String getRandomString(int maxLength) {
		final Random random = new Random();
		
		final int length = random.nextInt(maxLength + 1);
		
		StringBuilder stringBuilder = new StringBuilder(length);
		
		for (int i = 0; i < length; i++) {
			char ch = (char) random.nextInt(256);
			
			stringBuilder.append(ch);
		}
		
		return stringBuilder.toString();
	}
	
	@Test
	public void randomStringRoundTrip() throws UnsupportedEncodingException {
		final String originalText = getRandomString(1000000);
		
		final String encodedText = Base64Coder.i18nEncode(originalText);
		final String decodedText = Base64Coder.i18nDecode(encodedText);
		
		Assert.assertEquals(originalText, decodedText);
	}
}
