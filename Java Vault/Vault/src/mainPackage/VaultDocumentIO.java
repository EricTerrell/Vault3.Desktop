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
import java.sql.DriverManager;
import java.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import commonCode.DocumentMetadata;
import commonCode.VaultDocumentVersion;

public class VaultDocumentIO {
	private static String xmlExportPreviousFolder = System.getProperty("user.home");
	private static String fileSavePreviousFolder = System.getProperty("user.home");
	private static String previousFileOpenFolder = null;

	public static String fileOpen(Shell shell, StringWrapper filePathStringWrapper) throws Throwable {
		String filePath = null;

		final FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
		fileDialog.setText("Open");
		String vault3files = MessageFormat.format("{0} Files", StringLiterals.ProgramName);
		
		fileDialog.setFilterNames(new String[] { vault3files, "All Files" });
		fileDialog.setFilterExtensions(new String[] { StringLiterals.ProgramFileTypeWildcardedCaseInsensitive, StringLiterals.Wildcard });
		
		fileDialog.setFilterPath(previousFileOpenFolder);
		
		boolean finished = false;
		
		do {
			filePath = fileDialog.open();
			
			if (filePath != null && new File(filePath).exists()) {
				filePathStringWrapper.setValue(filePath);

				fileOpen(shell, filePath);

				final File file = new File(filePath);
				
				previousFileOpenFolder = file.getParent();
				
				finished = true;
			}
			else if (filePath == null) {
				finished = true;
			}
		} while (!finished);
		
		return filePath;
	}

	public static void fileSaveAs(Shell shell, String filePath, boolean export) throws Throwable {
		Globals.getLogger().info(String.format("Saving file %s", filePath));

		Globals.getVaultTextViewer().saveChanges();
		
		if (filePath == null || Globals.getVaultDocument().hasDefaultFilePath()) {
			final FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
			
			if (export) {
				fileDialog.setFilterNames(new String[] { "XML Files", "All Files" });
				fileDialog.setFilterExtensions(new String[] { StringLiterals.XMLFileTypeWildcardedCaseInsensitive, StringLiterals.Wildcard });
				fileDialog.setFilterPath(xmlExportPreviousFolder);
			}
			else {
				String vault3Files = MessageFormat.format("{0} Files", StringLiterals.ProgramName);
				
				fileDialog.setFilterNames(new String[] { vault3Files, "All Files" });
				fileDialog.setFilterExtensions(new String[] { StringLiterals.ProgramFileTypeWildcardedCaseInsensitive, StringLiterals.Wildcard });
				fileDialog.setFilterPath(fileSavePreviousFolder);
			}

			// Enable overwrite warning.
			fileDialog.setOverwrite(true);

			if (export) {
				fileDialog.setText("Export");
			}
			else {
				fileDialog.setText("Save");
			}
			
			filePath = fileDialog.open();
		}

		Globals.getLogger().info("start saving file");

		if (filePath != null) {
			try {
				Globals.setBusyCursor();
				
				if (!export) {
					VaultDocument.saveOldFileWithBakType(filePath);
				}
				
				if (export) {
					Globals.getVaultDocument().saveAsXMLFile(filePath);
				}
				else {
					Globals.getVaultDocument().saveAsSQLiteFile(filePath);
					Globals.getVaultDocument().setDocumentMetadata(new DocumentMetadata(filePath));
				}

				final String folderPath = new File(filePath).getParent();
				
				if (export) {
					xmlExportPreviousFolder = folderPath; 
				}
				else {
					fileSavePreviousFolder = folderPath;
				}
			}
			finally {
				Globals.setPreviousCursor();
			}
		}
		
		Globals.getLogger().info("finished saving file");
	}
	
	public static void fileSaveAs(Shell shell) throws Throwable {
		fileSaveAs(shell, null, false);
	}

	public static void fileSave(Shell shell) throws Throwable {
		fileSaveAs(shell, Globals.getVaultDocument().getFilePath(), false);
	}

	/**
	 * Attempt to open the specified Vault 3 file. If successful, the file becomes the current Vault 3 document.
	 * @param shell Shell
	 * @param filePath path of Vault 3 file
	 * @throws Throwable
	 */
	public static void fileOpen(Shell shell, String filePath) throws Throwable {
		// Do a File / New in case load fails for any reason.
		VaultDocumentUtils.fileNew();

		final StringWrapper password = new StringWrapper();
		password.setValue(Globals.getPasswordCache().get(filePath));
		
		OutlineItem outlineItem;

		final VaultDocument vaultDocument = new VaultDocument();

		if (VaultDocument.isDatabase(filePath)) {
			final String dbURL = VaultDocument.getDBURL(filePath);

			Globals.getLogger().info(String.format("About to open database: \"%s\"", dbURL));

			try (final Connection db = DriverManager.getConnection(dbURL)) {
				outlineItem = vaultDocument.loadFromDatabase(db, filePath, password);

				final VaultDocumentVersion originalVersion = vaultDocument.getOriginalDocumentVersion(db);
				vaultDocument.setVaultDocumentOriginalVersion(originalVersion);
			}
		}
		else {
			outlineItem = VaultDocumentXML.parseVault3File(filePath, password);
		}
		
		vaultDocument.setContent(outlineItem);

		vaultDocument.setVaultDocumentVersion(VaultDocumentVersion.getLatestVaultDocumentVersion());

		vaultDocument.setPassword(password.getValue());
		vaultDocument.setIsModified(false);
		vaultDocument.setDocumentMetadata(new DocumentMetadata(filePath));
		
		Globals.setVaultDocument(vaultDocument);
		Globals.getVaultTreeViewer().load(Globals.getVaultDocument().getContent());
		vaultDocument.setFilePath(filePath);
		Globals.getPreferenceStore().setValue(PreferenceKeys.MostRecentlyUsedFilePath, filePath);
		Globals.getMRUFiles().update(filePath, password.getValue());
	}

}
