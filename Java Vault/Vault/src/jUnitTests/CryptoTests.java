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

package jUnitTests;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import commonCode.VaultDocumentVersion;
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
	public void roundTrip() throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		final String plainText = "Robert Eric Terrell is my name.";
		final String password = "PASSWORD";

		Cipher encryptionCipher = CryptoUtils.createEncryptionCipher(password);
		Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(password, VaultDocumentVersion.getLatestVaultDocumentVersion());
		
		for (int i = 0; i < 1000000; i++) {
			String cipherText = CryptoUtils.encryptString(encryptionCipher, plainText);

			Assert.assertNotEquals(plainText, cipherText);
			
			String decryptedText = CryptoUtils.decryptString(decryptionCipher, cipherText);
			Assert.assertEquals(plainText, decryptedText);
		}
	}
	
	@Test
	public void emptyStringRoundTrip() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		final String plainText = StringLiterals.EmptyString;
		final String password = "PASSWORD";

		Cipher encryptionCipher = CryptoUtils.createEncryptionCipher(password);
		Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(password, VaultDocumentVersion.getLatestVaultDocumentVersion());
		
		String cipherText = CryptoUtils.encryptString(encryptionCipher, plainText);

		Assert.assertNotEquals(plainText, cipherText);
		
		String decryptedText = CryptoUtils.decryptString(decryptionCipher, cipherText);
		Assert.assertEquals(plainText, decryptedText);
	}

	@Test
	public void nullStringRoundTrip() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		final String plainText = null;
		final String password = "PASSWORD";

		Cipher encryptionCipher = CryptoUtils.createEncryptionCipher(password);
		Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(password, VaultDocumentVersion.getLatestVaultDocumentVersion());
		
		String cipherText = CryptoUtils.encryptString(encryptionCipher, plainText);
		
		String decryptedText = CryptoUtils.decryptString(decryptionCipher, cipherText);
		Assert.assertEquals(StringLiterals.EmptyString, decryptedText);
	}
}
