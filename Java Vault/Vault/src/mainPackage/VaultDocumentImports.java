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
import java.text.MessageFormat;
import java.util.Dictionary;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import mainPackage.OutlineItem.AddDirection;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.xml.sax.SAXException;

public class VaultDocumentImports {
	private static String previousImportFilterPath = null;
	private static String previousFileSystemImportFolder;
	private static String previousPhotoFilterPath = null;

	public static boolean canFolderImport() {
		return Globals.getVaultTreeViewer().canAddItem();
	}
	
	/**
	 * Import an XML file that was exported from Vault or The Photo Program
	 * @param shell Shell
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public static void legacyXMLFileImport(Shell shell) throws SAXException, IOException, ParserConfigurationException {
		if (canFileImport()) {
			final FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
			fileDialog.setFilterNames(new String[] { "Vault for Windows or The Photo Program Export File (*.xml)", "All Files (*.*)" });
			fileDialog.setFilterExtensions(new String[] { StringLiterals.XMLFileTypeWildcardedCaseInsensitive, StringLiterals.Wildcard });
			fileDialog.setFilterPath(previousImportFilterPath);
			fileDialog.setText("Import Vault for Windows or The Photo Program Export File");
			
			boolean finished = false;
			
			do {
				final String filePath = fileDialog.open();
				
				if (filePath != null && new File(filePath).exists()) {
					try {
						Globals.setBusyCursor();
						
						Globals.getLogger().info("Starting SAX parsing");

						final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

						final SAXParser saxParser = saxParserFactory.newSAXParser();

						final ImportDefaultHandler importDefaultHandler = new ImportDefaultHandler();

						// If the first argument to parse is a filename with embedded spaces, an exception will be thrown. The solution is to
						// use a FileInputStream instead of a file path.
						try (FileInputStream inputStream = new FileInputStream(filePath)) {
							saxParser.parse(inputStream, importDefaultHandler);
							
							Globals.getLogger().info("Finished SAX parsing");

							final OutlineItem importedOutlineItem = importDefaultHandler.getOutlineItem();
							importedOutlineItem.setTitle(filePath);
							Globals.getVaultTreeViewer().addItem(importDefaultHandler.getOutlineItem(), OutlineItem.AddDirection.Below);
							
							previousImportFilterPath = fileDialog.getFilterPath();
							
							finished = true;
						}
					}
					finally {
						Globals.setPreviousCursor();
					}
				}
				else if (filePath == null) {
					finished = true;
				}
			} while (!finished);
		}
	}

	/**
	 * Import text files from a filesystem folder.
	 * @param shell Shell
	 * @throws IOException
	 */
	public static void folderImport(Shell shell) throws IOException {
		if (canFolderImport()) {
			final DirectoryDialog directoryDialog = new DirectoryDialog(shell);
			directoryDialog.setText("Select Folder from which to Import");
			directoryDialog.setMessage("Folder:");
			directoryDialog.setFilterPath(previousFileSystemImportFolder);

			final String importFolder = directoryDialog.open();
			
			if (importFolder != null) {
				final Dictionary<String, Boolean> uniqueFileTypes;
				
				try {
					Globals.setBusyCursor();
					uniqueFileTypes = FileUtils.getUniqueFileTypes(importFolder);
				}
				finally {
					Globals.setPreviousCursor();
				}

				final ImportFolderDialog importFolderDialog = new ImportFolderDialog(shell, uniqueFileTypes);
	
				if (importFolderDialog.open() == IDialogConstants.OK_ID) {
					try
					{
						Globals.setBusyCursor();

						final OutlineItem outlineItem = FileSystemImport.importFromFileSystem(importFolder, uniqueFileTypes);
						
						Globals.getVaultTreeViewer().addItem(outlineItem, AddDirection.Below);
					}
					finally {
						Globals.setPreviousCursor();
					}
				}
				
				previousFileSystemImportFolder = importFolder;
			}
		}
	}

	public static boolean canFileImport() {
		return Globals.getVaultTreeViewer().canAddItem();
	}

