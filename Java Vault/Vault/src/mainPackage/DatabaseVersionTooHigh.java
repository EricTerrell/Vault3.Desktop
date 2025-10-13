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

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.MessageDialog;
import commonCode.VaultException;

public class DatabaseVersionTooHigh {

	/**
	 * Display error message, and allow user to download update, when database version is higher than the code can handle.
	 * @param ex exception
	 * @return true if the exception indicated a database version too high situation
	 */
	public static boolean displayMessaging(Throwable ex, String filePath) {
		boolean displayedMessaging = false;
		
		if (ex instanceof VaultException) {
			final VaultException vaultException = (VaultException) ex;
			
			if (vaultException.getExceptionCode() == VaultException.ExceptionCode.DatabaseVersionTooHigh) {
				final String message = MessageFormat.format("You must upgrade to the latest version of Vault 3 in order to open {0}.", filePath);
				final MessageDialog messageDialog = new MessageDialog(Globals.getMainApplicationWindow().getShell(), StringLiterals.ProgramName, Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_VAULT_ICON), message, MessageDialog.ERROR, new String[] { "&OK" }, 0);
				messageDialog.open();

				// Allow user to download the latest version of Vault 3.
				try {
					final SoftwareUpdatesDialog softwareUpdatesDialog = new SoftwareUpdatesDialog(Globals.getMainApplicationWindow().getShell());
					softwareUpdatesDialog.open();
				}
				catch (Throwable ex2) {
					ex2.printStackTrace();
				}
				
				displayedMessaging = true;
			}
		}

		return displayedMessaging;
	}
}
