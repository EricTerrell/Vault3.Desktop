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
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * @author Eric Bergman-Terrell
 *
 */
public class OptionsActions {
	public static class SettingsAction extends Action {
		@Override
		public String getDescription() {
			return MessageFormat.format("Configure {0} based on your preferences", StringLiterals.ProgramName);
		}

		public SettingsAction() {
			super("&Settings...", ImageDescriptor.createFromImage(new Image(Display.getCurrent(), MainApplicationWindow.class.getResourceAsStream("/resources/settings.png"))));
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void run() {
			SettingsDialog settingsDialog = new SettingsDialog(Globals.getMainApplicationWindow().getShell());
			
			if (settingsDialog.open() == IDialogConstants.OK_ID) {
				Globals.getVaultTextViewer().setFontAndColor();

				final List<OutlineItem> selectedItems = Globals.getVaultTreeViewer().getSelectedItems();

				// Redraw the current photo in case the photo settings changed.
				if (selectedItems.size() == 1) {
					Globals.getMainApplicationWindow().getPhotoAndTextUI().getPhotoUI().setImages(selectedItems.getFirst().getPhotoPath());
				}
				
				if (!Globals.getPreferenceStore().getBoolean(PreferenceKeys.CachePasswords)) {
					Globals.getPasswordCache().clear();
				}
				
				Globals.getMainApplicationWindow().startAutoSaveTimer();
				Globals.getMainApplicationWindow().startCheckForModificationsTimer();
				
				((FileActions.CopyPictureFileAction)Globals.getMainApplicationWindow().getAction(FileActions.CopyPictureFileAction.class)).setEnabled();
				((FileActions.RenamePictureFileAction)Globals.getMainApplicationWindow().getAction(FileActions.RenamePictureFileAction.class)).setEnabled();
				((FileActions.DeletePictureFileAction)Globals.getMainApplicationWindow().getAction(FileActions.DeletePictureFileAction.class)).setEnabled();
				((FileActions.EditPictureFileAction)Globals.getMainApplicationWindow().getAction(FileActions.EditPictureFileAction.class)).setEnabled();
			}
		}
	}
}
