/*
  Vault 3
  (C) Copyright 2021, Eric Bergman-Terrell
  
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

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import mainPackage.StringLiterals;
import org.junit.Assert;
import mainPackage.CryptoUtils;

import org.junit.Test;

/**
 * @author Eric Bergman-Terrell
 *
 * Tests for the CryptoUtils class. 
 */
public class CryptoTests {
	@Test
	public void roundTrip() throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		final String plainText = "Robert Eric Terrell is my name.";
		final String password = "PASSWORD";

		Cipher encryptionCipher = CryptoUtils.createEncryptionCipher(password);
		Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(password);
		
		for (int i = 0; i < 1000000; i++) {
			String cipherText = CryptoUtils.encryptString(encryptionCipher, plainText);
			
			Assert.assertTrue(!plainText.equals(cipherText));
			
			String decryptedText = CryptoUtils.decryptString(decryptionCipher, cipherText);
			Assert.assertTrue(plainText.equals(decryptedText));
		}
	}
	
	@Test
	public void emptyStringRoundTrip() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
		final String plainText = StringLiterals.EmptyString;
		final String password = "PASSWORD";

		Cipher encryptionCipher = CryptoUtils.createEncryptionCipher(password);
		Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(password);
		
		String cipherText = CryptoUtils.encryptString(encryptionCipher, plainText);
		
		Assert.assertTrue(!plainText.equals(cipherText));
		
		String decryptedText = CryptoUtils.decryptString(decryptionCipher, cipherText);
		Assert.assertTrue(plainText.equals(decryptedText));
	}

	@Test
	public void nullStringRoundTrip() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
		final String plainText = null;
		final String password = "PASSWORD";

		Cipher encryptionCipher = CryptoUtils.createEncryptionCipher(password);
		Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(password);
		
		String cipherText = CryptoUtils.encryptString(encryptionCipher, plainText);
		
		String decryptedText = CryptoUtils.decryptString(decryptionCipher, cipherText);
		Assert.assertTrue(decryptedText.equals(StringLiterals.EmptyString));
	}
}
