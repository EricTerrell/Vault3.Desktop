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

package mainPackage;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import commonCode.Base64Coder;
import commonCode.VaultDocumentVersion;

/**
 * @author Eric Bergman-Terrell
 *
 */
public class CryptoUtils {
	private static final int minPasswordLength = 4;
	
	public static int getMinPasswordLength() {
		return minPasswordLength;
	}

	private static byte[] getPasswordMessageDigestVaultDocument_1_0(String password) throws NoSuchAlgorithmException {
		return MessageDigest.getInstance("SHA-512").digest(password.getBytes());
	}
	
	private static byte[] getPasswordMessageDigestVaultDocument_1_1(String password) throws NoSuchAlgorithmException {
		return MessageDigest.getInstance("SHA-512").digest(password.getBytes(StandardCharsets.UTF_8));
	}
	
	private static SecretKey createSecretKeyVaultDocumentVersion_1_0(String password) throws NoSuchAlgorithmException {
		byte[] passwordMessageDigest = getPasswordMessageDigestVaultDocument_1_0(password);

        SecureRandom secureRandom = SecureRandom.getInstance(Globals.getPreferenceStore().getString(PreferenceKeys.SecureRandomAlgorithm));
        secureRandom.setSeed(passwordMessageDigest);
		
        KeyGenerator keyGenerator = KeyGenerator.getInstance(Globals.getPreferenceStore().getString(PreferenceKeys.CipherAlgorithm));
        keyGenerator.init(Globals.getPreferenceStore().getInt(PreferenceKeys.KeyLength), secureRandom);
        
        SecretKey secretKey = keyGenerator.generateKey();
        
        Globals.getLogger().info(String.format("Cipher Algorithm: %s, Secret key length: %d bits", keyGenerator.getAlgorithm(), secretKey.getEncoded().length * 8));
        
        return secretKey;
	}
	
	private static SecretKey createSecretKeyVaultDocumentVersion_1_1(String password) throws NoSuchAlgorithmException {
        final int keyLengthBits = Globals.getPreferenceStore().getInt(PreferenceKeys.KeyLength1_1);
        final int keyLengthBytes = keyLengthBits / 8;
        final String keyAlgorithm = Globals.getPreferenceStore().getString(PreferenceKeys.KeyAlgorithm1_1);
        
		final byte[] passwordMessageDigest = getPasswordMessageDigestVaultDocument_1_1(password);
		
        Globals.getLogger().info(
        		String.format("Create Key: key length (bits): %d, key length (bytes): %d, algorithm: %s message digest length: %d", 
        				keyLengthBits, keyLengthBytes, keyAlgorithm, passwordMessageDigest.length));
        
		final List<Byte> passwordBytes = new ArrayList<>();
		
		for (final byte passwordByte : passwordMessageDigest) {
			passwordBytes.add(passwordByte);
		}
		
		while (passwordBytes.size() < keyLengthBytes) {
			passwordBytes.add((byte) 0);
		}
		
		final byte[] passwordByteArray = new byte[keyLengthBytes];
		
		for (int i = 0; i < keyLengthBytes; i++) {
			passwordByteArray[i] = passwordBytes.get(i);
		} 
		
		return new SecretKeySpec(passwordByteArray, keyAlgorithm);
	}

	/**
	 * Encrypt the specified plainText using the specified password. Encryption is always done for the latest document version.
	 * @param cipher encryption cipher
	 * @param plainText plaintext
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeySpecException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws UnsupportedEncodingException 
	 */
	public static byte[] encrypt(Cipher cipher, byte[] plainText) throws IllegalBlockSizeException,
			BadPaddingException {
		Globals.getLogger().info(String.format("encrypt: cipher algorithm: %s", cipher.getAlgorithm()));
		
		byte[] cipherText = cipher.doFinal(plainText);
		
		Globals.getLogger().info("finished encryption");
		
		return cipherText;
	}
	
	public static byte[] decrypt(Cipher cipher, byte[] cipherText) throws IllegalBlockSizeException, BadPaddingException {
		Globals.getLogger().info(String.format("decrypt: cipher algorithm: %s", cipher.getAlgorithm()));

		byte[] plainText = cipher.doFinal(cipherText);

		Globals.getLogger().info("finished decryption");
		
		return plainText;
	}

	public static Cipher createEncryptionCipher(String password) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException {
		final SecretKey secretKey = createSecretKeyVaultDocumentVersion_1_1(password);
		
		final Cipher cipher = Cipher.getInstance(Globals.getPreferenceStore().getString(PreferenceKeys.CipherAlgorithm1_1));

		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		
		return cipher;
	}
	
	public static Cipher createDecryptionCipher(String password, VaultDocumentVersion vaultDocumentVersion) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException {
		SecretKey secretKey;
		Cipher cipher;

		if (vaultDocumentVersion.compareTo(VaultDocumentVersion.VERSION_1_0) == 0) {
			secretKey = createSecretKeyVaultDocumentVersion_1_0(password);

			cipher = Cipher.getInstance(Globals.getPreferenceStore().getString(PreferenceKeys.CipherAlgorithm));
		} else {
			secretKey = createSecretKeyVaultDocumentVersion_1_1(password);

			cipher = Cipher.getInstance(Globals.getPreferenceStore().getString(PreferenceKeys.CipherAlgorithm1_1));
		}

		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		
		return cipher;
	}
	
	public static String encryptString(Cipher cipher, String plainText) throws IllegalBlockSizeException, BadPaddingException {
		// Silently convert null strings to empty strings.
		if (plainText == null) {
			plainText = StringLiterals.EmptyString;
		}
		
		byte[] plainTextBytes = plainText.getBytes(StandardCharsets.UTF_8);
		
		byte[] cipherTextBytes = cipher.doFinal(plainTextBytes);
		
		char[] cipherTextArray = Base64Coder.encode(cipherTextBytes);
		return new String(cipherTextArray);
	}
	
	public static String decryptString(Cipher cipher, String cipherText) throws IllegalBlockSizeException, BadPaddingException {
		byte[] cipherTextBytes = Base64Coder.decode(cipherText);
		
		byte[] plainTextBytes = cipher.doFinal(cipherTextBytes);
		
		return new String(plainTextBytes, StandardCharsets.UTF_8);
	}
}
