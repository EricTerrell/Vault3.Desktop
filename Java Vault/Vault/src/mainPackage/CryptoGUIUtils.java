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

/**
 * 
 */
package mainPackage;

import java.io.File;
import javax.crypto.Cipher;
import org.eclipse.jface.dialogs.IDialogConstants;
import commonCode.VaultDocumentVersion;

/**
 * @author Eric Bergman-Terrell
 *
 */
public class CryptoGUIUtils {
	public static byte[] promptUserForPasswordAndDecrypt(String filePath, byte[] cipherText, StringWrapper password, VaultDocumentVersion vaultDocumentVersion) throws Exception {
		boolean decrypted = false;

		byte[] plainText = null;
		
		do 
		{
			PasswordPromptDialog passwordPromptDialog = new PasswordPromptDialog(null, new File(filePath).getName());

			if (passwordPromptDialog.open() == IDialogConstants.OK_ID) {
				Globals.getLogger().info("XML File is encrypted, Decrypting");

				try {
					plainText = CryptoUtils.decrypt(passwordPromptDialog.getPassword(), cipherText, vaultDocumentVersion);
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

	public static void promptUserForPasswordAndDecrypt(String filePath, String cipherText, StringWrapper password) throws Exception {
		boolean decrypted = false;

		do 
		{
			PasswordPromptDialog passwordPromptDialog = new PasswordPromptDialog(null, new File(filePath).getName());

			if (passwordPromptDialog.open() == IDialogConstants.OK_ID) {

				try {
					Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(passwordPromptDialog.getPassword());

					CryptoUtils.decryptString(decryptionCipher, cipherText);
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
	}
}
