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

package mainPackage;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import javax.crypto.Cipher;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.swt.widgets.Shell;

import commonCode.VaultDocumentVersion;
import commonCode.VaultException;

public class VaultDocumentXML {
	/**
	 * Read the specified Vault 3 file. Prompt the user for the password and decrypt the file if necessary.
	 * @param shell shell
	 * @param filePath path of Vault 3 file
	 * @param password the password entered by the user
	 * @return an OutlineItem containing the entire Vault 3 file's contents
	 * @throws Exception
	 */
	public static OutlineItem parseVault3File(Shell shell, String filePath, StringWrapper password) throws Exception {
		OutlineItem outlineItem = null;
		
		Globals.getLogger().info("Starting SAX parsing");
		
		try {
			Globals.setBusyCursor();
			
			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
			SAXParser saxParser = saxParserFactory.newSAXParser();
		
			NativeDefaultHandler nativeDefaultHandler = new NativeDefaultHandler();

			// If the first argument to parse is a filename with embedded spaces, an exception will be thrown. The solution is to
			// use a FileInputStream instead of a file path.
			try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
				saxParser.parse(fileInputStream, nativeDefaultHandler);
			}

			Globals.getLogger().info(String.format("Finished SAX parsing Pass 1. Parsed %s Version: %d.%d", 
					filePath, nativeDefaultHandler.getMajorVersion(), nativeDefaultHandler.getMinorVersion()));
			
			final VaultDocumentVersion vaultDocumentVersion = new VaultDocumentVersion(nativeDefaultHandler.getMajorVersion(), nativeDefaultHandler.getMinorVersion());
			VaultDocumentVersion codeVaultDocumentVersion = VaultDocumentVersion.getLatestVaultDocumentVersion();
			
			if (vaultDocumentVersion.compareTo(codeVaultDocumentVersion) > 0) {
				throw new VaultException("Database version is too high", VaultException.ExceptionCode.DatabaseVersionTooHigh);
			}
			
			if (nativeDefaultHandler.getIsEncrypted()) {
				final Cipher cipher = CryptoUtils.createDecryptionCipher(password.getValue(), vaultDocumentVersion);

				byte[] plainText = null;
				byte[] cipherText = nativeDefaultHandler.getCipherText();
				
				boolean decrypted = false;
				
				if (password != null) {
					try {
						plainText = CryptoUtils.decrypt(cipher, cipherText);
						decrypted = true;
					}
					catch (Throwable ex) {
						Globals.getPasswordCache().remove(filePath);
						ex.printStackTrace();
					}
				}

				if (!decrypted) {
					plainText = CryptoGUIUtils.promptUserForPasswordAndDecrypt(filePath, cipherText, password, vaultDocumentVersion);
				}
				
				final String unicodeCharset = "UTF-8";
				
				Charset charSet = Charset.forName(unicodeCharset);
				CharsetDecoder charsetDecoder = charSet.newDecoder();

				ByteBuffer input = ByteBuffer.wrap(plainText);
				CharBuffer decodedBuffer = charsetDecoder.decode(input);
				String clearTextString = decodedBuffer.toString();

				Globals.getLogger().info("Starting parse pass 2");

				try (ByteArrayInputStream inputStream = new ByteArrayInputStream(clearTextString.getBytes(unicodeCharset))) {
					saxParser.parse(inputStream, nativeDefaultHandler);
					
					Globals.getLogger().info("finished pass 2");
				}
			}
			
			outlineItem = nativeDefaultHandler.getOutlineItem();
		}
		finally {
			Globals.setPreviousCursor();
		}

		return outlineItem;
	}
}
