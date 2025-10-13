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
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

/**
 * @author Eric Bergman-Terrell
 *
 */
public class PhotoUIActions {
	private static boolean photoExists() {
		final String imagePath = PhotoUI.getCurrentImagePath();
		
		return imagePath != null && !imagePath.trim().isEmpty();
	}
	
	public static class CopyPictureToClipboardAction extends Action implements IPhotoListener {
		@Override
		public String getDescription() {
			return "Copy the current photo to the clipboard";
		}

		public CopyPictureToClipboardAction() {
			super("&Copy Photo to Clipboard");
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void run() {
			try {
				Globals.getMainApplicationWindow().getPhotoAndTextUI().getPhotoUI().copyPictureToClipboard();
			}
			catch (Throwable ex) {
				ex.printStackTrace();
				
				final String message = MessageFormat.format("Cannot copy photo to clipboard.{0}{0}{1}",
						PortabilityUtils.getNewLine(),  ex.getMessage());
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
		}

		@Override
		public void getNotification() {
			setEnabled(photoExists());
		}
	}

	public static class CopyPictureFileAction extends Action implements IPhotoListener {
		@Override
		public String getDescription() {
			return "Copy the current photo file to a new file";
		}

		public CopyPictureFileAction() {
			super("Copy &Photo File...");
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void run() {
			try {
				Globals.getMainApplicationWindow().getPhotoAndTextUI().getPhotoUI().copyPictureFile();
			}
			catch (Throwable ex) {
				ex.printStackTrace();

				final String message = MessageFormat.format(
						"Cannot copy photo file.{0}{0}{1}",
						PortabilityUtils.getNewLine(),
						ex.getMessage());

				final MessageDialog messageDialog = new MessageDialog(
						Globals.getMainApplicationWindow().getShell(),
						StringLiterals.ProgramName,
						Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_VAULT_ICON),
						message,
						MessageDialog.ERROR,
						new String[] { "&OK" },
						0);

				messageDialog.open();
			}
		}

		@Override
		public void getNotification() {
			setEnabled(photoExists());
		}
	}

	public static class CopyPictureFilePathAction extends Action implements IPhotoListener {
		@Override
		public String getDescription() {
			return "Copy the current photo file's path to the clipboard";
		}

		public CopyPictureFilePathAction() {
			super("Copy P&hoto File Path");
			setId(HelpUtils.helpIDFromClass(this));
		}
		
		public void run() {
			Clipboard clipboard = null;
			
			try {
				clipboard = new Clipboard(Display.getCurrent());
				final TextTransfer textTransfer = TextTransfer.getInstance();
				clipboard.setContents(new Object[] { PhotoUI.getCurrentImagePath() }, new Transfer[] { textTransfer });
			}
			catch (Throwable ex) {
				ex.printStackTrace();

				final String message = MessageFormat.format(
						"Cannot copy photo file path.{0}{0}{1}",
						PortabilityUtils.getNewLine(),
						ex.getMessage());

				final MessageDialog messageDialog = new MessageDialog(
						Globals.getMainApplicationWindow().getShell(),
						StringLiterals.ProgramName,
						Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_VAULT_ICON),
						message,
						MessageDialog.ERROR,
						new String[] { "&OK" },
						0);

				messageDialog.open();
			}
			finally {
				if (clipboard != null) {
					clipboard.dispose();
				}
			}
		}

		@Override
		public void getNotification() {
			setEnabled(photoExists());
		}
	}
}
