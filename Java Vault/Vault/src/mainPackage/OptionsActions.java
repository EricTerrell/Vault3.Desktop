/*
  Vault 3
  (C) Copyright 2026, Eric Bergman-Terrell
  
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

import org.eclipse.jface.dialogs.IDialogConstants;

/**
 * @author Eric Bergman-Terrell
 *
 */
public class OptionsActions {
	public static class SettingsAction extends UpdatableAction {
		@Override
		public String getDescription() {
			return MessageFormat.format("Configure {0} based on your preferences", StringLiterals.ProgramName);
		}

		public SettingsAction() {
			super("&Settings...", "settings.png");
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void run() {
			SettingsDialog settingsDialog = new SettingsDialog(Globals.getMainApplicationWindow().getShell());

			var originalDarkModeSetting = Globals.getPreferenceStore().getBoolean(PreferenceKeys.UseDarkModeIcons);

			if (settingsDialog.open() == IDialogConstants.OK_ID) {
				Globals.getVaultTextViewer().setWidgetFont();

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

				var newDarkModeSetting = Globals.getPreferenceStore().getBoolean(PreferenceKeys.UseDarkModeIcons);

				if (newDarkModeSetting != originalDarkModeSetting) {
					Globals.getMainApplicationWindow().updateUIForDarkModeChange(originalDarkModeSetting, newDarkModeSetting);
				}
			}
		}
	}
}
