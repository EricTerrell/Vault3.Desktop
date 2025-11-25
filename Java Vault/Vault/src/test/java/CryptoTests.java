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

package test.java;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

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
public class CryptoTests extends BaseTests {
	private final static String PASSWORD = "PASSWORD1234";

    private final static byte[] NONRANDOM_SALT = new byte[] {
            (byte) 0x45, (byte) 0x22, (byte) 0x00, (byte) 0xff, (byte) 0x11, (byte) 0xfa, (byte) 0x81, (byte) 0x88,
            (byte) 0x04, (byte) 0x89, (byte) 0x99, (byte) 0x11, (byte) 0x07, (byte) 0x14, (byte) 0xcc, (byte) 0xdd
    };

    private final static byte[] NONRANDOM_IV = new byte[] {
            (byte) 0x78, (byte) 0x90, (byte) 0x43, (byte) 0x42, (byte) 0x11, (byte) 0x09, (byte) 0x87, (byte) 0x82,
            (byte) 0x73, (byte) 0x19, (byte) 0x34, (byte) 0x17, (byte) 0x32, (byte) 0x32, (byte) 0xdd, (byte) 0xff
    };

    private final static byte[] PLAINTEXT = new byte[] {
            (byte) 0xaa, (byte) 0xbb, (byte) 0xdd, (byte) 0xcc, (byte) 0x11, (byte) 0x00, (byte) 0x30, (byte) 0x03,
            (byte) 0x9a, (byte) 0xa0, (byte) 0xdd, (byte) 0x05, (byte) 0x05, (byte) 0x55, (byte) 0x77, (byte) 0x72,
            (byte) 0x79, (byte) 0x29, (byte) 0x33, (byte) 0x01, (byte) 0x88, (byte) 0x99, (byte) 0xf9, (byte) 0x99
    };

    private final static byte[] CIPHERTEXT = new byte[] {
            (byte)  45, (byte) 191, (byte) 169, (byte) 114, (byte) 204, (byte) 128, (byte) 152, (byte) 135, (byte)  53,
            (byte)  32, (byte) 254, (byte) 163, (byte) 247, (byte)  44, (byte)  19, (byte)  71, (byte)  88, (byte) 119,
            (byte)  33, (byte) 135, (byte) 180, (byte)  63, (byte)  25, (byte) 151, (byte) 108, (byte) 143, (byte) 192,
            (byte)  37, (byte) 145, (byte) 164, (byte) 147, (byte)  5
    };

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

    @Test
    public void testInteropWithOtherPlatformsEncrypt() throws InvalidAlgorithmParameterException, NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {
        final Cipher encryptionCipher = CryptoUtils.createEncryptionCipher(PASSWORD,
                VaultDocumentVersion.getLatestVaultDocumentVersion(), NONRANDOM_SALT, NONRANDOM_IV);

        final byte[] cipherText = CryptoUtils.encrypt(encryptionCipher, PLAINTEXT);

        Assert.assertArrayEquals(CIPHERTEXT, cipherText);
    }

    @Test
    public void testInteropWithOtherPlatformsDecrypt() throws IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeySpecException, InvalidKeyException {
        final Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(PASSWORD,
                VaultDocumentVersion.getLatestVaultDocumentVersion(),
                NONRANDOM_SALT, NONRANDOM_IV);

        final byte[] plainText = CryptoUtils.decrypt(decryptionCipher, CIPHERTEXT);

        Assert.assertArrayEquals(PLAINTEXT, plainText);
    }

    @Test
    public void TestEncryptDecryptStringRoundTrip() throws InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        final Cipher encryptionCipher = CryptoUtils.createEncryptionCipher(PASSWORD,
                VaultDocumentVersion.getLatestVaultDocumentVersion(), NONRANDOM_SALT, NONRANDOM_IV);

        final Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(PASSWORD,
                VaultDocumentVersion.getLatestVaultDocumentVersion(),
                NONRANDOM_SALT, NONRANDOM_IV);

        String[][] plainTexts = new String[][]
        {
                {
                    "Eric R. Terrell is my name",
                    "Ni91Io9MCfPLxKqAC+Lp4RrQ6uu8/+VJ3WA62wm2QlU="
                },
                {
                    "床前明月光，\n\n疑是地上霜。\n\n举头望明月，\n\n低头思故乡",
                    "94TwqoYgs6M8vjJptNsoXlaC7uQbkIkWPc4Krfr2R0LcMNCC2Zs301eDo4HXqk0xD2FH/QxfCqyXdFqSkcE9jNvZXMr9iVweGmUItBu5zh8="
                },
                {
                    "وأعرف أن الطريق إلى المستحيل طويـل",
                    "KFv6Typ8vP4kARwSBAZiQpcbSbvLUYQkuEHkgUIjkmgFPt/A3d2jy7t57kfXjltSLKM9dTdmwxzfLbF3KfL84g=="
                }
        };

        Arrays.stream(plainTexts).forEach(plainText -> {
            try {
                String cipherText = CryptoUtils.encryptString(encryptionCipher, plainText[0]);

                // Verify the ciphertext to ensure that the encoding will work on other platforms.
                Assert.assertEquals(plainText[1], cipherText);

                String decryptedText = CryptoUtils.decryptString(decryptionCipher, cipherText);

                Assert.assertEquals(plainText[0], decryptedText);
            } catch (Exception ex) {
                Assert.fail();
            }
        });
    }
}
