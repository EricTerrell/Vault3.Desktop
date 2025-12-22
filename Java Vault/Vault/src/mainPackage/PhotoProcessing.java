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
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class PhotoProcessing {
	private static String copyPictureFilePreviousFolder = System.getProperty("user.home");
	private static String renamePictureFilePreviousFolder = System.getProperty("user.home");

	private static boolean selectedItemHasPictureFile() {
		List<OutlineItem> selectedItems = Globals.getVaultTreeViewer().getSelectedItems();

		boolean selectedItemHasPictureFile = false;
		
		if (selectedItems.size() == 1 && selectedItems.getFirst().getPhotoPath() != null && !selectedItems.get(0).getPhotoPath().trim().isEmpty()) {
			String photoPath = selectedItems.getFirst().getPhotoPath();
			photoPath = PhotoUtils.getPhotoPath(photoPath);
			
			if (photoPath != null) {
				final File file = new File(photoPath);
				
				selectedItemHasPictureFile = file.exists();
			}
		}
		
		return selectedItemHasPictureFile;
	}
	
	public static String selectedItemPhotoPath() {
		final List<OutlineItem> selectedItems = Globals.getVaultTreeViewer().getSelectedItems();

		return selectedItems.getFirst().getPhotoPath();
	}

	public static boolean canCopyPictureFile() {
		return selectedItemHasPictureFile();
	}
	
	public static void copyPictureFile(Shell shell) throws IOException {
		if (canCopyPictureFile()) {
			final String selectedItemPhotoPath = PhotoUtils.getPhotoPath(selectedItemPhotoPath());
			
			if (selectedItemPhotoPath != null) {
				final File selectedItemPhotoFile = new File(selectedItemPhotoPath);
				
				FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
				fileDialog.setFilterExtensions(GraphicsUtils.getFilterExtensions());
				fileDialog.setFilterNames(GraphicsUtils.getFilterNames());
				fileDialog.setFilterPath(copyPictureFilePreviousFolder);
				fileDialog.setOverwrite(true);
				fileDialog.setFileName(selectedItemPhotoFile.getName());
				
				fileDialog.setText("Copy Picture File");

				final String destFilePath = fileDialog.open();
				
				if (destFilePath != null) {
					FileUtils.copyFile(selectedItemPhotoPath, destFilePath);
					
					copyPictureFilePreviousFolder = fileDialog.getFilterPath();
				}
			}
			else {
				final String message = "Cannot copy picture file - file does not exist.";

				final Image icon = Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_VAULT_ICON);
				
				MessageDialog messageDialog = new MessageDialog(shell, StringLiterals.ProgramName, icon, message, MessageDialog.ERROR, new String[] { "&OK" }, 0);
				messageDialog.open();
			}
		}
	}

	public static boolean canEditPictureFile() {
		final String photoEditorPath = Globals.getPreferenceStore().getString(PreferenceKeys.PhotoEditingProgramPath).trim();

		return !photoEditorPath.isEmpty() && selectedItemHasPictureFile();
	}

	public static void editPictureFile() {
		if (canEditPictureFile()) {
			final String selectedItemPhotoPath = PhotoUtils.getPhotoPath(selectedItemPhotoPath());

			final ProcessBuilder processBuilder = new ProcessBuilder(
					Globals.getPreferenceStore().getString(PreferenceKeys.PhotoEditingProgramPath),
					selectedItemPhotoPath);

			try {
				processBuilder.start();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public static boolean canRotatePictureFile() {
		return selectedItemHasPictureFile();
	}

	public static void rotatePictureFile(float degrees) {
		if (canRotatePictureFile()) {
			final String selectedItemPhotoPath = PhotoUtils.getPhotoPath(selectedItemPhotoPath());

			try {
				GraphicsUtils.rotate(selectedItemPhotoPath, degrees);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	public static boolean canRenamePictureFile() {
		return selectedItemHasPictureFile();
	}
	
	public static void renamePictureFile(Shell shell) {
		if (canRenamePictureFile()) {
			final String selectedItemPhotoPath = PhotoUtils.getPhotoPath(selectedItemPhotoPath());

			final File selectedItemPhotoFile = new File(selectedItemPhotoPath);
			
			FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
			fileDialog.setFilterExtensions(GraphicsUtils.getFilterExtensions());
			fileDialog.setFilterNames(GraphicsUtils.getFilterNames());
			fileDialog.setFilterPath(renamePictureFilePreviousFolder);
			fileDialog.setOverwrite(true);
			fileDialog.setFileName(selectedItemPhotoFile.getName());
			
			fileDialog.setText("Rename Picture File");

			final String destFilePath = fileDialog.open();
			
			if (destFilePath != null) {
				FileUtils.renameFile(selectedItemPhotoPath, destFilePath);

				// Redraw the current photo.
				Globals.getMainApplicationWindow().getPhotoAndTextUI().getPhotoUI().setImages(selectedItemPhotoPath);
				
				renamePictureFilePreviousFolder = fileDialog.getFilterPath();
			}
		}
	}
	
	public static boolean canDeletePictureFile() {
		return selectedItemHasPictureFile();
	}
	
	public static void deletePictureFile(Shell shell) {
		if (canDeletePictureFile()) {
			final String selectedItemPhotoPath = PhotoUtils.getPhotoPath(selectedItemPhotoPath());
			
			FileUtils.deleteFile(selectedItemPhotoPath);

			// Redraw the current photo.
			Globals.getMainApplicationWindow().getPhotoAndTextUI().getPhotoUI().setImages(selectedItemPhotoPath);
		}
	}
	
	public static boolean canSlideshow() {
		return !Globals.getVaultTreeViewer().getAllTopLevelItems().isEmpty();
	}
	
	public static void slideshow(Shell shell) {
		Globals.setBusyCursor();

		List<OutlineItem> selectedPhotos, allPhotos;

		final Pattern[] exclusionPatterns = Search.getSearchPatterns(
				Globals.getPreferenceStore().getString(PreferenceKeys.PhotoExclusions),
				false,
				true);
		
		try {
			allPhotos = Globals.getVaultDocument().getContent().getPhotos(Globals.getVaultDocument().getContent(), exclusionPatterns);
			selectedPhotos = Globals.getVaultDocument().getContent().getPhotos(Globals.getVaultTreeViewer().getSelectedItems(), exclusionPatterns);
		}
		finally {
			Globals.setPreviousCursor();
		}

		if (!allPhotos.isEmpty()) {
			SlideshowDialog slideshowDisplayDialog = new SlideshowDialog(shell, allPhotos, selectedPhotos);
			slideshowDisplayDialog.open();
		}
		else {
			final Image icon = Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_VAULT_ICON);

			final MessageDialog messageDialog = new MessageDialog(Globals.getMainApplicationWindow().getShell(), StringLiterals.ProgramName, icon, "No photos to display.", MessageDialog.ERROR, new String[] { "&Close" }, 0);
			messageDialog.open();
		}
	}
}