	/**
	 * Import the specified Vault 3 file. Prompt the user for the password and decrypt if necessary.
	 * @param shell Shell
	 * @throws Exception
	 */
	public static void vault3FileImport(Shell shell) throws Throwable {
		if (canFileImport()) {
			FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
			fileDialog.setFilterNames(new String[] { MessageFormat.format("{0} XML File (*.xml)", StringLiterals.ProgramName), "All Files (*.*)" });
			fileDialog.setFilterExtensions(new String[] { StringLiterals.XMLFileTypeWildcardedCaseInsensitive, StringLiterals.Wildcard });
			fileDialog.setFilterPath(previousImportFilterPath);
			fileDialog.setText(MessageFormat.format("Import {0} File", StringLiterals.ProgramName));

			boolean finished = false;
			
			do {
				final String filePath = fileDialog.open();
				
				if (filePath != null && new File(filePath).exists()) {
					final StringWrapper password = new StringWrapper();
					
					try {
                        var importedOutlineItem = new VaultDocumentXMLPersistence()
                                .load(filePath, password)
                                .getContent();

                        importedOutlineItem.setTitle(filePath);

						Globals.getVaultTreeViewer().addItem(importedOutlineItem, OutlineItem.AddDirection.Below);
					} catch (Throwable ex) {
						final boolean processedException = DatabaseVersionTooHigh.displayMessaging(ex, filePath);

						if (!processedException) {
							throw ex;
						}
					}
					
					previousImportFilterPath = fileDialog.getFilterPath();
					
					finished = true;
				}
				else if (filePath == null) {
					finished = true;
				}
			} while (!finished);
		}
	}

	public static boolean canImportPictures() {
		final List<OutlineItem> selectedItems = Globals.getVaultTreeViewer().getSelectedItems();
		
		return selectedItems.size() <= 1;
	}

	public static void importPictures(Shell shell) {
		if (canImportPictures()) {
			final FileDialog fileDialog = new FileDialog(shell, SWT.OPEN | SWT.MULTI);
			fileDialog.setText("Import Photos");
			fileDialog.setFilterExtensions(GraphicsUtils.getFilterExtensions());
			fileDialog.setFilterNames(GraphicsUtils.getFilterNames());

			if (previousPhotoFilterPath != null) {
				fileDialog.setFilterPath(previousPhotoFilterPath);
			}

			// Need to keep trying to import, even when the user enters an invalid file path.
			boolean finished  = false;

			do
			{
				final String filePath = fileDialog.open();
				
				if (filePath != null) {
					previousPhotoFilterPath = fileDialog.getFilterPath();

					int filesAdded = 0;

					// Don't want to draw each item's image as it's added - way too slow.
					Globals.getMainApplicationWindow().getPhotoAndTextUI().getPhotoUI().setDisableDraw(true);

					try {
						for (String fileName : fileDialog.getFileNames()) {
							final String photoPath = String.format(
									"%s%s%s",
									fileDialog.getFilterPath(),
									System.getProperty("file.separator"),
									fileName);
							
							if (new File(photoPath).exists()) {
								final OutlineItem newItem = new OutlineItem();

								final File photoFile = new File(photoPath);
								
								newItem.setTitle(photoFile.getName());
								newItem.setPhotoPath(photoPath);
								
								// Just draw the last item's image.
								if (filesAdded == fileDialog.getFileNames().length - 1) {
									Globals.getMainApplicationWindow().getPhotoAndTextUI().getPhotoUI().setDisableDraw(false);
								}
								
								Globals.getVaultTreeViewer().addItem(newItem, OutlineItem.AddDirection.Below);
								filesAdded++;
							}
						}
					}
					finally {
						// Ensure that we draw images after the import is complete, regardless of any exceptions.
						Globals.getMainApplicationWindow().getPhotoAndTextUI().getPhotoUI().setDisableDraw(false);
					}
					
					if (filesAdded > 0) {
						finished = true;
					}
				}
				else {
					finished = true;
				}
			} while (!finished);
		}
	}
}
