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

import java.io.File;
import java.sql.Connection;
import java.text.MessageFormat;
import javax.crypto.Cipher;

import commonCode.Base64Coder;
import org.eclipse.jface.dialogs.IDialogConstants;
import commonCode.VaultDocumentVersion;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * @author Eric Bergman-Terrell
 *
 */
public class CryptoGUIUtils {
	public static byte[] promptUserForPasswordAndDecrypt(String filePath, byte[] cipherText, byte[] salt, byte[] iv, StringWrapper password, VaultDocumentVersion vaultDocumentVersion) throws Exception {
		boolean decrypted = false;

		byte[] plainText = null;
		
		do 
		{
			final PasswordPromptDialog passwordPromptDialog = new PasswordPromptDialog(null, new File(filePath).getName());

			if (passwordPromptDialog.open() == IDialogConstants.OK_ID) {
				Globals.getLogger().info("XML File is encrypted, Decrypting");

				try {
					final Cipher cipher = CryptoUtils.createDecryptionCipher(passwordPromptDialog.getPassword(),
							vaultDocumentVersion, salt, iv);

					plainText = CryptoUtils.decrypt(cipher, cipherText);
					decrypted = true;
					
					password.setValue(passwordPromptDialog.getPassword());
					Globals.getPasswordCache().put(filePath, password.getValue());
				}
				catch (Throwable ex) {
					ex.printStackTrace();
				}
				
				Globals.getLogger().info("Finished decrypting");
			}
			else {
				throw new Exception("User cancelled decryption.");
			}
		} while (!decrypted);
		
		return plainText;
	}

	public static void promptUserForPasswordAndDecrypt(Connection db, String filePath, StringWrapper password, VaultDocumentVersion vaultDocumentVersion) throws Exception {
		boolean decrypted = false;

		final String documentName = new File(filePath).getName();

		do
		{
			final PasswordPromptDialog passwordPromptDialog =
					new PasswordPromptDialog(null, documentName);

			if (passwordPromptDialog.open() == IDialogConstants.OK_ID) {
				try {
					final String cipherText = VaultDocument.getVaultDocumentInfo(db,StringLiterals.CipherText);
					final String saltString = VaultDocument.getVaultDocumentInfo(db,StringLiterals.Salt);
					final String ivString = VaultDocument.getVaultDocumentInfo(db,StringLiterals.IV);

					byte[] salt = null, iv = null;

					if (saltString != null && ivString != null) {
						salt = Base64Coder.decode(saltString);
						iv = Base64Coder.decode(ivString);
					}

					final Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(
							passwordPromptDialog.getPassword(), vaultDocumentVersion, salt, iv);

					CryptoUtils.decryptString(decryptionCipher, cipherText);
					decrypted = true;
					
					password.setValue(passwordPromptDialog.getPassword());
					Globals.getPasswordCache().put(filePath, password.getValue());
				}
				catch (Throwable ex) {
					ex.printStackTrace();

					if (!decrypted) {
						final String message = MessageFormat.format("Incorrect password specified for {0}",
								documentName);

						final MessageDialog messageDialog =
								new MessageDialog(
										null,
										StringLiterals.ProgramName,
										Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_VAULT_ICON),
										message,
										MessageDialog.ERROR,
										new String[] { "&OK" },
										0);

						messageDialog.open();
					}
				}
				
				Globals.getLogger().info("Finished decrypting");
			}
			else {
				throw new Exception("User cancelled decryption.");
			}
		} while (!decrypted);
	}
}
