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
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;

import commonCode.DocumentMetadata;
import commonCode.VaultDocumentVersion;
import commonCode.VaultException;

public class VaultDocument {
	private final static String VAULT_FILE_TYPE = ".vl3";

	private OutlineItem content;

	public OutlineItem getContent() {
		return content;
	}

	private String filePath;
	
	public String getFilePath() {
		return filePath;
	}
	
	private final String defaultFilePath;
	
	public boolean hasDefaultFilePath() {
		return filePathsEqual(filePath, defaultFilePath);
	}

	private boolean filePathsEqual(String filePath, String defaultFilePath) {
		return StringUtils.equals(filePath, defaultFilePath);
	}
	
	public void setFilePath(String filePath) {
		this.filePath = filePath;
		
		final String caption = String.format("%s - %s", new File(filePath).getName(), StringLiterals.ProgramName);
		
		Globals.getMainApplicationWindow().setText(caption);
	}
	
	public String getFileName() {
		return new File(filePath).getName();
	}

	private String password;
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		boolean passwordChanged = false;
		
		if (password == null) {
			passwordChanged = this.password != null;
		}
		else {
			passwordChanged = !password.equals(this.password);
		}
		
		this.password = password;
		
		if (passwordChanged) {
			isModified = true;
		}
	}
	
	public boolean isEncrypted() {
		return this.password != null && !this.password.isEmpty();
	}

	private VaultDocumentVersion vaultDocumentVersion = new VaultDocumentVersion();

	public VaultDocumentVersion getVaultDocumentVersion() {
		return vaultDocumentVersion;
	}

	public void setVaultDocumentVersion(VaultDocumentVersion vaultDocumentVersion) {
		this.vaultDocumentVersion = vaultDocumentVersion;
	}

	private VaultDocumentVersion vaultDocumentOriginalVersion = null;

	public void setVaultDocumentOriginalVersion(VaultDocumentVersion vaultDocumentVersion) {
		this.vaultDocumentOriginalVersion = vaultDocumentVersion;
	}

	public VaultDocumentVersion getVaultDocumentOriginalVersion() { return vaultDocumentOriginalVersion; }

	private boolean isModified;
	
	public boolean getIsModified() { 
		return isModified; 
	}
	
	public void setIsModified(boolean isModified) { 
		this.isModified = isModified; 
	}
	
	private DocumentMetadata documentMetadata;
	
	public DocumentMetadata getDocumentMetadata() {
		return documentMetadata;
	}

	public void setDocumentMetadata(DocumentMetadata documentMetadata) {
		this.documentMetadata = documentMetadata;
	}

	public void setContent(OutlineItem content) {
		this.content = content;
	}
	
	/**
	 * Determine if the specified file is an SQLite database
	 * @param filePath file path
	 * @return true if the file is an SQLite database
	 * @throws IOException
	 */
	public static boolean isDatabase(String filePath) throws IOException {
		boolean isDatabase = false;

		try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
			// http://www.sqlite.org/fileformat.html
			final byte[] sqliteFileMarker = new byte[] { 0x53, 0x51, 0x4c, 0x69, 0x74, 0x65, 0x20, 0x66, 0x6f, 0x72, 0x6d, 0x61, 0x74, 0x20, 0x33, 0x00 };

			final byte[] fileContents = new byte[sqliteFileMarker.length];
			
			final int bytesRead = (fileInputStream.read(fileContents));
			
			if (bytesRead == fileContents.length) {
				isDatabase = Arrays.equals(sqliteFileMarker, fileContents);
			}
		}

		return isDatabase;
	}
	
	public boolean warnAboutDocVersionUpgrade() {
		final boolean upgrade = getVaultDocumentOriginalVersion() != null &&
				getVaultDocumentOriginalVersion().compareTo(VaultDocumentVersion.getLatestVaultDocumentVersion()) < 0;

		boolean okToWrite = true;

		if (upgrade)
		{
			final Image icon = Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_VAULT_ICON);

			final String message = String.format("The current document is version %s. It will be saved as version %s.\r\n\r\nAfter saving, you will need to ensure that all Vault 3 apps that will open this document have been updated.\r\n\r\nContinue saving document?",
					getVaultDocumentOriginalVersion(), getVaultDocumentVersion());

			final MessageDialog messageDialog = new MessageDialog(Globals.getMainApplicationWindow().getShell(),
					StringLiterals.ProgramName, icon, message, MessageDialog.INFORMATION,
					new String[] { "&Yes", "&No" }, 1);

			final int userSelection = messageDialog.open();

			okToWrite = userSelection == 0;
		}

		return okToWrite;
	}

	/**
	 * Saves the current Vault document with a .bak file type if the user has chosen this option.
	 * @throws VaultException
	 */
	public static void saveOldFileWithBakType(String filePath) throws VaultException {
		if (Globals.getPreferenceStore().getBoolean(PreferenceKeys.SaveOldFileWithBakType)) {
			final File originalFile = new File(filePath);
			
			if (originalFile.exists()) {
				String bakFilePath = String.format("%s.bak", filePath);
				
				final File bakFile = new File(bakFilePath);
				
				// Remove previous .bak file if it exists.
				if (bakFile.exists()) {
					Globals.getLogger().info(String.format("saveOldFileWithBakType: deleting file %s", bakFile.getPath()));
					bakFile.delete();
				}
				
				// Rename file to {filepath}.bak
				Globals.getLogger().info(String.format("saveOldFileWithBakType: renaming file %s to %s", originalFile.getPath(), bakFile.getPath()));
				final boolean renamed = originalFile.renameTo(bakFile);
				
				if (!renamed) {
					String errorMessage = MessageFormat.format("Cannot rename {0} to {1}.", originalFile.getAbsolutePath(), bakFile.getAbsolutePath());
					throw new VaultException(errorMessage);
				}
			}
		}
	}
	
	public VaultDocument() {
		content = new OutlineItem();
		isModified = false;
		final String vaultDefaultFilename = "Untitled";

		defaultFilePath = String.format("%s%s%s%s", System.getProperty("user.home"), PortabilityUtils.getFileSeparator(), vaultDefaultFilename, VAULT_FILE_TYPE);
		setFilePath(defaultFilePath);
	}
}