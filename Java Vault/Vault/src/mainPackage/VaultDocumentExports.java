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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class VaultDocumentExports {
	private static String textExportPreviousFolder = System.getProperty("user.home");

	private static void addPhotoPath(OutlineItem outlineItem, List<String> photoPaths) {
		String photoPath = outlineItem.getPhotoPath();
		
		if (photoPath != null && !photoPath.isEmpty()) {
			photoPath = PhotoUtils.getPhotoPath(outlineItem.getPhotoPath());

			photoPaths.add(photoPath);
		}
	}
	
	public static StringBuilder getExportText(OutlineItem outlineItem, List<String> photoPaths) {
		final StringBuilder textToExport = new StringBuilder();

		final String newLine = PortabilityUtils.getNewLine();
		final String text = MessageFormat.format(
				"{0}{1}{1}{2}{1}{1}",
				outlineItem.getTitle(),
				newLine,
				outlineItem.getText()).replace("\n", newLine);
		
		if (photoPaths != null) {
			addPhotoPath(outlineItem, photoPaths);
		}
		
		textToExport.append(text);
		
		for (OutlineItem childItem : outlineItem.getChildren()) {
			final StringBuilder childText = getExportText(childItem, photoPaths);
			textToExport.append(childText);
		}
		
		return textToExport;
	}

	public static boolean canTextFileExport() {
		final List<OutlineItem> selectedItems = Globals.getVaultTreeViewer().getSelectedItems();
		
		return !selectedItems.isEmpty();
	}

	public static void textFileExport(Shell shell) throws IOException {
		if (canTextFileExport()) {
			Globals.getVaultTextViewer().saveChanges();

			final FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
			fileDialog.setFilterNames(new String[] { "Text Files", "All Files" });
			fileDialog.setFilterExtensions(new String[] { StringLiterals.TextFileTypeWildcardedCaseInsensitive, StringLiterals.Wildcard });
			fileDialog.setFilterPath(textExportPreviousFolder);
			
			fileDialog.setText("Export");

			final String filePath = fileDialog.open();
			
			if (filePath != null) {
				try {
					Globals.setBusyCursor();
					
					textExportPreviousFolder = new File(filePath).getParent();

					final List<OutlineItem> selectedItems = Globals.getVaultTreeViewer().getSelectedItems();

					final StringBuilder textToExport = new StringBuilder();
					
					for (OutlineItem outlineItem : selectedItems) {
						textToExport.append(getExportText(outlineItem, null));
					}

					try (FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                         OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
                         BufferedWriter writer = new BufferedWriter(outputStreamWriter)) {
						fileOutputStream.write(FileUtils.getUTF8BOM());

						writer.write(textToExport.toString());
						writer.flush();
					}
				}
				finally {
					Globals.setPreviousCursor();
				}
			}
		}
	}

	public static boolean canExportPhotosToDevice() {
		final List<OutlineItem> selectedItems = Globals.getVaultTreeViewer().getSelectedItems();
		
		return !selectedItems.isEmpty();
	}

	public static void exportPhotosToDevice(Shell shell, Point deviceDimensions, String destinationFolder, int maxPhotos, int maxPhotosPerFolder, boolean shuffle, boolean deleteDestinationFolderContents) {
		final Pattern[] exclusionPatterns = Search.getSearchPatterns(
				Globals.getPreferenceStore().getString(PreferenceKeys.SlideshowExclusions),
				false,
				true);

		final List<OutlineItem> selectedPhotos = Globals.getVaultDocument().getContent().getPhotos(Globals.getVaultTreeViewer().getSelectedItems(), exclusionPatterns);

		if (canExportPhotosToDevice()) {
			ExportPhotosToDevice.export(shell, deviceDimensions, destinationFolder, maxPhotos, maxPhotosPerFolder, shuffle, selectedPhotos, deleteDestinationFolderContents);
		}
	}

	public static boolean canXmlFileExport() {
		final List<OutlineItem> selectedItems = Globals.getVaultTreeViewer().getSelectedItems();
		
		return !selectedItems.isEmpty();
	}
	
	public static void xmlFileExport(Shell shell) throws Throwable {
		VaultDocumentIO.fileSaveAs(shell, null, true);
	}

	public static boolean canPDFFileExport() {
		List<OutlineItem> selectedItems = Globals.getVaultTreeViewer().getSelectedItems();
		
		return !selectedItems.isEmpty();
	}
	
	public static void pdfFileExport(Shell shell) {
		if (canPDFFileExport()) {
			new PDFExport().pdfFileExport(shell);
		}
	}
}
