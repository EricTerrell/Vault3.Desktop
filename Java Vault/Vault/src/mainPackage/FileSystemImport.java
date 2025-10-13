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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

public class FileSystemImport {
	public static OutlineItem importFromFileSystem(String rootFolderPath, Dictionary<String, Boolean> uniqueFileTypes) throws IOException {
		final OutlineItem outlineItem = new OutlineItem();
		outlineItem.setTitle(rootFolderPath);

		Dictionary<UUID, Boolean> nonEmptyOutlineItems = new Hashtable<>();
		
		importFromFileSystem(rootFolderPath, uniqueFileTypes, outlineItem, nonEmptyOutlineItems);
		
		removeEmptyOutlineItems(outlineItem, nonEmptyOutlineItems);
		
		return outlineItem;
	}

	private static void removeEmptyOutlineItems(OutlineItem outlineItem, Dictionary<UUID, Boolean> nonEmptyOutlineItems) {
		if (nonEmptyOutlineItems.get(outlineItem.getUuid()) == null) {
			final OutlineItem parent = outlineItem.getParent();
			
			if (parent != null) {
				parent.getChildren().remove(outlineItem);
			}
		}
		else {
			final List<OutlineItem> children = outlineItem.getChildren();
			
			for (int i = children.size() - 1; i >= 0; i--) {
				removeEmptyOutlineItems(children.get(i), nonEmptyOutlineItems);
			}
		}
	}
	
	private static class FileComparitor implements Comparator<File> {
		private final Collator collator = Collator.getInstance();

		@Override
		public int compare(File file1, File file2) {
			int result = collator.compare(file1.getName(), file2.getName());
			
			// Folders sort before files.
			if (file1.isDirectory() != file2.isDirectory()) {
				result = file1.isDirectory() ? -1 : 1;
			}
			
			return result;
		}
	}

	private static void importFromFileSystem(String rootFolderPath, Dictionary<String, Boolean> uniqueFileTypes, OutlineItem outlineItem, Dictionary<UUID, Boolean> nonEmptyOutlineItems) throws IOException {
		final File rootFolder = new File(rootFolderPath);
		
		if (rootFolder.isDirectory()) {
			File[] list = rootFolder.listFiles();
			
			if (list != null && list.length > 0) {
				Arrays.sort(list, new FileComparitor());

				for (File child : list) {
					if (child.isFile()) {
						String fileType = FileUtils.getFileType(child);
						
						if (fileType != null) {
							fileType = fileType.toLowerCase();
						} else {
							fileType = StringLiterals.EmptyString;
						}

						final Boolean importFileType = uniqueFileTypes.get(fileType);

						if (importFileType != null && importFileType) {
							OutlineItem childOutlineItem = new OutlineItem();
							childOutlineItem.setParent(outlineItem);
							childOutlineItem.setTitle(child.getName());

							boolean addItem = false;

							boolean fileIsPhoto = PhotoUtils.isPhotoFile(child);

							if (fileIsPhoto) {
								childOutlineItem.setPhotoPath(child.getAbsolutePath());
								addItem = true;
							}
							else if (VideoUtils.isVideoFile(child)) {
								try {
									String filePathUrl = URLEncoder.encode(child.getAbsolutePath(), "UTF-8");
									String videoUrl = String.format("file:///%s", filePathUrl);
									childOutlineItem.setText(videoUrl);
									addItem = true;
								} catch (UnsupportedEncodingException e) {
									e.printStackTrace();
								}
							}
							else {
								// Read file contents and add to current outline item.
								String fileText = FileUtils.readFile(child.getAbsolutePath());

								if (fileText != null) {
									childOutlineItem.setText(fileText);

									addItem = true;
								}
							}

							if (addItem) {
								outlineItem.addChild(childOutlineItem);

								OutlineItem currentOutlineItem = childOutlineItem;

								while (currentOutlineItem != null) {
									nonEmptyOutlineItems.put(currentOutlineItem.getUuid(), Boolean.TRUE);
									currentOutlineItem = currentOutlineItem.getParent();
								}
							}
						}
					}
					else if (child.isDirectory()) {
						final OutlineItem childOutlineItem = new OutlineItem();
						childOutlineItem.setParent(outlineItem);
						childOutlineItem.setTitle(child.getName());
						outlineItem.addChild(childOutlineItem);
						
						importFromFileSystem(child.getAbsolutePath(), uniqueFileTypes, childOutlineItem, nonEmptyOutlineItems);
					}
				}
			}
		}
	}
}
