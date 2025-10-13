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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * @author Eric Bergman-Terrell
 *
 */
public class MRUFileActions {
	public static class MRUFileAction extends Action {
		private final String filePath;

		public String getFilePath() {
			return filePath;
		}
		
		public MRUFileAction(String menuText, String filePath) {
			super(menuText);
			
			this.filePath = filePath;
			setDescription(MessageFormat.format("Open {0}", filePath));
			
			setEnabled(true);
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void run() {
			if (!Globals.getVaultDocument().getFilePath().equals(filePath)) {
				try {
					final boolean cancelled = Globals.getMainApplicationWindow().saveCurrentDocument();
					
					if (!cancelled) {
						VaultDocumentIO.fileOpen(Globals.getMainApplicationWindow().getShell(), filePath);
						Globals.getMainApplicationWindow().getSearchUI().reset();
						Globals.getVaultTreeViewer().selectFirstItem();
						Globals.getMainApplicationWindow().notifyDocumentLoadUnloadListeners();
					}
				}
				catch (Throwable ex) {
					// Remove file from MRU list and File menu.
					Globals.getMRUFiles().remove(filePath);
					Globals.getMainApplicationWindow().createFileMenuItems();

					boolean processedException = DatabaseVersionTooHigh.displayMessaging(ex, filePath);

					if (!processedException) {
						final String message = MessageFormat.format("Cannot open file {2}.{0}{0}{1}",
								PortabilityUtils.getNewLine(),  ex.getMessage(), filePath);
						final MessageDialog messageDialog =
								new MessageDialog(
										Globals.getMainApplicationWindow().getShell(),
										StringLiterals.ProgramName,
										Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_VAULT_ICON),
										message,
										MessageDialog.ERROR,
										new String[] { "&OK" },
										0);

						messageDialog.open();
					}

					ex.printStackTrace();
				}
			}
		}
	}
}
