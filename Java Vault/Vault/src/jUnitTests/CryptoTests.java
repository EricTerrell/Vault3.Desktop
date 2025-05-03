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

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

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
	final static String PASSWORD = "PASSWORD";

	@Test
	public void roundTrip() throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException {
		final String plainText = "Robert Eric Terrell is my name.";

		final byte[] salt = CryptoUtils.createSalt();
		final byte[] iv = CryptoUtils.createIV();

		final Cipher encryptionCipher = CryptoUtils.createEncryptionCipher(PASSWORD, VaultDocumentVersion.getLatestVaultDocumentVersion(), salt, iv);
		final Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(PASSWORD, VaultDocumentVersion.getLatestVaultDocumentVersion(), salt, iv);
		
		for (int i = 0; i < 1_000_000; i++) {
			final String cipherText = CryptoUtils.encryptString(encryptionCipher, plainText);

			Assert.assertNotEquals(plainText, cipherText);

			final String decryptedText = CryptoUtils.decryptString(decryptionCipher, cipherText);
			Assert.assertEquals(plainText, decryptedText);
		}
	}
	
	@Test
	public void emptyStringRoundTrip() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException {
		final String plainText = StringLiterals.EmptyString;

		final byte[] salt = CryptoUtils.createSalt();
		final byte[] iv = CryptoUtils.createIV();

		final Cipher encryptionCipher = CryptoUtils.createEncryptionCipher(PASSWORD, VaultDocumentVersion.getLatestVaultDocumentVersion(), salt, iv);
		final Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(PASSWORD, VaultDocumentVersion.getLatestVaultDocumentVersion(), salt, iv);

		final String cipherText = CryptoUtils.encryptString(encryptionCipher, plainText);

		Assert.assertNotEquals(plainText, cipherText);

		final String decryptedText = CryptoUtils.decryptString(decryptionCipher, cipherText);
		Assert.assertEquals(plainText, decryptedText);
	}

	@Test
	public void nullStringRoundTrip() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException {
		final String plainText = null;

		final byte[] salt = CryptoUtils.createSalt();
		final byte[] iv = CryptoUtils.createIV();

		final Cipher encryptionCipher = CryptoUtils.createEncryptionCipher(PASSWORD,
				VaultDocumentVersion.getLatestVaultDocumentVersion(), salt, iv);
		final Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(PASSWORD,
				VaultDocumentVersion.getLatestVaultDocumentVersion(), salt, iv);

		final String cipherText = CryptoUtils.encryptString(encryptionCipher, plainText);

		final String decryptedText = CryptoUtils.decryptString(decryptionCipher, cipherText);
		Assert.assertEquals(StringLiterals.EmptyString, decryptedText);
	}
}
