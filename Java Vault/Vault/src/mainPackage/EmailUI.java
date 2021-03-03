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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;

public class EmailUI {
	public static boolean canEmail() {
		List<OutlineItem> selectedItems = Globals.getVaultTreeViewer().getSelectedItems();
		
		return selectedItems.size() > 0;
	}
	
	public static void email() {
		if (canEmail()) {
			String serverAddress = Globals.getPreferenceStore().getString(PreferenceKeys.EmailServerAddress).trim();
			
			if (serverAddress.length() > 0) {
				List<OutlineItem> selectedItems = Globals.getVaultTreeViewer().getSelectedItems();

				ArrayList<String> photoPaths = new ArrayList<>();
				
				StringBuilder body = new StringBuilder();
				
				String newLine = PortabilityUtils.getNewLine();
				
				for (OutlineItem outlineItem : selectedItems) {
					body.append(VaultDocumentExports.getExportText(outlineItem, photoPaths));
					body.append(newLine);
				}
				
				String selectedText = selectedItems.size() == 1 ? Globals.getVaultTextViewer().getTextWidget().getSelectionText() : null;
				
				SendEmailDialog sendEmailDialog = new SendEmailDialog(Globals.getMainApplicationWindow().getShell(), body.toString(), selectedText, photoPaths);
				
				sendEmailDialog.open();
			}
			else {
				String message = "Before sending email you must specify an Email Server Address in the Options / Settings Dialog's Email tab.";
				
				Image icon = Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_VAULT_ICON);
				
				MessageDialog messageDialog = new MessageDialog(Globals.getMainApplicationWindow().getShell(), StringLiterals.ProgramName, icon, message, MessageDialog.ERROR, new String[] { "&OK" }, 0);
				messageDialog.open();
			}
		}
	}
	
	public static boolean canEmailFeedback() {
		return true;
	}
	
	public static void emailFeedback() {
		if (canEmailFeedback()) {
			String serverAddress = Globals.getPreferenceStore().getString(PreferenceKeys.EmailServerAddress).trim();
			
			if (serverAddress.length() > 0) {
				SendEmailFeedbackDialog sendEmailFeedbackDialog = new SendEmailFeedbackDialog(Globals.getMainApplicationWindow().getShell());
				
				sendEmailFeedbackDialog.open();
			}
			else {
				String message = "Before sending email you must specify an Email Server Address in the Options / Settings Dialog's Email tab.";
				
				Image icon = Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_VAULT_ICON);
				
				MessageDialog messageDialog = new MessageDialog(Globals.getMainApplicationWindow().getShell(), StringLiterals.ProgramName, icon, message, MessageDialog.ERROR, new String[] { "&OK" }, 0);
				messageDialog.open();
			}
		}
	}
}
